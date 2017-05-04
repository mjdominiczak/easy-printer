package com.mancode.easyprinter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michał Dominiczak
 * on 04.05.2017
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */

class CoversPanel extends JPanel {

    private ProjectInfoParser pip;
    private List<JTextField> tfList;

    CoversPanel(String path) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        pip = new ProjectInfoParser(path);
        tfList = new ArrayList<>();
        addFields();
    }

    private void addFields() {
        addFieldWithLabel("Project Number", pip.getProjectNumber());
        addFieldWithLabel("Project Name", pip.getProjectName());
        addFieldWithLabel("Conveyor Type", pip.getConveyorType());
        addFieldWithLabel("Engineering Release Number", pip.getEngReleaseNumber());
        addFieldWithLabel("Release Date", pip.getReleaseDate());
        addFieldWithLabel("User", pip.getUser());
        addFieldWithLabel("Phone Number", pip.getPhoneNumber());
    }

    private void addFieldWithLabel(String labelText, String placeholder) {
        JLabel label = new JLabel(labelText);
        JTextField textField = new JTextField(placeholder, 30);
        label.setAlignmentX(LEFT_ALIGNMENT);
        textField.setAlignmentX(LEFT_ALIGNMENT);
        add(label);
        add(textField);
        tfList.add(textField);
    }

    void showDialog(JComponent parent) {
        int result = JOptionPane.showConfirmDialog(parent, this, "Accept project data", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            pip.setProjectNumber(tfList.get(0).getText());
            pip.setProjectName(tfList.get(1).getText());
            pip.setConveyorType(tfList.get(2).getText());
            pip.setEngReleaseNumber(tfList.get(3).getText());
            pip.setReleaseDate(tfList.get(4).getText());
            pip.setUser(tfList.get(5).getText());
            pip.setPhoneNumber(tfList.get(6).getText());
            if (new CoverPageCreator(pip).generateCovers()) {
                JOptionPane.showMessageDialog(parent, "Done!");
            } else {
                JOptionPane.showMessageDialog(parent, "Error occurred during creating covers!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
