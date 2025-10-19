package org.stm.atty.dto;

public class CadastroDTO {
    private String email;
    private String senha;
    private String nome;
    private String tipoUsuario;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    // Para compatibilidade com novo sistema
    public String getPerfil() { return tipoUsuario; }
}