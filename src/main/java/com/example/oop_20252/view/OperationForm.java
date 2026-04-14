package com.example.oop_20252.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class OperationForm extends HBox {
    public final ComboBox<String> operationChoice = new ComboBox<>();
    public final TextField valueField = new TextField();
    public final TextField parentValueField = new TextField();
    public final TextField newValueField = new TextField();
    public final ComboBox<String> traverseChoice = new ComboBox<>();
    public final ComboBox<String> rbDeleteModeChoice = new ComboBox<>();
    public final CheckBox showNilLeavesCheck = new CheckBox("Show NILs");
    public final Button btnRun = new Button("▶ Execute");

    public OperationForm(Runnable onRun) {
        this.getStyleClass().add("vis-toolbar");
        this.setSpacing(16.0);
        this.setAlignment(Pos.CENTER_LEFT);

        operationChoice.setPrefWidth(130);
        valueField.setPrefWidth(80);
        valueField.setPromptText("value");
        parentValueField.setPrefWidth(100);
        parentValueField.setPromptText("parent (opt)");
        newValueField.setPrefWidth(80);
        newValueField.setPromptText("update");
        traverseChoice.setPrefWidth(90);
        rbDeleteModeChoice.setPrefWidth(120);
        
        btnRun.getStyleClass().add("button-run");
        btnRun.setOnAction(e -> onRun.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(
            createLabeled("Op:", operationChoice),
            createLabeled("Val:", valueField),
            createLabeled("Parent:", parentValueField),
            createLabeled("New val:", newValueField),
            createLabeled("Traversal:", traverseChoice),
            createLabeled("RB Del Mode:", rbDeleteModeChoice),
            showNilLeavesCheck,
            spacer,
            btnRun
        );
    }

    private HBox createLabeled(String labelTxt, Control c) {
        Label l = new Label(labelTxt);
        l.getStyleClass().add("form-label");
        HBox box = new HBox(5, l, c);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}
