package com.github.adrianR_Souza.Barbearia.Service;

import com.github.adrianR_Souza.Barbearia.Exception.DadosNaoConferemException;
import com.github.adrianR_Souza.Barbearia.Exception.TokenInvalidoException;
import com.github.adrianR_Souza.Barbearia.Model.PasswordResetTokenEntity;
import com.github.adrianR_Souza.Barbearia.Model.UsuarioEntity;
import com.github.adrianR_Souza.Barbearia.Repository.PasswordResetTokenRepository;
import com.github.adrianR_Souza.Barbearia.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService service;

    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(usuarioRepository, tokenRepository, passwordEncoder);

        usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setEmail("cliente@example.com");
        usuario.setSenha("hashAntigo");
        usuario.setCpf("11122233344");
        usuario.setTelefone("44999998888");
    }

    @Test
    void solicitarRecuperacao_quandoEmailExiste_deveCriarToken() {
        when(usuarioRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(usuario));

        service.solicitarRecuperacao("cliente@example.com");

        ArgumentCaptor<PasswordResetTokenEntity> captor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(tokenRepository).save(captor.capture());

        PasswordResetTokenEntity tokenSalvo = captor.getValue();
        assertThat(tokenSalvo.getUsuario()).isEqualTo(usuario);
        assertThat(tokenSalvo.getToken()).isNotBlank();
        assertThat(tokenSalvo.isUsado()).isFalse();
        assertThat(tokenSalvo.getDataExpiracao()).isAfter(LocalDateTime.now());
    }

    @Test
    void solicitarRecuperacao_quandoEmailNaoExiste_naoDeveCriarToken() {
        when(usuarioRepository.findByEmail("fantasma@example.com")).thenReturn(Optional.empty());

        service.solicitarRecuperacao("fantasma@example.com");

        verify(tokenRepository, never()).save(any());
    }

    @Test
    void redefinirSenha_comTokenValido_deveAtualizarSenhaEMarcarTokenComoUsado() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setToken("token-valido");
        token.setUsuario(usuario);
        token.setDataExpiracao(LocalDateTime.now().plusMinutes(10));
        token.setUsado(false);

        when(tokenRepository.findByToken("token-valido")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hashNovo");

        service.redefinirSenha("token-valido", "novaSenha123");

        assertThat(usuario.getSenha()).isEqualTo("hashNovo");
        assertThat(token.isUsado()).isTrue();
        verify(usuarioRepository).save(usuario);
        verify(tokenRepository).save(token);
    }

    @Test
    void redefinirSenha_comTokenInexistente_deveLancarTokenInvalidoException() {
        when(tokenRepository.findByToken("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.redefinirSenha("nao-existe", "novaSenha123"))
                .isInstanceOf(TokenInvalidoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void redefinirSenha_comTokenJaUsado_deveLancarTokenInvalidoException() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setToken("token-usado");
        token.setUsuario(usuario);
        token.setDataExpiracao(LocalDateTime.now().plusMinutes(10));
        token.setUsado(true);

        when(tokenRepository.findByToken("token-usado")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.redefinirSenha("token-usado", "novaSenha123"))
                .isInstanceOf(TokenInvalidoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void redefinirSenha_comTokenExpirado_deveLancarTokenInvalidoException() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setToken("token-expirado");
        token.setUsuario(usuario);
        token.setDataExpiracao(LocalDateTime.now().minusMinutes(1));
        token.setUsado(false);

        when(tokenRepository.findByToken("token-expirado")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.redefinirSenha("token-expirado", "novaSenha123"))
                .isInstanceOf(TokenInvalidoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void redefinirSenhaPorCpfETelefone_quandoCpfETelefoneBatem_deveAtualizarSenha() {
        when(usuarioRepository.findByCpf("11122233344")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hashNovo");

        service.redefinirSenhaPorCpfETelefone("11122233344", "44999998888", "novaSenha123");

        assertThat(usuario.getSenha()).isEqualTo("hashNovo");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void redefinirSenhaPorCpfETelefone_quandoCpfNaoExiste_deveLancarDadosNaoConferemException() {
        when(usuarioRepository.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.redefinirSenhaPorCpfETelefone("00000000000", "44999998888", "novaSenha123"))
                .isInstanceOf(DadosNaoConferemException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void redefinirSenhaPorCpfETelefone_quandoTelefoneNaoBate_deveLancarDadosNaoConferemExceptionENaoSalvar() {
        when(usuarioRepository.findByCpf("11122233344")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.redefinirSenhaPorCpfETelefone("11122233344", "00000000000", "novaSenha123"))
                .isInstanceOf(DadosNaoConferemException.class);

        verify(usuarioRepository, never()).save(any());
    }
}
