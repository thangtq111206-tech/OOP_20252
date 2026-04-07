package com.example.oop_20252.service.snapshots;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.binary.BinarySearchTree;

public class BinaryTreeSnapshot implements TreeSnapshot {
    private final BinarySearchTree.Node root;

    public BinaryTreeSnapshot(BinarySearchTree.Node root) {
        this.root = root;
    }

    @Override
    public TreeKind getKind() {
        return TreeKind.BINARY;
    }

    public BinarySearchTree.Node getRoot() {
        return root;
    }
}

