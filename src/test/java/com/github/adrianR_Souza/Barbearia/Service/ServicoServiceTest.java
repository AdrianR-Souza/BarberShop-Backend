package com.github.adrianR_Souza.Barbearia.Service;

import com.github.adrianR_Souza.Barbearia.Exception.RecursoNotFoundException;
import com.github.adrianR_Souza.Barbearia.Model.ServicoEntity;
import com.github.adrianR_Souza.Barbearia.Repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    private ServicoEntity servicoExistente;

    @BeforeEach
    void setUp() {
        servicoExistente = new ServicoEntity();
        servicoExistente.setId(1L);
        servicoExistente.setNomeServico("Corte simples");
        servicoExistente.setDuracaoServico(30);
        servicoExistente.setPrecoServico(new BigDecimal("40.00"));
    }

    @Test
    void cadastrar_devePersistirEDevolverOServicoSalvo() {
        when(servicoRepository.save(servicoExistente)).thenReturn(servicoExistente);

        ServicoEntity resultado = servicoService.cadastrar(servicoExistente);

        assertThat(resultado).isEqualTo(servicoExistente);
        verify(servicoRepository).save(servicoExistente);
    }

    @Test
    void listarTodos_deveDevolverTodosOsServicos() {
        when(servicoRepository.findAll()).thenReturn(List.of(servicoExistente));

        List<ServicoEntity> resultado = servicoService.listarTodos();

        assertThat(resultado).containsExactly(servicoExistente);
    }

    @Test
    void buscarPorId_quandoExiste_deveDevolverOServico() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoExistente));

        ServicoEntity resultado = servicoService.buscarPorId(1L);

        assertThat(resultado).isEqualTo(servicoExistente);
    }

    @Test
    void buscarPorId_quandoNaoExiste_deveLancarRecursoNotFoundException() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.buscarPorId(99L))
                .isInstanceOf(RecursoNotFoundException.class)
                .hasMessage("Serviço não encontrado");
    }

    @Test
    void atualizar_quandoExiste_deveSobrescreverCamposESalvar() {
        ServicoEntity dadosNovos = new ServicoEntity();
        dadosNovos.setNomeServico("Corte + Barba");
        dadosNovos.setDuracaoServico(60);
        dadosNovos.setPrecoServico(new BigDecimal("70.00"));

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoExistente));
        when(servicoRepository.save(any(ServicoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ServicoEntity resultado = servicoService.atualizar(1L, dadosNovos);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNomeServico()).isEqualTo("Corte + Barba");
        assertThat(resultado.getDuracaoServico()).isEqualTo(60);
        assertThat(resultado.getPrecoServico()).isEqualByComparingTo("70.00");
    }

    @Test
    void atualizar_quandoNaoExiste_deveLancarRecursoNotFoundExceptionENaoSalvar() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.atualizar(99L, servicoExistente))
                .isInstanceOf(RecursoNotFoundException.class);

        verify(servicoRepository, never()).save(any());
    }

    @Test
    void deletar_quandoExiste_deveRemoverOServico() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoExistente));

        servicoService.deletar(1L);

        verify(servicoRepository, times(1)).delete(servicoExistente);
    }

    @Test
    void deletar_quandoNaoExiste_deveLancarRecursoNotFoundExceptionENaoRemover() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.deletar(99L))
                .isInstanceOf(RecursoNotFoundException.class);

        verify(servicoRepository, never()).delete(any());
    }
}
