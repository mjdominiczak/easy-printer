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

    public static int getPageCount(File file) throws IOException {
        // load file
        PDDocument pdDocument = PDDocument.load(file);
        int pageCount = pdDocument.getNumberOfPages();
        pdDocument.close();
        return pageCount;
    }

    public static PDFProperties getPropertiesSet(File file) throws IOException {
        PDDocument pdDocument = PDDocument.load(file);
        PageSize pageSize = getPageSize(pdDocument);
        int pageCount = getPageCount(pdDocument);
        pdDocument.close();
        return new PDFProperties(pageCount, pageSize);
    }

    public static PageSize getPageSize(PDDocument pdDocument) throws IOException {
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

    public static int getPageCount(PDDocument pdDocument) {
        return pdDocument.getNumberOfPages();
    }

    public static PageSize getPageSize(PDDocument pdDocument, int pageNumber) throws IOException {
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

    // convert default dimensions units (points) to mm: 1 point equals to 1/72 inch
    private static int pt2mm(float pt) {
        return Math.round(pt * 25.4f / 72);
    }

    public static void splitDocument(CustomFile file) {
        PDDocument pdDocument = null;
        try {
            pdDocument = PDDocument.load(file);
        } catch (IOException e) {
            System.err.println("IOException while trying to open file: " + file);
            e.printStackTrace();
        }
//        ArrayList<CustomFile> singlePages = new ArrayList<>();
        for (int i = 0; i < file.getPageCount(); i++) {
            File directory = new File(file.getParent() + "\\split");
            directory.mkdir();
            String filename = file.getName();
            String[] splittedFilename = filename.split("\\.");
            String extension = "." + splittedFilename[splittedFilename.length - 1];
            String rawFilename = filename.substring(0, filename.lastIndexOf(extension));
            String newFilename = String.format("%1$s p%2$02d%3$s", rawFilename, i, extension);
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
//                singlePages.add(newFile);
            }
        }
        try {
            pdDocument.close();
        } catch (IOException e) {
            System.err.println("IOException while trying to close file: " + file);
            e.printStackTrace();
        }
    }

    public void closePDF() {
        try {
            pdDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException while trying to close file:" + pdDocument.getDocumentInformation().getTitle());
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

    public static class PDFProperties {
        private int pageCount;
        private PageSize pageSize;

        public PDFProperties(int pageCount, PageSize pageSize) {
            this.pageCount = pageCount;
            this.pageSize = pageSize;
        }

        public int getPageCount() {
            return pageCount;
        }

        public PageSize getPageSize() {
            return pageSize;
        }
    }
}
