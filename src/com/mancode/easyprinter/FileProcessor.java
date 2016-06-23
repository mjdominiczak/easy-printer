package com.mancode.easyprinter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Manveru on 19.06.2016.
 */
public class FileProcessor {

    private Map<PageSize, DefaultListModel<CustomFile>> fileListModelMap;
    private JProgressBar progressBar;

    // Inner class implementing worker for running long lasting tasks in the background
    private class FileProcessorWorker extends SwingWorker<Void, Void> {

        private final Path path;
        private final int maxDepth = 3;
        private AtomicInteger counter = new AtomicInteger(0);

        public FileProcessorWorker(Path path) {
            this.path = path;
        }

        @Override
        protected Void doInBackground() throws Exception {
            int filesCount = countFilesInDirectory(path.toFile());
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), maxDepth, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    incrementCounter();
                    CustomFile customFile = new CustomFile(file.toString());
                    fileListModelMap.get(PageSize.GENERAL).addElement(customFile); // TODO: wprowadzić dzielenie plików na strony
                    int progress = 100 * counter.get() / filesCount;
//                    System.out.println(progress + ": " + file.toString());
                    setProgress(progress);
                    return super.visitFile(file, attrs);
                }
            });
            return null;
        }

        void incrementCounter() {
            counter.incrementAndGet();
        }
    }
    
    
    public FileProcessor(JProgressBar progressBar) {
        this.progressBar = progressBar;

        // Initialize structures for storing documents of different sizes
        fileListModelMap = new EnumMap<>(PageSize.class);
        fileListModelMap.put(PageSize.GENERAL, new DefaultListModel<>());
        fileListModelMap.put(PageSize.VARIOUS, new DefaultListModel<>());
        fileListModelMap.put(PageSize.A4_BOM, new DefaultListModel<>());
        fileListModelMap.put(PageSize.A4, new DefaultListModel<>());
        fileListModelMap.put(PageSize.A3, new DefaultListModel<>());
        fileListModelMap.put(PageSize.A2, new DefaultListModel<>());
        fileListModelMap.put(PageSize.A1, new DefaultListModel<>());
        fileListModelMap.put(PageSize.A0, new DefaultListModel<>());
        fileListModelMap.put(PageSize.A0_1609, new DefaultListModel<>());
        fileListModelMap.put(PageSize.A0_2450, new DefaultListModel<>());
        fileListModelMap.put(PageSize.A0_3291, new DefaultListModel<>());
        fileListModelMap.put(PageSize.A0_4132, new DefaultListModel<>());

    }

    public void addFilesFromDirectory(Path path) {
        FileProcessorWorker worker = new FileProcessorWorker(path);
        worker.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("progress")) {
                Integer progress = (Integer) evt.getNewValue();
                progressBar.setValue(Math.min(progress, 100));
            }
        });
        worker.execute();
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

    public DefaultListModel<CustomFile> getFileListModel(PageSize pageSize) {
        return fileListModelMap.get(pageSize);
    }
}
