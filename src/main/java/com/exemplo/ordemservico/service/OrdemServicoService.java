
package com.exemplo.ordemservico.service;

import com.exemplo.ordemservico.enums.StatusOS;
import com.exemplo.ordemservico.model.Cliente;
import com.exemplo.ordemservico.model.OrdemServico;
import com.exemplo.ordemservico.model.Tecnico;
import com.exemplo.ordemservico.repository.ClienteRepository;
import com.exemplo.ordemservico.repository.OrdemServicoRepository;
import com.exemplo.ordemservico.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdemServicoService {

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

    public OrdemServico salvar(OrdemServico ordemServico) {

        Long clienteId = ordemServico.getCliente().getId();

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() ->
                        new RuntimeException("Cliente não encontrado."));

        ordemServico.setCliente(cliente);

        if (ordemServico.getTecnico() != null
                && ordemServico.getTecnico().getId() != null) {

            Long tecnicoId = ordemServico.getTecnico().getId();

            Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                    .orElseThrow(() ->
                            new RuntimeException("Técnico não encontrado."));

            ordemServico.setTecnico(tecnico);
        }

        return ordemServicoRepository.save(ordemServico);
    }

    public OrdemServico atualizar(Long id, OrdemServico dadosAtualizados) {

        OrdemServico ordemServico = buscarPorId(id);

        Long clienteId = dadosAtualizados.getCliente().getId();

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() ->
                        new RuntimeException("Cliente não encontrado."));

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

            Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                    .orElseThrow(() ->
                            new RuntimeException("Técnico não encontrado."));

            ordemServico.setTecnico(tecnico);

        } else {
            ordemServico.setTecnico(null);
        }

        return ordemServicoRepository.save(ordemServico);
    }

    public void excluir(Long id) {
        OrdemServico ordemServico = buscarPorId(id);
        ordemServicoRepository.delete(ordemServico);
    }
}
