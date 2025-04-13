package com.azure.samples.todo;

import java.io.Serializable;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("resources/todo")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ToDoResource implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private ToDoService service;

    @POST
    public ToDoItem create(@Valid ToDoItem item) {
        return service.addToDoItem(item);
    }

    @PUT
    @Path("/{id}")
    public void edit(@Valid ToDoItem item) {
        service.updateToDoItem(item);
    }

    @DELETE
    @Path("/{id}")
    public void remove(Long id) {
        service.removeToDoItem(id);
    }

    @GET
    public List<ToDoItem> getAll() {
        return service.findAllToDoItems();
    }
}
