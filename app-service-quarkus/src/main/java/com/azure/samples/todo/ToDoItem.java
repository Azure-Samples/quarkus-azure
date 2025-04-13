package com.azure.samples.todo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "ToDoItem")
@NamedQuery(
        name = "ToDoItem.findAll",
        query = "SELECT i FROM ToDoItem i")
public class ToDoItem extends PanacheEntity {

    @NotBlank(message = "Item description cannot be blank")
    @Size(min = 5, max = 110, 
        message = "Item description must be between 5 and 110 characters")
    private String description;

    private boolean completed;

    protected ToDoItem() {
        // Default constructor
    }

    public ToDoItem(String description, boolean completed) {
        this.description = description;
        this.completed = completed;
    }    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ToDoItem)) {
            return false;
        }

        ToDoItem other = (ToDoItem) object;

        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "ToDoItem[id=" + id
                + ", desciption=" + description
                + ", completed=" + completed + "]";
    }
}
