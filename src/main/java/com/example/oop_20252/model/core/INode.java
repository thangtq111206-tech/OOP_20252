package com.example.oop_20252.model.core;

import java.util.List;

public interface INode<T extends Comparable<T>> {
    T getValue();
    void setValue(T value);
    List<? extends INode<T>> getChildren();
}
