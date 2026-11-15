package com.exemplo.ordemservico.controller;

import com.exemplo.ordemservico.model.Tecnico;
import com.exemplo.ordemservico.service.TecnicoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/tecnicos")
public class TecnicoController {

    private final TecnicoService tecnicoService;

    public TecnicoController(TecnicoService tecnicoService) {
        this.tecnicoService = tecnicoService;
    }

    @PostMapping
    public ResponseEntity<Resposta> salvar(@Valid @RequestBody Dados dados) {
        Resposta resposta = Resposta.de(tecnicoService.salvar(dados.paraTecnico()));
        URI local = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(local).body(resposta);
    }

    @GetMapping
    public List<Resposta> listar() {
        return tecnicoService.listarTodos().stream().map(Resposta::de).toList();
    }

    @GetMapping("/{id}")
    public Resposta buscarPorId(@PathVariable("id") Long id) {
        return Resposta.de(tecnicoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public Resposta atualizar(@PathVariable("id") Long id, @Valid @RequestBody Dados dados) {
        return Resposta.de(tecnicoService.atualizar(id, dados.paraTecnico()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        tecnicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    public record Dados(
            @NotBlank(message = "Informe o nome do técnico.")
            @Size(max = 100, message = "O nome deve ter até 100 caracteres.") String nome,
            @Size(max = 60, message = "A especialidade deve ter até 60 caracteres.") String especialidade,
            @Size(max = 20, message = "O telefone deve ter até 20 caracteres.") String telefone) {

        private Tecnico paraTecnico() {
            Tecnico tecnico = new Tecnico();
            tecnico.setNome(nome.strip());
            tecnico.setEspecialidade(especialidade);
            tecnico.setTelefone(telefone);
            return tecnico;
        }
    }

    public record Resposta(Long id, String nome, String especialidade, String telefone) {

        private static Resposta de(Tecnico tecnico) {
            return new Resposta(tecnico.getId(), tecnico.getNome(), tecnico.getEspecialidade(), tecnico.getTelefone());
        }
    }
}
