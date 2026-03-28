package com.wallet.demo.config;


import com.wallet.demo.services.saga.SagaStepInterface;
import com.wallet.demo.services.saga.steps.CreditDestinationWalletStepInterface;
import com.wallet.demo.services.saga.steps.DebitSourceWalletStepInterface;
import com.wallet.demo.services.saga.steps.SagaStepType;
import com.wallet.demo.services.saga.steps.UpdateTransactionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SagaConfiguration {

    @Bean
    public Map<String, SagaStepInterface>sagaStepMap(
            DebitSourceWalletStepInterface debitSourceWalletStep,
            CreditDestinationWalletStepInterface creditDestinationWalletStep,
            UpdateTransactionStatus updateTransactionStatus)
    {
        Map<String, SagaStepInterface> map=new HashMap<>();
        map.put(SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(),debitSourceWalletStep);
        map.put(SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(),creditDestinationWalletStep);
        map.put(SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(),updateTransactionStatus);
        return map;
    }

}
