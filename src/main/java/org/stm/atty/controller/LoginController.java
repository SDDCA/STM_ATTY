package org.stm.atty.controller;

import org.stm.atty.dto.LoginDTO;
import org.stm.atty.model.Usuario;
import org.stm.atty.service.AuthService;
import org.stm.atty.service.AuditoriaService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/login")
public class LoginController {

    @Inject
    AuthService authService;

    @Inject
    AuditoriaService auditoriaService;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance login();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return Templates.login();
    }

    @POST
    @Path("/logar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logarPost(LoginDTO dto) {
        try {
            // Autentica usuário
            String token = authService.autenticar(dto);

            // Busca o usuário que fez login
            String email = dto.getEmail().toLowerCase();
            Usuario usuario = Usuario.findByEmail(email);

            if (usuario == null) {
                throw new RuntimeException("Usuário não encontrado");
            }

            // Registra login na auditoria
            auditoriaService.registrarLogin(usuario);

            // Define URL de redirecionamento baseado no perfil
            String redirectUrl = switch (usuario.perfil) {
                case MASTER -> "/admin/dashboard";
                case ADVOGADO -> "/dashboard";
                case CLIENTE -> "/dashboard-cliente";
            };

            // Retorna resposta com token e informações
            return Response.ok()
                    .entity("{"
                            + "\"token\": \"" + token + "\", "
                            + "\"success\": true, "
                            + "\"redirectUrl\": \"" + redirectUrl + "\", "
                            + "\"tipoUsuario\": \"" + usuario.perfil.name().toLowerCase() + "\", "
                            + "\"nome\": \"" + usuario.nome + "\""
                            + "}")
                    .build();

        } catch (RuntimeException e) {
            System.err.println("❌ Erro no login: " + e.getMessage());

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{"
                            + "\"error\": \"" + e.getMessage() + "\", "
                            + "\"success\": false"
                            + "}")
                    .build();
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado no login: " + e.getMessage());
            e.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{"
                            + "\"error\": \"Erro interno no servidor\", "
                            + "\"success\": false"
                            + "}")
                    .build();
        }
    }

    // Endpoint para verificar validade do token
    @GET
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verificarToken(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"valid\": false}")
                        .build();
            }

            String token = authHeader.substring(7);
            // Aqui você deve validar o token JWT
            // Por enquanto, retornamos true se o token existe

            return Response.ok()
                    .entity("{\"valid\": true}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"valid\": false}")
                    .build();
        }
    }
}