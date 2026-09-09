package com.github.adrianR_Souza.Barbearia.Service;

import com.github.adrianR_Souza.Barbearia.Model.AgendamentoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class EmailService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    //assincrono e com try/catch pra um problema no envio de email nunca derrubar o agendamento em si
    @Async
    public void notificarNovoAgendamento(AgendamentoEntity agendamento) {
        try {
            String nomeAtendido = agendamento.getNomeTerceiro() != null
                    ? agendamento.getNomeTerceiro()
                    : agendamento.getCliente().getNome();

            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setTo(agendamento.getBarbeiro().getEmail());
            mensagem.setSubject("Novo agendamento - Barber Trindade");
            mensagem.setText("""
                    Você tem um novo agendamento!

                    Cliente: %s
                    Serviço: %s
                    Data e hora: %s
                    Marcado por: %s (telefone: %s)

                    Acesse o painel do barbeiro pra confirmar.
                    """.formatted(
                    nomeAtendido,
                    agendamento.getServico().getNomeServico(),
                    agendamento.getDataHoraInicio().format(FORMATO_DATA),
                    agendamento.getCliente().getNome(),
                    agendamento.getCliente().getTelefone()
            ));

            mailSender.send(mensagem);
        } catch (Exception e) {
            log.warn("Não foi possível enviar o e-mail de notificação do agendamento {}: {}", agendamento.getId(), e.getMessage());
        }
    }
}
