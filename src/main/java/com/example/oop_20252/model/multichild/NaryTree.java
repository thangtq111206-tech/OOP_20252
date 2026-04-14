package com.example.oop_20252.model.multichild;

import com.example.oop_20252.model.core.AbstractTree;

import java.util.ArrayDeque;
import java.util.Deque;

public class NaryTree<T extends Comparable<T>> extends AbstractTree<T> {

    public NaryTree() {
    }

    public NaryTree(MultiNode<T> snapshotRoot) {
        this.root = snapshotRoot == null ? null : deepCopyNode(snapshotRoot);
    }
    
    @Override
    public MultiNode<T> getRoot() {
        return (MultiNode<T>) root;
    }
    
    public void setRoot(MultiNode<T> node) {
        this.root = node;
    }

    public MultiNode<T> find(T value) {
        if (root == null) return null;
        Deque<MultiNode<T>> stack = new ArrayDeque<>();
        stack.push(getRoot());
        while (!stack.isEmpty()) {
            MultiNode<T> cur = stack.pop();
            if (cur.value.compareTo(value) == 0) return cur;
            for (MultiNode<T> child : cur.children) stack.push(child);
        }
        return null;
    }

    public MultiNode<T> findParent(T value) {
        if (root == null) return null;
        if (getRoot().value.compareTo(value) == 0) return null;

        Deque<MultiNode<T>> stack = new ArrayDeque<>();
        stack.push(getRoot());
        while (!stack.isEmpty()) {
            MultiNode<T> cur = stack.pop();
            for (MultiNode<T> child : cur.children) {
                if (child.value.compareTo(value) == 0) return cur;
                stack.push(child);
            }
        }
        return null;
    }

    public NaryTree<T> deepCopy() {
        NaryTree<T> copy = new NaryTree<>();
        if (this.root != null) {
            copy.setRoot(deepCopyNode(getRoot()));
        }
        return copy;
    }

    private MultiNode<T> deepCopyNode(MultiNode<T> node) {
        if (node == null) return null;
        MultiNode<T> copy = new MultiNode<>(node.value);
        for (MultiNode<T> child : node.children) {
            copy.children.add(deepCopyNode(child));
        }
        return copy;
    }
}
