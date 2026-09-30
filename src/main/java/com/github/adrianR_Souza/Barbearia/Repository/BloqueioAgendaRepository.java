package com.github.adrianR_Souza.Barbearia.Repository;

import com.github.adrianR_Souza.Barbearia.Model.BloqueioAgendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgendaEntity, Long> {
    List<BloqueioAgendaEntity> findByBarbeiro_IdOrderByDataHoraInicio(Long barbeiroId);

    List<BloqueioAgendaEntity> findByBarbeiro_IdAndDataHoraInicioLessThanAndDataHoraFimGreaterThan(
            Long barbeiroId, LocalDateTime fimDoPeriodo, LocalDateTime inicioDoPeriodo);
}
