package com.fmeyer.hackernews;

public class Utils {

    public static CharSequence trim(CharSequence s) {
        return trim(s, 0, s.length());
    }

    private static CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }
        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }
        return s.subSequence(start, end);
    }
}
