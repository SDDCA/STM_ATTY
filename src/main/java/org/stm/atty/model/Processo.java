package org.stm.atty.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "processos")
public class Processo extends PanacheEntity {

    @Column(name = "numero_processo", unique = true, nullable = false)
    public String numeroProcesso;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String descricao;

    public String tipo; // Trabalhista, Civil, Criminal, etc
    public String area; // Área do direito

    // Status do processo
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Status status = Status.EM_ANDAMENTO;

    @Enumerated(EnumType.STRING)
    public Prioridade prioridade = Prioridade.MEDIA;

    // Dados do tribunal
    public String tribunal;
    public String vara;
    public String comarca;

    // Relacionamentos
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    public Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "advogado_responsavel_id")
    public Usuario advogadoResponsavel;

    // Datas importantes
    @Column(name = "data_abertura")
    public LocalDate dataAbertura;

    @Column(name = "data_distribuicao")
    public LocalDate dataDistribuicao;

    @Column(name = "data_conclusao")
    public LocalDate dataConclusao;

    @Column(name = "prazo_final")
    public LocalDate prazoFinal;

    // Valores
    @Column(name = "valor_causa")
    public BigDecimal valorCausa;

    @Column(name = "valor_condenacao")
    public BigDecimal valorCondenacao;

    // Observações
    @Column(columnDefinition = "TEXT")
    public String observacoes;

    // Auditoria
    @Column(name = "data_criacao")
    public LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    public LocalDateTime dataAtualizacao;

    // Enums
    public enum Status {
        EM_ANDAMENTO("Em Andamento"),
        CONCLUIDO("Concluído"),
        ARQUIVADO("Arquivado"),
        SUSPENSO("Suspenso");

        public final String label;

        Status(String label) {
            this.label = label;
        }
    }

    public enum Prioridade {
        BAIXA("Baixa"),
        MEDIA("Média"),
        ALTA("Alta"),
        URGENTE("Urgente");

        public final String label;

        Prioridade(String label) {
            this.label = label;
        }
    }

    // Construtor
    public Processo() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.status = Status.EM_ANDAMENTO;
        this.prioridade = Prioridade.MEDIA;
    }

    // Métodos de busca estáticos
    public static List<Processo> findByAdvogado(Usuario advogado) {
        return list("advogadoResponsavel = ?1 ORDER BY dataCriacao DESC", advogado);
    }

    public static List<Processo> findByCliente(Cliente cliente) {
        return list("cliente = ?1 ORDER BY dataCriacao DESC", cliente);
    }

    public static List<Processo> findByStatus(Status status) {
        return list("status = ?1 ORDER BY dataCriacao DESC", status);
    }

    public static List<Processo> findByNumero(String numero) {
        return list("LOWER(numeroProcesso) LIKE LOWER(?1)", "%" + numero + "%");
    }

    public static List<Processo> findAtivosComPrazo() {
        return list("status = ?1 AND prazoFinal IS NOT NULL AND prazoFinal >= ?2 ORDER BY prazoFinal ASC",
                Status.EM_ANDAMENTO, LocalDate.now());
    }

    public static List<Processo> findPrazosProximos(int dias) {
        LocalDate dataLimite = LocalDate.now().plusDays(dias);
        return list("status = ?1 AND prazoFinal BETWEEN ?2 AND ?3 ORDER BY prazoFinal ASC",
                Status.EM_ANDAMENTO, LocalDate.now(), dataLimite);
    }

    public static long countByAdvogado(Usuario advogado) {
        return count("advogadoResponsavel = ?1", advogado);
    }

    public static long countByStatus(Status status) {
        return count("status = ?1", status);
    }

    // Métodos de negócio
    public boolean estaPrazoVencido() {
        return prazoFinal != null && prazoFinal.isBefore(LocalDate.now());
    }

    public boolean estaPrazoProximo(int dias) {
        if (prazoFinal == null) return false;
        LocalDate dataLimite = LocalDate.now().plusDays(dias);
        return prazoFinal.isAfter(LocalDate.now()) && prazoFinal.isBefore(dataLimite);
    }

    public long getDiasAtePrazo() {
        if (prazoFinal == null) return -1;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), prazoFinal);
    }

    public void concluir() {
        this.status = Status.CONCLUIDO;
        this.dataConclusao = LocalDate.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void arquivar() {
        this.status = Status.ARQUIVADO;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void suspender() {
        this.status = Status.SUSPENSO;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void reativar() {
        this.status = Status.EM_ANDAMENTO;
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Lifecycle callbacks
    @PrePersist
    public void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    // ToString para debug
    @Override
    public String toString() {
        return "Processo{" +
                "id=" + id +
                ", numero='" + numeroProcesso + '\'' +
                ", cliente=" + (cliente != null ? cliente.nome : "null") +
                ", status=" + status +
                '}';
    }
}