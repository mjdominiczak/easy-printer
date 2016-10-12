package com.mancode.easyprinter;

import java.util.List;

/**
 * Created by Michał Dominiczak
 * on 20.07.2016
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */

class Test {

    private static final String file = "C:\\Users\\Manveru\\Desktop\\test\\ER test.xlsx";
    private static List<Integer> order;

//    public static void main(String[] args) throws Exception {
//        OPCPackage pkg = OPCPackage.open(file);
//        XSSFWorkbook wb = new XSSFWorkbook(pkg);
//        ExcelExtractor extractor = new XSSFExcelExtractor(wb);
//
//        Scanner scanner = new Scanner(extractor.getText());
//        order = new ArrayList<>();
//        scanner.next();
//        while (scanner.hasNext()) {
//            order.add(scanner.nextInt());
//        }
//        System.out.println(order);
//        System.out.println();
//
//        List<Integer> list = new ArrayList<>();
//        list.add(500113774);
//        list.add(500284771);
//        list.add(500083340);
//        list.add(500114100);
//        list.add(500083341);
//
//        System.out.println(list);
//
//        Collections.sort(list, (file1, file2) -> {
//            int index1 = order.indexOf(file1);
//            int index2 = order.indexOf(file2);
//            if (index1 == -1) {
//                throw new IllegalArgumentException(file1 + " not found in ER!");
//            } else if (index2 == -1) {
//                throw new IllegalArgumentException(file2 + " not found in ER!");
//            }
//            int result = index1 - index2;
//            if (result == 0) {
//                return file1.compareTo(file2);
//            }
//            return result;
//        });
//
//        System.out.println(list);
//
//        pkg.close();
//    }
}
