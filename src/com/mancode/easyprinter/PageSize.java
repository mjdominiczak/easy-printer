package com.mancode.easyprinter;

/**
 * Created by Manveru on 27.05.2016.
 */
public enum PageSize {
    A4_BOM ("A4-BOM"),
    A4 ("A4"),
    A3 ("A3"),
    A2 ("A2"),
    A1 ("A1"),
    A0 ("A0"),
    A0_1609 ("A0-1609"),
    A0_2450 ("A0-2450"),
    A0_3291 ("A0-3291"),
    A0_4132 ("A0-4132"),
    GENERAL ("GENERAL"),
    VARIOUS ("VARIOUS");

    private final String text;

    PageSize(String name) {
        text = name;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return text;
    }
}
