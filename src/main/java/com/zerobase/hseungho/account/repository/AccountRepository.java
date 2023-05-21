package com.zerobase.hseungho.account.repository;

import com.zerobase.hseungho.account.domain.Account;
import com.zerobase.hseungho.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findFirstByOrderByIdDesc();
    Long countByAccountUser(AccountUser accountUser);
}
