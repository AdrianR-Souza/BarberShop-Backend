package com.github.adrianR_Souza.Barbearia.Service;

import com.github.adrianR_Souza.Barbearia.Exception.DadosNaoConferemException;
import com.github.adrianR_Souza.Barbearia.Exception.TokenInvalidoException;
import com.github.adrianR_Souza.Barbearia.Model.PasswordResetTokenEntity;
import com.github.adrianR_Souza.Barbearia.Model.UsuarioEntity;
import com.github.adrianR_Souza.Barbearia.Repository.PasswordResetTokenRepository;
import com.github.adrianR_Souza.Barbearia.Repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PasswordResetService {

    private static final long EXPIRACAO_MINUTOS = 30;

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UsuarioRepository usuarioRepository, PasswordResetTokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void solicitarRecuperacao(String email) {
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            log.info("Recuperação de senha pedida para email não cadastrado: {}", email);
            return;
        }

        UsuarioEntity usuario = usuarioOpt.get();

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUsuario(usuario);
        resetToken.setDataExpiracao(LocalDateTime.now().plusMinutes(EXPIRACAO_MINUTOS));
        tokenRepository.save(resetToken);

        log.info("Token de redefinição de senha para {}: {} (expira em {} min)",
                usuario.getEmail(), resetToken.getToken(), EXPIRACAO_MINUTOS);
    }

    public void redefinirSenha(String token, String novaSenha) {
        PasswordResetTokenEntity resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenInvalidoException("Token inválido ou expirado"));

        if (resetToken.isUsado()) {
            throw new TokenInvalidoException("Token inválido ou expirado");
        }
        if (resetToken.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoException("Token inválido ou expirado");
        }

        UsuarioEntity usuario = resetToken.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        tokenRepository.save(resetToken);
    }

    public void redefinirSenhaPorCpfETelefone(String cpf, String telefone, String novaSenha) {
        UsuarioEntity usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new DadosNaoConferemException("CPF ou telefone não conferem com nenhum cadastro."));

        if (!usuario.getTelefone().equals(telefone)) {
            throw new DadosNaoConferemException("CPF ou telefone não conferem com nenhum cadastro.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}
