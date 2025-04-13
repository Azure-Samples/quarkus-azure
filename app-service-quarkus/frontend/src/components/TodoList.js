import React from 'react';
import TodoItem from './TodoItem';

function TodoList({ items, itemToEdit, editItem, commitEditItem, removeItem }) {
  return (
    <ul id="todo-list">
      {items.map((item, index) => (
        <TodoItem
          key={index}
          item={item}
          itemToEdit={itemToEdit}
          editItem={editItem}
          commitEditItem={commitEditItem}
          removeItem={removeItem}
        />
      ))}
    </ul>
  );
}

export default TodoList;
