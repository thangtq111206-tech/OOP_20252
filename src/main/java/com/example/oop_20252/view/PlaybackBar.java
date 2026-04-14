package com.example.oop_20252.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PlaybackBar extends HBox {
    public final Button btnStepBack = new Button("Step Back");
    public final Button btnPause = new Button("Pause");
    public final Button btnContinue = new Button("Play");
    public final Button btnStepForward = new Button("Step Forward");
    public final Button btnUndo = new Button("Undo");
    public final Button btnRedo = new Button("Redo");
    public final Slider speedSlider = new Slider(100, 1500, 500);
    public final ProgressBar progressBar = new ProgressBar(0);

    public PlaybackBar(Runnable onStepBack, Runnable onPause, Runnable onContinue, Runnable onStepForward, Runnable onUndo, Runnable onRedo) {
        this.getStyleClass().add("playback-bar");
        this.setSpacing(16.0);
        this.setAlignment(Pos.CENTER_LEFT);

        btnStepBack.setOnAction(e -> onStepBack.run());
        btnPause.setOnAction(e -> onPause.run());
        btnContinue.setOnAction(e -> onContinue.run());
        btnStepForward.setOnAction(e -> onStepForward.run());
        btnUndo.setOnAction(e -> onUndo.run());
        btnRedo.setOnAction(e -> onRedo.run());

        btnStepBack.getStyleClass().add("button-playback");
        btnPause.getStyleClass().add("button-playback");
        btnContinue.getStyleClass().addAll("button-playback", "button-primary-outline");
        btnStepForward.getStyleClass().add("button-playback");
        btnUndo.getStyleClass().add("button-playback");
        btnRedo.getStyleClass().add("button-playback");

        speedSlider.setShowTickMarks(false);
        speedSlider.setPrefWidth(150);
        progressBar.setPrefWidth(240);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Separator s1 = new Separator(javafx.geometry.Orientation.VERTICAL);
        s1.setStyle("-fx-opacity: 0.1;");
        Separator s2 = new Separator(javafx.geometry.Orientation.VERTICAL);
        s2.setStyle("-fx-opacity: 0.1;");

        Label speedLabel = new Label("Playback Speed");
        speedLabel.getStyleClass().add("panel-card-title");
        speedLabel.setStyle("-fx-font-size: 10px;");
        VBox speedBox = new VBox(4, speedLabel, speedSlider);
        speedBox.setAlignment(Pos.CENTER_RIGHT);

        this.getChildren().addAll(
            btnStepBack, btnPause, btnContinue, btnStepForward, s1, btnUndo, btnRedo, s2, spacer, speedBox, progressBar
        );
    }
}
