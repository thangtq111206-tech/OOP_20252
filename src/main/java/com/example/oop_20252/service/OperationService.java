package com.example.oop_20252.service;

import com.example.oop_20252.model.TreeKind;
import com.example.oop_20252.model.binary.BinarySearchTree;
import com.example.oop_20252.model.generic.GenericTree;
import com.example.oop_20252.model.redblack.RedBlackTree;
import com.example.oop_20252.service.frames.OperationResult;
import com.example.oop_20252.service.frames.StepFrame;
import com.example.oop_20252.service.snapshots.BinaryTreeSnapshot;
import com.example.oop_20252.service.snapshots.GenericTreeSnapshot;
import com.example.oop_20252.service.snapshots.RedBlackTreeSnapshot;
import com.example.oop_20252.service.snapshots.TreeSnapshot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OperationService {

    public OperationResult create(TreeKind kind, TreeSnapshot current) {
        String[] codeLines = new String[]{
                "Create a new, empty tree",
                "Replace current tree with empty tree"
        };

        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Clearing the current tree..."));

        TreeSnapshot empty = createEmptySnapshot(kind);
        frames.add(new StepFrame(empty, List.of(), List.of(), 1, "Tree created."));
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Create");
    }

    public OperationResult insert(TreeKind kind, TreeSnapshot current, Integer parentValue, int newValue) {
        if (kind == TreeKind.GENERIC) return insertGeneric(current, parentValue, newValue);
        if (kind == TreeKind.BINARY) return insertBinary(current, newValue);
        return insertRedBlack(current, newValue);
    }

    public OperationResult delete(TreeKind kind, TreeSnapshot current, int value) {
        if (kind == TreeKind.GENERIC) return deleteGeneric(current, value);
        if (kind == TreeKind.BINARY) return deleteBinary(current, value);
        return deleteRedBlackRebuild(current, value);
    }

    public OperationResult update(TreeKind kind, TreeSnapshot current, int oldValue, int newValue) {
        if (kind == TreeKind.GENERIC) return updateGeneric(current, oldValue, newValue);
        if (kind == TreeKind.BINARY) return updateBinary(current, oldValue, newValue);
        return updateRedBlackRebuild(current, oldValue, newValue);
    }

    public OperationResult traverse(TreeKind kind, TreeSnapshot current, boolean bfs) {
        if (kind == TreeKind.GENERIC) return traverseGeneric(current, bfs);
        if (kind == TreeKind.BINARY) return traverseBinary(current, bfs);
        return traverseRedBlack(current, bfs);
    }

    public OperationResult search(TreeKind kind, TreeSnapshot current, int searchValue) {
        if (kind == TreeKind.GENERIC) return searchGeneric(current, searchValue);
        if (kind == TreeKind.BINARY) return searchBinary(current, searchValue);
        return searchRedBlack(current, searchValue);
    }

    private TreeSnapshot createEmptySnapshot(TreeKind kind) {
        if (kind == TreeKind.GENERIC) {
            GenericTree empty = new GenericTree();
            return new GenericTreeSnapshot(empty.deepCopy().getRoot());
        }
        if (kind == TreeKind.BINARY) {
            BinarySearchTree empty = new BinarySearchTree();
            return new BinaryTreeSnapshot(empty.deepCopy().getRoot());
        }
        RedBlackTree empty = new RedBlackTree();
        RedBlackTree copy = empty.deepCopy();
        return new RedBlackTreeSnapshot(copy.getRoot(), copy.getNil());
    }

    private TreeSnapshot snapshotOf(TreeKind kind, GenericTree t) {
        GenericTree copy = t.deepCopy();
        return new GenericTreeSnapshot(copy.getRoot());
    }

    private TreeSnapshot snapshotOf(TreeKind kind, BinarySearchTree t) {
        BinarySearchTree copy = t.deepCopy();
        return new BinaryTreeSnapshot(copy.getRoot());
    }

    private TreeSnapshot snapshotOf(TreeKind kind, RedBlackTree t) {
        RedBlackTree copy = t.deepCopy();
        return new RedBlackTreeSnapshot(copy.getRoot(), copy.getNil());
    }

    private OperationResult insertGeneric(TreeSnapshot current, Integer parentValue, int newValue) {
        String[] codeLines = new String[]{
                "Find parent node (DFS)",
                "If parent not found -> error",
                "Create new node",
                "Attach to parent's children",
                "Done"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting insert..."));

        GenericTree working = new GenericTree(((GenericTreeSnapshot) current).getRoot());
        if (working.find(newValue) != null) {
            frames.add(new StepFrame(new GenericTreeSnapshot(working.deepCopy().getRoot()), List.of(newValue), List.of(), 4, "Value already exists."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Insert");
        }

        if (working.getRoot() == null) {
            working = new GenericTree();
            // Root insertion when tree is empty.
            working.getRoot(); // no-op
            GenericTree.Node root = new GenericTree.Node(newValue);
            // Hack: tree has no setter, but we can rebuild by deep-copying; easiest is to attach root by direct field via reflection? Not allowed.
        }

        if (working.getRoot() == null) {
            // We can't set root directly because it's private; rebuild by creating a new tree.
            GenericTree empty = new GenericTree();
            GenericTree.Node root = new GenericTree.Node(newValue);
            // Use deep copy in snapshot by copying root into a new tree.
            GenericTree rebuild = new GenericTree(root);
            TreeSnapshot after = snapshotOf(TreeKind.GENERIC, rebuild);
            frames.add(new StepFrame(after, List.of(newValue), List.of(), 2, "Tree was empty, created root."));
            frames.add(new StepFrame(after, List.of(newValue), List.of(), 4, "Done."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Insert");
        }

        // Parent insertion.
        if (parentValue == null) {
            TreeSnapshot snap = snapshotOf(TreeKind.GENERIC, working);
            frames.add(new StepFrame(snap, List.of(), List.of(), 1, "Parent value is required for Generic Tree when not empty."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Insert");
        }

        Deque<GenericTree.Node> stack = new ArrayDeque<>();
        stack.push(working.getRoot());
        Set<Integer> visited = new HashSet<>();
        GenericTree.Node parent = null;

        while (!stack.isEmpty()) {
            GenericTree.Node cur = stack.pop();
            visited.add(cur.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.GENERIC, working), List.of(cur.value), new ArrayList<>(visited), 0,
                    "Searching for parent..."));

            if (cur.value == parentValue) {
                parent = cur;
                break;
            }
            for (GenericTree.Node child : cur.children) stack.push(child);
        }

        if (parent == null) {
            frames.add(new StepFrame(snapshotOf(TreeKind.GENERIC, working), List.of(), new ArrayList<>(visited), 1,
                    "Parent not found."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Insert");
        }

        GenericTree.Node newNode = new GenericTree.Node(newValue);
        frames.add(new StepFrame(snapshotOf(TreeKind.GENERIC, working), List.of(parent.value), new ArrayList<>(visited), 2,
                "Creating new node..."));
        parent.children.add(newNode);
        TreeSnapshot after = snapshotOf(TreeKind.GENERIC, working);
        frames.add(new StepFrame(after, List.of(newValue), new ArrayList<>(visited), 3,
                "Node attached."));
        frames.add(new StepFrame(after, List.of(newValue), new ArrayList<>(visited), 4,
                "Done."));
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Insert");
    }

    private OperationResult insertBinary(TreeSnapshot current, int newValue) {
        String[] codeLines = new String[]{
                "Start at root",
                "While current != null",
                "If new < current: move left",
                "Else if new > current: move right",
                "Create new node and link",
                "Done"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting insert..."));

        BinarySearchTree working = new BinarySearchTree(((BinaryTreeSnapshot) current).getRoot());
        if (working.getRoot() == null) {
            // Rebuild via constructor since root is private.
            BinarySearchTree.Node root = new BinarySearchTree.Node(newValue);
            BinarySearchTree rebuild = new BinarySearchTree(root);
            TreeSnapshot after = snapshotOf(TreeKind.BINARY, rebuild);
            frames.add(new StepFrame(after, List.of(newValue), List.of(newValue), 4, "Inserted as root."));
            frames.add(new StepFrame(after, List.of(newValue), List.of(newValue), 5, "Done."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Insert");
        }

        BinarySearchTree.Node cur = working.getRoot();
        BinarySearchTree.Node parent = null;
        Set<Integer> visited = new HashSet<>();

        while (cur != null) {
            visited.add(cur.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(cur.value), new ArrayList<>(visited), 1,
                    "Comparing with current node..."));

            parent = cur;
            if (newValue == cur.value) {
                frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(cur.value), new ArrayList<>(visited), 5,
                        "Value already exists."));
                return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Insert");
            } else if (newValue < cur.value) {
                cur = cur.left;
            } else {
                cur = cur.right;
            }
        }

        BinarySearchTree.Node inserted = new BinarySearchTree.Node(newValue);
        inserted.parent = parent;
        if (newValue < parent.value) parent.left = inserted;
        else parent.right = inserted;

        TreeSnapshot after = snapshotOf(TreeKind.BINARY, working);
        frames.add(new StepFrame(after, List.of(newValue), new ArrayList<>(visited), 4, "Created and linked new node."));
        frames.add(new StepFrame(after, List.of(newValue), new ArrayList<>(visited), 5, "Done."));
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Insert");
    }

    private OperationResult insertRedBlack(TreeSnapshot current, int newValue) {
        String[] codeLines = new String[]{
                "BST insert node",
                "Set new node color RED",
                "Fix violations while parent is RED",
                "Case 1: Uncle is RED (recolor)",
                "Case 2: Uncle BLACK and node is INNER",
                "Case 3: Uncle BLACK and node is OUTER (rotate + recolor)",
                "Set root color BLACK"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting RB insert..."));

        RedBlackTree working = new RedBlackTree(
                ((RedBlackTreeSnapshot) current).getRoot(),
                ((RedBlackTreeSnapshot) current).getNil()
        );

        // Duplicate check (BST-like).
        if (working.search(newValue) != null) {
            TreeSnapshot after = snapshotOf(TreeKind.RED_BLACK, working);
            frames.add(new StepFrame(after, list(newValue), list(newValue), 6, "Value already exists."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Insert");
        }

        RedBlackTree.Node z = working.createNode(newValue);
        RedBlackTree.Node y = working.getNil();
        RedBlackTree.Node x = working.getRoot();
        Set<Integer> visited = new HashSet<>();

        // BST insert while recording the descent path.
        while (x != working.getNil()) {
            visited.add(x.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working), list(x.value), new ArrayList<>(visited), 0,
                    "Descending to insert position..."));
            y = x;
            if (z.value < x.value) x = x.left;
            else x = x.right;
        }

        z.parent = y;
        if (y == working.getNil()) {
            working.setRoot(z);
        } else if (z.value < y.value) {
            y.left = z;
        } else {
            y.right = z;
        }
        // Newly inserted nodes are RED by definition (set in createNode()).
        frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working), list(z.value), new ArrayList<>(visited), 1,
                "Set new node color RED."));

        // Fix red-black violations.
        while (z.parent.red) {
            if (z.parent == z.parent.parent.left) {
                RedBlackTree.Node uncle = z.parent.parent.right;

                if (uncle.red) {
                    // Case 1: uncle is RED -> recolor.
                    z.parent.red = false;
                    uncle.red = false;
                    z.parent.parent.red = true;
                    frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working),
                            rbVals(working, z, z.parent, uncle, z.parent.parent),
                            new ArrayList<>(visited), 3,
                            "Case 1: Uncle is RED (recolor)."));
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {
                        // Case 2: inner child -> rotate to transform into case 3.
                        z = z.parent;
                        working.leftRotate(z);
                        frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working),
                                rbVals(working, z, z.parent),
                                new ArrayList<>(visited), 4,
                                "Case 2: Uncle BLACK and node is INNER (left rotate)."));
                    }

                    // Case 3: outer child -> rotate and recolor.
                    z.parent.red = false;
                    z.parent.parent.red = true;
                    working.rightRotate(z.parent.parent);
                    frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working),
                            rbVals(working, z, z.parent, z.parent.parent),
                            new ArrayList<>(visited), 5,
                            "Case 3: Uncle BLACK and node is OUTER (right rotate + recolor)."));
                }
            } else {
                // Mirror cases.
                RedBlackTree.Node uncle = z.parent.parent.left;

                if (uncle.red) {
                    // Case 1 mirror: recolor.
                    z.parent.red = false;
                    uncle.red = false;
                    z.parent.parent.red = true;
                    frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working),
                            rbVals(working, z, z.parent, uncle, z.parent.parent),
                            new ArrayList<>(visited), 3,
                            "Case 1 (mirror): Uncle is RED (recolor)."));
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        // Case 2 mirror: inner child -> rotate.
                        z = z.parent;
                        working.rightRotate(z);
                        frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working),
                                rbVals(working, z, z.parent),
                                new ArrayList<>(visited), 4,
                                "Case 2 (mirror): Uncle BLACK and node is INNER (right rotate)."));
                    }

                    // Case 3 mirror: rotate and recolor.
                    z.parent.red = false;
                    z.parent.parent.red = true;
                    working.leftRotate(z.parent.parent);
                    frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working),
                            rbVals(working, z, z.parent, z.parent.parent),
                            new ArrayList<>(visited), 5,
                            "Case 3 (mirror): Uncle BLACK and node is OUTER (left rotate + recolor)."));
                }
            }
        }

        // Root must be BLACK.
        working.getRoot().red = false;
        frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working),
                list(working.getRoot().value),
                new ArrayList<>(visited),
                6,
                "Set root color BLACK."));
        frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working),
                list(newValue),
                new ArrayList<>(visited),
                6,
                "Done."));

        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Insert");
    }

    // RB delete rebuild uses a silent RB insertion helper.

    private OperationResult deleteGeneric(TreeSnapshot current, int value) {
        String[] codeLines = new String[]{
                "Find node to delete (DFS)",
                "If node not found -> error",
                "Remove node from parent's children",
                "Done"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting delete..."));

        GenericTree working = new GenericTree(((GenericTreeSnapshot) current).getRoot());
        Set<Integer> visited = new HashSet<>();
        GenericTree.Node node = working.find(value);
        if (node == null) {
            // Visited path is unknown; just show current.
            TreeSnapshot snap = snapshotOf(TreeKind.GENERIC, working);
            frames.add(new StepFrame(snap, List.of(), new ArrayList<>(visited), 1, "Value not found."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Delete");
        }

        // Record search highlight by DFS.
        Deque<GenericTree.Node> stack = new ArrayDeque<>();
        stack.push(working.getRoot());
        while (!stack.isEmpty()) {
            GenericTree.Node cur = stack.pop();
            visited.add(cur.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.GENERIC, working), List.of(cur.value), new ArrayList<>(visited), 0,
                    "Searching for node to delete..."));
            if (cur.value == value) break;
            for (GenericTree.Node child : cur.children) stack.push(child);
        }

        GenericTree.Node parent = working.findParent(value);
        if (parent == null) {
            // Deleting root.
            GenericTree rebuilt = new GenericTree();
            TreeSnapshot after = snapshotOf(TreeKind.GENERIC, rebuilt);
            frames.add(new StepFrame(after, List.of(), new ArrayList<>(visited), 2, "Removed root."));
            frames.add(new StepFrame(after, List.of(), new ArrayList<>(visited), 3, "Done."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Delete");
        }

        // Remove node from parent's children list.
        parent.children.remove(node);
        TreeSnapshot after = snapshotOf(TreeKind.GENERIC, working);
        frames.add(new StepFrame(after, List.of(), new ArrayList<>(visited), 2, "Node removed."));
        frames.add(new StepFrame(after, List.of(), new ArrayList<>(visited), 3, "Done."));
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Delete");
    }

    private OperationResult deleteBinary(TreeSnapshot current, int value) {
        String[] codeLines = new String[]{
                "Search for node to delete",
                "If node not found -> error",
                "Case: node has 0 or 1 child",
                "Case: node has 2 children (use successor)",
                "Done"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting delete..."));

        BinarySearchTree working = new BinarySearchTree(((BinaryTreeSnapshot) current).getRoot());
        BinarySearchTree.Node node = working.find(value);
        Set<Integer> visited = new HashSet<>();

        if (node == null) {
            TreeSnapshot snap = snapshotOf(TreeKind.BINARY, working);
            frames.add(new StepFrame(snap, List.of(), new ArrayList<>(visited), 1, "Value not found."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Delete");
        }

        // Record search comparisons.
        BinarySearchTree.Node cur = working.getRoot();
        while (cur != null) {
            visited.add(cur.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(cur.value), new ArrayList<>(visited), 0,
                    "Comparing..."));
            if (cur.value == value) break;
            if (value < cur.value) cur = cur.left;
            else cur = cur.right;
        }

        frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(value), new ArrayList<>(visited), 2,
                "Found node to delete."));

        if (node.left == null) {
            working.transplant(node, node.right);
        } else if (node.right == null) {
            working.transplant(node, node.left);
        } else {
            frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(node.value), new ArrayList<>(visited), 3,
                    "Has two children: using successor..."));
            BinarySearchTree.Node succ = working.minimum(node.right);
            // Move successor value into node (simplifies visualization).
            visited.add(succ.value);
            int succValue = succ.value;
            node.value = succValue;
            frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(succValue), new ArrayList<>(visited), 3,
                    "Swapped with successor; deleting successor node..."));

            if (succ.parent != node) {
                working.transplant(succ, succ.right);
            } else {
                // successor is direct right child
                working.transplant(succ, succ.right);
            }
        }

        TreeSnapshot after = snapshotOf(TreeKind.BINARY, working);
        frames.add(new StepFrame(after, List.of(), new ArrayList<>(visited), 4, "Done."));
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Delete");
    }

    private OperationResult deleteRedBlackRebuild(TreeSnapshot current, int value) {
        String[] codeLines = new String[]{
                "Find node to delete",
                "Remove the value",
                "Rebuild RB tree by inserting remaining nodes"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting RB delete..."));

        RedBlackTree working = new RedBlackTree(
                ((RedBlackTreeSnapshot) current).getRoot(),
                ((RedBlackTreeSnapshot) current).getNil()
        );

        // Search path.
        Set<Integer> visited = new HashSet<>();
        RedBlackTree.Node cur = working.getRoot();
        while (cur != working.getNil() && cur != null) {
            visited.add(cur.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working), List.of(cur.value), new ArrayList<>(visited), 0,
                    "Comparing..."));
            if (cur.value == value) break;
            if (value < cur.value) cur = cur.left;
            else cur = cur.right;
        }

        if (cur == null || cur == working.getNil()) {
            TreeSnapshot snap = snapshotOf(TreeKind.RED_BLACK, working);
            frames.add(new StepFrame(snap, List.of(), new ArrayList<>(visited), 1, "Value not found."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Delete");
        }

        frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working), List.of(value), new ArrayList<>(visited), 1,
                "Found. Removing and rebuilding..."));

        // Rebuild by reinserting all remaining values.
        List<Integer> values = new ArrayList<>();
        collectInOrder(working.getRoot(), working.getNil(), values);
        values.removeIf(v -> v == value);

        RedBlackTree rebuilt = new RedBlackTree();
        TreeSnapshot cleared = snapshotOf(TreeKind.RED_BLACK, rebuilt);
        frames.add(new StepFrame(cleared, List.of(), new ArrayList<>(visited), 2, "Cleared RB tree."));

        for (int v : values) {
            // For visualization purposes, we do a single-step insertion (no intermediate fix-up frames).
            // We'll use a minimal silent insert that relies on the RB insertion fix-up implemented below.
            rbInsertRebalanceSilent(rebuilt, v);
            TreeSnapshot after = snapshotOf(TreeKind.RED_BLACK, rebuilt);
            frames.add(new StepFrame(after, List.of(v), new ArrayList<>(visited), 2, "Inserted " + v + " during rebuild."));
        }

        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Delete");
    }

    private void rbInsertRebalanceSilent(RedBlackTree t, int value) {
        if (t.search(value) != null) return;

        RedBlackTree.Node z = t.createNode(value);
        RedBlackTree.Node y = t.getNil();
        RedBlackTree.Node x = t.getRoot();

        while (x != t.getNil()) {
            y = x;
            if (z.value < x.value) x = x.left;
            else x = x.right;
        }

        z.parent = y;
        if (y == t.getNil()) {
            t.setRoot(z);
        } else if (z.value < y.value) {
            y.left = z;
        } else {
            y.right = z;
        }

        // Fix-up (CLRS) without recording frames.
        while (z.parent.red) {
            if (z.parent == z.parent.parent.left) {
                RedBlackTree.Node uncle = z.parent.parent.right;
                if (uncle.red) {
                    z.parent.red = false;
                    uncle.red = false;
                    z.parent.parent.red = true;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {
                        z = z.parent;
                        t.leftRotate(z);
                    }
                    z.parent.red = false;
                    z.parent.parent.red = true;
                    t.rightRotate(z.parent.parent);
                }
            } else {
                RedBlackTree.Node uncle = z.parent.parent.left;
                if (uncle.red) {
                    z.parent.red = false;
                    uncle.red = false;
                    z.parent.parent.red = true;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        z = z.parent;
                        t.rightRotate(z);
                    }
                    z.parent.red = false;
                    z.parent.parent.red = true;
                    t.leftRotate(z.parent.parent);
                }
            }
        }

        t.getRoot().red = false;
    }

    // Placeholder methods to satisfy compilation for traversal/search; they will be completed in a follow-up patch.

    private OperationResult updateGeneric(TreeSnapshot current, int oldValue, int newValue) {
        String[] codeLines = new String[]{
                "Find node with old value",
                "If node not found -> error",
                "Update node's value",
                "Done"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting update..."));
        GenericTree working = new GenericTree(((GenericTreeSnapshot) current).getRoot());
        GenericTree.Node node = working.find(oldValue);
        if (node == null) {
            frames.add(new StepFrame(snapshotOf(TreeKind.GENERIC, working), List.of(), List.of(), 1, "Old value not found."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Update");
        }
        if (working.find(newValue) != null && newValue != oldValue) {
            frames.add(new StepFrame(snapshotOf(TreeKind.GENERIC, working), List.of(), List.of(newValue), 1, "New value already exists."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Update");
        }
        // Highlight old node, then change value.
        frames.add(new StepFrame(snapshotOf(TreeKind.GENERIC, working), List.of(oldValue), List.of(oldValue), 2, "Updating value..."));
        node.value = newValue;
        TreeSnapshot after = snapshotOf(TreeKind.GENERIC, working);
        frames.add(new StepFrame(after, List.of(newValue), List.of(newValue), 3, "Done."));
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Update");
    }

    private OperationResult updateBinary(TreeSnapshot current, int oldValue, int newValue) {
        String[] codeLines = new String[]{
                "Find node with old value",
                "delete(old)",
                "insert(new)",
                "Done"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting update..."));
        BinarySearchTree working = new BinarySearchTree(((BinaryTreeSnapshot) current).getRoot());
        BinarySearchTree.Node oldNode = working.find(oldValue);
        if (oldNode == null) {
            frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(), List.of(), 0, "Old value not found."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Update");
        }
        BinarySearchTree.Node newExisting = working.find(newValue);
        if (newExisting != null && newValue != oldValue) {
            frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(), List.of(newValue), 0, "New value already exists."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Update");
        }

        // Delete old node (reuse delete logic but with codeLine mapping).
        // Record search path (minimal).
        BinarySearchTree.Node cur = working.getRoot();
        Set<Integer> visited = new HashSet<>();
        while (cur != null) {
            visited.add(cur.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(cur.value), new ArrayList<>(visited), 1,
                    "Locating node to delete..."));
            if (cur.value == oldValue) break;
            if (oldValue < cur.value) cur = cur.left;
            else cur = cur.right;
        }

        // Perform BST deletion.
        BinarySearchTree.Node node = working.find(oldValue);
        if (node.left == null) {
            working.transplant(node, node.right);
        } else if (node.right == null) {
            working.transplant(node, node.left);
        } else {
            BinarySearchTree.Node succ = working.minimum(node.right);
            int succValue = succ.value;
            node.value = succValue;
            if (succ.parent != node) working.transplant(succ, succ.right);
            else working.transplant(succ, succ.right);
        }

        TreeSnapshot afterDelete = snapshotOf(TreeKind.BINARY, working);
        frames.add(new StepFrame(afterDelete, List.of(), new ArrayList<>(visited), 1, "Deleted old value."));

        // Insert new value.
        BinarySearchTree.Node c = working.getRoot();
        BinarySearchTree.Node p = null;
        Set<Integer> visitedInsert = new HashSet<>();
        while (c != null) {
            visitedInsert.add(c.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(c.value), new ArrayList<>(visitedInsert), 2,
                    "Searching insertion position..."));
            p = c;
            if (newValue < c.value) c = c.left;
            else c = c.right;
        }
        BinarySearchTree.Node inserted = new BinarySearchTree.Node(newValue);
        inserted.parent = p;
        if (p == null) working = new BinarySearchTree(inserted);
        else if (newValue < p.value) p.left = inserted;
        else p.right = inserted;

        TreeSnapshot afterInsert = snapshotOf(TreeKind.BINARY, working);
        frames.add(new StepFrame(afterInsert, List.of(newValue), new ArrayList<>(visitedInsert), 2, "Inserted new value."));
        frames.add(new StepFrame(afterInsert, List.of(newValue), new ArrayList<>(visitedInsert), 3, "Done."));
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Update");
    }

    private OperationResult updateRedBlackRebuild(TreeSnapshot current, int oldValue, int newValue) {
        // Simple rebuild-based update: delete old (rebuild) then insert new (recorded as part of delete timeline).
        OperationResult del = deleteRedBlackRebuild(current instanceof RedBlackTreeSnapshot ? current : current, oldValue);
        TreeSnapshot intermediate = del.getFrames().get(del.getFrames().size() - 1).getSnapshot();
        OperationResult ins = insertRedBlack(intermediate, newValue);
        // For UI, merge frames.
        List<StepFrame> merged = new ArrayList<>(del.getFrames());
        merged.addAll(ins.getFrames().subList(1, ins.getFrames().size())); // avoid duplicate initial
        // Use the RB-insert pseudocode line count so highlight indices coming from `insertRedBlack()`
        // remain meaningful during the merged animation.
        String[] codeLines = new String[]{
                "BST insert node",
                "Set new node color RED",
                "Fix violations while parent is RED",
                "Case 1: Uncle is RED (recolor)",
                "Case 2: Uncle BLACK and node is INNER",
                "Case 3: Uncle BLACK and node is OUTER (rotate + recolor)",
                "Set root color BLACK"
        };
        // Map codeLine indices: keep as-is for simplicity.
        return new OperationResult(merged.get(0).getSnapshot(), merged, codeLines, "Update");
    }

    private OperationResult traverseGeneric(TreeSnapshot current, boolean bfs) {
        String[] codeLines = new String[]{
                "Use stack/queue",
                "Pop next node",
                "Mark visited",
                "Add children to structure",
                "Repeat until empty"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting traversal..."));

        GenericTree working = new GenericTree(((GenericTreeSnapshot) current).getRoot());
        GenericTree.Node root = working.getRoot();
        if (root == null) {
            TreeSnapshot snap = snapshotOf(TreeKind.GENERIC, working);
            frames.add(new StepFrame(snap, List.of(), List.of(), 4, "Tree is empty."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Traverse");
        }

        List<Integer> visitedOrder = new ArrayList<>();
        if (bfs) {
            Deque<GenericTree.Node> q = new ArrayDeque<>();
            q.add(root);
            while (!q.isEmpty()) {
                GenericTree.Node cur = q.removeFirst();
                frames.add(new StepFrame(snapshotOf(TreeKind.GENERIC, working), List.of(cur.value), new ArrayList<>(visitedOrder), 1,
                        "Visiting node..."));
                visitedOrder.add(cur.value);
                for (GenericTree.Node child : cur.children) q.add(child);
                TreeSnapshot after = snapshotOf(TreeKind.GENERIC, working);
                frames.add(new StepFrame(after, List.of(cur.value), new ArrayList<>(visitedOrder), 2,
                        "Marked visited."));
            }
        } else {
            Deque<GenericTree.Node> st = new ArrayDeque<>();
            st.push(root);
            while (!st.isEmpty()) {
                GenericTree.Node cur = st.pop();
                frames.add(new StepFrame(snapshotOf(TreeKind.GENERIC, working), List.of(cur.value), new ArrayList<>(visitedOrder), 1,
                        "Visiting node..."));
                visitedOrder.add(cur.value);
                // Push children onto stack
                for (int i = cur.children.size() - 1; i >= 0; i--) {
                    st.push(cur.children.get(i));
                }
                TreeSnapshot after = snapshotOf(TreeKind.GENERIC, working);
                frames.add(new StepFrame(after, List.of(cur.value), new ArrayList<>(visitedOrder), 2,
                        "Marked visited."));
            }
        }
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Traverse");
    }

    private OperationResult traverseBinary(TreeSnapshot current, boolean bfs) {
        String[] codeLines = new String[]{
                "Use stack/queue",
                "Pop next node",
                "Mark visited",
                "Add children to structure",
                "Repeat until empty"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting traversal..."));

        BinarySearchTree working = new BinarySearchTree(((BinaryTreeSnapshot) current).getRoot());
        BinarySearchTree.Node root = working.getRoot();
        if (root == null) {
            TreeSnapshot snap = snapshotOf(TreeKind.BINARY, working);
            frames.add(new StepFrame(snap, List.of(), List.of(), 4, "Tree is empty."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Traverse");
        }

        List<Integer> visitedOrder = new ArrayList<>();
        if (bfs) {
            Deque<BinarySearchTree.Node> q = new ArrayDeque<>();
            q.add(root);
            while (!q.isEmpty()) {
                BinarySearchTree.Node cur = q.removeFirst();
                frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(cur.value), new ArrayList<>(visitedOrder), 1,
                        "Visiting node..."));
                visitedOrder.add(cur.value);
                if (cur.left != null) q.add(cur.left);
                if (cur.right != null) q.add(cur.right);
                TreeSnapshot after = snapshotOf(TreeKind.BINARY, working);
                frames.add(new StepFrame(after, List.of(cur.value), new ArrayList<>(visitedOrder), 2,
                        "Marked visited."));
            }
        } else {
            Deque<BinarySearchTree.Node> st = new ArrayDeque<>();
            st.push(root);
            while (!st.isEmpty()) {
                BinarySearchTree.Node cur = st.pop();
                frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(cur.value), new ArrayList<>(visitedOrder), 1,
                        "Visiting node..."));
                visitedOrder.add(cur.value);
                if (cur.right != null) st.push(cur.right);
                if (cur.left != null) st.push(cur.left);
                TreeSnapshot after = snapshotOf(TreeKind.BINARY, working);
                frames.add(new StepFrame(after, List.of(cur.value), new ArrayList<>(visitedOrder), 2,
                        "Marked visited."));
            }
        }
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Traverse");
    }

    private OperationResult traverseRedBlack(TreeSnapshot current, boolean bfs) {
        String[] codeLines = new String[]{
                "Use stack/queue",
                "Pop next node",
                "Mark visited",
                "Add children to structure",
                "Repeat until empty"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting traversal..."));

        RedBlackTree working = new RedBlackTree(((RedBlackTreeSnapshot) current).getRoot(), ((RedBlackTreeSnapshot) current).getNil());
        RedBlackTree.Node root = working.getRoot();
        if (root == working.getNil() || root == null) {
            TreeSnapshot snap = snapshotOf(TreeKind.RED_BLACK, working);
            frames.add(new StepFrame(snap, List.of(), List.of(), 4, "Tree is empty."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Traverse");
        }

        List<Integer> visitedOrder = new ArrayList<>();
        if (bfs) {
            Deque<RedBlackTree.Node> q = new ArrayDeque<>();
            q.add(root);
            while (!q.isEmpty()) {
                RedBlackTree.Node cur = q.removeFirst();
                frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working), List.of(cur.value), new ArrayList<>(visitedOrder), 1,
                        "Visiting node..."));
                visitedOrder.add(cur.value);
                if (cur.left != working.getNil()) q.add(cur.left);
                if (cur.right != working.getNil()) q.add(cur.right);
                TreeSnapshot after = snapshotOf(TreeKind.RED_BLACK, working);
                frames.add(new StepFrame(after, List.of(cur.value), new ArrayList<>(visitedOrder), 2,
                        "Marked visited."));
            }
        } else {
            Deque<RedBlackTree.Node> st = new ArrayDeque<>();
            st.push(root);
            while (!st.isEmpty()) {
                RedBlackTree.Node cur = st.pop();
                frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working), List.of(cur.value), new ArrayList<>(visitedOrder), 1,
                        "Visiting node..."));
                visitedOrder.add(cur.value);
                if (cur.right != working.getNil()) st.push(cur.right);
                if (cur.left != working.getNil()) st.push(cur.left);
                TreeSnapshot after = snapshotOf(TreeKind.RED_BLACK, working);
                frames.add(new StepFrame(after, List.of(cur.value), new ArrayList<>(visitedOrder), 2,
                        "Marked visited."));
            }
        }
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Traverse");
    }

    private OperationResult searchGeneric(TreeSnapshot current, int searchValue) {
        String[] codeLines = new String[]{
                "Start at root",
                "Compare key",
                "Continue search in children",
                "If found: highlight and stop",
                "If not found: display not found"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting search..."));

        GenericTree working = new GenericTree(((GenericTreeSnapshot) current).getRoot());
        Set<Integer> visited = new HashSet<>();
        if (working.getRoot() == null) {
            TreeSnapshot snap = snapshotOf(TreeKind.GENERIC, working);
            frames.add(new StepFrame(snap, List.of(), new ArrayList<>(visited), 4, "Tree is empty."));
            return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Search");
        }

        Deque<GenericTree.Node> st = new ArrayDeque<>();
        st.push(working.getRoot());
        while (!st.isEmpty()) {
            GenericTree.Node cur = st.pop();
            visited.add(cur.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.GENERIC, working), List.of(cur.value), new ArrayList<>(visited), 1,
                    "Comparing..."));
            if (cur.value == searchValue) {
                TreeSnapshot after = snapshotOf(TreeKind.GENERIC, working);
                frames.add(new StepFrame(after, List.of(searchValue), new ArrayList<>(visited), 3,
                        "Found node."));
                return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Search");
            }
            for (GenericTree.Node child : cur.children) st.push(child);
        }

        TreeSnapshot after = snapshotOf(TreeKind.GENERIC, working);
        frames.add(new StepFrame(after, List.of(), new ArrayList<>(visited), 4, "not found"));
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Search");
    }

    private OperationResult searchBinary(TreeSnapshot current, int searchValue) {
        String[] codeLines = new String[]{
                "Start at root",
                "Compare key",
                "Move left/right",
                "If found: highlight and stop",
                "If reach null: not found"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting search..."));

        BinarySearchTree working = new BinarySearchTree(((BinaryTreeSnapshot) current).getRoot());
        Set<Integer> visited = new HashSet<>();
        BinarySearchTree.Node cur = working.getRoot();
        while (cur != null) {
            visited.add(cur.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.BINARY, working), List.of(cur.value), new ArrayList<>(visited), 1,
                    "Comparing..."));
            if (cur.value == searchValue) {
                TreeSnapshot after = snapshotOf(TreeKind.BINARY, working);
                frames.add(new StepFrame(after, List.of(searchValue), new ArrayList<>(visited), 3,
                        "Found node."));
                return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Search");
            }
            if (searchValue < cur.value) cur = cur.left;
            else cur = cur.right;
        }

        TreeSnapshot after = snapshotOf(TreeKind.BINARY, working);
        frames.add(new StepFrame(after, List.of(), new ArrayList<>(visited), 4, "not found"));
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Search");
    }

    private OperationResult searchRedBlack(TreeSnapshot current, int searchValue) {
        String[] codeLines = new String[]{
                "Start at root",
                "Compare key",
                "Move left/right",
                "If found: highlight and stop",
                "If reach NIL: not found"
        };
        List<StepFrame> frames = new ArrayList<>();
        frames.add(new StepFrame(current, List.of(), List.of(), 0, "Starting search..."));

        RedBlackTree working = new RedBlackTree(((RedBlackTreeSnapshot) current).getRoot(), ((RedBlackTreeSnapshot) current).getNil());
        Set<Integer> visited = new HashSet<>();
        RedBlackTree.Node cur = working.getRoot();
        while (cur != working.getNil() && cur != null) {
            visited.add(cur.value);
            frames.add(new StepFrame(snapshotOf(TreeKind.RED_BLACK, working), List.of(cur.value), new ArrayList<>(visited), 1,
                    "Comparing..."));
            if (cur.value == searchValue) {
                TreeSnapshot after = snapshotOf(TreeKind.RED_BLACK, working);
                frames.add(new StepFrame(after, List.of(searchValue), new ArrayList<>(visited), 3,
                        "Found node."));
                return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Search");
            }
            if (searchValue < cur.value) cur = cur.left;
            else cur = cur.right;
        }

        TreeSnapshot after = snapshotOf(TreeKind.RED_BLACK, working);
        frames.add(new StepFrame(after, List.of(), new ArrayList<>(visited), 4, "not found"));
        return new OperationResult(frames.get(0).getSnapshot(), frames, codeLines, "Search");
    }

    private void collectInOrder(RedBlackTree.Node root, RedBlackTree.Node nil, List<Integer> out) {
        Deque<RedBlackTree.Node> st = new ArrayDeque<>();
        RedBlackTree.Node cur = root;
        while (cur != nil || !st.isEmpty()) {
            while (cur != nil) {
                st.push(cur);
                cur = cur.left;
            }
            cur = st.pop();
            out.add(cur.value);
            cur = cur.right;
        }
    }

    private static List<Integer> list(int... values) {
        List<Integer> out = new ArrayList<>();
        if (values == null) return out;
        for (int v : values) out.add(v);
        return out;
    }

    private List<Integer> rbVals(RedBlackTree t, RedBlackTree.Node... nodes) {
        List<Integer> out = new ArrayList<>();
        if (nodes == null) return out;
        for (RedBlackTree.Node n : nodes) {
            if (n == null || n == t.getNil()) continue;
            out.add(n.value);
        }
        return out;
    }
}

