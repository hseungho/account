package com.zerobase.hseungho.account.service;

import com.zerobase.hseungho.account.domain.Account;
import com.zerobase.hseungho.account.domain.AccountUser;
import com.zerobase.hseungho.account.domain.Transaction;
import com.zerobase.hseungho.account.dto.TransactionDto;
import com.zerobase.hseungho.account.exception.AccountException;
import com.zerobase.hseungho.account.repository.AccountRepository;
import com.zerobase.hseungho.account.repository.AccountUserRepository;
import com.zerobase.hseungho.account.repository.TransactionRepository;
import com.zerobase.hseungho.account.type.AccountStatus;
import com.zerobase.hseungho.account.type.ErrorCode;
import com.zerobase.hseungho.account.type.TransactionResultType;
import com.zerobase.hseungho.account.type.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    /**
     * 정책 <br>
     * - 사용자가 없는 경우, <br>
     * - 사용자 아이디와 계좌 소유주가 다른 경우, <br>
     * - 계좌가 이미 해지 상태인 경우, <br>
     * - 거래 금액이 잔액보다 큰 경우, <br>
     * - 거래금액이 너무 작거나 큰 경우, 실패 응답 <br>
     */
    public TransactionDto useBalance(Long userId,
                                     String accountNumber,
                                     Long amount) {
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateUseBalance(user, account, amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(
                saveAndGetTransaction(
                        TransactionType.USE,
                        TransactionResultType.S,
                        account,
                        amount
                )
        );
    }

    /**
     * 정책 <br>
     * - 거래 아이디에 해당하는 거래가 없는 경우, <br>
     * - 계좌가 없는 경우, <br>
     * - 거래와 계좌가 일치하지 않는 경우, <br>
     * - 거래금액과 거래취소금액이 일치하지 않는 경우(부분취소 불가), <br>
     * - 1년이 넘은 거래는 거래 취소 불가, <br>
     * - 해당 계좌에서 다른 거래가 진행 중일때 동시 처리 불가 <br>
     */
    public TransactionDto cancelBalance(String transactionId,
                                        String accountNumber,
                                        Long amount) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateCancelBalance(transaction, account, amount);

        account.cancelBalance(amount);

        return TransactionDto.fromEntity(
                saveAndGetTransaction(
                        TransactionType.CANCEL,
                        TransactionResultType.S,
                        account,
                        amount
                )
        );
    }

    public TransactionDto queryTransactionById(String transactionId) {
        return TransactionDto.fromEntity(
                transactionRepository.findByTransactionId(transactionId)
                        .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND))
        );
    }

    private Transaction saveAndGetTransaction(TransactionType transactionType,
                                              TransactionResultType transactionResultType,
                                              Account account,
                                              Long amount) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(transactionType)
                        .transactionResultType(transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }

    public void saveFailedUseTransaction(String accountNumber,
                                         Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(
                TransactionType.USE,
                TransactionResultType.F,
                account,
                amount
        );
    }

    public void saveFailedCancelTransaction(String accountNumber,
                                            Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(
                TransactionType.CANCEL,
                TransactionResultType.F,
                account,
                amount
        );
    }

    private void validateUseBalance(AccountUser user,
                                    Account account,
                                    Long amount) {
        if (!Objects.equals(user.getId(), account.getAccountUser().getId())) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() < amount) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
    }

    private void validateCancelBalance(Transaction transaction,
                                       Account account,
                                       Long amount) {
        if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
            throw new AccountException(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH);
        }
        if (!Objects.equals(transaction.getAmount(), amount)) {
            throw new AccountException(ErrorCode.CANCEL_MUST_FULLY);
        }
        if (transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1L))) {
            throw new AccountException(ErrorCode.TOO_OLD_ORDER_TO_CANCEL);
        }
    }

}
