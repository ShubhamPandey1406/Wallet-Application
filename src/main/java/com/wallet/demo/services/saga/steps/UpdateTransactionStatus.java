package com.wallet.demo.services.saga.steps;

import com.wallet.demo.entities.Transaction;
import com.wallet.demo.entities.TransactionStatus;
import com.wallet.demo.repositories.TransactionRepository;
import com.wallet.demo.services.saga.SagaContext;
import com.wallet.demo.services.saga.SagaStepInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionStatus implements SagaStepInterface {

    private final TransactionRepository transactionRepository;

    @Override
    public boolean execute(SagaContext context) {
        Long transactionId = context.getLong("transactionId");

        log.info("Updating transaction status for transaction id {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

        context.put("originalTransactionStatus", transaction.getStatus());

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        context.put("transactionStatusAfterUpdate", transaction.getStatus());

        log.info("Updated transaction status step executed successfully)");



        return true;
    }

    @Override
    public boolean compensate(SagaContext context) {

        Long transactionId = context.getLong("transactionId");
        TransactionStatus originalTransactionStatus= TransactionStatus.valueOf(context.getString("originalTransactionStatus"));

        log.info("Compensating transaction status for transaction id {}", transactionId);

        Transaction transaction=transactionRepository.findById(transactionId)
                .orElseThrow(()-> new RuntimeException("Transaction not found with id: "+ transactionId));

        transaction.setStatus(originalTransactionStatus);
        transactionRepository.save(transaction);

        log.info("Transaction status compensated for transaction {}", transactionId);


        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString();
    }
}
