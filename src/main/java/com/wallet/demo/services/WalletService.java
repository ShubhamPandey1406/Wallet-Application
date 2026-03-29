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
        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created with id {}", savedWallet.getId());
        return savedWallet;
    }

    public Wallet getWalletById(Long walletId) {
        log.info("Fetching wallet with id {}", walletId);
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));
    }

    public List<Wallet> getWalletByUserId(Long userId) {
        log.info("Fetching wallet with userId {}", userId);
        return walletRepository.findByUserId(userId);

    }

    @Transactional
    public void debit(Long walletId, BigDecimal amount) {
        log.info("Debiting wallet with id {} by amount {}", walletId, amount);
        Wallet wallet = getWalletById(walletId);
        wallet.debit(amount);
        walletRepository.save(wallet);
        log.info("Wallet with id {} debited by amount {}", walletId, amount);
    }

    @Transactional
    public void credit(Long walletId, BigDecimal amount) {
        log.info("Crediting wallet with id {} by amount {}", walletId, amount);
        Wallet wallet = getWalletById(walletId);
        wallet.Credit(amount);
        walletRepository.save(wallet);
        log.info("Wallet with id {} credited by amount {}", walletId, amount);
    }

    public  BigDecimal getBalance(Long walletId){
        log.info("Fetching balance for wallet with id {}", walletId);
        BigDecimal balance = getWalletById(walletId).getBalance();
        log.info("Balance for wallet with id {} is {}", walletId, balance);
        return balance;
    }



}
