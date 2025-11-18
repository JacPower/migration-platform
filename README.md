# Migration Tool

A Java tool to migrate jobs from a competitor's job scheduling system to the Redwood platform.

---

## What It Does

- Parses competitor job exports (JSON format)
- Validates data quality (detects circular dependencies, missing references, etc.)
- Converts jobs to Desired format
- Reports what can and cannot be migrated

---

## Requirements

- Java 21 or higher
- Maven 3.6+

---

## Build

```bash
mvn clean install
```

---

## Run

```bash
java -jar target/migration-tool-1.0.0.jar <input-file.json>
```

**Example:**
```bash
java -jar target/migration-tool-1.0.0.jar competitor_export.json
```

---

## Input File Format

Create a JSON file with your jobs:

```json
{
  "jobs": [
    {
      "jobId": 1001,
      "jobName": "Daily_Backup",
      "jobType": "BACKUP",
      "system": "ORACLE",
      "trigger": {
        "type": "SCHEDULE",
        "cronExpression": "0 2 * * *",
        "timezone": "UTC"
      },
      "dependencies": [],
      "executionConstraints": {
        "priority": 5,
        "maxRuntimeMinutes": 60,
        "excludeHolidays": false
      },
      "notes": "Daily backup job"
    }
  ]
}
```

### Trigger Types
SCHEDULE
MANUAL
API
FILE_WATCH
DEPENDENCY

---

## Output

The tool will show:

1. **Validation results** - Any data issues found
2. **Migration analysis** - What can be migrated
3. **Migration results** - Success/failure for each job

**Example output:**
```
🚀 Starting migration...

📄 Parsing: Found 4 jobs

🔍 Validating...
✅ All validations passed!

🔄 Analyzing triggers...
✅ Direct Migration: 3
❌ Cannot Migrate: 1

=== MIGRATION RESULT ===
✅ Success: 3
❌ Failed: 1
```

---

## Project Structure

```
src/main/java/com/example/migration/
├── dto/              # Data transfer objects
├── domain/           # Internal models
├── parser/           # JSON parsing
├── validator/        # Data validation
├── handler/          # Trigger handlers
├── service/          # Migration logic
├── client/           # Redwood API client
├── report/           # Result reporting
└── MigrationApplication.java
```

---

## Validation Rules

The tool checks for:

1. No duplicate job IDs
2. No circular dependencies
3. All dependencies reference existing jobs
4. Valid trigger configurations
5. No orphaned jobs (jobs with no way to trigger)

---

## Configuration

Edit the API URL in `MigrationApplication.java`:

```java
MigrationApplication app = new MigrationApplication("https://api.redwood.com");
```

---

## Testing

Run tests:
```bash
mvn test
```

---

## Dependencies

- **Lombok** - Reduces boilerplate code
- **Jackson** - JSON parsing
- **SLF4J + Logback** - Logging
- **JUnit 5** - Testing
- **Hibernate Validator** - Bean validation

---

## Troubleshooting

### "Circular dependency detected"
Your jobs have a dependency loop. Check the error message to see which jobs are involved.

### "Job X depends on non-existent job Y"
A dependency references a job ID that doesn't exist in the file.

### "No handler available for trigger type"
The trigger type is not supported by Redwood. These jobs need manual configuration.

---

## License

---

## Support
