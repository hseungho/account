package com.zerobase.hseungho.account.controller;

import com.zerobase.hseungho.account.dto.CancelBalance;
import com.zerobase.hseungho.account.dto.QueryTransaction;
import com.zerobase.hseungho.account.dto.UseBalance;
import com.zerobase.hseungho.account.exception.AccountException;
import com.zerobase.hseungho.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 잔액 관련 컨트롤러
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(
            @RequestBody @Valid UseBalance.Request request
    ) {
        try {
            return UseBalance.Response.fromDto(
                    transactionService.useBalance(
                            request.getUserId(),
                            request.getAccountNumber(),
                            request.getAmount()
                    )
            );
        } catch (AccountException e) {
            log.error("Failed to use balance.");

            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    public CancelBalance.Response cancelBalance(
            @RequestBody @Valid CancelBalance.Request request
    ) {
        try {
            return CancelBalance.Response.fromDto(
                    transactionService.cancelBalance(
                            request.getTransactionId(),
                            request.getAccountNumber(),
                            request.getAmount()
                    )
            );
        } catch (AccountException e) {
            log.error("Failed to cancel balance.");

            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e;
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public QueryTransaction.Response queryTransaction(
            @PathVariable String transactionId
    ) {
        return QueryTransaction.Response.fromDto(
                transactionService.queryTransactionById(transactionId)
        );
    }

}
