package com.wallet.demo.services.saga.steps;

import com.wallet.demo.entities.Wallet;
import com.wallet.demo.repositories.WalletRepository;
import com.wallet.demo.services.saga.SagaContext;
import com.wallet.demo.services.saga.SagaStepInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditDestinationWalletStepInterface implements SagaStepInterface {

    private final WalletRepository walletRepository;


    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        //Step1: Get the destination walllet id from context

        Long toWalletId= context.getLong("toWalletId");
        BigDecimal amount= context.getBigDecimal("amount");
        log.info("Crediting destination wallet {} with amount {}", toWalletId, amount);

        //Step 2:Fetch the destination wallet from one database with a lock

        Wallet wallet=walletRepository.findByIdWithLock(toWalletId).
                orElseThrow(()-> new IllegalArgumentException("Wallet not found: "+ toWalletId));

        log.info("Wallet eftched with balance {}", wallet.getBalance());
        context.put("OriginalWalletBalance", wallet.getBalance());



        //Step 3: Credit the destination wallet

        wallet.Credit(amount); //by calling this method,only jave object is updated in memory
        walletRepository.save(wallet);// this will persist the changes to database

        log.info("Wallet credited successfully with new balance {}", wallet.getBalance());
        context.put("toWalletBalanceAfterCredit", wallet.getBalance());

        //Step 4: Update the context with the changes

        log.info("CreditDestinationWalletStep executed successfully");
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {

        //Step1: Get the destination walllet id from context
        Long toWalletId= context.getLong("toWalletId");
        BigDecimal amount= context.getBigDecimal("amount");
        log.info("Compensating CreditDestinationWalletStep for wallet {} with amount {}", toWalletId, amount);

        //Step 2:Fetch the destination wallet from one database with a lock

        Wallet wallet=walletRepository.findByIdWithLock(toWalletId).
                orElseThrow(()-> new IllegalArgumentException("Wallet not found: "+ toWalletId));

        log.info("Wallet eftched with balance {}", wallet.getBalance());



        //Step 3: Credit the destination wallet

        wallet.debit(amount); //by calling this method,only jave object is updated in memory
        walletRepository.save(wallet);// this will persist the changes to database

        log.info("Wallet credited successfully with new balance {}", wallet.getBalance());
        context.put("toWalletBalanceAfterCreditAfterCreditCompensation", wallet.getBalance());

        log.info("CreditDestinationWalletStep compensation executed successfully");

        return false;
    }

    @Override
    public String getStepName() {
        return SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString();
    }
}
