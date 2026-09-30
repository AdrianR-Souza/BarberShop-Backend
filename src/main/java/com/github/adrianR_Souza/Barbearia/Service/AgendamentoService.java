package com.github.adrianR_Souza.Barbearia.Service;

import com.github.adrianR_Souza.Barbearia.Config.HorarioFuncionamentoConfig;
import com.github.adrianR_Souza.Barbearia.Exception.AcessoNegadoException;
import com.github.adrianR_Souza.Barbearia.Exception.HorarioIndisponivelException;
import com.github.adrianR_Souza.Barbearia.Exception.RecursoNotFoundException;
import com.github.adrianR_Souza.Barbearia.Model.*;
import com.github.adrianR_Souza.Barbearia.Repository.AgendamentoRepository;
import com.github.adrianR_Souza.Barbearia.Repository.BloqueioAgendaRepository;
import com.github.adrianR_Souza.Barbearia.Repository.ServicoRepository;
import com.github.adrianR_Souza.Barbearia.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {
    private static final int PASSO_MINUTOS = 15;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;
    private final HorarioFuncionamentoConfig horarioFuncionamentoConfig;
    private final EmailService emailService;
    private final BloqueioAgendaRepository bloqueioAgendaRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, UsuarioRepository usuarioRepository,
                               ServicoRepository servicoRepository, HorarioFuncionamentoConfig horarioFuncionamentoConfig,
                               EmailService emailService, BloqueioAgendaRepository bloqueioAgendaRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.servicoRepository = servicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.horarioFuncionamentoConfig = horarioFuncionamentoConfig;
        this.emailService = emailService;
        this.bloqueioAgendaRepository = bloqueioAgendaRepository;
    }

    private boolean colide(LocalDateTime inicioA, LocalDateTime fimA, LocalDateTime inicioB, LocalDateTime fimB){
        return inicioA.isBefore(fimB) && inicioB.isBefore(fimA);

    }

    private List<AgendamentoEntity> buscarAgendaDoDia(Long barbeiroId, LocalDate data) {
        LocalDateTime dataInicio = data.atStartOfDay();
        LocalDateTime dataFim = dataInicio.plusDays(1);

        return agendamentoRepository.findByBarbeiro_IdAndDataHoraInicioBetween(barbeiroId, dataInicio, dataFim)
                .stream()
                .filter(a -> a.getStatus() != StatusAgendamento.CANCELADO)
                .toList();
    }

    private List<BloqueioAgendaEntity> buscarBloqueiosDoDia(Long barbeiroId, LocalDate data) {
        LocalDateTime dataInicio = data.atStartOfDay();
        LocalDateTime dataFim = dataInicio.plusDays(1);

        return bloqueioAgendaRepository.findByBarbeiro_IdAndDataHoraInicioLessThanAndDataHoraFimGreaterThan(
                barbeiroId, dataFim, dataInicio);
    }

    public AgendamentoEntity criar(AgendamentoEntity agendamento){
        LocalDateTime fim = agendamento.getDataHoraInicio().plusMinutes(agendamento.getServico().getDuracaoServico()) ;
        agendamento.setDataHoraFim(fim);

        Long id_barbeiro = agendamento.getBarbeiro().getId();
        List<AgendamentoEntity> agendaDoDia = buscarAgendaDoDia(id_barbeiro, agendamento.getDataHoraInicio().toLocalDate());

        for (AgendamentoEntity agendamentoExistente : agendaDoDia) {
            if(colide(agendamentoExistente.getDataHoraInicio(), agendamentoExistente.getDataHoraFim(), agendamento.getDataHoraInicio(), agendamento.getDataHoraFim ())){
                throw new HorarioIndisponivelException("Esse horário já está ocupado para esse barbeiro");
            }
        }

        List<BloqueioAgendaEntity> bloqueiosDoDia = buscarBloqueiosDoDia(id_barbeiro, agendamento.getDataHoraInicio().toLocalDate());
        for (BloqueioAgendaEntity bloqueio : bloqueiosDoDia) {
            if (colide(bloqueio.getDataHoraInicio(), bloqueio.getDataHoraFim(), agendamento.getDataHoraInicio(), agendamento.getDataHoraFim())) {
                throw new HorarioIndisponivelException("O barbeiro fechou a agenda nesse período.");
            }
        }

        agendamento.setStatus(StatusAgendamento.PENDENTE);
        return agendamentoRepository.save(agendamento);
    }

    public List<String> listarHorariosDisponiveis(Long barbeiroId, Long servicoId, LocalDate data) {
        if (!usuarioRepository.existsById(barbeiroId)) {
            throw new RecursoNotFoundException("Barbeiro não encontrado.");
        }
        ServicoEntity servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RecursoNotFoundException("Serviço não encontrado"));

        Optional<Horario> horarioOpt = horarioFuncionamentoConfig.getHorario(data.getDayOfWeek());
        if (horarioOpt.isEmpty()) {
            return List.of();
        }
        Horario horario = horarioOpt.get();

        List<AgendamentoEntity> agendaDoDia = buscarAgendaDoDia(barbeiroId, data);
        List<BloqueioAgendaEntity> bloqueiosDoDia = buscarBloqueiosDoDia(barbeiroId, data);

        LocalDateTime abertura = LocalDateTime.of(data, horario.abertura());
        LocalDateTime fechamento = LocalDateTime.of(data, horario.fechamento());
        LocalDateTime agora = LocalDateTime.now();

        List<String> disponiveis = new ArrayList<>();

        for (LocalDateTime candidato = abertura;
             !candidato.plusMinutes(servico.getDuracaoServico()).isAfter(fechamento);
             candidato = candidato.plusMinutes(PASSO_MINUTOS)) {

            if (candidato.isBefore(agora)) {
                continue;
            }

            final LocalDateTime candidatoInicio = candidato;
            final LocalDateTime candidatoFim = candidato.plusMinutes(servico.getDuracaoServico());
            boolean colideComAgendamento = agendaDoDia.stream()
                    .anyMatch(a -> colide(a.getDataHoraInicio(), a.getDataHoraFim(), candidatoInicio, candidatoFim));
            boolean colideComBloqueio = bloqueiosDoDia.stream()
                    .anyMatch(b -> colide(b.getDataHoraInicio(), b.getDataHoraFim(), candidatoInicio, candidatoFim));

            if (!colideComAgendamento && !colideComBloqueio) {
                disponiveis.add(candidatoInicio.toLocalTime().format(FORMATO_HORA));
            }
        }

        return disponiveis;
    }

    public AgendamentoEntity criarAgendamento(AgendamentoRequest request, String emailLogado) {
        UsuarioEntity cliente = buscarUsuarioLogado(emailLogado);

        UsuarioEntity barbeiro = usuarioRepository.findById((request.getBarbeiroId()))
                .orElseThrow(() -> new RecursoNotFoundException("Barbeiro não encontrado."));

        ServicoEntity servico = servicoRepository.findById((request.getServicoId()))
                .orElseThrow(() -> new RecursoNotFoundException("Serviço não encontrado"));

        AgendamentoEntity agendamento = new AgendamentoEntity();
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setNomeTerceiro(request.getNomeTerceiro());
        agendamento.setIdadeTerceiro(request.getIdadeTerceiro());

        agendamento.setDataHoraInicio(request.getDataHoraInicio());

        AgendamentoEntity agendamentoCriado = criar(agendamento);
        emailService.notificarNovoAgendamento(agendamentoCriado);
        return agendamentoCriado;
    }
    public AgendamentoEntity buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Agendamento não encontrado"));
    }

    public AgendamentoResumo listarAgendamentos(Long id, String emailLogado){
        AgendamentoEntity agendamento = buscarPorId(id);
        validarPermissao(agendamento, emailLogado, true);

        return new AgendamentoResumo(
                agendamento.getBarbeiro().getNome(),
                agendamento.getCliente().getNome(),
                agendamento.getServico().getNomeServico(),
                agendamento.getStatus(),
                agendamento.getDataHoraInicio(),
                agendamento.getDataHoraFim()
        );
    }

    public List<AgendamentoEntity> listarAllAgendamento(){return agendamentoRepository.findAll();}

    public List<AgendamentoEntity> listarMeusAgendamentosCliente(String emailLogado) {
        UsuarioEntity cliente = buscarUsuarioLogado(emailLogado);
        return agendamentoRepository.findByCliente_Id(cliente.getId()).stream()
                .sorted(Comparator.comparing(AgendamentoEntity::getDataHoraInicio))
                .toList();
    }

    public List<AgendamentoEntity> listarAgendaDoBarbeiroNoDia(String emailLogado, LocalDate data) {
        UsuarioEntity barbeiro = buscarUsuarioLogado(emailLogado);
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = inicio.plusDays(1);

        return agendamentoRepository.findByBarbeiro_IdAndDataHoraInicioBetween(barbeiro.getId(), inicio, fim).stream()
                .sorted(Comparator.comparing(AgendamentoEntity::getDataHoraInicio))
                .toList();
    }

    private UsuarioEntity buscarUsuarioLogado(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNotFoundException("Usuário não encontrado"));
    }

    private void validarPermissao(AgendamentoEntity agendamento, String emailLogado, boolean permiteCliente) {
        UsuarioEntity usuarioLogado = buscarUsuarioLogado(emailLogado);

        if (usuarioLogado.getRole() == Role.ROLE_MASTER) {
            return;
        }
        boolean ehOBarbeiro = agendamento.getBarbeiro().getId().equals(usuarioLogado.getId());
        boolean ehOCliente = permiteCliente && agendamento.getCliente().getId().equals(usuarioLogado.getId());

        if (!ehOBarbeiro && !ehOCliente) {
            throw new AcessoNegadoException("Você não tem permissão para alterar esse agendamento.");
        }
    }

    public AgendamentoEntity altStatusCancelar(Long id, String emailLogado) {
        AgendamentoEntity agendamentos = buscarPorId(id);
        validarPermissao(agendamentos, emailLogado, true);

        agendamentos.setStatus(StatusAgendamento.CANCELADO);
        return agendamentoRepository.save(agendamentos);
    }

    public AgendamentoEntity altStatusConfirmar(Long id, String emailLogado) {
        AgendamentoEntity agendamentos = buscarPorId(id);
        validarPermissao(agendamentos, emailLogado, false);

        agendamentos.setStatus(StatusAgendamento.CONFIRMADO);
        AgendamentoEntity agendamentoConfirmado = agendamentoRepository.save(agendamentos);
        emailService.notificarAgendamentoConfirmado(agendamentoConfirmado);
        return agendamentoConfirmado;
    }

    public AgendamentoEntity altStatusConcluido(Long id, String emailLogado) {
        AgendamentoEntity agendamentos = buscarPorId(id);
        validarPermissao(agendamentos, emailLogado, false);

        agendamentos.setStatus(StatusAgendamento.CONCLUIDO);
        return agendamentoRepository.save(agendamentos);
    }

    public RelatorioServicosResponse gerarRelatorioServicos(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime fimDateTime = fim.plusDays(1).atStartOfDay();

        List<AgendamentoEntity> concluidos = agendamentoRepository
                .findByStatusAndDataHoraInicioBetween(StatusAgendamento.CONCLUIDO, inicioDateTime, fimDateTime);

        BigDecimal valorTotal = concluidos.stream()
                .map(a -> a.getServico().getPrecoServico())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, List<AgendamentoEntity>> agrupadosPorServico = concluidos.stream()
                .collect(Collectors.groupingBy(a -> a.getServico().getNomeServico(), LinkedHashMap::new, Collectors.toList()));

        List<ResumoServico> porServico = agrupadosPorServico.entrySet().stream()
                .map(entry -> new ResumoServico(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .map(a -> a.getServico().getPrecoServico())
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .sorted(Comparator.comparingLong(ResumoServico::getQuantidade).reversed())
                .toList();

        Map<String, List<AgendamentoEntity>> agrupadosPorBarbeiro = concluidos.stream()
                .collect(Collectors.groupingBy(a -> a.getBarbeiro().getNome(), LinkedHashMap::new, Collectors.toList()));

        List<ResumoBarbeiro> porBarbeiro = agrupadosPorBarbeiro.entrySet().stream()
                .map(entry -> new ResumoBarbeiro(entry.getKey(), entry.getValue().size()))
                .sorted(Comparator.comparingLong(ResumoBarbeiro::getQuantidade).reversed())
                .toList();

        return new RelatorioServicosResponse(inicio, fim, concluidos.size(), valorTotal, porServico, porBarbeiro);
    }

    public BloqueioAgendaEntity criarBloqueio(BloqueioAgendaRequest request, String emailLogado) {
        if (!request.getDataHoraInicio().isBefore(request.getDataHoraFim())) {
            throw new HorarioIndisponivelException("A data/hora de início precisa ser antes da data/hora de fim.");
        }

        UsuarioEntity usuarioLogado = buscarUsuarioLogado(emailLogado);

        //MASTER pode escolher pra qual barbeiro e' o bloqueio; qualquer outra
        //pessoa so fecha a propria agenda, mesmo que mande outro barbeiroId
        UsuarioEntity barbeiro;
        if (usuarioLogado.getRole() == Role.ROLE_MASTER && request.getBarbeiroId() != null) {
            barbeiro = usuarioRepository.findById(request.getBarbeiroId())
                    .orElseThrow(() -> new RecursoNotFoundException("Barbeiro não encontrado."));
        } else {
            barbeiro = usuarioLogado;
        }

        boolean atuaComoBarbeiro = barbeiro.getRole() == Role.ROLE_BARBEIRO
                || barbeiro.getRole() == Role.ROLE_MASTER
                || barbeiro.isAtendeComoBarbeiro();
        if (!atuaComoBarbeiro) {
            throw new AcessoNegadoException("Esse usuário não atende como barbeiro.");
        }

        BloqueioAgendaEntity bloqueio = new BloqueioAgendaEntity();
        bloqueio.setBarbeiro(barbeiro);
        bloqueio.setDataHoraInicio(request.getDataHoraInicio());
        bloqueio.setDataHoraFim(request.getDataHoraFim());
        bloqueio.setMotivo(request.getMotivo());

        return bloqueioAgendaRepository.save(bloqueio);
    }

    public List<BloqueioAgendaEntity> listarBloqueios(String emailLogado, Long barbeiroIdOpcional) {
        UsuarioEntity usuarioLogado = buscarUsuarioLogado(emailLogado);

        Long idAlvo = usuarioLogado.getId();
        if (usuarioLogado.getRole() == Role.ROLE_MASTER && barbeiroIdOpcional != null) {
            idAlvo = barbeiroIdOpcional;
        }

        return bloqueioAgendaRepository.findByBarbeiro_IdOrderByDataHoraInicio(idAlvo);
    }

    public void removerBloqueio(Long id, String emailLogado) {
        BloqueioAgendaEntity bloqueio = bloqueioAgendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("Bloqueio não encontrado"));

        UsuarioEntity usuarioLogado = buscarUsuarioLogado(emailLogado);
        boolean dono = bloqueio.getBarbeiro().getId().equals(usuarioLogado.getId());

        if (!dono && usuarioLogado.getRole() != Role.ROLE_MASTER) {
            throw new AcessoNegadoException("Você não tem permissão pra remover esse bloqueio.");
        }

        bloqueioAgendaRepository.delete(bloqueio);
    }
}
