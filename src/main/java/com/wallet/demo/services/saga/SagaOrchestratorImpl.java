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

import java.util.List;

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

        SagaStep sagaStepDB=sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.PENDING)
                .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build());

        if(sagaStepDB.getId()==null){
            sagaStepDB=sagaStepRepository.save(sagaStepDB);
        }

       try {
           SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
           sagaStepDB.markAsRunning();
           sagaStepRepository.save(sagaStepDB); //updating the status to running in DB
           boolean success = step.execute(sagaContext);

           if (success) {
               sagaStepDB.markAsCompleted();
               sagaStepRepository.save(sagaStepDB);//updating the status to completed in DB
               sagaInstance.setCurrentStep(stepName);
               sagaInstance.markAsRunning();
               sagaInstanceRepository.save(sagaInstance); //updating the current step and status in DB
               log.info("Saga step {} executed successfully for saga instance id: {}", stepName, sagaInstanceId);
               return true;
           } else {
               sagaStepDB.markAsFailed();
               sagaStepRepository.save(sagaStepDB); //updating the status to failed in DB
               log.error("Saga step {} execution failed for saga instance id: {}", stepName, sagaInstanceId);
               return false;

           }
       }
       catch (Exception e) {
           sagaStepDB.markAsFailed();
           sagaStepRepository.save(sagaStepDB);
           log.error(" Step {} failed", stepName);
          // throw new RuntimeException("Failed to deserialize saga context for saga instance id: " + sagaInstanceId, e);
           return false;
       }

    }

    @Override
    @Transactional
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

        SagaStep sagaStepDB=sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.COMPLETED)   // Only steps which are completed can be compensated
                .orElse(null); // if there is no step in DB with completed status then we can not compensate it. so returning null

        if(sagaStepDB.getId()==null){
            log.info("Saga step with name {} and status completed not found for saga instance id: {}", stepName, sagaInstanceId);
           return true;
        }

        //3)Take the context from saga instance and call compensate method
        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.markAsCompensating();  // Mark the step as compensating in DB
            sagaStepRepository.save(sagaStepDB); //updating the status to compensating in DB
            boolean success = step.compensate(sagaContext);   // calling the compensate method of the step

            if (success) {
                sagaStepDB.markAsCompensated(); //if successful then mark the step compensated
                sagaStepRepository.save(sagaStepDB); //updating the status to completed in DB
                log.info("Saga step {} compensated successfully for saga instance id: {}", stepName, sagaInstanceId);
                return true;
            } else {
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB); //updating the status to failed in DB
                log.error("Saga step {} compenation failed for saga instance id: {}", stepName, sagaInstanceId);
                return false;

            }
        }
        catch (Exception e) {
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB);
            log.error(" Step {} failed", stepName);
            // throw new RuntimeException("Failed to deserialize saga context for saga instance id: " + sagaInstanceId, e);
            return false;
        }

    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));
    }

    @Override
    @Transactional
    public void completeSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));

        sagaInstance.markAsCompleted();
        sagaInstanceRepository.save(sagaInstance);
        log.info("Saga instance with id {} marked as completed", sagaInstanceId);
    }

    @Override
    @Transactional
    public void failSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));

        sagaInstance.markAsFailed();
        sagaInstanceRepository.save(sagaInstance);
        log.info("Saga instance with id {} marked as failed", sagaInstanceId);
    }

    @Override
    @Transactional
    public void compensateSaga(Long sagaInstanceId) {

        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId)
                    .orElseThrow(() -> new RuntimeException("Saga instance not found with id: " + sagaInstanceId));

            sagaInstance.markAsCompensating();
            sagaInstanceRepository.save(sagaInstance);
            log.info("Saga instance with id {} marked as compensated", sagaInstanceId);

            //Get all the steps which are completed or compensated
        List<SagaStep> completedOStep=sagaStepRepository.findcompletedStepsBySagaInstanceId(sagaInstanceId);

        boolean allCompensated=true;
        for(SagaStep step:completedOStep){
            boolean compensate =this.compensateStep(sagaInstanceId, step.getStepName());

            if(!compensate)
            {
                allCompensated=false;
            }

            if(allCompensated)
            {
                sagaInstance.markAsCompensated();
                sagaInstanceRepository.save(sagaInstance);
                log.info("Saga instance with id {} marked as compensated", sagaInstanceId);
            }
            else
            {
                log.info("Saga {} compensation failed ", sagaInstanceId);
            }
        }

    }


}
