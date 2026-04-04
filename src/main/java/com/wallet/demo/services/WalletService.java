package com.wallet.demo.services;

import com.wallet.demo.entities.Wallet;
import com.wallet.demo.repositories.WalletRepository;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet createWallet(Long userId) {
        log.info("Creating wallet for userId {}", userId);
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .isActive(true)
                .balance(BigDecimal.ZERO)
                .build();
        wallet = walletRepository.save(wallet);
        log.info("Wallet created with id {}", wallet.getId());
        return wallet;
    }

    public Wallet getWalletById(Long walletId) {
        log.info("Fetching wallet with id {}", walletId);
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));
    }

    public Wallet getWalletByUserId(Long userId) {
        log.info("Fetching wallet with userId {}", userId);
        return walletRepository.findByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Wallet not found for user id: " + userId));
    }

//    public List<Wallet> getWalletByUserId(Long userId) {
//        log.info("Fetching wallet with userId {}", userId);
//        return walletRepository.findByUserId(userId);
//
//    }

    @Transactional
    public void debit(Long userId, BigDecimal amount) {
        log.info("Debiting wallet with id {} by amount {}", userId, amount);
        Wallet wallet = getWalletByUserId(userId);
        walletRepository.updateBalanceByUserId(userId, wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        log.info("Wallet with id {} debited by amount {}", userId, amount);
    }

    @Transactional
    public void credit(Long userId, BigDecimal amount) {
        log.info("Crediting wallet with id {} by amount {}", userId, amount);
        Wallet wallet = getWalletByUserId(userId);
        walletRepository.updateBalanceByUserId(userId, wallet.getBalance().add(amount));
        log.info("Wallet with id {} credited by amount {}", userId, amount);
    }

    public  BigDecimal getBalance(Long walletId){
        log.info("Fetching balance for wallet with id {}", walletId);
        BigDecimal balance = getWalletById(walletId).getBalance();
        log.info("Balance for wallet with id {} is {}", walletId, balance);
        return balance;
    }



}
