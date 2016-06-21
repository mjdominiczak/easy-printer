package com.mancode.easyprinter;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Manveru on 28.05.2016.
 */
public class FilesHandler extends SimpleFileVisitor<Path> {

    private ArrayList<CustomFile> fileList;
    private ArrayList<CustomFile> fileQueue;
    private DefaultListModel<String> fileListModel;
    private Task task;
    private MainGui gui;
    private static final int maxDepth = 3;

    class Task extends SwingWorker<Void, Void> {
        private final ArrayList<CustomFile> filesArray;

        Task(List filesArray) {
            this.filesArray = new ArrayList<>();
            this.filesArray.addAll(filesArray);
        }

        @Override
        protected Void doInBackground() throws Exception {
            int progress;
            int maxProgress = filesArray.size();
            setProgress(0);
            for (int i = 0; i < maxProgress; i++) {
                CustomFile file = filesArray.get(i);
                addFile(file);
                progress = (int)((i + 1.) / (maxProgress) * 100);
                setProgress(Math.min(progress, 100));
            }
            return null;
        }

        @Override
        protected void done() {
            updateFileListModel();
            super.done();
        }
    }

    public FilesHandler() {
        fileList = new ArrayList<>();
        fileQueue= new ArrayList<>();
        fileListModel = new DefaultListModel<>();
    }

    public void addFile(CustomFile file) {
        if (!fileList.contains(file)) {
            if (file.getPageSize() == PageSize.VARIOUS) {
                PDFHandler pdfHandler = new PDFHandler(file);
                ArrayList<CustomFile> filePages = pdfHandler.splitDocument();

//                pdfHandler.printPDF(); TODO
                pdfHandler.closePDF();
                for (CustomFile filePage : filePages) {
                    fileList.add(filePage);
                    fileListModel.addElement(filePage.toString());
                }
            } else {
                fileList.add(file);
                fileListModel.addElement(file.toString());
            }
        } else {
            System.err.println("File already added: " + file.getName());
        }
    }

    public void addFile(ArrayList<CustomFile> filesArray){
        List synchronizedFilesArray = Collections.synchronizedList(new ArrayList<CustomFile>());
        synchronizedFilesArray.addAll(filesArray);
        task = new Task(synchronizedFilesArray);
        task.addPropertyChangeListener(gui);
        task.execute();
    }

    public void clear() {
        fileList.clear();
        fileListModel.clear();
    }

    public void removeFile(CustomFile file) {
        if (fileList.contains(file)) {
            fileList.remove(file);
        } else {
            System.err.println("File not found.");
        }
    }

//    public void inspectFiles() {
//        for (CustomFile file : fileList) {
//            System.out.println("============");
//            System.out.println(file);
//            System.out.println(file.getFileType());
//            if (file.getFileType().equals("application/pdf")) {
//                PDFHandler pdfHandler = new PDFHandler(file);
//                for (int i = 0; i < pdfHandler.getPageCount(); i++) {
//                    System.out.println("Page " + i + ": " + pdfHandler.getPageSize(i));
//                }
//                pdfHandler.closePDF();
//            }
//        }
//    }

    public void addFilesFromDirectory(Path path, MainGui mainGui) {
        gui = mainGui;
        try {
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), maxDepth, this);
            addFile(fileQueue);
            fileQueue.clear();
        } catch (IOException e) {
            System.err.println("IOException while walking the file tree.");
            e.printStackTrace();
        }
        updateFileListModel();
    }

    public void updateFileListModel() {
        fileListModel.clear();
        for (CustomFile file : fileList) {
            fileListModel.addElement(file.toString());
        }
    }

    public ArrayList<CustomFile> extractPageSize(PageSize pageSize) {
        ArrayList<CustomFile> filesArray = new ArrayList<>();
        for (CustomFile file : fileList) {
            if (file.getPageSize() == pageSize) {
                filesArray.add(file);
            }
        }
        return filesArray;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        CustomFile customFile = new CustomFile(path.toString());
        fileQueue.add(customFile);
        return super.visitFile(path, attrs);
    }

    public DefaultListModel<String> getFileListModel() {
        return fileListModel;
    }
}
