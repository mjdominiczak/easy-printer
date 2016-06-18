package com.mancode.easyprinter;

import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;

/**
 * Created by Manveru on 10.06.2016.
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

    public CustomFile(File parent, String child) {
        super(parent, child);
    }

    public void runChecks() {
        checkFileType();
        checkPageSize();
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

    public void checkPageSize() {
        if (fileType.equals("application/pdf")) {
            PDFHandler pdfHandler = new PDFHandler(this);
            pageCount = pdfHandler.getPageCount();
            pageSize = pdfHandler.getPageSize(0);
            if (pageCount > 1) {
                for (int i = 1; i < pageCount; i++) {
                    if (pdfHandler.getPageSize(i) != pageSize) {
                        pageSize = PageSize.VARIOUS;
                    }
                }
            }
            pdfHandler.closePDF();
        }
    }

    public void createFileInfo() {
        if (fileType.equals("application/pdf")) {
            fileInfo = String.format("%-30sPage count: %-5sPage size: %-10s", getName(), pageCount, pageSize);
        } else {
            fileInfo = String.format("%-30s", getName());
        }
    }

    public PageSize getPageSize() {
        return pageSize;
    }

    public String getFileType() {
        return fileType;
    }

    public int getPageCount() {
        return pageCount;
    }

    @Override
    public String toString() {
        if (fileInfo != null) {
            return fileInfo;
        } else {
            return super.toString();
        }
    }
}
