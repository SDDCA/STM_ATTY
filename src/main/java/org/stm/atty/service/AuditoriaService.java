package org.stm.atty.service;

import org.stm.atty.model.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class AuditoriaService {

    @Inject
    EntityManager em;

    @Transactional
    public void registrarLogin(Usuario usuario) {
        String sql = "INSERT INTO auditoria (usuario_id, usuario_nome, acao, entidade, descricao, data_hora) " +
                "VALUES (:usuarioId, :usuarioNome, :acao, :entidade, :descricao, :dataHora)";

        em.createNativeQuery(sql)
                .setParameter("usuarioId", usuario.id)
                .setParameter("usuarioNome", usuario.nome)
                .setParameter("acao", "LOGIN")
                .setParameter("entidade", "usuarios")
                .setParameter("descricao", "Usuário realizou login no sistema")
                .setParameter("dataHora", LocalDateTime.now())
                .executeUpdate();

        System.out.println("✅ Login registrado na auditoria: " + usuario.nome);
    }

    @Transactional
    public void registrarAcao(Long usuarioId, String usuarioNome, String acao,
                              String entidade, Long entidadeId, String descricao) {
        String sql = "INSERT INTO auditoria (usuario_id, usuario_nome, acao, entidade, entidade_id, descricao, data_hora) " +
                "VALUES (:usuarioId, :usuarioNome, :acao, :entidade, :entidadeId, :descricao, :dataHora)";

        em.createNativeQuery(sql)
                .setParameter("usuarioId", usuarioId)
                .setParameter("usuarioNome", usuarioNome)
                .setParameter("acao", acao)
                .setParameter("entidade", entidade)
                .setParameter("entidadeId", entidadeId)
                .setParameter("descricao", descricao)
                .setParameter("dataHora", LocalDateTime.now())
                .executeUpdate();
    }

    @Transactional
    public void registrarCriacao(Usuario usuario, String entidade, Long entidadeId, String descricao) {
        registrarAcao(usuario.id, usuario.nome, "CRIAR", entidade, entidadeId, descricao);
    }

    @Transactional
    public void registrarEdicao(Usuario usuario, String entidade, Long entidadeId, String descricao) {
        registrarAcao(usuario.id, usuario.nome, "EDITAR", entidade, entidadeId, descricao);
    }

    @Transactional
    public void registrarExclusao(Usuario usuario, String entidade, Long entidadeId, String descricao) {
        registrarAcao(usuario.id, usuario.nome, "EXCLUIR", entidade, entidadeId, descricao);
    }
}