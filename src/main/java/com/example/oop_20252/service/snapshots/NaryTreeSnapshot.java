package com.example.oop_20252.service.snapshots;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.multichild.MultiNode;

public class NaryTreeSnapshot implements TreeSnapshot {
    private final MultiNode<Integer> root;

    public NaryTreeSnapshot(MultiNode<Integer> root) {
        this.root = root;
    }

    @Override
    public TreeKind getKind() {
        return TreeKind.N_ARY;
    }

    public MultiNode<Integer> getRoot() {
        return root;
    }
}
