package com.example.oop_20252.model.multichild;

import com.example.oop_20252.model.core.INode;

import java.util.ArrayList;
import java.util.List;

public class MultiNode<T extends Comparable<T>> implements INode<T> {
    public T value;
    public final List<MultiNode<T>> children;

    public MultiNode(T value) {
        this.value = value;
        this.children = new ArrayList<>();
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
        return new ArrayList<>(children);
    }
}
