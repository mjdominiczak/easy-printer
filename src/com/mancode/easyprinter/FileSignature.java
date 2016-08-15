package com.mancode.easyprinter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Michał Dominiczak
 * on 14.08.2016
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */

class FileSignature {

    private int drawingNumber;
    private String revision;
    private boolean hasBOM;

    FileSignature(int drawingNumber, String revision, boolean hasBOM) {
        this.drawingNumber = drawingNumber;
        this.revision = revision;
        this.hasBOM = hasBOM;
    }

    int getDrawingNumber() {
        return drawingNumber;
    }

    void setDrawingNumber(int drawingNumber) {
        this.drawingNumber = drawingNumber;
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19,47)
                .append(drawingNumber)
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
                .append(hasBOM, rhs.hasBOM)
                .isEquals();
    }

    @Override
    public String toString() {
        return (Integer.toString(drawingNumber) + (revision.equals("") ? "" : ("_" + revision)) + (hasBOM ? " BOM" : ""));
    }
}
