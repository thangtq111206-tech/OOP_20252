package com.example.oop_20252.service.snapshots;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.redblack.RBNode;

public class RedBlackTreeSnapshot implements TreeSnapshot {
    private final RBNode<Integer> root;
    private final RBNode<Integer> nil;

    public RedBlackTreeSnapshot(RBNode<Integer> root, RBNode<Integer> nil) {
        this.root = root;
        this.nil = nil;
    }

    @Override
    public TreeKind getKind() {
        return TreeKind.RED_BLACK;
    }

    public RBNode<Integer> getRoot() {
        return root;
    }

    public RBNode<Integer> getNil() {
        return nil;
    }
}
