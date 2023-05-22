package com.zerobase.hseungho.account.dto;

import com.zerobase.hseungho.account.type.TransactionResultType;
import com.zerobase.hseungho.account.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

public class QueryTransaction {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private String accountNumber;
        private TransactionType transactionType;
        private TransactionResultType transactionResult;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactedAt;

        public static Response fromDto(TransactionDto transactionDto) {
            return Response.builder()
                    .accountNumber(transactionDto.getAccountNumber())
                    .transactionType(transactionDto.getTransactionType())
                    .transactionResult(transactionDto.getTransactionResultType())
                    .transactionId(transactionDto.getTransactionId())
                    .amount(transactionDto.getAmount())
                    .transactedAt(transactionDto.getTransactedAt())
                    .build();
        }
    }

}
