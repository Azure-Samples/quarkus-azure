package com.azure.samples.todo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ToDoItemRepository implements PanacheRepository<ToDoItem> {}
