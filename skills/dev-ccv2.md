# CCv2 — Plugin Dev Reference

Module: `modules/ccv2/`. Submodules: `core`, `ui`, `exec`, `project`, `manifest`, `mcp`.

## Module Responsibilities

| Submodule | Gradle id | Role |
|---|---|---|
| `ccv2-core` | `:ccv2-core` | API clients (CCv2Api, CCv1Api), DTOs, settings, service layer |
| `ccv2-ui` | `:ccv2-ui` | Tool window, actions, dialogs, unscrambler UI |
| `ccv2-exec` | `:ccv2-exec` | Remote execution (Groovy via HAC on CCv2) |
| `ccv2-project` | `:ccv2-project` | Project-level CCv2 facet/configuration |
| `ccv2-manifest` | `:ccv2-manifest` | `manifest.json` / `extensionPacks.json` editor |
| `ccv2-mcp` | `:ccv2-mcp` | MCP toolset — 17 `@McpTool` methods |

## Settings & Auth (ccv2-core)

### `CCv2ProjectSettings`
App-level `@Service @State(PER_OS)`. Entry point: `CCv2ProjectSettings.getInstance()`.

Key state:
- `subscriptions: List<CCv2Subscription>` — configured subscriptions list
- `authentication: CCv2Authentication` — global OAuth settings (token endpoint, resource URL)
- `kymaApiUrl: String` — base API URL; default: `"https://portalapi.commerce.ondemand.com"`

`CCv2Subscription` fields: `uuid: String`, `id: String?`, `name: String?`, `presentableName: String`, `authentication: CCv2Authentication?` (subscription-specific OAuth override)

### Credential Storage
Credentials are stored in PasswordSafe (platform secure store), NOT in the settings XML.

```kotlin
// Read (synchronous, call on IO thread)
val creds: Credentials? = settings.getCCv2Authentication(subscriptionUUID?)

// Write
val key = CredentialAttributes(CCv2Constants.SECURE_STORAGE_CCV2_AUTHENTICATION)          // global
val key = CredentialAttributes("${CCv2Constants.SECURE_STORAGE_CCV2_AUTHENTICATION} - $uuid")  // per-subscription
PasswordSafe.instance[key] = Credentials(clientId, clientSecret)
```

`CCv2Constants.SECURE_STORAGE_CCV2_AUTHENTICATION` = `"SAP CX CCv2 Authentication"`.

### Auth Flow
1. `CCv2ProjectSettings.getCCv2Authentication(uuid?)` → `Credentials?`
2. `HttpRequests.post(auth.tokenEndpoint, "application/x-www-form-urlencoded")` with body:
   `client_id=...&client_secret=...&grant_type=client_credentials&resource=<encoded>`
3. Parse `access_token` from JSON response
4. Return `KymaApiContext(apiUrl, accessToken)`

`KymaApiContext` implements `ApiContext` (fields: `apiUrl`, `authToken`, `authHeader`).
`CCv2Service.retrieveAuthToken(apiUrl, auth, credentials)` is a **public** instance method (use it from project-level code). MCP layer replicates this logic to avoid project scope.

## API Clients (ccv2-core)

### `CCv2Api` — App-level `@Service`
Entry: `CCv2Api.getInstance()`. All methods are `suspend fun`.

Methods requiring `ProgressReporter`:
```kotlin
fetchEnvironments(progressReporter, apiContext, subscription, statuses, requestV1Details, requestV1Health)
fetchBuilds(apiContext, subscription, statusNot, top, orderBy, progressReporter)
fetchDeployments(apiContext, subscription, progressReporter)
fetchDeploymentsForBuild(subscription, deploymentCode, apiContext, progressReporter)
```
Wrap in `coroutineScope { reportProgressScope(1) { reporter -> ... } }`.

Plain suspend methods:
```kotlin
fetchBuildForCode(apiContext, subscription, buildCode): CCv2BuildDto
fetchEndpoints(apiContext, subscription, environment): List<CCv2EndpointDto>?
fetchEnvironmentDataBackups(apiContext, subscription, environment): List<CCv2DataBackupDto>?
fetchScheduledActivities(apiContext, subscription, environment): List<CCv2ScheduledActivityDto>?
createBuild(apiContext, buildRequest): String           // returns buildCode
deployBuild(apiContext, subscription, environment, build, updateMode, strategy): String  // returns deploymentCode
downloadBuildLogs(apiContext, subscription, build): File  // returns ZIP file
updateEndpoint(apiContext, subscription, environment, endpoint, payload)
```

### `CCv1Api` — App-level `@Service`
Entry: `CCv1Api.getInstance()`. Used for service/replica operations (CCv1 Kyma API).

```kotlin
fetchEnvironmentServices(apiContext, subscription, environment): List<CCv2ServiceDto>
restartServiceReplica(apiContext, subscription, environment, service, replica)
```

### `CCv2Service` — Project-level `@Service`
Entry: `CCv2Service.getInstance(project)`. **Do NOT use from app-level MCP code** — it is project-scoped and uses callback-based (non-suspend) public methods. Use `CCv2Api`/`CCv1Api` directly from app-level code.

`CCv2Service.retrieveAuthToken(apiUrl, auth, credentials)` IS a public method — usable in project scope.

## Key DTOs (ccv2-core `dto/` package)

| DTO | Key fields |
|---|---|
| `CCv2EnvironmentDto` | `code`, `name`, `type: CCv2EnvironmentType`, `status: CCv2EnvironmentStatus`, `deploymentStatus`, `deploymentAllowed: Boolean`, `link: String?` |
| `CCv2BuildDto` | `code`, `name`, `branch`, `status: CCv2BuildStatus`, `appCode`, `appDefVersion`, `createdBy`, `startTime/endTime: ZonedDateTime?`, `buildVersion`, `deployed: Boolean`, `link: String?` |
| `CCv2DeploymentDto` | `code`, `buildCode`, `envCode`, `status: CCv2DeploymentStatus`, `updateMode: CCv2DeploymentDatabaseUpdateModeEnum`, `strategy: CCv2DeploymentStrategy`, `createdBy`, `createdTime/scheduledTime/deployedTime/failedTime: ZonedDateTime?`, `link: String?` |
| `CCv2ServiceDto` | `code`, `name`, `desiredReplicas: Int?`, `availableReplicas: Int?`, `replicas: List<CCv2ReplicaDto>`, `link: String` |
| `CCv2ReplicaDto` | `name`, `status`, `ready: Boolean` |
| `CCv2EndpointDto` | `code`, `name`, `service`, `url`, `maintenanceMode: Boolean`, `link: String` |
| `CCv2DataBackupDto` | `dataBackupCode`, `name`, `buildCode`, `status`, `dataBackupType`, `description`, `createdBy`, `createdTimestamp: ZonedDateTime?` |
| `CCv2ScheduledActivityDto` | `code`, `activityType: CCv2ActivityType`, `activityName`, `status: CCv2ActivityStatus`, `scheduledTimestamp: ZonedDateTime`, `startedTimestamp/finishedTimestamp/createdTimestamp: ZonedDateTime?`, `createdBy` |

### Enum Value Sets

`CCv2EnvironmentStatus`: PROVISIONING, AVAILABLE, TERMINATING, TERMINATED, READY_FOR_DEPLOYMENT, UNKNOWN
`CCv2BuildStatus`: BUILDING, SUCCESS, FAIL, DELETED, UNKNOWN
`CCv2DeploymentDatabaseUpdateModeEnum`: NONE, UPDATE, INITIALIZE, UNKNOWN — `allowedOptions()` returns non-UNKNOWN
`CCv2DeploymentStrategy`: ROLLING_UPDATE, RECREATE, GREEN, UNKNOWN — `allowedOptions()` returns non-UNKNOWN

`tryValueOf(s)` extension: returns `UNKNOWN` for unrecognised strings (all enums have it).

## MCP Layer (ccv2-mcp)

Module: `modules/ccv2/mcp/` (`:ccv2-mcp`). Key files under `src/.../ccv2/mcp/`:

- `CCv2McpToolset.kt` — 17 `@McpTool suspend fun`; no logic; delegates to service
- `CCv2McpService.kt` — app-level `@Service`; auth, API calls, DTO mapping
- `CCv2McpConstants.kt` — shared `@McpDescription` strings
- `dto/` — `@Serializable data class` MCP DTOs

### MCP Tools Summary

| Tool | Method | Notes |
|---|---|---|
| `sap_commerce_ccv2_list_subscriptions` | `listSubscriptions()` | No auth needed; reads local settings |
| `sap_commerce_ccv2_list_environments` | `listEnvironments(subscriptionId?)` | |
| `sap_commerce_ccv2_list_builds` | `listBuilds(subscriptionId?, top)` | Excludes DELETED |
| `sap_commerce_ccv2_get_build` | `getBuild(subscriptionId?, buildCode)` | |
| `sap_commerce_ccv2_create_build` | `createBuild(subscriptionId?, branch, name)` | Returns buildCode |
| `sap_commerce_ccv2_list_deployments` | `listDeployments(subscriptionId?)` | |
| `sap_commerce_ccv2_deploy_build` | `deployBuild(subscriptionId?, buildCode, environmentCode, mode, strategy)` | Destructive |
| `sap_commerce_ccv2_get_deployment_status` | `getDeploymentStatus(subscriptionId?, deploymentCode)` | |
| `sap_commerce_ccv2_list_environment_services` | `listEnvironmentServices(subscriptionId?, environmentCode)` | CCv1 API |
| `sap_commerce_ccv2_restart_service_pod` | `restartServicePod(subscriptionId?, environmentCode, serviceCode, replicaName)` | Destructive |
| `sap_commerce_ccv2_list_environment_endpoints` | `listEnvironmentEndpoints(subscriptionId?, environmentCode)` | |
| `sap_commerce_ccv2_toggle_endpoint_maintenance` | `toggleEndpointMaintenance(subscriptionId?, environmentCode, endpointCode)` | Toggles on↔off |
| `sap_commerce_ccv2_list_data_backups` | `listDataBackups(subscriptionId?, environmentCode)` | |
| `sap_commerce_ccv2_list_scheduled_activities` | `listScheduledActivities(subscriptionId?, environmentCode)` | |
| `sap_commerce_ccv2_download_build_logs` | `downloadBuildLogs(subscriptionId?, buildCode)` | Large; defaults to FILE |
| `sap_commerce_ccv2_unscramble_log` | `unscrambleLog(logText)` | No auth needed |
| `sap_commerce_ccv2_set_credentials` | `setCredentials(subscriptionId?, clientId, clientSecret)` | Writes PasswordSafe |

### `CCv2McpService` Patterns

**Subscription resolution** (all tools except `listSubscriptions`, `unscrambleLog`, `setCredentials`):
```kotlin
// in getApiContext(subscriptionId?):
//   subscriptionId == null → first configured subscription
//   subscriptionId != null → match by id OR name
//   → error with actionable message if not found
```

**Auth context resolution**:
```kotlin
// resolveApiContext(): subscription-specific creds first, then global
// retrieveAuthToken(): replicates CCv2Service logic; uses kotlinx.serialization.json (not Gson)
// All credential/HTTP calls wrapped in withContext(Dispatchers.IO)
```

**Environment resolution** (tools needing environmentCode):
```kotlin
// resolveEnvironment(subscription, apiContext, environmentCode)
// fetches all environments, finds by code; error with helpful message if not found
```

**ProgressReporter wrapping**:
```kotlin
coroutineScope {
    reportProgressScope(1) { reporter ->
        CCv2Api.getInstance().fetchXxx(reporter, ...)
    }
}
```

### MCP DTO Mapping Convention
Extension functions at the bottom of `CCv2McpService.kt`:
`CCv2XxxDto.toMcpDto()` → `CCv2MXxxDto`. Enum fields: `.name` (string). Timestamps: `.toString()`.

## Unscramble Service

`CCv2UnscrambleService` — app-level `@Service`. Entry: `CCv2UnscrambleService.getInstance()`.
`buildStackTraceString(logText: String): String?` — parses CCv2 JSON exception log, returns readable stack trace.
Used by `sap_commerce_ccv2_unscramble_log` MCP tool. Returns `null` when input has no `thrown.extendedStackTrace`.

## Build Requests

```kotlin
CCv2BuildRequest(
    subscription: CCv2Subscription,
    branch: String,
    name: String,
    track: Boolean,           // false for MCP (fire-and-forget)
    deploymentRequests: List  // emptyList() for MCP
)
```

## Manifest / extensionPacks

`modules/ccv2/manifest/` — editors for `manifest.json` and `extensionPacks.json` (CCv2 cloud configuration).
Not related to the API layer; pure file editing with JSON schema support.
