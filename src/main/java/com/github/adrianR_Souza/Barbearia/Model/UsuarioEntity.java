package com.github.adrianR_Souza.Barbearia.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.adrianR_Souza.Barbearia.Validation.CPF;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data//gera o setNome automaticamente
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String nome;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank//não pode ser vazio nem só espaços em branco (serve pra String)
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^\\d{11}$")//padrao do cpf
    @CPF
    @Column(unique = true, nullable = false)
    private String cpf;

    @NotBlank
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^\\d{11}$")
    @Column(unique = true, nullable = false)
    private String telefone;

    @NotBlank
    @Size(min = 8, max = 100)
    @Column(nullable = false)
    @JsonIgnore
    private String senha;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;



}
