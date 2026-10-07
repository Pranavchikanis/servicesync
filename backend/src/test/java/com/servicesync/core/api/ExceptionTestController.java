package com.servicesync.core.api;

import com.servicesync.core.exception.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-exceptions")
public class ExceptionTestController {

    public static class TestRequest {
        @NotBlank(message = "Field cannot be blank")
        private String requiredField;

        public String getRequiredField() { return requiredField; }
        public void setRequiredField(String requiredField) { this.requiredField = requiredField; }
    }

    @PostMapping("/validation")
    public void validationError(@Valid @RequestBody TestRequest request) {
        // Will throw MethodArgumentNotValidException if field is blank
    }

    @GetMapping("/not-found")
    public void notFound() {
        throw new ResourceNotFoundException("Not found test");
    }

    @GetMapping("/forbidden")
    public void forbidden() {
        throw new UnauthorizedActionException("Forbidden test");
    }

    @GetMapping("/conflict-state")
    public void conflictState() {
        throw new InvalidTicketStateException("Bad state");
    }

    @GetMapping("/conflict-stock")
    public void conflictStock() {
        throw new InsufficientInventoryException("Not enough stock");
    }

    @GetMapping("/conflict-lock")
    public void conflictLock() {
        throw new ObjectOptimisticLockingFailureException(Object.class, "lock-id");
    }

    @GetMapping("/conflict-data")
    public void conflictData() {
        throw new DataIntegrityViolationException("Constraint violation");
    }

    @GetMapping("/internal")
    public void internal() {
        throw new RuntimeException("Unexpected error");
    }
}
