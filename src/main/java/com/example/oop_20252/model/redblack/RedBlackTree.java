package com.example.oop_20252.model.redblack;

public class RedBlackTree {
    public static class Node {
        public int value;
        public Node left;
        public Node right;
        public Node parent;
        public boolean red; // true = RED, false = BLACK

        public Node(int value) {
            this.value = value;
        }
    }

    private final Node nil;
    private Node root;

    public RedBlackTree() {
        nil = new Node(0);
        nil.red = false; // NIL is black
        nil.left = nil;
        nil.right = nil;
        nil.parent = nil;
        root = nil;
    }

    // Builds a new tree by deep-copying snapshot nodes (including the snapshot's nil sentinel).
    public RedBlackTree(Node snapshotRoot, Node snapshotNil) {
        nil = new Node(0);
        nil.red = false;
        nil.left = nil;
        nil.right = nil;
        nil.parent = nil;

        if (snapshotRoot == null || snapshotRoot == snapshotNil) {
            root = nil;
        } else {
            root = deepCopyNodeFromSnapshot(snapshotRoot, nil, snapshotNil);
        }
    }

    public Node getRoot() {
        return root;
    }

    public Node getNil() {
        return nil;
    }

    // Used by the service to attach a newly created node as the root.
    public void setRoot(Node newRoot) {
        this.root = newRoot == null ? nil : newRoot;
        if (this.root != nil) {
            this.root.parent = nil;
        }
    }

    public boolean isEmpty() {
        return root == nil;
    }

    public Node createNode(int value) {
        Node n = new Node(value);
        n.red = true; // newly inserted nodes are red
        n.left = nil;
        n.right = nil;
        n.parent = nil;
        return n;
    }

    public Node search(int value) {
        Node cur = root;
        while (cur != nil) {
            if (value == cur.value) return cur;
            if (value < cur.value) cur = cur.left;
            else cur = cur.right;
        }
        return null;
    }

    public Node minimum(Node start) {
        Node cur = start;
        while (cur != nil && cur.left != nil) cur = cur.left;
        return cur;
    }

    public void leftRotate(Node x) {
        Node y = x.right;
        x.right = y.left;
        if (y.left != nil) y.left.parent = x;

        y.parent = x.parent;
        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }

        y.left = x;
        x.parent = y;
    }

    public void rightRotate(Node y) {
        Node x = y.left;
        y.left = x.right;
        if (x.right != nil) x.right.parent = y;

        x.parent = y.parent;
        if (y.parent == nil) {
            root = x;
        } else if (y == y.parent.right) {
            y.parent.right = x;
        } else {
            y.parent.left = x;
        }

        x.right = y;
        y.parent = x;
    }

    public void transplant(Node u, Node v) {
        if (u.parent == nil) {
            root = v;
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        v.parent = u.parent;
    }

    public RedBlackTree deepCopy() {
        RedBlackTree copy = new RedBlackTree();
        if (this.root == this.nil) {
            copy.root = copy.nil;
            return copy;
        }
        copy.root = deepCopyNode(this.root, copy.nil, copy.nil);
        return copy;
    }

    private Node deepCopyNodeFromSnapshot(Node node, Node parentInCopy, Node snapshotNil) {
        if (node == snapshotNil) return nil;
        Node copyNode = new Node(node.value);
        copyNode.red = node.red;
        copyNode.parent = parentInCopy;
        copyNode.left = (node.left == snapshotNil) ? nil : deepCopyNodeFromSnapshot(node.left, copyNode, snapshotNil);
        copyNode.right = (node.right == snapshotNil) ? nil : deepCopyNodeFromSnapshot(node.right, copyNode, snapshotNil);
        return copyNode;
    }

    private Node deepCopyNode(Node node, Node parentInCopy, Node copyNil) {
        // node will never be the original NIL sentinel here.
        Node copyNode = new Node(node.value);
        copyNode.red = node.red;
        copyNode.parent = parentInCopy;
        copyNode.left = (node.left == this.nil) ? copyNil : deepCopyNode(node.left, copyNode, copyNil);
        copyNode.right = (node.right == this.nil) ? copyNil : deepCopyNode(node.right, copyNode, copyNil);
        return copyNode;
    }
}

