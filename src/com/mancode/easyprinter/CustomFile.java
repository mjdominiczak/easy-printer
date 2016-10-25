package com.mancode.easyprinter;

import org.apache.tika.Tika;

import javax.swing.*;
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
//        signature = new FileSignature(checkDrawingNumber(), "", checkBOM());
        signature = createSignature();
        if (fileType != null) {
            checkProperties();
            createFileInfo();
            existsInER = -1;
        }
    }

    private void checkFileType() {
        Tika tika = new Tika();
        try {
            fileType = tika.detect(this);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null , "File cannot be accessed:\n" + this.toString(), "IO Exception", JOptionPane.WARNING_MESSAGE);
        }
    }

    private FileSignature createSignature() {
        int drwNo = 0;
        int sheetNo = 0;
        String name = getName();
        Pattern pattern = Pattern.compile("[1-9][0-9]{8}");
        Matcher matcher = pattern.matcher(name);
        if (matcher.lookingAt()) {
            drwNo = Integer.parseInt(matcher.group());
        } else {
            pattern = Pattern.compile("([0-9]{2})k([0-9]*).*");
            matcher = pattern.matcher(name.toLowerCase());
            if (matcher.lookingAt()) {
                String convertedString = matcher.group(1) + "0" + matcher.group(2);
                drwNo = Integer.parseInt(convertedString);
            }
        }
        pattern = Pattern.compile(".*(p[0-9]{2})");
        matcher = pattern.matcher(name);
        if (matcher.lookingAt()) {
            sheetNo = Integer.parseInt(matcher.group(1).substring(1));
        }
        return new FileSignature(drwNo, sheetNo, "", checkBOM());
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

    private boolean checkBOM() {
        return getName().contains("BOM");
    }

    CustomFile(File parent, String child) {
        super(parent, child);
        runChecks();
    }

    CustomFile(File parent, String child, int sheet) {
        super(parent, child);
//        runChecks();
        signature = createSignature();
        signature.setSheet(sheet);
    }

    FileSignature getSignature() {
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
