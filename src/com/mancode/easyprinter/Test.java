//package com.mancode.easyprinter;
//
//import net.miginfocom.swing.MigLayout;
//
//import javax.swing.*;
//import java.awt.*;
//
///**
// * Created by Michał Dominiczak
// * on 20.07.2016
// * e-mail: michal-dominiczak@o2.pl
// * Copyright reserved
// */
//
//class Test extends JPanel {
//
//    private JButton openButton, loadERButton, loadRawButton, mergeButton;
//    private JTextField openTextField, loadERTextField, loadRawTextField;
//    private JList<CustomFile> mainList;
//
//    private Test() {
//        setLayout(new MigLayout(
//                "wrap 2",
//                "[][grow]"
//        ));
//
//        initComponents();
//        addComponents();
//    }
//
//    private void initComponents() {
//        openButton = getButton("Open", "open");
//        loadERButton = getButton("Load ER", "loadER");
//        loadERButton.setEnabled(false);
//        loadRawButton = getButton("Load raw", "loadRaw");
//        loadRawButton.setEnabled(false);
//        mergeButton = getButton("Merge", "merge");
//        mergeButton.setEnabled(false);
//        openTextField = new JTextField();
//        loadERTextField = new JTextField();
//        loadERTextField.setEnabled(false);
//        loadRawTextField = new JTextField();
//        loadRawTextField.setEnabled(false);
//        mainList = new JList<>();
////        mainList.setCellRenderer(new EasyPrinter.ERHighlightingRenderer());
//    }
//
//    private void addComponents() {
//        add(openButton, "sg buttons");
//        add(openTextField, "sg tfs, growx");
//        add(loadERButton, "sg buttons");
//        add(loadERTextField, "sg tfs, growx");
//        add(loadRawButton, "sg buttons");
//        add(loadRawTextField, "sg tfs, growx");
//        add(mergeButton, "sg buttons, wrap");
//        add(new JScrollPane(mainList));
//    }
//
//    private JButton getButton(String label, String action) {
//        JButton button = new JButton(label);
//        button.setActionCommand(action);
//        return button;
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(Test::initUI);
//    }
//
//    private static void initUI() {
//        JFrame frame = new JFrame("Test");
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.setContentPane(new Test());
//        frame.setMinimumSize(new Dimension(800, 600));
//        frame.pack();
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
//    }
//
//}
