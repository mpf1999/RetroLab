package uoc.edu.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

// @Embeddable as this is in Console in estimatedValue, does not form a table
@Embeddable
public class Money {

    @Column(name = "estimated_value", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "estimated_currency", length = 3)
    private String currencyCode;

    // empty as HJPA needs to build the object
    protected Money() {}

    public Money(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }

        String normalizedCurrency = currencyCode.trim().toUpperCase();
        Currency.getInstance(normalizedCurrency);

        this.amount = amount;
        this.currencyCode = normalizedCurrency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Money money)) {
            return false;
        }

        return amount.compareTo(money.amount) == 0 && currencyCode.equals(money.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currencyCode);
    }
}