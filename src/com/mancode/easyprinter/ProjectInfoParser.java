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

public class ProjectInfoParser {

    private final String projectNumberPattern = "[a-zA-Z]{2}[0-9]{2}-[0-9]{7}";
    private final String projectNamePattern1 = "Projects\\\\[A-Z]{3}\\\\([\\s\\w\\\\-]*)\\\\[a-zA-Z]{2}[0-9]{2}-[0-9]{7}\\\\*";
    private final String projectNamePattern2 = projectNumberPattern + ".([\\s\\w\\-]*)\\\\*";
    private final String conveyorTypePattern = "\\D([1-9]\\d{2})\\D";
    private final String engReleaseNumberPattern = "\\D(0\\d{2})\\D?";
    private String stringToParse;
    private Properties phoneBook;
    private String projectNumber = "";
    private String projectName = "";
    private String conveyorType = "";
    private String engReleaseNumber = "";
    private String releaseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    private String user = System.getProperty("user.name");
    private String phoneNumber = "";

    public ProjectInfoParser(String str) {
        stringToParse = str;
        parse();
        loadPhoneBook();
        phoneNumber = phoneBook.getProperty(user);
    }

    private void parse() {
        Pattern pattern;
        Matcher matcher;

        if (projectNumber.equals("")) {
            pattern = Pattern.compile(projectNumberPattern);
            matcher = pattern.matcher(stringToParse);
            if (matcher.find()) projectNumber = matcher.group();
        }

        if (projectName.equals("")) {
            pattern = Pattern.compile(projectNamePattern1);
            matcher = pattern.matcher(stringToParse);
            if (matcher.find()) projectName = matcher.group(1).replaceAll("[_\\\\]"," ");
            else {
                pattern = Pattern.compile(projectNamePattern2);
                matcher = pattern.matcher(stringToParse);
                if (matcher.find()) projectName = matcher.group(1);
            }
        }

        if (conveyorType.equals("")) {
            pattern = Pattern.compile(conveyorTypePattern);
            matcher = pattern.matcher(stringToParse);
            if (matcher.find()) conveyorType = matcher.group(1);
        }

        if (engReleaseNumber.equals("")) {
            pattern = Pattern.compile(engReleaseNumberPattern);
            matcher = pattern.matcher(stringToParse);
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

    public String getProjectNumber() {
        return projectNumber;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getConveyorType() {
        return conveyorType;
    }

    public String getEngReleaseNumber() {
        return engReleaseNumber;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getUser() {
        return user;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
