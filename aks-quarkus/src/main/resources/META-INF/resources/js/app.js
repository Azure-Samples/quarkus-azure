/*global Vue, todoStorage */
const filters = {
    all: function (todos) {
        return todos;
    },
    active: function (todos) {
        return todos.filter(function (todo) {
            return !todo.completed;
        });
    },
    completed: function (todos) {
        return todos.filter(function (todo) {
            return todo.completed;
        });
    }
};

const todoFocus = {
    mounted(el, binding) {
        if (binding.value) {
            el.focus();
        }
    }
};

const { createApp } = Vue

const app = createApp({
    // app initial state
    data() {
        return {
            todos: [],
            newTodo: '',
            editedTodo: null,
            visibility: 'all'
        }
    },

    computed: {
        filteredTodos() {
            return filters[this.visibility](this.todos);
        },
        remaining() {
            return filters.active(this.todos).length;
        },
        allDone: {
            get() {
                return this.remaining === 0;
            },
            set(value) {
                this.todos.forEach(function (todo) {
                    todo.completed = value;
                    todoStorage.save(todo);
                });
            }
        }
    },

    methods: {
        pluralize(word, count) {
            return word + (count === 1 ? '' : 's');
        },

        async addTodo() {
            var value = this.newTodo && this.newTodo.trim();
            if (!value) {
                return;
            }

            const item = await todoStorage.add({
                title : value,
                order: this.todos.length + 1,
                completed: false
            });
            this.todos.push(item);
            this.newTodo = '';
        },

        async removeTodo(todo) {
            await todoStorage.delete(todo);
            await this.reload();
        },

        toggleTodo(todo) {
            todo.completed = ! todo.completed;
            todoStorage.save(todo);
        },

        async editTodo(todo) {
            this.beforeEditCache = todo.title;
            this.editedTodo = todo;
        },

        doneEdit(todo) {
            if (!this.editedTodo) {
                return;
            }
            this.editedTodo = null;
            todo.title = todo.title.trim();
            if (!todo.title) {
                this.removeTodo(todo);
            } else {
                todoStorage.save(todo);
            }
        },

        cancelEdit(todo) {
            this.editedTodo = null;
            todo.title = this.beforeEditCache;
        },

        async removeCompleted () {
            await todoStorage.deleteCompleted();
            await this.reload();
        },

        async reload() {
            const data = await todoStorage.fetch();
            this.todos = data;
        }
    },

    async mounted() {
        await this.reload();
    },

    directives: {
        todoFocus
    }
});

app.mount('.todoapp');

window.app = app;