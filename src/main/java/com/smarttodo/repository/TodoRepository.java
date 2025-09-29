package com.smarttodo.repository;

import com.smarttodo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByStatus(Todo.TaskStatus status);

    List<Todo> findByPriority(Todo.Priority priority);

    @Query("SELECT t FROM Todo t WHERE t.status != 'COMPLETED' ORDER BY t.priority DESC, t.createdAt ASC")
    List<Todo> findActiveTasks();

    @Query("SELECT COUNT(t) FROM Todo t WHERE t.status = 'COMPLETED'")
    long countCompletedTasks();
}
