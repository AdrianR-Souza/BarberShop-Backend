package com.github.adrianR_Souza.Barbearia.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RedefinirSenhaPorCpfRequest {

    @NotBlank
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^\\d{11}$")
    private String cpf;

    @NotBlank
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^\\d{11}$")
    private String telefone;

    @NotBlank
    @Size(min = 8, max = 100)
    private String novaSenha;
}
