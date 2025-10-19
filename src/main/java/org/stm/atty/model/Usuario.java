package org.stm.atty.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String email;

    @Column(nullable = false)
    public String senha;

    @Column(nullable = false)
    public String nome;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public Perfil perfil;

    // Informações profissionais (ADVOGADO)
    public String oab;

    @Column(columnDefinition = "TEXT")
    public String especialidades;

    // Informações de contato
    public String telefone;

    @Column(columnDefinition = "TEXT")
    public String endereco;

    public String cidade;
    public String estado;
    public String cep;

    // Controle de status
    @Column(nullable = false)
    public Boolean ativo = true;

    @Column(nullable = false)
    public Boolean aprovado = false;

    // Auditoria
    @Column(name = "data_criacao")
    public LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    public LocalDateTime dataAtualizacao;

    @Column(name = "ultimo_acesso")
    public LocalDateTime ultimoAcesso;

    public enum Perfil {
        MASTER,
        ADVOGADO,
        CLIENTE
    }

    public Usuario() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.ativo = true;
        this.perfil = Perfil.CLIENTE;
    }

    // Métodos estáticos de busca
    public static Usuario findByEmail(String email) {
        return find("LOWER(email) = LOWER(?1) AND ativo = true", email).firstResult();
    }

    public static Usuario findByEmailIncludeInactive(String email) {
        return find("LOWER(email) = LOWER(?1)", email).firstResult();
    }

    public static Boolean existsByEmail(String email) {
        return find("LOWER(email) = LOWER(?1)", email).count() > 0;
    }

    public static java.util.List<Usuario> findByPerfil(Perfil perfil) {
        return list("perfil = ?1 AND ativo = true", perfil);
    }

    public static java.util.List<Usuario> findAdvogadosPendentes() {
        return list("perfil = ?1 AND aprovado = false AND ativo = true", Perfil.ADVOGADO);
    }

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

    // Método para verificar se precisa aprovação
    public boolean precisaAprovacao() {
        return perfil == Perfil.ADVOGADO && !aprovado;
    }
}