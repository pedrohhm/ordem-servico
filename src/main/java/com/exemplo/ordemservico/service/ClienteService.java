
package com.exemplo.ordemservico.service;

import com.exemplo.ordemservico.model.Cliente;
import com.exemplo.ordemservico.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cliente não encontrado."));
    }

    public Cliente salvar(Cliente cliente) {

        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new RuntimeException("Já existe um cliente com esse CPF.");
        }

        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(Long id, Cliente dadosAtualizados) {

        Cliente cliente = buscarPorId(id);

        if (!cliente.getCpf().equals(dadosAtualizados.getCpf())
                && clienteRepository.existsByCpf(dadosAtualizados.getCpf())) {
            throw new RuntimeException("Esse CPF já está cadastrado.");
        }

        cliente.setNome(dadosAtualizados.getNome());
        cliente.setCpf(dadosAtualizados.getCpf());
        cliente.setTelefone(dadosAtualizados.getTelefone());
        cliente.setEmail(dadosAtualizados.getEmail());

        return clienteRepository.save(cliente);
    }

    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }
}
