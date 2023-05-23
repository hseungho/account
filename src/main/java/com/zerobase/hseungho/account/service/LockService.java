package com.zerobase.hseungho.account.service;

import com.zerobase.hseungho.account.exception.AccountException;
import com.zerobase.hseungho.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
    private final RedissonClient redissonClient;

    public void lock(String accountNumber) {
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));
        log.debug("Trying lock for accountNumber : {}", accountNumber);

        try {
            // waitTime: lock 취득 대기 시간
            // leaseTime: lock 자동 해제 시간
            boolean isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if(!isLock) {
                log.error("======Lock acquisition failed=====");
                throw new AccountException(ErrorCode.ACCOUNT_TRANSACTION_LOCK);
            }
        } catch (AccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis lock failed");
        }
    }

    public void unlock(String accountNumber) {
        log.debug("Unlock for accountNumber : {}" ,accountNumber);

        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }

    private String getLockKey(String accountNumber) {
        return "ACLK:" + accountNumber;
    }
}
