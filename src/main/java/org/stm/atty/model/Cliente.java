package org.stm.atty.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente extends PanacheEntity {

    @Column(nullable = false)
    public String nome;

    public String email;
    public String telefone;

    @Column(name = "cpf_cnpj")
    public String cpfCnpj;

    @Enumerated(EnumType.STRING)
    public TipoCliente tipo = TipoCliente.FISICA;

    @Column(columnDefinition = "TEXT")
    public String endereco;

    public String cidade;
    public String estado;
    public String cep;

    // Relacionamento com usuário (se cliente tiver conta no sistema)
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    public Usuario usuario;

    // Advogado responsável pelo cliente
    @ManyToOne
    @JoinColumn(name = "advogado_responsavel_id")
    public Usuario advogadoResponsavel;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public StatusCliente status = StatusCliente.ATIVO;

    // Auditoria
    @Column(name = "data_criacao")
    public LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    public LocalDateTime dataAtualizacao;

    // Enums
    public enum TipoCliente {
        FISICA("Pessoa Física"),
        JURIDICA("Pessoa Jurídica");

        public final String label;

        TipoCliente(String label) {
            this.label = label;
        }
    }

    public enum StatusCliente {
        ATIVO("Ativo"),
        INATIVO("Inativo"),
        ARQUIVADO("Arquivado");

        public final String label;

        StatusCliente(String label) {
            this.label = label;
        }
    }

    // Construtor
    public Cliente() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.status = StatusCliente.ATIVO;
        this.tipo = TipoCliente.FISICA;
    }

    // Métodos de busca estáticos
    public static List<Cliente> findByAdvogado(Usuario advogado) {
        return list("advogadoResponsavel = ?1 AND status = ?2 ORDER BY nome",
                advogado, StatusCliente.ATIVO);
    }

    public static List<Cliente> findByStatus(StatusCliente status) {
        return list("status = ?1 ORDER BY nome", status);
    }

    public static Cliente findByCpfCnpj(String cpfCnpj) {
        return find("cpfCnpj = ?1", cpfCnpj.replaceAll("[^0-9]", "")).firstResult();
    }

    public static Cliente findByEmail(String email) {
        return find("LOWER(email) = LOWER(?1)", email).firstResult();
    }

    public static List<Cliente> findByNome(String nome) {
        return list("LOWER(nome) LIKE LOWER(?1) ORDER BY nome", "%" + nome + "%");
    }

    public static List<Cliente> findAtivos() {
        return list("status = ?1 ORDER BY nome", StatusCliente.ATIVO);
    }

    public static long countByAdvogado(Usuario advogado) {
        return count("advogadoResponsavel = ?1 AND status = ?2",
                advogado, StatusCliente.ATIVO);
    }

    // Métodos de negócio
    public void inativar() {
        this.status = StatusCliente.INATIVO;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void reativar() {
        this.status = StatusCliente.ATIVO;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void arquivar() {
        this.status = StatusCliente.ARQUIVADO;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public boolean temUsuario() {
        return usuario != null;
    }

    public boolean isPessoaFisica() {
        return tipo == TipoCliente.FISICA;
    }

    public boolean isPessoaJuridica() {
        return tipo == TipoCliente.JURIDICA;
    }

    // Validação de CPF/CNPJ
    public String getCpfCnpjFormatado() {
        if (cpfCnpj == null) return "";

        String numeros = cpfCnpj.replaceAll("[^0-9]", "");

        if (numeros.length() == 11) {
            // CPF: 000.000.000-00
            return numeros.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        } else if (numeros.length() == 14) {
            // CNPJ: 00.000.000/0000-00
            return numeros.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }

        return cpfCnpj;
    }

    // Lifecycle callbacks
    @PrePersist
    public void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        dataAtualizacao = LocalDateTime.now();

        // Remove formatação do CPF/CNPJ antes de salvar
        if (cpfCnpj != null) {
            cpfCnpj = cpfCnpj.replaceAll("[^0-9]", "");
        }
    }

    @PreUpdate
    public void preUpdate() {
        dataAtualizacao = LocalDateTime.now();

        // Remove formatação do CPF/CNPJ antes de salvar
        if (cpfCnpj != null) {
            cpfCnpj = cpfCnpj.replaceAll("[^0-9]", "");
        }
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", tipo=" + tipo +
                ", status=" + status +
                '}';
    }
}