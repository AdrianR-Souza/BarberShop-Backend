package com.github.adrianR_Souza.Barbearia.Repository;

import com.github.adrianR_Souza.Barbearia.Model.Role;
import com.github.adrianR_Souza.Barbearia.Model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Boolean existsByEmail(String email) ;
    Boolean existsByCpf(String cpf);
    Boolean existsByTelefone(String telefone);
    Optional<UsuarioEntity> findByEmail(String email);
    Optional<UsuarioEntity> findByCpf(String cpf);
    List<UsuarioEntity> findByRole(Role role);
    List<UsuarioEntity> findByRoleOrAtendeComoBarbeiroTrue(Role role);



}

