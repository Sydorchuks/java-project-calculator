package com.example.calculator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class CalculatorController {

    @FXML private Label displayLabel;
    @FXML private ListView<String> historyList;

    private final CalculatorEngine engine = new CalculatorEngine();

    @FXML
    public void initialize() {
        displayLabel.setText(engine.getDisplayText());
    }

    @FXML
    private void onButtonClick(javafx.event.ActionEvent event) {
        var btn = (javafx.scene.control.Button) event.getSource();
        String value = btn.getText();

        if ("=".equals(value)) {
            String before = engine.getDisplayText();
            String result = engine.calculateAndGetResult();
            if (result != null) {
                historyList.getItems().add(before + " = " + result);
            }
            displayLabel.setText(engine.getDisplayText());
            return;
        }

        engine.press(value);
        displayLabel.setText(engine.getDisplayText());
    }
}
