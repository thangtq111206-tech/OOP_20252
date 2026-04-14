package com.example.oop_20252.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class InformationPanel extends VBox {
    public final Label statusLabel = new Label("Ready.");
    public final Label traversalOrderLabel = new Label("(empty)");
    public final CodePanel codePanel = new CodePanel();

    public InformationPanel() {
        this.getStyleClass().add("vis-right-column");
        this.setSpacing(16.0);
        this.setStyle("-fx-padding: 0 0 0 10px;");

        statusLabel.getStyleClass().add("status-label");
        statusLabel.setWrapText(true);
        traversalOrderLabel.getStyleClass().add("traversal-order");
        traversalOrderLabel.setWrapText(true);

        VBox statusBox = createCard("Operation Status", statusLabel, null);
        
        Label legend = new Label("Cyan = Current node · Green = Visited");
        legend.getStyleClass().add("legend-label");
        VBox traverseBox = createCard("Traversal Path", traversalOrderLabel, legend);
        
        VBox codeBox = createCard("Algorithm Pseudocode", codePanel, null);
        VBox.setVgrow(codeBox, Priority.ALWAYS);
        VBox.setVgrow(codePanel, Priority.ALWAYS);

        this.getChildren().addAll(statusBox, traverseBox, codeBox);
    }

    private VBox createCard(String title, javafx.scene.Node content1, javafx.scene.Node content2) {
        Label t = new Label(title);
        t.getStyleClass().add("panel-card-title");
        VBox box = new VBox(10.0);
        box.getStyleClass().add("panel-card");
        if (content2 != null) box.getChildren().addAll(t, content1, content2);
        else box.getChildren().addAll(t, content1);
        return box;
    }
}
