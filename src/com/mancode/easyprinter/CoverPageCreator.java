package com.mancode.easyprinter;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Michał Dominiczak
 * on 01.03.2017
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */

public class CoverPageCreator {

    private static final String pathTemplate = "res/template.docx";
    private final List<String> headers = new ArrayList<>(Arrays.asList(
            "ASSEMBLY ME - MP",
            "MANUFACTURING ME - PU"
    ));
    private final List<String> filenames = new ArrayList<>(Arrays.asList(
            "Documentation Release - Assembly.docx",
            "Documentation Release - Manufacturing.docx"
    ));
    private ProjectInfoParser info;
    private String inputPath;

    CoverPageCreator(String path) {
        this.inputPath = path;
        info = new ProjectInfoParser(path);
    }

    CoverPageCreator(ProjectInfoParser pip) {
        this.info = pip;
        inputPath = pip.getPathToParse();
    }

    boolean generateCovers() {
        boolean result = false;

        for (int i = 0; i < headers.size(); i++) {
            try {
                XWPFDocument doc = new XWPFDocument(OPCPackage.open(getClass().getResourceAsStream(pathTemplate)));
                for (XWPFParagraph p : doc.getParagraphs()) {
                    List<XWPFRun> runs = p.getRuns();
                    if (runs != null) {
                        for (XWPFRun r : runs) {
                            String text = r.getText(0);
                            if (text != null) {
                                if (text.contains("${Destination}")) {
                                    String h = headers.get(i);
                                    text = text.replace("${Destination}", h);
                                    r.setText(text, 0);
                                } else if (text.contains("${ProjectName}")) {
                                    text = text.replace("${ProjectName}", info.getProjectName());
                                    r.setText(text, 0);
                                } else if (text.contains("${ProjectNumber}")) {
                                    text = text.replace("${ProjectNumber}", info.getProjectNumber());
                                    r.setText(text, 0);
                                } else if (text.contains("${ConveyorType}")) {
                                    text = text.replace("${ConveyorType}", info.getConveyorType());
                                    r.setText(text, 0);
                                } else if (text.contains("${EngReleaseNumber}")) {
                                    text = text.replace("${EngReleaseNumber}", info.getEngReleaseNumber());
                                    r.setText(text, 0);
                                } else if (text.contains("${UserName}")) {
                                    text = text.replace("${UserName}", info.getUser());
                                    r.setText(text, 0);
                                } else if (text.contains("${PhoneNumber}")) {
                                    text = text.replace("${PhoneNumber}", info.getPhoneNumber());
                                    r.setText(text, 0);
                                } else if (text.contains("${ReleaseDate}")) {
                                    text = text.replace("${ReleaseDate}", info.getReleaseDate());
                                    r.setText(text, 0);
                                }
                            }
                        }
                    }
                }
                doc.write(new FileOutputStream(inputPath + "\\" + filenames.get(i)));
                result = true;
            } catch (IOException | InvalidFormatException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

}
