package com.example.oop_20252.service.snapshots;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.binary.BinaryNode;

public class BinaryTreeSnapshot implements TreeSnapshot {
    private final BinaryNode<Integer> root;
    private final TreeKind kind;

    public BinaryTreeSnapshot(BinaryNode<Integer> root, TreeKind kind) {
        this.root = root;
        this.kind = kind;
    }

    @Override
    public TreeKind getKind() {
        return kind;
    }

    public BinaryNode<Integer> getRoot() {
        return root;
    }
}
