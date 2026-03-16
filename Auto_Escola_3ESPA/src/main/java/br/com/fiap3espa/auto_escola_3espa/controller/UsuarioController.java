package br.com.fiap3espa.auto_escola_3espa.controller;

import br.com.fiap3espa.auto_escola_3espa.domain.usuario.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Usuario> cadastrarUsuario(
            @RequestBody @Valid DadosCadastroUsuario dados,
            UriComponentsBuilder uriBuilder) {

        String senhaCriptografada = this.passwordEncoder.encode(dados.senha());

        Usuario usuario = new Usuario(dados, senhaCriptografada);

        repository.save(usuario);

        URI uri = uriBuilder.path("/usuarios/{id}")
                .buildAndExpand(usuario.getId())
                .toUri();

        return ResponseEntity.created(uri).body(usuario);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<DadosDetalhamentoUsuario>> listarUsuarios() {
        List<DadosDetalhamentoUsuario> lista = repository.findAllByAtivoTrue()
                .stream()
                .map(DadosDetalhamentoUsuario::new)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DadosDetalhamentoUsuario> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody @Valid DadosAtualizacaoPerfil dados) {

        Usuario usuario = repository.getReferenceById(id);
        usuario.atualizarPerfil(dados);

        return ResponseEntity.ok(new DadosDetalhamentoUsuario(usuario));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id) {
        Usuario usuario = repository.getReferenceById(id);
        usuario.excluir();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/alterar-senha")
    @Transactional
    public ResponseEntity<Void> alterarSenha(
            @RequestBody @Valid DadosAlteracaoSenhaUsuario dados,
            Authentication authentication) {
        String login = authentication.getName();
        Usuario usuario = repository.findByLogin(login);
        if(!passwordEncoder.matches(dados.senhaAtual(), usuario.getSenha())) {
            throw new RuntimeException("Senha atual incorreta!");
        }
        String novaSenhaCriptografada = passwordEncoder.encode(dados.novaSenha());
        usuario.alterarSenha(novaSenhaCriptografada);
        return ResponseEntity.noContent().build();
    }

}
