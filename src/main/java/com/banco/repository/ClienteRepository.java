package com.banco.repository;

import com.banco.config.JPAUtil;
import com.banco.model.Cliente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.time.LocalDateTime;
import java.util.List;

public class ClienteRepository implements Repositorio<Cliente> {

    @Override
    public void salvar(Cliente cliente) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(cliente);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Cliente buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Cliente.class, id);
        } finally {
            em.close();
        }
    }

    public Cliente buscarPorCpf(String cpf) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Cliente c WHERE c.cpf = :cpf", Cliente.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Cliente> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Cliente c WHERE c.ativo = true", Cliente.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Cliente atualizar(Cliente cliente) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Cliente atualizado = em.merge(cliente);
            em.getTransaction().commit();
            return atualizado;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void excluir(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Cliente cliente = em.find(Cliente.class, id);
            if (cliente != null) cliente.desativar();   // não remove, só marca como excluído
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Cliente> listarExcluidos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Cliente c WHERE c.ativo = false", Cliente.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public int purgarExpirados(int dias) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            LocalDateTime limite = LocalDateTime.now().minusDays(dias);
            List<Cliente> expirados = em.createQuery(
                    "SELECT c FROM Cliente c WHERE c.ativo = false AND c.dataExclusao < :limite",
                    Cliente.class).setParameter("limite", limite).getResultList();
            expirados.forEach(em::remove);   // aqui sim apaga de verdade
            em.getTransaction().commit();
            return expirados.size();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}