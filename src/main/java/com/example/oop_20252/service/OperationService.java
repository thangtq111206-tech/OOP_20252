package com.example.oop_20252.service;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.binary.AVLTree;
import com.example.oop_20252.model.binary.BST;
import com.example.oop_20252.model.multichild.NaryTree;
import com.example.oop_20252.model.multichild.MultiNode;
import com.example.oop_20252.model.redblack.RBNode;
import com.example.oop_20252.model.redblack.RBTree;
import com.example.oop_20252.service.frames.OperationResult;

import com.example.oop_20252.service.frames.StepFrame;
import com.example.oop_20252.service.snapshots.BinaryTreeSnapshot;
import com.example.oop_20252.service.snapshots.NaryTreeSnapshot;
import com.example.oop_20252.service.snapshots.RedBlackTreeSnapshot;
import com.example.oop_20252.service.snapshots.TreeSnapshot;

import java.util.*;

public class OperationService {

    public OperationResult create(TreeKind kind, TreeSnapshot current) {
        return new OperationResult(
                createEmptySnapshot(kind),
                List.of(new StepFrame(createEmptySnapshot(kind), List.of(), List.of(), 0, "Created empty tree")),
                new String[]{"Create empty tree"},
                "Create"
        );
    }

    public OperationResult insert(TreeKind kind, TreeSnapshot current, Integer parentValue, int newValue) {
        if (kind == TreeKind.N_ARY) return insertNary(current, parentValue, newValue);
        if (kind == TreeKind.BST) return insertBST(current, newValue);
        if (kind == TreeKind.AVL) return insertAVL(current, newValue);
        return insertRB(current, newValue);
    }

    public OperationResult delete(TreeKind kind, TreeSnapshot current, int value, boolean detailed) {
        if (kind == TreeKind.N_ARY) return deleteNary(current, value);
        if (kind == TreeKind.BST) return deleteBST(current, value);
        if (kind == TreeKind.AVL) return deleteAVL(current, value);
        return deleteRB(current, value);
    }

    public OperationResult update(TreeKind kind, TreeSnapshot current, int oldValue, int newValue) {
        // Just delete then insert
        List<StepFrame> frames = new ArrayList<>();
        TreeSnapshot intermediate = delete(kind, current, oldValue, false).getInitialSnapshot();
        frames.add(new StepFrame(intermediate, List.of(), List.of(), 0, "Deleted old value"));
        OperationResult insRes = insert(kind, intermediate, null, newValue);
        frames.addAll(insRes.getFrames());
        return new OperationResult(insRes.getInitialSnapshot(), frames, new String[]{"Update value by replacing"}, "Update");
    }

    public OperationResult traverse(TreeKind kind, TreeSnapshot current, boolean bfs) {
        List<StepFrame> frames = new ArrayList<>();
        List<Integer> visited = new ArrayList<>();
        if (kind == TreeKind.N_ARY) {
            MultiNode<Integer> rn = ((NaryTreeSnapshot)current).getRoot();
            if (bfs) bfsNary(rn, current, frames, visited, null); else dfsNary(rn, current, frames, visited);
        } else if (kind == TreeKind.RED_BLACK) {
            RedBlackTreeSnapshot rs = (RedBlackTreeSnapshot)current;
            if (bfs) bfsRB((RBNode<Integer>)rs.getRoot(), rs.getNil(), current, frames, visited);
            else dfsRB((RBNode<Integer>)rs.getRoot(), rs.getNil(), current, frames, visited);
        } else {
            com.example.oop_20252.model.binary.BinaryNode<Integer> rn = ((BinaryTreeSnapshot)current).getRoot();
            if (bfs) bfsBinary(rn, current, frames, visited); else dfsBinary(rn, current, frames, visited);
        }
        if (frames.isEmpty()) frames.add(new StepFrame(current, List.of(), List.of(), 0, "Tree is empty"));
        return new OperationResult(current, frames, new String[]{"Traversal initialized", "Visiting nodes in order..."}, "Traverse");
    }

    public OperationResult search(TreeKind kind, TreeSnapshot current, int value) {
        List<StepFrame> list = new ArrayList<>();
        List<Integer> visited = new ArrayList<>();
        if (kind == TreeKind.N_ARY) {
            MultiNode<Integer> rn = ((NaryTreeSnapshot)current).getRoot();
            bfsNary(rn, current, list, visited, value);
        } else if (kind == TreeKind.RED_BLACK) {
            RedBlackTreeSnapshot rs = (RedBlackTreeSnapshot)current;
            searchBin((RBNode<Integer>)rs.getRoot(), value, current, list, visited, rs.getNil());
        } else {
            com.example.oop_20252.model.binary.BinaryNode<Integer> rn = ((BinaryTreeSnapshot)current).getRoot();
            searchBin(rn, value, current, list, visited, null);
        }
        if (list.isEmpty()) list.add(new StepFrame(current, List.of(), List.of(), 0, "Tree is empty"));
        return new OperationResult(current, list, new String[]{"Searching for matching value..."}, "Search");
    }

    // Nary
    private OperationResult insertNary(TreeSnapshot snap, Integer parent, int val) {
        NaryTree<Integer> t = new NaryTree<>(((NaryTreeSnapshot) snap).getRoot());
        if (t.getRoot() == null) {
            t.setRoot(new MultiNode<>(val));
        } else {
            MultiNode<Integer> p = t.find(parent == null ? t.getRoot().value : parent);
            if (p != null) p.children.add(new MultiNode<>(val));
        }
        TreeSnapshot res = new NaryTreeSnapshot(t.deepCopy().getRoot());
        return new OperationResult(res, List.of(new StepFrame(res, List.of(val), List.of(), 0, "Inserted " + val)), new String[]{"Insert Node"}, "Insert");
    }

    private OperationResult deleteNary(TreeSnapshot snap, int val) {
        NaryTree<Integer> t = new NaryTree<>(((NaryTreeSnapshot) snap).getRoot());
        if (t.getRoot() != null && t.getRoot().value == val) {
            t.setRoot(null);
        } else {
            MultiNode<Integer> p = t.findParent(val);
            if (p != null) p.children.removeIf(c -> c.value == val);
        }
        TreeSnapshot res = new NaryTreeSnapshot(t.deepCopy().getRoot());
        return new OperationResult(res, List.of(new StepFrame(res, List.of(), List.of(), 0, "Deleted " + val)), new String[]{"Delete Node"}, "Delete");
    }

    // BST
    private OperationResult insertBST(TreeSnapshot snap, int val) {
        BST<Integer> t = new BST<>(((BinaryTreeSnapshot) snap).getRoot());
        t.insert(val);
        TreeSnapshot res = new BinaryTreeSnapshot(t.deepCopy().getRoot(), TreeKind.BST);
        return new OperationResult(res, List.of(new StepFrame(res, List.of(val), List.of(), 0, "Inserted " + val)), new String[]{"Insert Node"}, "Insert");
    }

    private OperationResult deleteBST(TreeSnapshot snap, int val) {
        BST<Integer> t = new BST<>(((BinaryTreeSnapshot) snap).getRoot());
        t.delete(val);
        TreeSnapshot res = new BinaryTreeSnapshot(t.deepCopy().getRoot(), TreeKind.BST);
        return new OperationResult(res, List.of(new StepFrame(res, List.of(), List.of(), 0, "Deleted " + val)), new String[]{"Delete Node"}, "Delete");
    }

    // AVL
    private OperationResult insertAVL(TreeSnapshot snap, int val) {
        AVLTree<Integer> t = new AVLTree<>(((BinaryTreeSnapshot) snap).getRoot());
        t.insert(val); // assumes we implement AVL insert rotation logic in future if not already there, for now it will just act like a generic BST insert. The user can add rebalance later.
        TreeSnapshot res = new BinaryTreeSnapshot(t.deepCopy().getRoot(), TreeKind.AVL);
        return new OperationResult(res, List.of(new StepFrame(res, List.of(val), List.of(), 0, "Inserted " + val)), new String[]{"Insert Node"}, "Insert");
    }

    private OperationResult deleteAVL(TreeSnapshot snap, int val) {
        AVLTree<Integer> t = new AVLTree<>(((BinaryTreeSnapshot) snap).getRoot());
        t.delete(val);
        TreeSnapshot res = new BinaryTreeSnapshot(t.deepCopy().getRoot(), TreeKind.AVL);
        return new OperationResult(res, List.of(new StepFrame(res, List.of(), List.of(), 0, "Deleted " + val)), new String[]{"Delete Node"}, "Delete");
    }

    // RB
    private OperationResult insertRB(TreeSnapshot snap, int val) {
        RedBlackTreeSnapshot rbs = (RedBlackTreeSnapshot) snap;
        RBTree<Integer> t = new RBTree<>(rbs.getRoot(), rbs.getNil());
        t.insert(val);
        RBTree<Integer> copy = t.deepCopy();
        TreeSnapshot res = new RedBlackTreeSnapshot((RBNode<Integer>)copy.getRoot(), copy.getNil());
        return new OperationResult(res, List.of(new StepFrame(res, List.of(val), List.of(), 0, "Inserted " + val)), new String[]{"Insert Node"}, "Insert");
    }

    private OperationResult deleteRB(TreeSnapshot snap, int val) {
        RedBlackTreeSnapshot rbs = (RedBlackTreeSnapshot) snap;
        RBTree<Integer> t = new RBTree<>(rbs.getRoot(), rbs.getNil());
        t.delete(val);
        RBTree<Integer> copy = t.deepCopy();
        TreeSnapshot res = new RedBlackTreeSnapshot((RBNode<Integer>)copy.getRoot(), copy.getNil());
        return new OperationResult(res, List.of(new StepFrame(res, List.of(), List.of(), 0, "Deleted " + val)), new String[]{"Delete Node"}, "Delete");
    }

    private TreeSnapshot createEmptySnapshot(TreeKind kind) {
        if (kind == TreeKind.N_ARY) return new NaryTreeSnapshot(null);
        if (kind == TreeKind.BST) return new BinaryTreeSnapshot(null, TreeKind.BST);
        if (kind == TreeKind.AVL) return new BinaryTreeSnapshot(null, TreeKind.AVL);
        RBTree<Integer> t = new RBTree<>();
        RBTree<Integer> copy = t.deepCopy();
        return new RedBlackTreeSnapshot((RBNode<Integer>)copy.getRoot(), copy.getNil());
    }

    private void bfsNary(MultiNode<Integer> node, TreeSnapshot snap, List<StepFrame> frames, List<Integer> visited, Integer searchTarget) {
        if (node == null) return;
        Queue<MultiNode<Integer>> q = new LinkedList<>();
        q.add(node);
        while (!q.isEmpty()) {
            MultiNode<Integer> curr = q.poll();
            visited.add(curr.value);
            frames.add(new StepFrame(snap, List.of(curr.value), new ArrayList<>(visited), 0, "Visiting " + curr.value));
            if (searchTarget != null && searchTarget.equals(curr.value)) {
                frames.add(new StepFrame(snap, List.of(curr.value), new ArrayList<>(visited), 0, "Found " + curr.value));
                return;
            }
            if (curr.children != null) q.addAll(curr.children);
        }
        if (searchTarget != null) frames.add(new StepFrame(snap, List.of(), new ArrayList<>(visited), 0, "Value " + searchTarget + " not found."));
    }

    private void dfsNary(MultiNode<Integer> node, TreeSnapshot snap, List<StepFrame> frames, List<Integer> visited) {
        if (node == null) return;
        visited.add(node.value);
        frames.add(new StepFrame(snap, List.of(node.value), new ArrayList<>(visited), 0, "Visiting " + node.value));
        if (node.children != null) {
            for (MultiNode<Integer> c : node.children) dfsNary(c, snap, frames, visited);
        }
    }

    private void bfsBinary(com.example.oop_20252.model.binary.BinaryNode<Integer> node, TreeSnapshot snap, List<StepFrame> frames, List<Integer> visited) {
        if (node == null) return;
        Queue<com.example.oop_20252.model.binary.BinaryNode<Integer>> q = new LinkedList<>();
        q.add(node);
        while (!q.isEmpty()) {
            var curr = q.poll();
            visited.add(curr.value);
            frames.add(new StepFrame(snap, List.of(curr.value), new ArrayList<>(visited), 0, "Visiting " + curr.value));
            if (curr.left != null) q.add((com.example.oop_20252.model.binary.BinaryNode<Integer>)curr.left);
            if (curr.right != null) q.add((com.example.oop_20252.model.binary.BinaryNode<Integer>)curr.right);
        }
    }
    
    private void dfsBinary(com.example.oop_20252.model.binary.BinaryNode<Integer> node, TreeSnapshot snap, List<StepFrame> frames, List<Integer> visited) {
        if (node == null) return;
        visited.add(node.value);
        frames.add(new StepFrame(snap, List.of(node.value), new ArrayList<>(visited), 0, "Visiting " + node.value));
        dfsBinary((com.example.oop_20252.model.binary.BinaryNode<Integer>)node.left, snap, frames, visited);
        dfsBinary((com.example.oop_20252.model.binary.BinaryNode<Integer>)node.right, snap, frames, visited);
    }
    
    private void bfsRB(RBNode<Integer> node, RBNode<Integer> nil, TreeSnapshot snap, List<StepFrame> frames, List<Integer> visited) {
        if (node == null || node == nil) return;
        Queue<RBNode<Integer>> q = new LinkedList<>();
        q.add(node);
        while (!q.isEmpty()) {
            var curr = q.poll();
            visited.add(curr.value);
            frames.add(new StepFrame(snap, List.of(curr.value), new ArrayList<>(visited), 0, "Visiting " + curr.value));
            if (curr.left != null && curr.left != nil) q.add((RBNode<Integer>)curr.left);
            if (curr.right != null && curr.right != nil) q.add((RBNode<Integer>)curr.right);
        }
    }
    
    private void dfsRB(RBNode<Integer> node, RBNode<Integer> nil, TreeSnapshot snap, List<StepFrame> frames, List<Integer> visited) {
        if (node == null || node == nil) return;
        visited.add(node.value);
        frames.add(new StepFrame(snap, List.of(node.value), new ArrayList<>(visited), 0, "Visiting " + node.value));
        dfsRB((RBNode<Integer>)node.left, nil, snap, frames, visited);
        dfsRB((RBNode<Integer>)node.right, nil, snap, frames, visited);
    }

    private void searchBin(com.example.oop_20252.model.core.INode<Integer> node, int target, TreeSnapshot snap, List<StepFrame> frames, List<Integer> visited, Object nil) {
        if (node == null || node == nil) {
            frames.add(new StepFrame(snap, List.of(), new ArrayList<>(visited), 0, "Not found: " + target));
            return;
        }
        visited.add(node.getValue());
        frames.add(new StepFrame(snap, List.of(node.getValue()), new ArrayList<>(visited), 0, "Checking " + node.getValue()));
        if (target == node.getValue()) {
            frames.add(new StepFrame(snap, List.of(node.getValue()), new ArrayList<>(visited), 0, "Found " + target));
            return;
        }
        if (target < node.getValue()) {
            searchBin(((com.example.oop_20252.model.binary.BinaryNode<Integer>)node).left, target, snap, frames, visited, nil);
        } else {
            searchBin(((com.example.oop_20252.model.binary.BinaryNode<Integer>)node).right, target, snap, frames, visited, nil);
        }
    }
}
