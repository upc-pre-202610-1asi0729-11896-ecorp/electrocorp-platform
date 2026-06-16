package com.electrocorp.electrocorpplatform.billing.interfaces.rest.resources;

import com.electrocorp.electrocorpplatform.billing.domain.model.aggregates.Subscription;

public record SubscriptionResource(
        Long id,
        Long userId,
        String planCode,
        String status,
        boolean active
) {
    public static SubscriptionResource from(Subscription subscription) {
        return new SubscriptionResource(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getPlan().getCode(),
                subscription.getStatus().name(),
                subscription.isActive()
        );
    }
}