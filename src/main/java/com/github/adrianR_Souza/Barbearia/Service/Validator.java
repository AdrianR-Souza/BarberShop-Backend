package com.github.adrianR_Souza.Barbearia.Service;

import com.github.adrianR_Souza.Barbearia.Exception.RecursoDuplicateException;
import com.github.adrianR_Souza.Barbearia.Model.UsuarioEntity;
import com.github.adrianR_Souza.Barbearia.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;

//valida as regras de negocio, usuario dup, tipo de dados incorretos...
@Service
public class Validator {

    private final UsuarioRepository repository;

    public Validator(UsuarioRepository repository) {
        this.repository = repository;
    }

    public void validar(UsuarioEntity user){
        if(repository.existsByCpf(user.getCpf())){
            throw new RecursoDuplicateException("Já existe um usuário cadastrado com esse CPF.");
        } else if (repository.existsByEmail(user.getEmail())) {
            throw new RecursoDuplicateException("Já existe um usuário cadastrado com esse EMAIL.");
            } else if (repository.existsByTelefone(user.getTelefone())) {
                throw new RecursoDuplicateException("Já existe um usuário cadastrado com esse TELEFONE.");
            }
    }



}

