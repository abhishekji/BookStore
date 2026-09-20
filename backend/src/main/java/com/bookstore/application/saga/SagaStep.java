package com.bookstore.application.saga;

/**
 * One forward action and its compensating action in a business workflow.
 *
 * @param <C> workflow context shared by the steps
 */
public interface SagaStep<C> {
    String name();

    void execute(C context);

    void compensate(C context);
}
