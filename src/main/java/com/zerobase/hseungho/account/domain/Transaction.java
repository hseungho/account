package com.zerobase.hseungho.account.domain;

import com.zerobase.hseungho.account.type.TransactionResultType;
import com.zerobase.hseungho.account.type.TransactionType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;

    @ManyToOne
    private Account account;

    private Long amount;

    private Long balanceSnapshot;

    private String transactionId;

    private LocalDateTime transactedAt;

}
