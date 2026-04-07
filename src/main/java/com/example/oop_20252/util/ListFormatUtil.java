package com.example.oop_20252.util;

import java.util.List;

/**
 * String formatting helpers for UI labels (traversal order, etc.).
 */
public final class ListFormatUtil {

    private ListFormatUtil() {
    }

    /**
     * Formats integers as {@code a → b → c} for traversal / visit order display.
     */
    public static String joinArrowSeparated(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(" → ");
            }
            sb.append(values.get(i));
        }
        return sb.toString();
    }
}
