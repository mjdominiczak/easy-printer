package com.mancode.easyprinter;

import java.util.EnumMap;

/**
 * Created by Manveru on 19.06.2016.
 */
public class FileProcessor {

    private EnumMap<PageSize, FileListModel> FileListModelMap;

    public FileProcessor() {

        // Initialize structures for storing documents of different sizes
        FileListModelMap = new EnumMap<>(PageSize.class);
        FileListModelMap.put(PageSize.GENERAL, new FileListModel());
//                FileListModelMap.put(PageSize.VARIOUS, new FileListModel());
        FileListModelMap.put(PageSize.A4_BOM, new FileListModel());
//                FileListModelMap.put(PageSize.A4, new FileListModel());
        FileListModelMap.put(PageSize.A3, new FileListModel());
        FileListModelMap.put(PageSize.A2, new FileListModel());
        FileListModelMap.put(PageSize.A1, new FileListModel());
        FileListModelMap.put(PageSize.A0, new FileListModel());
        FileListModelMap.put(PageSize.A0_1609, new FileListModel());
//                FileListModelMap.put(PageSize.A0_2450, new FileListModel());
//                FileListModelMap.put(PageSize.A0_3291, new FileListModel());
//                FileListModelMap.put(PageSize.A0_4132, new FileListModel());

    }
}
