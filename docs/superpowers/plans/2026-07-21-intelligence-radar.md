# Intelligence Radar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a server-backed intelligence feed at `/intelligence` with real GitHub, RSS, webpage, and YouTube collectors first, then add the account-backed social channels and shared bookmark workflow.

**Architecture:** Spring owns subscriptions, collection runs, normalization, batch persistence, filters, bookmarks, tags, scheduling, and channel state. Collector adapters implement one `IntelligenceCollector` interface and return normalized candidates without writing the database. React consumes only `/api/intelligence`, while platform authentication is referenced by `PlatformAccountId` from the unified account center.

**Tech Stack:** Java 8, Spring Boot 2.7, MyBatis annotations, MySQL/Flyway, React/Vite/Vitest, `gh`, Python `feedparser`, Jina Reader HTTP, `yt-dlp`.

## Global Constraints

- No fallback collector or demo data. A failed fixed adapter must return an explicit stable error.
- Every entity uses a backend-assigned numeric ID; all batch mutations accept deduplicated `ids` and use one batch SQL statement.
- Credentials stay in `c_PlatformAccountSecrets`; intelligence records store only `PlatformAccountId` references.
- Business logs include operation, platform, account ID, subscription ID, run ID, requested, fetched, deduplicated, and affected counts without secrets or full content.
- Frontend search uses `UiSearchField`; the workspace consumes semantic `uiTheme` variables and native interactive controls.
- Only relevant local tests run; production never runs tests.

---

### Task 1: Pin the collector reference and probe fixed executors

**Files:**
- Create: `third_party/agent-reach/` from a fixed upstream commit
- Create: `docs/intelligence-sources.md`
- Create: `scripts/Test-IntelligenceCollectors.ps1`

**Interfaces:**
- Produces: a source manifest and a probe script returning one JSON object per adapter with `adapter`, `available`, `version`, and `errorCode`.

- [ ] Clone the upstream source into `third_party/agent-reach`, remove its nested `.git`, and record the exact commit in `docs/intelligence-sources.md`.
- [ ] Write `Test-IntelligenceCollectors.ps1` to call `gh --version`, `python -c "import feedparser"`, a Jina Reader HEAD/read request, and `yt-dlp --version` independently.
- [ ] Run the probe locally and record only availability/version/error code, never environment values.
- [ ] Commit with `chore(intelligence): pin collector reference and probes`.

### Task 2: Create intelligence schema and persistence contracts

**Files:**
- Create: `AIProvider-back/src/main/resources/db/migration/V52__intelligence_radar.sql`
- Create: `AIProvider-back/src/main/java/com/aiprovider/mapper/IntelligenceMapper.java`
- Create: `AIProvider-back/src/main/java/com/aiprovider/repository/IntelligenceRepository.java`
- Test: `AIProvider-back/src/test/java/com/aiprovider/repository/IntelligenceRepositoryTest.java`

**Interfaces:**
- Produces: `insertSubscription`, `updateSubscription`, `archiveSubscription`, `startRun`, `completeRun`, `findItems`, `countItems`, `insertCandidatesBatch`, `updateItemStateBatch`, `replaceItemTagsBatch`, and `findChannelStates`.

- [ ] Write repository tests proving generated IDs, server-side filters, unique platform identity, single-statement batch mutations, and affected-row mismatch failure.
- [ ] Run `mvn "-Dtest=IntelligenceRepositoryTest" test` and verify failure because the repository does not exist.
- [ ] Add V52 tables for subscriptions, items, runs, bookmarks, tags, item tags, and channel states with numeric keys and required unique indexes.
- [ ] Implement mapper batch SQL and repository affected-row checks.
- [ ] Rerun the repository test and commit with `feat(intelligence): add radar persistence model`.

### Task 3: Implement the collection state machine

**Files:**
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/IntelligenceCollector.java`
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/IntelligenceCandidate.java`
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/IntelligenceCollectionService.java`
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/IntelligenceErrorCode.java`
- Test: `AIProvider-back/src/test/java/com/aiprovider/service/intelligence/IntelligenceCollectionServiceTest.java`

**Interfaces:**
- `IntelligenceCollector.platform(): String`
- `IntelligenceCollector.collect(IntelligenceSubscriptionVO subscription): List<IntelligenceCandidate>`
- `IntelligenceCollectionService.collect(long subscriptionId): IntelligenceRunVO`

- [ ] Write tests for CREATED -> RUNNING -> SUCCEEDED/FAILED, URL normalization, within-batch deduplication, existing-row deduplication, and mismatch failure.
- [ ] Run the focused test and verify it fails before implementation.
- [ ] Implement exact-platform collector lookup, validation, fingerprinting, one transaction for dedup/write/run completion, and structured logs.
- [ ] Rerun tests and commit with `feat(intelligence): add collection state machine`.

### Task 4: Add GitHub, RSS, webpage, and YouTube adapters

**Files:**
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/ProcessRunner.java`
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/GithubIntelligenceCollector.java`
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/RssIntelligenceCollector.java`
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/WebIntelligenceCollector.java`
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/YoutubeIntelligenceCollector.java`
- Create: `AIProvider-back/src/main/resources/intelligence/feedparser_collect.py`
- Test: corresponding `*CollectorTest.java` files

**Interfaces:**
- Process output is bounded, UTF-8 JSON, timeout-controlled, and contains no credential arguments.
- Each collector returns the common candidate fields `platformItemId`, `sourceUrl`, `title`, `summary`, `author`, `publishedAt`, and `metadataJson`.

- [ ] Write contract tests with executable fixtures for success, timeout, malformed output, upstream unavailable, and empty result.
- [ ] Implement `gh` JSON collection, Python feedparser JSON collection, Jina Reader HTTP parsing, and `yt-dlp --dump-single-json` collection.
- [ ] Run the four collector tests and then one real public-source probe per adapter.
- [ ] Commit with `feat(intelligence): collect core public channels`.

### Task 5: Add API, scheduling, and account references

**Files:**
- Create: `AIProvider-back/src/main/java/com/aiprovider/controller/IntelligenceController.java`
- Create: DTO/VO files under `model/dto/intelligence` and `model/vo/intelligence`
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/IntelligenceService.java`
- Create: `AIProvider-back/src/main/java/com/aiprovider/service/intelligence/IntelligenceScheduler.java`
- Test: `AIProvider-back/src/test/java/com/aiprovider/controller/IntelligenceControllerTest.java`
- Test: `AIProvider-back/src/test/java/com/aiprovider/service/intelligence/IntelligenceSchedulerTest.java`

**Interfaces:**
- `GET /api/intelligence/items`
- `GET|POST|PUT|DELETE /api/intelligence/subscriptions`
- `POST /api/intelligence/subscriptions/{id}/collect`
- `POST /api/intelligence/subscriptions/collect` with `{ids:[...]}`
- `GET /api/intelligence/runs` and `GET /api/intelligence/channels`
- `PUT /api/intelligence/items/state` and `PUT /api/intelligence/items/tags`

- [ ] Write MVC and scheduler tests for validation, pagination, combined filters, account/platform matching, deduplicated IDs, and independent channel failures.
- [ ] Implement thin controller methods, service validation, fixed-delay due-subscription scheduling, and `PlatformAccountId` checks.
- [ ] Run focused tests and commit with `feat(intelligence): expose radar API and scheduler`.

### Task 6: Build the four-section left-nav workspace

**Files:**
- Create: `AIProvider-front/src/IntelligenceRadar.jsx`
- Create: `AIProvider-front/src/IntelligenceRadar.css`
- Create: `AIProvider-front/src/IntelligenceRadar.test.jsx`
- Modify: `AIProvider-front/src/App.jsx`
- Modify: `AIProvider-front/src/SemanticTheme.css`
- Modify: `AIProvider-front/src/uiGate.test.js`

**Interfaces:**
- Route `/intelligence`; tabs `今日雷达`, `订阅管理`, `我的收藏`, `渠道状态`.
- Consumes only the Task 5 endpoints and navigates account setup to `/accounts`.

- [ ] Write failing frontend tests for navigation, four sections, filters, pagination, subscription CRUD, batch state changes, stale-data channel errors, and account-center links.
- [ ] Add the left navigation route and semantic workspace shell.
- [ ] Implement the dense timeline/card UI using `UiSearchField`, native controls, visible focus, and backend pagination.
- [ ] Run `npm test -- IntelligenceRadar.test.jsx uiGate.test.js` and `npm run build`.
- [ ] Commit with `feat(intelligence): add radar workspace`.

### Task 7: Add account-backed social collectors and complete real acceptance

**Files:**
- Create: social collector classes under `service/intelligence/`
- Modify: `PlatformAccountMapper.java` and `PlatformAccountService.java` only for intelligence usage reporting
- Modify: `IntelligenceRadar.jsx` for newly available platforms
- Test: focused collector, service, account usage, and UI tests

**Interfaces:**
- Fixed platform adapters: Bilibili -> `bili-cli`, Xiaohongshu -> configured XHS executor, X -> `twitter-cli`, Reddit -> `rdt-cli`, long-tail -> configured RSSHub URL through the RSS collector.

- [ ] Probe each executor locally and on AWS; record explicit blocked channels without adding alternatives.
- [ ] Write contract tests and implement only adapters that pass their fixed-executor probes.
- [ ] Bind all secrets through `PlatformAccountCredentialService`, using permission-restricted temporary files only where the executor requires one and verifying deletion.
- [ ] Execute real create-subscription -> collect -> persist -> display -> recollect/deduplicate acceptance per available platform.
- [ ] Run all intelligence-focused backend/frontend/UI tests, `git diff --check`, and build.
- [ ] Commit with `feat(intelligence): complete account-backed channels`.

## Self-Review

- Spec coverage: schema, fixed adapters, account references, four UI sections, batch SQL, scheduler, channel errors, observability, and real acceptance each map to a task.
- Placeholder scan: no deferred implementation placeholders; unavailable fixed adapters are explicit blocked outcomes, not alternate implementations.
- Type consistency: all collectors return `IntelligenceCandidate`; API and UI names use `subscriptionId`, `platformAccountId`, numeric `ids`, and `/api/intelligence` consistently.
