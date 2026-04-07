package com.example.oop_20252.controller;

import com.example.oop_20252.model.TreeKind;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {

    @FXML
    private Button btnGeneric;
    @FXML
    private Button btnBinary;
    @FXML
    private Button btnRB;
    @FXML
    private Button btnQuit;

    @FXML
    private void onSelectGeneric() throws IOException {
        goToVisualization(TreeKind.GENERIC);
    }

    @FXML
    private void onSelectBinary() throws IOException {
        goToVisualization(TreeKind.BINARY);
    }

    @FXML
    private void onSelectRedBlack() throws IOException {
        goToVisualization(TreeKind.RED_BLACK);
    }

    private void goToVisualization(TreeKind kind) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/oop_20252/visualization.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 760);
        Stage stage = (Stage) btnGeneric.getScene().getWindow();
        VisualizationController controller = loader.getController();
        controller.setTreeKind(kind);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void onHelpHowToUse() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("How to use");
        alert.setHeaderText(null);
        alert.setContentText(
                "1. Choose a tree type.\n" +
                "2. Select an operation (Create/Insert/Delete/Update/Traverse/Search).\n" +
                "3. Enter required values and click Run.\n" +
                "4. Use Pause/Continue and Step controls to inspect execution.\n" +
                "5. Undo/Redo moves between completed operations.\n"
        );
        alert.showAndWait();
    }

    @FXML
    private void onQuit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quit");
        confirm.setHeaderText("Are you sure you want to quit?");
        confirm.setContentText("Your current progress will be lost.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        Platform.exit();
    }
}

