package com.example.demo.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Locale;

public class SimpleTestEntryPoint {
    private static StrDist.SearchBorder t(StrDistComplicated.SearchBorder border) {
        switch(border) {
            case StrDistComplicated.SearchBorder.ANYWHERE -> { return StrDist.SearchBorder.ANYWHERE; }
            case StrDistComplicated.SearchBorder.ROW -> { return StrDist.SearchBorder.ROW; }
            case StrDistComplicated.SearchBorder.WORD -> { return StrDist.SearchBorder.WORD; }
            case StrDistComplicated.SearchBorder.WHOLE_TEXT -> { return StrDist.SearchBorder.WHOLE_TEXT; }
        }
        return StrDist.SearchBorder.ANYWHERE;
    }

    public static String cc(String subStr, String superStr, StrDistComplicated.SearchBorder left, StrDistComplicated.SearchBorder right) {
        long start = System.nanoTime();
        String res = StrDist.calcStrDist(subStr, superStr, t(left), t(right), true).diffAsHtml;
        long end = System.nanoTime();
        return res.replace("<html>", "<td>")
                .replace("</html>", "</td>")
                .replace("(dist", "(time = " + ((end-start)/(1000*1000)) + "ms; dist");
    }

    public static String c(String subStr, String superStr, StrDistComplicated.SearchBorder left, StrDistComplicated.SearchBorder right, EnumSet<StrDistComplicated.MatchOption> options) {
        long start = System.nanoTime();
        try {
            String res = StrDistComplicated.calcStrDist(subStr, superStr, left, right, options).diffAsHtml;
            long end = System.nanoTime();
            return res.replace("<html>", "<td>")
                    .replace("</html>", "</td>")
                    .replace("(dist", "(time = " + ((end - start) / (1000 * 1000)) + "ms; dist");
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception occurred";
        }

    }

    public static String sameBordersDifferentOptionsTwo(String subStr, String superStr, StrDistComplicated.SearchBorder left, StrDistComplicated.SearchBorder right) {
        StringBuilder sb = new StringBuilder("<tr>\n");
        sb.append(cc(subStr, superStr, left, right));
        EnumSet<StrDistComplicated.MatchOption> options = EnumSet.of(StrDistComplicated.MatchOption.DO_RESTORE_PATH);
        sb.append(c(subStr, superStr, left, right, options));
        sb.append(cc(subStr, superStr, left, right));
        EnumSet<StrDistComplicated.MatchOption> options2 = options.clone();
        options2.add(StrDistComplicated.MatchOption.DO_DISCOUNT_STRINGS);
        sb.append(c(subStr, superStr, left, right, options2));
        options2 = options.clone();
        options2.add(StrDistComplicated.MatchOption.DO_DISCOUNT_REPEAT_INS);
        sb.append(c(subStr, superStr, left, right, options2));
        options2 = options.clone();
        options2.add(StrDistComplicated.MatchOption.DO_DISCOUNT_SYMBOL_DOUBLING);
        sb.append(c(subStr, superStr, left, right, options2));
        options2 = options.clone();
        options2.add(StrDistComplicated.MatchOption.DO_DISCOUNT_REPEAT_DEL);
        sb.append(c(subStr, superStr, left, right, options2));
        options2 = options.clone();
        options.add(StrDistComplicated.MatchOption.DO_DISCOUNT_REPEAT_INS);
        options.add(StrDistComplicated.MatchOption.DO_DISCOUNT_SYMBOL_DOUBLING);
        options.add(StrDistComplicated.MatchOption.DO_DISCOUNT_REPEAT_DEL);
        options.add(StrDistComplicated.MatchOption.DO_DISCOUNT_STRINGS);
        sb.append(c(subStr, superStr, left, right, options));
        sb.append("</tr>\n");
        return sb.toString();
    }

    public static String sameBordersDifferentOptions(String subStr, String superStr, StrDistComplicated.SearchBorder left, StrDistComplicated.SearchBorder right) {
        return sameBordersDifferentOptionsTwo(subStr, superStr, left, right) +
                sameBordersDifferentOptionsTwo(subStr.toUpperCase(Locale.ROOT), superStr.toUpperCase(Locale.ROOT), left, right);
    }
    public static void main(String[] args) throws IOException {
//        String subStr = "студент";
//        String superStr = "піб студента";
        String subStr = Files.readString(Path.of("sub.txt"));
        String superStr = Files.readString(Path.of("super.txt"));
        StringBuilder sb = new StringBuilder("<html>\n<table border=2px>\n");
        sb.append(sameBordersDifferentOptions(subStr, superStr, StrDistComplicated.SearchBorder.WHOLE_TEXT, StrDistComplicated.SearchBorder.WHOLE_TEXT));
        sb.append(sameBordersDifferentOptions(subStr, superStr, StrDistComplicated.SearchBorder.ROW, StrDistComplicated.SearchBorder.ROW));
        sb.append(sameBordersDifferentOptions(subStr, superStr, StrDistComplicated.SearchBorder.WORD, StrDistComplicated.SearchBorder.ROW));
        sb.append(sameBordersDifferentOptions(subStr, superStr, StrDistComplicated.SearchBorder.ROW, StrDistComplicated.SearchBorder.WORD));
        sb.append(sameBordersDifferentOptions(subStr, superStr, StrDistComplicated.SearchBorder.WORD, StrDistComplicated.SearchBorder.WORD));
        sb.append(sameBordersDifferentOptions(subStr, superStr, StrDistComplicated.SearchBorder.ANYWHERE, StrDistComplicated.SearchBorder.ANYWHERE));
        sb.append("</table>\n");
        sb.append("<style>\n\t.good {\n\t\tcolor: green;\n\t\tfont-weight: bold;\n\t}\n</style>\n");
        sb.append("<style>\n\t.skip {\n\t\tcolor: orange;\n\t\ttext-decoration: underline;\n\t}\n</style>\n");
        sb.append("<style>\n\t.ins {\n\t\tcolor: red;\n\t\ttext-decoration: line-through;\n\t}\n</style>\n</html>");
        Files.writeString(Path.of("cmp.html"), sb.toString());
    }
}
