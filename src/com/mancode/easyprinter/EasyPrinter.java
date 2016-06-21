package com.mancode.easyprinter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;

/**
 * Created by Manveru on 22.05.2016.
 */
public class EasyPrinter extends JPanel implements ActionListener {

    private JButton runButton;
    private JButton openFolderButton;
    private JProgressBar progressBar;
    private JLabel selectDirectoryLabel;
    private JTextField pathTextField;

    private FileProcessor fileProcessor;

    public EasyPrinter() {
        super(new BorderLayout());

        selectDirectoryLabel = new JLabel("Select directory with data to be printed");
        pathTextField = new JTextField();
        pathTextField.setEnabled(false);
        openFolderButton = new JButton("Open");
        openFolderButton.setActionCommand("open");
        openFolderButton.addActionListener(this);

        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.add(selectDirectoryLabel, BorderLayout.NORTH);
        selectionPanel.add(pathTextField, BorderLayout.CENTER);
        selectionPanel.add(openFolderButton, BorderLayout.EAST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        JPanel progressPanel = new JPanel();
        progressPanel.add(progressBar);


        add(selectionPanel, BorderLayout.NORTH);
        add(progressPanel, BorderLayout.SOUTH);


        fileProcessor = new FileProcessor(progressBar);
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
                    UIManager.getSystemLookAndFeelClassName()
//                            UIManager.getCrossPlatformLookAndFeelClassName()
//                            "com.sun.java.swing.plaf.motif.MotifLookAndFeel"
//                            "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
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