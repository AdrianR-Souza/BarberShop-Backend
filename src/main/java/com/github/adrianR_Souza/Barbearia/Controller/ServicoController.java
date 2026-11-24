package com.github.adrianR_Souza.Barbearia.Controller;

import com.github.adrianR_Souza.Barbearia.Model.ServicoEntity;
import com.github.adrianR_Souza.Barbearia.Service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoEntity cadastrar(@Valid @RequestBody ServicoEntity servico){
        return servicoService.cadastrar(servico);
    }

    @GetMapping
    public List<ServicoEntity> buscarTodos(){
        return servicoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ServicoEntity buscarPorId(@PathVariable Long id){
        return servicoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ServicoEntity atualizar(@PathVariable Long id, @Valid @RequestBody ServicoEntity servico){
        return servicoService.atualizar(id, servico);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id){
        servicoService.deletar(id);
    }

}
