package com.example.calculator;

import java.util.ArrayList;
import java.util.List;

public class CalculatorEngine {

    private final StringBuilder expression = new StringBuilder();
    private boolean lastWasEquals = false;

    public String getDisplayText() {
        return expression.length() == 0 ? "0" : expression.toString();
    }

    public void press(String value) {
        if ("C".equals(value)) { clearAll(); return; }
        if ("⌫".equals(value)) { backspace(); return; }
        if ("=".equals(value)) { return; }

        if (lastWasEquals && isDigit(value)) {
            expression.setLength(0);
            lastWasEquals = false;
        }

        if (isDigit(value)) { appendDigit(value); return; }
        if (".".equals(value)) { appendDot(); return; }
        if (isOperator(value)) { appendOperator(value); }
    }

    public String calculateAndGetResult() {
        try {
            String exp = expression.toString();
            if (exp.isEmpty()) return null;

            char last = exp.charAt(exp.length() - 1);
            if (isOperatorChar(last)) return null;

            double result = eval(exp);
            String formatted = format(result);

            expression.setLength(0);
            expression.append(formatted);
            lastWasEquals = true;

            return formatted;
        } catch (Exception e) {
            expression.setLength(0);
            lastWasEquals = false;
            return "Error";
        }
    }

    private void appendDigit(String d) {
        expression.append(d);
    }

    private void appendDot() {
        int lastOp = lastOperatorIndex(expression);
        String currentNumber = expression.substring(lastOp + 1);

        if (currentNumber.isEmpty()) {
            expression.append("0.");
        } else if (!currentNumber.contains(".")) {
            expression.append(".");
        }

        lastWasEquals = false;
    }

    private void appendOperator(String op) {
        if (expression.length() == 0) {
            if ("-".equals(op)) expression.append("-");
            return;
        }

        char last = expression.charAt(expression.length() - 1);
        if (isOperatorChar(last)) {
            expression.setCharAt(expression.length() - 1, op.charAt(0));
        } else {
            expression.append(op);
        }
    }

    private void backspace() {
        if (expression.length() == 0) return;
        expression.deleteCharAt(expression.length() - 1);
        lastWasEquals = false;
    }

    private void clearAll() {
        expression.setLength(0);
        lastWasEquals = false;
    }

    private double eval(String exp) {
        int opIndex = findMainOperatorIndex(exp);
        if (opIndex == -1) return Double.parseDouble(exp);

        String left = exp.substring(0, opIndex);
        String right = exp.substring(opIndex + 1);
        char op = exp.charAt(opIndex);

        double a = Double.parseDouble(left);
        double b = Double.parseDouble(right);

        return switch (op) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if (b == 0) throw new ArithmeticException();
                yield a / b;
            }
            default -> throw new IllegalArgumentException("Unknown operator");
        };
    }

    private int findMainOperatorIndex(String exp) {
        for (int i = 1; i < exp.length(); i++) {
            char ch = exp.charAt(i);
            if (ch == '+' || ch == '*' || ch == '/') return i;
            if (ch == '-') {
                char prev = exp.charAt(i - 1);
                if (Character.isDigit(prev) || prev == '.') return i;
            }
        }
        return -1;
    }

    private int lastOperatorIndex(CharSequence sb) {
        for (int i = sb.length() - 1; i >= 0; i--) {
            char ch = sb.charAt(i);
            if (isOperatorChar(ch)) return i;
        }
        return -1;
    }

    private String format(double value) {
        if (value == (long) value) return String.valueOf((long) value);
        return String.valueOf(value);
    }

    private boolean isDigit(String v) {
        return v.length() == 1 && Character.isDigit(v.charAt(0));
    }

    private boolean isOperator(String v) {
        return "+".equals(v) || "-".equals(v) || "*".equals(v) || "/".equals(v);
    }

    private boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }
}
