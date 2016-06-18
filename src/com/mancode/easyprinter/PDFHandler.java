package com.mancode.easyprinter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Manveru on 27.05.2016.
 */
public class PDFHandler {

    private PDDocument pdDocument;
    private int pageCount;
    private boolean pageOrientationHorizontal;
    private String filename;
    private File parentFile;

    PDFHandler(File file) {
        parentFile = file;
        loadPDF(file);
        filename = file.getName();
    }

    public void loadPDF(File file) {
        try {
            pdDocument = PDDocument.load(file);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while trying to load file:" + file);
        }
        pageCount = pdDocument.getNumberOfPages();
    }

    public void closePDF() {
        try {
            pdDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while trying to close file:" + pdDocument.getDocumentInformation().getTitle());
        }
    }

    public int getPageCount() {
        return pageCount;
    }

    public PageSize getPageSize(int pageNumber) {
        // get page dimensions
        PDRectangle box = null;
        try {
            box = pdDocument.getPage(pageNumber).getMediaBox();
        } catch (Exception e) {
            e.printStackTrace();
            return PageSize.GENERAL;
        }
        int width = pt2mm(box.getWidth());
        int height = pt2mm(box.getHeight());

        // check page orientation
        int maxDim, minDim;
        if (width > height) {
            pageOrientationHorizontal = true;
            maxDim = width;
            minDim = height;
        } else {
            pageOrientationHorizontal = false;
            maxDim = height;
            minDim = width;
        }

        // check page format (dimensions in mm)
        if (minDim == 210 && maxDim == 297) {
            return PageSize.A4;
        } else if (minDim == 210 && maxDim == 279) {
            return PageSize.A4_BOM;
        } else if (minDim == 297 && maxDim == 420) {
            return PageSize.A3;
        } else if (minDim == 420 && maxDim == 594) {
            return PageSize.A2;
        } else if (minDim == 594 && maxDim == 841) {
            return PageSize.A1;
        } else if (minDim == 841 && maxDim == 1189) {
            return PageSize.A0;
        } else if (minDim == 841 && maxDim == 1609) {
            return PageSize.A0_1609;
        } else if (minDim == 841 && maxDim == 2450) {
            return PageSize.A0_2450;
        } else if (minDim == 841 && maxDim == 3291) {
            return PageSize.A0_3291;
        } else if (minDim == 841 && maxDim == 4132) {
            return PageSize.A0_4132;
        } else {
            return PageSize.GENERAL;
        }
    }

    public void addPagesToDocument(File targetFile) {
        PDDocument targetDocument = null;
        try {
            targetDocument = PDDocument.load(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while trying to load file:" + targetFile);
        }
        for (PDPage page : pdDocument.getPages()) {
            targetDocument.addPage(page);
        }
        try {
            targetDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while trying to close file:" + targetDocument.getDocumentInformation().getTitle());
        }
    }

    public ArrayList<CustomFile> splitDocument() {
        ArrayList<CustomFile> singlePages = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            File directory = new File(parentFile.getParent() + "\\split");
            directory.mkdir();
            String[] splittedFilename = filename.split("\\.");
            String extension = "." + splittedFilename[splittedFilename.length - 1];
            String rawFilename = filename.substring(0, filename.lastIndexOf(extension));
            String newFilename = rawFilename + " page " + i + extension;
            CustomFile newFile = new CustomFile(directory, newFilename);
            boolean fileCreated = false;
            try {
                fileCreated = newFile.createNewFile();
            } catch (IOException e) {
                System.err.println("IOException while trying to create new file: " + newFilename);
                e.printStackTrace();
            }
            if (fileCreated) {
                PDDocument singlePageDocument = new PDDocument();
                singlePageDocument.addPage(pdDocument.getPage(i));
                try {
                    singlePageDocument.save(newFile);
                    newFile.runChecks();
                } catch (IOException e) {
                    System.err.println("IOException while saving new file: " + newFilename);
                    e.printStackTrace();
                }
                try {
                    singlePageDocument.close();
                } catch (IOException e) {
                    System.err.println("IOException while closing document: " + singlePageDocument.toString());
                    e.printStackTrace();
                }
                singlePages.add(newFile);
            }
        }
        return singlePages;
    }

    public void printPDF() {
        PrintHandler printHandler = new PrintHandler();
        try {
            printHandler.printPDF(pdDocument);
        } catch (PrinterException e) {
            e.printStackTrace();
            System.err.println("PrinterException while trying to print " + pdDocument);
        }
    }

    // convert default dimensions units (points) to mm: 1 point equals to 1/72 inch
    private int pt2mm(float pt) {
        return Math.round(pt * 25.4f / 72);
    }
}
