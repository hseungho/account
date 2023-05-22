package com.zerobase.hseungho.account.service;

import com.zerobase.hseungho.account.dto.UseBalance;
import com.zerobase.hseungho.account.exception.AccountException;
import com.zerobase.hseungho.account.type.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {

    @Mock
    private LockService lockService;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @InjectMocks
    private LockAopAspect lockAopAspect;

    @Test
    void lockAndUnlock() throws Throwable {
        // given
        ArgumentCaptor<String> lockArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unLockArgumentCaptor = ArgumentCaptor.forClass(String.class);

        UseBalance.Request request = new UseBalance.Request(123L, "1234", 1000L);
        // when
        lockAopAspect.aroundMethod(proceedingJoinPoint, request);

        // then
        verify(lockService, times(1)).lock(lockArgumentCaptor.capture());
        verify(lockService, times(1)).unlock(unLockArgumentCaptor.capture());
        Assertions.assertEquals("1234", lockArgumentCaptor.getValue());
        Assertions.assertEquals("1234", unLockArgumentCaptor.getValue());
    }

    @Test
    void lockAndUnlock_evenIfThrow() throws Throwable {
        // given
        ArgumentCaptor<String> lockArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unLockArgumentCaptor = ArgumentCaptor.forClass(String.class);

        UseBalance.Request request = new UseBalance.Request(123L, "54321", 1000L);
        given(proceedingJoinPoint.proceed())
                .willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        // when
        Assertions.assertThrows(
                AccountException.class,
                () -> lockAopAspect.aroundMethod(proceedingJoinPoint, request)
        );

        // then
        verify(lockService, times(1)).lock(lockArgumentCaptor.capture());
        verify(lockService, times(1)).unlock(unLockArgumentCaptor.capture());
        Assertions.assertEquals("54321", lockArgumentCaptor.getValue());
        Assertions.assertEquals("54321", unLockArgumentCaptor.getValue());
    }


}