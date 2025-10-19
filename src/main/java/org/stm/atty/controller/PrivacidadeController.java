package org.stm.atty.controller;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/privacidade")
public class PrivacidadeController {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance privacidade();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return PrivacidadeController.Templates.privacidade();
    }

}