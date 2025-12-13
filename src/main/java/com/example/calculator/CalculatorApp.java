package com.example.calculator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class CalculatorApp extends Application {

    private final StringBuilder expression = new StringBuilder();
    private final Label display = new Label("0");
    private final ListView<String> history = new ListView<>();

    private boolean lastWasEquals = false;

    @Override
    public void start(Stage stage) {
        display.setFont(Font.font(36));
        display.setAlignment(Pos.CENTER_RIGHT);
        display.setPadding(new Insets(20));
        display.setMinHeight(100);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        String[][] layout = {
                {"C", "⌫", "",  "/"},
                {"7", "8", "9", "*"},
                {"4", "5", "6", "-"},
                {"1", "2", "3", "+"},
                {"0", ".", "=", ""}
        };

        for (int r = 0; r < layout.length; r++) {
            for (int c = 0; c < layout[r].length; c++) {
                String text = layout[r][c];
                if (text.isEmpty()) {
                    grid.add(new Label(), c, r);
                    continue;
                }
                Button button = new Button(text);
                button.setFont(Font.font(18));
                button.setPrefSize(80, 60);
                button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                button.setOnAction(e -> onPress(text));
                grid.add(button, c, r);
                GridPane.setHgrow(button, Priority.ALWAYS);
                GridPane.setVgrow(button, Priority.ALWAYS);
            }
        }

        history.setPlaceholder(new Label("History"));

        BorderPane root = new BorderPane();
        root.setTop(display);
        root.setCenter(grid);
        root.setRight(history);
        BorderPane.setMargin(history, new Insets(10));
        BorderPane.setMargin(grid, new Insets(0, 10, 10, 10));

        Scene scene = new Scene(root, 800, 500);
        stage.setTitle("Calculator");
        stage.setScene(scene);
        stage.show();
    }

    private void onPress(String value) {

        if ("C".equals(value)) {
            clearAll();
            return;
        }

        if ("⌫".equals(value)) {
            backspace();
            return;
        }

        if ("=".equals(value)) {
            calculate();
            return;
        }

        if (lastWasEquals && isDigit(value)) {
            expression.setLength(0);
            lastWasEquals = false;
        }

        if (isDigit(value)) {
            appendDigit(value);
            return;
        }

        if (".".equals(value)) {
            appendDot();
            return;
        }

        if (isOperator(value)) {
            appendOperator(value);
        }
    }

    private void appendDigit(String d) {
        normalizeIfDisplayError();
        expression.append(d);
        updateDisplay();
    }

    private void appendDot() {
        normalizeIfDisplayError();

        int lastOp = lastOperatorIndex(expression);
        String currentNumber = expression.substring(lastOp + 1);

        if (currentNumber.isEmpty()) {
            expression.append("0.");
        } else if (!currentNumber.contains(".")) {
            expression.append(".");
        }

        lastWasEquals = false;
        updateDisplay();
    }

    private void appendOperator(String op) {
        normalizeIfDisplayError();

        if (lastWasEquals) {
            lastWasEquals = false;
        }

        if (expression.length() == 0) {
            if ("-".equals(op)) {
                expression.append("-");
                updateDisplay();
            }
            return;
        }

        char last = expression.charAt(expression.length() - 1);
        if (isOperatorChar(last)) {
            expression.setCharAt(expression.length() - 1, op.charAt(0));
        } else {
            expression.append(op);
        }

        updateDisplay();
    }

    private void calculate() {
        try {
            normalizeIfDisplayError();

            String exp = expression.toString();
            if (exp.isEmpty()) return;

            char last = exp.charAt(exp.length() - 1);
            if (isOperatorChar(last)) return;

            double result = eval(exp);
            String formatted = format(result);

            history.getItems().add(exp + " = " + formatted);

            display.setText(formatted);

            expression.setLength(0);
            expression.append(formatted);

            lastWasEquals = true;

        } catch (Exception e) {
            display.setText("Error");
            expression.setLength(0);
            lastWasEquals = false;
        }
    }

    private void backspace() {
        normalizeIfDisplayError();

        if (expression.length() == 0) {
            display.setText("0");
            return;
        }

        expression.deleteCharAt(expression.length() - 1);

        if (expression.length() == 0) {
            display.setText("0");
        } else {
            updateDisplay();
        }

        lastWasEquals = false;
    }

    private void clearAll() {
        expression.setLength(0);
        display.setText("0");
        lastWasEquals = false;
    }

    private void normalizeIfDisplayError() {
        if ("Error".equals(display.getText())) {
            expression.setLength(0);
            display.setText("0");
            lastWasEquals = false;
        }
    }

    private void updateDisplay() {
        display.setText(expression.length() == 0 ? "0" : expression.toString());
    }

    private double eval(String exp) {

        int opIndex = findMainOperatorIndex(exp);
        if (opIndex == -1) {
            return Double.parseDouble(exp);
        }

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

    private int lastOperatorIndex(StringBuilder sb) {
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

    public static void main(String[] args) {
        launch();
    }
}
