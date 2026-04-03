package com.wallet.demo.repositories;

import com.wallet.demo.entities.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance,Long> {

}
