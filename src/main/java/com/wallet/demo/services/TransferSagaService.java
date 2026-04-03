package com.wallet.demo.services;

import com.wallet.demo.entities.Transaction;
import com.wallet.demo.services.saga.SagaContext;
import com.wallet.demo.services.saga.SagaOrchestrator;
import com.wallet.demo.services.saga.steps.SagaStepFactory;
import com.wallet.demo.services.saga.steps.SagaStepType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static ch.qos.logback.classic.spi.ThrowableProxyVO.build;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferSagaService {

    private final TransactionService transactionService;

    private SagaOrchestrator sagaOrchestrator;



    @Transactional
    public Long initiateTransfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String description) {
        log.info("Initiating transfer from wallet {} to wallet {} for amount {}", fromWalletId, toWalletId, amount);
        Transaction transaction = transactionService.createTransaction(fromWalletId, toWalletId, amount, description);

        SagaContext sagaContext = SagaContext.builder()
                .data(Map.ofEntries(
                        Map.entry("transactionId", transaction.getId()),
                        Map.entry("fromWalletId", fromWalletId),
                        Map.entry("toWalletId", toWalletId),
                        Map.entry("amount", amount),
                        Map.entry("description", description)
                ))
                .build();
        // Start the saga
        Long sagaInstanceId = sagaOrchestrator.startSaga(sagaContext);
        log.info("Saga instance created  with id {}", sagaInstanceId);

        transactionService.updateTransactionWithSagaInstanceId(transaction.getId(), sagaInstanceId);

         executeTransferSaga(sagaInstanceId);

         return sagaInstanceId;
    }

    @Transactional
    public void executeTransferSaga(Long SagaIntanceId)
    {
       log.info("Executing transfer saga with id {}", SagaIntanceId);
       try{
           for(SagaStepFactory.SagaStepType stepType : SagaStepFactory.TransferMoneySagaSteps)
           {
               boolean stepResult = sagaOrchestrator.executeStep(SagaIntanceId, stepType.name());
               if(!stepResult)
               {
                   log.error("Saga step {} failed for saga instance {}", stepType.name(), SagaIntanceId);
                   sagaOrchestrator.failSaga(SagaIntanceId);

               }
           }
           sagaOrchestrator.completeSaga(SagaIntanceId);
           log.info("Saga instance {} completed successfully", SagaIntanceId);
       }
       catch(Exception e)
       {
              log.error("Saga instance {} failed with exception {}", SagaIntanceId, e.getMessage());
              sagaOrchestrator.failSaga(SagaIntanceId);
       }
    }


}
