package com.mancode.easyprinter;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Michal Dominiczak
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */
public class EasyPrinter extends JPanel implements ActionListener {

    private static final String programVersion = "v0.7";

    private static Properties properties;

    private static Logger logger = Logger.getLogger(FileProcessor.class.getName());
    private final int defaultFontSize = UIManager.getDefaults().getFont("List.font").getSize();
    private final Font defaultMonoFont = new Font(Font.MONOSPACED, Font.PLAIN, defaultFontSize);
    private MainPanel mainPanel;
    private JComboBox pageSizeComboBox;
    private FileProcessor fileProcessor;

    private EasyPrinter() {
        super(new MigLayout(
                "wrap",         // layout constraints
                "[grow]",       // columns constraints
                "[grow]"        // rows constraints
        ));

        loadProperties();
        setLogFormat();
        setLoggerProperties();

        fileProcessor = new FileProcessor(properties);
        mainPanel = new MainPanel();
        fileProcessor.setProgressBar(mainPanel.getProgressBar());
        add(mainPanel, "grow");
        pageSizeComboBox = new JComboBox<>(fileProcessor.getPageSizeComboBoxModel());
    }

    private void loadProperties() {
        Properties defaultProperties = new Properties();
        try {
            defaultProperties.load(getClass().getResourceAsStream("default.properties"));
            properties = new Properties(defaultProperties);
            FileInputStream in = new FileInputStream("user.properties");
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setLogFormat() {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                properties.getProperty("logFormat"));
    }

    private void setLoggerProperties() {
        try {
            FileHandler fileHandler;
            String logPath = "N:\\Public\\Dominiczak\\EasyPrinter\\Logs";
            File nFile = new File(logPath);
            if (nFile.exists()) {
                fileHandler = new FileHandler(logPath + "\\log%u.txt", true);
            } else {
                fileHandler = new FileHandler(System.getProperty("user.dir") + "\\log%u.txt", true);
            }
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        EasyPrinter easyPrinter = new EasyPrinter();
        frame.setContentPane(easyPrinter);
        frame.setJMenuBar(easyPrinter.createMenuBar());

        // Display the frame
        frame.setMinimumSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();

        menu = new JMenu("File");
        menuBar.add(menu);

        menuItem = new JMenuItem("Clear");
        menuItem.setActionCommand("clear");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menuItem = new JMenuItem("Exit");
        menuItem.setActionCommand("exit");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu = new JMenu("Help");
        menuBar.add(menu);

        menuItem = new JMenuItem("About");
        menuItem.setActionCommand("about");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        return menuBar;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("open")) {
            openDirectory();
        } else if (e.getActionCommand().equals("loadER")) {
            loadER();
        } else if (e.getActionCommand().equals("loadRaw")) {
            loadRaw();
        } else if (e.getActionCommand().equals("merge")) {
            merge();
        } else if (e.getActionCommand().equals("clear")) {
            clear();
        } else if (e.getActionCommand().equals("exit")) {
            exit();
        } else if (e.getActionCommand().equals("about")) {
            about();
        }
    }

    private void openDirectory() {
        final JFileChooser openFileChooser = new JFileChooser(properties.getProperty("defaultOpenPath"));
        openFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = openFileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            Path path = openFileChooser.getSelectedFile().toPath();
            mainPanel.stateUpdateDirectoryOpened(path.toString());
            fileProcessor.addFilesFromDirectory(path);
            properties.setProperty("defaultOpenPath", path.toString());
            saveProperties();
        }
        logger.info("\tUser: " + System.getProperty("user.name") + "\topen");
    }

    private void loadER() {
        final JFileChooser openFileChooser = new JFileChooser(properties.getProperty("defaultOpenPath"));
        openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = openFileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = openFileChooser.getSelectedFile();
            mainPanel.stateUpdateERLoaded(file.toString());
            properties.setProperty("defaultOpenPath", file.getParent());
            saveProperties();
            try {
                fileProcessor.addER(file);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
        logger.info("\tUser: " + System.getProperty("user.name") + "\tloadER");
    }

    private void loadRaw() {
        final JFileChooser openFileChooser = new JFileChooser(properties.getProperty("defaultOpenPath"));
        openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = openFileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = openFileChooser.getSelectedFile();
            mainPanel.stateUpdateRawLoaded(file.toString());
            properties.setProperty("defaultOpenPath", file.getParent());
            saveProperties();
            try {
                fileProcessor.addRaw(file);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
        logger.info("\tUser: " + System.getProperty("user.name") + "\tloadRaw");
    }

    private void merge() {
        File directory = new File(fileProcessor.getRootPath().toString() + "\\_Merged pdfs");
        boolean created = directory.mkdir();
        if (created || directory.exists()) {
            JOptionPane.showMessageDialog(this, "Merged files will be created at:\n" + directory.getPath());
            if (directory.isDirectory() && directory.listFiles().length > 0)
                JOptionPane.showMessageDialog(this, "Existing files will be overwritten!");
            for (int i = 0; i < pageSizeComboBox.getModel().getSize(); i++) {
                String filename = "all " + pageSizeComboBox.getModel().getElementAt(i).toString() + ".pdf";
                File newFile = new File(directory, filename);
                fileProcessor.mergeList(newFile, (PageSize)pageSizeComboBox.getModel().getElementAt(i));
            }
        }
        logger.info(fileProcessor.getLogInfo());
    }

//    private void print() {
//        mainList.getSelectedValue().printPDF();
//    }

//    private void merge() {
//        final JFileChooser saveFileChooser = new JFileChooser();
////        saveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        String filename = "all " + pageSizeComboBox.getSelectedItem().toString() + ".pdf";
//        saveFileChooser.setSelectedFile(new File(filename));
//        int returnValue = saveFileChooser.showSaveDialog(this);
//        if (returnValue == JFileChooser.APPROVE_OPTION) {
//            File file = saveFileChooser.getSelectedFile();
//            try {
//                fileProcessor.mergeList(file, (PageSize)pageSizeComboBox.getSelectedItem());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        logger.info("\tUser: " + System.getProperty("user.name") + "\tmerge");
//    }

    private void clear() {
        mainPanel.reset();
        fileProcessor.clear();
        logger.info("\tUser: " + System.getProperty("user.name") + "\tclear");
    }

    private void exit() {
        System.exit(0);
    }

    private void about() {
        JEditorPane aboutPane = new JEditorPane();
        aboutPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        aboutPane.setEditable(false);
        Color c = UIManager.getColor("OptionPane.background");
        String backgroundColor = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
        aboutPane.setBackground(c);
        aboutPane.setText("<p style=\"background-color:" + backgroundColor + "\"><b>EasyPrinter " + programVersion + "</b><br><br>" +
                "Author: Michał Dominiczak<br>" +
                "Mail: <a href=\"mailto:michal-dominiczak@o2.pl\">michal-dominiczak@o2.pl</a><p>");
        aboutPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().mail(e.getURL().toURI());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        JOptionPane.showMessageDialog(this, aboutPane, "About EasyPrinter", JOptionPane.PLAIN_MESSAGE);
    }

    private void saveProperties() {
        try {
            FileOutputStream out = new FileOutputStream("user.properties");
            properties.store(out, "Current properties state");
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MainPanel extends JPanel {
        private JButton openButton, loadERButton, loadRawButton, mergeButton;
        private JTextField openTextField, loadERTextField, loadRawTextField;
        private JList<CustomFile> mainList;
        private JTextArea console;
        private JProgressBar progressBar;

        private MainPanel() {
            setLayout(new MigLayout(
                    "wrap 2",
                    "[][grow]6",
                    "[][][][][grow]6[]"
            ));

            initComponents();
            addComponents();
        }

        private void initComponents() {
            openButton = getButton("Open directory", "open");
            openButton.setToolTipText("Open directory containing files to be printed");
            openButton.addActionListener(EasyPrinter.this);
            loadERButton = getButton("Load ER", "loadER");
            loadERButton.setToolTipText("Select location of the Engineering Release file, according to which files should be sorted");
            loadERButton.addActionListener(EasyPrinter.this);
            loadRawButton = getButton("Load raw", "loadRaw");
            loadRawButton.setToolTipText("Select location of the raw Excel file, according to which files should be sorted");
            loadRawButton.addActionListener(EasyPrinter.this);
            mergeButton = getButton("Merge", "merge");
            mergeButton.setToolTipText("Click to merge pdf files of the same page size into single files");
            mergeButton.addActionListener(EasyPrinter.this);
            openTextField = getTextField();
            loadERTextField = getTextField();
            loadRawTextField = getTextField();
            mainList = new JList<>(fileProcessor.getFileListModel(PageSize.GENERAL));
            mainList.setFont(defaultMonoFont);
            mainList.setCellRenderer(new ERHighlightingRenderer());
            console = new JTextArea();
            console.setFont(defaultMonoFont);
            console.setEditable(false);
            PrintStream printStream = new PrintStream(new MyOutputStream(console));
            System.setOut(printStream);
            System.setErr(printStream);
            progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            progressBar.setString("");
            reset();
        }

        private void addComponents() {
            add(openButton, "sg buttons");
            add(openTextField, "sg tfs, grow");
            add(loadERButton, "sg buttons");
            add(loadERTextField, "sg tfs, grow");
            add(loadRawButton, "sg buttons");
            add(loadRawTextField, "sg tfs, grow");
            add(mergeButton, "sg buttons, wrap");
            add(new JScrollPane(mainList), "span, grow");
            add(progressBar, "dock south, growx");
            add(new JScrollPane(console), "dock east, grow, width max(200, 25%), gapbottom 6");
        }

        private JButton getButton(String label, String action) {
            JButton button = new JButton(label);
            button.setActionCommand(action);
            return button;
        }

        private JTextField getTextField() {
            JTextField tf = new JTextField();
            tf.setEditable(false);
            return tf;
        }

        void reset() {
            openButton.requestFocus();
            loadERButton.setEnabled(false);
            loadRawButton.setEnabled(false);
            mergeButton.setEnabled(false);
            openTextField.setText("");
            openTextField.setEditable(false);
            loadERTextField.setText("");
            loadERTextField.setEditable(false);
            loadRawTextField.setText("");
            loadRawTextField.setEditable(false);
            console.setText("");
        }

        void stateUpdateDirectoryOpened(String path) {
            openTextField.setText(path);
            loadERButton.setEnabled(true);
            loadRawButton.setEnabled(true);
            mergeButton.setEnabled(true);
        }

        void stateUpdateERLoaded(String path) {
            loadERTextField.setText(path);
            loadRawTextField.setText("");
        }

        void stateUpdateRawLoaded(String path) {
            loadRawTextField.setText(path);
            loadERTextField.setText("");
        }

        JProgressBar getProgressBar() {
            return progressBar;
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