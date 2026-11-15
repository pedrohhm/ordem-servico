
package com.exemplo.ordemservico.service;

import com.exemplo.ordemservico.enums.Prioridade;
import com.exemplo.ordemservico.enums.StatusOS;
import com.exemplo.ordemservico.model.Cliente;
import com.exemplo.ordemservico.model.OrdemServico;
import com.exemplo.ordemservico.model.Tecnico;
import com.exemplo.ordemservico.repository.ClienteRepository;
import com.exemplo.ordemservico.repository.OrdemServicoRepository;
import com.exemplo.ordemservico.repository.TecnicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdemServicoService {

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("99999999.99");

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;

    public OrdemServicoService(
            OrdemServicoRepository ordemServicoRepository,
            ClienteRepository clienteRepository,
            TecnicoRepository tecnicoRepository) {

        this.ordemServicoRepository = ordemServicoRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<OrdemServico> listarTodos() {
        return ordemServicoRepository.findAll();
    }

    public OrdemServico buscarPorId(Long id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Ordem de serviço não encontrada."));
    }

    public List<OrdemServico> listarPorStatus(StatusOS status) {
        return ordemServicoRepository.findByStatus(status);
    }

    public List<OrdemServico> listarPorCliente(Long clienteId) {
        return ordemServicoRepository.findByClienteId(clienteId);
    }

    @Transactional
    public OrdemServico abrir(Long clienteId, String descricao, Prioridade prioridade) {
        OrdemServico ordem = new OrdemServico();
        ordem.setCliente(buscarCliente(clienteId));
        ordem.setDescricao(descricao.strip());
        ordem.setPrioridade(prioridade);
        ordem.setDataAbertura(LocalDateTime.now());
        ordem.setStatus(StatusOS.ABERTA);
        return ordemServicoRepository.save(ordem);
    }

    // As transições concorrentes não podem sobrescrever o estado da mesma ordem.
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrdemServico atribuirTecnico(Long id, Long tecnicoId) {
        OrdemServico ordem = buscarOrdemEditavel(id);
        ordem.setTecnico(buscarTecnico(tecnicoId));
        ordem.setStatus(StatusOS.EM_ANDAMENTO);
        return ordem;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrdemServico concluir(Long id, BigDecimal valor) {
        OrdemServico ordem = buscarOrdemEditavel(id);
        if (ordem.getTecnico() == null) {
            throw new EstadoInvalidoException("Atribua um técnico antes de concluir a ordem de serviço.");
        }
        ordem.setValor(validarValor(valor));
        ordem.setDataFechamento(LocalDateTime.now());
        ordem.setStatus(StatusOS.CONCLUIDA);
        return ordem;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrdemServico cancelar(Long id) {
        OrdemServico ordem = buscarOrdemEditavel(id);
        ordem.setDataFechamento(LocalDateTime.now());
        ordem.setStatus(StatusOS.CANCELADA);
        return ordem;
    }

    public OrdemServico salvar(OrdemServico ordemServico) {

        Long clienteId = ordemServico.getCliente().getId();
        ordemServico.setCliente(buscarCliente(clienteId));

        if (ordemServico.getTecnico() != null
                && ordemServico.getTecnico().getId() != null) {

            Long tecnicoId = ordemServico.getTecnico().getId();
            ordemServico.setTecnico(buscarTecnico(tecnicoId));
        }

        return ordemServicoRepository.save(ordemServico);
    }

    public OrdemServico atualizar(Long id, OrdemServico dadosAtualizados) {

        OrdemServico ordemServico = buscarPorId(id);

        Long clienteId = dadosAtualizados.getCliente().getId();
        Cliente cliente = buscarCliente(clienteId);

        ordemServico.setDescricao(dadosAtualizados.getDescricao());
        ordemServico.setDataAbertura(dadosAtualizados.getDataAbertura());
        ordemServico.setDataFechamento(dadosAtualizados.getDataFechamento());
        ordemServico.setValor(dadosAtualizados.getValor());
        ordemServico.setStatus(dadosAtualizados.getStatus());
        ordemServico.setPrioridade(dadosAtualizados.getPrioridade());
        ordemServico.setCliente(cliente);

        if (dadosAtualizados.getTecnico() != null
                && dadosAtualizados.getTecnico().getId() != null) {

            Long tecnicoId = dadosAtualizados.getTecnico().getId();
            ordemServico.setTecnico(buscarTecnico(tecnicoId));

        } else {
            ordemServico.setTecnico(null);
        }

        return ordemServicoRepository.save(ordemServico);
    }

    public void excluir(Long id) {
        OrdemServico ordemServico = buscarPorId(id);
        ordemServicoRepository.delete(ordemServico);
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
    }

    private Tecnico buscarTecnico(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado."));
    }

    private OrdemServico buscarOrdemEditavel(Long id) {
        OrdemServico ordem = buscarPorId(id);
        if (ordem.getStatus() == StatusOS.CONCLUIDA || ordem.getStatus() == StatusOS.CANCELADA) {
            throw new EstadoInvalidoException("A ordem de serviço já foi finalizada.");
        }
        return ordem;
    }

    private BigDecimal validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new ValorInvalidoException("O valor cobrado deve ser maior que zero.");
        }
        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            throw new ValorInvalidoException("O valor cobrado deve ser de até 99999999,99.");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new ValorInvalidoException("O valor cobrado deve ter no máximo duas casas decimais.");
        }
        return valor.setScale(2);
    }

    public static class EstadoInvalidoException extends RuntimeException {
        public EstadoInvalidoException(String mensagem) {
            super(mensagem);
        }
    }

    public static class ValorInvalidoException extends RuntimeException {
        public ValorInvalidoException(String mensagem) {
            super(mensagem);
        }
    }
}
