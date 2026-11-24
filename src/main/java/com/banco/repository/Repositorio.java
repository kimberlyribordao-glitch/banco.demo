package com.banco.repository;

import java.util.List;

public interface Repositorio<T> {
    void salvar(T entidade);
    T buscarPorId(Long id);
    List<T> listarTodos();
    T atualizar(T entidade);
    void excluir(Long id);
}