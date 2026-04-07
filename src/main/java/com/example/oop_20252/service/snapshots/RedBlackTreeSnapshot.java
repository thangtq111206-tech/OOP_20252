package com.example.oop_20252.service.snapshots;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.redblack.RedBlackTree;

public class RedBlackTreeSnapshot implements TreeSnapshot {
    private final RedBlackTree.Node root;
    private final RedBlackTree.Node nil;

    public RedBlackTreeSnapshot(RedBlackTree.Node root, RedBlackTree.Node nil) {
        this.root = root;
        this.nil = nil;
    }

    @Override
    public TreeKind getKind() {
        return TreeKind.RED_BLACK;
    }

    public RedBlackTree.Node getRoot() {
        return root;
    }

    public RedBlackTree.Node getNil() {
        return nil;
    }
}

