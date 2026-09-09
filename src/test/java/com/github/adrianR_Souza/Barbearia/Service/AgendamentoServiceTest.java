package com.github.adrianR_Souza.Barbearia.Service;

import com.github.adrianR_Souza.Barbearia.Config.HorarioFuncionamentoConfig;
import com.github.adrianR_Souza.Barbearia.Exception.AcessoNegadoException;
import com.github.adrianR_Souza.Barbearia.Exception.RecursoNotFoundException;
import com.github.adrianR_Souza.Barbearia.Model.AgendamentoEntity;
import com.github.adrianR_Souza.Barbearia.Model.Horario;
import com.github.adrianR_Souza.Barbearia.Model.Role;
import com.github.adrianR_Souza.Barbearia.Model.ServicoEntity;
import com.github.adrianR_Souza.Barbearia.Model.StatusAgendamento;
import com.github.adrianR_Souza.Barbearia.Model.UsuarioEntity;
import com.github.adrianR_Souza.Barbearia.Repository.AgendamentoRepository;
import com.github.adrianR_Souza.Barbearia.Repository.ServicoRepository;
import com.github.adrianR_Souza.Barbearia.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.adrianR_Souza.Barbearia.Model.RelatorioServicosResponse;
import com.github.adrianR_Souza.Barbearia.Model.ResumoServico;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private HorarioFuncionamentoConfig horarioFuncionamentoConfig;

    @Mock
    private EmailService emailService;

    private AgendamentoService service;

    private ServicoEntity servicoCorte;

    private static final LocalDate TERCA = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
    private static final LocalDate DOMINGO = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

    private UsuarioEntity cliente;
    private UsuarioEntity barbeiro;
    private UsuarioEntity outroBarbeiro;
    private UsuarioEntity master;
    private AgendamentoEntity agendamento;

    @BeforeEach
    void setUp() {
        service = new AgendamentoService(agendamentoRepository, usuarioRepository, servicoRepository, horarioFuncionamentoConfig, emailService);

        servicoCorte = new ServicoEntity();
        servicoCorte.setId(1L);
        servicoCorte.setNomeServico("Corte");
        servicoCorte.setDuracaoServico(30);

        cliente = new UsuarioEntity();
        cliente.setId(100L);
        cliente.setEmail("cliente@example.com");
        cliente.setRole(Role.ROLE_CLIENTE);

        barbeiro = new UsuarioEntity();
        barbeiro.setId(9L);
        barbeiro.setNome("Barbeiro Um");
        barbeiro.setEmail("barbeiro@example.com");
        barbeiro.setRole(Role.ROLE_BARBEIRO);

        outroBarbeiro = new UsuarioEntity();
        outroBarbeiro.setId(8L);
        outroBarbeiro.setNome("Barbeiro Dois");
        outroBarbeiro.setEmail("outro.barbeiro@example.com");
        outroBarbeiro.setRole(Role.ROLE_BARBEIRO);

        master = new UsuarioEntity();
        master.setId(1L);
        master.setEmail("master@example.com");
        master.setRole(Role.ROLE_MASTER);

        agendamento = new AgendamentoEntity();
        agendamento.setId(500L);
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servicoCorte);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
    }

    @Test
    void listarHorariosDisponiveis_quandoBarbeariaFechadaNesseDia_devolveListaVazia() {
        when(usuarioRepository.existsById(9L)).thenReturn(true);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoCorte));
        when(horarioFuncionamentoConfig.getHorario(DOMINGO.getDayOfWeek())).thenReturn(Optional.empty());

        List<String> resultado = service.listarHorariosDisponiveis(9L, 1L, DOMINGO);

        assertThat(resultado).isEmpty();
    }

    @Test
    void listarHorariosDisponiveis_quandoBarbeiroNaoExiste_lancaRecursoNotFoundException() {
        when(usuarioRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.listarHorariosDisponiveis(999L, 1L, TERCA))
                .isInstanceOf(RecursoNotFoundException.class);
    }

    @Test
    void listarHorariosDisponiveis_quandoServicoNaoExiste_lancaRecursoNotFoundException() {
        when(usuarioRepository.existsById(9L)).thenReturn(true);
        when(servicoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listarHorariosDisponiveis(9L, 999L, TERCA))
                .isInstanceOf(RecursoNotFoundException.class);
    }

    @Test
    void listarHorariosDisponiveis_diaLivre_devolveGradeComeandoNaAberturaEIndoAteAntesDoFechamento() {
        when(usuarioRepository.existsById(9L)).thenReturn(true);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoCorte));
        when(horarioFuncionamentoConfig.getHorario(TERCA.getDayOfWeek()))
                .thenReturn(Optional.of(new Horario(LocalTime.of(8, 0), LocalTime.of(9, 0))));
        when(agendamentoRepository.findByBarbeiro_IdAndDataHoraInicioBetween(anyLong(), any(), any()))
                .thenReturn(List.of());

        List<String> resultado = service.listarHorariosDisponiveis(9L, 1L, TERCA);

        assertThat(resultado).containsExactly("08:00", "08:15", "08:30");
    }

    @Test
    void listarHorariosDisponiveis_comAgendamentoExistente_removeOHorarioQueColide() {
        when(usuarioRepository.existsById(9L)).thenReturn(true);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoCorte));
        when(horarioFuncionamentoConfig.getHorario(TERCA.getDayOfWeek()))
                .thenReturn(Optional.of(new Horario(LocalTime.of(8, 0), LocalTime.of(9, 0))));

        AgendamentoEntity existente = new AgendamentoEntity();
        existente.setDataHoraInicio(LocalDateTime.of(TERCA, LocalTime.of(8, 15)));
        existente.setDataHoraFim(LocalDateTime.of(TERCA, LocalTime.of(8, 45)));
        existente.setStatus(StatusAgendamento.CONFIRMADO);

        when(agendamentoRepository.findByBarbeiro_IdAndDataHoraInicioBetween(anyLong(), any(), any()))
                .thenReturn(List.of(existente));

        List<String> resultado = service.listarHorariosDisponiveis(9L, 1L, TERCA);

        assertThat(resultado).isEmpty();
    }

    @Test
    void listarHorariosDisponiveis_agendamentoCanceladoNaoBloqueiaOHorario() {
        when(usuarioRepository.existsById(9L)).thenReturn(true);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoCorte));
        when(horarioFuncionamentoConfig.getHorario(TERCA.getDayOfWeek()))
                .thenReturn(Optional.of(new Horario(LocalTime.of(8, 0), LocalTime.of(9, 0))));

        AgendamentoEntity cancelado = new AgendamentoEntity();
        cancelado.setDataHoraInicio(LocalDateTime.of(TERCA, LocalTime.of(8, 0)));
        cancelado.setDataHoraFim(LocalDateTime.of(TERCA, LocalTime.of(8, 30)));
        cancelado.setStatus(StatusAgendamento.CANCELADO);

        when(agendamentoRepository.findByBarbeiro_IdAndDataHoraInicioBetween(anyLong(), any(), any()))
                .thenReturn(List.of(cancelado));

        List<String> resultado = service.listarHorariosDisponiveis(9L, 1L, TERCA);

        assertThat(resultado).contains("08:00");
    }

    @Test
    void altStatusCancelar_quandoQuemChamaEOCliente_permite() {
        when(agendamentoRepository.findById(500L)).thenReturn(Optional.of(agendamento));
        when(usuarioRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(cliente));
        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        AgendamentoEntity resultado = service.altStatusCancelar(500L, "cliente@example.com");

        assertThat(resultado.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    void altStatusCancelar_quandoQuemChamaEOBarbeiroDoAgendamento_permite() {
        when(agendamentoRepository.findById(500L)).thenReturn(Optional.of(agendamento));
        when(usuarioRepository.findByEmail("barbeiro@example.com")).thenReturn(Optional.of(barbeiro));
        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        AgendamentoEntity resultado = service.altStatusCancelar(500L, "barbeiro@example.com");

        assertThat(resultado.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    void altStatusCancelar_quandoQuemChamaEUmBarbeiroDeOutraAgenda_lancaAcessoNegadoException() {
        when(agendamentoRepository.findById(500L)).thenReturn(Optional.of(agendamento));
        when(usuarioRepository.findByEmail("outro.barbeiro@example.com")).thenReturn(Optional.of(outroBarbeiro));

        assertThatThrownBy(() -> service.altStatusCancelar(500L, "outro.barbeiro@example.com"))
                .isInstanceOf(AcessoNegadoException.class);

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void altStatusCancelar_quandoQuemChamaEMaster_permiteMesmoSemSerDonoOuBarbeiro() {
        when(agendamentoRepository.findById(500L)).thenReturn(Optional.of(agendamento));
        when(usuarioRepository.findByEmail("master@example.com")).thenReturn(Optional.of(master));
        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        AgendamentoEntity resultado = service.altStatusCancelar(500L, "master@example.com");

        assertThat(resultado.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    void altStatusConfirmar_quandoQuemChamaEOCliente_lancaAcessoNegadoException() {
        when(agendamentoRepository.findById(500L)).thenReturn(Optional.of(agendamento));
        when(usuarioRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> service.altStatusConfirmar(500L, "cliente@example.com"))
                .isInstanceOf(AcessoNegadoException.class);

        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void altStatusConfirmar_quandoQuemChamaEOBarbeiroDoAgendamento_permite() {
        when(agendamentoRepository.findById(500L)).thenReturn(Optional.of(agendamento));
        when(usuarioRepository.findByEmail("barbeiro@example.com")).thenReturn(Optional.of(barbeiro));
        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        AgendamentoEntity resultado = service.altStatusConfirmar(500L, "barbeiro@example.com");

        assertThat(resultado.getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    @Test
    void listarMeusAgendamentosCliente_devolveSoOsDoClienteLogado() {
        when(usuarioRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(cliente));
        when(agendamentoRepository.findByCliente_Id(100L)).thenReturn(List.of(agendamento));

        List<AgendamentoEntity> resultado = service.listarMeusAgendamentosCliente("cliente@example.com");

        assertThat(resultado).containsExactly(agendamento);
    }

    @Test
    void listarAgendaDoBarbeiroNoDia_devolveSoOsDoBarbeiroLogadoNaquelaData() {
        when(usuarioRepository.findByEmail("barbeiro@example.com")).thenReturn(Optional.of(barbeiro));
        when(agendamentoRepository.findByBarbeiro_IdAndDataHoraInicioBetween(eq(9L), any(), any()))
                .thenReturn(List.of(agendamento));

        List<AgendamentoEntity> resultado = service.listarAgendaDoBarbeiroNoDia("barbeiro@example.com", TERCA);

        assertThat(resultado).containsExactly(agendamento);
    }

    @Test
    void gerarRelatorioServicos_somaQuantidadeEValorPorServicoEBarbeiro() {
        servicoCorte.setPrecoServico(new BigDecimal("40.00"));

        ServicoEntity servicoBarba = new ServicoEntity();
        servicoBarba.setId(2L);
        servicoBarba.setNomeServico("Barba");
        servicoBarba.setPrecoServico(new BigDecimal("30.00"));

        AgendamentoEntity corte1 = new AgendamentoEntity();
        corte1.setServico(servicoCorte);
        corte1.setBarbeiro(barbeiro);
        corte1.setStatus(StatusAgendamento.CONCLUIDO);

        AgendamentoEntity corte2 = new AgendamentoEntity();
        corte2.setServico(servicoCorte);
        corte2.setBarbeiro(outroBarbeiro);
        corte2.setStatus(StatusAgendamento.CONCLUIDO);

        AgendamentoEntity barba1 = new AgendamentoEntity();
        barba1.setServico(servicoBarba);
        barba1.setBarbeiro(barbeiro);
        barba1.setStatus(StatusAgendamento.CONCLUIDO);

        when(agendamentoRepository.findByStatusAndDataHoraInicioBetween(eq(StatusAgendamento.CONCLUIDO), any(), any()))
                .thenReturn(List.of(corte1, corte2, barba1));

        RelatorioServicosResponse relatorio = service.gerarRelatorioServicos(TERCA, TERCA.plusDays(6));

        assertThat(relatorio.getTotalConcluidos()).isEqualTo(3);
        assertThat(relatorio.getValorTotal()).isEqualByComparingTo("110.00");

        assertThat(relatorio.getPorServico())
                .extracting(ResumoServico::getNomeServico, ResumoServico::getQuantidade)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Corte", 2L),
                        org.assertj.core.groups.Tuple.tuple("Barba", 1L)
                );

        assertThat(relatorio.getPorBarbeiro())
                .extracting("nomeBarbeiro", "quantidade")
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(barbeiro.getNome(), 2L),
                        org.assertj.core.groups.Tuple.tuple(outroBarbeiro.getNome(), 1L)
                );
    }

    @Test
    void gerarRelatorioServicos_semAgendamentosNoPeriodo_devolveZerado() {
        when(agendamentoRepository.findByStatusAndDataHoraInicioBetween(eq(StatusAgendamento.CONCLUIDO), any(), any()))
                .thenReturn(List.of());

        RelatorioServicosResponse relatorio = service.gerarRelatorioServicos(TERCA, TERCA);

        assertThat(relatorio.getTotalConcluidos()).isZero();
        assertThat(relatorio.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(relatorio.getPorServico()).isEmpty();
        assertThat(relatorio.getPorBarbeiro()).isEmpty();
    }
}
