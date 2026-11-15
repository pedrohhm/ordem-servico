
package com.exemplo.ordemservico.service;

import com.exemplo.ordemservico.model.Tecnico;
import com.exemplo.ordemservico.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<Tecnico> listarTodos() {
        return tecnicoRepository.findAll();
    }

    public Tecnico buscarPorId(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Técnico não encontrado."));
    }

    public Tecnico salvar(Tecnico tecnico) {
        return tecnicoRepository.save(tecnico);
    }

    public Tecnico atualizar(Long id, Tecnico dadosAtualizados) {

        Tecnico tecnico = buscarPorId(id);

        tecnico.setNome(dadosAtualizados.getNome());
        tecnico.setEspecialidade(dadosAtualizados.getEspecialidade());
        tecnico.setTelefone(dadosAtualizados.getTelefone());

        return tecnicoRepository.save(tecnico);
    }

    public void excluir(Long id) {
        Tecnico tecnico = buscarPorId(id);
        tecnicoRepository.delete(tecnico);
    }
}
