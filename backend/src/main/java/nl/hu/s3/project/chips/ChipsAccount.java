package nl.hu.s3.project.chips;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
/**
 * JPA entity that represents the current chip balance for a single user.
 * Balance mutations are exposed through methods to protect the integrity of the stored amount.
 */
public class ChipsAccount {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private Long amount;
    @CreationTimestamp
    private LocalDateTime creationDate;
    @UpdateTimestamp
    private LocalDateTime lastUpdate;

    protected ChipsAccount() {}

    public ChipsAccount(String username, Long amount) {
        this.username = username;
        this.amount = amount;
    }

    public void deposit(Long amount) {
        // Delegate the calculation to the centralized validation logic.
        this.amount = ChipsController.amountAfterDeposit(this.amount, amount);
    }

    public void withdraw(Long amount) {
        // The helper prevents invalid and overdrawn balances.
        this.amount = ChipsController.amountAfterWithdrawal(this.amount, amount);
    }

    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public Long getAmount() {
        return amount;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
}
