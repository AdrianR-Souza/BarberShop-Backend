package com.github.adrianR_Souza.Barbearia.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

//periodo em que um barbeiro fica indisponivel pra agendamento (ferias, folga,
//compromisso pessoal etc). Nao afeta agendamentos ja marcados, so impede
//novos agendamentos dentro do periodo.
@Data
@Entity
@Table(name = "bloqueios_agenda")
public class BloqueioAgendaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private UsuarioEntity barbeiro;

    @NotNull
    private LocalDateTime dataHoraInicio;

    @NotNull
    private LocalDateTime dataHoraFim;

    private String motivo;
}
