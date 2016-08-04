//package com.mancode.easyprinter;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.EnumMap;
//import java.util.Vector;
//
///**
// * Created by Manveru on 02.06.2016.
// */
//public class MainGui implements ActionListener, PropertyChangeListener {
//    private JPanel easyPrinter;
//    private JPanel selectionPanel;
//    private JTextField pathTextField;
//    private JButton openFolderButton;
//    private JLabel selectDirectoryLabel;
//    private JButton okButton;
//    private JButton cancelButton;
//    private JList generalJList;
//    private JScrollPane generalJListScrollPane;
//    private JList a3JList;
//    private JScrollPane a3JListScrollPane;
//    private JPanel filesPanel;
//    private JPanel buttonPanel;
//    private JButton runButton;
//    private JProgressBar progressBar;
//    private JComboBox selectA3PrinterComboBox;
//    private JLabel selectA3PrinterLabel;
//    private JButton printButton;
//    private EnumMap<PageSize, FilesHandler> filesHandlers;
//
//    private static final Dimension easyPrinterDimension = new Dimension(800, 600);
//
//    MainGui(EnumMap<PageSize, FilesHandler> filesHandlersMap) {
//        filesHandlers = filesHandlersMap;
//        cancelButton.addActionListener(this);
//        openFolderButton.addActionListener(this);
//        runButton.addActionListener(this);
//        generalJList.setModel(filesHandlers.get(PageSize.GENERAL).getFileListModel());
//        a3JList.setModel(filesHandlers.get(PageSize.A3).getFileListModel());
//        int defaultFontSize = UIManager.getDefaults().getFont("List.font").getSize();
//        generalJList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, defaultFontSize));
//        a3JList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, defaultFontSize));
//        easyPrinter.setPreferredSize(easyPrinterDimension);
//        setButtonSizes(cancelButton, okButton);
//
//        // Set combo box values
//        Vector<String> printers = PrintHandler.getAvailablePrinters();
//        for (String printer : printers) {
//            selectA3PrinterComboBox.addItem(printer);
//        }
//        selectA3PrinterComboBox.setSelectedItem(PrintHandler.getDefaultPrinter().getName());
//
//
////        progressBar.setValue(0);
//    }
//
//    private void setButtonSizes(JButton... buttons) {
//        Dimension preferredSize = new Dimension();
//        for (JButton button : buttons) {
//            Dimension d = button.getPreferredSize();
//            preferredSize = setLarger(preferredSize, d);
//        }
//        for (JButton button : buttons) {
//            button.setPreferredSize(preferredSize);
//        }
//    }
//
//    private Dimension setLarger(Dimension a, Dimension b) {
//        Dimension d = new Dimension();
//        d.height = Math.max(a.height, b.height);
//        d.width = Math.max(a.width, b.width);
//        return d;
//    }
//
//    private void openDirectory() {
//        final JFileChooser openFileChooser = new JFileChooser();
//        openFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        int returnValue = openFileChooser.showOpenDialog(easyPrinter);
//        if (returnValue == JFileChooser.APPROVE_OPTION) {
//            Path path = openFileChooser.getSelectedFile().toPath();
//            pathTextField.setText(path.toString());
//            filesHandlers.get(PageSize.GENERAL).addFilesFromDirectory(path, this);
//        }
//    }
//
//    private void runInspection() {
//        FilesHandler filesHandler = filesHandlers.get(PageSize.A3);
//        ArrayList<CustomFile> filesArray = filesHandlers.get(PageSize.GENERAL).extractPageSize(PageSize.A3);
//        filesHandler.clear();
//        filesHandler.addFile(filesArray);
//    }
//
//    public static void createAndShowGui(EnumMap<PageSize, FilesHandler> filesHandlersMap) {
//        try {
//            UIManager.setLookAndFeel(
//                UIManager.getSystemLookAndFeelClassName()
////                            UIManager.getCrossPlatformLookAndFeelClassName()
////                            "com.sun.java.swing.plaf.motif.MotifLookAndFeel"
////                            "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
//            );
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            System.err.println("ClassNotFoundException");
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//            System.err.println("InstantiationException");
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//            System.err.println("IllegalAccessException");
//        } catch (UnsupportedLookAndFeelException e) {
//            e.printStackTrace();
//            System.err.println("UnsupportedLookAndFeelException");
//        }
//        //Turn off metal's use of bold fonts
//        UIManager.put("swing.boldMetal", Boolean.FALSE);
//
//        JFrame frame = new JFrame("GUI");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        JFrame.setDefaultLookAndFeelDecorated(true);
//
//        frame.setContentPane(new MainGui(filesHandlersMap).easyPrinter);
//        frame.pack();
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        if(e.getSource() == cancelButton) {
//            System.exit(0);
//        } else if (e.getSource() == openFolderButton) {
//            openDirectory();
//        } else if (e.getSource() == runButton) {
//            runInspection();
//        } else if (e.getSource() == printButton) {
////            printA3(); TODO
//        }
//    }
//
//    @Override
//    public void propertyChange(PropertyChangeEvent evt) {
//        if (evt.getPropertyName().equals("progress")) {
//            int progress = (Integer) evt.getNewValue();
//            progressBar.setValue(progress);
//        }
//    }
//}
