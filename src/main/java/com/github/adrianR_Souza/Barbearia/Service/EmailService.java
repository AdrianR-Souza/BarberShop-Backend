package com.github.adrianR_Souza.Barbearia.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.adrianR_Souza.Barbearia.Model.AgendamentoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

//envia por HTTP (API do Brevo) em vez de SMTP direto -- o Railway bloqueia
//conexao de saida nas portas de SMTP (25/465/587), entao um JavaMailSender
//normal nunca conseguiria conectar; a API roda por cima de HTTPS (porta 443,
//nunca bloqueada) e resolve isso.
@Slf4j
@Service
public class EmailService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final String URL_BREVO = "https://api.brevo.com/v3/smtp/email";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${brevo.api-key:}")
    private String apiKey;

    @Value("${brevo.remetente-email:}")
    private String remetenteEmail;

    @Value("${brevo.remetente-nome:Barber Trindade}")
    private String remetenteNome;

    @Async
    public void notificarNovoAgendamento(AgendamentoEntity agendamento) {
        if (apiKey == null || apiKey.isBlank() || remetenteEmail == null || remetenteEmail.isBlank()) {
            log.warn("Envio de e-mail não configurado (falta BREVO_API_KEY ou BREVO_REMETENTE_EMAIL) -- notificação pulada.");
            return;
        }

        try {
            String nomeAtendido = agendamento.getNomeTerceiro() != null
                    ? agendamento.getNomeTerceiro()
                    : agendamento.getCliente().getNome();

            String corpo = """
                    <p>Você tem um novo agendamento!</p>
                    <p><b>Cliente:</b> %s<br>
                    <b>Serviço:</b> %s<br>
                    <b>Data e hora:</b> %s<br>
                    <b>Marcado por:</b> %s (telefone: %s)</p>
                    <p>Acesse o painel do barbeiro pra confirmar.</p>
                    """.formatted(
                    nomeAtendido,
                    agendamento.getServico().getNomeServico(),
                    agendamento.getDataHoraInicio().format(FORMATO_DATA),
                    agendamento.getCliente().getNome(),
                    agendamento.getCliente().getTelefone()
            );

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", remetenteNome, "email", remetenteEmail),
                    "to", List.of(Map.of("email", agendamento.getBarbeiro().getEmail(), "name", agendamento.getBarbeiro().getNome())),
                    "subject", "Novo agendamento - Barber Trindade",
                    "htmlContent", corpo
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_BREVO))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                log.warn("Falha ao enviar e-mail via Brevo (status {}) pro agendamento {}: {}",
                        response.statusCode(), agendamento.getId(), response.body());
            }
        } catch (Exception e) {
            log.warn("Não foi possível enviar o e-mail de notificação do agendamento {}: {}", agendamento.getId(), e.getMessage());
        }
    }
}
