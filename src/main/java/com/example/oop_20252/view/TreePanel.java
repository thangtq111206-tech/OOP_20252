package com.example.oop_20252.view;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.binary.BinarySearchTree;
import com.example.oop_20252.model.generic.GenericTree;
import com.example.oop_20252.model.redblack.RedBlackTree;
import com.example.oop_20252.service.snapshots.BinaryTreeSnapshot;
import com.example.oop_20252.service.snapshots.GenericTreeSnapshot;
import com.example.oop_20252.service.snapshots.RedBlackTreeSnapshot;
import com.example.oop_20252.service.snapshots.TreeSnapshot;
import com.example.oop_20252.util.TreeLayoutConstants;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreePanel extends Pane {
    private final Group rootGroup = new Group();

    private static final double NODE_RADIUS = TreeLayoutConstants.NODE_RADIUS;
    private static final double X_SPACING = TreeLayoutConstants.X_SPACING;
    private static final double Y_SPACING = TreeLayoutConstants.Y_SPACING;

    private Set<Integer> highlightSet = Set.of();
    private Set<Integer> visitedSet = Set.of();

    public TreePanel() {
        getChildren().add(rootGroup);
        getStyleClass().add("tree-panel-canvas");
    }

    public void render(TreeSnapshot snapshot, List<Integer> highlightValues, List<Integer> visitedValues) {
        rootGroup.getChildren().clear();
        if (snapshot == null) return;

        highlightSet = highlightValues == null ? Set.of() : new HashSet<>(highlightValues);
        visitedSet = visitedValues == null ? Set.of() : new HashSet<>(visitedValues);

        TreeKind kind = snapshot.getKind();

        if (kind == TreeKind.GENERIC) {
            renderGeneric(((GenericTreeSnapshot) snapshot).getRoot());
        } else if (kind == TreeKind.BINARY) {
            renderBinary(((BinaryTreeSnapshot) snapshot).getRoot());
        } else if (kind == TreeKind.RED_BLACK) {
            renderRedBlack((RedBlackTreeSnapshot) snapshot);
        }
    }

    private void renderGeneric(GenericTree.Node root) {
        if (root == null) return;
        // Coordinates for each node object reference.
        Map<Object, Point2D> coords = new IdentityHashMap<>();
        Map<Object, Double> widths = new IdentityHashMap<>();
        IdentityHashMap<Object, Double> widthCache = (IdentityHashMap<Object, Double>) widths;

        double totalWidth = computeGenericWidths(root, widthCache);
        if (totalWidth <= 0) totalWidth = 1;

        double originX = 60;
        double originY = 60;
        double assignStartX = originX;
        // Root width is implied by totalWidth; we keep this value for future spacing tweaks.
        double rootWidth = widthCache.get(root);
        assignGenericPositions(root, 0, assignStartX, totalWidth, originY, coords, widthCache);
        drawEdgesGeneric(root, coords, visitedSet, highlightSet);
        drawNodesGeneric(root, coords, visitedSet, highlightSet);
    }

    private void renderBinary(BinarySearchTree.Node root) {
        if (root == null) return;
        Map<Object, Point2D> coords = new IdentityHashMap<>();
        double[] nextX = new double[]{1};
        assignBinaryPositions(root, 0, nextX, coords);
        drawEdgesBinary(root, coords, visitedSet, highlightSet);
        drawNodesBinary(root, coords, visitedSet, highlightSet);
    }

    private void renderRedBlack(RedBlackTreeSnapshot snapshot) {
        RedBlackTree.Node root = snapshot.getRoot();
        if (root == null) return;
        Map<Object, Point2D> coords = new IdentityHashMap<>();
        double[] nextX = new double[]{1};
        assignBinaryPositionsRB(root, 0, nextX, coords, snapshot.getNil());
        drawEdgesRB(root, coords, visitedSet, highlightSet, snapshot.getNil());
        drawNodesRB(root, coords, visitedSet, highlightSet, snapshot.getNil());
    }

    private double computeGenericWidths(GenericTree.Node node, Map<Object, Double> cache) {
        if (node.children.isEmpty()) {
            cache.put(node, NODE_RADIUS * 2 + 40);
            return cache.get(node);
        }
        double sum = 0;
        for (GenericTree.Node child : node.children) {
            sum += computeGenericWidths(child, cache);
        }
        double gap = 40;
        if (node.children.size() > 1) {
            sum += gap * (node.children.size() - 1);
        }
        cache.put(node, sum);
        return sum;
    }

    private void assignGenericPositions(
            GenericTree.Node node,
            int depth,
            double xStart,
            double totalWidth,
            double originY,
            Map<Object, Point2D> coords,
            Map<Object, Double> widthCache
    ) {
        double nodeWidth = widthCache.get(node);
        double xCenter = xStart + totalWidth / 2.0;
        double y = originY + depth * Y_SPACING;
        coords.put(node, new Point2D(xCenter, y));
        double childX = xStart;

        for (GenericTree.Node child : node.children) {
            double childWidth = widthCache.get(child);
            assignGenericPositions(child, depth + 1, childX, childWidth, originY, coords, widthCache);
            childX += childWidth + 40; // gap between children
        }
    }

    private void assignBinaryPositions(BinarySearchTree.Node node, int depth, double[] nextX, Map<Object, Point2D> coords) {
        if (node == null) return;
        assignBinaryPositions(node.left, depth + 1, nextX, coords);
        coords.put(node, new Point2D(nextX[0] * X_SPACING, 60 + depth * Y_SPACING));
        nextX[0] += 1;
        assignBinaryPositions(node.right, depth + 1, nextX, coords);
    }

    private void assignBinaryPositionsRB(RedBlackTree.Node node, int depth, double[] nextX, Map<Object, Point2D> coords, RedBlackTree.Node nil) {
        if (node == null || node == nil) return;
        assignBinaryPositionsRB(node.left, depth + 1, nextX, coords, nil);
        coords.put(node, new Point2D(nextX[0] * X_SPACING, 60 + depth * Y_SPACING));
        nextX[0] += 1;
        assignBinaryPositionsRB(node.right, depth + 1, nextX, coords, nil);
    }

    private void drawEdgesGeneric(
            GenericTree.Node root,
            Map<Object, Point2D> coords,
            Set<Integer> visitedValues,
            Set<Integer> highlightValues
    ) {
        Deque<GenericTree.Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            GenericTree.Node cur = stack.pop();
            Point2D p = coords.get(cur);
            if (p == null) continue;
            for (GenericTree.Node child : cur.children) {
                Point2D c = coords.get(child);
                if (c == null) continue;
                boolean isHighlight = highlightValues.contains(child.value) || highlightValues.contains(cur.value);
                boolean isVisited = visitedValues.contains(child.value);
                Color stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#8a8f98"));
                double width = isHighlight ? 2.8 : (isVisited ? 2.4 : 1.8);
                Line line = new Line(p.getX(), p.getY(), c.getX(), c.getY());
                line.setStroke(stroke);
                line.setStrokeWidth(width);
                rootGroup.getChildren().add(line);
                stack.push(child);
            }
        }
    }

    private void drawNodesGeneric(
            GenericTree.Node root,
            Map<Object, Point2D> coords,
            Set<Integer> visitedValues,
            Set<Integer> highlightValues
    ) {
        Deque<GenericTree.Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            GenericTree.Node cur = stack.pop();
            Point2D p = coords.get(cur);
            if (p == null) continue;

            boolean isHighlight = highlightValues.contains(cur.value);
            boolean isVisited = visitedValues.contains(cur.value);
            Color fill = Color.web("#e8eaf0");
            Color stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#5a616a"));
            double strokeWidth = isHighlight ? 4.0 : (isVisited ? 3.0 : 2.0);
            Circle circle = new Circle(p.getX(), p.getY(), NODE_RADIUS);
            circle.setFill(fill);
            circle.setStroke(stroke);
            circle.setStrokeWidth(strokeWidth);

            Text text = new Text(p.getX() - 7, p.getY() + 5, String.valueOf(cur.value));
            text.setFill(Color.web("#1b1f25"));
            text.setFont(Font.font("Consolas", 14));

            rootGroup.getChildren().addAll(circle, text);
            for (GenericTree.Node child : cur.children) stack.push(child);
        }
    }

    private void drawEdgesBinary(
            BinarySearchTree.Node root,
            Map<Object, Point2D> coords,
            Set<Integer> visitedValues,
            Set<Integer> highlightValues
    ) {
        if (root == null) return;
        Deque<BinarySearchTree.Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            BinarySearchTree.Node cur = stack.pop();
            Point2D p = coords.get(cur);
            if (p == null) continue;
            if (cur.left != null) {
                Point2D c = coords.get(cur.left);
                if (c != null) {
                    boolean isHighlight = highlightValues.contains(cur.left.value) || highlightValues.contains(cur.value);
                    boolean isVisited = visitedValues.contains(cur.left.value);
                    Color stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#8a8f98"));
                    double width = isHighlight ? 2.8 : (isVisited ? 2.4 : 1.8);
                    Line line = new Line(p.getX(), p.getY(), c.getX(), c.getY());
                    line.setStroke(stroke);
                    line.setStrokeWidth(width);
                    rootGroup.getChildren().add(line);
                    stack.push(cur.left);
                }
            }
            if (cur.right != null) {
                Point2D c = coords.get(cur.right);
                if (c != null) {
                    boolean isHighlight = highlightValues.contains(cur.right.value) || highlightValues.contains(cur.value);
                    boolean isVisited = visitedValues.contains(cur.right.value);
                    Color stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#8a8f98"));
                    double width = isHighlight ? 2.8 : (isVisited ? 2.4 : 1.8);
                    Line line = new Line(p.getX(), p.getY(), c.getX(), c.getY());
                    line.setStroke(stroke);
                    line.setStrokeWidth(width);
                    rootGroup.getChildren().add(line);
                    stack.push(cur.right);
                }
            }
        }
    }

    private void drawNodesBinary(
            BinarySearchTree.Node root,
            Map<Object, Point2D> coords,
            Set<Integer> visitedValues,
            Set<Integer> highlightValues
    ) {
        if (root == null) return;
        Deque<BinarySearchTree.Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            BinarySearchTree.Node cur = stack.pop();
            Point2D p = coords.get(cur);
            if (p == null) continue;
            boolean isHighlight = highlightValues.contains(cur.value);
            boolean isVisited = visitedValues.contains(cur.value);

            Circle circle = new Circle(p.getX(), p.getY(), NODE_RADIUS);
            circle.setFill(Color.web("#e8eaf0"));
            circle.setStroke(isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#5a616a")));
            circle.setStrokeWidth(isHighlight ? 4.0 : (isVisited ? 3.0 : 2.0));

            Text text = new Text(p.getX() - 7, p.getY() + 5, String.valueOf(cur.value));
            text.setFill(Color.web("#1b1f25"));
            text.setFont(Font.font("Consolas", 14));

            rootGroup.getChildren().addAll(circle, text);

            if (cur.left != null) stack.push(cur.left);
            if (cur.right != null) stack.push(cur.right);
        }
    }

    private void drawEdgesRB(
            RedBlackTree.Node root,
            Map<Object, Point2D> coords,
            Set<Integer> visitedValues,
            Set<Integer> highlightValues,
            RedBlackTree.Node nil
    ) {
        if (root == null || root == nil) return;
        Deque<RedBlackTree.Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            RedBlackTree.Node cur = stack.pop();
            Point2D p = coords.get(cur);
            if (p == null) continue;

            if (cur.left != nil) {
                Point2D c = coords.get(cur.left);
                if (c != null) {
                    boolean isHighlight = highlightValues.contains(cur.left.value) || highlightValues.contains(cur.value);
                    boolean isVisited = visitedValues.contains(cur.left.value);
                    Color stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#8a8f98"));
                    double width = isHighlight ? 2.8 : (isVisited ? 2.4 : 1.8);
                    Line line = new Line(p.getX(), p.getY(), c.getX(), c.getY());
                    line.setStroke(stroke);
                    line.setStrokeWidth(width);
                    rootGroup.getChildren().add(line);
                    stack.push(cur.left);
                }
            }
            if (cur.right != nil) {
                Point2D c = coords.get(cur.right);
                if (c != null) {
                    boolean isHighlight = highlightValues.contains(cur.right.value) || highlightValues.contains(cur.value);
                    boolean isVisited = visitedValues.contains(cur.right.value);
                    Color stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#8a8f98"));
                    double width = isHighlight ? 2.8 : (isVisited ? 2.4 : 1.8);
                    Line line = new Line(p.getX(), p.getY(), c.getX(), c.getY());
                    line.setStroke(stroke);
                    line.setStrokeWidth(width);
                    rootGroup.getChildren().add(line);
                    stack.push(cur.right);
                }
            }
        }
    }

    private void drawNodesRB(
            RedBlackTree.Node root,
            Map<Object, Point2D> coords,
            Set<Integer> visitedValues,
            Set<Integer> highlightValues,
            RedBlackTree.Node nil
    ) {
        if (root == null || root == nil) return;
        Deque<RedBlackTree.Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            RedBlackTree.Node cur = stack.pop();
            Point2D p = coords.get(cur);
            if (p == null) continue;

            boolean isHighlight = highlightValues.contains(cur.value);
            boolean isVisited = visitedValues.contains(cur.value);
            Color fill = cur.red ? Color.web("#ff6b6b") : Color.web("#5b616e");
            Color stroke = isHighlight ? Color.CYAN : (isVisited ? Color.web("#2ecc71") : Color.web("#2b2f36"));

            Circle circle = new Circle(p.getX(), p.getY(), NODE_RADIUS);
            circle.setFill(fill);
            circle.setStroke(stroke);
            circle.setStrokeWidth(isHighlight ? 4.0 : (isVisited ? 3.0 : 2.2));

            Text text = new Text(p.getX() - 7, p.getY() + 5, String.valueOf(cur.value));
            text.setFill(Color.WHITE);
            text.setFont(Font.font("Consolas", 14));

            rootGroup.getChildren().addAll(circle, text);

            if (cur.left != nil) stack.push(cur.left);
            if (cur.right != nil) stack.push(cur.right);
        }
    }
}

