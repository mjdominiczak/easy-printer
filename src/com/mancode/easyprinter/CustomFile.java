package com.mancode.easyprinter;

import org.apache.tika.Tika;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Michał Dominiczak
 * on 10.06.2016
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */
class CustomFile extends File {

    private String fileInfo;
    private String fileType;
    private int pageCount;
    private int existsInER;         // -1 if not checked, 0 if does not exist, 1 if exists
    private PageSize pageSize;
    private FileSignature signature;

    CustomFile(String pathname) {
        super(pathname);
        runChecks();
    }

    void runChecks() {
        checkFileType();
        signature = new FileSignature(checkDrawingNumber(), "", checkBOM());
        checkProperties();
        createFileInfo();
        existsInER = -1;
    }

    private void checkFileType() {
        Tika tika = new Tika();
        try {
            fileType = tika.detect(this);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while trying to detect file type");
        }
    }

    private int checkDrawingNumber() {
        String name = getName();
        Pattern pattern = Pattern.compile("500[0-9]{6}");
        Matcher matcher = pattern.matcher(name);
        if (matcher.lookingAt()) {
            return Integer.parseInt(matcher.group());
        } else return 0;
    }

    private boolean checkBOM() {
        return getName().contains("BOM");
    }

    private void checkProperties() {
        if (fileType.equals("application/pdf")) {
            PDFHandler.PDFProperties properties = PDFHandler.getPropertiesSet(this);
            pageCount = properties.getPageCount();
            pageSize = properties.getPageSize();
        }
    }

    private void createFileInfo() {
        if (fileType.equals("application/pdf")) {
            fileInfo = String.format("%-30s Pcount: %-5s Psize: %-10s", getName(), pageCount, pageSize);
        } else {
            fileInfo = String.format("%-30s", getName());
        }
    }

    CustomFile(File parent, String child) {
        super(parent, child);
    }

    public FileSignature getSignature() {
        return signature;
    }

    int getExistsInER() {
        return existsInER;
    }

    void setExistsInER(int existsInER) {
        this.existsInER = existsInER;
    }

    void printPDF() {
        if (fileType.equals("application/pdf")) {
            try {
                PDFHandler.printPDF(this);
            } catch (PrinterException e) {
                e.printStackTrace();
                System.err.println("PrinterException: ");
                System.err.println(e.getMessage());
            }
        } else {
            System.err.println("File format is not pdf!");
        }
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

    PageSize getPageSize() {
        return pageSize;
    }

    String getFileType() {
        return fileType;
    }

    public String getFileInfo() {
        return fileInfo;
    }

    int getPageCount() {
        return pageCount;
    }

    int getDrawingNumber() {
        return signature.getDrawingNumber();
    }

}
