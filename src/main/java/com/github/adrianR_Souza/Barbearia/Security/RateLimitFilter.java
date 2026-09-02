package com.github.adrianR_Souza.Barbearia.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final double CAPACIDADE_SENSIVEL = 5;
    private static final double TOKENS_POR_SEGUNDO_SENSIVEL = 5.0 / 60.0;

    private static final double CAPACIDADE_GERAL = 60;
    private static final double TOKENS_POR_SEGUNDO_GERAL = 60.0 / 60.0;

    private static final long OCIOSO_PARA_LIMPEZA_MILLIS = TimeUnit.MINUTES.toMillis(10);

    private final ConcurrentHashMap<String, TokenBucket> baldesPorChave = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        ScheduledExecutorService limpeza = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        });
        limpeza.scheduleAtFixedRate(this::limparBaldesOciosos, 10, 10, TimeUnit.MINUTES);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String categoria = categoriaDaRota(request);
        String chave = obterIp(request) + ":" + categoria;

        TokenBucket balde = baldesPorChave.computeIfAbsent(chave, k -> criarBalde(categoria));

        if (!balde.tentarConsumir()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"mensagem\":\"Muitas requisições em pouco tempo. Tente novamente em instantes.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private TokenBucket criarBalde(String categoria) {
        return "sensivel".equals(categoria)
                ? new TokenBucket(CAPACIDADE_SENSIVEL, TOKENS_POR_SEGUNDO_SENSIVEL)
                : new TokenBucket(CAPACIDADE_GERAL, TOKENS_POR_SEGUNDO_GERAL);
    }

    private String categoriaDaRota(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean isCadastro = "/usuarios".equals(path) && "POST".equalsIgnoreCase(request.getMethod());

        if (path.startsWith("/login") || path.startsWith("/senha/") || isCadastro) {
            return "sensivel";
        }
        return "geral";
    }

    private String obterIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void limparBaldesOciosos() {
        baldesPorChave.entrySet().removeIf(
                entry -> entry.getValue().millisDesdeUltimoAcesso() > OCIOSO_PARA_LIMPEZA_MILLIS
        );
    }
}
