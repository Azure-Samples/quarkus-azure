package io.quarkus.sample;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.List;

import org.jboss.logging.Logger;

@Path("/api")
public class TodoResource {

    private static final Logger LOG = Logger.getLogger(TodoResource.class);

    @Inject
    TodoRepository todoRepository;

    @GET
    public List<Todo> getAll() {
        List<Todo> todos = todoRepository.findAll();
        LOG.info("Found " + todos.size() + " todos");
        return todos;
    }

    @GET
    @Path("/{id}")
    public Todo getOne(@PathParam("id") Long id) {
        Todo entity = todoRepository.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND);
        }
        LOG.info("Found todo: " + entity);
        return entity;
    }

    @POST
    @Transactional
    public Response create(@Valid Todo item) {
        todoRepository.persist(item);
        LOG.info("Created todo: " + item);
        return Response.status(Status.CREATED).entity(item).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@Valid Todo todo, @PathParam("id") Long id) {
        Todo entity = todoRepository.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND);
        }
        entity.setCompleted(todo.isCompleted());
        entity.setOrder(todo.getOrder());
        entity.setTitle(todo.getTitle());
        entity.setUrl(todo.getUrl());
        LOG.info("Updated todo: " + entity);
        return Response.ok(entity).build();
    }

    @DELETE
    @Transactional
    public Response deleteCompleted() {
        todoRepository.deleteCompleted();
        LOG.info("Deleted completed todos");
        return Response.noContent().build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public Response deleteOne(@PathParam("id") Long id) {
        Todo entity = todoRepository.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND);
        }
        todoRepository.delete(entity);
        LOG.info("Deleted todo: " + entity);
        return Response.noContent().build();
    }
}
