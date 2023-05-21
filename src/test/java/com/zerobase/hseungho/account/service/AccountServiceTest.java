package com.zerobase.hseungho.account.service;

import com.zerobase.hseungho.account.domain.Account;
import com.zerobase.hseungho.account.domain.AccountUser;
import com.zerobase.hseungho.account.dto.AccountDto;
import com.zerobase.hseungho.account.exception.AccountException;
import com.zerobase.hseungho.account.repository.AccountUserRepository;
import com.zerobase.hseungho.account.type.AccountStatus;
import com.zerobase.hseungho.account.repository.AccountRepository;
import com.zerobase.hseungho.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccountSuccess() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1000000012").build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(1L, 100L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000013", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("첫 계좌 생성")
    void createFirstAccount() {
        // given
        AccountUser user = AccountUser.builder()
                .id(15L)
                .name("Pobi")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013").build());
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.createAccount(1L, 100L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void createAccount_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.createAccount(1L, 100L)
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("유저당 최대 계좌는 10개")
    void createAccount_maxAccountIs10() {
        // given
        AccountUser user = AccountUser.builder()
                .id(15L)
                .name("Pobi")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10L);

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.createAccount(1L, 100L)
        );

        // then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());

    }

    @Test
    @DisplayName("계좌 해지 성공")
    void deleteAccountSuccess() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.deleteAccount(1L, "1234567890");

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() {
        // given
        given((accountUserRepository.findById(anyLong())))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890")
        );
        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccount_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        given((accountUserRepository.findById(anyLong())))
                .willReturn(Optional.of(user));
        given((accountRepository.findByAccountNumber(anyString())))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890")
        );
        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자와 계좌 소유주 불일치 - 계좌 해지 실패")
    void deleteAccount_UserAccountUnMatch() {
        // given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        AccountUser harry = AccountUser.builder()
                .id(13L)
                .name("Harry")
                .build();
        given((accountUserRepository.findById(anyLong())))
                .willReturn(Optional.of(pobi));
        given((accountRepository.findByAccountNumber(anyString())))
                .willReturn(Optional.of(
                        Account.builder()
                                .accountUser(harry)
                                .balance(0L)
                                .accountNumber("1000000012").build()
                ));
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890")
        );
        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 해지된 계좌 - 계좌 해지 실패")
    void deleteAccount_AccountAlreadyUnregistered() {
        // given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        given((accountUserRepository.findById(anyLong())))
                .willReturn(Optional.of(pobi));
        given((accountRepository.findByAccountNumber(anyString())))
                .willReturn(Optional.of(
                        Account.builder()
                                .accountUser(pobi)
                                .balance(100L)
                                .accountStatus(AccountStatus.UNREGISTERED)
                                .accountNumber("1000000012").build()
                ));
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890")
        );
        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 잔액 보유 - 계좌 해지 실패")
    void deleteAccount_BalanceNotEmpty() {
        // given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        given((accountUserRepository.findById(anyLong())))
                .willReturn(Optional.of(pobi));
        given((accountRepository.findByAccountNumber(anyString())))
                .willReturn(Optional.of(
                        Account.builder()
                                .accountUser(pobi)
                                .balance(100L)
                                .accountNumber("1000000012").build()
                ));
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890")
        );
        // then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
    }








    @Test
    @DisplayName("계좌 조회 성공")
    void testXXX() {
        //given
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65789").build()));
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        //when
        Account account = accountService.getAccount(4555L);

        //then
        verify(accountRepository, times(1)).findById(captor.capture());
        verify(accountRepository, times(0)).save(any());
        assertEquals(4555L, captor.getValue());
        assertNotEquals(45515L, captor.getValue());
        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }

    @Test
    @DisplayName("계좌 조회 실패 - 음수로 조회")
    void testFailedToSearchAccount() {
        //given
        //when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.getAccount(-10L));

        //then
        assertEquals("Minus", exception.getMessage());
    }

    @Test
    @DisplayName("Test 이름 변경")
    void testGetAccount() {
        //given
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65789").build()));

        //when
        Account account = accountService.getAccount(4555L);

        //then
        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }

    @Test
    void testGetAccount2() {
        //given
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65789").build()));

        //when
        Account account = accountService.getAccount(4555L);

        //then
        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }
}