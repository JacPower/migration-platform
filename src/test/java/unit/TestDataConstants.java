package unit;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestDataConstants {

    public static final Path ACTUAL_OUTPUT_DIR = Paths.get("/Users/ninja/Desktop/test/output");

    public static final String SINGLE_ORACLE_BACKUP_JOB = """
            {
              "jobs": [
                {
                  "jobId": 2001,
                  "jobName": "Backup_Production_Databases_Oracle",
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
                    "maxRuntimeMinutes": 120,
                    "excludeHolidays": false
                  },
                  "notes": "Standard daily backup of production Oracle databases"
                }
              ]
            }
            """;
    public static final String SAP_TRANSFORM_WITH_DEPENDENCY = """
            {
              "jobs": [
                {
                  "jobId": 3100,
                  "jobName": "Aggregate_Stuttgart_Assembly_Data",
                  "jobType": "TRANSFORM",
                  "system": "SAP",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 3 * * *",
                    "timezone": "Europe/Berlin"
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 2001,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 8,
                    "maxRuntimeMinutes": 60,
                    "excludeHolidays": false
                  },
                  "notes": "High priority - aggregates assembly line data from Stuttgart plant"
                }
              ]
            }
            """;
    public static final String SIMPLE_DEPENDENCY_CHAIN = """
            {
              "jobs": [
                {
                  "jobId": 2001,
                  "jobName": "Backup_Production_Databases_Oracle",
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
                    "maxRuntimeMinutes": 120,
                    "excludeHolidays": false
                  },
                  "notes": "Standard daily backup of production Oracle databases"
                },
                {
                  "jobId": 3100,
                  "jobName": "Aggregate_Stuttgart_Assembly_Data",
                  "jobType": "TRANSFORM",
                  "system": "SAP",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 3 * * *",
                    "timezone": "Europe/Berlin"
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 2001,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 8,
                    "maxRuntimeMinutes": 60,
                    "excludeHolidays": false
                  },
                  "notes": "High priority - aggregates assembly line data from Stuttgart plant"
                },
                {
                  "jobId": 3200,
                  "jobName": "Reconcile_Parts_Inventory_SAP",
                  "jobType": "RECONCILE",
                  "system": "SAP",
                  "trigger": {
                    "type": "DEPENDENCY",
                    "upstreamJobId": 3100
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 3100,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 7,
                    "maxRuntimeMinutes": 45,
                    "excludeHolidays": false
                  },
                  "notes": "Reconciles parts inventory with assembly consumption"
                }
              ]
            }
            """;
    public static final String FINANCIAL_REPORTING_CHAIN = """
            {
              "jobs": [
                {
                  "jobId": 4100,
                  "jobName": "Consolidate_Financials_SAP",
                  "jobType": "REPORT",
                  "system": "SAP",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 5 * * *",
                    "timezone": "UTC"
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 3200,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 10,
                    "maxRuntimeMinutes": 30,
                    "excludeHolidays": true
                  },
                  "notes": "Critical path - daily financial consolidation report. Hold on bank holidays."
                },
                {
                  "jobId": 4200,
                  "jobName": "Generate_Daily_Sales_Report",
                  "jobType": "REPORT",
                  "system": "SAP",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 6 * * *",
                    "timezone": "America/New_York"
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 4100,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 9,
                    "maxRuntimeMinutes": 20,
                    "excludeHolidays": true
                  },
                  "notes": "Daily sales report for management review"
                }
              ]
            }
            """;
    public static final String FILE_WATCH_TRIGGER_JOB = """
            {
              "jobs": [
                {
                  "jobId": 5001,
                  "jobName": "Process_Incoming_Orders",
                  "jobType": "PROCESS",
                  "system": "SAP",
                  "trigger": {
                    "type": "FILE_WATCH",
                    "watchPath": "/data/incoming/orders",
                    "filePattern": "*.csv"
                  },
                  "dependencies": [],
                  "executionConstraints": {
                    "priority": 9,
                    "maxRuntimeMinutes": 15,
                    "excludeHolidays": false
                  },
                  "notes": "Processes incoming order files from EDI partners"
                }
              ]
            }
            """;
    public static final String MANUAL_TRIGGER_JOB = """
            {
              "jobs": [
                {
                  "jobId": 5002,
                  "jobName": "Manual_Data_Correction",
                  "jobType": "MAINTENANCE",
                  "system": "ORACLE",
                  "trigger": {
                    "type": "MANUAL"
                  },
                  "dependencies": [],
                  "executionConstraints": {
                    "priority": 3,
                    "maxRuntimeMinutes": 60,
                    "excludeHolidays": false
                  },
                  "notes": "Manual job for data corrections - run on demand by DBA team"
                }
              ]
            }
            """;
    public static final String API_WEBHOOK_TRIGGER_JOB = """
            {
              "jobs": [
                {
                  "jobId": 5003,
                  "jobName": "API_Webhook_Integration",
                  "jobType": "INTEGRATION",
                  "system": "CUSTOM",
                  "trigger": {
                    "type": "API",
                    "eventSource": "external_webhook",
                    "eventType": "order_received"
                  },
                  "dependencies": [],
                  "executionConstraints": {
                    "priority": 8,
                    "maxRuntimeMinutes": 10,
                    "excludeHolidays": false
                  },
                  "notes": "Triggered by external webhook from partner system"
                }
              ]
            }
            """;
    public static final String WEEKLY_MAINTENANCE_JOB = """
            {
              "jobs": [
                {
                  "jobId": 6001,
                  "jobName": "Weekly_Archive_Cleanup",
                  "jobType": "MAINTENANCE",
                  "system": "ORACLE",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 1 * * 0",
                    "timezone": "UTC"
                  },
                  "dependencies": [],
                  "executionConstraints": {
                    "priority": 2,
                    "maxRuntimeMinutes": 180,
                    "excludeHolidays": false
                  },
                  "notes": "Weekly cleanup of archived data older than 90 days"
                }
              ]
            }
            """;
    public static final String MONTHLY_REPORT_WITH_MULTIPLE_DEPENDENCIES = """
            {
              "jobs": [
                {
                  "jobId": 6002,
                  "jobName": "Monthly_Compliance_Report",
                  "jobType": "REPORT",
                  "system": "SAP",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 4 1 * *",
                    "timezone": "UTC"
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 4100,
                      "requiredStatus": "SUCCESS"
                    },
                    {
                      "dependsOnJobId": 4200,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 10,
                    "maxRuntimeMinutes": 60,
                    "excludeHolidays": false
                  },
                  "notes": "Monthly compliance report for regulatory submission"
                }
              ]
            }
            """;
    public static final String COMPLETE_WORKFLOW = """
            {
              "jobs": [
                {
                  "jobId": 2001,
                  "jobName": "Backup_Production_Databases_Oracle",
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
                    "maxRuntimeMinutes": 120,
                    "excludeHolidays": false
                  },
                  "notes": "Standard daily backup of production Oracle databases"
                },
                {
                  "jobId": 3100,
                  "jobName": "Aggregate_Stuttgart_Assembly_Data",
                  "jobType": "TRANSFORM",
                  "system": "SAP",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 3 * * *",
                    "timezone": "Europe/Berlin"
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 2001,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 8,
                    "maxRuntimeMinutes": 60,
                    "excludeHolidays": false
                  },
                  "notes": "High priority - aggregates assembly line data from Stuttgart plant"
                },
                {
                  "jobId": 3200,
                  "jobName": "Reconcile_Parts_Inventory_SAP",
                  "jobType": "RECONCILE",
                  "system": "SAP",
                  "trigger": {
                    "type": "DEPENDENCY",
                    "upstreamJobId": 3100
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 3100,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 7,
                    "maxRuntimeMinutes": 45,
                    "excludeHolidays": false
                  },
                  "notes": "Reconciles parts inventory with assembly consumption"
                },
                {
                  "jobId": 4100,
                  "jobName": "Consolidate_Financials_SAP",
                  "jobType": "REPORT",
                  "system": "SAP",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 5 * * *",
                    "timezone": "UTC"
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 3200,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 10,
                    "maxRuntimeMinutes": 30,
                    "excludeHolidays": true
                  },
                  "notes": "Critical path - daily financial consolidation report. Hold on bank holidays."
                },
                {
                  "jobId": 4200,
                  "jobName": "Generate_Daily_Sales_Report",
                  "jobType": "REPORT",
                  "system": "SAP",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 6 * * *",
                    "timezone": "America/New_York"
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 4100,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 9,
                    "maxRuntimeMinutes": 20,
                    "excludeHolidays": true
                  },
                  "notes": "Daily sales report for management review"
                },
                {
                  "jobId": 5001,
                  "jobName": "Process_Incoming_Orders",
                  "jobType": "PROCESS",
                  "system": "SAP",
                  "trigger": {
                    "type": "FILE_WATCH",
                    "watchPath": "/data/incoming/orders",
                    "filePattern": "*.csv"
                  },
                  "dependencies": [],
                  "executionConstraints": {
                    "priority": 9,
                    "maxRuntimeMinutes": 15,
                    "excludeHolidays": false
                  },
                  "notes": "Processes incoming order files from EDI partners"
                },
                {
                  "jobId": 5002,
                  "jobName": "Manual_Data_Correction",
                  "jobType": "MAINTENANCE",
                  "system": "ORACLE",
                  "trigger": {
                    "type": "MANUAL"
                  },
                  "dependencies": [],
                  "executionConstraints": {
                    "priority": 3,
                    "maxRuntimeMinutes": 60,
                    "excludeHolidays": false
                  },
                  "notes": "Manual job for data corrections - run on demand by DBA team"
                },
                {
                  "jobId": 5003,
                  "jobName": "API_Webhook_Integration",
                  "jobType": "INTEGRATION",
                  "system": "CUSTOM",
                  "trigger": {
                    "type": "API",
                    "eventSource": "external_webhook",
                    "eventType": "order_received"
                  },
                  "dependencies": [],
                  "executionConstraints": {
                    "priority": 8,
                    "maxRuntimeMinutes": 10,
                    "excludeHolidays": false
                  },
                  "notes": "Triggered by external webhook from partner system"
                },
                {
                  "jobId": 6001,
                  "jobName": "Weekly_Archive_Cleanup",
                  "jobType": "MAINTENANCE",
                  "system": "ORACLE",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 1 * * 0",
                    "timezone": "UTC"
                  },
                  "dependencies": [],
                  "executionConstraints": {
                    "priority": 2,
                    "maxRuntimeMinutes": 180,
                    "excludeHolidays": false
                  },
                  "notes": "Weekly cleanup of archived data older than 90 days"
                },
                {
                  "jobId": 6002,
                  "jobName": "Monthly_Compliance_Report",
                  "jobType": "REPORT",
                  "system": "SAP",
                  "trigger": {
                    "type": "SCHEDULE",
                    "cronExpression": "0 4 1 * *",
                    "timezone": "UTC"
                  },
                  "dependencies": [
                    {
                      "dependsOnJobId": 4100,
                      "requiredStatus": "SUCCESS"
                    },
                    {
                      "dependsOnJobId": 4200,
                      "requiredStatus": "SUCCESS"
                    }
                  ],
                  "executionConstraints": {
                    "priority": 10,
                    "maxRuntimeMinutes": 60,
                    "excludeHolidays": false
                  },
                  "notes": "Monthly compliance report for regulatory submission"
                }
              ]
            }
            """;
    public static final String EMPTY_JOBS_ARRAY = """
            {
              "jobs": []
            }
            """;

    private TestDataConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }



    public static String createSimpleJob(int jobId, String jobName, String jobType, String system) {
        return String.format("""
                {
                  "jobs": [
                    {
                      "jobId": %d,
                      "jobName": "%s",
                      "jobType": "%s",
                      "system": "%s",
                      "trigger": {
                        "type": "SCHEDULE",
                        "cronExpression": "0 0 * * *",
                        "timezone": "UTC"
                      },
                      "dependencies": [],
                      "executionConstraints": {
                        "priority": 5,
                        "maxRuntimeMinutes": 60,
                        "excludeHolidays": false
                      },
                      "notes": "Test job %d"
                    }
                  ]
                }
                """, jobId, jobName, jobType, system, jobId);
    }



    public static final class FileNames {
        public static final String ORACLE_BACKUP = "oracle_backup_export.json";
        public static final String SAP_TRANSFORM = "sap_transform_export.json";
        public static final String DEPENDENCY_CHAIN = "dependency_chain_export.json";
        public static final String FINANCIAL_REPORTING = "financial_reporting_export.json";
        public static final String FILE_WATCH_TRIGGER = "file_watch_trigger_export.json";
        public static final String MANUAL_TRIGGER = "manual_trigger_export.json";
        public static final String API_WEBHOOK = "api_webhook_export.json";
        public static final String WEEKLY_MAINTENANCE = "weekly_maintenance_export.json";
        public static final String MONTHLY_COMPLIANCE = "monthly_compliance_export.json";
        public static final String COMPLETE_WORKFLOW = "complete_workflow_export.json";



        private FileNames() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }

    public static final class JobIds {
        public static final int ORACLE_BACKUP = 2001;
        public static final int SAP_AGGREGATE = 3100;
        public static final int SAP_RECONCILE = 3200;
        public static final int FINANCIAL_CONSOLIDATION = 4100;
        public static final int SALES_REPORT = 4200;
        public static final int PROCESS_ORDERS = 5001;
        public static final int MANUAL_CORRECTION = 5002;
        public static final int API_WEBHOOK = 5003;
        public static final int WEEKLY_CLEANUP = 6001;
        public static final int MONTHLY_COMPLIANCE = 6002;



        private JobIds() {
            throw new AssertionError("Cannot instantiate constants class");
        }
    }
}
