package com.wallet.demo.services;

import com.wallet.demo.entities.Transaction;
import com.wallet.demo.entities.TransactionStatus;
import com.wallet.demo.entities.TransactionType;
import com.wallet.demo.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction createTransaction(Long fromWalletId, Long toWalletId, BigDecimal amount,String description) {
        log.info("Creating transaction from wallet {} to wallet {} with amount {}", fromWalletId, toWalletId, amount);
        Transaction transaction = Transaction.builder()
                .fromWalletId(fromWalletId)
                .toWalletId(toWalletId)
                .amount(amount)
                .description(description)
                .status(TransactionStatus.PENDING)
                .transactionType(TransactionType.TRANSFER)
                .build();

                Transaction savedTransaction =transactionRepository.save(transaction);
                log.info("Transaction created with id {}", savedTransaction.getId());
                return savedTransaction;
    }

    public Transaction getTransactionById(Long transactionId) {
        log.info("Fetching transaction with id {}", transactionId);
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));
    }

    public List<Transaction> getTransactionByWalletId(Long walletId) {
        log.info("Fetching transaction with wallet id {}", walletId);
        return transactionRepository.findByWalletId(walletId);
    }

    public List<Transaction> getTransactionByFromWalletId(Long WalletId){
        return transactionRepository.findByFromWalletId(WalletId);
    }

    public List<Transaction> getTransactionByToWalletId(Long WalletId){
        return transactionRepository.findByToWalletId(WalletId);
    }

    public List<Transaction> getTransactionByStatus(TransactionStatus status){
        return transactionRepository.findByStatus(status);
    }

    public List<Transaction> getTransactionBySagaInstanceId(Long sagaInstanceId){
        return transactionRepository.findBySagaInstanceId(sagaInstanceId);
    }

    public void updateTransactionWithSagaInstanceId(Long transactionId, Long sagaInstanceId){
        log.info("Updating transaction status for transaction id {}", transactionId);
        Transaction transaction = getTransactionById(transactionId);
        transaction.setSagaInstanceId(sagaInstanceId);
        transactionRepository.save(transaction);
        log.info("Transaction status updated for transaction id {} to {}", transactionId);
    }


}

