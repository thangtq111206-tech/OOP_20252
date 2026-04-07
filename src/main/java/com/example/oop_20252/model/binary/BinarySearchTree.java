package com.example.oop_20252.model.binary;

public class BinarySearchTree {
    public static class Node {
        public int value;
        public Node left;
        public Node right;
        public Node parent;

        public Node(int value) {
            this.value = value;
        }
    }

    private Node root;

    public BinarySearchTree() {
    }

    // Builds a new tree by deep-copying the provided snapshot root.
    public BinarySearchTree(Node snapshotRoot) {
        this.root = snapshotRoot == null ? null : deepCopyNode(snapshotRoot, null);
    }

    public Node getRoot() {
        return root;
    }

    public void clear() {
        root = null;
    }

    public Node find(int value) {
        Node cur = root;
        while (cur != null) {
            if (value == cur.value) return cur;
            if (value < cur.value) cur = cur.left;
            else cur = cur.right;
        }
        return null;
    }

    public Node minimum(Node start) {
        Node cur = start;
        while (cur != null && cur.left != null) cur = cur.left;
        return cur;
    }

    public void transplant(Node u, Node v) {
        if (u.parent == null) {
            root = v;
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        if (v != null) v.parent = u.parent;
    }

    public BinarySearchTree deepCopy() {
        BinarySearchTree copy = new BinarySearchTree();
        if (this.root == null) return copy;
        copy.root = deepCopyNode(this.root, null);
        return copy;
    }

    private static Node deepCopyNode(Node node, Node parent) {
        Node copy = new Node(node.value);
        copy.parent = parent;
        if (node.left != null) copy.left = deepCopyNode(node.left, copy);
        if (node.right != null) copy.right = deepCopyNode(node.right, copy);
        return copy;
    }
}

