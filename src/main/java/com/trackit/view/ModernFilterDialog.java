package com.trackit.view;

import com.trackit.model.ExpenseCategory;
import com.trackit.model.ExpenseSearchCriteria;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

/**
 * ModernFilterDialog - Interactive modal dialog for configuring multi-criteria
 * expense search queries (date range, category, amount, keyword).
 */
public class ModernFilterDialog extends JDialog {

    public static final double MAX_AMOUNT_LIMIT = 1000000.0;

    private final JTextField startDateField;
    private final JTextField endDateField;
    private final JComboBox<String> categoryCombo;
    private final JSpinner minAmountSpinner;
    private final JSpinner maxAmountSpinner;
    private final JTextField keywordField;
    private final Consumer<ExpenseSearchCriteria> onApplyCallback;

    private boolean applied = false;
    private ExpenseSearchCriteria currentCriteria;

    public ModernFilterDialog(Frame owner, Consumer<ExpenseSearchCriteria> onApplyCallback) {
        super(owner, "Filter Expenses", true);
        this.onApplyCallback = onApplyCallback;

        setLayout(new BorderLayout(16, 16));
        setSize(420, 480);
        setLocationRelativeTo(owner);
        setResizable(false);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 250, 252));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Filter Transactions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subLabel = new JLabel("Narrow down expenses across multiple parameters");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Fields Panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 12, 12));
        formPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        startDateField = new JTextField();
        startDateField.setToolTipText("Format: YYYY-MM-DD");
        endDateField = new JTextField();
        endDateField.setToolTipText("Format: YYYY-MM-DD");

        categoryCombo = new JComboBox<>();
        categoryCombo.addItem("ALL CATEGORIES");
        for (ExpenseCategory cat : ExpenseCategory.values()) {
            categoryCombo.addItem(cat.name());
        }

        minAmountSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, MAX_AMOUNT_LIMIT, 10.0));
        maxAmountSpinner = new JSpinner(new SpinnerNumberModel(MAX_AMOUNT_LIMIT, 0.0, MAX_AMOUNT_LIMIT, 10.0));
        keywordField = new JTextField();

        formPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        formPanel.add(startDateField);
        formPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
        formPanel.add(endDateField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryCombo);
        formPanel.add(new JLabel("Min Amount ($):"));
        formPanel.add(minAmountSpinner);
        formPanel.add(new JLabel("Max Amount ($):"));
        formPanel.add(maxAmountSpinner);
        formPanel.add(new JLabel("Description Keyword:"));
        formPanel.add(keywordField);

        add(formPanel, BorderLayout.CENTER);

        // Action Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 12, 20));

        JButton resetButton = new JButton("Reset");
        JButton cancelButton = new JButton("Cancel");
        JButton applyButton = new JButton("Apply Filter");
        applyButton.setBackground(new Color(79, 70, 229));
        applyButton.setForeground(Color.WHITE);
        applyButton.setFocusPainted(false);

        resetButton.addActionListener(e -> resetFields());
        cancelButton.addActionListener(e -> dispose());
        applyButton.addActionListener(e -> handleApply());

        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void resetFields() {
        startDateField.setText("");
        endDateField.setText("");
        categoryCombo.setSelectedIndex(0);
        minAmountSpinner.setValue(0.0);
        maxAmountSpinner.setValue(MAX_AMOUNT_LIMIT);
        keywordField.setText("");
        this.currentCriteria = new ExpenseSearchCriteria.Builder().build();
        if (onApplyCallback != null) {
            onApplyCallback.accept(this.currentCriteria);
        }
        applied = false;
        dispose();
    }

    private void handleApply() {
        ExpenseSearchCriteria.Builder builder = new ExpenseSearchCriteria.Builder();

        String startText = startDateField.getText().trim();
        if (!startText.isEmpty()) {
            try {
                builder.startDate(LocalDate.parse(startText));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Start Date format. Use YYYY-MM-DD.", 
                                              "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String endText = endDateField.getText().trim();
        if (!endText.isEmpty()) {
            try {
                builder.endDate(LocalDate.parse(endText));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid End Date format. Use YYYY-MM-DD.", 
                                              "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String selectedCat = (String) categoryCombo.getSelectedItem();
        if (selectedCat != null && !selectedCat.equals("ALL CATEGORIES")) {
            builder.category(selectedCat);
        }

        double min = ((Number) minAmountSpinner.getValue()).doubleValue();
        double max = ((Number) maxAmountSpinner.getValue()).doubleValue();
        if (min > max) {
            JOptionPane.showMessageDialog(this, "Minimum amount cannot be greater than Maximum amount.", 
                                          "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (min > 0.0) {
            builder.minAmount(min);
        }
        if (max < MAX_AMOUNT_LIMIT) {
            builder.maxAmount(max);
        }

        String rawKw = keywordField.getText();
        if (rawKw != null && !rawKw.trim().isEmpty()) {
            builder.keyword(rawKw.trim());
        }

        this.currentCriteria = builder.build();
        this.applied = true;

        if (onApplyCallback != null) {
            onApplyCallback.accept(this.currentCriteria);
        }
        dispose();
    }

    public boolean isApplied() {
        return applied;
    }

    public ExpenseSearchCriteria getCurrentCriteria() {
        return currentCriteria;
    }
}
