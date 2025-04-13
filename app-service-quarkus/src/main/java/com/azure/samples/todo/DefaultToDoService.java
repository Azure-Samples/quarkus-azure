package com.azure.samples.todo;

import java.util.List;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DefaultToDoService implements ToDoService {
    @Inject
    private Logger logger;

    @Inject
    private ToDoItemRepository repository;

    @Override
    @Transactional
    public ToDoItem addToDoItem(ToDoItem item) {
        logger.info("Adding item: " + item);
        repository.persist(item);
        return item;
    }

    @Override
    @Transactional
    public void updateToDoItem(ToDoItem item) {
        logger.info("Updating item: " + item);
        ToDoItem existingItem = repository.findById(item.getId());
        
        if (existingItem == null) {
            throw new IllegalArgumentException("Item with ID " + item.getId() + " does not exist");
        }

        existingItem.setDescription(item.getDescription());
        existingItem.setCompleted(item.isCompleted());
    }

    @Override
    @Transactional
    public void removeToDoItem(Long id) {
        logger.info("Removing item with ID: " + id);

        repository.delete("id", id);
    }

    @Override
    public List<ToDoItem> findAllToDoItems() {
        logger.info("Getting all items");

        return repository.findAll().list();
    }
}
