package org.stm.atty.controller;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/sobre")
public class SobreController {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance sobre();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return SobreController.Templates.sobre();
    }

}