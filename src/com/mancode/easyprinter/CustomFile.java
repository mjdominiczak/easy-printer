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
public class CustomFile extends File {

    private String fileInfo;
    private String fileType;
    private int drawingNumber;
    private int sheetNumber;
    private int pageCount;
    private PageSize pageSize;

    CustomFile(String pathname) {
        super(pathname);
        runChecks();
    }

    void runChecks() {
        checkFileType();
        checkDrawingNumber();
        checkProperties();
        createFileInfo();
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

    private void checkDrawingNumber() {
        String name = getName();
        Pattern pattern = Pattern.compile("500[0-9]{6}");
        Matcher matcher = pattern.matcher(name);
        if (matcher.lookingAt()) {
            drawingNumber = Integer.parseInt(matcher.group());
        }
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
        return drawingNumber;
    }

//    /**
//     * Comparator class for sorting list according to referenceList order
//     */
//    class ERComparator implements Comparator<CustomFile> {
//
//        @Override
//        public int compare(CustomFile file1, CustomFile file2) {
//            int index1 = referenceList.indexOf(file1.drawingNumber);
//            int index2 = referenceList.indexOf(file2.drawingNumber);
//            if (index1 == -1) {
//                throw new IllegalArgumentException(file1.drawingNumber + " not found in ER!");
//            } else if (index2 == -1) {
//                throw new IllegalArgumentException(file2.drawingNumber + " not found in ER!");
//            }
//            int result = index1 - index2;
//            if (result == 0) {
//                return file1.compareTo(file2);
//            }
//            return result;
//        }
//    }

}
