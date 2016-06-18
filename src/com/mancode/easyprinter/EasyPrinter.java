package com.mancode.easyprinter;

import javax.print.PrintService;
import javax.swing.*;
import java.util.EnumMap;

/**
 * Created by Manveru on 22.05.2016.
 */
public class EasyPrinter {
    private static EnumMap<PageSize, FilesHandler> filesHandlersMap;

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                filesHandlersMap = new EnumMap<>(PageSize.class);
                filesHandlersMap.put(PageSize.GENERAL, new FilesHandler());
//                filesHandlersMap.put(PageSize.VARIOUS, new FilesHandler());
                filesHandlersMap.put(PageSize.A4_BOM, new FilesHandler());
//                filesHandlersMap.put(PageSize.A4, new FilesHandler());
                filesHandlersMap.put(PageSize.A3, new FilesHandler());
                filesHandlersMap.put(PageSize.A2, new FilesHandler());
                filesHandlersMap.put(PageSize.A1, new FilesHandler());
                filesHandlersMap.put(PageSize.A0, new FilesHandler());
                filesHandlersMap.put(PageSize.A0_1609, new FilesHandler());
//                filesHandlersMap.put(PageSize.A0_2450, new FilesHandler());
//                filesHandlersMap.put(PageSize.A0_3291, new FilesHandler());
//                filesHandlersMap.put(PageSize.A0_4132, new FilesHandler());

                MainGui.createAndShowGui(filesHandlersMap);
            }
        });

    }
}