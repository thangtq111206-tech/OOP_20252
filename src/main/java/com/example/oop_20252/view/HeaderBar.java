package com.example.oop_20252.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HeaderBar extends HBox {
    public final Button btnBack = new Button("← Main menu");
    public final Label titleLabel = new Label("Visualization");

    public HeaderBar(Runnable onBack) {
        this.getStyleClass().add("vis-header");
        this.setSpacing(20.0);
        this.setAlignment(Pos.CENTER_LEFT);
        
        btnBack.getStyleClass().add("button-back");
        btnBack.setOnAction(e -> onBack.run());
        titleLabel.getStyleClass().add("vis-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(btnBack, titleLabel, spacer);
    }
}
