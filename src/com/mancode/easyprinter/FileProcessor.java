package com.mancode.easyprinter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Michał Dominiczak
 * on 19.06.2016
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */
class FileProcessor {

    private Map<PageSize, DefaultListModel<CustomFile>> fileListModelMap;
    private DefaultComboBoxModel<PageSize> pageSizeComboBoxModel;
    private JProgressBar progressBar;
    private ERProcessor erProcessor;
    private Path rootPath;

    /**
     * Initializes fields with default, empty constructors,
     * assigns reference to progress bar for long-running tasks
     */
    FileProcessor(JProgressBar progressBar) {
        this.progressBar = progressBar;

        //Initialize structures for storing documents of different sizes
        fileListModelMap = new EnumMap<>(PageSize.class);
        for (PageSize pageSize : PageSize.values()) {
            fileListModelMap.put(pageSize, new DefaultListModel<>());
        }

        //Initialize combo box model
        pageSizeComboBoxModel = new DefaultComboBoxModel<>();
    }

    private static int countFilesInDirectory(File directory) {
        int count = 0;
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    count++;
                } else if (file.isDirectory() && !file.toPath().endsWith("_Merged pdfs") && !file.toPath().endsWith("_Split")) {
                    count += countFilesInDirectory(file);
                }
            }
        }
        return count;
    }

    String getLogInfo() {
        return  "\tUser: " + System.getProperty("user.name") +
                "\tmergeAll" +
                "\tNumber of files: " + fileListModelMap.get(PageSize.GENERAL).getSize() +
                "\tPath: " + getRootPath().toString();
    }

    Path getRootPath() {
        return rootPath;
    }

    void clear() {
        pageSizeComboBoxModel.removeAllElements();
        fileListModelMap.forEach((k, v) -> v.removeAllElements());
    }
    
    void addFilesFromDirectory(Path path) {
        rootPath = path;
        FileProcessorWorker worker = new FileProcessorWorker(path);
        worker.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("progress")) {
                Integer progress = (Integer) evt.getNewValue();
                progressBar.setValue(Math.min(progress, 100));
                if (progress >= 100) {
                    progressBar.setValue(0);
                    progressBar.setStringPainted(false);
                }
            }
        });
        worker.execute();
    }

    /**
     * Method for adding ER file and reading a drawings reference order from it.
     *
     * @param engineeringRelease Engineering Release file
     * @throws IOException Throws an exception when ER file is in format other than .xlsx
     */
    void addER(File engineeringRelease) throws IOException {
        String name = engineeringRelease.getName();
        if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
            erProcessor = new ERProcessor(engineeringRelease, 1);
            sortListModels(SortType.REFERENCE_LIST);
            updateExistsInER();
            checkERConsistency();
        } else {
            throw new IOException("ER file must be an Excel file!");
        }
    }

    private void sortListModels(SortType sortType) {
        fileListModelMap.forEach((k, v) -> {
            List<CustomFile> list = Collections.list(v.elements());
            if (!list.isEmpty() && k != PageSize.VARIOUS) {
                switch (sortType) {
                    case ASCENDING:
                        Collections.sort(list);
                        break;
                    case DESCENDING:
                        Collections.sort(list);
                        Collections.reverse(list);
                        break;
                    case REFERENCE_LIST:
                        Collections.sort(list, erProcessor.new ERComparator());
                        break;
                }
                v.clear();
                list.forEach(v::addElement);
            }
        });
    }

    private void updateExistsInER() {
        boolean inconsistent = false;
        List<FileSignature> inconsistentList = new ArrayList<>();
        for (int i = 0; i < fileListModelMap.get(PageSize.GENERAL).getSize(); i++) {
            CustomFile file = fileListModelMap.get(PageSize.GENERAL).get(i);
            if (erProcessor.getReferenceList().contains(file.getSignature())) {
                file.setExistsInER(1);
            } else {
                file.setExistsInER(0);
                inconsistentList.add(file.getSignature());
                if (!inconsistent) inconsistent = true;
            }
        }
        if (inconsistent) {
//            Collections.sort(inconsistentList);
            System.err.println("---WARNING!---");
            System.err.println("Drawings/BOMs not found in ER:");
            inconsistentList.forEach(System.err::println);
            System.err.println("===========");
        }
    }

    private void checkERConsistency() {
        boolean inconsistent = false;
        List<FileSignature> inconsistentList = new ArrayList<>();
        for (FileSignature erSignature : erProcessor.getReferenceList()) {
            DefaultListModel<CustomFile> listModel = fileListModelMap.get(PageSize.GENERAL);
            boolean check = false;
            for (int i = 0; i < listModel.getSize(); i++) {
                FileSignature fileSignature = listModel.get(i).getSignature();
                if (erSignature.equals(fileSignature)) {
                    check = true;
                    break;
                }
            }
            if (!check) {
                if (!inconsistent) inconsistent = true;
                inconsistentList.add(erSignature);
            }
        }
        if (inconsistent) {
            Collections.sort(inconsistentList);
            System.err.println("---WARNING!---");
            System.err.println("Drawings/BOMs not found in specified directory:");
            inconsistentList.forEach(System.err::println);
            System.err.println("===========");
        }
    }

    void addRaw(File rawSheet) throws IOException {
        String name = rawSheet.getName();
        if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
            erProcessor = new ERProcessor(rawSheet, 0);
            sortListModels(SortType.REFERENCE_LIST);
            updateExistsInER();
            checkERConsistency();
        } else {
            throw new IOException("Raw file must be an Excel file!");
        }
    }

    private void distributeDocuments() {
        for (int i = 0; i < fileListModelMap.get(PageSize.GENERAL).getSize(); i++) {
            CustomFile file = fileListModelMap.get(PageSize.GENERAL).get(i);
            if (file.getFileType().equals("application/pdf")) {
                DefaultListModel<CustomFile> listModel = fileListModelMap.get(file.getPageSize());
                if (!listModel.contains(file)) {
                    listModel.addElement(file);
                }
                if (file.getPageSize() != PageSize.A4_BOM) {
                    listModel = fileListModelMap.get(PageSize.ALL_SIZES);
                    listModel.addElement(file);
                }
            }
        }
    }

    DefaultComboBoxModel<PageSize> getPageSizeComboBoxModel() {
        return pageSizeComboBoxModel;
    }

    private void setPageSizeComboBoxModel() {
        for (PageSize pageSize : PageSize.values()) {
            if (!fileListModelMap.get(pageSize).isEmpty() && pageSize != PageSize.GENERAL && pageSize != PageSize.VARIOUS) {
                if (pageSizeComboBoxModel.getIndexOf(pageSize) == -1) {
                    pageSizeComboBoxModel.addElement(pageSize);
                }
            }
        }
    }

    /**
     * Method for merging single pages of the same size into one file.
     *
     * @param file        output file
     * @param pageSize    size of documents
     */
    void mergeList(File file, PageSize pageSize) {
        PDFHandler.mergePDFs(file, getFileListModel(pageSize));
    }

    DefaultListModel<CustomFile> getFileListModel(PageSize pageSize) {
        return fileListModelMap.get(pageSize);
    }

    private enum SortType {
        ASCENDING,
        DESCENDING,
        REFERENCE_LIST
    }

    // Inner class implementing worker for running long lasting tasks in the background
    private class FileProcessorWorker extends SwingWorker<Void, Void> {

        private final Path path;
        private final int maxDepth = 3;
        private AtomicInteger counter = new AtomicInteger(0);

        FileProcessorWorker(Path path) {
            this.path = path;
        }

        @Override
        protected Void doInBackground() throws Exception {
            progressBar.setStringPainted(true);
            int filesCount = countFilesInDirectory(path.toFile());
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), maxDepth, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getParent().endsWith("_Merged pdfs") || file.getParent().endsWith("_Split")) return super.visitFile(file, attrs);
                    incrementCounter();
                    CustomFile customFile = new CustomFile(file.toString());
                    DefaultListModel<CustomFile> listModel = fileListModelMap.get(PageSize.GENERAL);
                    if (!listModel.contains(customFile) &&
                            customFile.getName().toLowerCase().endsWith(".pdf")) {
                        if (customFile.getPageSize() == PageSize.VARIOUS) {
                            progressBar.setIndeterminate(true);
                            PDFHandler.splitDocument(customFile, rootPath).forEach(listModel::addElement);
                            progressBar.setIndeterminate(false);
                        } else {
                            listModel.addElement(customFile);
                        }
                    }
                    int progress = 100 * counter.get() / filesCount;
                    setProgress(progress);
                    return super.visitFile(file, attrs);
                }
            });
            return null;
        }

        @Override
        protected void done() {
            distributeDocuments();
            setPageSizeComboBoxModel();
            super.done();
        }

        void incrementCounter() {
            counter.incrementAndGet();
        }
    }
}
