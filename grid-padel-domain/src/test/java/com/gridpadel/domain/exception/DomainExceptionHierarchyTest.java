package com.gridpadel.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DomainExceptionHierarchyTest {

    @Test
    void validationExceptionIsDomainException() {
        DomainException ex = new ValidationException("bad input", "field");
        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("bad input");
        assertThat(ex.field()).isEqualTo("field");
    }

    @Test
    void invalidOperationExceptionIsDomainException() {
        DomainException ex = new InvalidOperationException("not allowed");
        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex.getMessage()).isEqualTo("not allowed");
        assertThat(ex.field()).isNull();
    }

    @Test
    void entityNotFoundExceptionIsDomainException() {
        DomainException ex = new EntityNotFoundException("not found");
        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex.getMessage()).isEqualTo("not found");
    }

    @Test
    void validationExceptionWithoutFieldWorks() {
        ValidationException ex = new ValidationException("generic error");
        assertThat(ex.field()).isNull();
        assertThat(ex.getMessage()).isEqualTo("generic error");
    }

    @Test
    void domainExceptionsCanBeCaughtAsRuntimeException() {
        assertThatThrownBy(() -> { throw new ValidationException("test", "x"); })
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }
}
