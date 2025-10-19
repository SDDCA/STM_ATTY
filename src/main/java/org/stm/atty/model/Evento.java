package org.stm.atty.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
public class Evento extends PanacheEntity {

    @Column(name = "titulo")
    public String titulo;

    @Column(name = "descricao")
    public String descricao;

    @Column(name = "data_inicio")
    public LocalDateTime dataInicio;

    @Column(name = "data_fim")
    public LocalDateTime dataFim;

    @Column(name = "cor")
    public String cor;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    public Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "processo_id")
    public Processo processo;

    @Column(name = "data_criacao")
    public LocalDateTime dataCriacao;
}