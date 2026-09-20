package com.bookstore.application.saga;

import com.bookstore.infrastructure.SagaLogEvents;
import com.bookstore.infrastructure.BusinessEventLogger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a local orchestration workflow and compensates completed steps in reverse order.
 * Database transaction boundaries remain owned by the application use case.
 */
@Component
public class SagaOrchestrator {
    private final BusinessEventLogger eventLogger;

    public SagaOrchestrator(BusinessEventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    public <C> void execute(C context, List<SagaStep<C>> steps) {
        List<SagaStep<C>> completedSteps = new ArrayList<>();
        try {
            for (SagaStep<C> step : steps) {
                eventLogger.sagaStep(SagaLogEvents.STEP_STARTED,
                        context.getClass().getSimpleName(), step.name());
                step.execute(context);
                completedSteps.add(step);
                eventLogger.sagaStep(SagaLogEvents.STEP_COMPLETED,
                        context.getClass().getSimpleName(), step.name());
            }
        } catch (RuntimeException failure) {
            compensate(context, completedSteps, failure);
            throw new SagaExecutionException("Saga execution failed", failure);
        }
    }

    private <C> void compensate(C context, List<SagaStep<C>> completedSteps, RuntimeException failure) {
        for (int index = completedSteps.size() - 1; index >= 0; index--) {
            SagaStep<C> step = completedSteps.get(index);
            try {
                eventLogger.sagaStep(SagaLogEvents.COMPENSATION_STARTED,
                        context.getClass().getSimpleName(), step.name());
                step.compensate(context);
                eventLogger.sagaStep(SagaLogEvents.COMPENSATION_COMPLETED,
                        context.getClass().getSimpleName(), step.name());
            } catch (RuntimeException compensationFailure) {
                eventLogger.sagaCompensationFailed(
                        context.getClass().getSimpleName(), step.name(), compensationFailure);
                failure.addSuppressed(compensationFailure);
            }
        }
    }
}
