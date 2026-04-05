package com.wallet.demo.services.saga.steps;

import com.wallet.demo.entities.Wallet;
import com.wallet.demo.repositories.WalletRepository;
import com.wallet.demo.services.saga.SagaContext;
import com.wallet.demo.services.saga.SagaStepInterface;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebitSourceWalletStepInterface implements SagaStepInterface {

    private final WalletRepository walletRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean execute(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Debiting source wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet=walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + fromWalletId));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put("OriginalSourceWalletBalance", wallet.getBalance());





     walletRepository.updateBalanceByUserId(wallet.getUserId(), wallet.getBalance().subtract(amount));

        log.info("Wallet debited successfully with new balance {}", wallet.getBalance());
        context.put("fromWalletBalanceAfterDebit", wallet.getBalance());


        return true;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean compensate(SagaContext context) {

        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");
        log.info("Compensating DebitSourceWalletStep for wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet=walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + fromWalletId));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put("SourceWalletBalanceBeforeCompensation", wallet.getBalance());

       walletRepository.updateBalanceByUserId(wallet.getUserId(), wallet.getBalance().add(amount));

        log.info("Wallet compensation successful with new balance {}", wallet.getBalance());

        context.put("SourceWalletBalanceAfterCreditCompensation", wallet.getBalance());

        log.info("DebitSourceWalletStep compensation executed successfully");



        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString();
    }
}
