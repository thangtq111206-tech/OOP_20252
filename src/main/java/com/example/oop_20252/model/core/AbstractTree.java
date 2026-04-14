package com.example.oop_20252.model.core;

public abstract class AbstractTree<T extends Comparable<T>> implements ITree<T> {
    protected INode<T> root;
    
    @Override
    public INode<T> getRoot() {
        return root;
    }
    
    @Override
    public void clear() {
        root = null;
    }
    
    @Override
    public boolean isEmpty() {
        return root == null;
    }
}
