package com.wallet.demo.repositories;

import com.wallet.demo.entities.Transaction;
import com.wallet.demo.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    List<Transaction> findByFromWalletId(Long fromWalletId);

    List<Transaction> findByToWalletId(Long toWalletId);

    @Query("SELECT t from Transaction t WHERE t.fromWalletId =:walletId OR t.toWalletId = :walletId")
    List<Transaction> findByWalletId(@Param("walletId") Long walletId);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);

}
