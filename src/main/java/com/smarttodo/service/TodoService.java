package com.smarttodo.service;

import com.smarttodo.entity.Todo;
import com.smarttodo.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    public List<Todo> findAll(){
        return todoRepository.findAll();
    }

    public Optional<Todo> findById(Long id){
        return todoRepository.findById(id);
    }

    public Todo save(Todo todo){
        if (todo.getCreatedAt() == null){
            todo.setCreatedAt(LocalDateTime.now());
        }

        return todoRepository.save(todo);
    }

    public Optional<Todo> update(Long id, Todo updatedTodo){
        return todoRepository.findById(id)
                .map(todo -> {
                    todo.setTitle(updatedTodo.getTitle());
                    todo.setDescription(updatedTodo.getDescription());
                    todo.setPriority(updatedTodo.getPriority());
                    todo.setStatus(updatedTodo.getStatus());
                    todo.setDueDate(updatedTodo.getDueDate());
                    return todoRepository.save(todo);
                });
    }

    public boolean delete(Long id){
        if (todoRepository.existsById(id)){
            todoRepository.deleteById(id);
            return true;
        }
        return true;
    }

    public Optional<Todo> markAsCompleted(Long id){
        return todoRepository.findById(id)
                .map(todo -> {
                    todo.setStatus(Todo.TaskStatus.COMPLETED);
                    todo.setCompletedAt(LocalDateTime.now());
                    return todoRepository.save(todo);
                });
    }

    public List<Todo> findActiveTasks(){
        return todoRepository.findActiveTasks();
    }

    public long getCompletedCount(){
        return todoRepository.countCompletedTasks();
    }
}
