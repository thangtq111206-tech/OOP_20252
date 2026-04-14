package com.example.oop_20252.model.binary;

import com.example.oop_20252.model.core.AbstractTree;

public abstract class AbstractBinaryTree<T extends Comparable<T>> extends AbstractTree<T> {

    @Override
    public BinaryNode<T> getRoot() {
        return (BinaryNode<T>) root;
    }

    public void setRoot(BinaryNode<T> newRoot) {
        this.root = newRoot;
    }

    public BinaryNode<T> find(T value) {
        BinaryNode<T> cur = getRoot();
        while (cur != null) {
            int cmp = value.compareTo(cur.getValue());
            if (cmp == 0) return cur;
            if (cmp < 0) cur = cur.left;
            else cur = cur.right;
        }
        return null;
    }

    public BinaryNode<T> minimum(BinaryNode<T> start) {
        BinaryNode<T> cur = start;
        while (cur != null && cur.left != null) cur = cur.left;
        return cur;
    }

    public void transplant(BinaryNode<T> u, BinaryNode<T> v) {
        if (u.parent == null) {
            setRoot(v);
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        if (v != null) v.parent = u.parent;
    }

    public void leftRotate(BinaryNode<T> x) {
        BinaryNode<T> y = x.right;
        x.right = y.left;
        if (y.left != null) y.left.parent = x;

        y.parent = x.parent;
        if (x.parent == null) {
            setRoot(y);
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }

        y.left = x;
        x.parent = y;
    }

    public void rightRotate(BinaryNode<T> y) {
        BinaryNode<T> x = y.left;
        y.left = x.right;
        if (x.right != null) x.right.parent = y;

        x.parent = y.parent;
        if (y.parent == null) {
            setRoot(x);
        } else if (y == y.parent.right) {
            y.parent.right = x;
        } else {
            y.parent.left = x;
        }

        x.right = y;
        y.parent = x;
    }
}
