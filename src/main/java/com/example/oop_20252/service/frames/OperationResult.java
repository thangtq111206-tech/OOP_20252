package com.example.oop_20252.service.frames;

import com.example.oop_20252.service.snapshots.TreeSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OperationResult {
    private final TreeSnapshot initialSnapshot;
    private final List<StepFrame> frames;
    private final String[] codeLines;
    private final String title;

    public OperationResult(
            TreeSnapshot initialSnapshot,
            List<StepFrame> frames,
            String[] codeLines,
            String title
    ) {
        this.initialSnapshot = initialSnapshot;
        this.frames = frames == null ? new ArrayList<>() : new ArrayList<>(frames);
        this.codeLines = codeLines == null ? new String[0] : codeLines.clone();
        this.title = title == null ? "" : title;
    }

    public TreeSnapshot getInitialSnapshot() {
        return initialSnapshot;
    }

    public List<StepFrame> getFrames() {
        return Collections.unmodifiableList(frames);
    }

    public String[] getCodeLines() {
        return codeLines.clone();
    }

    public String getTitle() {
        return title;
    }
}

