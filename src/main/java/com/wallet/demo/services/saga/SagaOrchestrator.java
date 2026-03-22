package com.wallet.demo.services.saga;

import com.wallet.demo.entities.SagaInstance;

public interface SagaOrchestrator {

    Long startSaga(SagaContext context);

    boolean executeStep(Long sagaInstanceId, String stepName);

   boolean compensateStep(Long sagaInstanceId, String stepName);

    SagaInstance getSagaInstance(Long sagaInstanceId);

    void completeSaga(Long sagaInstanceId);

    void failSaga(Long sagaInstanceId);


}
