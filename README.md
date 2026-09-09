Barber Trindade — Backend

Nas últimas semanas venho construindo, do zero, o sistema completo de agendamento de uma barbearia real. 
Esse repositório é o backend: cadastro e autenticação de usuários, controle de horários, painéis por perfil e toda a parte de segurança por trás disso. 
O frontend (Angular) começou a ser construído logo em seguida, consumindo essa mesma API.


Previsão de implementação no cliente final: setembro/2026.


O que o sistema faz:

  Cliente se cadastra, loga e marca horário com o barbeiro que quiser, vendo só os horários realmente livres, sem digitar data/hora na mão. 
Dá pra marcar um agendamento pra outra pessoa (por exemplo, marcar um corte pro seu pai), informando nome e idade de quem vai ser atendido. 
Barbeiro tem um painel próprio: vê a agenda do dia, confirma, cancela ou marca como concluído cada atendimento. 
O admin (dono/gerente) tem um painel geral: cadastra novos barbeiros, vê a agenda de todo mundo e tira relatório de faturamento por período. 
Esqueceu a senha? Dá pra redefinir confirmando CPF + telefone (não tem servidor de e-mail configurado, então optei por essa validação em vez de mandar e-mail). 
Tudo protegido por autenticação JWT e por um limitador de requisições próprio (rate limit), pra não deixar ninguém martelar login/cadastro com script.


O maior desafio técnico até agora:

  A parte que mais me exigiu foi o algoritmo de detecção de conflito de horário: garantir que nenhum barbeiro fique com dois agendamentos ao mesmo tempo, 
considerando a duração de cada serviço (um corte não ocupa o mesmo tempo que um corte + barba), o horário de funcionamento da barbearia e os 
horários já ocupados no dia. O sistema gera os horários candidatos em passos de 15 minutos, 
descarta os que colidem com algo já agendado (menos os cancelados) e os que já passaram, e só então mostra pro cliente o que sobrou como realmente disponível.
A outra frente que exigiu bastante cuidado foi a área de segurança: hash de senha (BCrypt, nunca texto puro, nunca devolvido pela API), 
geração e validação de access token JWT, e controle de acesso por perfil (cliente/barbeiro/admin), tanto nas rotas quanto dentro da própria regra de negócio, 
pra um cliente nunca conseguir mexer no agendamento de outra pessoa, por exemplo.


Aprendendo com cada parte do processo — inclusive os bugs, que me ensinaram mais do que qualquer tutorial.


Stack
Java 21 com Spring Boot 4.1. 
Spring Security fazendo autenticação via JWT, sem sessão, cada request carrega seu próprio token. 
Spring Data JPA e Hibernate rodando em cima de PostgreSQL, tudo containerizado com Docker. 
Bean Validation (Jakarta) validando tudo que entra pela API, incluindo um validador de CPF que escrevi na mão (confere o dígito verificador de verdade, não só se tem 11 números). 
JWT pra gerar/validar os tokens, BCrypt pro hash de senha, Lombok pra não escrever getter/setter repetido. 
JUnit 5 e Mockito nos testes (48 testes cobrindo serviços, permissões e validações). 
Swagger/OpenAPI documentando toda a API (desligado em produção).



Principais endpoints:

Método	  Rota                      	                   O que faz	Quem acessa
POST	   /usuarios	                                     Cadastro de cliente	Público
POST	   /usuarios/barbeiro	                             Cadastro de barbeiro	Admin
POST	   /login	Login (devolve o access token JWT)	     Público
GET	     /usuarios/me	                                   Dados do usuário logado	Autenticado
GET      /POST/PUT/DELETE	/servicos	                     CRUD dos serviços (corte, barba...)	Leitura: qualquer autenticado · Escrita: admin
POST	   /agenda	Marcar horário	                       Cliente
GET	     /agenda/horarios-disponiveis	                   Horários livres de um barbeiro num dia (aqui entra a detecção de conflito)	Autenticado
GET	     /agenda/meus-agendamentos	                     Agendamentos do cliente logado	Cliente
GET	     /agenda/minha-agenda	                           Agenda do dia do barbeiro logado	Barbeiro
PUT	     /agenda/{id}/confirmar, /cancelar, /concluido	 Muda o status do agendamento	Dono do agendamento, barbeiro responsável ou admin
GET	     /agenda/relatorio	                             Faturamento por período	Admin
POST	   /senha/esqueci, /senha/redefinir-por-cpf	       Recuperação de senha (por CPF + telefone)	Público
