package com.github.adrianR_Souza.Barbearia.Model;

import com.github.adrianR_Souza.Barbearia.Validation.CPF;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioCadastroRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String nome;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^\\d{11}$")
    @CPF
    private String cpf;

    @NotBlank
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^\\d{11}$")
    private String telefone;

    @NotBlank
    @Size(min = 8, max = 100)
    private String senha;
}
