package com.example.oop_20252.model.binary;

public class AVLNode<T extends Comparable<T>> extends BinaryNode<T> {
    public int height;

    public AVLNode(T value) {
        super(value);
        this.height = 1;
    }
}
