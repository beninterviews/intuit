package com.intuit.cg.backendtechassessment.constraints;

import com.google.inject.Injector;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GuiceConstraintValidatorFactory implements ConstraintValidatorFactory {
    private final Injector injector;

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
        return injector.getInstance(key);
    }

    @Override
    public void releaseInstance(final ConstraintValidator<?, ?> instance) {
        // Nothing to do here.
    }
}
