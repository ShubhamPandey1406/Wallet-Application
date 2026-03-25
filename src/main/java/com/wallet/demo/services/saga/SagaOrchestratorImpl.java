package com.wallet.demo.services.saga;

import com.wallet.demo.entities.SagaInstance;
import com.wallet.demo.entities.SagaStatus;
import com.wallet.demo.repositories.SagaInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorImpl implements SagaOrchestrator {


    private final ObjectMapper objectMapper;

    private final SagaInstanceRepository sagaInstanceRepository;

    @Override
    public Long startSaga(SagaContext context) {
        try {
            String contextJson = objectMapper.writeValueAsString(context); // coveerts the context to a JSON string
            log.info("Saga context serialized to JSON: {}", contextJson);
            SagaInstance sagaInstance = SagaInstance.builder()
                    .context(contextJson)
                    .status((SagaStatus.STARTED))
                    .build();

            sagaInstance = sagaInstanceRepository.save(sagaInstance);
            log.info("Saga instance created with id: {}", sagaInstance.getId());
            return sagaInstance.getId();


        } catch (Exception e) {
            log.error("Failed to start saga", e);
            throw new RuntimeException("Failed to start saga", e);
        }
    }

    @Override
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));



        return true;
    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        return false;
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return null;
    }

    @Override
    public void completeSaga(Long sagaInstanceId) {

    }

    @Override
    public void failSaga(Long sagaInstanceId) {

    }
}
