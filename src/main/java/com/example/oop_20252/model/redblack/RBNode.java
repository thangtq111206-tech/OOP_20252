package com.example.oop_20252.model.redblack;

import com.example.oop_20252.model.binary.BinaryNode;

public class RBNode<T extends Comparable<T>> extends BinaryNode<T> {
    public boolean red;

    public RBNode(T value) {
        super(value);
        this.red = true; 
    }
}
