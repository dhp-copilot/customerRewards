package my.customer.rewards.controller;

import my.customer.rewards.dto.*;
import my.customer.rewards.service.CustomerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/{username}/transactions")
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    public TransactionResponse addTransaction(@PathVariable String username,
                                              @Valid @RequestBody TransactionRequest request) {
        return customerService.addTransaction(username, request);
    }

    @GetMapping("/{username}/transactions")
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    public List<TransactionResponse> transactions(@PathVariable String username) {
        return customerService.transactions(username);
    }
}
