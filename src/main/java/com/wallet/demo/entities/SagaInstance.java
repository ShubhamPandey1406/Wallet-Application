package com.wallet.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.calcite.avatica.SqlType;
import org.apache.calcite.model.JsonType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="saga_instance")
public class SagaInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="status",nullable = false)
    private SagaStatus status = SagaStatus.STARTED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="context" , columnDefinition = "json")
    private String context;

    @Column(name="current_step",nullable = false)
    private String currentStep;





}
