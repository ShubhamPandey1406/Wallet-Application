package com.wallet.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
@Data
@Slf4j
@AllArgsConstructor
@Builder
public class TransferRequestDTO {
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
    private String description;
}
