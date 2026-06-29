package ai.analizza.system.async.kafka.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validDtoHasNoViolations() {
        OrderDto dto = new OrderDto();
        dto.setProductName("Widget");
        dto.setProductPrice(BigDecimal.valueOf(9.99));
        dto.setProductAmount(3);

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "A valid DTO should have no violations");
    }

    @Test
    void validationFailsWhenBlankName() {
        OrderDto dto = new OrderDto();
        dto.setProductName("");
        dto.setProductPrice(BigDecimal.valueOf(10));
        dto.setProductAmount(1);

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Blank product name should trigger a violation");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("productName")));
    }

    @Test
    void validationFailsWhenNullName() {
        OrderDto dto = new OrderDto();
        dto.setProductName(null);
        dto.setProductPrice(BigDecimal.TEN);
        dto.setProductAmount(1);

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("productName")));
    }

    @Test
    void validationFailsWhenNullPrice() {
        OrderDto dto = new OrderDto();
        dto.setProductName("Widget");
        dto.setProductPrice(null);
        dto.setProductAmount(1);

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("productPrice")));
    }

    @Test
    void validationFailsWhenNegativePrice() {
        OrderDto dto = new OrderDto();
        dto.setProductName("Widget");
        dto.setProductPrice(BigDecimal.valueOf(-5));
        dto.setProductAmount(1);

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("productPrice")));
    }

    @Test
    void validationFailsWhenZeroPrice() {
        OrderDto dto = new OrderDto();
        dto.setProductName("Widget");
        dto.setProductPrice(BigDecimal.ZERO);
        dto.setProductAmount(1);

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("productPrice")));
    }

    @Test
    void validationFailsWhenZeroAmount() {
        OrderDto dto = new OrderDto();
        dto.setProductName("Widget");
        dto.setProductPrice(BigDecimal.TEN);
        dto.setProductAmount(0);

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("productAmount")));
    }

    @Test
    void validationFailsWhenNegativeAmount() {
        OrderDto dto = new OrderDto();
        dto.setProductName("Widget");
        dto.setProductPrice(BigDecimal.TEN);
        dto.setProductAmount(-1);

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("productAmount")));
    }
}
