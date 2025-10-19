package org.stm.atty.controller;

import org.stm.atty.dto.AuditoriaDTO;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/auditoria")
public class AuditoriaController {

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance auditoria();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return Templates.auditoria();
    }

    @GET
    @Path("/logs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogs() {
        try {
            List<AuditoriaDTO> logs = new ArrayList<>();

            AuditoriaDTO log1 = new AuditoriaDTO();
            log1.setId("1");
            log1.setUsuario("Dr. Silva");
            log1.setAcao("LOGIN");
            log1.setDescricao("Usuário fez login no sistema");
            log1.setDataHora(LocalDateTime.now().minusHours(2));
            logs.add(log1);

            AuditoriaDTO log2 = new AuditoriaDTO();
            log2.setId("2");
            log2.setUsuario("Dr. Silva");
            log2.setAcao("CRIAR_PROCESSO");
            log2.setDescricao("Criou novo processo para João Silva");
            log2.setDataHora(LocalDateTime.now().minusHours(1));
            logs.add(log2);

            return Response.ok(logs).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/log")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarLog(AuditoriaDTO log) {
        try {
            System.out.println("Novo log: " + log.getAcao() + " - " + log.getUsuario());
            return Response.ok().entity("{\"status\": \"success\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}