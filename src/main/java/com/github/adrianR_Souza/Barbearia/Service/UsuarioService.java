package com.github.adrianR_Souza.Barbearia.Service;


import com.github.adrianR_Souza.Barbearia.Exception.AcessoNegadoException;
import com.github.adrianR_Souza.Barbearia.Exception.RecursoNotFoundException;
import com.github.adrianR_Souza.Barbearia.Model.Role;
import com.github.adrianR_Souza.Barbearia.Model.UsuarioEntity;
import com.github.adrianR_Souza.Barbearia.Repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


//Ele é o gerente da balada: decide a ordem das coisas ("primeiro checa quem pode entrar, depois libera a entrada, depois manda pro caixa"), fala com os outros setores (segurança, caixa, bar), e devolve um resultado final pra quem pediu (o Controller).
@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, Validator validator, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioEntity cadastrar(UsuarioEntity usuario) {
        usuario.setRole(Role.ROLE_CLIENTE);
        validator.validar(usuario);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    public UsuarioEntity cadastrarBarbeiro(UsuarioEntity usuario) {
        usuario.setRole(Role.ROLE_BARBEIRO);
        validator.validar(usuario);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    public UsuarioEntity buscarPorId(Long id){
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNotFoundException("Usuário não encontrado"));
    }

    public UsuarioEntity buscarPorId(Long id, String emailLogado){
        UsuarioEntity usuario = buscarPorId(id);
        UsuarioEntity solicitante = buscarPorEmail(emailLogado);

        boolean donoDaConta = usuario.getEmail().equals(emailLogado);
        if (!donoDaConta && solicitante.getRole() != Role.ROLE_MASTER) {
            throw new AcessoNegadoException("Você não tem permissão para ver esse usuário.");
        }

        return usuario;
    }

    public UsuarioEntity atualizar(Long id, UsuarioEntity novoUsuario, String emailLogado){
        UsuarioEntity usuarioExistente = buscarPorId(id);
        UsuarioEntity solicitante = buscarPorEmail(emailLogado);

        boolean donoDaConta = usuarioExistente.getEmail().equals(emailLogado);
        if (!donoDaConta && solicitante.getRole() != Role.ROLE_MASTER) {
            throw new AcessoNegadoException("Você não tem permissão para alterar esse usuário.");
        }

        usuarioExistente.setNome(novoUsuario.getNome());
        usuarioExistente.setTelefone(novoUsuario.getTelefone());
        usuarioExistente.setCpf(novoUsuario.getCpf());
        usuarioExistente.setEmail(novoUsuario.getEmail());
        usuarioExistente.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));

        return usuarioRepository.save(usuarioExistente);
    }

    public List<UsuarioEntity> listarBarbeiros(){
        return usuarioRepository.findByRole(Role.ROLE_BARBEIRO);
    }

    public UsuarioEntity buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNotFoundException("Usuário não encontrado"));
    }

    public List<UsuarioEntity> listarClientes(){
        return usuarioRepository.findByRole(Role.ROLE_CLIENTE);
    }




}