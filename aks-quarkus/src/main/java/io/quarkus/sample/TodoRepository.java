package io.quarkus.sample;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@Transactional
@ApplicationScoped
public class TodoRepository {

    @PersistenceContext
    EntityManager em;

    public Todo findById(Long id) {
        return em.find(Todo.class, id);
    }

    public void persist(Todo todo) {
        em.persist(todo);
    }

    public void delete(Todo todo) {
        em.remove(todo);
    }

    public List<Todo> findAll() {
        return em.createQuery("SELECT t FROM Todo t", Todo.class).getResultList();
    }

    public void deleteCompleted() {
        em.createQuery("DELETE FROM Todo t WHERE t.completed = true").executeUpdate();
    }
}
