package com.github.adrianR_Souza.Barbearia.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "agendamentos")
public class AgendamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private UsuarioEntity barbeiro;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private UsuarioEntity cliente;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    private ServicoEntity servico;

    @NotNull
    private LocalDateTime dataHoraInicio;


    @NotNull
    private LocalDateTime dataHoraFim;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;

    //preenchidos só quando quem agenda marca horário pra outra pessoa
    private String nomeTerceiro;

    private Integer idadeTerceiro;
}
