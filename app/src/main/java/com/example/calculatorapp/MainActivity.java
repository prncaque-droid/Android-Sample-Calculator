package com.example.calculatorapp;

import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.BigDecimal;
import java.math.MathContext;

public class MainActivity extends AppCompatActivity {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    private EditText display;
    private String entry = "0";
    private BigDecimal accumulator;
    private String pendingOperator;
    private boolean replaceEntry = true;
    private boolean showingError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        display = findViewById(R.id.editTextNumber);
        display.setShowSoftInputOnFocus(false);
        display.setKeyListener(null);
        display.setTextIsSelectable(false);
        display.setCursorVisible(false);
        updateDisplay();

        bindDigit(R.id.Button0, "0");
        bindDigit(R.id.Button1, "1");
        bindDigit(R.id.Button2, "2");
        bindDigit(R.id.Button3, "3");
        bindDigit(R.id.Button4, "4");
        bindDigit(R.id.Button5, "5");
        bindDigit(R.id.Button6, "6");
        bindDigit(R.id.Button7, "7");
        bindDigit(R.id.Button8, "8");
        bindDigit(R.id.Button9, "9");

        findViewById(R.id.DecimalButton).setOnClickListener(view -> appendDecimal());
        findViewById(R.id.plusButton22).setOnClickListener(view -> chooseOperator("+"));
        findViewById(R.id.MinusButton).setOnClickListener(view -> chooseOperator("-"));
        findViewById(R.id.MultiplyButton).setOnClickListener(view -> chooseOperator("*"));
        findViewById(R.id.DivideButton).setOnClickListener(view -> chooseOperator("/"));
        findViewById(R.id.EqualsButton).setOnClickListener(view -> calculateResult());
        findViewById(R.id.clearButton).setOnClickListener(view -> clear());
        findViewById(R.id.DeleteButton).setOnClickListener(view -> deleteLastCharacter());
        findViewById(R.id.percentButton).setOnClickListener(view -> applyPercent());
    }

    private void bindDigit(int buttonId, String digit) {
        findViewById(buttonId).setOnClickListener(view -> appendDigit(digit));
    }

    private void appendDigit(String digit) {
        if (showingError) {
            clear();
        }
        if (replaceEntry || "0".equals(entry)) {
            entry = digit;
            replaceEntry = false;
        } else {
            entry += digit;
        }
        updateDisplay();
    }

    private void appendDecimal() {
        if (showingError) {
            clear();
        }
        if (replaceEntry) {
            entry = "0.";
            replaceEntry = false;
        } else if (!entry.contains(".")) {
            entry += ".";
        }
        updateDisplay();
    }

    private void chooseOperator(String operator) {
        if (showingError) {
            return;
        }
        if (pendingOperator != null && !replaceEntry) {
            if (!applyPendingOperation(new BigDecimal(entry))) {
                return;
            }
        } else if (accumulator == null) {
            accumulator = new BigDecimal(entry);
        }
        pendingOperator = operator;
        replaceEntry = true;
        updateDisplay();
    }

    private void calculateResult() {
        if (showingError || pendingOperator == null) {
            return;
        }
        BigDecimal rightOperand = replaceEntry ? accumulator : new BigDecimal(entry);
        if (applyPendingOperation(rightOperand)) {
            pendingOperator = null;
            accumulator = null;
            replaceEntry = true;
            updateDisplay();
        }
    }

    private boolean applyPendingOperation(BigDecimal rightOperand) {
        BigDecimal leftOperand = accumulator == null ? new BigDecimal(entry) : accumulator;
        switch (pendingOperator) {
            case "+":
                accumulator = leftOperand.add(rightOperand, MATH_CONTEXT);
                break;
            case "-":
                accumulator = leftOperand.subtract(rightOperand, MATH_CONTEXT);
                break;
            case "*":
                accumulator = leftOperand.multiply(rightOperand, MATH_CONTEXT);
                break;
            case "/":
                if (rightOperand.compareTo(BigDecimal.ZERO) == 0) {
                    showError();
                    return false;
                }
                accumulator = leftOperand.divide(rightOperand, MATH_CONTEXT);
                break;
            default:
                return false;
        }
        entry = formatNumber(accumulator);
        updateDisplay();
        return true;
    }

    private void applyPercent() {
        if (showingError) {
            return;
        }
        entry = formatNumber(new BigDecimal(entry).divide(BigDecimal.valueOf(100), MATH_CONTEXT));
        replaceEntry = false;
        updateDisplay();
    }

    private void deleteLastCharacter() {
        if (showingError) {
            clear();
            return;
        }
        if (replaceEntry) {
            entry = "0";
            replaceEntry = false;
        } else if (entry.length() <= 1 || (entry.length() == 2 && entry.startsWith("-"))) {
            entry = "0";
        } else {
            entry = entry.substring(0, entry.length() - 1);
        }
        updateDisplay();
    }

    private void clear() {
        entry = "0";
        accumulator = null;
        pendingOperator = null;
        replaceEntry = true;
        showingError = false;
        updateDisplay();
    }

    private void showError() {
        entry = "Error";
        accumulator = null;
        pendingOperator = null;
        replaceEntry = true;
        showingError = true;
        updateDisplay();
    }

    private void updateDisplay() {
        if (pendingOperator == null || accumulator == null) {
            display.setText(entry);
            return;
        }

        String visibleOperator = "*".equals(pendingOperator) ? "×" : pendingOperator;
        String expression = formatNumber(accumulator) + " " + visibleOperator;
        if (!replaceEntry) {
            expression += " " + entry;
        }
        display.setText(expression);
    }

    private String formatNumber(BigDecimal number) {
        BigDecimal normalized = number.stripTrailingZeros();
        return normalized.compareTo(BigDecimal.ZERO) == 0 ? "0" : normalized.toPlainString();
    }
}