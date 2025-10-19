package org.stm.atty.controller;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.stm.atty.model.Processo;
import org.stm.atty.model.Usuario;
import org.stm.atty.model.Cliente;
import org.stm.atty.dto.ProcessoDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Path("/processos")
public class ProcessosController {

    @Inject
    @Location("ProcessosController/processos.html")
    Template processosTemplate;

    @Inject
    @Location("ProcessosController/processodetalhe.html")
    Template processodetalheTemplate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance processos() {
        List<ProcessoDTO> processosList = Processo.listAll().stream()
                .map((PanacheEntityBase processo) -> toDTO((Processo) processo))
                .collect(Collectors.toList());

        Usuario usuario = new Usuario();
        usuario.nome = "Dr. Arthur Silva";
        usuario.email = "arthur.silva@stm-adv.com.br";
        usuario.perfil = "ADVOGADO";

        return processosTemplate
                .data("processos", processosList)
                .data("usuario", usuario);
    }

    @GET
    @Path("/detalhe")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance processodetalhe(@QueryParam("id") Long processoId) {
        Processo processo = Processo.findById(processoId);

        if (processo == null) {
            throw new WebApplicationException("Processo não encontrado", Response.Status.NOT_FOUND);
        }

        ProcessoDTO processoDTO = toDTO(processo);
        Usuario usuario = new Usuario();
        usuario.nome = "Dr. Arthur Silva";
        usuario.email = "arthur.silva@stm-adv.com.br";
        usuario.perfil = "ADVOGADO";

        return processodetalheTemplate
                .data("processo", processoDTO)
                .data("usuario", usuario);
    }

    @POST
    @Path("/novo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response criarProcesso(ProcessoRequest request) {
        try {
            // Validar dados
            if (request.numero == null || request.numero.trim().isEmpty() ||
                    request.descricao == null || request.descricao.trim().isEmpty()) {
                return Response.status(400).entity("Número e descrição são obrigatórios").build();
            }

            // Buscar cliente
            Cliente cliente = Cliente.findById(request.clienteId);
            if (cliente == null) {
                return Response.status(400).entity("Cliente não encontrado").build();
            }

            // Buscar usuário responsável (por enquanto fixo, depois pega do usuário logado)
            Usuario usuarioResponsavel = Usuario.findById(1L);

            Processo processo = new Processo();
            processo.numero = request.numero;
            processo.descricao = request.descricao;
            processo.status = request.status != null ? request.status : "EM_ANDAMENTO";
            processo.dataAbertura = LocalDate.now();
            processo.dataCriacao = java.time.LocalDateTime.now();
            processo.cliente = cliente;
            processo.usuarioResponsavel = usuarioResponsavel;

            processo.persist();

            // Retorna o DTO
            ProcessoDTO processoDTO = toDTO(processo);
            return Response.status(201).entity(processoDTO).build();

        } catch (Exception e) {
            return Response.status(500).entity("Erro ao criar processo: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizarProcesso(@PathParam("id") Long id, ProcessoRequest request) {
        try {
            Processo processo = Processo.findById(id);
            if (processo == null) {
                return Response.status(404).entity("Processo não encontrado").build();
            }

            // Atualizar dados
            processo.numero = request.numero;
            processo.descricao = request.descricao;
            processo.status = request.status;

            ProcessoDTO processoDTO = toDTO(processo);
            return Response.ok(processoDTO).build();

        } catch (Exception e) {
            return Response.status(500).entity("Erro ao atualizar processo: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response excluirProcesso(@PathParam("id") Long id) {
        try {
            boolean deleted = Processo.deleteById(id);
            if (!deleted) {
                return Response.status(404).entity("Processo não encontrado").build();
            }
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(500).entity("Erro ao excluir processo: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response mudarStatus(@PathParam("id") Long id, StatusRequest statusRequest) {
        try {
            Processo processo = Processo.findById(id);
            if (processo == null) {
                return Response.status(404).entity("Processo não encontrado").build();
            }

            processo.status = statusRequest.getStatus();
            return Response.ok().build();

        } catch (Exception e) {
            return Response.status(500).entity("Erro ao alterar status: " + e.getMessage()).build();
        }
    }

    //Método de conversão de Entidade para DTO
    private ProcessoDTO toDTO(Processo processo) {
        ProcessoDTO dto = new ProcessoDTO();
        dto.setId(processo.id.toString());
        dto.setNumero(processo.numero);
        dto.setCliente(processo.cliente != null ? processo.cliente.nome : "Cliente não informado");
        dto.setDescricao(processo.descricao);
        dto.setStatus(processo.status);
        dto.setDataCriacao(processo.dataCriacao != null ?
                processo.dataCriacao.format(DateTimeFormatter.ISO_DATE) :
                LocalDate.now().toString());
        return dto;
    }

    public static class ProcessoRequest {
        public String numero;
        public String descricao;
        public String status;
        public Long clienteId;

        public String getNumero() { return numero; }
        public void setNumero(String numero) { this.numero = numero; }

        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Long getClienteId() { return clienteId; }
        public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    }

    public static class StatusRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}