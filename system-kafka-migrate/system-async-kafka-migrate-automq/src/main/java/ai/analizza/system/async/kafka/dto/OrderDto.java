package ai.analizza.system.async.kafka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class OrderDto {

    @NotBlank(message = "Product name must not be blank")
    private String productName;

    @NotNull(message = "Product price must be provided")
    @Positive(message = "Product price must be positive")
    private BigDecimal productPrice;

    @Positive(message = "Product amount must be positive")
    private int productAmount;

    // Getters and setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public int getProductAmount() {
        return productAmount;
    }

    public void setProductAmount(int productAmount) {
        this.productAmount = productAmount;
    }
}
