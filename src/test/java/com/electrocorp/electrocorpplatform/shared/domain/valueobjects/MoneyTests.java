package com.electrocorp.electrocorpplatform.shared.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTests {

    @Test
    void shouldCreateMoneyWithDefaultCurrency() {
        Money money = Money.of(BigDecimal.valueOf(12.345));

        assertEquals(BigDecimal.valueOf(12.35), money.getAmount());
        assertEquals("PEN", money.getCurrency());
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(BigDecimal.valueOf(-1)));
    }

    @Test
    void shouldAddOnlySameCurrency() {
        Money result = Money.of(BigDecimal.TEN).add(Money.of(BigDecimal.ONE));

        assertEquals(BigDecimal.valueOf(11.00).setScale(2), result.getAmount());
        assertThrows(IllegalArgumentException.class, () -> Money.of(BigDecimal.TEN, "PEN").add(Money.of(BigDecimal.ONE, "USD")));
    }
}