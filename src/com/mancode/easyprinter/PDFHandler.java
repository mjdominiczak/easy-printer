package com.mancode.easyprinter;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import javax.swing.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Michał Dominiczak
 * on 27.05.2016
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */
class PDFHandler {

    private static final int sizeTolerance = 2;

    static PDFProperties getPropertiesSet(File file) {
        PDDocument pdDocument = loadPDF(file);
        PageSize pageSize = getPageSize(pdDocument);
        int pageCount = getPageCount(pdDocument);
        closePDF(pdDocument);
        return new PDFProperties(pageCount, pageSize);
    }

    private static PDDocument loadPDF(File file) {
        PDDocument pdDocument = new PDDocument();
        try {
            pdDocument.close();
            pdDocument = PDDocument.load(file);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while trying to load file:" + file);
        }
        return pdDocument;
    }

    public static PageSize getPageSize(PDDocument pdDocument) {
        PageSize pageSize = getPageSize(pdDocument, 0);
        int pageCount = getPageCount(pdDocument);
        if (pageCount > 1) {
            for (int i = 1; i < pageCount; i++) {
                if (getPageSize(pdDocument, i) != pageSize) {
                    pageSize = PageSize.VARIOUS;
                    break;
                }
            }
        }
        return pageSize;
    }

    private static int getPageCount(PDDocument pdDocument) {
        return pdDocument.getNumberOfPages();
    }

    private static void closePDF(PDDocument pdDocument) {
        try {
            pdDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while trying to close file:" + pdDocument.getDocumentInformation().getTitle());
        }
    }

    public static PageSize getPageSize(PDDocument pdDocument, int pageNumber) {
        // get page dimensions
        PDRectangle box;
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
            maxDim = width;
            minDim = height;
        } else {
            maxDim = height;
            minDim = width;
        }

        // check page format (dimensions in mm)
        if (Math.abs(minDim - 210) < sizeTolerance && Math.abs(maxDim - 297) < sizeTolerance) {
            return PageSize.A4;
        } else if (Math.abs(minDim - 210) < sizeTolerance && Math.abs(maxDim - 279) < sizeTolerance) {
            return PageSize.A4_BOM;
        } else if (Math.abs(minDim - 297) < sizeTolerance && Math.abs(maxDim - 420) < sizeTolerance) {
            return PageSize.A3;
        } else if (Math.abs(minDim - 420) < sizeTolerance && Math.abs(maxDim - 594) < sizeTolerance) {
            return PageSize.A2;
        } else if (Math.abs(minDim - 594) < sizeTolerance && Math.abs(maxDim - 841) < sizeTolerance) {
            return PageSize.A1;
        } else if (Math.abs(minDim - 841) < sizeTolerance && Math.abs(maxDim - 1189) < sizeTolerance) {
            return PageSize.A0;
        } else if (Math.abs(minDim - 841) < sizeTolerance && Math.abs(maxDim - 1609) < sizeTolerance) {
            return PageSize.A0_1609;
        } else if (Math.abs(minDim - 841) < sizeTolerance && Math.abs(maxDim - 2450) < sizeTolerance) {
            return PageSize.A0_2450;
        } else if (Math.abs(minDim - 841) < sizeTolerance && Math.abs(maxDim - 3291) < sizeTolerance) {
            return PageSize.A0_3291;
        } else if (Math.abs(minDim - 841) < sizeTolerance && Math.abs(maxDim - 4132) < sizeTolerance) {
            return PageSize.A0_4132;
        } else
            return PageSize.GENERAL;
    }

    // convert default dimensions units (points) to mm: 1 point equals to 1/72 inch
    private static int pt2mm(float pt) {
        return Math.round(pt * 25.4f / 72);
    }

    static List<CustomFile> splitDocument(CustomFile file, Path rootPath) {
        PDDocument pdDocument = loadPDF(file);
        List<CustomFile> singlePages = new ArrayList<>();
        for (int i = 0; i < file.getPageCount(); i++) {
            int sheetNo = i + 1;
            File directory = new File(rootPath.toString() + "\\_Split");
            directory.mkdir();
            String filename = file.getName();
            String[] splittedFilename = filename.split("\\.");
            String extension = "." + splittedFilename[splittedFilename.length - 1];
            String rawFilename = filename.substring(0, filename.lastIndexOf(extension));
            String newFilename = String.format("%1$s p%2$02d%3$s", rawFilename, sheetNo, extension);
            CustomFile newFile = new CustomFile(directory, newFilename, sheetNo);
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
        closePDF(pdDocument);
        return singlePages;
    }

    static void mergePDFs(File targetFile, DefaultListModel<CustomFile> listModel) {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(targetFile.getAbsolutePath());
        Collections.list(listModel.elements()).forEach(file -> {
            try {
                merger.addSource(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
        try {
            merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void printPDF(File file) throws PrinterException {
        PDDocument pdDocument = null;
        try {
            pdDocument = PDDocument.load(file);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while loading file for printing: " + file.getName());
        }
        PrintHandler printHandler = new PrintHandler();
        printHandler.printPDF(pdDocument);
        try {
            pdDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while closing file for printing: " + file.getName());
        }
    }

    static class PDFProperties {
        private int pageCount;
        private PageSize pageSize;

        PDFProperties(int pageCount, PageSize pageSize) {
            this.pageCount = pageCount;
            this.pageSize = pageSize;
        }

        int getPageCount() {
            return pageCount;
        }

        public PageSize getPageSize() {
            return pageSize;
        }
    }
}
