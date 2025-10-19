package org.stm.atty.controller;

import org.stm.atty.dto.DashboardDTO;
import org.stm.atty.dto.EventoDTO;
import org.stm.atty.dto.UsuarioDTO;
import org.stm.atty.dto.ProcessoDTO;
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

@Path("/dashboard")
public class DashboardController {

    @Inject
    AuthService authService;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance dashboard();
    }

    // ENDPOINT PRINCIPAL - RENDERIZA TEMPLATE COMPLETO COM DADOS
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString("Authorization");
        UsuarioDTO usuario = authService.getUsuarioLogado(authHeader);

        if (usuario == null) {
            throw new WebApplicationException("Não autorizado", Response.Status.UNAUTHORIZED);
        }

        Map<String, Object> dadosTemplate = prepararDadosTemplate(usuario);
        return Templates.dashboard().data(dadosTemplate);
    }

    // ENDPOINTS DA API MANTIDOS PARA COMPATIBILIDADE
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

            Map<String, Object> dados = prepararDadosDashboard(usuario);
            return Response.ok(dados).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/eventos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventos(@Context HttpHeaders headers) {
        try {
            String authHeader = headers.getHeaderString("Authorization");
            UsuarioDTO usuario = authService.getUsuarioLogado(authHeader);

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            List<EventoDTO> eventos = getEventosDoBanco(usuario);
            return Response.ok(eventos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/evento")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response adicionarEvento(@Context HttpHeaders headers, EventoDTO evento) {
        try {
            String authHeader = headers.getHeaderString("Authorization");
            UsuarioDTO usuario = authService.getUsuarioLogado(authHeader);

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            System.out.println("Novo evento: " + evento.getTitle() + " para usuário: " + usuario.getNome());
            // Aqui você salvaria no banco de dados
            // eventoService.salvarEvento(evento, usuario.getId());

            return Response.ok().entity("{\"status\": \"success\", \"message\": \"Evento adicionado\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Map<String, Object> prepararDadosTemplate(UsuarioDTO usuario) {
        Map<String, Object> dados = new HashMap<>();

        dados.put("usuario", usuario);
        dados.put("dados", prepararDadosDashboardDTO(usuario));
        dados.put("eventos", getEventosDoBanco(usuario));
        dados.put("processo", new ProcessoDTO());

        return dados;
    }

    private Map<String, Object> prepararDadosDashboard(UsuarioDTO usuario) {
        Map<String, Object> dados = new HashMap<>();

        dados.put("usuario", usuario);
        dados.put("dashboard", prepararDadosDashboardDTO(usuario));
        dados.put("eventos", getEventosDoBanco(usuario));

        return dados;
    }

    private DashboardDTO prepararDadosDashboardDTO(UsuarioDTO usuario) {
        DashboardDTO dados = new DashboardDTO();
        dados.setNomeUsuario(usuario.getNome());
        dados.setTotalProcessos(12);  // Futuramente: Processo.count()
        dados.setTotalClientes(8);    // Futuramente: Cliente.count()
        dados.setCompromissosHoje(3); // Futuramente: Evento.count("data = hoje")
        return dados;
    }

    // MÉTODO AUXILIAR PARA BUSCAR EVENTOS DO BANCO
    private List<EventoDTO> getEventosDoBanco(UsuarioDTO usuario) {
        List<EventoDTO> eventos = new ArrayList<>();

        // 🔥 FUTURAMENTE: Substituir por consulta real ao banco
        // eventos = Evento.find("usuario_id = ?1", usuario.getId()).list()
        //     .stream().map(this::toEventoDTO).collect(Collectors.toList());

        // 🔥 DADOS MOCKADOS TEMPORÁRIOS - REMOVER QUANDO TIVER BANCO
        if (usuario.getPerfil().equals("ADVOGADO")) {
            EventoDTO evento1 = new EventoDTO();
            evento1.setId("1");
            evento1.setTitle("Reunião com Cliente - Maria Santos");
            evento1.setStart(LocalDateTime.of(2025, 7, 25, 14, 0));
            evento1.setEnd(LocalDateTime.of(2025, 7, 25, 15, 0));
            evento1.setColor("#007bff");
            eventos.add(evento1);

            EventoDTO evento2 = new EventoDTO();
            evento2.setId("2");
            evento2.setTitle("Audiência - Processo 0001234-56.2025.8.07.0001");
            evento2.setStart(LocalDateTime.of(2025, 7, 26, 10, 0));
            evento2.setEnd(LocalDateTime.of(2025, 7, 26, 12, 0));
            evento2.setColor("#28a745");
            eventos.add(evento2);

            EventoDTO evento3 = new EventoDTO();
            evento3.setId("3");
            evento3.setTitle("Consulta Jurídica - Novo Cliente");
            evento3.setStart(LocalDateTime.of(2025, 7, 27, 16, 0));
            evento3.setEnd(LocalDateTime.of(2025, 7, 27, 17, 0));
            evento3.setColor("#dc3545");
            eventos.add(evento3);
        } else {
            // Eventos para cliente
            EventoDTO evento1 = new EventoDTO();
            evento1.setId("1");
            evento1.setTitle("Reunião com Advogado");
            evento1.setStart(LocalDateTime.of(2025, 7, 25, 14, 0));
            evento1.setEnd(LocalDateTime.of(2025, 7, 25, 15, 0));
            evento1.setColor("#007bff");
            eventos.add(evento1);
        }

        return eventos;
    }
}