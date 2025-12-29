package org.example.expert.domain.todo.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.user.entity.QUser.user;
import static org.example.expert.domain.comment.entity.QComment.comment;

@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository{

    private final JPAQueryFactory queryFactory;

    // 일정 검색 기능
    @Override
    public Page<TodoSearchResponse> search(String title,
                                           String managerNickname,
                                           LocalDateTime createdStart,
                                           LocalDateTime createdEnd,
                                           Pageable pageable
    ) {
        List<TodoSearchResponse> results = queryFactory
                .select(Projections.constructor(  // dto 바로 사용가능
                        TodoSearchResponse.class,
                        todo.id,
                        todo.title,
                        manager.countDistinct(),
                        comment.countDistinct()
                )
                )
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.user, user)
                .leftJoin(todo.comments, comment)
                .where(  // 만들어 준 조건식 사용해주기
                        titleContains(title),
                        createdAtBetween(createdStart, createdEnd),
                        managerNicknameContains(managerNickname)
                )
                .offset(pageable.getOffset())  // 몇 번째부터 가져올 지
                .limit(pageable.getPageSize())  // 한 페이지의 개수
                .groupBy(todo.id)  // todo 단위로 묶어주기
                .orderBy(todo.id.desc())  // 내림차순 정렬
                .fetch();

        // 전체 개수 조회하기(Page 만들기 위해서)
        Long totalCount = queryFactory
                .select(todo.id.countDistinct())  // 개수 세기
                .from(todo)
                .where(
                        titleContains(title),
                        createdAtBetween(createdStart, createdEnd),
                        managerNicknameContains(managerNickname)
                )
                .fetchOne();

        return new PageImpl<>(results, pageable, totalCount != null ? totalCount : 0L);
    }

    // BooleanExpression: null일 경우 조건에서 자동으로 제외
    // 제목 기준으로 검색하기
    private BooleanExpression titleContains(String title) {
        return title != null? todo.title.containsIgnoreCase(title) : null;
    }

    // 생성일 범위 기준으로 검색하기
    private BooleanExpression createdAtBetween(LocalDateTime createStart, LocalDateTime createEnd) {
        if (createStart != null && createEnd != null) {  // Start O, End O
            return todo.createdAt.between(createStart, createEnd);
        } else if (createStart != null) {  // Start O, End X
            return todo.createdAt.after(createStart);
        } else if(createEnd != null) {  // Start X, End O
            return todo.createdAt.before(createEnd);
        } else {  // Start X, End X
            return null;
        }
    }

    // nickname 가준으로 검색하기
    private BooleanExpression managerNicknameContains(String managerNickname) {
        return managerNickname != null ? user.nickname.containsIgnoreCase(managerNickname) : null;
    }


    // 특정 일정 조회
    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(
                        todoIdEq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    // todoId가 있는 경우(특정 일정 조회)
    private BooleanExpression todoIdEq(Long todoId) {
        return todoId != null ? todo.id.eq(todoId) : null;
    }
}
