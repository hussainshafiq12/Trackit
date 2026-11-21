package com.trackit.model;

import com.trackit.exception.ValidationException;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * ExpenseSearchCriteria - Immutable domain model encapsulating multi-criteria
 * filtering parameters for financial transaction searches.
 * Implements the Builder Pattern for flexible query formulation.
 */
public class ExpenseSearchCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String category;
    private final double minAmount;
    private final double maxAmount;
    private final String keyword;

    private ExpenseSearchCriteria(Builder builder) {
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.category = builder.category != null && !builder.category.trim().isEmpty() 
                        ? builder.category.trim() : null;
        this.minAmount = builder.minAmount;
        this.maxAmount = builder.maxAmount;
        this.keyword = builder.keyword != null && !builder.keyword.trim().isEmpty() 
                       ? builder.keyword.trim() : null;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getCategory() {
        return category;
    }

    public double getMinAmount() {
        return minAmount;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean hasActiveFilters() {
        return startDate != null 
                || endDate != null 
                || category != null 
                || minAmount > 0.0 
                || maxAmount < Double.MAX_VALUE 
                || keyword != null;
    }

    /**
     * Fluent Builder for constructing validated ExpenseSearchCriteria instances.
     */
    public static class Builder {
        private LocalDate startDate;
        private LocalDate endDate;
        private String category;
        private double minAmount = 0.0;
        private double maxAmount = Double.MAX_VALUE;
        private String keyword;

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder minAmount(double minAmount) {
            this.minAmount = minAmount;
            return this;
        }

        public Builder maxAmount(double maxAmount) {
            this.maxAmount = maxAmount;
            return this;
        }

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public ExpenseSearchCriteria build() {
            if (minAmount < 0.0) {
                throw new ValidationException("Minimum amount cannot be negative");
            }
            if (minAmount > maxAmount) {
                throw new ValidationException("Minimum amount cannot exceed maximum amount");
            }
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new ValidationException("Start date cannot be after end date");
            }
            return new ExpenseSearchCriteria(this);
        }
    }
}
