package org.stm.atty.controller;

import org.stm.atty.service.AuthService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/logout")
public class LogoutController {

    @Inject
    AuthService authService;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance logout();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance logoutPage() {
        return Templates.logout();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("Authorization") String authHeader) {
        try {
            // Extrai o token do header
            String token = extrairToken(authHeader);

            // 🔥 Log do logout
            System.out.println("🚪 Solicitação de logout recebida");
            if (token != null) {
                System.out.println("🔐 Token invalidado: " + token.substring(0, Math.min(20, token.length())) + "...");
            }

            // 🔥 Para JWT, o logout é basicamente o frontend esquecer o token
            // Não há muito o que fazer no backend com JWT stateless

            return Response.ok()
                    .entity("{\"success\": true, \"message\": \"Logout realizado com sucesso\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"success\": false, \"error\": \"Erro no logout\"}")
                    .build();
        }
    }

    private String extrairToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}