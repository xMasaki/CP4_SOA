package br.com.fiap3espa.auto_escola_3espa.domain.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosAlteracaoSenhaUsuario(

        @NotBlank
        String senhaAtual,

        @NotBlank
        String novaSenha
) {
}
