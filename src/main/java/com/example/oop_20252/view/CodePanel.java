package com.example.oop_20252.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CodePanel extends VBox {

    public CodePanel() {
        getStyleClass().add("code-panel");
        setSpacing(6);
        setPadding(new Insets(12));
    }

    public void setCodeLines(String[] codeLines) {
        getChildren().clear();
        if (codeLines == null) {
            codeLines = new String[0];
        }

        for (String line : codeLines) {
            Label l = new Label(formatLine(line));
            l.getStyleClass().add("code-line");
            l.setWrapText(true);
            l.setMaxWidth(430);
            getChildren().add(l);
        }
    }

    public void highlightLine(int index) {
        for (int i = 0; i < getChildren().size(); i++) {
            if (!(getChildren().get(i) instanceof Label l)) {
                continue;
            }
            l.getStyleClass().remove("code-line-highlight");
            if (i == index) {
                l.getStyleClass().add("code-line-highlight");
            }
        }
    }

    private String formatLine(String line) {
        return line == null ? "" : line;
    }
}
