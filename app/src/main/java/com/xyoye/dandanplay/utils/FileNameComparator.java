package com.xyoye.dandanplay.utils;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FileNameComparator<T> implements Comparator<T> {
    private boolean desc = false;

    public FileNameComparator() {
    }

    public FileNameComparator(boolean desc) {
        this.desc = desc;
    }

    @Override
    public int compare(T o1, T o2) {
        String str1 = getCompareValue(o1);
        String str2 = getCompareValue(o2);
        if (desc){
            return getCompareResult(str2, str1);
        }
        return getCompareResult(str1, str2);
    }

    private int getCompareResult(String o1, String o2){
        if (o1 == null) return -1;
        if (o2 == null) return 1;

        Pattern pattern = Pattern.compile("\\d+$");
        Matcher matcher1 = pattern.matcher(o1);
        Matcher matcher2 = pattern.matcher(o2);

        if (matcher1.find() && matcher2.find()) {
            int index1 = matcher1.start();
            int index2 = matcher2.start();

            String text1 = o1.substring(0, index1);
            String number1 = o1.substring(index1);

            String text2 = o2.substring(0, index2);
            String number2 = o2.substring(index2);
            if (text1.equals(text2)) {
                return Integer.valueOf(number1).compareTo(Integer.valueOf(number2));
            }
        }
        return o1.compareTo(o2);
    }

    public abstract String getCompareValue(T t);
}