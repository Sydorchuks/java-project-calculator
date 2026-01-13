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

        lastWasEquals = false;
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
        exp = exp.replace(" ", "");
        List<String> tokens = tokenize(exp);

        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if ("*".equals(t) || "/".equals(t)) {
                double a = Double.parseDouble(tokens.get(i - 1));
                double b = Double.parseDouble(tokens.get(i + 1));

                double res;
                if ("*".equals(t)) {
                    res = a * b;
                } else {
                    if (b == 0) throw new ArithmeticException();
                    res = a / b;
                }

                tokens.set(i - 1, String.valueOf(res));
                tokens.remove(i);
                tokens.remove(i);
                i--;
            }
        }

        double result = Double.parseDouble(tokens.get(0));
        for (int i = 1; i < tokens.size(); i += 2) {
            String op = tokens.get(i);
            double b = Double.parseDouble(tokens.get(i + 1));

            if ("+".equals(op)) result += b;
            else if ("-".equals(op)) result -= b;
            else throw new IllegalArgumentException("Unknown operator");
        }

        return result;
    }

    private List<String> tokenize(String exp) {
        List<String> tokens = new ArrayList<>();
        StringBuilder num = new StringBuilder();

        for (int i = 0; i < exp.length(); i++) {
            char ch = exp.charAt(i);

            if (ch == '-' && (i == 0 || isOperatorChar(exp.charAt(i - 1)))) {
                num.append(ch);
                continue;
            }

            if (Character.isDigit(ch) || ch == '.') {
                num.append(ch);
                continue;
            }

            if (isOperatorChar(ch)) {
                if (num.length() == 0) throw new IllegalArgumentException("Broken expression");
                tokens.add(num.toString());
                num.setLength(0);
                tokens.add(String.valueOf(ch));
                continue;
            }

            throw new IllegalArgumentException("Invalid char");
        }

        if (num.length() > 0) tokens.add(num.toString());
        return tokens;
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

    private int lastOperatorIndex(CharSequence sb) {
        for (int i = sb.length() - 1; i >= 0; i--) {
            char ch = sb.charAt(i);
            if (isOperatorChar(ch)) return i;
        }
        return -1;
    }
}
