package com.github.adrianR_Souza.Barbearia.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ResumoServico {
    private String nomeServico;
    private long quantidade;
    private BigDecimal valorTotal;
}
