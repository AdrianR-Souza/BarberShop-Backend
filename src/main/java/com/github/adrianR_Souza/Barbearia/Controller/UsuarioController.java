package com.github.adrianR_Souza.Barbearia.Controller;


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
    public UsuarioEntity cadastrar(@Valid @RequestBody UsuarioEntity usuario) {
        return usuarioService.cadastrar(usuario);

    }

    @PostMapping("/barbeiro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioEntity cadastrarBarbeiro(@Valid @RequestBody UsuarioEntity usuario) {
        return usuarioService.cadastrarBarbeiro(usuario);
    }

    @GetMapping("/{id}")
    public UsuarioEntity buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @PutMapping("/{id}")

    public UsuarioEntity atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioEntity usuario) {
        return usuarioService.atualizar(id, usuario);
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