package com.github.adrianR_Souza.Barbearia.Model;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AgendamentoRequest {
    private Long barbeiroId;
    private Long servicoId;
    private LocalDateTime dataHoraInicio;

    //preenchidos só quando o agendamento é pra outra pessoa (não pra quem está logado)
    @Size(min = 2, max = 100)
    private String nomeTerceiro;

    @Positive
    private Integer idadeTerceiro;

    public Long getBarbeiroId() { return barbeiroId; }
    public void setBarbeiroId(Long barbeiroId) { this.barbeiroId = barbeiroId; }
    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }
    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(LocalDateTime dataHoraInicio) { this.dataHoraInicio = dataHoraInicio; }
    public String getNomeTerceiro() { return nomeTerceiro; }
    public void setNomeTerceiro(String nomeTerceiro) { this.nomeTerceiro = nomeTerceiro; }
    public Integer getIdadeTerceiro() { return idadeTerceiro; }
    public void setIdadeTerceiro(Integer idadeTerceiro) { this.idadeTerceiro = idadeTerceiro; }
}