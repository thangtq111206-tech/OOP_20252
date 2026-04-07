package com.example.oop_20252;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.binary.BinarySearchTree;
import com.example.oop_20252.model.generic.GenericTree;
import com.example.oop_20252.model.redblack.RedBlackTree;
import com.example.oop_20252.service.OperationService;
import com.example.oop_20252.service.frames.OperationResult;
import com.example.oop_20252.service.frames.StepFrame;
import com.example.oop_20252.service.snapshots.BinaryTreeSnapshot;
import com.example.oop_20252.service.snapshots.GenericTreeSnapshot;
import com.example.oop_20252.service.snapshots.RedBlackTreeSnapshot;
import com.example.oop_20252.service.snapshots.TreeSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OperationServiceTest {

    private static TreeSnapshot emptySnapshot(TreeKind kind) {
        return switch (kind) {
            case GENERIC -> new GenericTreeSnapshot(new GenericTree().deepCopy().getRoot());
            case BINARY -> new BinaryTreeSnapshot(new BinarySearchTree().deepCopy().getRoot());
            case RED_BLACK -> {
                RedBlackTree t = new RedBlackTree();
                RedBlackTree copy = t.deepCopy();
                yield new RedBlackTreeSnapshot(copy.getRoot(), copy.getNil());
            }
        };
    }

    private static TreeSnapshot lastSnapshot(OperationResult r) {
        assertNotNull(r);
        assertNotNull(r.getFrames());
        assertFalse(r.getFrames().isEmpty());
        StepFrame last = r.getFrames().get(r.getFrames().size() - 1);
        assertNotNull(last.getSnapshot());
        return last.getSnapshot();
    }

    @Test
    void genericTree_allOperations_smoke() {
        OperationService s = new OperationService();
        TreeSnapshot cur = emptySnapshot(TreeKind.GENERIC);

        // Create
        cur = lastSnapshot(s.create(TreeKind.GENERIC, cur));

        // Insert root (parent not required when empty)
        cur = lastSnapshot(s.insert(TreeKind.GENERIC, cur, null, 10));

        // Insert child under parent 10
        cur = lastSnapshot(s.insert(TreeKind.GENERIC, cur, 10, 5));
        cur = lastSnapshot(s.insert(TreeKind.GENERIC, cur, 10, 15));

        // Search hit / miss
        OperationResult hit = s.search(TreeKind.GENERIC, cur, 15);
        StepFrame hitLast = hit.getFrames().get(hit.getFrames().size() - 1);
        assertTrue(hitLast.getHighlightValues().contains(15));

        OperationResult miss = s.search(TreeKind.GENERIC, cur, 999);
        StepFrame missLast = miss.getFrames().get(miss.getFrames().size() - 1);
        assertTrue(missLast.getStatusText().toLowerCase().contains("not found"));

        // Traverse DFS/BFS
        assertNotNull(lastSnapshot(s.traverse(TreeKind.GENERIC, cur, false)));
        assertNotNull(lastSnapshot(s.traverse(TreeKind.GENERIC, cur, true)));

        // Update (5 -> 7)
        cur = lastSnapshot(s.update(TreeKind.GENERIC, cur, 5, 7));

        // Delete (15)
        cur = lastSnapshot(s.delete(TreeKind.GENERIC, cur, 15));

        // Final traverse
        assertNotNull(lastSnapshot(s.traverse(TreeKind.GENERIC, cur, false)));
    }

    @Test
    void binaryTree_allOperations_smoke() {
        OperationService s = new OperationService();
        TreeSnapshot cur = emptySnapshot(TreeKind.BINARY);

        cur = lastSnapshot(s.create(TreeKind.BINARY, cur));

        // Insert
        cur = lastSnapshot(s.insert(TreeKind.BINARY, cur, null, 10));
        cur = lastSnapshot(s.insert(TreeKind.BINARY, cur, null, 5));
        cur = lastSnapshot(s.insert(TreeKind.BINARY, cur, null, 15));
        cur = lastSnapshot(s.insert(TreeKind.BINARY, cur, null, 12));
        cur = lastSnapshot(s.insert(TreeKind.BINARY, cur, null, 18));

        // Search hit/miss
        OperationResult hit = s.search(TreeKind.BINARY, cur, 12);
        assertTrue(hit.getFrames().get(hit.getFrames().size() - 1).getHighlightValues().contains(12));

        OperationResult miss = s.search(TreeKind.BINARY, cur, 999);
        assertTrue(miss.getFrames().get(miss.getFrames().size() - 1).getStatusText().toLowerCase().contains("not found"));

        // Traverse
        assertNotNull(lastSnapshot(s.traverse(TreeKind.BINARY, cur, false)));
        assertNotNull(lastSnapshot(s.traverse(TreeKind.BINARY, cur, true)));

        // Update (12 -> 13)
        cur = lastSnapshot(s.update(TreeKind.BINARY, cur, 12, 13));

        // Delete (node with two children possible: delete 15)
        cur = lastSnapshot(s.delete(TreeKind.BINARY, cur, 15));

        assertNotNull(lastSnapshot(s.traverse(TreeKind.BINARY, cur, false)));
    }

    @Test
    void redBlackTree_allOperations_smoke() {
        OperationService s = new OperationService();
        TreeSnapshot cur = emptySnapshot(TreeKind.RED_BLACK);

        cur = lastSnapshot(s.create(TreeKind.RED_BLACK, cur));

        // Insert a few values (should rebalance)
        for (int v : List.of(10, 5, 15, 1, 7, 12, 18, 6)) {
            cur = lastSnapshot(s.insert(TreeKind.RED_BLACK, cur, null, v));
        }

        // Search hit/miss
        OperationResult hit = s.search(TreeKind.RED_BLACK, cur, 7);
        assertTrue(hit.getFrames().get(hit.getFrames().size() - 1).getHighlightValues().contains(7));

        OperationResult miss = s.search(TreeKind.RED_BLACK, cur, 999);
        assertTrue(miss.getFrames().get(miss.getFrames().size() - 1).getStatusText().toLowerCase().contains("not found"));

        // Traverse
        assertNotNull(lastSnapshot(s.traverse(TreeKind.RED_BLACK, cur, false)));
        assertNotNull(lastSnapshot(s.traverse(TreeKind.RED_BLACK, cur, true)));

        // Update (7 -> 8) (rebuild-based path)
        cur = lastSnapshot(s.update(TreeKind.RED_BLACK, cur, 7, 8));

        // Delete (rebuild-based path)
        cur = lastSnapshot(s.delete(TreeKind.RED_BLACK, cur, 12));

        assertNotNull(lastSnapshot(s.traverse(TreeKind.RED_BLACK, cur, false)));
    }
}

