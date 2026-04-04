package com.wallet.demo.controllers;

import com.wallet.demo.dto.TransferRequestDTO;
import com.wallet.demo.dto.TransferResponseDTO;
import com.wallet.demo.entities.Transaction;
import com.wallet.demo.services.TransactionService;
import com.wallet.demo.services.TransferSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    private final TransferSagaService transferSagaService;

    @PostMapping
    public ResponseEntity<TransferResponseDTO> createTranasaction(@RequestBody TransferRequestDTO transferRequestDTO) {
        log.info("Received request to create transaction from wallet {} to wallet {} for amount {}",
                transferRequestDTO.getFromWalletId(), transferRequestDTO.getToWalletId(), transferRequestDTO.getAmount());

        try {
            Long sagaInstanceId = transferSagaService.initiateTransfer(transferRequestDTO.getFromWalletId()
                    , transferRequestDTO.getToWalletId(), transferRequestDTO.getAmount(), transferRequestDTO.getDescription());

            log.info("Transfer saga initiated with instance id: {}", sagaInstanceId);

            return ResponseEntity.status(201).body(
                    TransferResponseDTO.builder()
                            .sagaInstanceId(sagaInstanceId)
                            .build()
            );

        } catch (Exception e) {
            log.error("Error creating transaction", e);
            return ResponseEntity.status(500).body(null);
        }


    }
}
