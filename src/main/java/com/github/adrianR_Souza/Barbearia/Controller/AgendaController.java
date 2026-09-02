package com.github.adrianR_Souza.Barbearia.Controller;

import com.github.adrianR_Souza.Barbearia.Model.AgendamentoEntity;
import com.github.adrianR_Souza.Barbearia.Model.AgendamentoRequest;
import com.github.adrianR_Souza.Barbearia.Model.AgendamentoResumo;
import com.github.adrianR_Souza.Barbearia.Model.MetodoPgto;
import com.github.adrianR_Souza.Barbearia.Model.RelatorioServicosResponse;
import com.github.adrianR_Souza.Barbearia.Service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/agenda")
public class AgendaController {

    private final AgendamentoService agendamentoService;

    public AgendaController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoEntity criar(@Valid @RequestBody AgendamentoRequest request) {
        return agendamentoService.criarAgendamento(request);
    }

    @GetMapping("/metodos-pagamento")
    public MetodoPgto[] listarMetodosPagamento() {
        return MetodoPgto.values();
    }

    @GetMapping("/horarios-disponiveis")
    public List<String> listarHorariosDisponiveis(
            @RequestParam Long barbeiroId,
            @RequestParam Long servicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return agendamentoService.listarHorariosDisponiveis(barbeiroId, servicoId, data);
    }

    @GetMapping("/{id}")
    public AgendamentoResumo listarAgendamentoId(@PathVariable Long id){return agendamentoService.listarAgendamentos(id);}

    @GetMapping("/agendamentos")
    public List<AgendamentoEntity> listarAllAgendamentos(){return agendamentoService.listarAllAgendamento();}

    @GetMapping("/relatorio")
    public RelatorioServicosResponse gerarRelatorio(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return agendamentoService.gerarRelatorioServicos(inicio, fim);
    }

    @GetMapping("/meus-agendamentos")
    public List<AgendamentoEntity> listarMeusAgendamentos() {
        String email = emailLogado();
        return agendamentoService.listarMeusAgendamentosCliente(email);
    }

    @GetMapping("/minha-agenda")
    public List<AgendamentoEntity> listarMinhaAgendaDoDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return agendamentoService.listarAgendaDoBarbeiroNoDia(emailLogado(), data);
    }

    @PutMapping("/{id}/cancelar")
    public AgendamentoEntity alterarStatusCancelar(@PathVariable Long id){
        return agendamentoService.altStatusCancelar(id, emailLogado());
    }

    @PutMapping("/{id}/confirmar")
    public AgendamentoEntity alterarStatusConfirmar(@PathVariable Long id){
        return agendamentoService.altStatusConfirmar(id, emailLogado());
    }

    @PutMapping("/{id}/concluido")
    public AgendamentoEntity alterarStatusConcluido(@PathVariable Long id){
        return agendamentoService.altStatusConcluido(id, emailLogado());
    }

    private String emailLogado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}