package com.wallet.demo.controllers;

import com.wallet.demo.dto.createWalletRequestDTO;
import com.wallet.demo.dto.creditWalletRequestDTO;
import com.wallet.demo.entities.Wallet;
import com.wallet.demo.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallets")
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody createWalletRequestDTO request) {
        log.info("Received request to create wallet");
        log.info("user id is {}" , request.getUserId());

        System.out.println("user id is " + request.getUserId());
         try{

             Wallet newWallet = walletService.createWallet(request.getUserId());

             log.info("Wallet created successfully with id: {}", newWallet.getId());
             return ResponseEntity.status(HttpStatus.CREATED).body(newWallet);

         }
         catch (Exception e){
             log.error("Error creating wallet", e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
         }
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<Wallet> getWallet(@PathVariable Long walletId) {
        log.info("Received request to get wallet with id: {}", walletId);
        Wallet wallet = walletService.getWalletById(walletId);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("{id}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable Long id) {
        log.info("Received request to get balance for wallet with id: {}", id);
        BigDecimal balance = walletService.getBalance(id);
        return ResponseEntity.ok(balance);
    }
  // Here in debit and credit we use userid instead of wallet id
  //beacuse we have to fetch the record from sharded database and in
    // wallet db sharding is done on the basis of user_id.
    @PostMapping("/{userid}/credit")
    public ResponseEntity<Wallet> creditWallet(@PathVariable Long userid, @RequestBody creditWalletRequestDTO request) {
        log.info("Received request to credit wallet with id: {}", userid);
        walletService.credit(userid, request.getAmount());
        Wallet wallet = walletService.getWalletByUserId(userid);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/{userid}/debit")
    public ResponseEntity<Wallet> debitWallet(@PathVariable Long userid, @RequestBody creditWalletRequestDTO request) {
        log.info("Received request to debit wallet with id: {}", userid);
        walletService.debit(userid, request.getAmount());
        Wallet wallet = walletService.getWalletByUserId(userid);
        return ResponseEntity.ok(wallet);
    }
}
