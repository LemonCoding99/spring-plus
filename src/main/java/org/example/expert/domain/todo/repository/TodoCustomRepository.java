package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoCustomRepository {

    // 특정 일정 조회
    Optional<Todo> findByIdWithUser(Long todoId);


    // 일정 검색 기능
    Page<TodoSearchResponse> search(String title,
                                    String managerNickname,
                                    LocalDateTime createdStart,
                                    LocalDateTime CreatedEnd,
                                    Pageable pageable);
}
