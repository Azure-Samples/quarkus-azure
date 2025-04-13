import React, { useState, useEffect } from 'react';

function TodoItem({ item, itemToEdit, editItem, commitEditItem, removeItem }) {
  const [description, setDescription] = useState(item.description);
  const [completed, setCompleted] = useState(item.completed);
  const [isEscapePressed, setIsEscapePressed] = useState(false);

  useEffect(() => {
    setDescription(item.description);
    setCompleted(item.completed);
  }, [item]);

  const handleInputChange = (e) => {
    setDescription(e.target.value);
  };

  const handleCheckboxChange = (e) => {
    setCompleted(e.target.checked);
    item.completed = e.target.checked;
    commitEditItem(item);
  };

  const handleBlur = (e) => {
    if (isEscapePressed) {
      e.preventDefault();
      setIsEscapePressed(false);
      return;
    }

    item.description = description;
    commitEditItem(item);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.stopPropagation(); // Stop the event from propagating
      e.preventDefault();
      setIsEscapePressed(true);
      item.description = description;
      commitEditItem(item);
    }
  };

  return (
    <li>
      <div className="todo-item-container">
        <input
          type="checkbox"
          checked={completed}
          onChange={handleCheckboxChange}
          className="todo-checkbox"
        />
        <span
          className={`todo-description ${completed ? 'completed' : ''}`}
          onDoubleClick={(e) => {
            e.stopPropagation(); // Stop the event from propagating
            editItem(item);
          }}
        >
          {description}
        </span>
        <button
          className="todo-item-remove-button todo-item-remove-icon"
          title="Remove this item"
          onClick={() => removeItem(item)}
        >
        </button>
      </div>
      <div className={item !== itemToEdit ? 'hidden' : ''}>
        <form
          onSubmit={() => commitEditItem(item)}
        >
          <input
            type="text"
            className="textbox"
            value={description}
            onBlur={handleBlur}
            onChange={handleInputChange}
            onKeyDown={handleKeyDown}
            required
          />
        </form>
      </div>
    </li>
  );
}

export default TodoItem;