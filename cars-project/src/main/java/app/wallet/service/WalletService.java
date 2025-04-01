package app.wallet.service;

import app.exception.DomainException;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionType;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import app.web.dto.TransferRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class WalletService {

    private static final String SMART_WALLET_LTD = "SMART_WALLET_LTD.";
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService, ApplicationEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }

    public List<Wallet> getAllWalletsByUsername(String username) {
        return walletRepository.findAllByOwnerUsername(username);
    }

    public void unlockNewWallet(User user) {

        List<Wallet> allUserWallets = walletRepository.findAllByOwnerUsername(user.getUsername());
        Subscription activeSubscription = user.getSubscriptions().getFirst();

        boolean isDefaultPlanAndMaxWalletsUnlocked = activeSubscription.getType() == SubscriptionType.DEFAULT && allUserWallets.size() == 1;

        if (isDefaultPlanAndMaxWalletsUnlocked) {
            throw new DomainException("Max wallet count reached for user with id [%s] and subscription type [%s]! ".formatted(user.getId(), activeSubscription.getType()));
        }

        Wallet newWallet = Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("0.00"))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        walletRepository.save(newWallet);
    }

    public Wallet initializeFirstWallet(User user) {
        List<Wallet> allUserWallets =  walletRepository.findAllByOwnerUsername(user.getUsername());
        if (!allUserWallets.isEmpty()) {
            throw new DomainException("User [%s] already has [%d] wallets! First wallet can't be initialized! ".formatted(user.getUsername(), allUserWallets.size()));
        }

        Wallet wallet = initWallet(user);
        walletRepository.save(wallet);
        log.info("Successfully created new wallet with id [%s] and balance [%.2f].".formatted(wallet.getId(), wallet.getBalance()));
        return wallet;
    }

    public Transaction transferFunds(User sender, TransferRequest transferRequest) {

        Wallet senderWallet = getWalletById(transferRequest.getFromWalletId());

//        Filter and find all active wallet by the receiver ID
        Optional<Wallet> receiverWalletOptional = walletRepository.findAllByOwnerUsername(transferRequest.getToUsername())
                .stream()
                .filter(wallet -> wallet.getStatus() == WalletStatus.ACTIVE)
                .findFirst();

        String transferDescription = "Transfer from %s to %s for %.2f EUR".formatted(
                sender.getUsername(),
                transferRequest.getToUsername(),
                transferRequest.getAmount().doubleValue()
        );

        if (receiverWalletOptional.isEmpty()) {
            String failureReason = "Receiver wallet is inactive";
            return transactionService.createNewTransaction(
                    sender,
                    senderWallet.getId().toString(),
                    transferRequest.getToUsername(),
                    transferRequest.getAmount(),
                    senderWallet.getBalance(),
                    senderWallet.getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    transferDescription,
                    failureReason
            );
        }

        Transaction withdrawal = charge(sender, senderWallet.getId(), transferRequest.getAmount(), transferDescription);

        if (withdrawal.getStatus() == TransactionStatus.FAILED) {
            return withdrawal;
        }

        Wallet receiverWallet = receiverWalletOptional.get();

        receiverWallet.setBalance(receiverWallet.getBalance().add(transferRequest.getAmount()));
        receiverWallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(receiverWallet);
        transactionService.createNewTransaction(
                receiverWallet.getOwner(),
                senderWallet.getId().toString(),
                receiverWallet.getId().toString(),
                transferRequest.getAmount(),
                receiverWallet.getBalance(),
                receiverWallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                transferDescription,
                null
        );
        log.info("Successfully transferred funds from wallet [%s] to wallet [%s] for %.2f".formatted(receiverWallet.getId(), senderWallet.getId(), transferRequest.getAmount().doubleValue()));
        return withdrawal;
    }

    @Transactional
    public Transaction topUp(UUID walletId, BigDecimal amount) {
        Wallet wallet = getWalletById(walletId);
        String description = "Top Up %.2f".formatted(amount.doubleValue());
        if (wallet.getStatus() == WalletStatus.DEACTIVATED) {
            return transactionService.createNewTransaction(wallet.getOwner(),
                    SMART_WALLET_LTD,
                    walletId.toString(),
                    amount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.DEPOSIT,
                    TransactionStatus.FAILED,
                    description,
                    "Inactive Wallet");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(wallet);

        return transactionService.createNewTransaction(
                wallet.getOwner(),
                SMART_WALLET_LTD,
                walletId.toString(),
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                description,
                null
        );
    }

    @Transactional
    public Transaction charge(User user, UUID walletId, BigDecimal amount, String description) {

        Wallet wallet = getWalletById(walletId);

        boolean isFailedTransaction = false;
        String failureReason = null;

        if (wallet.getStatus() == WalletStatus.DEACTIVATED) {
            failureReason = "Inactive wallet status";
            isFailedTransaction = true;
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            failureReason = "Insufficient balance";
            isFailedTransaction = true;
        }

        if (isFailedTransaction) {
            return transactionService.createNewTransaction(
                    user,
                    wallet.getId().toString(),
                    SMART_WALLET_LTD,
                    amount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    description,
                    failureReason
            );
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedOn(LocalDateTime.now());
        walletRepository.save(wallet);

        return transactionService.createNewTransaction(
                user,
                wallet.getId().toString(),
                SMART_WALLET_LTD,
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCEEDED,
                description,
                null
        );
    }

    private Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId).orElseThrow(() ->
                new DomainException("Wallet with [%s] does not exist".formatted(walletId)));
    }

    private Wallet initWallet(User user) {
        return Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("20.00"))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public Map<UUID, List<Transaction>> getLastFourTransactions(List<Wallet> wallets) {
        Map<UUID, List<Transaction>> transactionsByWalletId = new LinkedHashMap<>();
        for (Wallet wallet : wallets) {
            List<Transaction> lastFourTransactions = transactionService.getLastFourTransactionsByWallet(wallet);
            transactionsByWalletId.put(wallet.getId(), lastFourTransactions);
        }
        return transactionsByWalletId;
    }

    public void switchStatus(UUID walletId, UUID ownerId) {
        Optional<Wallet> optionalWallet = walletRepository.findByIdAndOwnerId(walletId, ownerId);
        if (optionalWallet.isEmpty()) {
            throw new DomainException("Wallet with [%s] does not belong to user with id [%s]".formatted(walletId, ownerId));
        }
        Wallet wallet = optionalWallet.get();
        if (wallet.getStatus() == WalletStatus.DEACTIVATED) {
            wallet.setStatus(WalletStatus.ACTIVE);
        } else {
            wallet.setStatus(WalletStatus.DEACTIVATED);
        }
        walletRepository.save(wallet);
    }
}
