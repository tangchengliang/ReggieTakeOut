package com.tcl.reggie.common;

import com.fasterxml.jackson.databind.JsonSerializable;

public class Test {
    public static void main(String[] args) {
        int n=703;
        for (int i = 1; i <=n; i++) {
            String data = String.valueOf(i)+"-"+ExcelColumnFromNumber(i);
            System.out.println(data);
        }
    }
    public static String ExcelColumnFromNumber(int column) {
        String columnString = "";
        int columnNumber = column;
        while (columnNumber > 0) {
            int currentLetterNumber = (columnNumber - 1) % 26;
            char currentLetter = (char) (currentLetterNumber + 65);
            columnString = currentLetter + columnString;
            columnNumber = (columnNumber - (currentLetterNumber + 1)) / 26;
        }
        return columnString;
    }
}



