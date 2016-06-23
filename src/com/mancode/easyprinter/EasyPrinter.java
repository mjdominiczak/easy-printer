package com.mancode.easyprinter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;

/**
 * Created by Michal Dominiczak
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */
public class EasyPrinter extends JPanel implements ActionListener {

    private JButton runButton;
    private JButton openFolderButton;
    private JProgressBar progressBar;
    private JLabel selectDirectoryLabel;
    private JTextField pathTextField;

    private FileProcessor fileProcessor;

    private final Insets defaultInsets = new Insets(5, 5, 5, 5);

    public EasyPrinter() {
        super(new GridBagLayout());

        //Initialize progress bar
        progressBar = new JProgressBar(0, 100);

        //Initialize the file processor
        fileProcessor = new FileProcessor(progressBar);

        //Set components in selectionPanel
        selectDirectoryLabel = new JLabel("Select directory with data to be printed");
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
        JList<CustomFile> mainList = new JList<>(fileProcessor.getFileListModel(PageSize.GENERAL));
        int defaultFontSize = UIManager.getDefaults().getFont("List.font").getSize();
        mainList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, defaultFontSize));

        //Set mainListPanel
        JScrollPane mainListPanel = new JScrollPane(mainList);

        //Set components in additionalListsPanel
        JList<CustomFile> listA3 = new JList<>(fileProcessor.getFileListModel(PageSize.A3));
        listA3.setFont(new Font(Font.MONOSPACED, Font.PLAIN, defaultFontSize));

        //Set additionalListsPanel
        JPanel additionalListsPanel = new JPanel(new CardLayout());
        JScrollPane listA3Panel= new JScrollPane(listA3);
        additionalListsPanel.add(listA3Panel);

        //Set components in progressPanel
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        //Set progressPanel
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        //Define constraints for top level panels and add them to the main layout
        //Set constraints for selectionPanel
        GridBagConstraints selectionPanelConstraints = new GridBagConstraints();
        selectionPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
        selectionPanelConstraints.gridx = 0;
        selectionPanelConstraints.gridy = 0;
        selectionPanelConstraints.gridwidth = 2;
        selectionPanelConstraints.weightx = 0.5;
        selectionPanelConstraints.insets = defaultInsets;

        //Set constraints for mainListPanel
        GridBagConstraints mainListConstraints = new GridBagConstraints();
        mainListConstraints.fill = GridBagConstraints.BOTH;
        mainListConstraints.gridx = 0;
        mainListConstraints.gridy = 1;
        mainListConstraints.weightx = 0.5;
        mainListConstraints.weighty = 1;
        mainListConstraints.insets = defaultInsets;

        //Set constraints for additionalListsPanel
        GridBagConstraints additionalListsConstraints = new GridBagConstraints();
        additionalListsConstraints.fill = GridBagConstraints.BOTH;
        additionalListsConstraints.gridx = 1;
        additionalListsConstraints.gridy = 1;
        additionalListsConstraints.weightx = 0.5;
        additionalListsConstraints.weighty = 1;
        additionalListsConstraints.insets = defaultInsets;

        //Set constraints for progressPanel
        GridBagConstraints progressPanelConstraints = new GridBagConstraints();
        progressPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
        progressPanelConstraints.gridx = 0;
        progressPanelConstraints.gridy = 2;
        progressPanelConstraints.gridwidth = 1;
        progressPanelConstraints.weightx = 0.5;
        progressPanelConstraints.insets = defaultInsets;

        //Add panels to the main layout
        add(selectionPanel, selectionPanelConstraints);
        add(mainListPanel, mainListConstraints);
        add(additionalListsPanel, additionalListsConstraints);
        add(progressPanel, progressPanelConstraints);

    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(() -> {
            createAndShowGui();
        });

    }

    private static void createAndShowGui() {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(
//                    UIManager.getSystemLookAndFeelClassName()
                    UIManager.getCrossPlatformLookAndFeelClassName()
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

//    @Override
//    public void propertyChange(PropertyChangeEvent evt) {
//        if (evt.getPropertyName().equals("progress")) {
//            int progress = (Integer) evt.getNewValue();
//            progressBar.setValue(progress);
//        }
//    }
}