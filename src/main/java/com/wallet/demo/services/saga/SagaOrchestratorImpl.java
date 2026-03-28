package com.wallet.demo.services.saga;

import com.wallet.demo.entities.SagaInstance;
import com.wallet.demo.entities.SagaStatus;
import com.wallet.demo.entities.SagaStep;
import com.wallet.demo.entities.StepStatus;
import com.wallet.demo.repositories.SagaInstanceRepository;
import com.wallet.demo.repositories.SagaStepRepository;
import com.wallet.demo.services.saga.steps.SagaStepFactory;
import jakarta.transaction.Transactional;
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

    private final SagaStepFactory sagaStepFactory;

    private final SagaStepRepository sagaStepRepository;

    @Override
    @Transactional
    public Long startSaga(SagaContext context) {
        try {
            String contextJson = objectMapper.writeValueAsString(context); // converts the context to a JSON string
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
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);

        if(step ==null){
            log.error("Saga step not found with name: {}", stepName);
            throw new RuntimeException("Saga step not found with name: " + stepName);
        }

        SagaStep sagaStepDB=sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, StepStatus.PENDING)
                .stream().filter(s -> s.getStepName().equals(stepName))
                .findFirst()
                .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build());


        if(sagaStepDB.getId()==null){
            sagaStepDB=sagaStepRepository.save(sagaStepDB);
        }

       try {
           SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
           sagaStepDB.setStatus(StepStatus.RUNNING);
           sagaStepRepository.save(sagaStepDB); //updating the status to running in DB
           boolean success = step.execute(sagaContext);

           if (success) {
               sagaStepDB.setStatus(StepStatus.COMPLETED);
               sagaStepRepository.save(sagaStepDB); //updating the status to completed in DB
               log.info("Saga step {} executed successfully for saga instance id: {}", stepName, sagaInstanceId);
               return true;
           } else {
               sagaStepDB.setStatus(StepStatus.FAILED);
               sagaStepRepository.save(sagaStepDB); //updating the status to failed in DB
               sagaInstance.setCurrentStep(stepName);
               sagaInstance.setStatus(SagaStatus.RUNNING);
               sagaInstanceRepository.save(sagaInstance); //updating the current step and status in DB
               log.error("Saga step {} execution failed for saga instance id: {}", stepName, sagaInstanceId);
               return false;

           }
       }
       catch (Exception e) {
           sagaStepDB.setStatus(StepStatus.FAILED);
           sagaStepRepository.save(sagaStepDB);
           log.error(" Step {} failed", stepName);
          // throw new RuntimeException("Failed to deserialize saga context for saga instance id: " + sagaInstanceId, e);
           return false;
       }

    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {

        //1) Fetch the saga instance from database using sagaInstanceId
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));

        //2) Fetch the step from database using sagaInstanceId and stepName.

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);

        if(step ==null){
            log.error("Saga step not found with name: {}", stepName);
            throw new RuntimeException("Saga step not found with name: " + stepName);
        }

        SagaStep sagaStepDB=sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, StepStatus.FAILED)

                .stream().filter(s -> s.getStepName().equals(stepName))
                .findFirst()
                .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build());


        if(sagaStepDB.getId()==null){
            sagaStepDB=sagaStepRepository.save(sagaStepDB);
        }



        //3)Take the context from saga instance and call compensate method

        //4)Update the appropriate status in the saga step


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
