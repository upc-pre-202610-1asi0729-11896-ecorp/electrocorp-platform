package com.electrocorp.electrocorpplatform.notifications.interfaces.rest.controllers;

import com.electrocorp.electrocorpplatform.notifications.application.commandservices.NotificationCommandService;
import com.electrocorp.electrocorpplatform.notifications.application.queryservices.NotificationQueryService;
import com.electrocorp.electrocorpplatform.notifications.domain.model.commands.ActivateAlertRuleProfileCommand;
import com.electrocorp.electrocorpplatform.notifications.domain.model.commands.DismissAlertCommand;
import com.electrocorp.electrocorpplatform.notifications.domain.model.commands.MarkAlertAsReadCommand;
import com.electrocorp.electrocorpplatform.notifications.domain.model.commands.ResolveAlertCommand;
import com.electrocorp.electrocorpplatform.notifications.domain.model.commands.ToggleAlertRuleCommand;
import com.electrocorp.electrocorpplatform.notifications.domain.model.queries.EvaluateAlertRulesQuery;
import com.electrocorp.electrocorpplatform.notifications.domain.model.queries.GetAlertRulesByUserQuery;
import com.electrocorp.electrocorpplatform.notifications.domain.model.queries.GetAlertRuleProfilesByUserQuery;
import com.electrocorp.electrocorpplatform.notifications.domain.model.queries.GetAlertsByUserQuery;
import com.electrocorp.electrocorpplatform.notifications.domain.model.queries.GetNotificationPreferenceQuery;
import com.electrocorp.electrocorpplatform.notifications.interfaces.rest.resources.*;
import com.electrocorp.electrocorpplatform.notifications.interfaces.rest.transform.*;
import com.electrocorp.electrocorpplatform.shared.infrastructure.security.CurrentUserProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationCommandService commandService;
    private final NotificationQueryService queryService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/alerts")
    public List<AlertResource> getAlerts() {
        return queryService.handle(new GetAlertsByUserQuery(currentUserProvider.getCurrentUserId())).stream()
                .map(AlertResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @PostMapping("/alerts")
    public AlertResource createAlert(@Valid @RequestBody CreateAlertResource request) {
        var command = CreateAlertCommandFromResourceAssembler.toCommandFromResource(request, currentUserProvider.getCurrentUserId());
        var alert = commandService.handle(command);
        return AlertResourceFromEntityAssembler.toResourceFromEntity(alert);
    }

    @PatchMapping("/alerts/{alertId}/read")
    public AlertResource markAsRead(@PathVariable Long alertId) {
        var alert = commandService.handle(new MarkAlertAsReadCommand(currentUserProvider.getCurrentUserId(), alertId));
        return AlertResourceFromEntityAssembler.toResourceFromEntity(alert);
    }

    @PatchMapping("/alerts/{alertId}/dismiss")
    public AlertResource dismissAlert(
            @PathVariable Long alertId,
            @RequestParam(defaultValue = "10") Integer minutes
    ) {
        var alert = commandService.handle(new DismissAlertCommand(currentUserProvider.getCurrentUserId(), alertId, minutes));
        return AlertResourceFromEntityAssembler.toResourceFromEntity(alert);
    }

    @PatchMapping("/alerts/{alertId}/resolve")
    public AlertResource resolveAlert(@PathVariable Long alertId) {
        var alert = commandService.handle(new ResolveAlertCommand(currentUserProvider.getCurrentUserId(), alertId));
        return AlertResourceFromEntityAssembler.toResourceFromEntity(alert);
    }

    @DeleteMapping("/alerts/{alertId}")
    public void deleteAlert(@PathVariable Long alertId) {
        commandService.deleteAlert(currentUserProvider.getCurrentUserId(), alertId);
    }

    @GetMapping("/alerts/rules")
    public List<AlertRuleResource> getRules() {
        return queryService.handle(new GetAlertRulesByUserQuery(currentUserProvider.getCurrentUserId())).stream()
                .map(AlertRuleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @PostMapping("/alerts/rules")
    public AlertRuleResource createRule(@Valid @RequestBody CreateAlertRuleResource request) {
        var command = CreateAlertRuleCommandFromResourceAssembler.toCommandFromResource(request, currentUserProvider.getCurrentUserId());
        var rule = commandService.handle(command);
        return AlertRuleResourceFromEntityAssembler.toResourceFromEntity(rule);
    }

    @PatchMapping("/alerts/rules/{ruleId}/toggle")
    public AlertRuleResource toggleRule(@PathVariable Long ruleId) {
        var rule = commandService.handle(new ToggleAlertRuleCommand(currentUserProvider.getCurrentUserId(), ruleId));
        return AlertRuleResourceFromEntityAssembler.toResourceFromEntity(rule);
    }

    @DeleteMapping("/alerts/rules/{ruleId}")
    public void deleteRule(@PathVariable Long ruleId) {
        commandService.deleteRule(currentUserProvider.getCurrentUserId(), ruleId);
    }

    @GetMapping("/alerts/rule-profiles")
    public List<AlertRuleProfileResource> getRuleProfiles() {
        return queryService.handle(new GetAlertRuleProfilesByUserQuery(currentUserProvider.getCurrentUserId())).stream()
                .map(AlertRuleProfileResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @PostMapping("/alerts/rule-profiles")
    public AlertRuleProfileResource createRuleProfile(@Valid @RequestBody CreateAlertRuleProfileResource request) {
        var command = CreateAlertRuleProfileCommandFromResourceAssembler.toCommandFromResource(request, currentUserProvider.getCurrentUserId());
        var profile = commandService.handle(command);
        return AlertRuleProfileResourceFromEntityAssembler.toResourceFromEntity(profile);
    }

    @PatchMapping("/alerts/rule-profiles/{profileId}/activate")
    public AlertRuleProfileResource activateRuleProfile(@PathVariable Long profileId) {
        var profile = commandService.handle(new ActivateAlertRuleProfileCommand(currentUserProvider.getCurrentUserId(), profileId));
        return AlertRuleProfileResourceFromEntityAssembler.toResourceFromEntity(profile);
    }

    @PostMapping("/alerts/rules/evaluate")
    public RuleEvaluationResultResource evaluateRules(@Valid @RequestBody EvaluateAlertRulesResource request) {
        var result = queryService.handle(new EvaluateAlertRulesQuery(
                currentUserProvider.getCurrentUserId(),
                request.scopeType(),
                request.scopeId(),
                request.observedValue()
        ));
        return RuleEvaluationResultResource.from(result);
    }

    @GetMapping("/notifications/preferences")
    public NotificationPreferenceResource getPreference() {
        var preference = queryService.handle(new GetNotificationPreferenceQuery(currentUserProvider.getCurrentUserId()));
        return NotificationPreferenceResourceFromEntityAssembler.toResourceFromEntity(preference);
    }

    @PutMapping("/notifications/preferences")
    public NotificationPreferenceResource updatePreference(@Valid @RequestBody UpdateNotificationPreferenceResource request) {
        var command = UpdateNotificationPreferenceCommandFromResourceAssembler.toCommandFromResource(request, currentUserProvider.getCurrentUserId());
        var preference = commandService.handle(command);
        return NotificationPreferenceResourceFromEntityAssembler.toResourceFromEntity(preference);
    }
}
