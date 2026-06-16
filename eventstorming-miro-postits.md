# ElectroCorp EventStorming para Miro

Fuente: codigo backend actual en `electrocorp-platform/src/main/java/com/electrocorp/electrocorpplatform`.

## Leyenda de colores EventStorming

- Naranja: Domain Event, algo que ya ocurrio.
- Azul: Command, intencion/orden que dispara comportamiento.
- Amarillo: Aggregate, entidad raiz que decide y protege invariantes.
- Lila: Policy, regla de negocio, domain service o process manager.
- Verde: Query, read model, projection o recurso de lectura.
- Rosa: External system, adapter, scheduler, gateway o mecanismo externo.
- Gris: Repository, puerto de persistencia o unidad tecnica.
- Turquesa: Value Object, enum o published language.
- Blanco: Actor/persona.
- Rojo: Hotspot, riesgo, deuda o decision pendiente.
- Contenedor: Bounded Context.

## Actores y disparadores globales

- Blanco | Actor | Usuario autenticado
- Blanco | Actor | Miembro ElectroCorp
- Blanco | Actor | Administrador/operador de cuenta
- Rosa | Scheduler | EnergyReadingSchedulerService
- Rosa | Adapter | JwtTokenService
- Rosa | Adapter | PasswordEncoder
- Rosa | Adapter | ConsoleNotificationDeliveryAdapter
- Rosa | Adapter | ConsoleTicketNotificationAdapter
- Rosa | API | REST Controllers bajo `/api/v1`
- Gris | Shared Kernel | DomainEventPublisher
- Gris | Shared Kernel | SpringDomainEventPublisher
- Gris | Shared Kernel | UnitOfWork
- Turquesa | Shared VO | UserId, DeviceId, LocationId, RoomId, DateRange, Money, EnergyUsage, EmailAddress, PhoneNumber, Percentage, NonBlankText, UserPlatformSummary

## Bounded Context: IAM

### Agregados
- Amarillo | Aggregate | User
- Amarillo | Aggregate | AccessProfile

### Comandos
- Azul | Command | SignUpCommand(fullName, email, password)
- Azul | Command | SignInCommand(email, password)
- Azul | Command | SignOut implicit command
- Azul | Command | RecoverPasswordCommand(email)
- Azul | Command | UpdateProfileCommand(fullName, email)
- Azul | Command | DeleteAccountCommand(userId)

### Eventos
- Naranja | Event | UserRegisteredEvent(userId, email, occurredOn)

### Politicas y reglas
- Lila | Policy | EmailPolicyService.isAllowedEmail
- Lila | Policy | PasswordPolicyService.isValid
- Lila | Policy | UserProfilePolicyService.validateFullName
- Lila | Policy | UserProfilePolicyService.validateEmail
- Lila | Rule | No registrar email duplicado
- Lila | Rule | Crear AccessProfile MEMBER por defecto si no existe
- Lila | Rule | Solo usuario ACTIVE puede iniciar sesion
- Lila | Rule | Delete account desactiva User, no elimina fisicamente

### Queries / read models / resources
- Verde | Read Model | AuthenticationResult
- Verde | Resource | AuthResource
- Verde | Resource | UserResource
- Verde | Resource | SignUpResource
- Verde | Resource | SignInResource
- Verde | Resource | RecoverPasswordResource
- Verde | Resource | UpdateProfileResource
- Verde | Endpoint | POST /api/v1/auth/sign-up
- Verde | Endpoint | POST /api/v1/auth/sign-in
- Verde | Endpoint | POST /api/v1/auth/sign-out
- Verde | Endpoint | POST /api/v1/auth/recover-password
- Verde | Endpoint | GET /api/v1/auth/me
- Verde | Endpoint | GET /api/v1/users/me
- Verde | Endpoint | PUT /api/v1/users/me
- Verde | Endpoint | DELETE /api/v1/users/me
- Verde | Endpoint | GET /api/v1/users/{userId}/profile
- Verde | Endpoint | PUT /api/v1/users/{userId}/profile
- Verde | Endpoint | DELETE /api/v1/users/{userId}

### Repositorios / VO / estados
- Gris | Repository | UserRepository
- Gris | Repository | AccessProfileRepository
- Turquesa | Enum | AccountStatus: ACTIVE, INACTIVE, DELETED
- Turquesa | VO | AccessProfileName
- Turquesa | VO | PasswordHash
- Turquesa | VO | RoleName

### Flujo sugerido en el tablero
- Blanco Usuario -> Azul SignUpCommand -> Amarillo User -> Naranja UserRegisteredEvent.
- Blanco Usuario -> Azul SignInCommand -> Lila validacion password/estado -> Verde AuthenticationResult/JWT.
- Blanco Usuario -> Azul DeleteAccountCommand -> Amarillo User.deactivate -> estado INACTIVE.

## Bounded Context: Billing

### Agregados
- Amarillo | Aggregate | Plan(fields: code, name, monthlyPrice, maxDevices, maxRoutines, maxAlerts, reportExportEnabled)
- Amarillo | Aggregate | Subscription(fields: userId, plan, status, startDate, endDate)
- Amarillo | Aggregate | Payment(fields: userId, subscription, amount, status, paymentMethod, paidAt)
- Amarillo | Aggregate | Invoice(fields: userId, payment, invoiceNumber, totalAmount, issuedAt)

### Comandos
- Azul | Command | SubscribeCommand(userId, planCode)
- Azul | Command | CheckoutSubscriptionCommand(userId, planCode, holderName, cardNumber, expirationDate, cvv)
- Azul | Command | ProcessPaymentCommand(userId, subscriptionId)
- Azul | Command | CancelSubscriptionCommand(userId)

### Eventos
- Naranja | Event | SubscriptionActivatedEvent(userId, subscriptionId, planCode, occurredOn)
- Naranja | Event | PaymentRegisteredEvent(userId, paymentId, amount, occurredOn)

### Politicas y reglas
- Lila | Policy | PaymentValidationService.isValidHolderName
- Lila | Policy | PaymentValidationService.isValidCardNumber
- Lila | Policy | PaymentValidationService.isValidExpirationDate
- Lila | Policy | PaymentValidationService.isValidCvv
- Lila | Policy | BillingAmountPolicyService.validatePositiveAmount
- Lila | Policy | BillingAmountPolicyService.validateCurrency
- Lila | Policy | InvoiceGenerationService.generateInvoice
- Lila | Policy | InvoiceGenerationService.generateInvoiceNumber
- Lila | Policy | PlanLimitPolicyService.canCreateDevice
- Lila | Policy | PlanLimitPolicyService.canCreateRoutine
- Lila | Policy | PlanLimitPolicyService.canCreateAlert
- Lila | Policy | PlanLimitPolicyService.canExportReports
- Lila | Policy | SubscriptionPolicyService.hasActiveSubscription
- Lila | Rule | Checkout cancela suscripcion ACTIVE previa del usuario antes de crear una nueva
- Lila | Rule | Checkout crea Subscription, Payment e Invoice en una transaccion
- Lila | Rule | ProcessPayment crea Payment e Invoice para una Subscription existente
- Lila | Rule | CancelSubscription requiere Subscription ACTIVE

### Queries / read models / resources
- Verde | Query | GetAllPlansQuery()
- Verde | Query | GetCurrentSubscriptionQuery(userId)
- Verde | Query | GetPaymentsByUserQuery(userId)
- Verde | Query | GetInvoicesByUserQuery(userId)
- Verde | Resource | PlanResource
- Verde | Resource | SubscriptionResource
- Verde | Resource | PaymentResource
- Verde | Resource | InvoiceResource
- Verde | Resource | SubscribeResource
- Verde | Resource | CheckoutSubscriptionResource
- Verde | Resource | ProcessPaymentResource
- Verde | Endpoint | GET /api/v1/billing/plans
- Verde | Endpoint | GET /api/v1/billing/subscriptions/current
- Verde | Endpoint | POST /api/v1/billing/subscriptions
- Verde | Endpoint | POST /api/v1/billing/subscriptions/checkout
- Verde | Endpoint | DELETE /api/v1/billing/subscriptions/current
- Verde | Endpoint | POST /api/v1/billing/payments
- Verde | Endpoint | GET /api/v1/billing/payments
- Verde | Endpoint | GET /api/v1/billing/invoices

### Repositorios / VO / estados
- Gris | Repository | PlanRepository
- Gris | Repository | SubscriptionRepository
- Gris | Repository | PaymentRepository
- Gris | Repository | InvoiceRepository
- Turquesa | Enum | SubscriptionStatus: ACTIVE, CANCELLED, EXPIRED
- Turquesa | Enum | PaymentStatus: PENDING, APPROVED, REJECTED
- Turquesa | VO | PlanName
- Turquesa | VO | SubscriptionId
- Turquesa | VO | PaymentAmount
- Turquesa | VO | InvoiceNumber
- Turquesa | VO | BillingPeriod

### Flujo sugerido en el tablero
- Blanco Usuario -> Azul CheckoutSubscriptionCommand -> Lila PaymentValidationService -> Amarillo Plan/Subscription/Payment/Invoice -> Naranja SubscriptionActivatedEvent / PaymentRegisteredEvent.
- Blanco Usuario -> Azul CancelSubscriptionCommand -> Amarillo Subscription.cancel -> estado CANCELLED.

## Bounded Context: Workplace

### Agregados
- Amarillo | Aggregate | Location(fields: userId, name, address, type)
- Amarillo | Aggregate | Room(fields: locationId, name, floor)
- Amarillo | Aggregate | DeviceAssignment(fields: deviceId, roomId, locationId, assignedAt)

### Comandos
- Azul | Command | CreateLocationCommand(userId, name, address, type)
- Azul | Command | UpdateLocationCommand(userId, locationId, name, address, type)
- Azul | Command | DeleteLocationCommand(userId, locationId)
- Azul | Command | CreateRoomCommand(locationId, name, floor)
- Azul | Command | UpdateRoomCommand(roomId, locationId, name, floor)
- Azul | Command | DeleteRoomCommand(roomId, locationId)
- Azul | Command | AssignDeviceCommand(deviceId, roomId, locationId)
- Azul | Command | MoveDeviceAssignmentResource(locationId, roomId)
- Azul | Command | RemoveDeviceAssignmentCommand(assignmentId)

### Eventos
- Naranja | Event | DeviceAssignedToRoomEvent(userId, deviceId, roomId, occurredOn)

### Politicas y reglas
- Lila | Policy | LocationPolicyService.validateLocation
- Lila | Policy | LocationPolicyService.normalizeType
- Lila | Policy | RoomPolicyService.validateRoom
- Lila | Policy | RoomPolicyService.normalizeFloor
- Lila | Policy | DeviceAssignmentPolicyService.validateAssignment
- Lila | Policy | DeviceAssignmentPolicyService.canAssignDevice
- Lila | Policy | WorkplacePolicyService.canCreateLocation
- Lila | Policy | WorkplacePolicyService.canCreateRoom
- Lila | Policy | WorkplacePolicyService.canAssignDevice
- Lila | Policy | WorkplaceSummaryService.countLocations/countRooms/countAssignments
- Lila | Rule | Location debe pertenecer al usuario actual para leer, editar o borrar
- Lila | Rule | Room debe pertenecer a la Location seleccionada
- Lila | Rule | Device debe existir en DeviceControl y pertenecer al usuario antes de asignarse
- Lila | Rule | Device solo puede tener una asignacion activa
- Lila | Rule | Al borrar Location se eliminan assignments de esa Location
- Lila | Rule | Al borrar Room se desasignan los assignments de ese Room

### Queries / read models / resources
- Verde | Read | getLocations(userId)
- Verde | Read | getRooms(userId, locationId)
- Verde | Read | getRoomsByUserId(userId)
- Verde | Read | getAssignments(userId, locationId)
- Verde | Read | getAssignmentsByUserId(userId)
- Verde | Read | getAssignmentsByDeviceId(userId, deviceId)
- Verde | Resource | LocationResource
- Verde | Resource | RoomResource
- Verde | Resource | DeviceAssignmentResource
- Verde | Resource | CreateLocationResource
- Verde | Resource | CreateRoomResource
- Verde | Resource | AssignDeviceResource
- Verde | Resource | UpdateLocationResource
- Verde | Resource | UpdateRoomResource
- Verde | Endpoint | GET /api/v1/workplace/locations
- Verde | Endpoint | POST /api/v1/workplace/locations
- Verde | Endpoint | GET /api/v1/workplace/rooms
- Verde | Endpoint | POST /api/v1/workplace/rooms
- Verde | Endpoint | GET /api/v1/workplace/device-assignments
- Verde | Endpoint | POST /api/v1/workplace/device-assignments
- Verde | Endpoint | PATCH /api/v1/workplace/locations/{locationId}
- Verde | Endpoint | DELETE /api/v1/workplace/locations/{locationId}
- Verde | Endpoint | PATCH /api/v1/workplace/rooms/{roomId}
- Verde | Endpoint | DELETE /api/v1/workplace/rooms/{roomId}
- Verde | Endpoint | PATCH /api/v1/workplace/device-assignments/{assignmentId}
- Verde | Endpoint | DELETE /api/v1/workplace/device-assignments/{assignmentId}

### Repositorios / VO
- Gris | Repository | LocationRepository
- Gris | Repository | RoomRepository
- Gris | Repository | DeviceAssignmentRepository
- Turquesa | VO | LocationName
- Turquesa | VO | RoomName
- Turquesa | VO | Address
- Turquesa | VO | Capacity

### Flujo sugerido en el tablero
- Blanco Usuario -> Azul CreateLocationCommand -> Amarillo Location.
- Blanco Usuario -> Azul CreateRoomCommand -> Lila validar Location del usuario -> Amarillo Room.
- Blanco Usuario -> Azul AssignDeviceCommand -> Lila validar Device, Location y Room -> Amarillo DeviceAssignment -> Naranja DeviceAssignedToRoomEvent.

## Bounded Context: DeviceControl

### Agregados y entidades
- Amarillo | Aggregate | Device(fields: userId, name, room, type, powerWatts, status)
- Amarillo | Aggregate | DeviceGroup(fields: userId, name, description)
- Amarillo | Aggregate | Routine(fields: userId, deviceId, groupId, targetType, targetId, name, action, time, repeatType, daysOfWeek, intervalDays, startsOn, enabled)
- Amarillo | Aggregate | OperationMode(fields: userId, locationId, roomIds, groupIds, deviceIds, turnOnDeviceIds, turnOffDeviceIds, keepOnDeviceIds, routineIds, goalIds, ruleProfileId, preferenceId, status)
- Amarillo | Entity | DeviceGroupDevice(deviceGroupId, deviceId)
- Amarillo | Entity | OperationModeRoutine(name, targetType, targetId, action, triggerTime, enabled)

### Comandos
- Azul | Command | CreateDeviceCommand(userId, name, room, type, powerWatts)
- Azul | Command | UpdateDeviceCommand(userId, deviceId, name, room, type, powerWatts)
- Azul | Command | ToggleDeviceCommand(userId, deviceId)
- Azul | Command | UpdateDeviceStatusCommand(status)
- Azul | Command | DeleteDeviceCommand(userId, deviceId)
- Azul | Command | CreateRoutineCommand(userId, deviceId, groupId, targetType, targetId, name, action, time, repeatType, daysOfWeek, intervalDays, startsOn)
- Azul | Command | UpdateRoutineStatusCommand(enabled)
- Azul | Command | ToggleRoutineCommand(userId, routineId)
- Azul | Command | ExecuteRoutineCommand(userId, routineId)
- Azul | Command | DeleteRoutineCommand(userId, routineId)
- Azul | Command | CreateDeviceGroupCommand(userId, name, description, deviceIds)
- Azul | Command | UpdateDeviceGroupCommand(name, description, deviceIds)
- Azul | Command | ExecuteGroupActionCommand(status)
- Azul | Command | DeleteDeviceGroupCommand(userId, deviceGroupId)
- Azul | Command | AddDeviceToGroupCommand(userId, deviceGroupId, deviceId)
- Azul | Command | RemoveDeviceFromGroupCommand(userId, deviceGroupId, deviceId)
- Azul | Command | CreateOperationModeCommand(userId, locationId, name, description, roomIds, groupIds, deviceIds, turnOnDeviceIds, turnOffDeviceIds, keepOnDeviceIds, routineIds, routinesToEnableIds, routinesToDisableIds, goalIds, internalRoutines, allDay, startsAt, endsAt, ruleProfileId, preferenceId, applyRuleProfile, applyNotificationPreference, applyRoutines, preserveCriticalSound)
- Azul | Command | ActivateOperationModeCommand(userId, modeId)
- Azul | Command | ArchiveOperationModeCommand(userId, modeId)
- Azul | Command | OperationModeRoutineCommand(name, targetType, targetId, action, triggerTime, enabled)

### Eventos
- Naranja | Event | DeviceCreatedEvent(userId, deviceId, deviceName, occurredOn)
- Naranja | Event | OperationModeActivatedEvent(userId, modeId, modeName, evidence, explanation, recommendedAction, occurredOn)

### Politicas y reglas
- Lila | Policy | DevicePolicyService.validateDeviceData
- Lila | Policy | DevicePolicyService.canToggleDevice
- Lila | Policy | DevicePowerPolicyService.isHighPowerDevice
- Lila | Policy | DevicePowerPolicyService.resolvePowerCategory
- Lila | Policy | DeviceGroupPolicyService.canCreateGroup
- Lila | Policy | RoutinePolicyService.validateRoutineData
- Lila | Policy | RoutineConflictCheckerService.hasConflict
- Lila | Rule | Dispositivos REMOVED y MAINTENANCE no reciben cambios operativos normales
- Lila | Rule | Toggle/StatusTransition llama EnergyReadingRecorderService para abrir/cerrar o registrar consumo
- Lila | Rule | DeleteDevice marca REMOVED, elimina relaciones DeviceGroupDevice y elimina rutinas directas del dispositivo
- Lila | Rule | DeviceGroup requiere dispositivos existentes, no removidos, no en mantenimiento, asignados a workplace y room
- Lila | Rule | DeviceGroup no puede mezclar dispositivos de diferentes workplaces
- Lila | Rule | DeviceGroup no puede mezclar dispositivos de diferentes rooms
- Lila | Rule | Routine target puede ser DEVICE, GROUP, ROOM o WORKPLACE
- Lila | Rule | ExecuteRoutine aplica TURN_ON/TURN_OFF al target y registra transicion energetica
- Lila | Rule | OperationMode requiere Location del usuario
- Lila | Rule | OperationMode puede ser General o de un solo Room
- Lila | Rule | OperationMode valida Devices, Groups, Routines, Goals, RuleProfile y Preference antes de crearse
- Lila | Rule | OperationMode room-scoped no acepta metas/rutinas mas amplias que el Room
- Lila | Rule | Activar OperationMode bloquea modos de la sede, desactiva otros activos de la misma Location y publica evento
- Lila | Rule | Activar OperationMode enciende/apaga dispositivos, habilita/deshabilita rutinas y genera preview/evidence

### Queries / read models / resources
- Verde | Query | GetDevicesQuery(userId)
- Verde | Query | GetRoutinesQuery(userId)
- Verde | Query | GetDeviceGroupsQuery(userId)
- Verde | Query | GetOperationModesQuery(userId)
- Verde | Query | PreviewOperationModeQuery(userId, modeId)
- Verde | Read Model | DeviceGroupDetails(group, deviceIds)
- Verde | Read Model | RoutineDetails
- Verde | Read Model | OperationModePreviewResult
- Verde | Read Model | OperationModeActivationResult
- Verde | Resource | DeviceResource
- Verde | Resource | RoutineResource
- Verde | Resource | DeviceGroupResource
- Verde | Resource | OperationModeResource
- Verde | Resource | OperationModePreviewResource
- Verde | Resource | OperationModeActivationResource
- Verde | Endpoint | GET /api/v1/devices
- Verde | Endpoint | POST /api/v1/devices
- Verde | Endpoint | PATCH /api/v1/devices/{deviceId}/toggle
- Verde | Endpoint | PATCH /api/v1/devices/{deviceId}/status
- Verde | Endpoint | DELETE /api/v1/devices/{deviceId}
- Verde | Endpoint | GET /api/v1/routines
- Verde | Endpoint | POST /api/v1/routines
- Verde | Endpoint | PATCH /api/v1/routines/{routineId}/status
- Verde | Endpoint | PATCH /api/v1/routines/{routineId}/execute
- Verde | Endpoint | DELETE /api/v1/routines/{routineId}
- Verde | Endpoint | GET /api/v1/device-groups
- Verde | Endpoint | POST /api/v1/device-groups
- Verde | Endpoint | PATCH /api/v1/device-groups/{groupId}
- Verde | Endpoint | PATCH /api/v1/device-groups/{groupId}/execute
- Verde | Endpoint | DELETE /api/v1/device-groups/{groupId}
- Verde | Endpoint | GET /api/v1/operation-modes
- Verde | Endpoint | POST /api/v1/operation-modes
- Verde | Endpoint | GET /api/v1/operation-modes/{modeId}/preview
- Verde | Endpoint | PATCH /api/v1/operation-modes/{modeId}/activate
- Verde | Endpoint | DELETE /api/v1/operation-modes/{modeId}

### Repositorios / VO / estados
- Gris | Repository | DeviceRepository
- Gris | Repository | RoutineRepository
- Gris | Repository | DeviceGroupRepository
- Gris | Repository | DeviceGroupDeviceRepository
- Gris | Repository | OperationModeRepository
- Turquesa | Enum | DeviceStatus: ON, OFF, MAINTENANCE, REMOVED
- Turquesa | Enum | RoutineAction: TURN_ON, TURN_OFF
- Turquesa | Enum | RoutineTargetType: DEVICE, GROUP, ROOM, WORKPLACE
- Turquesa | Enum | RoutineRepeatType: ONCE, DAILY, WEEKLY, CUSTOM_INTERVAL
- Turquesa | Enum | OperationModeStatus: DRAFT, ACTIVE, INACTIVE, ARCHIVED
- Turquesa | VO | DeviceName
- Turquesa | VO | DeviceSerialNumber
- Turquesa | VO | DeviceGroupName
- Turquesa | VO | RoutineName
- Turquesa | VO | PowerWatts

### Flujo sugerido en el tablero
- Blanco Usuario -> Azul ToggleDeviceCommand -> Amarillo Device.toggle -> Rosa EnergyReadingRecorderService -> Naranja consumo registrado.
- Blanco Usuario -> Azul CreateDeviceGroupCommand -> Lila validar assignments en Workplace -> Amarillo DeviceGroup + DeviceGroupDevice.
- Blanco Usuario -> Azul ActivateOperationModeCommand -> Amarillo OperationMode.activate -> Naranja OperationModeActivatedEvent -> Bounded Context Notifications.

## Bounded Context: EnergyMonitoring

### Agregados y estado operacional
- Amarillo | Aggregate | EnergyReading(fields: userId, deviceId, deviceName, recordedAt, watts, kilowattHours, estimatedCost, sampleSeconds, status)
- Amarillo | Entity/State | EnergyDeviceUsageState(deviceId, startedAt, lastRecordedAt)

### Comandos
- Azul | Command | CreateEnergyReadingCommand(userId, deviceId, deviceName, watts)
- Azul | Command | UpdateEnergySamplingSettingsCommand(sampleSeconds)
- Azul | Command | FilterEnergyReadingsCommand(userId, deviceId, startDate, endDate)
- Azul | Command | GetEnergyStatisticsCommand(userId, startDate, endDate)
- Azul | Command | ExportEnergyReadingsCommand(userId, startDate, endDate)

### Eventos
- Naranja | Event | EnergyThresholdExceededEvent(userId, deviceId, watts, occurredOn)
- Naranja | Event | EnergyReading recorded from active device interval
- Naranja | Event | Device usage state opened
- Naranja | Event | Device usage state closed

### Politicas y reglas
- Lila | Policy | EnergyConsumptionSimulatorService.simulateWatts
- Lila | Policy | EnergyReadingPolicyService.validateReading
- Lila | Policy | EnergyReadingPolicyService.isHighConsumption
- Lila | Policy | EnergyReadingPolicyService.isLowConsumption
- Lila | Policy | EnergyReadingFilterService.filterByDevice
- Lila | Policy | EnergyReadingFilterService.filterByDateRange
- Lila | Policy | EnergyStatisticsService.calculateTotalWatts
- Lila | Policy | EnergyStatisticsService.calculateAverageWatts
- Lila | Policy | EnergyUsageClassifierService.classify
- Lila | Policy | EnergyRecommendationService.resolveRecommendation
- Lila | Policy | HighConsumptionSpecification
- Lila | Rule | Solo dispositivos ON generan intervalos activos
- Lila | Rule | SamplingSettings normaliza sampleSeconds entre 5 y 3600
- Lila | Rule | recordActiveDeviceIntervalIfDue espera minimo sampleSeconds
- Lila | Rule | Cada intervalo calcula watts, kWh y costo estimado con tarifa 0.75
- Lila | Rule | Cada EnergyReading creada puede disparar evaluacion de reglas en Notifications
- Lila | Rule | DashboardSummary actualiza intervalos vencidos antes de calcular metricas

### Queries / read models / resources
- Verde | Query | GetEnergyReadingsByUserQuery(userId, start, end)
- Verde | Query | GetEnergyDashboardSummaryQuery(userId)
- Verde | Query | GetEnergySamplingSettingsQuery()
- Verde | Read Model | EnergyDashboardSummaryResource
- Verde | Resource | EnergyReadingResource
- Verde | Resource | EnergySamplingSettingsResource
- Verde | Resource | CreateEnergyReadingResource
- Verde | Resource | UpdateEnergySamplingSettingsResource
- Verde | Endpoint | GET /api/v1/energy-readings
- Verde | Endpoint | GET /api/v1/energy-readings/dashboard-summary
- Verde | Endpoint | GET /api/v1/energy-readings/sampling-settings
- Verde | Endpoint | PATCH /api/v1/energy-readings/sampling-settings
- Verde | Endpoint | POST /api/v1/energy-readings

### Repositorios / VO / estados / adapters
- Gris | Repository | EnergyReadingRepository
- Gris | Repository | EnergyDeviceUsageStateRepository
- Rosa | Scheduler | EnergyReadingSchedulerService.configureTasks
- Rosa | Exporter | EnergyReadingsCsvExporter
- Turquesa | Enum | EnergyReadingStatus: NORMAL, HIGH
- Turquesa | VO | Watts
- Turquesa | VO | KilowattHours
- Turquesa | VO | EnergyCost
- Turquesa | VO | EnergyThreshold
- Turquesa | VO | SampleSeconds

### Flujo sugerido en el tablero
- Rosa Scheduler -> Azul recordActiveDevicesConsumption -> Amarillo EnergyDeviceUsageState -> Amarillo EnergyReading -> Naranja EnergyReading recorded.
- Amarillo EnergyReading -> Lila RuleEvaluationService en Notifications -> Azul CreateAlertCommand si aplica.
- Blanco Usuario -> Verde DashboardSummary -> Lila recalcular intervalos -> Verde EnergyDashboardSummaryResource.

## Bounded Context: Notifications

### Agregados
- Amarillo | Aggregate | Alert(fields: userId, title, message, level, sourceType, sourceId, sourceLabel, eventType, threadKey, evidence, explanation, recommendedAction, severityScore, repeatCount, active, resolved, readStatus, firstDetectedAt, lastTriggeredAt, dismissedUntil, expiresAt)
- Amarillo | Aggregate | AlertRule(fields: userId, name, metric, conditionType, threshold, level, scopeType, scopeId, evaluatorType, weight, profileName, enabled)
- Amarillo | Aggregate | AlertRuleProfile(fields: userId, name, description, scopeType, scopeId, mode, sensitivity, active)
- Amarillo | Aggregate | NotificationPreference(fields: userId, scopeType, scopeId, emailEnabled, pushEnabled, inAppEnabled, toastEnabled, dashboardEnabled, criticalOnly, minimumLevel, allowedLevels, allowedSourceTypes, quietHours, criticalBreaksQuietHours, groupSimilarAlerts, remindersEnabled, cooldownMinutes, maxAlertsPerHour, routineNightSilence, goalDeadlineAlerts, maintenanceDeviceAlerts, systemRecommendations, dailySummaryEnabled, weeklySummaryEnabled, defaultDeliveryMode)

### Comandos
- Azul | Command | CreateAlertCommand(userId, title, message, level, sourceType, sourceId, sourceLabel, eventType, threadKey, evidence, explanation, recommendedAction, severityScore, expiresAt)
- Azul | Command | MarkAlertAsReadCommand(userId, alertId)
- Azul | Command | DismissAlertCommand(userId, alertId, minutes)
- Azul | Command | ResolveAlertCommand(userId, alertId)
- Azul | Command | CreateAlertRuleCommand(userId, name, metric, conditionType, threshold, level, scopeType, scopeId, evaluatorType, weight, profileName)
- Azul | Command | ToggleAlertRuleCommand(userId, ruleId)
- Azul | Command | CreateAlertRuleProfileCommand(userId, name, description, scopeType, scopeId, mode, sensitivity)
- Azul | Command | ActivateAlertRuleProfileCommand(userId, profileId)
- Azul | Command | UpdateNotificationPreferenceCommand(userId, emailEnabled, pushEnabled, criticalOnly, minimumLevel, scopeType, scopeId, inAppEnabled, toastEnabled, dashboardEnabled, allowedLevels, allowedSourceTypes, quietHours, criticalBreaksQuietHours, groupSimilarAlerts, remindersEnabled, cooldownMinutes, maxAlertsPerHour, routineNightSilence, goalDeadlineAlerts, maintenanceDeviceAlerts, systemRecommendations, dailySummaryEnabled, weeklySummaryEnabled, defaultDeliveryMode)

### Eventos
- Naranja | Event | AlertCreatedEvent(userId, alertId, severity, occurredOn)
- Naranja | Event | Alert refreshed by threadKey
- Naranja | Event | Alert marked as read
- Naranja | Event | Alert dismissed until time
- Naranja | Event | Alert resolved
- Naranja | Event | Alert expired
- Naranja | Event | AlertRule toggled
- Naranja | Event | AlertRuleProfile activated
- Naranja | Event | NotificationPreference updated

### Politicas y reglas
- Lila | Policy | AlertPolicyService.validateAlert
- Lila | Policy | AlertPolicyService.isValidLevel
- Lila | Policy | AlertPolicyService.isCritical
- Lila | Policy | AlertPolicyService.parseLevel
- Lila | Policy | AlertPolicyService.parseSourceType
- Lila | Policy | AlertPolicyService.parseEventType
- Lila | Policy | AlertPolicyService.normalizeScore
- Lila | Policy | AlertSourceOwnershipService.validateSourceOwnership
- Lila | Policy | RuleEvaluationService.evaluate
- Lila | Policy | RuleSeverityPolicyService.classify
- Lila | Policy | RuleSeverityPolicyService.recommendedAction
- Lila | Policy | NotificationPreferencePolicyService.validatePreference
- Lila | Policy | NotificationPreferencePolicyService.normalizeLevelSet
- Lila | Policy | NotificationPreferencePolicyService.normalizeSourceSet
- Lila | Policy | NotificationPreferencePolicyService.allowsLevel
- Lila | Policy | NotificationDeliveryPolicyService.decide
- Lila | Policy | NotificationChannelPolicyService.canSendEmail/canSendPush
- Lila | Policy | AlertRulePolicyService.validateRule/evaluate
- Lila | Policy | AlertSummaryService.countUnread/countCritical
- Lila | Rule | CreateAlert valida source ownership contra Device, Group, Routine, Workplace, Room o Goal segun sourceType
- Lila | Rule | SYSTEM, RULE, REPORT y MODE pueden omitir ownership numerico
- Lila | Rule | Alert threadKey evita duplicados: refresca alerta activa existente
- Lila | Rule | DismissAlert limita minutos entre 1 y 1440
- Lila | Rule | Activar RuleProfile desactiva los demas perfiles del usuario
- Lila | Rule | EvaluateRules suma pesos de reglas activas aplicables y clasifica severity
- Lila | Rule | createAlertFromRuleEvaluation crea alerta automatica solo si hay evaluadores activos
- Lila | Rule | QuietHours puede silenciar segun preferencia salvo criticalBreaksQuietHours

### Queries / read models / resources
- Verde | Query | GetAlertsByUserQuery(userId)
- Verde | Query | GetAlertRulesByUserQuery(userId)
- Verde | Query | GetAlertRuleProfilesByUserQuery(userId)
- Verde | Query | EvaluateAlertRulesQuery(userId, scopeType, scopeId, observedValue)
- Verde | Query | GetNotificationPreferenceQuery(userId)
- Verde | Read Model | RuleEvaluationResult
- Verde | Read Model | NotificationDeliveryDecision
- Verde | Resource | AlertResource
- Verde | Resource | AlertRuleResource
- Verde | Resource | AlertRuleProfileResource
- Verde | Resource | NotificationPreferenceResource
- Verde | Resource | RuleEvaluationResultResource
- Verde | Endpoint | GET /api/v1/alerts
- Verde | Endpoint | POST /api/v1/alerts
- Verde | Endpoint | PATCH /api/v1/alerts/{alertId}/read
- Verde | Endpoint | PATCH /api/v1/alerts/{alertId}/dismiss
- Verde | Endpoint | PATCH /api/v1/alerts/{alertId}/resolve
- Verde | Endpoint | DELETE /api/v1/alerts/{alertId}
- Verde | Endpoint | GET /api/v1/alerts/rules
- Verde | Endpoint | POST /api/v1/alerts/rules
- Verde | Endpoint | PATCH /api/v1/alerts/rules/{ruleId}/toggle
- Verde | Endpoint | DELETE /api/v1/alerts/rules/{ruleId}
- Verde | Endpoint | GET /api/v1/alerts/rule-profiles
- Verde | Endpoint | POST /api/v1/alerts/rule-profiles
- Verde | Endpoint | PATCH /api/v1/alerts/rule-profiles/{profileId}/activate
- Verde | Endpoint | POST /api/v1/alerts/rules/evaluate
- Verde | Endpoint | GET /api/v1/notifications/preferences
- Verde | Endpoint | PUT /api/v1/notifications/preferences

### Repositorios / VO / estados / adapters
- Gris | Repository | AlertRepository
- Gris | Repository | AlertRuleRepository
- Gris | Repository | AlertRuleProfileRepository
- Gris | Repository | NotificationPreferenceRepository
- Rosa | Adapter | NotificationDeliveryPort
- Rosa | Adapter | ConsoleNotificationDeliveryAdapter
- Turquesa | Enum | AlertLevel: STABLE, INFO, WARNING, CRITICAL, SUCCESS
- Turquesa | Enum | AlertSourceType: DEVICE, GROUP, ROOM, WORKPLACE, ROUTINE, GOAL, REPORT, SYSTEM, RULE, MODE
- Turquesa | Enum | AlertEventType: MANUAL, CONSUMPTION_REVIEW, DEVICE_STATUS, GROUP_STATUS, ROOM_ACTIVITY, WORKPLACE_ACTIVITY, ROUTINE_UPCOMING, ROUTINE_EXECUTED, ROUTINE_FAILED, ROUTINE_DISABLED, GOAL_PROGRESS, GOAL_DEADLINE, GOAL_EXCEEDED, GOAL_COMPLETED, REPORT_SUMMARY, RULE_EVALUATION, SYSTEM_STATUS, MODE_ACTIVITY
- Turquesa | Enum | RuleScopeType: GENERAL, WORKPLACE, ROOM, DEVICE, GROUP, ROUTINE, GOAL
- Turquesa | Enum | RuleEvaluatorType: DAILY_KWH, AVERAGE_WATTS, ACTIVE_POWER, HIGH_READING_COUNT, SUSTAINED_CONSUMPTION, DEVICE_COUNT, GOAL_USAGE, GOAL_DEADLINE, ROUTINE_CONTEXT, COST_ESTIMATE, CONFIGURATION_COVERAGE
- Turquesa | Enum | RuleProfileMode: BALANCED, SAVINGS, PROTECTION
- Turquesa | Enum | RuleSensitivity: LOW, NORMAL, HIGH, STRICT
- Turquesa | Enum | NotificationPreferenceScope: USER, WORKPLACE
- Turquesa | Enum | NotificationDeliveryMode: BANNER, QUIET, INBOX_ONLY, MUTED
- Turquesa | VO | AlertTitle
- Turquesa | VO | AlertMessage
- Turquesa | VO | AlertSeverity
- Turquesa | VO | NotificationChannel
- Turquesa | VO | QuietHours

### Flujo sugerido en el tablero
- Naranja OperationModeActivatedEvent -> Lila OperationModeActivatedEventHandler -> Azul CreateAlertCommand -> Amarillo Alert.
- Naranja EnergyReading recorded -> Lila RuleEvaluationService -> Verde RuleEvaluationResult -> Azul CreateAlertCommand -> Amarillo Alert.
- Blanco Usuario -> Azul UpdateNotificationPreferenceCommand -> Amarillo NotificationPreference.applyNoiseSettings.

## Bounded Context: Reporting

### Agregados
- Amarillo | Aggregate | ConsumptionReport(fields: userId, totalWatts, averageWatts, highestWatts, startDate, endDate)
- Amarillo | Aggregate | EnergyGoal(fields: userId, title, targetKilowattHours, currentKilowattHours, deadline, status, scopeType, scopeId, scopeName, activeFrom, activeTo)

### Comandos
- Azul | Command | CreateConsumptionReportCommand(userId, startDate, endDate)
- Azul | Command | GenerateConsumptionReportCommand(userId, startDate, endDate)
- Azul | Command | ExportReportCommand(userId, startDate, endDate, format)
- Azul | Command | CreateEnergyGoalCommand(userId, title, targetKilowattHours, deadline, scopeType, scopeId, scopeName, activeFrom, activeTo)
- Azul | Command | UpdateEnergyGoalCommand(userId, goalId, title, targetKilowattHours, currentKilowattHours, deadline, status, scopeType, scopeId, scopeName, activeFrom, activeTo)
- Azul | Command | DeleteEnergyGoalCommand(userId, goalId)
- Azul | Command | DeleteReport implicit command(userId, reportId)

### Eventos
- Naranja | Event | ConsumptionReport created
- Naranja | Event | ConsumptionReport refreshed
- Naranja | Event | EnergyGoal created
- Naranja | Event | EnergyGoal progress refreshed
- Naranja | Event | EnergyGoal failed when current kWh exceeds target
- Naranja | Event | EnergyGoal deleted

### Politicas y reglas
- Lila | Policy | ConsumptionReportPolicyService.validateReportRange
- Lila | Policy | ConsumptionReportPolicyService.isValidRange
- Lila | Policy | EnergyGoalPolicyService.canCreateGoal
- Lila | Policy | EnergyGoalProgressService.calculateProgressPercentage
- Lila | Policy | EnergyGoalProgressService.isCompleted
- Lila | Policy | ReportFormatPolicyService.validateFormat/isCsv/isPdf/isJson
- Lila | Policy | ReportSummaryService.calculateTotalReportedWatts
- Lila | Rule | Report requiere readings existentes en rango si no hay reporte cerrado previo
- Lila | Rule | Report de ciclo cerrado se reutiliza si ya existe
- Lila | Rule | Report calcula totalWatts, averageWatts y highestWatts desde EnergyReading
- Lila | Rule | EnergyGoal scope GENERAL no requiere scopeId
- Lila | Rule | EnergyGoal scope DEVICE valida Device del usuario
- Lila | Rule | EnergyGoal scope GROUP valida DeviceGroup del usuario
- Lila | Rule | EnergyGoal scope WORKPLACE valida Location del usuario
- Lila | Rule | EnergyGoal scope ROOM valida Room y su Location del usuario
- Lila | Rule | EnergyGoal progress se calcula filtrando EnergyReading por scope y ventana horaria
- Lila | Rule | EnergyGoal cambia a FAILED cuando currentKilowattHours > targetKilowattHours

### Queries / read models / resources
- Verde | Query | GetConsumptionReportsByUserQuery(userId)
- Verde | Query | GetEnergyGoalsByUserQuery(userId)
- Verde | Read Model | PlatformSummaryResource
- Verde | Read Model | UserPlatformSummary
- Verde | Resource | ConsumptionReportResource
- Verde | Resource | EnergyGoalResource
- Verde | Resource | CreateConsumptionReportResource
- Verde | Resource | CreateEnergyGoalResource
- Verde | Resource | UpdateEnergyGoalResource
- Verde | Endpoint | GET /api/v1/reports
- Verde | Endpoint | POST /api/v1/reports
- Verde | Endpoint | DELETE /api/v1/reports/{reportId}
- Verde | Endpoint | GET /api/v1/reports/energy-goals
- Verde | Endpoint | POST /api/v1/reports/energy-goals
- Verde | Endpoint | PATCH /api/v1/reports/energy-goals/{goalId}
- Verde | Endpoint | DELETE /api/v1/reports/energy-goals/{goalId}
- Verde | Endpoint | GET /api/v1/reporting/platform/summary

### Repositorios / VO / adapters
- Gris | Repository | ConsumptionReportRepository
- Gris | Repository | EnergyGoalRepository
- Rosa | Exporter | ConsumptionReportCsvExporter
- Rosa | Exporter | EnergyGoalCsvExporter
- Turquesa | VO | ReportPeriod
- Turquesa | VO | ReportEstimatedCost
- Turquesa | VO | EnergyGoalTitle
- Turquesa | VO | GoalTargetWatts

### Flujo sugerido en el tablero
- Blanco Usuario -> Azul CreateConsumptionReportCommand -> Lila validar rango -> Amarillo ConsumptionReport -> Verde ConsumptionReportResource.
- Blanco Usuario -> Azul CreateEnergyGoalCommand -> Lila validar scope con Workplace/Room/DeviceGroup/Device -> Amarillo EnergyGoal.
- Verde PlatformInsightApplicationService -> consulta Billing + DeviceControl + EnergyMonitoring + Workplace + Notifications + ServiceManagement -> Verde UserPlatformSummary.

## Bounded Context: ServiceManagement

### Agregados
- Amarillo | Aggregate | SupportTicket(fields: userId, subject, description, priority, status)
- Amarillo | Aggregate | MaintenanceTicket(fields: userId, deviceId, deviceName, type, description, scheduledDate, status)

### Comandos
- Azul | Command | CreateSupportTicketCommand(userId, subject, description, priority)
- Azul | Command | UpdateTicketStatusCommand(status)
- Azul | Command | DeleteSupportTicketCommand(userId, ticketId)
- Azul | Command | CreateMaintenanceTicketCommand(userId, deviceId, deviceName, type, description, scheduledDate)
- Azul | Command | ScheduleMaintenanceTicketCommand(userId, ticketId, scheduledDate)
- Azul | Command | DeleteMaintenanceTicketCommand(userId, ticketId)

### Eventos
- Naranja | Event | SupportTicketCreatedEvent(userId, ticketId, priority, occurredOn)
- Naranja | Event | MaintenanceTicketCreatedEvent(userId, ticketId, deviceId, occurredOn)
- Naranja | Event | SupportTicket status updated
- Naranja | Event | MaintenanceTicket status updated
- Naranja | Event | SupportTicket deleted
- Naranja | Event | MaintenanceTicket deleted

### Politicas y reglas
- Lila | Policy | SupportTicketPolicyService.validateSupportTicket
- Lila | Policy | MaintenanceTicketPolicyService.validateMaintenanceTicket
- Lila | Policy | MaintenanceTicketPolicyService.normalizeType
- Lila | Policy | MaintenanceSchedulePolicyService.validateScheduleDate
- Lila | Policy | MaintenanceSchedulePolicyService.isScheduledForToday
- Lila | Policy | MaintenanceSchedulePolicyService.isOverdue
- Lila | Policy | TicketPriorityPolicyService.isValidPriority
- Lila | Policy | TicketStatusPolicyService.isValidStatus
- Lila | Policy | TicketSummaryService.countOpenSupportTickets
- Lila | Policy | TicketSummaryService.countUrgentSupportTickets
- Lila | Policy | TicketSummaryService.countOpenMaintenanceTickets
- Lila | Policy | TicketSummaryService.countScheduledMaintenanceTickets
- Lila | Specification | OpenTicketSpecification
- Lila | Rule | CreateMaintenanceTicket requiere Device existente del usuario
- Lila | Rule | MaintenanceTicket se crea OPEN
- Lila | Rule | SupportTicketFactory crea SupportTicket desde userId, subject, description y priority
- Lila | Rule | Actualizar status requiere ticket del usuario

### Queries / read models / resources
- Verde | Read | getSupportTickets(userId)
- Verde | Read | getMaintenanceTickets(userId)
- Verde | Resource | SupportTicketResource
- Verde | Resource | MaintenanceTicketResource
- Verde | Resource | CreateSupportTicketResource
- Verde | Resource | CreateMaintenanceTicketResource
- Verde | Resource | UpdateTicketStatusResource
- Verde | Endpoint | GET /api/v1/support-tickets
- Verde | Endpoint | POST /api/v1/support-tickets
- Verde | Endpoint | PATCH /api/v1/support-tickets/{ticketId}/status
- Verde | Endpoint | DELETE /api/v1/support-tickets/{ticketId}
- Verde | Endpoint | GET /api/v1/maintenance-tickets
- Verde | Endpoint | POST /api/v1/maintenance-tickets
- Verde | Endpoint | PATCH /api/v1/maintenance-tickets/{ticketId}/status
- Verde | Endpoint | DELETE /api/v1/maintenance-tickets/{ticketId}

### Repositorios / VO / adapters
- Gris | Repository | SupportTicketRepository
- Gris | Repository | MaintenanceTicketRepository
- Rosa | Adapter | TicketNotificationPort
- Rosa | Adapter | ConsoleTicketNotificationAdapter
- Turquesa | VO | TicketTitle
- Turquesa | VO | TicketDescription
- Turquesa | VO | TicketCode
- Turquesa | VO | Priority

### Flujo sugerido en el tablero
- Blanco Usuario -> Azul CreateSupportTicketCommand -> Amarillo SupportTicket -> Naranja SupportTicketCreatedEvent.
- Blanco Usuario -> Azul CreateMaintenanceTicketCommand -> Lila validar DeviceRepository -> Amarillo MaintenanceTicket -> Naranja MaintenanceTicketCreatedEvent.

## Integraciones reales entre bounded contexts

- Naranja | Cross-context Event | OperationModeActivatedEvent de DeviceControl es consumido por OperationModeActivatedEventHandler y genera CreateAlertCommand en Notifications.
- Lila | ACL/Published Language | DeviceControl consulta Workplace: LocationRepository, RoomRepository y DeviceAssignmentRepository para validar Location, Room y assignments.
- Lila | ACL/Published Language | DeviceControl consulta Reporting: EnergyGoalRepository para validar goalIds en OperationMode.
- Lila | ACL/Published Language | DeviceControl consulta Notifications: AlertRuleProfileRepository y NotificationPreferenceRepository para validar ruleProfileId y preferenceId en OperationMode.
- Lila | ACL/Published Language | DeviceControl invoca EnergyMonitoring: EnergyReadingRecorderService para registrar consumo al cambiar estado de Device.
- Lila | ACL/Published Language | EnergyMonitoring consulta DeviceControl: DeviceRepository y DeviceStatus para registrar lecturas de dispositivos ON.
- Lila | ACL/Published Language | EnergyMonitoring invoca Notifications: NotificationApplicationService.createAlertFromRuleEvaluation despues de registrar EnergyReading.
- Lila | ACL/Published Language | Notifications valida ownership contra DeviceControl, Workplace y Reporting por AlertSourceOwnershipService.
- Lila | ACL/Published Language | Reporting consulta EnergyMonitoring: EnergyReadingRepository para reportes y progreso de metas.
- Lila | ACL/Published Language | Reporting consulta Workplace: LocationRepository, RoomRepository, DeviceAssignmentRepository para scopes WORKPLACE y ROOM.
- Lila | ACL/Published Language | Reporting consulta DeviceControl: DeviceRepository, DeviceGroupRepository, DeviceGroupDeviceRepository para scopes DEVICE y GROUP.
- Lila | ACL/Published Language | ServiceManagement consulta DeviceControl: DeviceRepository para crear MaintenanceTicket.
- Verde | Integration Read Model | PlatformInsightApplicationService compone Subscription, Devices, Routines, DeviceGroups, EnergyReadings, Locations, Rooms, Assignments, Alerts, Rules, SupportTickets y MaintenanceTickets.
- Verde | Dashboard | EnergyDashboardSummaryResource combina DeviceControl devices, EnergyMonitoring readings y Notifications active alerts.
- Turquesa | Published Language | Scope types compartidos conceptualmente: GENERAL, WORKPLACE, ROOM, DEVICE, GROUP, ROUTINE, GOAL.
- Turquesa | Published Language | AlertSourceType cubre DEVICE, GROUP, ROOM, WORKPLACE, ROUTINE, GOAL, REPORT, SYSTEM, RULE, MODE.

## Hotspots / decisiones para Miro

- Rojo | Hotspot | Algunos eventos existen como clases pero no siempre se publican explicitamente: UserRegisteredEvent, DeviceCreatedEvent, DeviceAssignedToRoomEvent, AlertCreatedEvent, SupportTicketCreatedEvent, MaintenanceTicketCreatedEvent, EnergyThresholdExceededEvent, SubscriptionActivatedEvent, PaymentRegisteredEvent.
- Rojo | Hotspot | Billing PlanLimitPolicyService existe pero no se aplica aun al crear devices/routines/alerts.
- Rojo | Hotspot | Workplace usa DeviceRepository directamente desde DeviceControl; marcar como ACL o published language si se formaliza.
- Rojo | Hotspot | Device.room sigue existiendo como texto aunque Workplace ya tiene Room aggregate y DeviceAssignment.
- Rojo | Hotspot | EnergySamplingSettingsService mantiene sampleSeconds en memoria, no persistido.
- Rojo | Hotspot | Algunos commands existen pero no tienen endpoint directo o uso completo: AddDeviceToGroupCommand, RemoveDeviceFromGroupCommand, GenerateConsumptionReportCommand, ExportReportCommand, ScheduleMaintenanceTicketCommand.
- Rojo | Hotspot | NotificationDeliveryPort y TicketNotificationPort son adapters preparados, pero la entrega real aun es consola.

## Secuencia principal de negocio recomendada en EventStorming

1. Usuario se registra e inicia sesion.
2. Usuario contrata o consulta plan en Billing.
3. Usuario crea Location/Sede.
4. Usuario crea Room dentro de Location.
5. Usuario crea Device en DeviceControl.
6. Usuario asigna Device a Location/Room en Workplace.
7. Usuario crea DeviceGroup con dispositivos asignados a la misma sede y room.
8. Usuario crea Routine sobre Device, Group, Room o Workplace.
9. Usuario crea AlertRule, AlertRuleProfile y NotificationPreference.
10. Usuario crea EnergyGoal con scope GENERAL, WORKPLACE, ROOM, DEVICE o GROUP.
11. Usuario crea OperationMode conectado a Location, Room, Groups, Devices, Routines, Goals, RuleProfile y Preference.
12. Usuario activa OperationMode.
13. DeviceControl aplica cambios a dispositivos/rutinas y publica OperationModeActivatedEvent.
14. Notifications crea Alert de modo activado.
15. EnergyMonitoring registra lecturas por scheduler o transiciones de estado.
16. RuleEvaluationService evalua reglas y crea Alert automatica si aplica.
17. Reporting genera reportes y refresca progreso de EnergyGoal desde EnergyReading.
18. PlatformInsight arma Dashboard/Summary cruzando todos los contextos.

