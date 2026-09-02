package com.github.adrianR_Souza.Barbearia.Repository;

import com.github.adrianR_Souza.Barbearia.Model.AgendamentoEntity;
import com.github.adrianR_Souza.Barbearia.Model.AgendamentoRequest;
import com.github.adrianR_Souza.Barbearia.Model.MetodoPgto;
import com.github.adrianR_Souza.Barbearia.Model.ServicoEntity;
import com.github.adrianR_Souza.Barbearia.Model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<AgendamentoEntity, Long> {
    List<AgendamentoEntity> findByBarbeiro_IdAndDataHoraInicioBetween(Long barbeiroId, LocalDateTime inicio, LocalDateTime fim);
    List<AgendamentoEntity> findByCliente_Id(Long clienteId);
    List<AgendamentoEntity> findByStatusAndDataHoraInicioBetween(StatusAgendamento status, LocalDateTime inicio, LocalDateTime fim);
}
