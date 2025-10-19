package org.stm.atty.service;

import org.stm.atty.model.Usuario;
import org.stm.atty.dto.UsuarioDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UsuarioService {


    public List<UsuarioDTO> listarTodos() {
        List<Usuario> usuarios = Usuario.listAll();
        return usuarios.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UsuarioDTO buscarPorId(Long id) {
        Usuario usuario = Usuario.findById(id);
        return usuario != null ? toDTO(usuario) : null;
    }

    public UsuarioDTO buscarPorEmail(String email) {
        Usuario usuario = Usuario.findByEmail(email);
        return usuario != null ? toDTO(usuario) : null;
    }


    public Usuario buscarCompletoPorEmail(String email) {
        return Usuario.findByEmail(email);
    }


    @Transactional
    public UsuarioDTO atualizar(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuario = Usuario.findById(id);
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        usuario.nome = usuarioDTO.getNome();
        usuario.email = usuarioDTO.getEmail();
        usuario.perfil = usuarioDTO.getPerfil();
        usuario.telefone = usuarioDTO.getTelefone();
        usuario.endereco = usuarioDTO.getEndereco();
        usuario.cidade = usuarioDTO.getCidade();
        usuario.dataAtualizacao = java.time.LocalDateTime.now();

        return toDTO(usuario);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario.deleteById(id);
    }


    private UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;

        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.id);
        dto.setEmail(usuario.email);
        dto.setNome(usuario.nome);
        dto.setPerfil(usuario.perfil);
        dto.setTelefone(usuario.telefone);
        dto.setEndereco(usuario.endereco);
        dto.setCidade(usuario.cidade);
        dto.setAtivo(usuario.ativo);
        dto.setDataCriacao(usuario.dataCriacao);
        dto.setDataAtualizacao(usuario.dataAtualizacao);
        return dto;
    }
}