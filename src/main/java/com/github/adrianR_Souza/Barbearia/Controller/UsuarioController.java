package com.github.adrianR_Souza.Barbearia.Controller;


import com.github.adrianR_Souza.Barbearia.Model.UsuarioCadastroRequest;
import com.github.adrianR_Souza.Barbearia.Model.UsuarioEntity;
import com.github.adrianR_Souza.Barbearia.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioEntity cadastrar(@Valid @RequestBody UsuarioCadastroRequest request) {
        return usuarioService.cadastrar(paraEntidade(request));
    }

    @PostMapping("/barbeiro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioEntity cadastrarBarbeiro(@Valid @RequestBody UsuarioCadastroRequest request) {
        return usuarioService.cadastrarBarbeiro(paraEntidade(request));
    }

    @GetMapping("/{id}")
    public UsuarioEntity buscarPorId(@PathVariable Long id) {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.buscarPorId(id, emailLogado);
    }

    @PutMapping("/{id}")
    public UsuarioEntity atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioCadastroRequest request) {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.atualizar(id, paraEntidade(request), emailLogado);
    }

    private UsuarioEntity paraEntidade(UsuarioCadastroRequest request) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setCpf(request.getCpf());
        usuario.setTelefone(request.getTelefone());
        usuario.setSenha(request.getSenha());
        return usuario;
    }

    @GetMapping("/barbeiros")
    public List<UsuarioEntity> buscarBarbeiro() {
        return usuarioService.listarBarbeiros();
    }

    @GetMapping("/clientes")
    public List<UsuarioEntity> buscarCliente() {
        return usuarioService.listarClientes();
    }

    @GetMapping("/me")
    public UsuarioEntity meuPerfil() {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.buscarPorEmail(emailLogado);
    }

}