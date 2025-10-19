package org.stm.atty.controller;

import org.stm.atty.dto.DashboardDTO;
import org.stm.atty.dto.UsuarioDTO;
import org.stm.atty.dto.ProcessoDTO;
import org.stm.atty.dto.EventoDTO;
import org.stm.atty.service.AuthService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.*;

@Path("/dashboard-cliente")
public class DashboardClienteController {

    @Inject
    AuthService authService;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance clienteDashboard();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString("Authorization");
        UsuarioDTO usuario = authService.getUsuarioLogado(authHeader);

        if (usuario == null) {
            throw new WebApplicationException("Não autorizado", Response.Status.UNAUTHORIZED);
        }

        Map<String, Object> dados = new HashMap<>();
        dados.put("usuarioObj", usuario);
        dados.put("dados", criarDadosDashboard(usuario));
        dados.put("processos", prepararProcessosCliente(usuario));
        dados.put("eventos", prepararEventosCliente(usuario));
        dados.put("processo", new ProcessoDTO());

        return Templates.clienteDashboard().data(dados);
    }

    @GET
    @Path("/dados")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDadosDashboard(@Context HttpHeaders headers) {
        try {
            String authHeader = headers.getHeaderString("Authorization");
            UsuarioDTO usuario = authService.getUsuarioLogado(authHeader);

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Map<String, Object> dados = new HashMap<>();
            dados.put("usuario", usuario);
            dados.put("dashboard", criarDadosDashboard(usuario));
            dados.put("processos", prepararProcessosCliente(usuario));
            dados.put("eventos", prepararEventosCliente(usuario));

            return Response.ok(dados).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erro ao carregar dados\"}")
                    .build();
        }
    }

    private DashboardDTO criarDadosDashboard(UsuarioDTO usuario) {
        DashboardDTO dados = new DashboardDTO();
        dados.setNomeUsuario(usuario != null ? usuario.getNome() : "Cliente");
        dados.setTotalProcessos(3);
        dados.setTotalClientes(0);
        dados.setCompromissosHoje(2);
        return dados;
    }

    private List<ProcessoDTO> prepararProcessosCliente(UsuarioDTO usuario) {
        List<ProcessoDTO> processos = new ArrayList<>();

        ProcessoDTO processo1 = new ProcessoDTO();
        processo1.setId("1");
        processo1.setNumero("0001234-56.2025.8.07.0001");
        processo1.setDescricao("Processo trabalhista - Horas extras");
        processo1.setStatus("EM_ANDAMENTO");
        processo1.setDataCriacao("2025-01-15");
        processos.add(processo1);

        ProcessoDTO processo2 = new ProcessoDTO();
        processo2.setId("2");
        processo2.setNumero("0005678-90.2025.8.07.0002");
        processo2.setDescricao("Ação de divórcio consensual");
        processo2.setStatus("CONCLUIDO");
        processo2.setDataCriacao("2025-02-20");
        processos.add(processo2);

        ProcessoDTO processo3 = new ProcessoDTO();
        processo3.setId("3");
        processo3.setNumero("0009012-34.2025.8.07.0003");
        processo3.setDescricao("Inventário extrajudicial");
        processo3.setStatus("EM_ANDAMENTO");
        processo3.setDataCriacao("2025-03-10");
        processos.add(processo3);

        return processos;
    }

    private List<EventoDTO> prepararEventosCliente(UsuarioDTO usuario) {
        List<EventoDTO> eventos = new ArrayList<>();

        EventoDTO evento1 = new EventoDTO();
        evento1.setId("1");
        evento1.setTitle("Reunião com Advogado");
        evento1.setStart(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
        evento1.setEnd(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0));
        evento1.setColor("#007bff");
        eventos.add(evento1);

        EventoDTO evento2 = new EventoDTO();
        evento2.setId("2");
        evento2.setTitle("Audiência - Processo Trabalhista");
        evento2.setStart(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));
        evento2.setEnd(LocalDateTime.now().plusDays(3).withHour(12).withMinute(0));
        evento2.setColor("#28a745");
        eventos.add(evento2);

        EventoDTO evento3 = new EventoDTO();
        evento3.setId("3");
        evento3.setTitle("Videochamada - Atualização do Caso");
        evento3.setStart(LocalDateTime.now().plusDays(7).withHour(16).withMinute(0));
        evento3.setEnd(LocalDateTime.now().plusDays(7).withHour(16).withMinute(30));
        evento3.setColor("#dc3545");
        eventos.add(evento3);

        return eventos;
    }
}