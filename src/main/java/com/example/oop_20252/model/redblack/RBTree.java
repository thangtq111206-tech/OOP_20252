package com.example.oop_20252.model.redblack;

import com.example.oop_20252.model.binary.AbstractBinaryTree;
import com.example.oop_20252.model.binary.BinaryNode;

public class RBTree<T extends Comparable<T>> extends AbstractBinaryTree<T> {
    private final RBNode<T> nil;

    public RBTree() {
        nil = new RBNode<>(null);
        nil.red = false; // NIL is black
        nil.left = nil;
        nil.right = nil;
        nil.parent = nil;
        setRoot(nil);
    }

    public RBTree(BinaryNode<T> snapshotRoot, BinaryNode<T> snapshotNil) {
        nil = new RBNode<>(null);
        nil.red = false;
        nil.left = nil;
        nil.right = nil;
        nil.parent = nil;

        if (snapshotRoot == null || snapshotRoot == snapshotNil) {
            setRoot(nil);
        } else {
            setRoot(deepCopyNodeFromSnapshot(snapshotRoot, nil, snapshotNil));
        }
    }

    public RBNode<T> getNil() {
        return nil;
    }

    @Override
    public boolean isEmpty() {
        return getRoot() == nil;
    }

    public RBNode<T> createNode(T value) {
        RBNode<T> n = new RBNode<>(value);
        n.red = true;
        n.left = nil;
        n.right = nil;
        n.parent = nil;
        return n;
    }

    public RBTree<T> deepCopy() {
        RBTree<T> copy = new RBTree<>();
        if (this.getRoot() == this.nil) {
            copy.setRoot(copy.nil);
            return copy;
        }
        copy.setRoot(deepCopyNode(this.getRoot(), copy.nil, copy.nil));
        return copy;
    }

    private BinaryNode<T> deepCopyNodeFromSnapshot(BinaryNode<T> node, BinaryNode<T> parentInCopy, BinaryNode<T> snapshotNil) {
        if (node == snapshotNil) return nil;
        RBNode<T> copyNode = new RBNode<>(node.getValue());
        copyNode.red = ((RBNode<T>) node).red;
        copyNode.parent = parentInCopy;
        copyNode.left = (node.left == snapshotNil) ? nil : deepCopyNodeFromSnapshot(node.left, copyNode, snapshotNil);
        copyNode.right = (node.right == snapshotNil) ? nil : deepCopyNodeFromSnapshot(node.right, copyNode, snapshotNil);
        return copyNode;
    }

    private BinaryNode<T> deepCopyNode(BinaryNode<T> node, BinaryNode<T> parentInCopy, BinaryNode<T> copyNil) {
        RBNode<T> copyNode = new RBNode<>(node.getValue());
        copyNode.red = ((RBNode<T>) node).red;
        copyNode.parent = parentInCopy;
        copyNode.left = (node.left == this.nil) ? copyNil : deepCopyNode(node.left, copyNode, copyNil);
        copyNode.right = (node.right == this.nil) ? copyNil : deepCopyNode(node.right, copyNode, copyNil);
        return copyNode;
    }

    @Override
    public BinaryNode<T> find(T value) {
        BinaryNode<T> cur = getRoot();
        while (cur != nil) {
            int cmp = value.compareTo(cur.getValue());
            if (cmp == 0) return cur;
            if (cmp < 0) cur = cur.left;
            else cur = cur.right;
        }
        return null;
    }
    
    @Override
    public void leftRotate(BinaryNode<T> x) {
        BinaryNode<T> y = x.right;
        x.right = y.left;
        if (y.left != nil) y.left.parent = x;

        y.parent = x.parent;
        if (x.parent == nil) {
            setRoot(y);
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }

        y.left = x;
        x.parent = y;
    }

    @Override
    public void rightRotate(BinaryNode<T> y) {
        BinaryNode<T> x = y.left;
        y.left = x.right;
        if (x.right != nil) x.right.parent = y;

        x.parent = y.parent;
        if (y.parent == nil) {
            setRoot(x);
        } else if (y == y.parent.right) {
            y.parent.right = x;
        } else {
            y.parent.left = x;
        }

        x.right = y;
        y.parent = x;
    }

    @Override
    public void transplant(BinaryNode<T> u, BinaryNode<T> v) {
        if (u.parent == nil) {
            setRoot(v);
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        v.parent = u.parent;
    }

    public void insert(T val) {
        RBNode<T> z = createNode(val);
        RBNode<T> y = nil;
        RBNode<T> x = (RBNode<T>)getRoot();
        while (x != nil) {
            y = x;
            if (val.compareTo(x.getValue()) < 0) x = (RBNode<T>)x.left;
            else if (val.compareTo(x.getValue()) == 0) return;
            else x = (RBNode<T>)x.right;
        }
        z.parent = y;
        if (y == nil) setRoot(z);
        else if (val.compareTo(y.getValue()) < 0) y.left = z;
        else y.right = z;
        z.left = nil;
        z.right = nil;
        z.red = true;
        insertFixup(z);
    }
    
    private void insertFixup(RBNode<T> z) {
        while (((RBNode<T>)z.parent).red) {
            if (z.parent == z.parent.parent.left) {
                RBNode<T> y = (RBNode<T>)z.parent.parent.right;
                if (y.red) {
                    ((RBNode<T>)z.parent).red = false;
                    y.red = false;
                    ((RBNode<T>)z.parent.parent).red = true;
                    z = (RBNode<T>)z.parent.parent;
                } else {
                    if (z == z.parent.right) {
                        z = (RBNode<T>)z.parent;
                        leftRotate(z);
                    }
                    ((RBNode<T>)z.parent).red = false;
                    ((RBNode<T>)z.parent.parent).red = true;
                    rightRotate(z.parent.parent);
                }
            } else {
                RBNode<T> y = (RBNode<T>)z.parent.parent.left;
                if (y.red) {
                    ((RBNode<T>)z.parent).red = false;
                    y.red = false;
                    ((RBNode<T>)z.parent.parent).red = true;
                    z = (RBNode<T>)z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        z = (RBNode<T>)z.parent;
                        rightRotate(z);
                    }
                    ((RBNode<T>)z.parent).red = false;
                    ((RBNode<T>)z.parent.parent).red = true;
                    leftRotate(z.parent.parent);
                }
            }
        }
        ((RBNode<T>)getRoot()).red = false;
    }
    
    public void delete(T val) {
        BinaryNode<T> z = find(val);
        if (z == null || z == nil) return;
        if (z.left == nil) transplant(z, z.right);
        else if (z.right == nil) transplant(z, z.left);
        else {
            BinaryNode<T> y = minimum(z.right);
            if (y.parent != z) {
                transplant(y, y.right);
                y.right = z.right;
                y.right.parent = y;
            }
            transplant(z, y);
            y.left = z.left;
            y.left.parent = y;
        }
    }
}
