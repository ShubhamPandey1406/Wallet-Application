package com.wallet.demo.services.saga.steps;

import com.wallet.demo.services.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {

    private final Map<String, SagaStep> sagaStepMap;

    public static SagaStep getSagaStep(String Stepname, Map<String, SagaStep> sagaStepMap)
    {
        SagaStep step=sagaStepMap.get(Stepname);
        if(step==null)
        {
            throw new IllegalArgumentException("No SagaStep found for name: "+ Stepname);
        }
        return step;

    }


}
