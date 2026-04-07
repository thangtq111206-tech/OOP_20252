package com.example.oop_20252.service.snapshots;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.generic.GenericTree;

public class GenericTreeSnapshot implements TreeSnapshot {
    private final GenericTree.Node root;

    public GenericTreeSnapshot(GenericTree.Node root) {
        this.root = root;
    }

    @Override
    public TreeKind getKind() {
        return TreeKind.GENERIC;
    }

    public GenericTree.Node getRoot() {
        return root;
    }
}

