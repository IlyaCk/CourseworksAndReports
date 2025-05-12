package com.example.demo.utils;

import java.io.IOException;
import java.util.*;

/**
 * @author IlyaCk a.k.a. Ilya Porublyov
 * The preferred way to use is via one of methods
 * Direct using of @calcStrDist allowed too, but may seem more complicated
 */
public class StrDistComplicated {

    public enum MatchLevel {
        NOT_MATCHED,
        LOW,
        MEDIUM,
        HIGH;

        public boolean betterOrEqual(MatchLevel that) {
            switch (this) {
                case HIGH -> {
                    return true;
                }
                case MEDIUM -> {
                    return that != HIGH;
                }
                case LOW -> {
                    return that == LOW || that == NOT_MATCHED;
                }
                case NOT_MATCHED -> {
                    return that == NOT_MATCHED;
                }
            }
            System.err.println("bad case in MatchLevel.betterOrEqual");
            return false;
        }
    }

    /**
     * DO_RESTORE_PATH -- if set, diffAsHtml is built; requires more memory&time comparing to this flag isn't set, but grow is NOT asymptotic
     * DO_EXTRA_DEBUG_OUTPUT -- obvious from name, debug output is in System.out; commonly, this option shouldn't be passed by client code
     * DO_DISCOUNT_SYMBOL_DOUBLING -- if set, misprinting two similar symbols instead one and/or only one instead two similar
     * is cheap, i.e. increases dist less, than other inserts or dels.
     * When DO_DISCOUNT_REPEAT_DEL or DO_DISCOUNT_REPEAT_INSERT isN'T set, only 2<->1 are discounted.
     * DO_DISCOUNT_STRINGS -- if set, tries to decrease positive penalties for replacing of continuous substrings
     * and decrease dist when long continuous substrings do match.
     * This and only this option can make distance negative.
     * Increases search's time (but not memory); commonly on natural texts this increase isn't huge,
     * but can become large when superstring contains a lot of common continuous substrings with substring being searched
     * DO_DISCOUNT_REPEAT_DEL -- makes relatively cheaper (i.e. value added to dist is relatively less)
     * situations when *continuous* fragment of superstring is absent in substring
     * DO_DISCOUNT_REPEAT_INS -- makes relatively cheaper (i.e. value added to dist is relatively less)
     * situations when *continuous* fragment of substring is absent in superstring
     * In current implementation, when multiple discounts are set and can be used in some situation,
     * it's guaranteed that result is less-or-equal to each discount separately,
     * but currently there is no warranty if different discounts are combined or just used separately (with choosing of minimal result)
     */
    public enum MatchOption {
        DO_RESTORE_PATH,
        DO_EXTRA_DEBUG_OUTPUT,
        DO_DISCOUNT_SYMBOL_DOUBLING,
        DO_DISCOUNT_STRINGS,
        DO_DISCOUNT_REPEAT_DEL,
        DO_DISCOUNT_REPEAT_INS
    }

    /**
     * WHOLE_TEXT -- no substr allowed, compare with whole text only
     * ROW -- substring allowed, but should begin/end at line breaks only; substring CAN match multiple rows
     * WORD -- substring allowed, but should begin/end at begin/end of words only; substring CAN match multiple words/rows
     * ANYWHERE -- any substring, even with begin/end inside words; substring CAN match multiple words/rows
     */
    public enum SearchBorder {
        WHOLE_TEXT,
        ROW,
        WORD,
        ANYWHERE
    }

    enum KindOfEdit {
        REPLACE_OR_COPY,
        DEL,
        INS,
        SWAP
    }

    /**
     * @param kind @see KindOfEdit
     * @param num  How many chars are replaced/copied/inserted/skipped at the step.
     */
    record Step(KindOfEdit kind, int num) {
    }

    final static String SPACES = "\u0020_\u00A0\u1680\u180E" +
            "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000\uFEFF";
    final static String LINE_BREAKS = "\r\n\f\u000B\u001C\u001D\u001E\u001F\u2028\u2029";
    final static String APOSTROPHES = "'\u2018\u2019\u02BC\u02BB\u02C8\u275B\u275C\uFF07";
    final static String QUOTES_OPEN = "\"\u201C\u00AB\u2039\u275D\u301D\u301F\uFF02";
    final static String QUOTES_CLOSE = "\"\u201D\u00BB\u203A\u275E\u301E\uFF02";
    final static String HYPHENS = "-\u2010\u2011\uFE63\uFF0D";
    final static String DASHES = "\u2012\u2013\u2014\u2015\u2212\uFE58";
    final static String DOTS = ".\u2024\uFE52\uFF0E";
    final static String CYRII_UPPER = "IІ"; // cyrillic and latin
    final static String CYRII_LOWER = "iі"; // cyrillic and latin
    final static String CYRG_UPPER = "ГҐ";
    final static String CYRG_LOWER = "гґ";

    private static int discountDelFunc(int sum) {
        if (sum <= COMMON_DIFF)
            return sum;
        else
            return (int) (COMMON_DIFF * Math.pow((sum + 1e-6) / COMMON_DIFF, 0.875));
    }

    private static int discountInsFunc(int sum) {
        if (sum <= COMMON_DIFF)
            return sum;
        else
            return (int) (COMMON_DIFF * Math.pow((sum + 1e-6) / COMMON_DIFF, 0.875));
    }

    private static int discountExtraSimilarSubstring(int len, int diff) {
        return Math.min(0, (int) (0.01 * Math.pow(len * (len - 1), 0.625) * (diff - 0.66 * COMMON_DIFF)));
    }

    private static boolean isWordBegin(String s, int idx) {
        return idx <= 0 || idx < s.length() &&
                (SPACES.indexOf(s.charAt(idx - 1)) != -1 ||
                        LINE_BREAKS.indexOf(s.charAt(idx - 1)) != -1 ||
                        QUOTES_OPEN.indexOf(s.charAt(idx - 1)) != -1);
    }

    private static boolean isWordEnd(String s, int idx) {
        return isJustAfterWordEnd(s, idx + 1);
    }

    private static boolean isJustAfterWordEnd(String s, int idx) {
        return idx >= s.length() || idx >= 0 &&
                (SPACES.indexOf(s.charAt(idx)) != -1 ||
                        LINE_BREAKS.indexOf(s.charAt(idx)) != -1 ||
                        QUOTES_CLOSE.indexOf(s.charAt(idx)) != -1);
    }

    private static boolean isRowBegin(String s, int idx) {
        return idx <= 0 || idx < s.length() && LINE_BREAKS.indexOf(s.charAt(idx - 1)) != -1;
    }

    private static boolean isRowEnd(String s, int idx) {
        return isLineBreak(s, idx + 1);
    }

    private static boolean isLineBreak(String s, int idx) {
        return idx >= s.length() || idx >= 0 && LINE_BREAKS.indexOf(s.charAt(idx)) != -1;
    }

    private static void initDistRules() {
        initCheapToInsert();

        similarCharsClasses.add(new SimilarChars(SPACES, 1));
        similarCharsClasses.add(new SimilarChars(LINE_BREAKS, 1));
        similarCharsClasses.add(new SimilarChars(SPACES + LINE_BREAKS + "\t", 3));
        similarCharsClasses.add(new SimilarChars(APOSTROPHES, 1));
        similarCharsClasses.add(new SimilarChars(QUOTES_OPEN, 1));
        similarCharsClasses.add(new SimilarChars(QUOTES_CLOSE, 1));
        similarCharsClasses.add(new SimilarChars(APOSTROPHES + QUOTES_OPEN + QUOTES_CLOSE, 5));
        similarCharsClasses.add(new SimilarChars(HYPHENS, 1));
        similarCharsClasses.add(new SimilarChars(DASHES, 1));
        similarCharsClasses.add(new SimilarChars(HYPHENS + DASHES, 4));
        similarCharsClasses.add(new SimilarChars(HYPHENS + SPACES, 9));
        similarCharsClasses.add(new SimilarChars(DOTS, 1));
        similarCharsClasses.add(new SimilarChars(CYRG_UPPER, 7));
        similarCharsClasses.add(new SimilarChars(CYRG_LOWER, 7));
        similarCharsClasses.add(new SimilarChars(CYRII_UPPER, 9));
        similarCharsClasses.add(new SimilarChars(CYRII_LOWER, 9));
        similarCharsClasses.add(new SimilarChars(CYRG_UPPER + CYRG_LOWER, 12));
    }

    public static boolean canBeSpecial(char c) {
        return charToSimClasses.containsKey(c);
    }

    /**
     * Compares two chars (not strings), considering similarity.
     *
     * @param c1 One of chars to be compared.
     * @param c2 Other of chars to be compared.
     * @return 0 for the same,
     * COMMON_DIFF for completely different,
     * COMMON_DIFF / 2 for upper case and lower case of the same character,
     * something between 0 and COMMON_DIFF for pairs treated as "similar"
     */
    public static int getCharsDist(char c1, char c2) {
        if (c1 == c2)
            return 0;
        if (charToSimClasses.containsKey(c1) && charToSimClasses.containsKey(c2)) {
            int cMax = (int) Math.max(c1, c2);
            int cMin = (int) Math.min(c1, c2);
            int code = cMin * 0x10000 + cMax;
            Integer resFromSaved = distSaved.get(code);
            if (resFromSaved != null)
                return resFromSaved;
            int resCalced = COMMON_DIFF - 1;
            for (int i : charToSimClasses.get(c1)) {
                if (charToSimClasses.get(c2).contains(i)) {
                    resCalced = Math.min(resCalced, similarCharsClasses.get(i).dist);
                }
            }
            distSaved.put(code, resCalced);
            return resCalced;
        }
        if (Character.toLowerCase(c1) == Character.toLowerCase(c2))
            return COMMON_DIFF / 2;
        return COMMON_DIFF;
    }


    private static void initCheapToInsert() {
        cheapToInsert = new HashMap<>();
        for (char c : SPACES.toCharArray()) {
            cheapToInsert.put(c, 3);
        }
        for (char c : LINE_BREAKS.toCharArray()) {
            cheapToInsert.put(c, 3);
        }
        cheapToInsert.put('\r', 1);
        for (char c : HYPHENS.toCharArray()) {
            cheapToInsert.put(c, 9);
        }
        for (char c : DOTS.toCharArray()) {
            cheapToInsert.put(c, 9);
        }
        for (char c : QUOTES_OPEN.toCharArray()) {
            cheapToInsert.put(c, 9);
        }
        for (char c : QUOTES_CLOSE.toCharArray()) {
            cheapToInsert.put(c, 9);
        }
    }

    /**
     * @author IlyaCk a.k.a. Ilya Porublyov
     */
    public static class DistResInfo {
        /**
         * Distance between strings.
         * Based on Levenshtein metrics, but is fundamentally generalized, so can be even negative.
         * Distance 1 by standard Levenshtein metrics, when characters are significantly different,
         * corresponds to COMMON_DIFF = 16.
         */
        public final int dist;

        /**
         * Indices in the substring (argument of calcStrDist), which are treated as "should be skipped"
         */
        final List<Integer> posSubDiffers;

        /**
         * Stores characters treated as "matched".
         * Keys are indices in substring, corresponding values are corresponding indices in superstring.
         * Each used key corresponds to exactly one value, and each used value is got from exactly one key.
         */
        final NavigableMap<Integer, Integer> commonSubToSuper;

        /**
         * html-format of detail explain how actually found substring differs from argument subStr
         * style "ins" means that smth not present in subStr was inserted
         * style ""
         */
        public final String diffAsHtml;

        public final MatchLevel matchLevel;

        @Override
        public java.lang.String toString() {
            return "DistResInfo{" +
                    "dist=" + dist +
                    ", matchLevel=" + matchLevel +
                    (diffAsHtml != null && diffAsHtml.length() < 50 ? ", diffAsHtml=" + diffAsHtml : "") +
                    (commonSubToSuper != null && commonSubToSuper.size() < 20 ? ", commonSubToSuper=" + commonSubToSuper : "") +
                    '}';
        }

        /**
         * @param additionalPenalty additional penalty to be added to dist of oldRes
         */
        private DistResInfo(DistResInfo oldRes, int additionalPenalty) {
            if (additionalPenalty < 0)
                throw new IllegalArgumentException("additionalPenalty < 0");
            this.dist = oldRes.dist + additionalPenalty;
            this.diffAsHtml = oldRes.diffAsHtml;
            this.posSubDiffers = oldRes.posSubDiffers;
            this.commonSubToSuper = oldRes.commonSubToSuper;
            this.matchLevel = (this.dist < 30 ? MatchLevel.MEDIUM :
                    (this.dist < 100 ? MatchLevel.LOW : MatchLevel.NOT_MATCHED));
        }

        /**
         * Used when doRestoreWay is true; indices and mappings are generated here,
         * based on generalized-Levenshtein DP table.
         *
         * @param subStr   substring used in calcStrDist
         * @param superStr superstring used in calcStrDist
         * @param dp       generalized-Levenshtein DP table
         * @param choices  choices for generalized-Levenshtein DP table
         */
        private DistResInfo(String subStr, String superStr, int[][] dp, Step[][] choices, SearchBorder left, SearchBorder right, EnumSet<MatchOption> options) throws IOException {
            boolean doDiscountStrings = options.contains(MatchOption.DO_DISCOUNT_STRINGS);
            boolean doDiscountDel = options.contains(MatchOption.DO_DISCOUNT_REPEAT_DEL);
            boolean doDiscountIns = options.contains(MatchOption.DO_DISCOUNT_REPEAT_INS);

            int iii = subStr.length();
            int minValue = dp[iii][superStr.length()];
            int minIdx = superStr.length();

            if (right != SearchBorder.WHOLE_TEXT) {
                for (int j = 0; j < superStr.length(); j++) {
                    if (dp[iii][j + 1] <= minValue &&
                            (right == SearchBorder.ANYWHERE ||
                                    right == SearchBorder.WORD && isWordEnd(superStr, j) ||
                                    right == SearchBorder.ROW && isRowEnd(superStr, j))) {
                        minValue = dp[iii][j + 1];
                        minIdx = j + 1;
                        if (right == SearchBorder.ROW) {
                            for(int jjj=j-1; jjj>=0 && (isWordEnd(superStr, jjj) || DOTS.indexOf(superStr.charAt(jjj+1))!=-1); jjj--) {
                                if (dp[iii][jjj + 1] < minValue) {
                                    minValue = dp[iii][jjj+1];
                                    minIdx = jjj+1;
                                }
                            }
                        }
                    }
                }
            }
            dist = minValue;
            int jjj = minIdx;

            if (choices != null && choices.length == dp.length) { // actually if doRestoreWay

                posSubDiffers = new ArrayList<>();
                commonSubToSuper = new TreeMap<>();
                while (iii > 0 && jjj > 0) {
                    int len = choices[iii][jjj].num();
                    switch (choices[iii][jjj].kind()) {
                        case REPLACE_OR_COPY -> {
                            for (int k = 0; k < len; k++) {
                                if (subStr.charAt(iii - 1) != superStr.charAt(jjj - 1)) {
                                    posSubDiffers.add(iii - 1);
                                } else {
                                    commonSubToSuper.put(iii - 1, jjj - 1);
                                }
                                iii--;
                                jjj--;
                            }
                        }
                        case DEL -> {
                            for (int k = 1; k <= len; k++) {
                                iii--;
                                posSubDiffers.add(iii);
                            }
                        }
                        case INS -> {
                            for (int k = 1; k <= len; k++) {
                                jjj--;
                            }
                        }
                        case SWAP -> {
                            posSubDiffers.add(iii - 1);
                            posSubDiffers.add(iii - 2);
//////                            commonSubToSuper.put(iii - 1, jjj - 2);
//////                            commonSubToSuper.put(iii - 2, jjj - 1);
                            iii -= 2;
                            jjj -= 2;
                        }
                    }
                }
                while (iii > 0) {
                    posSubDiffers.add(iii - 1);
                    iii--;
                }
                diffAsHtml = buildDiffAsHtml(superStr, subStr, left, right, options);
            } else {
                posSubDiffers = null;
                commonSubToSuper = null;
                diffAsHtml = "cmp not restored because you didn't pass such option";
            }
            this.matchLevel =
                    (!doDiscountStrings && !doDiscountDel && !doDiscountIns && this.dist < 10 ?
                            MatchLevel.HIGH :
                            (this.dist < 30 ?
                                    MatchLevel.MEDIUM :
                                    (this.dist < 100 ?
                                            MatchLevel.LOW :
                                            MatchLevel.NOT_MATCHED)));
        }

        /**
         * Used ONLY when trivial string match occurred
         * and main (generalized-Levenshtein) algorithm is skipped.
         *
         * @param subStr substring used in calcStrDist
         * @param start  index in superStr where trivial occurrence of subStr starts
         */
        private DistResInfo(String subStr, int start, EnumSet<MatchOption> options) {
            boolean doDiscountStrings = options.contains(MatchOption.DO_DISCOUNT_STRINGS);
            this.matchLevel = MatchLevel.HIGH;
            this.dist = doDiscountStrings ? discountExtraSimilarSubstring(subStr.length(), 0) : 0;
            this.diffAsHtml = "<html>\n<span class=\"good\">\n" + subStr + "\n</span>\n(dist = " + this.dist + ")\n</html>";
            this.commonSubToSuper = new TreeMap<>();
            this.posSubDiffers = new ArrayList<>();
            for (int i = 0; i < subStr.length(); i++)
                this.commonSubToSuper.put(i, i + start);
        }

        /**
         * Should be called from constructor ONLY!
         * Depends on commonSubToSuper which SHOULD be already set
         */
        private String buildDiffAsHtml(String superStr, String subStr, SearchBorder left, SearchBorder right, EnumSet<MatchOption> options) throws IOException {
            final boolean doExtraDebugOutput = options.contains(MatchOption.DO_EXTRA_DEBUG_OUTPUT);

            if (commonSubToSuper == null || commonSubToSuper.isEmpty()) {
                return "<html>\n<span class=\"skip\">" + superStr + "</span>\n<span class=\"ins\">" + subStr + "</span>\n<br>(dist = " + this.dist + "(?))</html>";
            }
            if (doExtraDebugOutput && subStr.indexOf("групи") == 0) {
                if (superStr.length() < 30)
                    System.out.println("superStr = " + superStr + " // length = " + superStr.length());
                else
                    System.out.println("superStr.length = " + superStr.length());
                System.out.println("subStr = " + subStr + " // length = " + subStr.length());
                System.out.println("commonSubToSuper: size = " + commonSubToSuper.size() + " ,  " + commonSubToSuper);
            }

            StringBuilder sb = new StringBuilder("<html>\n<p>\n");
            var posInSuper = commonSubToSuper.values();
            int minInSuper = posInSuper.stream().min(Integer::compareTo).get();
            int maxInSuper = posInSuper.stream().max(Integer::compareTo).get();
            int idx = 0;
            switch (left) {
                case ANYWHERE -> idx = minInSuper;
                case WORD -> {
                    idx = minInSuper;
                    while (idx >= 0 && !isWordBegin(superStr, idx))
                        idx--;
                }
                case ROW -> {
                    idx = minInSuper;
                    while (idx >= 0 && !isRowBegin(superStr, idx))
                        idx--;
                }
                case WHOLE_TEXT -> idx = 0;
            }
            if (idx < minInSuper) {
                sb.append("<span class=\"ins\">");

                while (idx < minInSuper) {
                    sb.append(superStr.charAt(idx));
                    idx++;
                }
                sb.append("</span>");
            }

            for (int i = 0; i < subStr.length(); ) {
                if (commonSubToSuper.containsKey(i) && superStr.charAt(commonSubToSuper.get(i)) == subStr.charAt(i)) {
                    sb.append("<span class=\"good\">");
                    while (i < subStr.length() && commonSubToSuper.containsKey(i) && superStr.charAt(commonSubToSuper.get(i)) == subStr.charAt(i)) {
                        sb.append(subStr.charAt(i));
                        i++;
                        if (commonSubToSuper.containsKey(i) &&
                                insertInsertedRange(superStr, commonSubToSuper.get(i - 1), commonSubToSuper.get(i), sb))
                            break;
                    }
                    sb.append("</span>");
                } else {
                    sb.append("<span class=\"skip\">");
                    while (i < subStr.length() && !(commonSubToSuper.containsKey(i))) {
                        sb.append(subStr.charAt(i));
                        i++;
                    }
                    try {
                        insertInsertedRange(superStr, commonSubToSuper.lowerEntry(i).getValue(), commonSubToSuper.ceilingEntry(i).getValue(), sb);
                    } catch (NullPointerException e) {

                    }
                    sb.append("</span>");
                }
            }

            if (right != SearchBorder.ANYWHERE) {
                boolean spanStarted = false;
                for (idx = maxInSuper + 1; idx < superStr.length(); idx++) {
                    if (right == SearchBorder.WORD && isJustAfterWordEnd(superStr, idx))
                        break;
                    if (right == SearchBorder.ROW && isLineBreak(superStr, idx))
                        break;
                    if (!spanStarted)
                        sb.append("<span class=\"ins\">");
                    spanStarted = true;
                    sb.append(superStr.charAt(idx));
                }
                if (spanStarted)
                    sb.append("</span>");
            }
            sb.append("\n</p>\n<br>\n");
            sb.append("(dist = ");
            sb.append(this.dist);
            sb.append(")");
            sb.append("\n</html>\n");
            return sb.toString();
        }

        private boolean insertInsertedRange(String superStr, Integer jStart, Integer jEnd, StringBuilder sb) {
            if (jStart == null || jEnd == null || jEnd <= jStart + 1)
                return false;
            else {
                sb.append("</span>");
                sb.append("<span class=\"ins\">");
                for (int j = jStart + 1; j < jEnd; j++) {
                    sb.append(superStr.charAt(j));
                }
                return true;
            }
        }

    }

    private static class SimilarChars {
        /**
         * All chars which are treated as similar.
         * The same char (and even the same pair of chars) MAY appear in different instances of SimilarChars
         */
        final String chars;
        /**
         * Distance between similar chars from the list. Normally should be less than COMMON_DIFF.
         */
        final int dist;

        SimilarChars(String chars, int dist) {
            this.chars = chars;
            this.dist = dist;
            if (this.dist >= COMMON_DIFF)
                throw new IllegalArgumentException("SimilarChars (" + dist + ") dist exceeds COMMON_DIFF (" + COMMON_DIFF + "). " +
                        "It's abnormal and contradicts sense of similar characters.");
            for (char c : chars.toCharArray()) {
                if (!(charToSimClasses.containsKey(c))) {
                    charToSimClasses.put(c, new HashSet<>());
                }
                charToSimClasses.get(c).add(similarCharsClasses.size());
            }
            similarCharsClasses.add(this);
        }
    }

    static Map<Character, Integer> cheapToInsert = null;
    static List<SimilarChars> similarCharsClasses = new ArrayList<>();
    static Map<Character, Set<Integer>> charToSimClasses = new HashMap<>();
    static Map<Integer, Integer> distSaved = new HashMap<>();
    static final int COMMON_DIFF = 16;

    /**
     * @param subStr   Substring which should be searched in superStr.
     *                 Penalty doesn't depend significantly on place of differences.
     * @param superStr Superstring where to search substring.
     * @param options  @see {@link MatchOption}
     * @return If trivial search founds res,  Found distance between subStr and superStr; distance-as-number is returned always,
     * indices and mapping are omitted when doRestoreWay is false.
     * @see DistResInfo
     */
    private static DistResInfo tryTrivialSearch(String subStr, String superStr, SearchBorder left, SearchBorder right, EnumSet<MatchOption> options) {
        // TODO: implement!!!
        return null;
    }

    /**
     * @param subStr   Substring which should be searched in superStr.
     *                 Penalty doesn't depend significantly on place of differences.
     * @param superStr Superstring where to search substring.
     * @param options  @see {@link MatchOption}
     * @return Found distance between subStr and superStr; distance-as-number and match quality (@see {@link MatchLevel}) are returned always,
     * indices, mapping and html-form of diff are omitted when doRestoreWay is false.
     * @see DistResInfo
     */
    public static DistResInfo calcStrDist(String subStr, String superStr, SearchBorder left, SearchBorder right, EnumSet<MatchOption> options) throws IOException {

        boolean doRestoreWay = options.contains(MatchOption.DO_RESTORE_PATH);
        boolean doDiscountStrings = options.contains(MatchOption.DO_DISCOUNT_STRINGS);
        boolean doDiscountRepIns = options.contains(MatchOption.DO_DISCOUNT_REPEAT_INS);
        boolean doDiscountRepDel = options.contains(MatchOption.DO_DISCOUNT_REPEAT_DEL);
        boolean doDiscountDoubling = options.contains(MatchOption.DO_DISCOUNT_SYMBOL_DOUBLING);
        boolean doExtraDebugOutput = options.contains(MatchOption.DO_EXTRA_DEBUG_OUTPUT);


        if (doExtraDebugOutput) {
            if (superStr.length() < 30)
                System.out.println("superStr = " + superStr + " // length = " + superStr.length());
            else
                for (int i = 0; i < superStr.length(); i++)
                    System.out.println("superStr[" + i + "] = " + superStr.charAt(i) + " (" + (int) (superStr.charAt(i)) + ")");
//            System.out.println("superStr.length = " + superStr.length());
            System.out.println("subStr = " + subStr + " // length = " + subStr.length());
        }

        if (cheapToInsert == null) {
            initDistRules();
        }

        DistResInfo trivSrchRes = tryTrivialSearch(subStr, superStr, left, right, options);
        if (trivSrchRes != null &&
                (!doDiscountStrings || trivSrchRes.matchLevel == MatchLevel.HIGH)) {
            return trivSrchRes;
        }

        int[][] dp = new int[subStr.length() + 1][superStr.length() + 1];
        Step[][] choices = (
                doRestoreWay ?
                        new Step[subStr.length() + 1][superStr.length() + 1] :
                        new Step[0][0]
        );

        int[] trivInsCosts = new int[superStr.length()];
        for (int j = 0; j < superStr.length(); j++) {
            Integer cost = cheapToInsert.get(superStr.charAt(j));
            trivInsCosts[j] = (cost != null ? cost : COMMON_DIFF);
        }
        int[] trivDelCosts = new int[subStr.length()];
        for (int i = 0; i < subStr.length(); i++) {
            Integer cost = cheapToInsert.get(subStr.charAt(i));
            trivDelCosts[i] = (cost != null ? cost : COMMON_DIFF);
        }

        dp[0][0] = 0;
        for (int i = 0; i <= subStr.length(); i++) {
            boolean allSpacesSinceRowBegin = true;
            for (int j = 0; j <= superStr.length(); j++) {
                if (i == 0 && j == 0)
                    continue;
                if (j > 0 && (SPACES+QUOTES_OPEN).indexOf(superStr.charAt(j-1)) == -1) { // is NOT a (SPACE or QUOTE_OPEN)
                    allSpacesSinceRowBegin = false;
                } else if (isLineBreak(superStr, j - 1)) {
                    allSpacesSinceRowBegin = true;
                }
                if (i==0 && left != SearchBorder.WHOLE_TEXT) {
                    if (left == SearchBorder.ANYWHERE ||
                            left == SearchBorder.WORD && isWordBegin(superStr, j) ||
                            left == SearchBorder.ROW && (isRowBegin(superStr, j) || allSpacesSinceRowBegin))
                    {
                        dp[0][j] = 0;
                        if (doRestoreWay) {
                            choices[0][j] = new Step(KindOfEdit.INS, j);
                        }
                        continue;
                    }
                }
                int minDist = Integer.MAX_VALUE / 2;
                int distIns = Integer.MAX_VALUE / 2;
                int insLen = -1;
                if (j > 0) {
                    distIns = dp[i][j - 1] + trivInsCosts[j - 1];
                    insLen = 1;
                    if (distIns < minDist) {
                        minDist = distIns;
                    }
                    if (doDiscountDoubling && doRestoreWay && j > 1 && choices[i][j-1] != null && choices[i][j-1].kind() != KindOfEdit.DEL) {
                        int costTwo = getCharsDist(superStr.charAt(j - 1), superStr.charAt(j - 2));
                        if (costTwo < trivInsCosts[j-1]) {
                            costTwo = (2 * costTwo + trivInsCosts[j-1]) / 3;
                            // considering costTwo < trivInsCosts[j-1], this is surely less than trivInsCosts[j-1]
                            // It's a BAD idea to leave costTwo as was,
                            // because this makes doubling of exactly same symbol absolutely free (no cost)
                            int distInsTwo = dp[i][j-1] + costTwo;
                            if (distInsTwo < minDist) {
                                minDist = distIns = distInsTwo;
                            }
                        }
                    }
                }

                int distDel = Integer.MAX_VALUE / 2;
                int delLen = -1;
                if (i > 0) {
                    distDel = dp[i - 1][j] + trivDelCosts[i - 1];
                    delLen = 1;
                    if (distDel < minDist) {
                        minDist = distDel;
                    }
                    if (doDiscountDoubling && doRestoreWay && i > 1 && choices[i-1][j] != null && choices[i-1][j].kind() != KindOfEdit.INS) {
                        int costTwo = getCharsDist(subStr.charAt(i - 1), subStr.charAt(i - 2));
                        if (costTwo < trivDelCosts[i-1]) {
                            costTwo = (2 * costTwo + trivDelCosts[i-1]) / 3;
                            // considering costTwo < trivDelCosts[i-1], this is surely less than trivDelCosts[i-1]
                            // It's a BAD idea to leave costTwo as was,
                            // because this makes doubling of exactly same symbol absolutely free (no cost)
                            int distDelTwo = dp[i-1][j] + costTwo;
                            if (distDelTwo < minDist) {
                                minDist = distDel = distDelTwo;
                            }
                        }
                    }
                }

                int distReplace = Integer.MAX_VALUE / 2;
                int copyOrReplaceLen = -1;
                if (i > 0 && j > 0) {
                    int thisCharDist = getCharsDist(subStr.charAt(i - 1), superStr.charAt(j - 1));
                    distReplace = dp[i - 1][j - 1] + thisCharDist;
                    copyOrReplaceLen = 1;
                    if (distReplace < minDist) {
                        minDist = distReplace;
                    }
                    if (i > 1 && j > 1) {
                        int commonOrderCost = getCharsDist(subStr.charAt(i - 1), superStr.charAt(j - 1)) + getCharsDist(subStr.charAt(i - 2), superStr.charAt(j - 2));
                        int swappedOrderCost = getCharsDist(subStr.charAt(i - 1), superStr.charAt(j - 2)) + getCharsDist(subStr.charAt(i - 2), superStr.charAt(j - 1));
                        if (swappedOrderCost < commonOrderCost) {
                            int distForSwapped = dp[i - 2][j - 2] + (swappedOrderCost + commonOrderCost) / 2;
                            if (distForSwapped < minDist) {
                                minDist = distReplace = distForSwapped;
                                copyOrReplaceLen = 2;
                            }
                        }
                    }
                }

                if (doDiscountStrings && i > 1 && j > 1) {
                    int thisStrDist = getCharsDist(subStr.charAt(i - 1), superStr.charAt(j - 1));
                    for (int len = 2; len <= i && len <= j; len++) {
                        thisStrDist += getCharsDist(subStr.charAt(i - len), superStr.charAt(j - len));
                        if (3 * thisStrDist >= 2 * COMMON_DIFF) {
                            break;
                        }
                        int curr = dp[i - len][j - len] + thisStrDist + discountExtraSimilarSubstring(len, thisStrDist);
                        if (curr < minDist) {
                            minDist = distReplace = curr;
                            copyOrReplaceLen = len;
                        }
                    }
                }

                if (doDiscountRepIns && j > 1) {
                    int separateInsCostsSum = trivInsCosts[j - 1];
                    for (int len = 2; len <= j; len++) {
                        separateInsCostsSum += trivInsCosts[j - len];
                        int discountedSum = discountInsFunc(separateInsCostsSum);
                        if (!doDiscountStrings && discountedSum >= minDist)
                            break;
                        int newDistIns = dp[i][j - len] + discountedSum;
                        if (newDistIns < minDist) {
                            minDist = distIns = newDistIns;
                            insLen = len;
                        }
                    }
                }

                if (doDiscountRepDel && i > 1) {
                    int separateDelCostsSum = trivDelCosts[i - 1];
                    for (int len = 2; len <= i; len++) {
                        separateDelCostsSum += trivDelCosts[i - len];
                        int discountedSum = discountDelFunc(separateDelCostsSum);
                        if (!doDiscountStrings && discountedSum >= minDist)
                            break;
                        int newDistDel = dp[i - len][j] + discountedSum;
                        if (newDistDel < minDist) {
                            minDist = distDel = newDistDel;
                            delLen = len;
                        }
                    }
                }

                dp[i][j] = minDist;

                if (doRestoreWay) {
                    if (distReplace == minDist)
                        choices[i][j] = new Step(KindOfEdit.REPLACE_OR_COPY, copyOrReplaceLen);
                    else if (distIns == minDist)
                        choices[i][j] = new Step(KindOfEdit.INS, insLen);
                    else if (distDel == minDist)
                        choices[i][j] = new Step(KindOfEdit.DEL, delLen);
                    else
                        throw new IllegalArgumentException("Smth bad in determining kind of edit");
                }
            }
        }
        return new DistResInfo(subStr, superStr, dp, choices, left, right, options);
    }

    public static boolean likelyContains(String subStr, String superStr) {
        try {
            DistResInfo resInfo = calcStrDist(subStr, superStr, SearchBorder.ANYWHERE, SearchBorder.ANYWHERE,
                    EnumSet.of(MatchOption.DO_DISCOUNT_SYMBOL_DOUBLING, MatchOption.DO_DISCOUNT_REPEAT_DEL, MatchOption.DO_DISCOUNT_REPEAT_INS));
            return resInfo.matchLevel.betterOrEqual(MatchLevel.MEDIUM);
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean likelyContainsRows(String subStr, String superStr) {
        try {
            DistResInfo resInfo = calcStrDist(subStr, superStr, SearchBorder.ROW, SearchBorder.ROW,
                    EnumSet.of(MatchOption.DO_DISCOUNT_SYMBOL_DOUBLING, MatchOption.DO_DISCOUNT_REPEAT_DEL, MatchOption.DO_DISCOUNT_REPEAT_INS));
            return resInfo.matchLevel.betterOrEqual(MatchLevel.MEDIUM);
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean likelyContainsWords(String subStr, String superStr) {
        try {
            DistResInfo resInfo = calcStrDist(subStr, superStr, SearchBorder.WORD, SearchBorder.WORD,
                    EnumSet.of(MatchOption.DO_DISCOUNT_SYMBOL_DOUBLING, MatchOption.DO_DISCOUNT_REPEAT_DEL, MatchOption.DO_DISCOUNT_REPEAT_INS));
            return resInfo.matchLevel.betterOrEqual(MatchLevel.MEDIUM);
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean likelyMatches(String subStr, String superStr) {
        try {
            DistResInfo resInfo = calcStrDist(subStr, superStr, SearchBorder.WHOLE_TEXT, SearchBorder.WHOLE_TEXT,
                    EnumSet.of(MatchOption.DO_DISCOUNT_SYMBOL_DOUBLING, MatchOption.DO_DISCOUNT_REPEAT_DEL, MatchOption.DO_DISCOUNT_REPEAT_INS));
            return resInfo.matchLevel.betterOrEqual(MatchLevel.MEDIUM);
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean highlyLikelyContains(String subStr, String superStr) {
        try {
            DistResInfo resInfo = calcStrDist(subStr, superStr, SearchBorder.ANYWHERE, SearchBorder.ANYWHERE,
                    EnumSet.noneOf(MatchOption.class));
            return resInfo.matchLevel == MatchLevel.HIGH;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean highlyLikelyContainsRows(String subStr, String superStr) {
        try {
            DistResInfo resInfo = calcStrDist(subStr, superStr, SearchBorder.ROW, SearchBorder.ROW,
                    EnumSet.noneOf(MatchOption.class));
            return resInfo.matchLevel == MatchLevel.HIGH;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean highlyLikelyContainsWords(String subStr, String superStr) {
        try {
            DistResInfo resInfo = calcStrDist(subStr, superStr, SearchBorder.WORD, SearchBorder.WORD,
                    EnumSet.noneOf(MatchOption.class));
            return resInfo.matchLevel == MatchLevel.HIGH;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean highlyLikelyMatches(String subStr, String superStr) {
        try {
            DistResInfo resInfo = calcStrDist(subStr, superStr, SearchBorder.WHOLE_TEXT, SearchBorder.WHOLE_TEXT,
                    EnumSet.noneOf(MatchOption.class));
            return resInfo.matchLevel == MatchLevel.HIGH;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static DistResInfo getBestMatch___(String substr, String str, SearchBorder left, SearchBorder right) {
        DistResInfo distInfo = null;
        DistResInfo distInfoUpperCase = null;
        try {
            distInfo = calcStrDist(substr, str,
                    left, right,
                    EnumSet.of(StrDistComplicated.MatchOption.DO_RESTORE_PATH));
        } catch (IOException e) {
            distInfo = new DistResInfo(new DistResInfo("", 0, EnumSet.noneOf(MatchOption.class)), Integer.MAX_VALUE / 3);
        }
        if (distInfo.matchLevel.betterOrEqual(MatchLevel.MEDIUM)) {
            return distInfo;
        }
        try {
            distInfoUpperCase = new DistResInfo(calcStrDist(substr.toUpperCase(Locale.ROOT), str.toUpperCase(Locale.ROOT),
                    left, right,
                    EnumSet.of(StrDistComplicated.MatchOption.DO_RESTORE_PATH,
                            MatchOption.DO_DISCOUNT_SYMBOL_DOUBLING,
                            MatchOption.DO_DISCOUNT_REPEAT_DEL,
                            MatchOption.DO_DISCOUNT_REPEAT_INS)),
                    25);
        } catch (IOException e) {
            distInfoUpperCase = new DistResInfo(new DistResInfo("", 0, EnumSet.noneOf(MatchOption.class)), Integer.MAX_VALUE / 3);
        }
        if (distInfo.dist <= distInfoUpperCase.dist) {
            return distInfo;
        } else {
            return distInfoUpperCase;
        }
    }

    public static DistResInfo getBestMatchAnywhere(String substr, String str) {
        return getBestMatch___(substr, str, SearchBorder.ANYWHERE, SearchBorder.ANYWHERE);
    }

    public static DistResInfo getBestMatchWord(String substr, String str) {
        return getBestMatch___(substr, str, SearchBorder.WORD, SearchBorder.WORD);
    }

    public static DistResInfo getBestMatchWordRow(String substr, String str) {
        return getBestMatch___(substr, str, SearchBorder.WORD, SearchBorder.ROW);
    }

    public static DistResInfo getBestMatchRow(String substr, String str) {
        return getBestMatch___(substr, str, SearchBorder.ROW, SearchBorder.ROW);
    }

    public static DistResInfo getBestMatchWhole(String substr, String str) {
        return getBestMatch___(substr, str, SearchBorder.WHOLE_TEXT, SearchBorder.WHOLE_TEXT);
    }

}

