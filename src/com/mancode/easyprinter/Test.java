package com.mancode.easyprinter;

/**
 * Created by Michał Dominiczak
 * on 20.07.2016
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */

class Test {

    private static final String path = "c:\\Users\\Manveru\\Desktop\\test\\Projects\\PFS\\Jaguar\\Nitra\\PL01-2102027\\08_Engineering\\11_Conveyors\\19_Manufacturing\\KB_MAWI\\723\\160927_723_002\\";

    public static void main(String[] args) {

        CoverPageCreator cpc = new CoverPageCreator(path);
        cpc.generateCovers();

    }

}
