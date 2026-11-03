package nl.hu.s3.project.chips;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/chips")
@Transactional
/**
 * REST controller that handles all chip-related operations.
 * It acts as the central entry point for retrieving and modifying a user's balance.
 */
public class ChipsController {
    private final ChipsRepository chips;

    public ChipsController(ChipsRepository chips) {
        this.chips = chips;
    }

    @GetMapping("/balance")
    public Balance showBalance(Authentication authentication) {
        // Obtain the authenticated username from the security context.
        String username = authentication.getName();

        // Find an existing balance or create a default one when necessary.
        ChipsAccount foundChips = chips.findOrCreate(username);
        Balance result = balance(foundChips);
        return result;
    }

    @PostMapping("/deposit")
    public Balance deposit(Authentication authentication, @Validated @RequestBody Amount request) {
        return depositChips(authentication.getName(), request.amount());
    }

    @PostMapping("/withdrawal")
    public Balance withdraw(Authentication authentication, @Validated @RequestBody Amount request) {
        return withdrawChips(authentication.getName(), request.amount());
    }

    // Other controllers use this controller as though it were an application service.
    public Balance depositChips(String username, Long amount) {
        // Retrieve the current chip balance.
        ChipsAccount found = chips.findOrCreate(username);

        // Apply the deposit and save the updated entity.
        found.deposit(amount);

        // Convert the entity to a safe API response object.
        Balance result = balance(found);
        return result;
    }

    public Balance withdrawChips(String username, Long amount) {
        // Retrieve the balance before attempting the withdrawal.
        ChipsAccount found = chips.findOrCreate(username);

        // The entity performs validation to ensure the balance cannot become negative.
        found.withdraw(amount);

        Balance result = balance(found);
        return result;
    }

    public static long amountAfterDeposit(long current, long amount) {
        // Defensive validation is performed even though the request DTO is also validated.
        if (amount < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot deposit a negative amount: " + amount);
        }
        long updatedAmount = current + amount;
        return updatedAmount;
    }

    public static long amountAfterWithdrawal(long current, long amount) {
        // Never allow negative transaction amounts.
        if (amount < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot withdraw a negative amount: " + amount);
        }
        if (current - amount < 0) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "Cannot withdraw " + amount + " chips: " + current + " chips remaining");
        }
        long updatedAmount = current - amount;
        return updatedAmount;
    }

    private static Balance balance(ChipsAccount chips) {
        // Map only the fields that should be exposed to clients.
        Balance response = new Balance(chips.getUsername(), chips.getLastUpdate(), chips.getAmount());
        return response;
    }

}
