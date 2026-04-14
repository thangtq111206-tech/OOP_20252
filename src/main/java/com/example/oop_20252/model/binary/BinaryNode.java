package com.example.oop_20252.model.binary;

import com.example.oop_20252.model.core.INode;

import java.util.ArrayList;
import java.util.List;

public class BinaryNode<T extends Comparable<T>> implements INode<T> {
    public T value;
    public BinaryNode<T> left;
    public BinaryNode<T> right;
    public BinaryNode<T> parent;

    public BinaryNode(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public List<INode<T>> getChildren() {
        List<INode<T>> children = new ArrayList<>();
        if (left != null) children.add(left);
        if (right != null) children.add(right);
        return children;
    }
}
