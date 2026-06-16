package com.electrocorp.electrocorpplatform.billing.infrastructure.persistence.jpa.repositories;

import com.electrocorp.electrocorpplatform.billing.domain.model.SubscriptionStatus;
import com.electrocorp.electrocorpplatform.billing.domain.model.aggregates.Invoice;
import com.electrocorp.electrocorpplatform.billing.domain.model.aggregates.Payment;
import com.electrocorp.electrocorpplatform.billing.domain.model.aggregates.Plan;
import com.electrocorp.electrocorpplatform.billing.domain.model.aggregates.Subscription;
import com.electrocorp.electrocorpplatform.billing.domain.model.commands.ProcessPaymentCommand;
import com.electrocorp.electrocorpplatform.billing.domain.model.commands.SubscribeCommand;
import com.electrocorp.electrocorpplatform.billing.domain.repositories.InvoiceRepository;
import com.electrocorp.electrocorpplatform.billing.domain.repositories.PaymentRepository;
import com.electrocorp.electrocorpplatform.billing.domain.repositories.PlanRepository;
import com.electrocorp.electrocorpplatform.billing.domain.repositories.SubscriptionRepository;
import com.electrocorp.electrocorpplatform.shared.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class BillingRepositoryPortPersistenceTests {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private JpaPlanRepository jpaPlanRepository;

    @Autowired
    private JpaSubscriptionRepository jpaSubscriptionRepository;

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private JpaInvoiceRepository jpaInvoiceRepository;

    @Test
    void repositoriesAreInjectedThroughDomainPortsAndJpaImplementations() {
        assertNotNull(planRepository);
        assertNotNull(subscriptionRepository);
        assertNotNull(paymentRepository);
        assertNotNull(invoiceRepository);
        assertNotNull(jpaPlanRepository);
        assertNotNull(jpaSubscriptionRepository);
        assertNotNull(jpaPaymentRepository);
        assertNotNull(jpaInvoiceRepository);
        assertInstanceOf(JpaPlanRepository.class, planRepository);
        assertInstanceOf(JpaSubscriptionRepository.class, subscriptionRepository);
        assertInstanceOf(JpaPaymentRepository.class, paymentRepository);
        assertInstanceOf(JpaInvoiceRepository.class, invoiceRepository);
    }

    @Test
    void planRepositoryFindsAllAndByCode() {
        Plan savedPlan = jpaPlanRepository.save(plan("PRO_TEST", "Professional Test", BigDecimal.valueOf(49.90)));

        assertFalse(planRepository.findAll().isEmpty());
        assertEquals(savedPlan.getId(), planRepository.findByCode("PRO_TEST").orElseThrow().getId());
    }

    @Test
    void subscriptionRepositoryPersistsAndFindsActiveByUser() {
        Long userId = 8101L;
        Plan savedPlan = jpaPlanRepository.save(plan("SUB_TEST", "Subscription Test", BigDecimal.valueOf(59.90)));
        Subscription activeSubscription = subscriptionRepository.save(new Subscription(new SubscribeCommand(userId, "SUB_TEST"), savedPlan));
        Subscription cancelledSubscription = new Subscription(new SubscribeCommand(userId, "SUB_TEST"), savedPlan);
        cancelledSubscription.cancel();
        subscriptionRepository.save(cancelledSubscription);

        assertNotNull(activeSubscription.getId());
        assertTrue(subscriptionRepository.findById(activeSubscription.getId()).isPresent());
        assertEquals(2, subscriptionRepository.findByUserId(userId).size());
        assertEquals(
                activeSubscription.getId(),
                subscriptionRepository.findFirstByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).orElseThrow().getId()
        );
    }

    @Test
    void paymentAndInvoiceRepositoriesPersistAndFindByUser() {
        Long userId = 8201L;
        Plan savedPlan = jpaPlanRepository.save(plan("PAY_TEST", "Payment Test", BigDecimal.valueOf(69.90)));
        Subscription subscription = subscriptionRepository.save(new Subscription(new SubscribeCommand(userId, "PAY_TEST"), savedPlan));
        Payment payment = paymentRepository.save(new Payment(new ProcessPaymentCommand(userId, subscription.getId()), subscription));
        Invoice invoice = invoiceRepository.save(new Invoice(payment));

        assertNotNull(payment.getId());
        assertNotNull(invoice.getId());
        assertEquals(1, paymentRepository.findByUserId(userId).size());
        assertEquals(1, invoiceRepository.findByUserId(userId).size());
    }

    private Plan plan(String code, String name, BigDecimal monthlyPrice) {
        Plan plan = new Plan();
        plan.setCode(code);
        plan.setName(name);
        plan.setMonthlyPrice(Money.of(monthlyPrice));
        plan.setMaxDevices(20);
        plan.setMaxRoutines(10);
        plan.setMaxAlerts(20);
        plan.setReportExportEnabled(true);
        return plan;
    }
}
