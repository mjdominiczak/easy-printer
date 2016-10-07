package com.mancode.easyprinter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Michał Dominiczak
 * on 14.08.2016
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */

class FileSignature implements Comparable<FileSignature> {

    private int drawingNumber;
    private int sheet = 0;          // 0 not checked, positive integer for sheet number
    private String revision;
    private boolean hasBOM;

    FileSignature(int drawingNumber, String revision, boolean hasBOM) {
        this.drawingNumber = drawingNumber;
        this.revision = revision;
        this.hasBOM = hasBOM;
    }

    FileSignature(int drawingNumber, int sheet, String revision, boolean hasBOM) {
        this.drawingNumber = drawingNumber;
        this.sheet = sheet;
        this.revision = revision;
        this.hasBOM = hasBOM;
    }

    FileSignature(FileSignature other) {
        this.drawingNumber = other.getDrawingNumber();
        this.sheet = other.getSheet();
        this.revision = other.getRevision();
        this.hasBOM = other.isHasBOM();
    }

    int getDrawingNumber() {
        return drawingNumber;
    }

    void setDrawingNumber(int drawingNumber) {
        this.drawingNumber = drawingNumber;
    }

    int getSheet() {
        return sheet;
    }

    void setSheet(int sheet) {
        this.sheet = sheet;
    }

    String getRevision() {
        return revision;
    }

    void setRevision(String revision) {
        this.revision = revision;
    }

    boolean isHasBOM() {
        return hasBOM;
    }

    void setHasBOM(boolean hasBOM) {
        this.hasBOM = hasBOM;
    }

    void resetSheet() {
        sheet = 0;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19,47)
                .append(drawingNumber)
                .append(sheet)
                .append(hasBOM)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        FileSignature rhs = (FileSignature) obj;
        return new EqualsBuilder()
                .append(drawingNumber, rhs.drawingNumber)
                .append(sheet, rhs.sheet)
                .append(hasBOM, rhs.hasBOM)
                .isEquals();
    }

    @Override
    public String toString() {
        return (Integer.toString(drawingNumber)
                + (sheet == 0 ? "" : ("_s" + sheet))
                + (revision.equals("") ? "" : ("_" + revision))
                + (hasBOM ? " BOM" : ""));
    }

    public boolean equalsWithoutSheet(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        FileSignature rhs = (FileSignature) obj;
        return new EqualsBuilder()
                .append(drawingNumber, rhs.drawingNumber)
                .append(hasBOM, rhs.hasBOM)
                .isEquals();
//        return result && ((sheet == 0 && rhs.sheet != 0) || (sheet != 0 && rhs.sheet == 0));
    }

    @Override
    public int compareTo(FileSignature fs) {
        int result = Integer.compare(drawingNumber, fs.drawingNumber);
        if (result == 0) {
            result = Integer.compare(sheet, fs.sheet);
            if (result == 0) {
                result = Boolean.compare(hasBOM, fs.hasBOM);
            }
        }
        return result;
    }
}
