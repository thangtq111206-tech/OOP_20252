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

import com.example.oop_20252.view.CodePanel;
import com.example.oop_20252.view.TreePanel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class VisualizationController {

    @FXML private TreePanel treePanel;
    @FXML private CodePanel codePanel;

    @FXML private ComboBox<String> operationChoice;
    @FXML private TextField parentValueField;
    @FXML private TextField valueField;
    @FXML private TextField newValueField;
    @FXML private ComboBox<String> traverseChoice;
    @FXML private ComboBox<String> rbDeleteModeChoice;
    @FXML private CheckBox showNilLeavesCheck;
    @FXML private ComboBox<String> binarySubtypeChoice;

    @FXML private Button btnRun;
    @FXML private Button btnPause;
    @FXML private Button btnContinue;
    @FXML private Button btnStepBack;
    @FXML private Button btnStepForward;
    @FXML private Button btnUndo;
    @FXML private Button btnRedo;
    @FXML private Button btnBack;

    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Label traversalOrderLabel;

    @FXML private javafx.scene.control.Slider speedSlider;
    @FXML private Label titleLabel;

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
            titleLabel.setText("Binary Search Family");
        } else {
            titleLabel.setText(kind == TreeKind.N_ARY ? "N-ary Tree" : "Red-Black Tree");
        }
        setupChoices();
        resetToEmptyTree();
        if (showNilLeavesCheck != null) {
            showNilLeavesCheck.setDisable(kind != TreeKind.RED_BLACK);
            showNilLeavesCheck.setSelected(false);
        }
        if (treePanel != null) {
            treePanel.setShowNilLeaves(false);
        }
    }

    @FXML
    private void initialize() {
        if (binarySubtypeChoice != null) {
            binarySubtypeChoice.getItems().addAll("BST", "AVL");
            binarySubtypeChoice.setOnAction(e -> {
                String val = binarySubtypeChoice.getValue();
                if ("AVL".equals(val) && treeKind != TreeKind.AVL) {
                    setTreeKind(TreeKind.AVL);
                } else if ("BST".equals(val) && treeKind != TreeKind.BST) {
                    setTreeKind(TreeKind.BST);
                }
            });
        }
    }

    private void setupChoices() {
        operationChoice.getItems().clear();
        operationChoice.getItems().addAll("Create", "Insert", "Delete", "Update", "Traverse", "Search");
        operationChoice.getSelectionModel().select("Insert");

        traverseChoice.getItems().clear();
        traverseChoice.getItems().addAll("DFS", "BFS");
        traverseChoice.getSelectionModel().select("DFS");

        if (rbDeleteModeChoice != null) {
            rbDeleteModeChoice.getItems().clear();
            rbDeleteModeChoice.getItems().addAll("Immediate", "Step-by-step");
            rbDeleteModeChoice.getSelectionModel().select("Immediate");
            rbDeleteModeChoice.setDisable(treeKind != TreeKind.RED_BLACK);
        }

        if (binarySubtypeChoice != null) {
            if (treeKind == TreeKind.BST || treeKind == TreeKind.AVL) {
                binarySubtypeChoice.setVisible(true);
                binarySubtypeChoice.setManaged(true);
                binarySubtypeChoice.setValue(treeKind == TreeKind.AVL ? "AVL" : "BST");
            } else {
                binarySubtypeChoice.setVisible(false);
                binarySubtypeChoice.setManaged(false);
            }
        }
    }

    private void resetToEmptyTree() {
        stopPlayback();
        history.clear();
        historyIndex = 0;
        TreeSnapshot empty = createEmptySnapshot(treeKind);
        history.add(empty);
        activeFrames = null;
        frameIndex = 0;
        codePanel.setCodeLines(new String[0]);
        progressBar.setProgress(0);
        renderSnapshot(empty);
        statusLabel.setText("Ready. Choose an operation and click Execute.");
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

    @FXML
    private void onCopyJson() {
        if (activeFrames == null || activeFrames.isEmpty()) {
            showError("No timeline to export.");
            return;
        }
        String json = "{}";
        ClipboardContent content = new ClipboardContent();
        content.putString(json);
        Clipboard.getSystemClipboard().setContent(content);
        statusLabel.setText("Copied JSON timeline.");
    }

    @FXML
    private void onBack() {
        stopPlayback();
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/oop_20252/main-menu.fxml"));
            var scene = new javafx.scene.Scene(loader.load(), 1200, 760);
            var stage = (javafx.stage.Stage) btnBack.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @FXML
    private void onRun(ActionEvent e) {
        if (activeFrames != null && frameIndex < activeFrames.size() - 1) {
            setFrame(activeFrames.size() - 1);
        }
        stopPlayback();

        try {
            String op = operationChoice.getSelectionModel().getSelectedItem();
            if (op == null) return;

            TreeSnapshot current = history.get(historyIndex);
            OperationResult result;

            switch (op) {
                case "Create" -> result = operationService.create(treeKind, current);
                case "Insert" -> {
                    Integer parent = parseNullableInt(parentValueField.getText());
                    int newValue = parseIntOrThrow(valueField.getText(), "Value required.");
                    result = operationService.insert(treeKind, current, parent, newValue);
                }
                case "Delete" -> {
                    int v = parseIntOrThrow(valueField.getText(), "Value required.");
                    boolean detailedRbDelete = rbDeleteModeChoice != null
                            && "Step-by-step".equalsIgnoreCase(rbDeleteModeChoice.getValue());
                    result = operationService.delete(treeKind, current, v, detailedRbDelete);
                }
                case "Update" -> {
                    int oldV = parseIntOrThrow(valueField.getText(), "Old required.");
                    int newV = parseIntOrThrow(newValueField.getText(), "New required.");
                    result = operationService.update(treeKind, current, oldV, newV);
                }
                case "Traverse" -> {
                    boolean bfs = "BFS".equalsIgnoreCase(traverseChoice.getSelectionModel().getSelectedItem());
                    result = operationService.traverse(treeKind, current, bfs);
                }
                case "Search" -> {
                    int v = parseIntOrThrow(valueField.getText(), "Value required.");
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
        codePanel.setCodeLines(result.getCodeLines());

        TreeSnapshot endSnapshot = activeFrames.isEmpty() ? history.get(historyIndex)
                : activeFrames.get(activeFrames.size() - 1).getSnapshot();
        history.add(endSnapshot);
        historyIndex = history.size() - 1;

        setFrame(0);
        statusLabel.setText(result.getTitle() + " started.");
        onContinue();
    }

    private void setFrame(int index) {
        if (activeFrames == null || activeFrames.isEmpty()) return;
        if (index < 0) index = 0;
        if (index >= activeFrames.size()) index = activeFrames.size() - 1;
        frameIndex = index;

        StepFrame frame = activeFrames.get(frameIndex);
        double durationMs = speedSlider.getValue() * 0.9;
        treePanel.render(frame.getSnapshot(), frame.getHighlightValues(), frame.getVisitedValues(), durationMs);
        codePanel.highlightLine(frame.getCodeLineIndex());
        statusLabel.setText(frame.getStatusText());
        if (traversalOrderLabel != null) {
            traversalOrderLabel.setText(ListFormatUtil.joinArrowSeparated(frame.getVisitedValues()));
        }
        double denom = Math.max(1, activeFrames.size() - 1);
        progressBar.setProgress(frameIndex / denom);
    }

    private void stopPlayback() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    @FXML
    private void onPause() {
        if (timeline != null) timeline.pause();
    }

    @FXML
    private void onContinue() {
        if (activeFrames == null || activeFrames.isEmpty()) return;
        if (frameIndex >= activeFrames.size() - 1) return;

        stopPlayback();
        long speedMs = (long) speedSlider.getValue();

        timeline = new Timeline();
        for (int i = frameIndex + 1; i < activeFrames.size(); i++) {
            final int idx = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(speedMs), e -> setFrame(idx)));
        }
        timeline.play();
    }

    @FXML
    private void onStepBack() {
        if (activeFrames == null || activeFrames.isEmpty()) return;
        stopPlayback();
        setFrame(frameIndex - 1);
    }

    @FXML
    private void onStepForward() {
        if (activeFrames == null || activeFrames.isEmpty()) return;
        stopPlayback();
        setFrame(frameIndex + 1);
    }

    @FXML
    private void onUndo() {
        stopPlayback();
        activeFrames = null;
        codePanel.setCodeLines(new String[0]);

        if (historyIndex > 0) historyIndex--;
        TreeSnapshot snapshot = history.get(historyIndex);
        frameIndex = 0;
        progressBar.setProgress(0);
        renderSnapshot(snapshot);
        statusLabel.setText("Undo to previous state.");
    }

    @FXML
    private void onRedo() {
        stopPlayback();
        activeFrames = null;
        codePanel.setCodeLines(new String[0]);

        if (historyIndex < history.size() - 1) historyIndex++;
        TreeSnapshot snapshot = history.get(historyIndex);
        frameIndex = 0;
        progressBar.setProgress(0);
        renderSnapshot(snapshot);
        statusLabel.setText("Redo.");
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
