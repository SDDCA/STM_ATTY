package org.stm.atty.dto;

public class ProcessoDTO {
    private String id;
    private String numero;
    private String cliente;
    private String descricao;
    private String status;
    private String dataCriacao;

    public ProcessoDTO() {}

    public ProcessoDTO(String id, String numero, String cliente, String descricao, String status, String dataCriacao) {
        this.id = id;
        this.numero = numero;
        this.cliente = cliente;
        this.descricao = descricao;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }
}