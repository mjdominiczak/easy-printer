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
public class FileProcessor {

    private Map<PageSize, DefaultListModel<CustomFile>> fileListModelMap;

    private DefaultComboBoxModel<PageSize> pageSizeComboBoxModel;
    private JProgressBar progressBar;

    public FileProcessor(JProgressBar progressBar) {
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
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                count++;
            } else if (file.isDirectory()) {
                count +=countFilesInDirectory(file);
            }
        }
        return count;
    }

    public void clear() {
        pageSizeComboBoxModel.removeAllElements();
        fileListModelMap.forEach((k, v) -> v.removeAllElements());
    }
    
    public void sortListModels() {
        fileListModelMap.forEach((k, v) -> {
            List<CustomFile> list = Collections.list(v.elements());
            Collections.sort(list);
            v.clear();
            list.forEach(v::addElement);
        });
    }

    public void addFilesFromDirectory(Path path) {
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

    public void distributeDocuments() {
        for (int i = 0; i < fileListModelMap.get(PageSize.GENERAL).getSize(); i++) {
            CustomFile file = fileListModelMap.get(PageSize.GENERAL).get(i);
            if (file.getFileType().equals("application/pdf")) {
                DefaultListModel<CustomFile> listModel = fileListModelMap.get(file.getPageSize());
                if (!listModel.contains(file)) {
                    listModel.addElement(file);
                }
            }
        }
    }

    public DefaultListModel<CustomFile> getFileListModel(PageSize pageSize) {
        return fileListModelMap.get(pageSize);
    }

    public DefaultComboBoxModel<PageSize> getPageSizeComboBoxModel() {
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
                    incrementCounter();
                    CustomFile customFile = new CustomFile(file.toString());
                    DefaultListModel<CustomFile> listModel = fileListModelMap.get(PageSize.GENERAL);
                    if (!listModel.contains(customFile)) {
                        listModel.addElement(customFile);
                    }
                    if (customFile.getPageSize() == PageSize.VARIOUS) {
                        PDFHandler.splitDocument(customFile);
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
            sortListModels();
            setPageSizeComboBoxModel();
            super.done();
        }

        void incrementCounter() {
            counter.incrementAndGet();
        }
    }
}
