package com.exemplo.ordemservico.controller;

import com.exemplo.ordemservico.enums.Prioridade;
import com.exemplo.ordemservico.enums.StatusOS;
import com.exemplo.ordemservico.model.OrdemServico;
import com.exemplo.ordemservico.service.ClienteService;
import com.exemplo.ordemservico.service.OrdemServicoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/ordens")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;
    private final ClienteService clienteService;

    public OrdemServicoController(OrdemServicoService ordemServicoService, ClienteService clienteService) {
        this.ordemServicoService = ordemServicoService;
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<Resposta> abrir(@Valid @RequestBody Abertura dados) {
        Resposta resposta = Resposta.de(ordemServicoService.abrir(
                dados.clienteId(), dados.descricao(), Prioridade.valueOf(dados.prioridade())));
        URI local = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(local).body(resposta);
    }

    @GetMapping
    public List<Resposta> listar(@RequestParam(name = "status", required = false) StatusOS status) {
        List<OrdemServico> ordens = status == null
                ? ordemServicoService.listarTodos()
                : ordemServicoService.listarPorStatus(status);
        return ordens.stream().map(Resposta::de).toList();
    }

    @GetMapping("/{id}")
    public Resposta buscarPorId(@PathVariable("id") Long id) {
        return Resposta.de(ordemServicoService.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Resposta> listarPorCliente(@PathVariable("clienteId") Long clienteId) {
        clienteService.buscarPorId(clienteId);
        return ordemServicoService.listarPorCliente(clienteId).stream().map(Resposta::de).toList();
    }

    @GetMapping("/prioridade/{prioridade}")
    public List<Resposta> listarPorPrioridade(@PathVariable("prioridade") Prioridade prioridade) {
        return ordemServicoService.listarTodos().stream()
                .filter(ordem -> ordem.getPrioridade() == prioridade)
                .map(Resposta::de).toList();
    }

    @PutMapping("/{id}/tecnico/{tecnicoId}")
    public Resposta atribuirTecnico(@PathVariable("id") Long id, @PathVariable("tecnicoId") Long tecnicoId) {
        return Resposta.de(ordemServicoService.atribuirTecnico(id, tecnicoId));
    }

    @PutMapping("/{id}/concluir")
    public Resposta concluir(@PathVariable("id") Long id, @RequestParam("valor") BigDecimal valor) {
        return Resposta.de(ordemServicoService.concluir(id, valor));
    }

    @PutMapping("/{id}/cancelar")
    public Resposta cancelar(@PathVariable("id") Long id) {
        return Resposta.de(ordemServicoService.cancelar(id));
    }

    public record Abertura(
            @NotNull(message = "Informe o clienteId.")
            @Positive(message = "O clienteId deve ser maior que zero.") Long clienteId,
            @NotBlank(message = "Informe a descrição da ordem de serviço.")
            @Size(max = 500, message = "A descrição deve ter até 500 caracteres.") String descricao,
            @NotNull(message = "Informe a prioridade.")
            @Pattern(regexp = "BAIXA|MEDIA|ALTA", message = "A prioridade deve ser BAIXA, MEDIA ou ALTA.")
            String prioridade) {
    }

    public record Resposta(Long id, String descricao, LocalDateTime dataAbertura, LocalDateTime dataFechamento,
            BigDecimal valor, StatusOS status, Prioridade prioridade, Long clienteId, Long tecnicoId) {

        private static Resposta de(OrdemServico ordem) {
            Long tecnicoId = ordem.getTecnico() == null ? null : ordem.getTecnico().getId();
            return new Resposta(ordem.getId(), ordem.getDescricao(), ordem.getDataAbertura(),
                    ordem.getDataFechamento(), ordem.getValor(), ordem.getStatus(), ordem.getPrioridade(),
                    ordem.getCliente().getId(), tecnicoId);
        }
    }
}
