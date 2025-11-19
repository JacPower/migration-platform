# Migration Platform

A high-performance Java tool for migrating job scheduling data from competitor systems to RunMyJobs format.

[![Java](https://img.shields.io/badge/Java-11%2B-orange)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-blue)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

---

## Overview

The Migration Platform automates the conversion of job scheduling configurations from competitor systems into Redwood's format. Instead of manually recreating hundreds of scheduled jobs—a process that takes weeks and introduces human errors—this tool completes migrations in minutes with full validation and detailed reporting.

### Key Features

- **Automated Migration** - Converts job definitions with a single command
- **Data Validation** - Detects circular dependencies, missing references, and duplicates
- **Concurrent Processing** - Processes multiple files in parallel for large datasets
- **Comprehensive Reporting** - Categorizes jobs by migration readiness
- **Extensible Architecture** - Add new trigger types without modifying existing code

---

## Quick Start

### Prerequisites

| Requirement | Minimum Version | Verification |
|-------------|-----------------|--------------|
| Java JDK | 11+ | `java -version` |
| Maven | 3.6+ | `mvn -version` |

### Installation

```bash
# Clone repository
git clone https://github.com/JacPower/migration-platform.git
cd migration-platform

# Build
mvn clean package -DskipTests

# Verify
ls -la target/migration-platform-*.jar
```

### Usage

```bash
# Single file
java -jar target/migration-platform-1.0.jar /path/to/export.json

# Multiple files (processed concurrently)
java -jar target/migration-platform-1.0.jar file1.json file2.json file3.json

# With increased memory for large datasets
java -Xmx4g -jar target/migration-platform-1.0.jar /path/to/large-export.json
```

---

## Documentation

### Command Reference

```bash
java [JVM_OPTIONS] -jar migration-platform-1.0.jar <file1.json> [file2.json] ...
```

| Argument | Description | Required |
|----------|-------------|----------|
| `file1.json` | Path to competitor export file | Yes |
| `file2.json ...` | Additional export files | No |

| JVM Option | Description | Example |
|------------|-------------|---------|
| `-Xmx` | Maximum heap size | `-Xmx4g` |
| `-Xms` | Initial heap size | `-Xms512m` |
| `-Dlogging.level.org.example` | Log level | `=DEBUG` |

### Memory Guidelines

| File Size | Jobs (approx) | Recommended Heap |
|-----------|---------------|------------------|
| < 10 MB | < 1,000 | Default (512 MB) |
| 10-50 MB | 1,000 - 5,000 | `-Xmx2g` |
| 50-200 MB | 5,000 - 20,000 | `-Xmx4g` |
| > 200 MB | > 20,000 | `-Xmx8g` |

---

## Architecture

### Processing Pipeline

```
Input (JSON) → Parse → Validate → Analyze → Transform → Output (RunMyJobs)
```

1. **Parse** - Reads competitor export files and extracts job definitions
2. **Validate** - Checks for circular dependencies, missing references, duplicates
3. **Analyze** - Categorizes jobs by migration readiness
4. **Transform** - Converts valid jobs to Redwood format
5. **Report** - Outputs results and issues requiring attention

### Supported Trigger Types

| Trigger Type | Migration Support |
|--------------|-------------------|
| SCHEDULE | ✅ Direct migration |
| MANUAL | ✅ Direct migration |
| API | ✅ Direct migration |
| DEPENDENCY | ✅ Direct migration |
| FILE_WATCH | ⚠️ Converted to polling |
| APPROVAL | ❌ Manual configuration required |
| DATABASE_CDC | ❌ Manual configuration required |

### Project Structure

```
migration-platform/
├── src/main/java/org/example/
│   ├── Main.java                    # Entry point
│   ├── config/                      # Configuration
│   │   └── MigrationDependencies.java
│   ├── orchestrator/                # Pipeline coordination
│   │   └── MigrationOrchestrator.java
│   ├── parser/                      # Data parsing
│   │   ├── DataParser.java
│   │   ├── CompetitorDataParser.java
│   │   └── ConcurrentFileParser.java
│   ├── validator/                   # Data validation
│   │   ├── ExportValidator.java
│   │   ├── JobValidator.java
│   │   └── ConcurrentJobValidator.java
│   ├── service/                     # Migration logic
│   │   ├── TriggerMigrationService.java
│   │   ├── TriggerHandler.java
│   │   └── TriggerType.java
│   ├── handler/                     # Trigger handlers
│   │   ├── ScheduleTriggerHandler.java
│   │   ├── ManualTriggerHandler.java
│   │   ├── ApiTriggerHandler.java
│   │   ├── FileWatchTriggerHandler.java
│   │   └── DependencyTriggerHandler.java
│   ├── dto/                         # Data transfer objects
│   ├── report/                      # Reporting
│   └── exception/                   # Custom exceptions
└── src/test/java/                   # Unit tests
```

---

## Validation & Error Handling

### Validations Performed

- **Duplicate Detection** - Identifies duplicate job IDs
- **Circular Dependencies** - Detects impossible dependency loops (A→B→C→A)
- **Missing References** - Flags jobs depending on non-existent jobs
- **Trigger Validation** - Validates required fields per trigger type

### Sample Output

```
=== TRIGGER MIGRATION ANALYSIS ===
Total Triggers: 150
Direct Migration: 142
With Workarounds: 5
Cannot Migrate: 3

=== MIGRATION RESULT ===
Total: 150
Success: 147
Failed: 3

Failures:
  - Job_101: No handler for trigger type: APPROVAL
  - Job_205: Validation failed: Missing cron expression
  - Job_308: Circular dependency detected
```

---

## Troubleshooting

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `Unable to access jarfile` | Wrong JAR path | Verify: `ls -la target/*.jar` |
| `File not found` | Invalid file path | Check: `ls -la /path/to/file.json` |
| `OutOfMemoryError` | Insufficient heap | Increase: `-Xmx4g` |
| `Circular dependency` | Data quality issue | Review source data |
| `No TriggerHandlers found` | Corrupted JAR | Rebuild: `mvn clean package` |

### Diagnostic Commands

```bash
# Verify JAR integrity
jar -tf target/migration-platform-1.0.jar | head

# Validate input JSON
jq '.' /path/to/export.json > /dev/null && echo "Valid"

# Monitor memory usage
ps aux | grep java | awk '{print $4 "% memory"}'

# View recent logs
tail -100 logs/migration.log

# Find errors in logs
grep "Exception" logs/migration.log
```

---

## Development

### Building from Source

```bash
# Full build with tests
mvn clean install

# Build without tests
mvn clean package -DskipTests

# Run tests only
mvn test
```

### Adding New Trigger Types

1. Create a new handler in `org.example.handler`:

```java
@Slf4j
public class NewTriggerHandler implements TriggerHandler {
    
    @Override
    public TriggerType getSupportedType() {
        return TriggerType.NEW_TYPE;
    }
    
    @Override
    public boolean canHandle(Trigger trigger) {
        return trigger.getType() == TriggerType.NEW_TYPE;
    }
    
    @Override
    public ValidationResult validate(Trigger trigger) {
        // Validation logic
    }
    
    @Override
    public RedwoodJobDto migrate(Trigger trigger) {
        // Migration logic
    }
    
    @Override
    public String getDescription() {
        return "Description of migration strategy";
    }
}
```

2. The handler is automatically registered via reflection—no other changes needed.

### Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=TriggerMigrationServiceTest

# With coverage report
mvn test jacoco:report
```

---

## Design Principles

### SOLID Compliance

- **Single Responsibility** - Each class has one focused purpose
- **Open/Closed** - Add handlers without modifying existing code
- **Liskov Substitution** - All handlers are interchangeable
- **Interface Segregation** - Focused, cohesive interfaces
- **Dependency Inversion** - Dependencies injected via constructors

### Performance Optimizations

- **Concurrent File Parsing** - Multiple files processed in parallel
- **Concurrent Validation** - Large batches validated across threads
- **Efficient Data Structures** - HashMaps for O(1) lookups
- **Stream Processing** - Memory-efficient transformations

---

### Time Savings

Manual migration of 500 jobs: **2-3 weeks**  
Automated migration: **< 10 minutes**

### Risk Mitigation

- Eliminates manual data entry errors
- Catches data quality issues before go-live
- Ensures complete data preservation
- Provides audit trail of migration decisions

### Client Deliverables

1. Console summary with success/failure counts
2. Detailed migration analysis report
3. Ready-to-import Redwood job configurations
4. Error report for items requiring attention

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-handler`)
3. Commit changes (`git commit -am 'Add new trigger handler'`)
4. Push to branch (`git push origin feature/new-handler`)
5. Open a Pull Request

### Code Standards

- Follow existing code style
- Add unit tests for new features
- Update documentation as needed
- Ensure all tests pass before PR

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Support

| Issue Type | Contact |
|------------|---------|
| Bug Reports | Create GitHub Issue |
| Feature Requests | Create GitHub Issue |
| Urgent Issues | oncall@company.com |

---

## Acknowledgments

- Contributors and reviewers