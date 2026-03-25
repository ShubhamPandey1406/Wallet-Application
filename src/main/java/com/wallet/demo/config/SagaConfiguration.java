package com.wallet.demo.config;


import com.wallet.demo.services.saga.SagaContext;
import com.wallet.demo.services.saga.SagaStep;
import com.wallet.demo.services.saga.steps.CreditDestinationWalletStep;
import com.wallet.demo.services.saga.steps.DebitSourceWalletStep;
import com.wallet.demo.services.saga.steps.SagaStepType;
import com.wallet.demo.services.saga.steps.UpdateTransactionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SagaConfiguration {

    @Bean
    public Map<String, SagaStep>sagaStepMap(
            DebitSourceWalletStep debitSourceWalletStep,
            CreditDestinationWalletStep creditDestinationWalletStep,
            UpdateTransactionStatus updateTransactionStatus)
    {
        Map<String, SagaStep> map=new HashMap<>();
        map.put(SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(),debitSourceWalletStep);
        map.put(SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(),creditDestinationWalletStep);
        map.put(SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(),updateTransactionStatus);
        return map;
    }

}
