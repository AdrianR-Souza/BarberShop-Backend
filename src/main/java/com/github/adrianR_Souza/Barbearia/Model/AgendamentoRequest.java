package com.github.adrianR_Souza.Barbearia.Model;

import java.time.LocalDateTime;

public class AgendamentoRequest {
    private Long clienteId;
    private Long barbeiroId;
    private Long servicoId;
    private LocalDateTime dataHoraInicio;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getBarbeiroId() { return barbeiroId; }
    public void setBarbeiroId(Long barbeiroId) { this.barbeiroId = barbeiroId; }
    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }
    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(LocalDateTime dataHoraInicio) { this.dataHoraInicio = dataHoraInicio; }
}