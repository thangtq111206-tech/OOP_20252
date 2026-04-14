package com.example.oop_20252.model.core;

public interface ITree<T extends Comparable<T>> {
    void clear();
    boolean isEmpty();
    INode<T> getRoot();
}
