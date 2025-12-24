package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import java.util.Optional;

public interface TodoCustomRepository {

    // 특정 일정 조회
    Optional<Todo> findByIdWithUser(Long todoId);
}
