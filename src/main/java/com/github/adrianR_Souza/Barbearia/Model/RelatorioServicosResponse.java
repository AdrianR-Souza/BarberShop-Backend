package com.github.adrianR_Souza.Barbearia.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class RelatorioServicosResponse {
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private long totalConcluidos;
    private BigDecimal valorTotal;
    private List<ResumoServico> porServico;
    private List<ResumoBarbeiro> porBarbeiro;
}
