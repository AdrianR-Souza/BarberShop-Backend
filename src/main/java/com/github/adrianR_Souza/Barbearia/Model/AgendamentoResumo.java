package com.github.adrianR_Souza.Barbearia.Model;

import java.time.LocalDateTime;


public record AgendamentoResumo(String barbeiro, String cliente, String servico, Enum StatusAgendamento, LocalDateTime inicio, LocalDateTime fim) {
}
