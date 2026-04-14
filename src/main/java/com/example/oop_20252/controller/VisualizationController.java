package com.example.oop_20252.controller;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.service.OperationService;
import com.example.oop_20252.service.frames.OperationResult;
import com.example.oop_20252.service.frames.StepFrame;
import com.example.oop_20252.service.snapshots.TreeSnapshot;
import com.example.oop_20252.service.snapshots.NaryTreeSnapshot;
import com.example.oop_20252.service.snapshots.BinaryTreeSnapshot;
import com.example.oop_20252.service.snapshots.RedBlackTreeSnapshot;
import com.example.oop_20252.model.multichild.NaryTree;
import com.example.oop_20252.model.binary.BST;
import com.example.oop_20252.model.binary.AVLTree;
import com.example.oop_20252.model.redblack.RBTree;
import com.example.oop_20252.model.redblack.RBNode;
import com.example.oop_20252.util.ListFormatUtil;

import com.example.oop_20252.view.HeaderBar;
import com.example.oop_20252.view.OperationForm;
import com.example.oop_20252.view.PlaybackBar;
import com.example.oop_20252.view.InformationPanel;
import com.example.oop_20252.view.TreePanel;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class VisualizationController {

    @FXML private BorderPane rootPane;

    private HeaderBar headerBar;
    private OperationForm operationForm;
    private PlaybackBar playbackBar;
    private InformationPanel informationPanel;
    private TreePanel treePanel;

    private TreeKind treeKind = TreeKind.N_ARY;
    private final OperationService operationService = new OperationService();

    private List<TreeSnapshot> history = new ArrayList<>();
    private int historyIndex = 0;

    private List<StepFrame> activeFrames = null;
    private int frameIndex = 0;
    private Timeline timeline;

    public void setTreeKind(TreeKind kind) {
        this.treeKind = kind;
        if (kind == TreeKind.BST || kind == TreeKind.AVL) {
            headerBar.titleLabel.setText("Binary Search Family");
        } else {
            headerBar.titleLabel.setText(kind == TreeKind.N_ARY ? "N-ary Tree" : "Red-Black Tree");
        }
        setupChoices();
        resetToEmptyTree();
        
        operationForm.showNilLeavesCheck.setDisable(kind != TreeKind.RED_BLACK);
        operationForm.showNilLeavesCheck.setSelected(false);
        treePanel.setShowNilLeaves(false);
    }

    @FXML
    private void initialize() {
        // Instantiate distributed UI components
        headerBar = new HeaderBar(this::onBack);
        operationForm = new OperationForm(this::onRun);
        playbackBar = new PlaybackBar(this::onStepBack, this::onPause, this::onContinue, this::onStepForward, this::onUndo, this::onRedo);
        informationPanel = new InformationPanel();
        treePanel = new TreePanel();
        
        operationForm.showNilLeavesCheck.selectedProperty().addListener((obs, o, n) -> {
            treePanel.setShowNilLeaves(n);
            if (activeFrames != null && !activeFrames.isEmpty()) {
                setFrame(frameIndex);
            } else if (!history.isEmpty()) {
                renderSnapshot(history.get(historyIndex));
            }
        });

        // Assemble Top Header
        VBox topBox = new VBox(headerBar, new javafx.scene.layout.Region() {{ setPrefHeight(15); }}, operationForm);
        topBox.getStyleClass().add("top-container");
        topBox.setPadding(new Insets(15, 20, 15, 20));
        rootPane.setTop(topBox);

        // Assemble Center Body (SplitPane)
        VBox treeContainer = new VBox(treePanel);
        VBox.setVgrow(treePanel, javafx.scene.layout.Priority.ALWAYS);
        treeContainer.getStyleClass().add("panel-card");
        treeContainer.setStyle("-fx-background-color: rgba(13, 17, 23, 0.4);");
        
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        splitPane.getItems().addAll(treeContainer, informationPanel);
        splitPane.setDividerPositions(0.72);
        splitPane.getStyleClass().add("vis-split");
        splitPane.setStyle("-fx-background-color: transparent; -fx-padding: 0 20 20 20;");
        
        rootPane.setCenter(splitPane);

        // Assemble Bottom Footer
        rootPane.setBottom(playbackBar);
    }

    private void setupChoices() {
        operationForm.operationChoice.getItems().clear();
        operationForm.operationChoice.getItems().addAll("Create", "Insert", "Delete", "Update", "Traverse", "Search");
        operationForm.operationChoice.getSelectionModel().select("Insert");

        operationForm.traverseChoice.getItems().clear();
        operationForm.traverseChoice.getItems().addAll("DFS", "BFS");
        operationForm.traverseChoice.getSelectionModel().select("DFS");

        operationForm.rbDeleteModeChoice.getItems().clear();
        operationForm.rbDeleteModeChoice.getItems().addAll("Immediate", "Step-by-step");
        operationForm.rbDeleteModeChoice.getSelectionModel().select("Immediate");
        operationForm.rbDeleteModeChoice.setDisable(treeKind != TreeKind.RED_BLACK);
    }

    private void resetToEmptyTree() {
        stopPlayback();
        history.clear();
        historyIndex = 0;
        TreeSnapshot empty = createEmptySnapshot(treeKind);
        history.add(empty);
        activeFrames = null;
        frameIndex = 0;
        informationPanel.codePanel.setCodeLines(new String[0]);
        playbackBar.progressBar.setProgress(0);
        renderSnapshot(empty);
        informationPanel.statusLabel.setText("Ready. Choose an operation and click Execute.");
    }

    private TreeSnapshot createEmptySnapshot(TreeKind kind) {
        if (kind == TreeKind.N_ARY) {
            NaryTree<Integer> t = new NaryTree<>();
            return new NaryTreeSnapshot(t.deepCopy().getRoot());
        }
        if (kind == TreeKind.BST) {
            BST<Integer> t = new BST<>();
            return new BinaryTreeSnapshot(t.deepCopy().getRoot(), TreeKind.BST);
        }
        if (kind == TreeKind.AVL) {
            AVLTree<Integer> t = new AVLTree<>();
            return new BinaryTreeSnapshot(t.deepCopy().getRoot(), TreeKind.AVL);
        }
        RBTree<Integer> t = new RBTree<>();
        RBTree<Integer> copy = t.deepCopy();
        return new RedBlackTreeSnapshot((RBNode<Integer>)copy.getRoot(), copy.getNil());
    }

    private void renderSnapshot(TreeSnapshot snapshot) {
        treePanel.render(snapshot, List.of(), List.of(), 0);
    }

    private void onBack() {
        stopPlayback();
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/oop_20252/main-menu.fxml"));
            var scene = new javafx.scene.Scene(loader.load(), 1200, 760);
            var stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    private void onRun() {
        if (activeFrames != null && frameIndex < activeFrames.size() - 1) {
            setFrame(activeFrames.size() - 1);
        }
        stopPlayback();

        try {
            String op = operationForm.operationChoice.getSelectionModel().getSelectedItem();
            if (op == null) return;

            TreeSnapshot current = history.get(historyIndex);
            OperationResult result;

            switch (op) {
                case "Create" -> result = operationService.create(treeKind, current);
                case "Insert" -> {
                    Integer parent = parseNullableInt(operationForm.parentValueField.getText());
                    int newValue = parseIntOrThrow(operationForm.valueField.getText(), "Value required.");
                    result = operationService.insert(treeKind, current, parent, newValue);
                }
                case "Delete" -> {
                    int v = parseIntOrThrow(operationForm.valueField.getText(), "Value required.");
                    boolean detailedRbDelete = "Step-by-step".equalsIgnoreCase(operationForm.rbDeleteModeChoice.getValue());
                    result = operationService.delete(treeKind, current, v, detailedRbDelete);
                }
                case "Update" -> {
                    int oldV = parseIntOrThrow(operationForm.valueField.getText(), "Old required.");
                    int newV = parseIntOrThrow(operationForm.newValueField.getText(), "New required.");
                    result = operationService.update(treeKind, current, oldV, newV);
                }
                case "Traverse" -> {
                    boolean bfs = "BFS".equalsIgnoreCase(operationForm.traverseChoice.getSelectionModel().getSelectedItem());
                    result = operationService.traverse(treeKind, current, bfs);
                }
                case "Search" -> {
                    int v = parseIntOrThrow(operationForm.valueField.getText(), "Value required.");
                    result = operationService.search(treeKind, current, v);
                }
                default -> { return; }
            }

            setActiveOperation(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Op failed: " + ex.getMessage());
        }
    }

    private void setActiveOperation(OperationResult result) {
        while (history.size() - 1 > historyIndex) {
            history.remove(history.size() - 1);
        }

        activeFrames = result.getFrames();
        frameIndex = 0;
        informationPanel.codePanel.setCodeLines(result.getCodeLines());

        TreeSnapshot endSnapshot = activeFrames.isEmpty() ? history.get(historyIndex)
                : activeFrames.get(activeFrames.size() - 1).getSnapshot();
        history.add(endSnapshot);
        historyIndex = history.size() - 1;

        setFrame(0);
        informationPanel.statusLabel.setText(result.getTitle() + " started.");
        onContinue();
    }

    private void setFrame(int index) {
        if (activeFrames == null || activeFrames.isEmpty()) return;
        if (index < 0) index = 0;
        if (index >= activeFrames.size()) index = activeFrames.size() - 1;
        frameIndex = index;

        StepFrame frame = activeFrames.get(frameIndex);
        double durationMs = playbackBar.speedSlider.getValue() * 0.9;
        treePanel.render(frame.getSnapshot(), frame.getHighlightValues(), frame.getVisitedValues(), durationMs);
        informationPanel.codePanel.highlightLine(frame.getCodeLineIndex());
        informationPanel.statusLabel.setText(frame.getStatusText());
        informationPanel.traversalOrderLabel.setText(ListFormatUtil.joinArrowSeparated(frame.getVisitedValues()));
        
        double denom = Math.max(1, activeFrames.size() - 1);
        playbackBar.progressBar.setProgress(frameIndex / denom);
    }

    private void stopPlayback() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void onPause() {
        if (timeline != null) timeline.pause();
    }

    private void onContinue() {
        if (activeFrames == null || activeFrames.isEmpty()) return;
        if (frameIndex >= activeFrames.size() - 1) return;

        stopPlayback();
        long speedMs = (long) playbackBar.speedSlider.getValue();

        timeline = new Timeline();
        for (int i = frameIndex + 1; i < activeFrames.size(); i++) {
            final int idx = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(speedMs), e -> setFrame(idx)));
        }
        timeline.play();
    }

    private void onStepBack() {
        if (activeFrames == null || activeFrames.isEmpty()) return;
        stopPlayback();
        setFrame(frameIndex - 1);
    }

    private void onStepForward() {
        if (activeFrames == null || activeFrames.isEmpty()) return;
        stopPlayback();
        setFrame(frameIndex + 1);
    }

    private void onUndo() {
        stopPlayback();
        activeFrames = null;
        informationPanel.codePanel.setCodeLines(new String[0]);

        if (historyIndex > 0) historyIndex--;
        TreeSnapshot snapshot = history.get(historyIndex);
        frameIndex = 0;
        playbackBar.progressBar.setProgress(0);
        renderSnapshot(snapshot);
        informationPanel.statusLabel.setText("Undo to previous state.");
    }

    private void onRedo() {
        stopPlayback();
        activeFrames = null;
        informationPanel.codePanel.setCodeLines(new String[0]);

        if (historyIndex < history.size() - 1) historyIndex++;
        TreeSnapshot snapshot = history.get(historyIndex);
        frameIndex = 0;
        playbackBar.progressBar.setProgress(0);
        renderSnapshot(snapshot);
        informationPanel.statusLabel.setText("Redo.");
    }

    private Integer parseNullableInt(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;
        return Integer.parseInt(t);
    }

    private int parseIntOrThrow(String text, String message) {
        if (text == null) throw new NumberFormatException(message);
        String t = text.trim();
        if (t.isEmpty()) throw new NumberFormatException(message);
        return Integer.parseInt(t);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input error");
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Invalid input." : message);
        alert.showAndWait();
    }
}
