package com.exemplo.ordemservico.controller;

import com.exemplo.ordemservico.service.OrdemServicoService.EstadoInvalidoException;
import com.exemplo.ordemservico.service.OrdemServicoService.ValorInvalidoException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class TratamentoDeErros {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Erro> erroDeServico(RuntimeException excecao) {
        String mensagem = excecao.getMessage();
        if (excecao.getClass() != RuntimeException.class || mensagem == null) {
            throw excecao;
        }
        return switch (mensagem) {
            case "Cliente não encontrado.", "Técnico não encontrado.", "Ordem de serviço não encontrada." ->
                responder(HttpStatus.NOT_FOUND, mensagem);
            case "Já existe um cliente com esse CPF.", "Esse CPF já está cadastrado." ->
                responder(HttpStatus.CONFLICT, mensagem);
            default -> throw excecao;
        };
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> conflitoDeDados() {
        return responder(HttpStatus.CONFLICT, "A operação viola uma restrição de unicidade ou de vínculo entre registros.");
    }

    @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<Erro> estadoInvalido(EstadoInvalidoException excecao) {
        return responder(HttpStatus.CONFLICT, excecao.getMessage());
    }

    @ExceptionHandler(ValorInvalidoException.class)
    public ResponseEntity<Erro> valorInvalido(ValorInvalidoException excecao) {
        return responder(HttpStatus.BAD_REQUEST, excecao.getMessage());
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Erro> alteracaoConcorrente() {
        return responder(HttpStatus.CONFLICT, "Outra requisição alterou esses dados. Consulte o registro e tente novamente.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> corpoInvalido(MethodArgumentNotValidException excecao) {
        String mensagem = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));
        return responder(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Erro> jsonInvalido() {
        return responder(HttpStatus.BAD_REQUEST, "Informe um corpo JSON válido com os campos esperados.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Erro> parametroAusente(MissingServletRequestParameterException excecao) {
        return responder(HttpStatus.BAD_REQUEST, "Informe o parâmetro '" + excecao.getParameterName() + "'.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Erro> parametroInvalido(MethodArgumentTypeMismatchException excecao) {
        String nome = excecao.getName();
        Class<?> tipo = excecao.getRequiredType();
        if (tipo != null && tipo.isEnum()) {
            return responder(HttpStatus.BAD_REQUEST,
                    "Use um dos valores " + Arrays.toString(tipo.getEnumConstants()) + " para '" + nome + "'.");
        }
        if (tipo == Long.class || tipo == BigDecimal.class) {
            return responder(HttpStatus.BAD_REQUEST, "Informe um número válido para '" + nome + "'.");
        }
        return responder(HttpStatus.BAD_REQUEST, "O parâmetro '" + nome + "' é inválido.");
    }

    private ResponseEntity<Erro> responder(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new Erro(mensagem));
    }

    public record Erro(String mensagem) {
    }
}
