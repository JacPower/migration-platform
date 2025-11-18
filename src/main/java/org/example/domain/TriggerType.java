package org.example.domain;

/* *
 * This could be stored in a database or configuration file in a real-world application
 */
public enum TriggerType {
    SCHEDULE,
    MANUAL,
    API,
    FILE_WATCH,
    DATABASE_CDC,
    DEPENDENCY,
    APPROVAL,
    THRESHOLD,
    EVENT,
    UNKNOWN
}
