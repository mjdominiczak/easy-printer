package com.mancode.easyprinter;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private List<FileSignature> referenceList;

    ERProcessor(File engineeringReleaseFile, int variant) {
        this.engineeringReleaseFile = engineeringReleaseFile;
        extractOrderFromER(variant);
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
                    POITextExtractor extractor = ExtractorFactory.createExtractor(engineeringReleaseFile);
                    Scanner scanner = new Scanner(extractor.getText());
                    scanner.next();
                    while (scanner.hasNext()) {
                        int nextInt = scanner.nextInt();
                        FileSignature tmp = new FileSignature(nextInt, "", false);
                        if (!referenceList.contains(tmp)) {
                            referenceList.add(tmp);
                        }
                    }
                    break;

                case 1:
                    Workbook workbook = WorkbookFactory.create(engineeringReleaseFile, null, true);
                    boolean isXLSX = workbook instanceof XSSFWorkbook;
                    Sheet sheet = workbook.getSheetAt(0);
                    int headerRowNo = -1;
                    int drawingColNo = -1;
                    int bomColNo = -1;
                    int descriptionColNo = -1;
                    List<Integer> colorColList = new ArrayList<>();
                    boolean searchCompleted = false;
                    searchLoop: {
                        for (Row row : sheet) {
                            for (Cell cell : row) {
                                if (drawingColNo < 0 && checkStringValue(cell, "Draw. ")) {
                                    drawingColNo = cell.getColumnIndex();
                                    headerRowNo = cell.getRowIndex();
                                }
                                if (descriptionColNo < 0 && checkStringValue(cell, "Description"))
                                    descriptionColNo = cell.getColumnIndex();
                                if (bomColNo < 0 && checkStringValue(cell, "BOM"))
                                    bomColNo = cell.getColumnIndex();

                                //finds the first YELLOW cell and all other YELLOW cells in the same row
                                Color color = cell.getCellStyle().getFillForegroundColorColor();
                                if ((isXLSX ? color != null : !(HSSFColor.toHSSFColor(color) instanceof HSSFColor.AUTOMATIC))
                                        && color != null
                                        && checkFillColor(cell, new short[]{255, 255, 0}, isXLSX))
                                    colorColList.add(cell.getColumnIndex());
                            }
                            searchCompleted = drawingColNo >= 0
                                    && bomColNo >= 0
                                    && descriptionColNo >= 0
                                    && !colorColList.isEmpty();
                            if (searchCompleted) {
                                break searchLoop;
                            }
                        }
                    }
                    if (!searchCompleted) {
                        System.err.println("Cell with yellow fill or \"Draw. No.:\" column or \"Description\" column not found!");
                        break;
                    }
                    for (int i = headerRowNo + 1; i < sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;
                        boolean transferToReferenceList = false;
                        boolean hasBOM = false;
                        boolean isGL = false;
                        Cell descriptionCell = row.getCell(descriptionColNo);
                        Cell bomCell = row.getCell(bomColNo);
                        int quantity = 0;
                        List<Cell> checkCells = colorColList.stream().map(row::getCell).collect(Collectors.toList());
                        if (bomCell != null && bomCell.getCellType() == Cell.CELL_TYPE_STRING) {
                            if (!bomCell.getStringCellValue().equals("")) hasBOM = true;
                        }
                        if (descriptionCell != null) {
                            Pattern pattern = Pattern.compile(".*general.*list.*");
                            Matcher matcher = pattern.matcher(descriptionCell.getStringCellValue().toLowerCase());
                            isGL = matcher.lookingAt();
                        }
                        for (Cell checkCell : checkCells) {
                            if (checkCell != null && !isGL) {
                                switch (checkCell.getCellType()) {
                                    case Cell.CELL_TYPE_STRING:
                                        String trimmedValue = checkCell.getStringCellValue().toLowerCase().trim();
                                        if (trimmedValue.equals("x"))
                                            transferToReferenceList = true;
                                        else if (trimmedValue.equals("-x") && transferToReferenceList)
                                            transferToReferenceList = false;
                                        break;
                                    case Cell.CELL_TYPE_NUMERIC:
                                        quantity += (int)checkCell.getNumericCellValue();
                                        transferToReferenceList = quantity > 0;
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        if (transferToReferenceList) {
                            Cell numberCell = row.getCell(drawingColNo);
                            int drawingNo = 0;
                            switch (numberCell.getCellType()) {
                                case Cell.CELL_TYPE_STRING:
                                    String stringValue = numberCell.getStringCellValue().trim();
                                    try {
                                        drawingNo = Integer.parseInt(stringValue);
                                    } catch (NumberFormatException e) {
                                        Pattern pattern = Pattern.compile("([0-9]{2})k([0-9]*).*");
                                        Matcher matcher = pattern.matcher(stringValue.trim().toLowerCase());
                                        if (matcher.lookingAt()) {
                                            String convertedString = matcher.group(1) + "0" + matcher.group(2);
                                            drawingNo = Integer.parseInt(convertedString);
                                        } else {
                                            System.err.println("Cannot parse drawing number from the cell " + numberCell.getAddress().toString());
                                            e.printStackTrace();
                                        }
                                    }
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
                    break;
            }
            System.err.println("---INFORMATION---");
            System.out.println("Reference list from ER:");
            referenceList.forEach(System.out::println);
            System.out.println("===========");
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            System.err.println("InvalidFormatException when trying to load ER file: " + engineeringReleaseFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException when opening workbook from file: " + engineeringReleaseFile.getPath());
        } catch (XmlException e) {
            e.printStackTrace();
            System.err.println("XmlException when creating extractor from file: " + engineeringReleaseFile.getPath());
        } catch (OpenXML4JException e) {
            e.printStackTrace();
            System.err.println("OpenXML4JException when creating extractor from file: " + engineeringReleaseFile.getPath());
        }
    }

    private boolean checkStringValue(Cell cell, String text) {
        return cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().contains(text);
    }

    private boolean checkFillColor(Cell cell, short[] rgbColor, boolean isXLSX) {
        short[] rgb;
        if (isXLSX) {
            XSSFColor color = XSSFColor.toXSSFColor(cell.getCellStyle().getFillForegroundColorColor());
            if (color == null || color.isIndexed()) return false;
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
            FileSignature fs1 = new FileSignature(file1.getSignature());
            FileSignature fs2 = new FileSignature(file2.getSignature());
            fs1.resetSheet();
            fs2.resetSheet();
            int index1 = referenceList.indexOf(fs1);
            int index2 = referenceList.indexOf(fs2);
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
