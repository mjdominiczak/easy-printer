package com.mancode.easyprinter;

import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;

/**
 * Created by Michał Dominiczak
 * on 10.06.2016
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */
public class CustomFile extends File {

    private String fileInfo;
    private String fileType;
    private int pageCount;
    private PageSize pageSize;
    public CustomFile(String pathname) {
        super(pathname);
        runChecks();
    }

    public void runChecks() {
        checkFileType();
        checkProperties();
        createFileInfo();
    }

    public void checkFileType() {
        Tika tika = new Tika();
        try {
            fileType = tika.detect(this);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while trying to detect file type");
        }
    }

    public void checkProperties() {
        if (fileType.equals("application/pdf")) {
            try {
                PDFHandler.PDFProperties properties = PDFHandler.getPropertiesSet(this);
                pageCount = properties.getPageCount();
                pageSize = properties.getPageSize();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("IOException while getting properties of file " + getName());
            }
        }
    }

    public void createFileInfo() {
        if (fileType.equals("application/pdf")) {
            fileInfo = String.format("%-30s Pcount: %-5s Psize: %-10s", getName(), pageCount, pageSize);
        } else {
            fileInfo = String.format("%-30s", getName());
        }
    }

    public CustomFile(File parent, String child) {
        super(parent, child);
    }

    @Override
    public int compareTo(File pathname) {
        return this.getName().compareTo(pathname.getName());
    }

    @Override
    public String toString() {
        if (fileInfo != null) {
            return fileInfo;
        } else {
            return super.toString();
        }
    }

    public PageSize getPageSize() {
        return pageSize;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileInfo() {
        return fileInfo;
    }

    public int getPageCount() {
        return pageCount;
    }
}
