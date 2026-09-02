package com.github.adrianR_Souza.Barbearia.Service;

import com.github.adrianR_Souza.Barbearia.Model.Role;
import com.github.adrianR_Souza.Barbearia.Model.UsuarioEntity;
import com.github.adrianR_Souza.Barbearia.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Validator validator;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarioRepository, validator, passwordEncoder);
    }

    @Test
    void cadastrar_forcaRoleClienteMesmoQuandoOutraRoleForEnviada() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setSenha("senha12345");
        usuario.setRole(Role.ROLE_MASTER);

        when(passwordEncoder.encode("senha12345")).thenReturn("hash");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioEntity resultado = service.cadastrar(usuario);

        assertThat(resultado.getRole()).isEqualTo(Role.ROLE_CLIENTE);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(validator).validar(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ROLE_CLIENTE);
    }

    @Test
    void cadastrarBarbeiro_forcaRoleBarbeiroMesmoQuandoOutraRoleForEnviada() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setSenha("senha12345");
        usuario.setRole(Role.ROLE_CLIENTE);

        when(passwordEncoder.encode("senha12345")).thenReturn("hash");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioEntity resultado = service.cadastrarBarbeiro(usuario);

        assertThat(resultado.getRole()).isEqualTo(Role.ROLE_BARBEIRO);
    }
}
