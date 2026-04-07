package com.example.oop_20252.model.generic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class GenericTree {
    public static class Node {
        public int value;
        public final List<Node> children;

        public Node(int value) {
            this.value = value;
            this.children = new ArrayList<>();
        }
    }

    private Node root;

    public GenericTree() {
    }

    // Builds a new tree by deep-copying the provided snapshot root.
    public GenericTree(Node snapshotRoot) {
        this.root = snapshotRoot == null ? null : deepCopyNode(snapshotRoot);
    }

    public Node getRoot() {
        return root;
    }

    public void clear() {
        root = null;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public Node find(int value) {
        if (root == null) return null;
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node cur = stack.pop();
            if (cur.value == value) return cur;
            for (Node child : cur.children) stack.push(child);
        }
        return null;
    }

    public Node findParent(int value) {
        if (root == null) return null;
        if (root.value == value) return null;

        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node cur = stack.pop();
            for (Node child : cur.children) {
                if (child.value == value) return cur;
                stack.push(child);
            }
        }
        return null;
    }

    public GenericTree deepCopy() {
        GenericTree copy = new GenericTree();
        if (this.root == null) return copy;
        copy.root = deepCopyNode(this.root);
        return copy;
    }

    private static Node deepCopyNode(Node node) {
        Node copy = new Node(node.value);
        for (Node child : node.children) {
            copy.children.add(deepCopyNode(child));
        }
        return copy;
    }
}

