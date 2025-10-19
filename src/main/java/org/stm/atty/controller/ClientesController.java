package org.stm.atty.controller;

import org.stm.atty.dto.ClienteDTO;
import org.stm.atty.dto.UsuarioDTO;
import org.stm.atty.service.AuthService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/clientes")
public class ClientesController {

    @Inject
    AuthService authService;

    // "Banco de dados" em memória (substituir por H2 depois)
    private List<ClienteDTO> clientes = new ArrayList<>();

    public ClientesController() {
        // Dados iniciais
        ClienteDTO cliente1 = new ClienteDTO();
        cliente1.setId("1");
        cliente1.setNome("João Silva");
        cliente1.setEmail("joao.silva@email.com");
        cliente1.setTelefone("(61) 99999-0000");
        cliente1.setStatus("Ativo");
        clientes.add(cliente1);

        ClienteDTO cliente2 = new ClienteDTO();
        cliente2.setId("2");
        cliente2.setNome("Maria Souza");
        cliente2.setEmail("maria.souza@email.com");
        cliente2.setTelefone("(61) 98888-1111");
        cliente2.setStatus("Inativo");
        clientes.add(cliente2);
    }

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance clientes(UsuarioDTO usuario, List<ClienteDTO> clientes, String activePage);
    }

    // RENDERIZA TEMPLATE COM DADOS
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString("Authorization");
        UsuarioDTO usuario = authService.getUsuarioLogado(authHeader);

        if (usuario == null) {
            throw new WebApplicationException("Não autorizado", Response.Status.UNAUTHORIZED);
        }

        return Templates.clientes(usuario, clientes, "clientes");
    }

    // API PARA CLIENTES (JSON) - mantido para SPA
    @GET
    @Path("/lista")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientes(@Context HttpHeaders headers) {
        try {
            String authHeader = headers.getHeaderString("Authorization");
            UsuarioDTO usuario = authService.getUsuarioLogado(authHeader);

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            return Response.ok(clientes).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // API PARA CRIAR CLIENTE
    @POST
    @Path("/novo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response criarCliente(@Context HttpHeaders headers, ClienteDTO cliente) {
        try {
            String authHeader = headers.getHeaderString("Authorization");
            UsuarioDTO usuario = authService.getUsuarioLogado(authHeader);

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            System.out.println("Novo cliente: " + cliente.getNome());
            // Simular criação
            cliente.setId(String.valueOf(clientes.size() + 1));
            clientes.add(cliente);
            return Response.ok(cliente).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // API PARA ATUALIZAR CLIENTE
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizarCliente(@Context HttpHeaders headers, @PathParam("id") String id, ClienteDTO clienteAtualizado) {
        try {
            String authHeader = headers.getHeaderString("Authorization");
            UsuarioDTO usuario = authService.getUsuarioLogado(authHeader);

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            for (ClienteDTO cliente : clientes) {
                if (cliente.getId().equals(id)) {
                    cliente.setNome(clienteAtualizado.getNome());
                    cliente.setEmail(clienteAtualizado.getEmail());
                    cliente.setTelefone(clienteAtualizado.getTelefone());
                    cliente.setStatus(clienteAtualizado.getStatus());
                    return Response.ok(cliente).build();
                }
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // API PARA EXCLUIR CLIENTE
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response excluirCliente(@Context HttpHeaders headers, @PathParam("id") String id) {
        try {
            String authHeader = headers.getHeaderString("Authorization");
            UsuarioDTO usuario = authService.getUsuarioLogado(authHeader);

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            clientes.removeIf(cliente -> cliente.getId().equals(id));
            return Response.ok().entity("{\"status\": \"success\", \"message\": \"Cliente excluído\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}