package org.stm.atty.controller;

import org.stm.atty.dto.CadastroDTO;
import org.stm.atty.model.Usuario;
import org.stm.atty.service.AuthService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/cadastro")
public class CadastroController {

    @Inject
    AuthService authService;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance cadastro();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return Templates.cadastro();
    }

    @POST
    @Path("cadastrar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cadastrarPost(CadastroDTO dto) {
        try {
            String token = authService.cadastrar(dto);

            // Busca o usuário recém-criado para redirecionamento
            String email = dto.getEmail().toLowerCase();
            Usuario usuario = Usuario.find("LOWER(email) = ?1", email).firstResult();

            String redirectUrl = "ADVOGADO".equals(usuario.perfil) ? "/dashboard" : "/dashboard-cliente";

            return Response.ok()
                    .entity("{\"token\": \"" + token +
                            "\", \"success\": true, " +
                            "\"redirectUrl\": \"" + redirectUrl +
                            "\", \"message\": \"Usuário cadastrado com sucesso!\"}")
                    .build();

        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"success\": false, \"error\": \"Erro interno no servidor\"}")
                    .build();
        }
    }
}