package com.mancode.easyprinter;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Michał Dominiczak
 * on 03.08.2016
 * e-mail: michal-dominiczak@o2.pl
 * Copyright reserved
 */

public class MyOutputStream extends OutputStream {

    private JTextArea textArea;

    public MyOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        textArea.append(String.valueOf((char)b));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
