package com.example.oop_20252.view;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.binary.BST;
import com.example.oop_20252.model.binary.BinaryNode;
import com.example.oop_20252.model.multichild.NaryTree;
import com.example.oop_20252.model.multichild.MultiNode;
import com.example.oop_20252.model.redblack.RBTree;
import com.example.oop_20252.model.redblack.RBNode;
import com.example.oop_20252.service.snapshots.BinaryTreeSnapshot;
import com.example.oop_20252.service.snapshots.NaryTreeSnapshot;
import com.example.oop_20252.service.snapshots.RedBlackTreeSnapshot;
import com.example.oop_20252.service.snapshots.TreeSnapshot;
import com.example.oop_20252.util.TreeLayoutConstants;
import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;

public class TreePanel extends Pane {
    private final Group edgeGroup = new Group();
    private final Group nodeGroup = new Group();

    private static final double NODE_RADIUS = TreeLayoutConstants.NODE_RADIUS;
    private static final double X_SPACING = TreeLayoutConstants.X_SPACING;
    private static final double Y_SPACING = TreeLayoutConstants.Y_SPACING;

    private Set<Integer> highlightSet = Set.of();
    private Set<Integer> visitedSet = Set.of();
    private boolean showNilLeaves = false;

    // Stateful UI maps
    private final Map<String, StackPane> nodeGuis = new HashMap<>();
    private final Map<String, Line> edgeGuis = new HashMap<>();

    public TreePanel() {
        getChildren().addAll(edgeGroup, nodeGroup);
        getStyleClass().add("tree-panel-canvas");
    }

    public void setShowNilLeaves(boolean showNilLeaves) {
        this.showNilLeaves = showNilLeaves;
    }

    public boolean isShowNilLeaves() {
        return showNilLeaves;
    }

    public void render(TreeSnapshot snapshot, List<Integer> highlightValues, List<Integer> visitedValues, double durationMs) {
        if (snapshot == null) return;
        this.highlightSet = highlightValues == null ? Set.of() : new HashSet<>(highlightValues);
        this.visitedSet = visitedValues == null ? Set.of() : new HashSet<>(visitedValues);

        TreeKind kind = snapshot.getKind();
        Map<String, Point2D> targetCoords = new HashMap<>();
        Map<String, String> targetEdges = new HashMap<>(); // childKey -> parentKey
        Map<String, NodeVisualState> targetVisuals = new HashMap<>();

        if (kind == TreeKind.N_ARY) {
            MultiNode<Integer> root = ((NaryTreeSnapshot) snapshot).getRoot();
            if (root != null) {
                Map<Object, Double> widthCache = new IdentityHashMap<>();
                double totalWidth = computeGenericWidths(root, widthCache);
                Map<Object, Point2D> coords = new IdentityHashMap<>();
                assignGenericPositions(root, 0, 60, Math.max(1, totalWidth), 60, coords, widthCache);
                extractGenericTargets(root, null, coords, targetCoords, targetEdges, targetVisuals);
            }
        } else if (kind == TreeKind.BST || kind == TreeKind.AVL) {
            BinaryNode<Integer> root = ((BinaryTreeSnapshot) snapshot).getRoot();
            if (root != null) {
                Map<Object, Point2D> coords = new IdentityHashMap<>();
                assignBinaryPositions(root, 0, new double[]{1}, coords);
                extractBinaryTargets(root, null, coords, targetCoords, targetEdges, targetVisuals);
            }
        } else if (kind == TreeKind.RED_BLACK) {
            RedBlackTreeSnapshot rbSnapshot = (RedBlackTreeSnapshot) snapshot;
            RBNode<Integer> root = rbSnapshot.getRoot();
            if (root != null) {
                Map<Object, Point2D> coords = new IdentityHashMap<>();
                assignBinaryPositionsRB(root, 0, new double[]{1}, coords, rbSnapshot.getNil());
                extractRBTargets(root, null, coords, targetCoords, targetEdges, targetVisuals, rbSnapshot.getNil(), "origin");
            }
        }

        animateToTargets(targetCoords, targetEdges, targetVisuals, durationMs);
    }

    private static class NodeVisualState {
        Color fill;
        Color stroke;
        double strokeWidth;
        String text;
        boolean isNil;

        public NodeVisualState(Color fill, Color stroke, double strokeWidth, String text, boolean isNil) {
            this.fill = fill;
            this.stroke = stroke;
            this.strokeWidth = strokeWidth;
            this.text = text;
            this.isNil = isNil;
        }
    }

    private void animateToTargets(Map<String, Point2D> targetCoords, Map<String, String> targetEdges, Map<String, NodeVisualState> targetVisuals, double durationMs) {
        ParallelTransition pt = new ParallelTransition();
        Duration dur = Duration.millis(durationMs > 0 ? durationMs : 1);

        // 1. Process Nodes
        Set<String> nodesToRemove = new HashSet<>(nodeGuis.keySet());
        nodesToRemove.removeAll(targetCoords.keySet());
        for (String nodeKey : nodesToRemove) {
            StackPane pane = nodeGuis.remove(nodeKey);
            FadeTransition ft = new FadeTransition(dur, pane);
            ft.setToValue(0);
            ft.setOnFinished(e -> nodeGroup.getChildren().remove(pane));
            pt.getChildren().add(ft);
        }

        for (String key : targetCoords.keySet()) {
            Point2D p = targetCoords.get(key);
            NodeVisualState vs = targetVisuals.get(key);

            StackPane pane = nodeGuis.get(key);
            if (pane == null) {
                Circle circle = new Circle(vs.isNil ? 7 : NODE_RADIUS);
                circle.setFill(vs.fill);
                circle.setStroke(vs.stroke);
                circle.setStrokeWidth(vs.strokeWidth);

                Text text = new Text(vs.text);
                text.setFill(vs.isNil ? Color.web("#64748b") : (vs.fill.equals(Color.web("#e8eaf0")) ? Color.web("#1b1f25") : Color.WHITE));
                text.setFont(Font.font("Consolas", vs.isNil ? 10 : 14));

                pane = new StackPane(circle, text);
                pane.setLayoutX(vs.isNil ? -7 : -NODE_RADIUS);
                pane.setLayoutY(vs.isNil ? -7 : -NODE_RADIUS);
                pane.setTranslateX(p.getX());
                
                // For a smooth entrance, slightly offset the Y start position
                pane.setTranslateY(p.getY() - 30);
                pane.setOpacity(durationMs > 0 ? 0 : 1);

                nodeGroup.getChildren().add(pane);
                nodeGuis.put(key, pane);

                FadeTransition ft = new FadeTransition(dur, pane);
                ft.setToValue(1);
                pt.getChildren().add(ft);

                TranslateTransition tt = new TranslateTransition(dur, pane);
                tt.setToX(p.getX());
                tt.setToY(p.getY());
                pt.getChildren().add(tt);
            } else {
                // Animate existing node properties and position dynamically
                Circle circle = (Circle) pane.getChildren().get(0);
                Text text = (Text) pane.getChildren().get(1);

                TranslateTransition tt = new TranslateTransition(dur, pane);
                tt.setToX(p.getX());
                tt.setToY(p.getY());
                pt.getChildren().add(tt);

                if (!circle.getFill().equals(vs.fill)) {
                    FillTransition ft = new FillTransition(dur, circle, (Color) circle.getFill(), vs.fill);
                    pt.getChildren().add(ft);
                }

                if (!circle.getStroke().equals(vs.stroke)) {
                    StrokeTransition st = new StrokeTransition(dur, circle, (Color) circle.getStroke(), vs.stroke);
                    pt.getChildren().add(st);
                }

                circle.setStrokeWidth(vs.strokeWidth);
                text.setFill(vs.isNil ? Color.web("#64748b") : (vs.fill.equals(Color.web("#e8eaf0")) ? Color.web("#1b1f25") : Color.WHITE));
            }
        }

        // 2. Process Edges
        Set<String> edgesToRemove = new HashSet<>(edgeGuis.keySet());
        edgesToRemove.removeAll(targetEdges.keySet());
        for (String cKey : edgesToRemove) {
            Line line = edgeGuis.remove(cKey);
            FadeTransition ft = new FadeTransition(dur, line);
            ft.setToValue(0);
            ft.setOnFinished(e -> edgeGroup.getChildren().remove(line));
            pt.getChildren().add(ft);
        }

        for (String cKey : targetEdges.keySet()) {
            String pKey = targetEdges.get(cKey);
            Line line = edgeGuis.get(cKey);
            
            if (line == null) {
                line = new Line();
                line.setOpacity(durationMs > 0 ? 0 : 1);
                edgeGroup.getChildren().add(line);
                edgeGuis.put(cKey, line);

                FadeTransition ft = new FadeTransition(dur, line);
                ft.setToValue(1);
                pt.getChildren().add(ft);
            }
            // Bind line ends directly to the translate properties of the nodes
            // This ensures lines completely cleanly stick to nodes during glide!
            line.startXProperty().bind(nodeGuis.get(pKey).translateXProperty());
            line.startYProperty().bind(nodeGuis.get(pKey).translateYProperty());
            line.endXProperty().bind(nodeGuis.get(cKey).translateXProperty());
            line.endYProperty().bind(nodeGuis.get(cKey).translateYProperty());

            NodeVisualState childVs = targetVisuals.get(cKey);
            NodeVisualState parentVs = targetVisuals.get(pKey);
            boolean isHighlight = (childVs != null && childVs.stroke.equals(Color.CYAN)) || (parentVs != null && parentVs.stroke.equals(Color.CYAN));
            boolean isVisited = childVs != null && childVs.stroke.equals(Color.web("#10b981"));
            Color strokeColor = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#8a8f98"));
            double width = isHighlight ? 2.8 : (isVisited ? 2.4 : 1.8);

            if (!line.getStroke().equals(strokeColor)) {
                StrokeTransition st = new StrokeTransition(dur, line, (Color) line.getStroke(), strokeColor);
                pt.getChildren().add(st);
            }
            line.setStrokeWidth(width);
        }

        pt.play();
    }

    private void extractGenericTargets(MultiNode<Integer> node, MultiNode<Integer> parent, Map<Object, Point2D> coords,
                                       Map<String, Point2D> targetCoords, Map<String, String> targetEdges, Map<String, NodeVisualState> targetVisuals) {
        String key = String.valueOf(node.value);
        targetCoords.put(key, coords.get(node));

        boolean isHighlight = highlightSet.contains(node.value);
        boolean isVisited = visitedSet.contains(node.value);
        Color fill = Color.web("#e8eaf0");
        Color stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#5a616a"));
        double strokeWidth = isHighlight ? 4.0 : (isVisited ? 3.0 : 2.0);

        targetVisuals.put(key, new NodeVisualState(fill, stroke, strokeWidth, key, false));

        if (parent != null) {
            targetEdges.put(key, String.valueOf(parent.value));
        }

        for (MultiNode<Integer> child : node.children) {
            extractGenericTargets(child, node, coords, targetCoords, targetEdges, targetVisuals);
        }
    }

    private void extractBinaryTargets(BinaryNode<Integer> node, BinaryNode<Integer> parent, Map<Object, Point2D> coords,
                                      Map<String, Point2D> targetCoords, Map<String, String> targetEdges, Map<String, NodeVisualState> targetVisuals) {
        String key = String.valueOf(node.value);
        targetCoords.put(key, coords.get(node));

        boolean isHighlight = highlightSet.contains(node.value);
        boolean isVisited = visitedSet.contains(node.value);
        Color fill = Color.web("#e8eaf0");
        Color stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#5a616a"));
        double strokeWidth = isHighlight ? 4.0 : (isVisited ? 3.0 : 2.0);

        targetVisuals.put(key, new NodeVisualState(fill, stroke, strokeWidth, key, false));

        if (parent != null) {
            targetEdges.put(key, String.valueOf(parent.value));
        }

        if (node.left != null) extractBinaryTargets(node.left, node, coords, targetCoords, targetEdges, targetVisuals);
        if (node.right != null) extractBinaryTargets(node.right, node, coords, targetCoords, targetEdges, targetVisuals);
    }

    private void extractRBTargets(RBNode<Integer> node, RBNode<Integer> parent, Map<Object, Point2D> coords,
                                  Map<String, Point2D> targetCoords, Map<String, String> targetEdges, Map<String, NodeVisualState> targetVisuals,
                                  RBNode<Integer> nil, String dir) {
        if (node == nil) {
            if (showNilLeaves && parent != null) {
                String nilKey = "NIL_" + parent.value + "_" + dir;
                Point2D parentP = coords.get(parent);
                double nx = parentP.getX() + (dir.equals("L") ? -1 : 1) * X_SPACING * 0.35;
                double ny = parentP.getY() + Y_SPACING * 0.65;
                targetCoords.put(nilKey, new Point2D(nx, ny));
                targetVisuals.put(nilKey, new NodeVisualState(Color.web("#111827"), Color.web("#334155"), 1.2, "NIL", true));
                targetEdges.put(nilKey, String.valueOf(parent.value));
            }
            return;
        }

        String key = String.valueOf(node.value);
        targetCoords.put(key, coords.get(node));

        boolean isHighlight = highlightSet.contains(node.value);
        boolean isVisited = visitedSet.contains(node.value);
        Color fill = node.red ? Color.web("#ef4444") : Color.web("#1f2937");
        Color stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#10b981") : Color.web("#111827"));
        double strokeWidth = isHighlight ? 4.0 : (isVisited ? 3.0 : 2.2);

        targetVisuals.put(key, new NodeVisualState(fill, stroke, strokeWidth, key, false));

        if (parent != null) {
            targetEdges.put(key, String.valueOf(parent.value));
        }

        extractRBTargets((RBNode<Integer>)node.left, node, coords, targetCoords, targetEdges, targetVisuals, nil, "L");
        extractRBTargets((RBNode<Integer>)node.right, node, coords, targetCoords, targetEdges, targetVisuals, nil, "R");
    }

    // --- Original coordinate layout code preserved below ---

    private double computeGenericWidths(MultiNode<Integer> node, Map<Object, Double> cache) {
        if (node.children.isEmpty()) {
            cache.put(node, NODE_RADIUS * 2 + 40);
            return cache.get(node);
        }
        double sum = 0;
        for (MultiNode<Integer> child : node.children) {
            sum += computeGenericWidths(child, cache);
        }
        double gap = 40;
        if (node.children.size() > 1) {
            sum += gap * (node.children.size() - 1);
        }
        cache.put(node, sum);
        return sum;
    }

    private void assignGenericPositions(MultiNode<Integer> node, int depth, double xStart, double totalWidth, double originY, Map<Object, Point2D> coords, Map<Object, Double> widthCache) {
        double xCenter = xStart + totalWidth / 2.0;
        double y = originY + depth * Y_SPACING;
        coords.put(node, new Point2D(xCenter, y));
        double childX = xStart;

        for (MultiNode<Integer> child : node.children) {
            double childWidth = widthCache.get(child);
            assignGenericPositions(child, depth + 1, childX, childWidth, originY, coords, widthCache);
            childX += childWidth + 40;
        }
    }

    private void assignBinaryPositions(BinaryNode<Integer> node, int depth, double[] nextX, Map<Object, Point2D> coords) {
        if (node == null) return;
        assignBinaryPositions((BinaryNode<Integer>)node.left, depth + 1, nextX, coords);
        coords.put(node, new Point2D(nextX[0] * X_SPACING, 60 + depth * Y_SPACING));
        nextX[0] += 1;
        assignBinaryPositions((BinaryNode<Integer>)node.right, depth + 1, nextX, coords);
    }

    private void assignBinaryPositionsRB(RBNode<Integer> node, int depth, double[] nextX, Map<Object, Point2D> coords, RBNode<Integer> nil) {
        if (node == null || node == nil) return;
        assignBinaryPositionsRB((RBNode<Integer>)node.left, depth + 1, nextX, coords, nil);
        coords.put(node, new Point2D(nextX[0] * X_SPACING, 60 + depth * Y_SPACING));
        nextX[0] += 1;
        assignBinaryPositionsRB((RBNode<Integer>)node.right, depth + 1, nextX, coords, nil);
    }
}
