package org.example.expert.domain.log.service;


import lombok.RequiredArgsConstructor;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    // 로그 생성하기
    @Transactional(propagation = Propagation.REQUIRES_NEW)  // 새로운 트랜잭션 생성하고 기존 트랜잭션 중단
    public void saveLog(String log) {
        logRepository.save(new Log(log));
    }
}
