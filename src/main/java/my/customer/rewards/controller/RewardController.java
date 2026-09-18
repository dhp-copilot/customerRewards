package my.customer.rewards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import my.customer.rewards.exception.ApiError;
import my.customer.rewards.dto.MonthlyRewardReport;
import my.customer.rewards.dto.TotalRewardReport;
import my.customer.rewards.service.RewardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Rewards", description = "Monthly and total reward reports.")
public class RewardController {
    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping({"/customers/{username}/rewards/monthly", "/rewards/{username}/monthly"})
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @Operation(summary = "Get one monthly reward report",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly report",
                    content = @Content(schema = @Schema(implementation = MonthlyRewardReport.class))),
            @ApiResponse(responseCode = "400", description = "Invalid year or month",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public MonthlyRewardReport monthly(@PathVariable String username,
                                       @Parameter(description = "Calendar year", example = "2024")
                                       @RequestParam int year,
                                       @Parameter(description = "Calendar month (1-12)", example = "1")
                                       @RequestParam int month) {
        return rewardService.monthly(username, year, month);
    }

    @GetMapping({"/customers/{username}/rewards/monthly/all", "/rewards/{username}/monthly/all"})
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @Operation(summary = "List all monthly reward reports",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly reports"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<MonthlyRewardReport> monthlyReports(@PathVariable String username) {
        return rewardService.monthlyReports(username);
    }

    @GetMapping({"/customers/{username}/rewards/total", "/rewards/{username}/total",
            "/customers/{username}/rewards", "/rewards/{username}"})
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @Operation(summary = "Get total rewards",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total report",
                    content = @Content(schema = @Schema(implementation = TotalRewardReport.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public TotalRewardReport total(@PathVariable String username) {
        return rewardService.total(username);
    }
}
