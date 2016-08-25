package com.mancode.easyprinter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by Michal Dominiczak
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */
public class EasyPrinter extends JPanel implements ActionListener, ItemListener {

    private final Insets defaultInsets = new Insets(5, 5, 5, 5);
    private final JPanel additionalListsPanel;
    private final JList<CustomFile> mainList;
    private JButton openFolderButton;
    private JButton clearButton;
    private JButton openERButton;
    private JButton printButton;
    private JButton mergeButton;
    private JButton mergeAllButton;
    private JProgressBar progressBar;
    private JTextField pathTextField;
    private JComboBox pageSizeComboBox;
    private JTextArea console;
    private FileProcessor fileProcessor;

    public EasyPrinter() {
        super(new GridBagLayout());

        int defaultFontSize = UIManager.getDefaults().getFont("List.font").getSize();
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, defaultFontSize);

        //Initialize progress bar
        progressBar = new JProgressBar(0, 100);

        //Initialize the file processor
        fileProcessor = new FileProcessor(progressBar);

        //Initialize a console
        console = new JTextArea(30,40);
        console.setFont(font);
        console.setEditable(false);
//        console.setBorder(BorderFactory.createLineBorder(Color.black));
        PrintStream printStream = new PrintStream(new MyOutputStream(console));
        System.setOut(printStream);
        System.setErr(printStream);

        //Set console panel
        JScrollPane consoleScrollPane = new JScrollPane(console);

        //Set components in selectionPanel
        JLabel selectDirectoryLabel = new JLabel("Open directory with data to be printed");
        pathTextField = new JTextField();
        pathTextField.setEnabled(false);
        openFolderButton = new JButton("Open");
        openFolderButton.setActionCommand("open");
        openFolderButton.addActionListener(this);

        //Set selectionPanel
        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.add(selectDirectoryLabel, BorderLayout.NORTH);
        selectionPanel.add(pathTextField, BorderLayout.CENTER);
        selectionPanel.add(openFolderButton, BorderLayout.EAST);

        //Set components in mainListPanel
        mainList = new JList<>(fileProcessor.getFileListModel(PageSize.GENERAL));
        mainList.setCellRenderer(new ERHighlightingRenderer());

        //Set mainListPanel
        JScrollPane mainListPanel = new JScrollPane(mainList);

        //Set components in cardAXPanels
        //Set additionalListsPanel
        additionalListsPanel = new JPanel(new CardLayout());
        Map<PageSize, JList<CustomFile>> additionalListsMap = new EnumMap<>(PageSize.class);
        Map<PageSize, JScrollPane> scrollPanesMap = new EnumMap<>(PageSize.class);
        mainList.setFont(font);
        for (PageSize pageSize : PageSize.values()) {
            if (pageSize != PageSize.GENERAL &&
                    pageSize != PageSize.VARIOUS) {
                JList<CustomFile> thisList = new JList<>(fileProcessor.getFileListModel(pageSize));
//                JList<CustomFile> thisList = new JList<>(fileProcessor.getFileListModel(PageSize.GENERAL));
                additionalListsMap.put(pageSize, thisList);
                JScrollPane scrollPane = new JScrollPane(thisList);
                scrollPanesMap.put(pageSize, scrollPane);
                thisList.setFont(font);
                JPanel listPanel = new JPanel(new BorderLayout());
                listPanel.add(scrollPane);
                additionalListsPanel.add(listPanel, pageSize.getText());
            }
        }

        //Set pageSizeComboBoxPanel
        JPanel pageSizeComboBoxPanel = new JPanel(new BorderLayout());
        pageSizeComboBox = new JComboBox<>(fileProcessor.getPageSizeComboBoxModel());
        pageSizeComboBox.setEditable(false);
        pageSizeComboBox.addItemListener(this);
//        pageSizeComboBox.setEnabled(false);
        pageSizeComboBoxPanel.add(pageSizeComboBox);

        //Set components in progressPanel
        progressBar.setValue(0);
        progressBar.setStringPainted(false);

        //Set progressPanel
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        //Set components in buttonsPanel
        openERButton = new JButton("Load ER");
        openERButton.setActionCommand("loadER");
        openERButton.addActionListener(this);

        clearButton = new JButton("Clear");
        clearButton.setActionCommand("clear");
        clearButton.addActionListener(this);

        printButton = new JButton("Print");
        printButton.setActionCommand("print");
        printButton.addActionListener(this);

        mergeButton = new JButton("Merge this list");
        mergeButton.setActionCommand("merge");
        mergeButton.addActionListener(this);

        mergeAllButton = new JButton("Merge all lists");
        mergeAllButton.setActionCommand("mergeAll");
        mergeAllButton.addActionListener(this);

        //Set buttonsPanel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(openERButton);
//        buttonsPanel.add(printButton);
        buttonsPanel.add(mergeButton);
        buttonsPanel.add(mergeAllButton);
        buttonsPanel.add(clearButton);

        //Define constraints for top level panels and add them to the main layout
        //Set constraints for selectionPanel
        GridBagConstraints selectionPanelConstraints = new GridBagConstraints(
                //gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
                0, 0, 2, 1, 0.5, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, defaultInsets, 0, 0);

        //Set constraints for mainListPanel
        GridBagConstraints mainListConstraints = new GridBagConstraints(
                0, 1, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0);

        //Set constraints for pageSizeComboBoxPanel
        GridBagConstraints pageSizeComboBoxConstraints = new GridBagConstraints(
                1, 1, 1, 1, 0.5, 0, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, defaultInsets, 0, 0);

        //Set constraints for additionalListsPanel
        GridBagConstraints additionalListsConstraints = new GridBagConstraints(
                1, 2, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0);

        //Set constraints for progressPanel
        GridBagConstraints progressPanelConstraints = new GridBagConstraints(
                0, 3, 1, 1, 0.5, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, defaultInsets, 0, 0);

        //Set constraints for buttonsPanel
        GridBagConstraints buttonsPanelConstraints = new GridBagConstraints(
                1, 3, 1, 1, 0.5, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, defaultInsets, 0, 0);

        //Set constraints for consoleScrollPane
        GridBagConstraints consoleScrollPaneConstraints = new GridBagConstraints(
                2, 0, 1, 4, 0.5, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0);

        //Add panels to the main layout
        add(selectionPanel, selectionPanelConstraints);
        add(mainListPanel, mainListConstraints);
        add(pageSizeComboBoxPanel, pageSizeComboBoxConstraints);
        add(additionalListsPanel, additionalListsConstraints);
        add(progressPanel, progressPanelConstraints);
        add(buttonsPanel, buttonsPanelConstraints);
        add(consoleScrollPane, consoleScrollPaneConstraints);

    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(EasyPrinter::createAndShowGui);

    }

    private static void createAndShowGui() {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
//                    UIManager.getCrossPlatformLookAndFeelClassName()
//                    "com.sun.java.swing.plaf.motif.MotifLookAndFeel"
//                    "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
            );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("ClassNotFoundException");
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.err.println("InstantiationException");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.err.println("IllegalAccessException");
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
            System.err.println("UnsupportedLookAndFeelException");
        }
        // Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        // Create and set up the JFrame
        JFrame frame = new JFrame("EasyPrinter");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JFrame.setDefaultLookAndFeelDecorated(true);

        // Create and set up the content pane
        frame.setContentPane(new EasyPrinter());

        // Display the frame
        frame.setMinimumSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("open")) {
            openDirectory();
        } else if (e.getActionCommand().equals("loadER")) {
            loadER();
        } else if (e.getActionCommand().equals("print")) {
            print();
        } else if (e.getActionCommand().equals("merge")) {
            merge();
        } else if (e.getActionCommand().equals("mergeAll")) {
            mergeAll();
        } else if (e.getActionCommand().equals("clear")) {
            clear();
        }
    }

    private void openDirectory() {
        final JFileChooser openFileChooser = new JFileChooser();
        openFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = openFileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            Path path = openFileChooser.getSelectedFile().toPath();
            pathTextField.setText(path.toString());
            fileProcessor.addFilesFromDirectory(path);
        }
    }

    private void loadER() {
        final JFileChooser openFileChooser = new JFileChooser();
        openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = openFileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = openFileChooser.getSelectedFile();
            try {
                fileProcessor.addER(file);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    private void print() {
        mainList.getSelectedValue().printPDF();
    }

    private void merge() {
        final JFileChooser saveFileChooser = new JFileChooser();
//        saveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String filename = "all " + pageSizeComboBox.getSelectedItem().toString() + ".pdf";
        saveFileChooser.setSelectedFile(new File(filename));
        int returnValue = saveFileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = saveFileChooser.getSelectedFile();
            try {
                fileProcessor.mergeList(file, (PageSize)pageSizeComboBox.getSelectedItem());
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    private void mergeAll() {
        File directory = new File(fileProcessor.getRootPath().toString() + "\\_Merged pdfs");
        boolean created = directory.mkdir();
        if (created || directory.exists()) {
            JOptionPane.showMessageDialog(this, "Merged files will be created at:\n" + directory.getPath());
            if (directory.isDirectory() && directory.listFiles().length > 0)
                JOptionPane.showMessageDialog(this, "Existing files will be overwritten!");
            for (int i = 0; i < pageSizeComboBox.getModel().getSize(); i++) {
                String filename = "all " + pageSizeComboBox.getModel().getElementAt(i).toString() + ".pdf";
                CustomFile newFile = new CustomFile(directory, filename);
                fileProcessor.mergeList(newFile, (PageSize)pageSizeComboBox.getModel().getElementAt(i));
            }
        }
    }

    private void clear() {
        pathTextField.setText("");
        console.setText("");
        fileProcessor.clear();
        openFolderButton.requestFocus();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == pageSizeComboBox) {
            CardLayout cl = (CardLayout)(additionalListsPanel.getLayout());
            cl.show(additionalListsPanel, e.getItem().toString());
        }
    }

    private class ERHighlightingRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            CustomFile fileValue = (CustomFile) value;
            setText(fileValue.toString());
            if (!isSelected) {
                if (fileValue.getExistsInER() == 1) {
                    component.setBackground(new Color(140,255,140));
                } else if (fileValue.getExistsInER() == 0) {
                    component.setBackground(new Color(255,140,140));
                } else {
                    component.setBackground(new Color(255,190,90));
                }
            }
            return component;
        }
    }
}