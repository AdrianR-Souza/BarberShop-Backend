package com.github.adrianR_Souza.Barbearia.Model;

import java.time.LocalTime;

public record Horario(LocalTime abertura, LocalTime fechamento) {
}