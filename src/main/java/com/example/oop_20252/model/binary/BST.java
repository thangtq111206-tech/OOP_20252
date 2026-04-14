package com.example.oop_20252.model.binary;

public class BST<T extends Comparable<T>> extends AbstractBinaryTree<T> {
    
    public BST() {}

    public BST(BinaryNode<T> snapshotRoot) {
        this.root = snapshotRoot == null ? null : deepCopyNode(snapshotRoot, null);
    }

    public BST<T> deepCopy() {
        BST<T> copy = new BST<>();
        if (getRoot() != null) {
            copy.setRoot(deepCopyNode(getRoot(), null));
        }
        return copy;
    }

    protected BinaryNode<T> createNode(T value) {
        return new BinaryNode<>(value);
    }

    protected BinaryNode<T> deepCopyNode(BinaryNode<T> node, BinaryNode<T> parent) {
        if (node == null) return null;
        BinaryNode<T> copy = createNode(node.getValue());
        copy.parent = parent;
        copy.left = deepCopyNode(node.left, copy);
        copy.right = deepCopyNode(node.right, copy);
        return copy;
    }

    public void insert(T val) {
        BinaryNode<T> z = createNode(val);
        BinaryNode<T> y = null;
        BinaryNode<T> x = getRoot();
        while (x != null) {
            y = x;
            if (val.compareTo(x.getValue()) < 0) x = x.left;
            else if (val.compareTo(x.getValue()) == 0) return; // ignore dupe
            else x = x.right;
        }
        z.parent = y;
        if (y == null) setRoot(z);
        else if (val.compareTo(y.getValue()) < 0) y.left = z;
        else y.right = z;
    }
    
    public void delete(T val) {
        BinaryNode<T> z = find(val);
        if (z == null) return;
        if (z.left == null) transplant(z, z.right);
        else if (z.right == null) transplant(z, z.left);
        else {
            BinaryNode<T> y = minimum(z.right);
            if (y.parent != z) {
                transplant(y, y.right);
                y.right = z.right;
                if (y.right != null) y.right.parent = y;
            }
            transplant(z, y);
            y.left = z.left;
            if (y.left != null) y.left.parent = y;
        }
    }
}
