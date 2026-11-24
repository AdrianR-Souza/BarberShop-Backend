package com.github.adrianR_Souza.Barbearia.Service;

import com.github.adrianR_Souza.Barbearia.Exception.RecursoNotFoundException;
import com.github.adrianR_Souza.Barbearia.Model.ServicoEntity;
import com.github.adrianR_Souza.Barbearia.Repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    public ServicoEntity cadastrar(ServicoEntity servico){
        return servicoRepository.save(servico);
    }

    public List<ServicoEntity> listarTodos(){
        return servicoRepository.findAll();
    }

    public ServicoEntity buscarPorId(Long id){
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Serviço não encontrado"));
    }

    public ServicoEntity atualizar(Long id, ServicoEntity novoServico){
        ServicoEntity servicoExistente = buscarPorId(id);

        servicoExistente.setNomeServico(novoServico.getNomeServico());
        servicoExistente.setDuracaoServico(novoServico.getDuracaoServico());
        servicoExistente.setPrecoServico(novoServico.getPrecoServico());

        return servicoRepository.save(servicoExistente);
    }

    public void deletar(Long id){
        ServicoEntity servico = buscarPorId(id);
        servicoRepository.delete(servico);
    }
}
