package com.wallet.demo.services.saga.steps;

import com.wallet.demo.entities.Wallet;
import com.wallet.demo.repositories.WalletRepository;
import com.wallet.demo.services.saga.SagaContext;
import com.wallet.demo.services.saga.SagaStep;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebitSourceWalletStep implements SagaStep {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Debiting source wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet=walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + fromWalletId));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put("OriginalSourceWalletBalance", wallet.getBalance());


        wallet.debit(amount);
        walletRepository.save(wallet);

        log.info("Wallet debited successfully with new balance {}", wallet.getBalance());
        context.put("fromWalletBalanceAfterDebit", wallet.getBalance());


        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {

        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");
        log.info("Compensating DebitSourceWalletStep for wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet=walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + fromWalletId));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put("SourceWalletBalanceBeforeCompensation", wallet.getBalance());

        wallet.Credit(amount);
        walletRepository.save(wallet);

        log.info("Wallet compensation successful with new balance {}", wallet.getBalance());

        context.put("SourceWalletBalanceAfterCreditCompensation", wallet.getBalance());

        log.info("DebitSourceWalletStep compensation executed successfully");



        return true;
    }

    @Override
    public String getStepName() {
        return "DebitSourceWalletStep";
    }
}
