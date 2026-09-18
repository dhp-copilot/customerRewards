package my.customer.rewards.controller;

import my.customer.rewards.dto.MonthlyRewardReport;
import my.customer.rewards.dto.TotalRewardReport;
import my.customer.rewards.service.RewardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RewardController {
    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping({"/customers/{username}/rewards/monthly", "/rewards/{username}/monthly"})
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    public MonthlyRewardReport monthly(@PathVariable String username,
                                       @RequestParam int year, @RequestParam int month) {
        return rewardService.monthly(username, year, month);
    }

    @GetMapping({"/customers/{username}/rewards/monthly/all", "/rewards/{username}/monthly/all"})
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    public List<MonthlyRewardReport> monthlyReports(@PathVariable String username) {
        return rewardService.monthlyReports(username);
    }

    @GetMapping({"/customers/{username}/rewards/total", "/rewards/{username}/total",
            "/customers/{username}/rewards", "/rewards/{username}"})
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    public TotalRewardReport total(@PathVariable String username) {
        return rewardService.total(username);
    }
}
