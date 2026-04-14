package com.example.oop_20252.service.frames;

import com.example.oop_20252.service.snapshots.TreeSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StepFrame {
    private final TreeSnapshot snapshot;
    private final List<Integer> highlightValues;
    private final List<Integer> visitedValues;
    private final int codeLineIndex;
    private final String statusText;

    public StepFrame(
            TreeSnapshot snapshot,
            List<Integer> highlightValues,
            List<Integer> visitedValues,
            int codeLineIndex,
            String statusText
    ) {
        this.snapshot = snapshot;
        this.highlightValues = highlightValues == null ? new ArrayList<>() : new ArrayList<>(highlightValues);
        this.visitedValues = visitedValues == null ? new ArrayList<>() : new ArrayList<>(visitedValues);
        this.codeLineIndex = codeLineIndex;
        this.statusText = statusText == null ? "" : statusText;
    }

    public TreeSnapshot getSnapshot() {
        return snapshot;
    }

    public List<Integer> getHighlightValues() {
        return highlightValues;
    }

    public List<Integer> getVisitedValues() {
        return visitedValues;
    }

    public int getCodeLineIndex() {
        return codeLineIndex;
    }

    public String getStatusText() {
        return statusText;
    }
}

