# Scheduled Jobs

This document lists all scheduled jobs in The True Market API, their purpose, configuration, and dependencies.

## Overview

The API uses Spring's `@Scheduled` annotation for executing periodic background tasks. Jobs are automatically enabled when the application starts unless explicitly disabled via configuration.

---

## 1. CleanupCompletedTasksJob

**Purpose**: Automatically cleans up old completed history update tasks to prevent database bloat.

**Schedule**: Every 30 minutes

**Retention Period**: 24 hours (hardcoded)

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `task.cleanup.interval-ms` | `1800000` | Execution interval in milliseconds (30 minutes) |

### How It Works

1. Calculates cutoff date (current time - 24 hours)
2. Finds all `COMPLETED` tasks with `finished_at` before the cutoff date
3. Deletes matching tasks in a single transaction
4. Logs the number of tasks deleted

### Dependencies

- `HistoryUpdateTaskRepository`: Queries and deletes tasks
  - `findByStatusAndFinishedAtBefore()`
  - `deleteByStatusAndFinishedAtBefore()`

### Transaction Behavior

- Runs in a single transaction (`@Transactional`)
- If deletion fails, all changes are rolled back

### Example Log Output

```
INFO  - Starting CleanupCompletedTasksJob
INFO  - Found 42 completed tasks older than 24 hours
INFO  - CleanupCompletedTasksJob completed: 42 tasks deleted
```

---

## 2. UpdateSkinPricesJob

**Purpose**: Updates Steam prices for existing skins by creating update tasks in batches, with automatic deduplication to ensure only one task per unique skin.

**Schedule**: Every 30 minutes

**Batch Processing**: Processes one page of skins per execution, cycling through all skins over time.

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `skin.price.update.enabled` | `true` | Enable/disable the job |
| `skin.price.update.interval-ms` | `1800000` | Execution interval in milliseconds (30 minutes) |
| `skin.price.update.batch-size` | `100` | Number of skins to process per execution |

### How It Works

1. Fetches one page of skins from the database (configurable batch size)
2. Groups skins by unique (skinName, wear) combination to avoid duplicate processing
3. For each unique skin:
   - Checks if price history needs update (`CheckPriceHistoryUseCase`)
   - Creates update task if needed (`CreateHistoryUpdateTaskUseCase`)
   - Built-in deduplication prevents creating duplicate waiting tasks
4. Advances to next page for the next execution
5. Resets to page 0 when reaching the last page

### Deduplication Strategy

**Double-layer deduplication ensures only one task per unique skin:**

1. **Batch-level**: Groups skins in current batch by (skinName, wear)
2. **Database-level**: `CreateHistoryUpdateTaskUseCase` checks for existing `WAITING` tasks before creation

**Example**: If there are 2 "AK-47 | Midnight Laminate (Field-Tested)" skins:
- Batch grouping processes only one
- Database check prevents creating duplicate if waiting task already exists

### Dependencies

- `SkinRepository`: Fetches skins with pagination
  - `findAll(Pageable)`
- `CheckPriceHistoryUseCase`: Determines if price history needs update
  - `needsUpdate(String skinName, Wear wear)`
- `CreateHistoryUpdateTaskUseCase`: Creates update tasks with built-in deduplication
  - `execute(String skinName, Wear wear)`

### Conditional Execution

The job only runs if:
- `skin.price.update.enabled=true` (or property not set, defaults to true)
- Uses `@ConditionalOnProperty` for runtime control

### State Management

- Tracks current page number across executions using `AtomicInteger`
- State is in-memory (resets if application restarts)
- Cycles through all skins over multiple executions

### Example Log Output

```
INFO  - Starting UpdateSkinPricesJob (batch size: 100)
INFO  - Processing page 1 of 15 (total elements: 1450, batch size: 100)
DEBUG - Found 87 unique skin combinations in current batch
INFO  - UpdateSkinPricesJob completed: 23 tasks created, 51 skipped (no update needed), 13 skipped (duplicate waiting task), page 1/15
```

### Performance Considerations

- Processes limited batch per execution to avoid memory issues
- Pagination prevents loading entire database into memory
- Configurable batch size allows tuning based on system resources
- Over time (15 executions with default config), all 1450 skins are checked

---

## 3. RetryFailedConversionsJob

**Purpose**: Retries currency conversions that previously failed due to exchange rate API unavailability.

**Schedule**: Every 1 hour (default)

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `exchange-rate.retry.interval-ms` | `3600000` | Execution interval in milliseconds (1 hour) |
| `exchange-rate.retry.max-attempts` | `10` | Maximum retry attempts before marking as permanently failed |
| `exchange-rate.retry.initial-delay-minutes` | `5` | Initial delay before first retry |

### How It Works

1. Finds all failed conversion tasks ready for retry (based on `next_retry_at`)
2. For each task:
   - Deserializes skin market data from JSON
   - Attempts currency conversion (BRL → USD)
   - Saves skin if successful and deletes the failed task
   - Updates retry metadata if failed (increments attempt, calculates next retry)
3. Marks tasks as permanently failed after max attempts

### Dependencies

- `FailedConversionTaskRepository`: Queries and manages failed tasks
  - `findTasksReadyForRetry(LocalDateTime)`
  - `save(FailedConversionTask)`
  - `deleteById(Long)`
- `CurrencyConversionService`: Performs BRL to USD conversion
  - `convertBrlToUsd(Long)`
- `SaveSkinUseCase`: Saves successfully converted skins
  - `execute(Skin)`

### Retry Strategy

- Exponential backoff with configurable initial delay
- Tasks marked permanently failed after max attempts
- Logs different messages for temporary failures vs permanent failures

### Example Log Output

```
INFO  - Starting RetryFailedConversionsJob
INFO  - Found 5 tasks ready for retry
INFO  - Successfully converted price for task 123: 15000 BRL -> 3000 USD
INFO  - Successfully processed and deleted task 123
WARN  - Task 124 failed, will retry at 2025-11-13T15:30:00
ERROR - Task 125 marked as permanently failed after 10 attempts
INFO  - RetryFailedConversionsJob completed: 2 succeeded, 2 failed, 1 permanently failed
```

---

## Job Scheduling Configuration

All jobs use Spring's `@Scheduled` annotation configured in:
- `SchedulingConfig.java` - Enables scheduling with `@EnableScheduling`

### Common Patterns

1. **Fixed Rate Execution**: Jobs run at fixed intervals regardless of execution time
   ```java
   @Scheduled(fixedRateString = "${property.name:defaultValue}")
   ```

2. **Configuration Externalization**: All intervals are configurable via `application.properties`

3. **Error Handling**: All jobs wrap logic in try-catch blocks to prevent job termination

4. **Logging**: Comprehensive logging at INFO, DEBUG, and ERROR levels

5. **Transactional Support**: Jobs that modify data use `@Transactional` when needed

---

## Monitoring and Troubleshooting

### Viewing Job Logs

Enable DEBUG logging for detailed execution information:

```properties
logging.level.com.thetruemarket.api.infrastructure.job=DEBUG
```

### Common Issues

**Issue**: Job not executing
- **Check**: `@EnableScheduling` is present in `SchedulingConfig`
- **Check**: Configuration property values are valid
- **Check**: For `UpdateSkinPricesJob`, ensure `skin.price.update.enabled=true`

**Issue**: Too many tasks created
- **Solution**: Increase `skin.price.update.interval-ms` to reduce execution frequency
- **Solution**: Decrease `skin.price.update.batch-size` to process fewer skins per run

**Issue**: Cleanup job deleting too many/few tasks
- **Solution**: Retention period is hardcoded to 24 hours in `CleanupCompletedTasksJob`
- **Solution**: Modify `RETENTION_HOURS` constant in the class

**Issue**: Memory issues with UpdateSkinPricesJob
- **Solution**: Reduce `skin.price.update.batch-size` (default: 100)
- **Solution**: Increase `skin.price.update.interval-ms` to allow more processing time

---

## Disabling Jobs

### Disable UpdateSkinPricesJob

```properties
skin.price.update.enabled=false
```

### Disable Other Jobs

Set interval to a very large value or comment out the `@Scheduled` annotation:

```properties
# Disable cleanup job (set to run once per year)
task.cleanup.interval-ms=31536000000

# Disable retry job (set to run once per year)
exchange-rate.retry.interval-ms=31536000000
```

---

## Future Enhancements

1. **Persistent State for UpdateSkinPricesJob**: Store current page in database to survive restarts
2. **Job Metrics**: Expose job execution metrics via Spring Actuator
3. **Dynamic Scheduling**: Allow runtime modification of job schedules via API
4. **Job History**: Track job execution history in database
5. **Configurable Retention**: Make cleanup retention period configurable via properties
6. **Parallel Processing**: Process multiple batches concurrently for faster updates
