package com.electrocorp.electrocorpplatform.billing.domain.model.aggregates;

import com.electrocorp.electrocorpplatform.billing.domain.model.PaymentStatus;
import com.electrocorp.electrocorpplatform.billing.domain.model.commands.ProcessPaymentCommand;
import com.electrocorp.electrocorpplatform.billing.domain.model.commands.SubscribeCommand;
import com.electrocorp.electrocorpplatform.shared.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BillingAggregateTests {

    @Test
    void subscriptionCreatedFromCommandStartsActive() {
        Plan plan = planWithPrice();

        Subscription subscription = new Subscription(new SubscribeCommand(7L, "PRO"), plan);

        assertEquals(7L, subscription.getUserId());
        assertEquals(plan, subscription.getPlan());
        assertTrue(subscription.isActive());
        assertNotNull(subscription.getStartDate());
        assertNull(subscription.getEndDate());
    }

    @Test
    void paymentCreatedFromCommandRequiresActiveSubscriptionOwner() {
        Plan plan = planWithPrice();
        Subscription subscription = new Subscription(new SubscribeCommand(7L, "PRO"), plan);

        Payment payment = new Payment(new ProcessPaymentCommand(7L, 1L), subscription);

        assertEquals(PaymentStatus.APPROVED, payment.getStatus());
        assertEquals(plan.getMonthlyPrice(), payment.getAmount());
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(new ProcessPaymentCommand(9L, 1L), subscription));
    }

    private Plan planWithPrice() {
        Plan plan = new Plan();
        plan.setCode("PRO");
        plan.setName("Pro");
        plan.setMonthlyPrice(Money.of(BigDecimal.valueOf(49.90)));
        return plan;
    }
}