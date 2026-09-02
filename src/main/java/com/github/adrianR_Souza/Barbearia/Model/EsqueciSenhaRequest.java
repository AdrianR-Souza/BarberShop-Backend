package com.github.adrianR_Souza.Barbearia.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EsqueciSenhaRequest {

    @NotBlank
    @Email
    private String email;
}
