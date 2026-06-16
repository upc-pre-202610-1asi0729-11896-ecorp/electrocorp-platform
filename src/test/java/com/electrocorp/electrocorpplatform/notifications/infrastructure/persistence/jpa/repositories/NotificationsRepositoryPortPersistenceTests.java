package com.electrocorp.electrocorpplatform.notifications.infrastructure.persistence.jpa.repositories;

import com.electrocorp.electrocorpplatform.devicecontrol.application.services.DeviceControlApplicationService;
import com.electrocorp.electrocorpplatform.energymonitoring.application.services.EnergyMonitoringApplicationService;
import com.electrocorp.electrocorpplatform.notifications.application.services.NotificationApplicationService;
import com.electrocorp.electrocorpplatform.notifications.domain.model.aggregates.Alert;
import com.electrocorp.electrocorpplatform.notifications.domain.model.aggregates.AlertRule;
import com.electrocorp.electrocorpplatform.notifications.domain.model.aggregates.AlertRuleProfile;
import com.electrocorp.electrocorpplatform.notifications.domain.model.aggregates.NotificationPreference;
import com.electrocorp.electrocorpplatform.notifications.domain.model.valueobjects.AlertEventType;
import com.electrocorp.electrocorpplatform.notifications.domain.model.valueobjects.AlertLevel;
import com.electrocorp.electrocorpplatform.notifications.domain.model.valueobjects.AlertSourceType;
import com.electrocorp.electrocorpplatform.notifications.domain.model.valueobjects.RuleEvaluatorType;
import com.electrocorp.electrocorpplatform.notifications.domain.model.valueobjects.RuleProfileMode;
import com.electrocorp.electrocorpplatform.notifications.domain.model.valueobjects.RuleScopeType;
import com.electrocorp.electrocorpplatform.notifications.domain.model.valueobjects.RuleSensitivity;
import com.electrocorp.electrocorpplatform.notifications.domain.repositories.AlertRepository;
import com.electrocorp.electrocorpplatform.notifications.domain.repositories.AlertRuleProfileRepository;
import com.electrocorp.electrocorpplatform.notifications.domain.repositories.AlertRuleRepository;
import com.electrocorp.electrocorpplatform.notifications.domain.repositories.NotificationPreferenceRepository;
import com.electrocorp.electrocorpplatform.notifications.domain.services.RuleEvaluationService;
import com.electrocorp.electrocorpplatform.reporting.application.services.PlatformInsightApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class NotificationsRepositoryPortPersistenceTests {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private AlertRuleProfileRepository alertRuleProfileRepository;

    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Autowired
    private JpaAlertRepository jpaAlertRepository;

    @Autowired
    private JpaAlertRuleRepository jpaAlertRuleRepository;

    @Autowired
    private JpaAlertRuleProfileRepository jpaAlertRuleProfileRepository;

    @Autowired
    private JpaNotificationPreferenceRepository jpaNotificationPreferenceRepository;

    @Test
    void repositoriesAreInjectedThroughDomainPortsAndJpaImplementations() {
        assertNotNull(alertRepository);
        assertNotNull(alertRuleRepository);
        assertNotNull(alertRuleProfileRepository);
        assertNotNull(notificationPreferenceRepository);
        assertNotNull(jpaAlertRepository);
        assertNotNull(jpaAlertRuleRepository);
        assertNotNull(jpaAlertRuleProfileRepository);
        assertNotNull(jpaNotificationPreferenceRepository);
        assertInstanceOf(JpaAlertRepository.class, alertRepository);
        assertInstanceOf(JpaAlertRuleRepository.class, alertRuleRepository);
        assertInstanceOf(JpaAlertRuleProfileRepository.class, alertRuleProfileRepository);
        assertInstanceOf(JpaNotificationPreferenceRepository.class, notificationPreferenceRepository);
    }

    @Test
    void alertRepositoryPersistsFindsActiveThreadAndDeletesByPortMethods() {
        Long userId = 5101L;
        String threadKey = "DEVICE:300:RULE_EVALUATION";
        Alert olderAlert = alertRepository.save(alert(userId, threadKey, true, LocalDateTime.now().minusMinutes(10)));
        Alert newerAlert = alertRepository.save(alert(userId, threadKey, true, LocalDateTime.now()));
        alertRepository.save(alert(5102L, threadKey, true, LocalDateTime.now().plusMinutes(1)));
        alertRepository.save(alert(userId, "DEVICE:301:RULE_EVALUATION", false, LocalDateTime.now().plusMinutes(2)));

        assertNotNull(olderAlert.getId());
        assertEquals(3, alertRepository.findByUserId(userId).size());
        assertEquals(2, alertRepository.findByUserIdAndActiveTrue(userId).size());
        assertTrue(alertRepository.findByIdAndUserId(newerAlert.getId(), userId).isPresent());
        assertEquals(
                newerAlert.getId(),
                alertRepository
                        .findFirstByUserIdAndThreadKeyAndActiveTrueOrderByLastTriggeredAtDesc(userId, threadKey)
                        .orElseThrow()
                        .getId()
        );

        alertRepository.delete(newerAlert);

        assertTrue(alertRepository.findByIdAndUserId(newerAlert.getId(), userId).isEmpty());
    }

    @Test
    void alertRuleRepositoryPersistsFindsEnabledAndDeletesByPortMethods() {
        Long userId = 5201L;
        AlertRule enabledRule = alertRuleRepository.save(alertRule(userId, true, "Potencia alta"));
        alertRuleRepository.save(alertRule(userId, false, "Potencia pausada"));
        alertRuleRepository.save(alertRule(5202L, true, "Otro usuario"));

        assertNotNull(enabledRule.getId());
        assertEquals(2, alertRuleRepository.findByUserId(userId).size());
        assertEquals(1, alertRuleRepository.findByUserIdAndEnabledTrue(userId).size());
        assertTrue(alertRuleRepository.findByIdAndUserId(enabledRule.getId(), userId).isPresent());

        alertRuleRepository.delete(enabledRule);

        assertTrue(alertRuleRepository.findByIdAndUserId(enabledRule.getId(), userId).isEmpty());
    }

    @Test
    void alertRuleProfileRepositoryPersistsAndFindsByPortMethods() {
        Long userId = 5301L;
        AlertRuleProfile savedProfile = alertRuleProfileRepository.save(alertRuleProfile(userId, "Perfil operativo"));
        alertRuleProfileRepository.save(alertRuleProfile(5302L, "Perfil externo"));

        assertNotNull(savedProfile.getId());
        assertEquals(1, alertRuleProfileRepository.findByUserId(userId).size());
        assertTrue(alertRuleProfileRepository.findByIdAndUserId(savedProfile.getId(), userId).isPresent());
        assertTrue(alertRuleProfileRepository.findByIdAndUserId(savedProfile.getId(), 5302L).isEmpty());
    }

    @Test
    void notificationPreferenceRepositoryPersistsAndFindsByPortMethods() {
        Long userId = 5401L;
        NotificationPreference savedPreference = notificationPreferenceRepository.save(notificationPreference(userId));

        assertNotNull(savedPreference.getId());
        assertTrue(notificationPreferenceRepository.findByUserId(userId).isPresent());
        assertTrue(notificationPreferenceRepository.findById(savedPreference.getId()).isPresent());
    }

    @Test
    void servicesKeepDomainRepositoryPorts() throws NoSuchFieldException {
        assertFieldType(NotificationApplicationService.class, "alertRepository", AlertRepository.class);
        assertFieldType(NotificationApplicationService.class, "alertRuleRepository", AlertRuleRepository.class);
        assertFieldType(NotificationApplicationService.class, "alertRuleProfileRepository", AlertRuleProfileRepository.class);
        assertFieldType(NotificationApplicationService.class, "preferenceRepository", NotificationPreferenceRepository.class);
        assertFieldType(RuleEvaluationService.class, "alertRuleRepository", AlertRuleRepository.class);
        assertFieldType(DeviceControlApplicationService.class, "alertRuleProfileRepository", AlertRuleProfileRepository.class);
        assertFieldType(DeviceControlApplicationService.class, "notificationPreferenceRepository", NotificationPreferenceRepository.class);
        assertFieldType(EnergyMonitoringApplicationService.class, "alertRepository", AlertRepository.class);
        assertFieldType(PlatformInsightApplicationService.class, "alertRepository", AlertRepository.class);
        assertFieldType(PlatformInsightApplicationService.class, "alertRuleRepository", AlertRuleRepository.class);
    }

    private void assertFieldType(Class<?> owner, String fieldName, Class<?> expectedType) throws NoSuchFieldException {
        Field field = owner.getDeclaredField(fieldName);

        assertEquals(expectedType, field.getType());
    }

    private Alert alert(Long userId, String threadKey, Boolean active, LocalDateTime lastTriggeredAt) {
        Alert alert = new Alert();
        alert.setUserId(userId);
        alert.setTitle("Consumo alto detectado");
        alert.setMessage("Se detecto consumo por encima del rango esperado.");
        alert.setLevel(AlertLevel.WARNING.name());
        alert.setSourceType(AlertSourceType.DEVICE);
        alert.setSourceId("300");
        alert.setSourceLabel("Televisor");
        alert.setEventType(AlertEventType.RULE_EVALUATION);
        alert.setThreadKey(threadKey);
        alert.setActive(active);
        alert.setResolved(false);
        alert.setReadStatus(false);
        alert.setLastTriggeredAt(lastTriggeredAt);
        alert.setFirstDetectedAt(lastTriggeredAt.minusMinutes(5));
        return alert;
    }

    private AlertRule alertRule(Long userId, Boolean enabled, String name) {
        AlertRule rule = new AlertRule();
        rule.setUserId(userId);
        rule.setName(name);
        rule.setMetric("ACTIVE_POWER");
        rule.setConditionType("GREATER_OR_EQUAL_THAN");
        rule.setThreshold(BigDecimal.valueOf(120));
        rule.setLevel(AlertLevel.WARNING);
        rule.setScopeType(RuleScopeType.GENERAL);
        rule.setEvaluatorType(RuleEvaluatorType.ACTIVE_POWER);
        rule.setWeight(10);
        rule.setProfileName("General");
        rule.setEnabled(enabled);
        return rule;
    }

    private AlertRuleProfile alertRuleProfile(Long userId, String name) {
        AlertRuleProfile profile = new AlertRuleProfile();
        profile.setUserId(userId);
        profile.setName(name);
        profile.setDescription("Perfil de clasificacion energetica.");
        profile.setScopeType(RuleScopeType.GENERAL);
        profile.setMode(RuleProfileMode.BALANCED);
        profile.setSensitivity(RuleSensitivity.NORMAL);
        profile.setActive(false);
        return profile;
    }

    private NotificationPreference notificationPreference(Long userId) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        return preference;
    }
}
