package com.bookstore.application.saga;

import org.junit.jupiter.api.Test;
import com.bookstore.infrastructure.BusinessEventLogger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SagaOrchestratorTest {
    private final SagaOrchestrator orchestrator = new SagaOrchestrator(mockLogger());
    private static final String FIRST_STEP = "first-step";
    private static final String SECOND_STEP = "second-step";

    @Test
    void executesStepsInOrder() {
        StringBuilder execution = new StringBuilder();
        SagaStep<StringBuilder> first = step(FIRST_STEP, execution, "A", false);
        SagaStep<StringBuilder> second = step(SECOND_STEP, execution, "B", false);

        orchestrator.execute(execution, List.of(first, second));

        assertEquals("AB", execution.toString());
    }

    @Test
    void compensatesCompletedStepsInReverseOrderWhenExecutionFails() {
        StringBuilder execution = new StringBuilder();
        SagaStep<StringBuilder> first = step(FIRST_STEP, execution, "A", false);
        SagaStep<StringBuilder> second = step(SECOND_STEP, execution, "B", true);

        SagaExecutionException exception = assertThrows(SagaExecutionException.class,
                () -> orchestrator.execute(execution, List.of(first, second)));

        assertEquals("AA", execution.toString());
        assertEquals("Saga execution failed", exception.getMessage());
    }

    @Test
    void preservesCompensationFailureAsSuppressedCause() {
        AtomicBoolean compensated = new AtomicBoolean();
        SagaStep<StringBuilder> failingCompensation = new SagaStep<>() {
            @Override public String name() { return FIRST_STEP; }
            @Override public void execute(StringBuilder context) { context.append("A"); }
            @Override public void compensate(StringBuilder context) {
                compensated.set(true);
                throw new IllegalStateException("compensation failure");
            }
        };
        SagaStep<StringBuilder> failingExecution = step(SECOND_STEP, new StringBuilder(), "", true);

        SagaExecutionException exception = assertThrows(SagaExecutionException.class,
                () -> orchestrator.execute(new StringBuilder(),
                        List.of(failingCompensation, failingExecution)));

        assertEquals(true, compensated.get());
        assertEquals(1, exception.getCause().getSuppressed().length);
    }

    private BusinessEventLogger mockLogger() {
        return org.mockito.Mockito.mock(BusinessEventLogger.class);
    }

    private SagaStep<StringBuilder> step(String name, StringBuilder execution,
                                         String marker, boolean failExecution) {
        return new SagaStep<>() {
            @Override public String name() { return name; }
            @Override public void execute(StringBuilder context) {
                if (failExecution) {
                    throw new IllegalStateException("execution failure");
                }
                context.append(marker);
            }
            @Override public void compensate(StringBuilder context) {
                context.append(marker);
            }
        };
    }
}
