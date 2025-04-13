import React, { useState, useEffect } from 'react';
import TodoList from './components/TodoList';
import ToDoService from './utils/ToDoService';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';
import ErrorHandler from './utils/ToDoErrorHandler';

const ToDoApp = () => {
  const [items, setItems] = useState([]);
  const [newToDoDescription, setNewToDoDescription] = useState('');
  const [itemToEdit, setItemToEdit] = useState(null);

  useEffect(() => {
    // Fetch items for the user when the component mounts
    ToDoService.getItems()
      .then(response => {
        setItems(response.data);
      })
      .catch(error => {
        toast.error(ErrorHandler.getErrorMessage(error, 'Error fetching item'));  // Show error toast
        console.error('Error fetching items:', error);
      });
  }, []); // Add empty dependency array to run only once

  // Show a notification when an item is added or edited
  const addItem = (event) => {
    event.preventDefault();
    const newItem = { description: newToDoDescription, completed: false };
    ToDoService.addItem(newItem)
      .then(response => {
        setItems([...items, response.data]);
        setNewToDoDescription('');
        toast.success('Item added successfully!');  // Show success toast
      })
      .catch(error => {
        toast.error(ErrorHandler.getErrorMessage(error, 'Error adding item'));  // Show error toast
        console.error('Error adding item:', error);
      });
  };

  const editItem = (item) => {
    setItemToEdit(item);
  };

  // Show error message on failure (replace with appropriate error handling)
  const commitEditItem = (item) => {
    ToDoService.updateItem(item.id, item)
      .then(() => {
        setItemToEdit(null);
        toast.success('Item updated successfully!');
      })
      .catch(error => {
        toast.error(ErrorHandler.getErrorMessage(error, 'Error updating item'));  // Show error toast
        console.error('Error updating item:', error);
      });
  };

  const removeItem = (item) => {
    ToDoService.removeItem(item.id)
      .then(() => {
        setItems(items.filter(i => i !== item));
        toast.success('Item removed successfully!');
      })
      .catch(error => {
        toast.error(ErrorHandler.getErrorMessage(error, 'Error removing item'));  // Show error toast
        console.error('Error removing item:', error);
      });
  };

  return (
    <div className="center">
      <div id="todo-panel">
        <label className="todo-label" htmlFor="add-todo">To-Do List</label>
        <form onSubmit={addItem}>
          <input
            id="add-todo"
            className="textbox"
            placeholder="Buy milk"
            value={newToDoDescription}
            onChange={(e) => setNewToDoDescription(e.target.value)}
            required
          />
        </form>
        {items.length > 0 && (
          <div>
            <TodoList
              items={items}
              itemToEdit={itemToEdit}
              editItem={editItem}
              commitEditItem={commitEditItem}
              removeItem={removeItem}
            />
            <span className="item-count-label">
              <strong>{items.length}</strong> {items.length === 1 ? 'item' : 'items'} on your list
            </span>
          </div>
        )}
      </div>
      <ToastContainer />
    </div>
  );
};

export default ToDoApp;
