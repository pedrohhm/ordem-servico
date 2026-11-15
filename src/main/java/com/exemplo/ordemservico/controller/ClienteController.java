package com.exemplo.ordemservico.controller;

import com.exemplo.ordemservico.model.Cliente;
import com.exemplo.ordemservico.service.ClienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<Resposta> salvar(@Valid @RequestBody Cadastro dados) {
        String cpf = dados.cpf().replace(".", "").replace("-", "")
                .replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        Cliente cliente = paraCliente(dados.nome(), cpf, dados.telefone(), dados.email());
        Resposta resposta = Resposta.de(clienteService.salvar(cliente));
        URI local = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(local).body(resposta);
    }

    @GetMapping
    public List<Resposta> listar() {
        return clienteService.listarTodos().stream().map(Resposta::de).toList();
    }

    @GetMapping("/{id}")
    public Resposta buscarPorId(@PathVariable("id") Long id) {
        return Resposta.de(clienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public Resposta atualizar(@PathVariable("id") Long id, @Valid @RequestBody Atualizacao dados) {
        String cpfAtual = clienteService.buscarPorId(id).getCpf();
        Cliente cliente = paraCliente(dados.nome(), cpfAtual, dados.telefone(), dados.email());
        return Resposta.de(clienteService.atualizar(id, cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private static Cliente paraCliente(String nome, String cpf, String telefone, String email) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome.strip());
        cliente.setCpf(cpf);
        cliente.setTelefone(telefone);
        cliente.setEmail(email);
        return cliente;
    }

    public record Cadastro(
            @NotBlank(message = "Informe o nome do cliente.")
            @Size(max = 100, message = "O nome deve ter até 100 caracteres.") String nome,
            @NotBlank(message = "Informe o CPF.")
            @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}",
                    message = "Informe um CPF com 11 dígitos ou no formato 000.000.000-00.") String cpf,
            @Size(max = 20, message = "O telefone deve ter até 20 caracteres.") String telefone,
            @Email(message = "Informe um e-mail válido.")
            @Size(max = 100, message = "O e-mail deve ter até 100 caracteres.") String email) {
    }

    public record Atualizacao(
            @NotBlank(message = "Informe o nome do cliente.")
            @Size(max = 100, message = "O nome deve ter até 100 caracteres.") String nome,
            @Size(max = 20, message = "O telefone deve ter até 20 caracteres.") String telefone,
            @Email(message = "Informe um e-mail válido.")
            @Size(max = 100, message = "O e-mail deve ter até 100 caracteres.") String email) {
    }

    public record Resposta(Long id, String nome, String cpf, String telefone, String email) {

        private static Resposta de(Cliente cliente) {
            return new Resposta(cliente.getId(), cliente.getNome(), cliente.getCpf(), cliente.getTelefone(),
                    cliente.getEmail());
        }
    }
}
