package com.azure.samples.todo;

import java.util.List;

public interface ToDoService {
    ToDoItem addToDoItem(ToDoItem item);

    List<ToDoItem> findAllToDoItems();

    void removeToDoItem(Long id);

    void updateToDoItem(ToDoItem item);
}
