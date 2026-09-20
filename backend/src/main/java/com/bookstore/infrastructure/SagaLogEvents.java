package com.bookstore.infrastructure;

public final class SagaLogEvents {
    public static final String STEP_STARTED = "saga_step_started";
    public static final String STEP_COMPLETED = "saga_step_completed";
    public static final String COMPENSATION_STARTED = "saga_compensation_started";
    public static final String COMPENSATION_COMPLETED = "saga_compensation_completed";
    public static final String COMPENSATION_FAILED = "saga_compensation_failed";

    private SagaLogEvents() {
    }
}
