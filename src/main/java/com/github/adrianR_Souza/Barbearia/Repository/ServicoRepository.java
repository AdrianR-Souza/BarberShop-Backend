package com.github.adrianR_Souza.Barbearia.Repository;

import com.github.adrianR_Souza.Barbearia.Model.AgendamentoRequest;
import com.github.adrianR_Souza.Barbearia.Model.Horario;
import com.github.adrianR_Souza.Barbearia.Model.ServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<ServicoEntity, Long> {
    Optional<ServicoEntity> findById(Long id);
}
