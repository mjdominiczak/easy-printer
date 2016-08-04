package com.mancode.easyprinter;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.extractor.ExcelExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Michał Dominiczak
 * on 24.07.2016
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */

class ERProcessor {

    private File engineeringReleaseFile;

    /**
     * List of drawing numbers extracted from Engineering Release
     * - duplicates removed (unique drawings)
     * - contains only drawing numbers (500xxxxxx)
     */
    private List<Integer> referenceList;

    ERProcessor(File engineeringReleaseFile) {
        this.engineeringReleaseFile = engineeringReleaseFile;
        extractOrderFromER(1);
    }

    /**
     * Method for extracting reference list of drawing numbers from ER
     * @param variant number representing method variant
     *                0 - raw file containing simple list in column A starting from cell A1
     *                1 - proper Engineering Release acc. to Duerr Standard (drawing numbers contained in
     *                  a column named "Draw. No.:" , current release column marked with yellow fill and "x" or positive integer)
     */
    private void extractOrderFromER(int variant) {
        try {
            OPCPackage pkg = OPCPackage.open(engineeringReleaseFile);
            XSSFWorkbook wb = new XSSFWorkbook(pkg);
            referenceList = new ArrayList<>();
            switch (variant) {
                case 0:
                    ExcelExtractor extractor = new XSSFExcelExtractor(wb);
                    Scanner scanner = new Scanner(extractor.getText());
                    scanner.next();
                    while (scanner.hasNext()) {
                        int nextInt = scanner.nextInt();
                        if (!referenceList.contains(nextInt)) {
                            referenceList.add(nextInt);
                        }
                    }
                    break;

                case 1:
                    XSSFSheet sheet = wb.getSheetAt(0);
                    int headerRowNo = -1;
                    int drawingColNo = -1;
                    int yellowColNo = -1;
                    searchLoop: {
                        for (Row row : sheet) {
                            for (Cell cell : row) {
                                if (drawingColNo < 0 || yellowColNo < 0) {
                                    if (drawingColNo < 0 && checkStringValue(cell, "Draw. ")) {
                                        drawingColNo = cell.getColumnIndex();
                                        headerRowNo = cell.getRowIndex();
                                    }
                                    if (yellowColNo < 0 && checkFillColor(cell, "FFFFFF00"))
                                        yellowColNo = cell.getColumnIndex();
                                } else {
                                    break searchLoop;
                                }
                            }
                        }
                    }
                    if (headerRowNo < 0 || drawingColNo < 0 || yellowColNo < 0) {
                        System.err.println("Yellow formatted cell or \"Draw. No.:\" column not found!");
                        break;
                    }
                    for (int i = headerRowNo + 1; i < sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        Cell checkCell = row.getCell(yellowColNo);
                        boolean transferToReferenceList = false;
                        if (checkCell != null) {
                            switch (checkCell.getCellType()) {
                                case Cell.CELL_TYPE_STRING:
                                    if (checkCell.getStringCellValue().toLowerCase().trim().equals("x"))
                                        transferToReferenceList = true;
                                    break;
                                case Cell.CELL_TYPE_NUMERIC:
                                    if (checkCell.getNumericCellValue() > 0)
                                        transferToReferenceList = true;
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (transferToReferenceList) {
                            Cell numberCell = row.getCell(drawingColNo);
                            int drawingNo = 0;
                            switch (numberCell.getCellType()) {
                                case Cell.CELL_TYPE_STRING:
                                    drawingNo = Integer.parseInt(numberCell.getStringCellValue());
                                    break;
                                case Cell.CELL_TYPE_NUMERIC:
                                    drawingNo = (int) numberCell.getNumericCellValue();
                                    break;
                                default:
                                    break;
                            }
                            if (!referenceList.contains(drawingNo)) {
                                referenceList.add(drawingNo);
                            }
                        }
                    }
                    System.out.println("===========");
                    System.out.println("Reference list from ER:");
                    referenceList.forEach(System.out::println);
                    System.out.println("===========");
                    break;
            }
            pkg.close();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            System.err.println("InvalidFormatException when trying to load ER file: " + engineeringReleaseFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException when opening workbook from file: " + engineeringReleaseFile.getPath());
        }
    }

    private boolean checkStringValue(Cell cell, String text) {
        return cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().startsWith(text);
    }

    private boolean checkFillColor(Cell cell, String argbHexColor) {
        XSSFColor color = XSSFColor.toXSSFColor(cell.getCellStyle().getFillForegroundColorColor());
        return color != null && color.getARGBHex().equals(argbHexColor);
    }

    public List<Integer> getReferenceList() {
        return referenceList;
    }

    /**
     * Comparator class for sorting list according to referenceList order
     */
    class ERComparator implements Comparator<CustomFile> {

        @Override
        public int compare(CustomFile file1, CustomFile file2) {
            int index1 = referenceList.indexOf(file1.getDrawingNumber());
            int index2 = referenceList.indexOf(file2.getDrawingNumber());
            if (index1 == -1) {
                System.err.println(file1.getName() + " not found in ER!");
                return file1.compareTo(file2);
            } else if (index2 == -1) {
                System.err.println(file2.getName() + " not found in ER!");
                return file1.compareTo(file2);
            }
            int result = index1 - index2;
            if (result == 0) {
                return file1.compareTo(file2);
            }
            return result;
        }
    }
}
