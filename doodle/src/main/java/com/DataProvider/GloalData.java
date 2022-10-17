package com.DataProvider;

public class GloalData {
    public GloalData() {
        System.out.println("hello");
    }
    private static boolean isEnglish = false;//默认是中文
    public static boolean getLanguage() {
        return isEnglish;
    }
    public static void setLanguage(boolean flag) {
        isEnglish=flag;
    }
}
