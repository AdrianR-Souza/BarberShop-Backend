package com.github.adrianR_Souza.Barbearia.Controller;

import com.github.adrianR_Souza.Barbearia.Model.EsqueciSenhaRequest;
import com.github.adrianR_Souza.Barbearia.Model.RedefinirSenhaPorCpfRequest;
import com.github.adrianR_Souza.Barbearia.Model.RedefinirSenhaRequest;
import com.github.adrianR_Souza.Barbearia.Service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/senha")
public class SenhaController {

    private final PasswordResetService passwordResetService;

    public SenhaController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/esqueci")
    public Map<String, String> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        passwordResetService.solicitarRecuperacao(request.getEmail());
        return Map.of("mensagem", "Se esse email estiver cadastrado, enviamos as instruções de redefinição.");
    }

    @PostMapping("/redefinir")
    public Map<String, String> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        passwordResetService.redefinirSenha(request.getToken(), request.getNovaSenha());
        return Map.of("mensagem", "Senha redefinida com sucesso.");
    }

    @PostMapping("/redefinir-por-cpf")
    public Map<String, String> redefinirSenhaPorCpf(@Valid @RequestBody RedefinirSenhaPorCpfRequest request) {
        passwordResetService.redefinirSenhaPorCpfETelefone(request.getCpf(), request.getTelefone(), request.getNovaSenha());
        return Map.of("mensagem", "Senha redefinida com sucesso.");
    }
}
