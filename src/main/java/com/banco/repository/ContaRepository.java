package com.banco.repository;

import com.banco.config.JPAUtil;
import com.banco.model.Conta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;

public class ContaRepository {

    public void salvar(Conta conta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(conta);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Conta buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Conta.class, id);
        } finally {
            em.close();
        }
    }

    public Conta buscarPorNumero(String numero) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Conta c WHERE c.numero = :numero", Conta.class)
                    .setParameter("numero", numero)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Conta> listarPorCliente(Long clienteId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Conta c WHERE c.cliente.id = :id", Conta.class)
                    .setParameter("id", clienteId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Conta atualizar(Conta conta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Conta atualizada = em.merge(conta);
            em.getTransaction().commit();
            return atualizada;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Salva várias contas na MESMA transação (tudo ou nada)
    public void atualizarVarias(List<Conta> contas) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            for (Conta c : contas) em.merge(c);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Conta> listarTodas() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Conta c WHERE c.cliente.ativo = true", Conta.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
