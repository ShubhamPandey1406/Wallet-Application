package com.wallet.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@AllArgsConstructor
@Builder
public class TransferResponseDTO {
    private Long sagaInstanceId;
}
