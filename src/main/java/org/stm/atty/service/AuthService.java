package org.stm.atty.service;

import org.eclipse.microprofile.jwt.Claims;
import org.stm.atty.model.Usuario;
import org.stm.atty.dto.UsuarioDTO;
import org.stm.atty.dto.LoginDTO;
import org.stm.atty.dto.CadastroDTO;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    @Transactional
    public String autenticar(LoginDTO loginDTO) {
        String email = loginDTO.getEmail().trim().toLowerCase();
        Usuario usuario = Usuario.findByEmail(email);

        if (usuario == null || !usuario.ativo) {
            throw new RuntimeException("Email ou senha inválidos");
        }

        // Verificação de senha (em produção use BCrypt)
        if (!usuario.senha.equals(loginDTO.getSenha().trim())) {
            throw new RuntimeException("Email ou senha inválidos");
        }

        // Verifica se advogado está aprovado
        if (usuario.perfil == Usuario.Perfil.ADVOGADO && !usuario.aprovado) {
            throw new RuntimeException("Sua conta ainda está pendente de aprovação");
        }

        // Atualiza último acesso
        usuario.ultimoAcesso = java.time.LocalDateTime.now();
        usuario.persist();

        // Gera JWT
        String token = gerarJwtToken(usuario);

        System.out.println("✅ Login realizado: " + usuario.nome + " (" + usuario.perfil + ")");
        return token;
    }

    @Transactional
    public String cadastrar(CadastroDTO cadastroDTO) {
        String email = cadastroDTO.getEmail().trim().toLowerCase();

        if (Usuario.existsByEmail(email)) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.email = email;
        usuario.senha = cadastroDTO.getSenha().trim(); // Em produção: BCrypt
        usuario.nome = cadastroDTO.getNome().trim();

        // Define perfil
        String tipoUsuario = cadastroDTO.getTipoUsuario() != null ?
                cadastroDTO.getTipoUsuario().toUpperCase() : "CLIENTE";
        usuario.perfil = Usuario.Perfil.valueOf(tipoUsuario);

        usuario.ativo = true;

        // Clientes são aprovados automaticamente
        // Advogados precisam de aprovação do master
        if (usuario.perfil == Usuario.Perfil.CLIENTE) {
            usuario.aprovado = true;
        } else {
            usuario.aprovado = false;
        }

        usuario.persist();

        // Se for advogado, não gera token (precisa aprovação)
        if (usuario.perfil == Usuario.Perfil.ADVOGADO) {
            System.out.println("⚠️ Advogado cadastrado, aguardando aprovação: " + usuario.nome);
            throw new RuntimeException("Cadastro realizado! Sua conta será analisada e você receberá um email quando for aprovada.");
        }

        // Gera JWT para clientes
        String token = gerarJwtToken(usuario);
        System.out.println("✅ Novo usuário cadastrado: " + usuario.nome);
        return token;
    }

    private String gerarJwtToken(Usuario usuario) {
        Set<String> groups = new HashSet<>();
        groups.add(usuario.perfil.name());

        return Jwt.issuer("https://stm-atty.com")
                .upn(usuario.email)
                .subject(usuario.id.toString())
                .groups(groups)
                .claim(Claims.full_name, usuario.nome)
                .claim("perfil", usuario.perfil.name())
                .expiresIn(Duration.ofHours(24))
                .sign();
    }

    public UsuarioDTO getUsuarioLogado(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                // Em produção, você faria o parsing do JWT aqui
                // Por enquanto, vamos buscar pelo contexto de segurança
                return getCurrentUserFromDatabase();
            } catch (Exception e) {
                System.out.println("⚠️ Token inválido: " + e.getMessage());
            }
        }
        return null;
    }

    private UsuarioDTO getCurrentUserFromDatabase() {
        // Retorna primeiro usuário ativo (em produção, vem do contexto JWT)
        Usuario usuario = Usuario.find("ativo = ?1", true).firstResult();
        if (usuario != null) {
            return toDTO(usuario);
        }
        return null;
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;

        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.id);
        dto.setEmail(usuario.email);
        dto.setNome(usuario.nome);
        dto.setPerfil(usuario.perfil.name());
        dto.setTelefone(usuario.telefone);
        dto.setEndereco(usuario.endereco);
        dto.setCidade(usuario.cidade);
        dto.setAtivo(usuario.ativo);
        dto.setAprovado(usuario.aprovado);
        dto.setDataCriacao(usuario.dataCriacao);
        dto.setDataAtualizacao(usuario.dataAtualizacao);
        return dto;
    }
}