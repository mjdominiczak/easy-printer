package com.mancode.easyprinter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Michał Dominiczak
 * on 06.02.2017
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */

class ProjectInfoParser {

    private final String projectNumberPattern = "[a-zA-Z]{2}[0-9]{2}-[0-9]{7}";
    private final String projectNamePattern1 = "Projects\\\\[A-Z]{3}\\\\([\\s\\w\\\\-]*)\\\\[a-zA-Z]{2}[0-9]{2}-[0-9]{7}\\\\*";
    private final String projectNamePattern2 = projectNumberPattern + ".([\\s\\w\\-]*)\\\\*";
    private final String conveyorTypePattern = "\\D([1-9]\\d{2})\\D";
    private final String engReleaseNumberPattern = "\\D(0\\d{2})\\D?";
    private String pathToParse;
    private Properties phoneBook;
    private String projectNumber = "";
    private String projectName = "";
    private String conveyorType = "";
    private String engReleaseNumber = "";
    private String releaseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    private String user = System.getProperty("user.name");
    private String phoneNumber = "";

    ProjectInfoParser(String path) {
        pathToParse = path;
        parse();
        loadPhoneBook();
        phoneNumber = phoneBook.getProperty(user);
    }

    private void parse() {
        Pattern pattern;
        Matcher matcher;

        if (projectNumber.equals("")) {
            pattern = Pattern.compile(projectNumberPattern);
            matcher = pattern.matcher(pathToParse);
            if (matcher.find()) projectNumber = matcher.group();
        }

        if (projectName.equals("")) {
            pattern = Pattern.compile(projectNamePattern1);
            matcher = pattern.matcher(pathToParse);
            if (matcher.find()) projectName = matcher.group(1).replaceAll("[_\\\\]"," ");
            else {
                pattern = Pattern.compile(projectNamePattern2);
                matcher = pattern.matcher(pathToParse);
                if (matcher.find()) projectName = matcher.group(1);
            }
        }

        if (conveyorType.equals("")) {
            pattern = Pattern.compile(conveyorTypePattern);
            matcher = pattern.matcher(pathToParse);
            if (matcher.find()) conveyorType = matcher.group(1);
        }

        if (engReleaseNumber.equals("")) {
            pattern = Pattern.compile(engReleaseNumberPattern);
            matcher = pattern.matcher(pathToParse);
            if (matcher.find()) engReleaseNumber = matcher.group(1);
        }
    }

    private void loadPhoneBook() {
        phoneBook = new Properties();
        try {
            phoneBook.load(getClass().getResourceAsStream("res/phoneNumbers.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getPathToParse() {
        return pathToParse;
    }

    String getProjectNumber() {
        return projectNumber;
    }

    void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    String getProjectName() {
        return projectName;
    }

    void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    String getConveyorType() {
        return conveyorType;
    }

    void setConveyorType(String conveyorType) {
        this.conveyorType = conveyorType;
    }

    String getEngReleaseNumber() {
        return engReleaseNumber;
    }

    void setEngReleaseNumber(String engReleaseNumber) {
        this.engReleaseNumber = engReleaseNumber;
    }

    String getReleaseDate() {
        return releaseDate;
    }

    void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    String getUser() {
        return user;
    }

    void setUser(String user) {
        this.user = user;
    }

    String getPhoneNumber() {
        return phoneNumber;
    }

    void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
