package com.github.adrianR_Souza.Barbearia.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "servicos")
public class ServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    @Size(min = 2, max = 100)
    private String nomeServico;

    @NotNull
    @Positive //deve ser maior que 0
    private Integer duracaoServico;

    @Positive
    @NotNull
    private BigDecimal precoServico;
}
