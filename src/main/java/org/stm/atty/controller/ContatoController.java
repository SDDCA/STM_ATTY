package org.stm.atty.controller;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/contato")
public class ContatoController {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance contato();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return ContatoController.Templates.contato();
    }

}