package com.github.adrianR_Souza.Barbearia.Config;

import com.github.adrianR_Souza.Barbearia.Model.Horario;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

    @Component
    public class HorarioFuncionamentoConfig {
        private final Map<DayOfWeek, Horario> horarios = Map.of(
                DayOfWeek.TUESDAY, new Horario(LocalTime.of(8, 0), LocalTime.of(21, 0)),
                DayOfWeek.WEDNESDAY, new Horario(LocalTime.of(8, 0), LocalTime.of(21, 0)),
                DayOfWeek.THURSDAY, new Horario(LocalTime.of(8, 0), LocalTime.of(21, 0)),
                DayOfWeek.FRIDAY, new Horario(LocalTime.of(8, 0), LocalTime.of(21, 0)),
                DayOfWeek.SATURDAY, new Horario(LocalTime.of(8, 0), LocalTime.of(18, 0))
        );

        //domingo e segunda devolve null direto
        public Optional<Horario> getHorario (DayOfWeek dia){
            return Optional.ofNullable(horarios.get(dia));
        }
    }


