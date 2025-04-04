package app.web;

import app.security.AuthenticationMetadata;
import app.transaction.model.Transaction;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping ("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final WalletService walletService;

    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService, WalletService walletService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping("")
    public ModelAndView getAllTransactions(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        List<Transaction> transactions = transactionService.getAllByOwnerId(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView("transactions");
        modelAndView.addObject("transactions", transactions);
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUri", "/transactions");
        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getTransactionById(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        Transaction transaction = transactionService.getById(id);
        UUID userId = authenticationMetadata.getUserId();
        User user = userService.getById(userId);
        ModelAndView modelAndView = new ModelAndView("transaction-result");
        modelAndView.addObject("transaction", transaction);
        modelAndView.addObject("user", user);
        return modelAndView;
    }
    
    @GetMapping("/top-up")
    public ModelAndView showTopUpPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView("wallet-top-up");
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUri", "/transactions/top-up");
        
        // Get the user's first wallet if they have one
        List<Wallet> wallets = walletService.getAllWalletsByUsername(user.getUsername());
        if (!wallets.isEmpty()) {
            modelAndView.addObject("wallet", wallets.get(0));
        }
        
        return modelAndView;
    }
    
    @PostMapping("/top-up")
    public RedirectView processTopUp(
            @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
            @RequestParam UUID walletId,
            @RequestParam BigDecimal amount) {
        
        // Process the top-up
        Transaction transaction = walletService.topUp(walletId, amount);
        
        // Redirect to the transaction result page
        return new RedirectView("/transactions/" + transaction.getId());
    }
}
