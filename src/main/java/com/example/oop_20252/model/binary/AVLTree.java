package com.example.oop_20252.model.binary;

public class AVLTree<T extends Comparable<T>> extends BST<T> {
    
    public AVLTree() {}

    public AVLTree(BinaryNode<T> snapshotRoot) {
        this.root = snapshotRoot == null ? null : deepCopyNode(snapshotRoot, null);
    }

    @Override
    protected BinaryNode<T> createNode(T value) {
        return new AVLNode<>(value);
    }

    @Override
    public AVLTree<T> deepCopy() {
        AVLTree<T> copy = new AVLTree<>();
        if (getRoot() != null) {
            copy.setRoot(deepCopyNode(getRoot(), null));
        }
        return copy;
    }

    @Override
    protected BinaryNode<T> deepCopyNode(BinaryNode<T> node, BinaryNode<T> parent) {
        if (node == null) return null;
        AVLNode<T> copy = (AVLNode<T>) createNode(node.getValue());
        if (node instanceof AVLNode) {
            copy.height = ((AVLNode<T>) node).height;
        }
        copy.parent = parent;
        copy.left = deepCopyNode(node.left, copy);
        copy.right = deepCopyNode(node.right, copy);
        return copy;
    }
}
