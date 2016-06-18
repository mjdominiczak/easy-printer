package com.mancode.easyprinter;

import javafx.print.Printer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Vector;

/**
 * Created by Manveru on 27.05.2016.
 */
public class PrintHandler {

    private PrinterJob printerJob;
    private PrintService printService;
    private PrintRequestAttributeSet printAttributes;

    public PrintHandler() {
        printerJob = PrinterJob.getPrinterJob();
        printService = printerJob.getPrintService();
    }

    public PrinterJob getPrinterJob() {
        return printerJob;
    }

    private void setPrintAttributes() {
        printAttributes = new HashPrintRequestAttributeSet();
    }

    public void printPDF(PDDocument document) throws PrinterException {
//        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
        PDFPageable pageableDocument = new PDFPageable(document);
        if (printService != null) {
            printerJob.setPrintService(printService);
        }
        printerJob.setPageable(pageableDocument);
        if (printerJob.printDialog()) {
            printerJob.print();
        }
    }

    public static Vector<String> getAvailablePrinters() {
        Vector<String> availablePrinters = new Vector<>();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            availablePrinters.add(service.getName());
        }
        return availablePrinters;
    }

    public static PrintService getDefaultPrinter() {
        return PrinterJob.getPrinterJob().getPrintService();
    }

    public void setPrintService(PrintService printService) {
        this.printService = printService;
    }
}
