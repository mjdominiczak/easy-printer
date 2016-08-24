package com.mancode.easyprinter;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.extractor.ExcelExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
//    private List<Integer> referenceList;
    private List<FileSignature> referenceList;

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
            referenceList = new ArrayList<>();
            switch (variant) {
                case 0:
                    OPCPackage pkg = OPCPackage.open(engineeringReleaseFile);
                    XSSFWorkbook wb = new XSSFWorkbook(pkg);
                    ExcelExtractor extractor = new XSSFExcelExtractor(wb);
                    Scanner scanner = new Scanner(extractor.getText());
                    scanner.next();
                    while (scanner.hasNext()) {
                        int nextInt = scanner.nextInt();
                        FileSignature tmp = new FileSignature(nextInt, "", false);
                        if (!referenceList.contains(tmp)) {
                            referenceList.add(tmp);
                        }
                    }
                    pkg.close();
                    break;

                case 1:
                    Workbook workbook = WorkbookFactory.create(engineeringReleaseFile);
                    boolean isXLSX = workbook instanceof XSSFWorkbook;
                    Sheet sheet = workbook.getSheetAt(0);
                    int headerRowNo = -1;
                    int drawingColNo = -1;
                    int bomColNo = -1;
                    int descriptionColNo = -1;
                    int yellowColNo = -1;
                    searchLoop: {
                        for (Row row : sheet) {
                            for (Cell cell : row) {
                                if (drawingColNo < 0
                                        || bomColNo < 0
                                        || descriptionColNo < 0
                                        || yellowColNo < 0) {
                                    if (drawingColNo < 0 && checkStringValue(cell, "Draw. ")) {
                                        drawingColNo = cell.getColumnIndex();
                                        headerRowNo = cell.getRowIndex();
                                    }
                                    if (descriptionColNo < 0 && checkStringValue(cell, "Description"))
                                        descriptionColNo = cell.getColumnIndex();
                                    if (bomColNo < 0 && checkStringValue(cell, "BOM"))
                                        bomColNo = cell.getColumnIndex();

                                    //finds the first YELLOW cell
//                                    if (yellowColNo < 0 && checkFillColor(cell, "FFFFFF00"))
                                    //finds the first FILLED and NOT WHITE cell
                                    if (yellowColNo < 0
                                            && cell.getCellStyle().getFillForegroundColorColor() != null
                                            && checkFillColor(cell, new short[]{255, 255, 0}, isXLSX))
                                        yellowColNo = cell.getColumnIndex();
                                } else {
                                    break searchLoop;
                                }
                            }
                        }
                    }
                    if (headerRowNo < 0 || drawingColNo < 0 || descriptionColNo < 0 || yellowColNo < 0) {
                        System.err.println("Yellow formatted cell or \"Draw. No.:\" column or \"Description\" column not found!");
                        break;
                    }
                    for (int i = headerRowNo + 1; i < sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;
                        Cell checkCell = row.getCell(yellowColNo);
                        Cell descriptionCell = row.getCell(descriptionColNo);
                        Cell bomCell = row.getCell(bomColNo);
                        boolean transferToReferenceList = false;
                        boolean hasBOM = false;
                        if (checkCell != null && descriptionCell != null && bomCell != null) {
                            switch (checkCell.getCellType()) {
                                case Cell.CELL_TYPE_STRING:
                                    if (checkCell.getStringCellValue().toLowerCase().trim().equals("x")) {
                                        Pattern pattern = Pattern.compile(".*general.*list.*");
                                        Matcher matcher = pattern.matcher(descriptionCell.getStringCellValue().toLowerCase());
                                        transferToReferenceList = !matcher.lookingAt();
                                        if (bomCell.getCellType() == Cell.CELL_TYPE_STRING) {
                                            if (!bomCell.getStringCellValue().equals("")) hasBOM = true;
                                        }
                                    }
                                    break;
                                case Cell.CELL_TYPE_NUMERIC:
                                    if (checkCell.getNumericCellValue() > 0) {
                                        transferToReferenceList = true;
                                        if (bomCell.getCellType() == Cell.CELL_TYPE_STRING) {
                                            if (!bomCell.getStringCellValue().equals("")) hasBOM = true;
                                        }
                                    }
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
                            FileSignature tmp = new FileSignature(drawingNo, "", hasBOM);
                            if (!referenceList.contains(tmp)) {
                                referenceList.add(tmp);
                                if (hasBOM) {
                                    referenceList.add(new FileSignature(drawingNo, "", false));
                                }
                            }
                        }
                    }
                    System.err.println("---INFORMATION---");
                    System.out.println("Reference list from ER:");
                    referenceList.forEach(System.out::println);
                    System.out.println("===========");
                    break;
            }
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            System.err.println("InvalidFormatException when trying to load ER file: " + engineeringReleaseFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException when opening workbook from file: " + engineeringReleaseFile.getPath());
        }
    }

    private boolean checkStringValue(Cell cell, String text) {
        return cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().contains(text);
    }

    private boolean checkFillColor(Cell cell, short[] rgbColor, boolean isXLSX) {
        short[] rgb;
        if (isXLSX) {
            XSSFColor color = XSSFColor.toXSSFColor(cell.getCellStyle().getFillForegroundColorColor());
            if (color == null) return false;
            rgb = new short[3];
            for (int i = 0; i < color.getRGB().length; i++) {
                rgb[i] = (short)(color.getRGB()[i] & 0xFF);
            }
        } else {
            HSSFColor color = HSSFColor.toHSSFColor(cell.getCellStyle().getFillForegroundColorColor());
            if (color == null) return false;
            rgb = color.getTriplet();
        }
        return Arrays.equals(rgb, rgbColor);
    }

    List<FileSignature> getReferenceList() {
        return referenceList;
    }

    /**
     * Comparator class for sorting list according to referenceList order
     */
    class ERComparator implements Comparator<CustomFile> {

        @Override
        public int compare(CustomFile file1, CustomFile file2) {
            int index1 = referenceList.indexOf(file1.getSignature());
            int index2 = referenceList.indexOf(file2.getSignature());
            if ((index1 == -1 && index2 == -1) || index1 == index2) {
                return file1.compareTo(file2);
            } else if (index1 == -1) {
                return 1;
            } else if (index2 == -1) {
                return -1;
            } else {
                return index1 - index2;
            }
        }
    }
}
