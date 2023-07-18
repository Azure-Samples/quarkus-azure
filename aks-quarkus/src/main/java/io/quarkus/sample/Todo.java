package io.quarkus.sample;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "Todo")
public class Todo {

    @PersistenceContext
    static EntityManager em;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String title;

    private boolean completed;

    @Column(name = "ordering")
    private int order;

    private String url;

    public static List<Todo> findNotCompleted() {
        try {
            return em.createQuery("SELECT t FROM Todo t WHERE t.completed = false", Todo.class).getResultList();
        } finally {
            em.close();
        }
    }

    public static List<Todo> findCompleted() {
        try {
            return em.createQuery("SELECT t FROM Todo t WHERE t.completed = true", Todo.class).getResultList();
        } finally {
            em.close();
        }
    }

    public static long deleteCompleted() {
        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            int deletedCount = em.createQuery("DELETE FROM Todo t WHERE t.completed = true").executeUpdate();
            transaction.commit();
            return deletedCount;
        } finally {
            em.close();
        }
    }

    public static List<Todo> listAll() {
        try {
            CriteriaQuery<Todo> cq = em.getCriteriaBuilder().createQuery(Todo.class);
            cq.select(cq.from(Todo.class));
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    public Todo() {
    }

    public Todo(Long id, String title, boolean completed, int order, String url) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.order = order;
        this.url = url;
    }

    // Getters and setters for the fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
	public String toString() {
		return new StringJoiner(", ", Todo.class.getSimpleName() + "[", "]")
			.add("id=" + this.id)
			.add("title='" + this.title + "'")
			.add("completed='" + this.completed + "'")
            .add("order='" + this.order + "'")
            .add("url='" + this.url + "'")
			.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if ((o == null) || (getClass() != o.getClass())) {
			return false;
		}

		Todo todo = (Todo) o;
		return this.id.equals(todo.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

}
