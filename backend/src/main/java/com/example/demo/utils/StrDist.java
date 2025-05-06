package com.example.demo.utils;

import java.io.IOException;
import java.util.*;

/**
 * @author IlyaCk a.k.a. Ilya Porublyov
 * The normal way to use is via method calcStrDist
 */
public class StrDist {

    public enum MatchLevel {
        NOT_MATCHED,
        LOW,
        MEDIUM,
        HIGH
    }

    public enum MatchOption {
        AS_SUBSTR,
        DO_RESTORE_PATH,
        DO_CONSIDER_SUBSTRINGS,
        DO_DISCOUNT_REPEAT_SKIP,
        DO_DISCOUNT_REPEAT_INSERT,
    }

    enum KindOfEdit {
        REPLACE_OR_COPY,
        DEL,
        INS,
        SWAP
    }

    /**
     * @param kind @see KindOfEdit
     * @param num How many chars are replaced/copied/inserted/skipped at the step.
     */
    record Step(KindOfEdit kind, int num) {}

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

    /**
     * @author IlyaCk a.k.a. Ilya Porublyov
     *
     *
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
//        /**
//         * Indices in the superstring (argument of calcStrDist), which are treated as "should be skipped"
//         */
//        final List<Integer> posSuperDiffers;
        /**
         * Stores characters treated as "matched".
         * Keys are indices in substring, corresponding values are corresponding indices in superstring.
         * Each used key corresponds to exactly one value, and each used value is got from exactly one key.
         */
        final NavigableMap<Integer, Integer> commonSubToSuper;

//        public final String subStrMarksPlusesAndMinuses;

        /**
         * html-format of detail explain how actually found substring differs from argument subStr
         * style "ins" means that smth not present in subStr was inserted
         * style ""
         */
        public final String diffAsHtml;

        public final MatchLevel matchLevel;

        /**
         * Used when doRestoreWay is true; indices and mappings are generated here,
         * based on generalized-Levenshtein DP table.
         * @param subStr substring used in calcStrDist
         * @param superStr superstring used in calcStrDist
         * @param dp generalized-Levenshtein DP table
         * @param choices choices for generalized-Levenshtein DP table
         */
        private DistResInfo(String subStr, String superStr, int[][] dp, Step[][] choices) throws IOException {
            int iii = subStr.length();
            int minValue = dp[iii][superStr.length()];
            int minIdx = superStr.length();
            for (int j = 0; j < superStr.length(); j++) {
                if (dp[iii][j] < minValue && (superStr.charAt(j) == '\n' || SPACES.indexOf(superStr.charAt(j)) != -1 || QUOTES_CLOSE.indexOf(superStr.charAt(j)) != -1)) {
                    minValue = dp[iii][j];
                    minIdx = j;
                }
            }
            dist = minValue;
            int jjj = minIdx;

            if(choices != null && choices.length == dp.length) { // actually if doRestoreWay

                posSubDiffers = new ArrayList<>();
//                posSuperDiffers = new ArrayList<>();
                commonSubToSuper = new TreeMap<>();
//                for (int j = superStr.length(); j >= jjj; j--) {
//                    posSuperDiffers.add(j);
//                }
                while (iii > 0 && jjj > 0) {
                    int len = choices[iii][jjj].num();
                    switch (choices[iii][jjj].kind()) {
                        case REPLACE_OR_COPY -> {
                            for (int k = 0; k < len; k++) {
                                if (subStr.charAt(iii - 1) != superStr.charAt(jjj - 1)) {
                                    posSubDiffers.add(iii - 1);
//                                    posSuperDiffers.add(jjj - 1);
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
//                                posSuperDiffers.add(jjj);
                            }
                        }
                        case SWAP -> {
                            posSubDiffers.add(iii - 1);
                            posSubDiffers.add(iii - 2);
//                            posSuperDiffers.add(jjj - 1);
//                            posSuperDiffers.add(jjj - 2);
//////                            commonSubToSuper.put(iii - 1, jjj - 2);
//////                            commonSubToSuper.put(iii - 2, jjj - 1);
                            iii -= 2;
                            jjj -= 2;
                        }
                    }
                }
//            while (jjj > 0) {
////                    posSuperDiffers.add(jjj - 1);
//                jjj--;
//            }
                while (iii > 0) {
                    posSubDiffers.add(iii - 1);
                    iii--;
                }
                diffAsHtml = buildDiffAsHtml(superStr, subStr);
            } else {
                posSubDiffers = null;
                commonSubToSuper = null;
                diffAsHtml = "cmp not restored because you didn't pass such option";
            }
            this.matchLevel = (this.dist < 10 ? MatchLevel.HIGH :
                    (this.dist < 30 ? MatchLevel.MEDIUM :
                            (this.dist < 100 ? MatchLevel.LOW : MatchLevel.NOT_MATCHED)));
        }

        public DistResInfo(String subStr, int start, boolean doConsiderStrings) {
            this.matchLevel = MatchLevel.HIGH;
            this.dist = doConsiderStrings ? (int)(-Math.pow(subStr.length(), 0.75) * Math.sqrt(COMMON_DIFF)): 0;
            this.diffAsHtml = "<html>\n<span class=\"good\">\n" + subStr + "\n</span>\n(dist = " + this.dist + ")\n</html>";
            this.commonSubToSuper = new TreeMap<>();
            this.posSubDiffers = new ArrayList<>();
            for (int i = 0; i < subStr.length(); i++)
                this.commonSubToSuper.put(i, i+start);
        }

        /**
         * Should be called from constructor ONLY!
         * Depends on commonSubToSuper which SHOULD be already set
         */
        private String buildDiffAsHtml(String superStr, String subStr) throws IOException {
            StringBuilder sb = new StringBuilder("<html>\n<p>\n");

            var posInSuper = commonSubToSuper.values();
            int minInSuper = posInSuper.stream().min(Integer::compareTo).get();
            int maxInSuper = posInSuper.stream().max(Integer::compareTo).get();
            int idx = minInSuper-1;
            while (idx >= 0
                    && SPACES.indexOf(superStr.charAt(idx)) == -1
                    && LINE_BREAKS.indexOf(superStr.charAt(idx)) == -1
                    && QUOTES_OPEN.indexOf(superStr.charAt(idx)) == -1)
            {
                idx--;
            }
            if (idx < minInSuper - 1) {
                sb.append("<span class=\"ins\">");
                idx++;
                while (idx < minInSuper) {
                    sb.append(superStr.charAt(idx));
                    idx++;
                }
                sb.append("</span>");
            }

            for(int i = 0; i < subStr.length(); ) {
                if(commonSubToSuper.containsKey(i) && superStr.charAt(commonSubToSuper.get(i)) == subStr.charAt(i)) {
                    sb.append("<span class=\"good\">");
                    while(i < subStr.length() && commonSubToSuper.containsKey(i) && superStr.charAt(commonSubToSuper.get(i)) == subStr.charAt(i)) {
                        sb.append(subStr.charAt(i));
                        i++;
                        if(commonSubToSuper.containsKey(i) &&
                                insertSkippedRange(superStr, commonSubToSuper.get(i-1), commonSubToSuper.get(i), sb))
                            break;
                    }
                    sb.append("</span>");
                } else {
                    sb.append("<span class=\"skip\">");
                    while(i < subStr.length() && !(commonSubToSuper.containsKey(i))) {
                        sb.append(subStr.charAt(i));
                        i++;
                    }
                    try {
                        insertSkippedRange(superStr, commonSubToSuper.lowerEntry(i).getValue(), commonSubToSuper.ceilingEntry(i).getValue(), sb);
                    } catch (NullPointerException e) {

                    }
                    sb.append("</span>");
                }
            }

            idx = maxInSuper+1;
            boolean spanStarted = false;
            while (idx < superStr.length()
                    && SPACES.indexOf(superStr.charAt(idx)) == -1
                    && LINE_BREAKS.indexOf(superStr.charAt(idx)) == -1
                    && QUOTES_CLOSE.indexOf(superStr.charAt(idx)) == -1)
            {
                if(!spanStarted)
                    sb.append("<span class=\"ins\">");
                spanStarted = true;
                sb.append(superStr.charAt(idx));
                idx++;
            }
            if(spanStarted)
                sb.append("</span>");

            sb.append("\n</p>\n");
            /*try {
                sb.append(Files.readString(Path.of("aa.css")));
            } catch (IOException e) {
                System.out.println("Failed to copy ``aa.css''");
            }*/
            sb.append("(dist = ");
            sb.append(this.dist);
            sb.append(")");
            sb.append("\n</html>\n");
            return sb.toString();
        }

        private boolean insertSkippedRange(String superStr, Integer jStart, Integer jEnd, StringBuilder sb) {
            if(jStart == null || jEnd == null || jEnd <= jStart + 1)
                return false;
            else {
                sb.append("</span>");
                sb.append("<span class=\"ins\">");
                for(int j = jStart +1; j < jEnd; j++) {
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
     *
     * @param subStr Substring which should be searched in superStr.
     *               Penalty doesn't depend significantly on place of differences.
     * @param superStr Superstring where to search substring. Skipping begin and end are very cheap.
     * @param options @see {@link MatchOption}
     *
     * @return Found distance between subStr and superStr; distance-as-number is returned always,
     * indices and mapping are omitted when doRestoreWay is false.
     * @see DistResInfo
     */
    public static DistResInfo calcStrDist(String subStr, String superStr, EnumSet<MatchOption> options) throws IOException {

        boolean doConsiderStrings = options.contains(MatchOption.DO_CONSIDER_SUBSTRINGS);
        boolean asSubstring = options.contains(MatchOption.AS_SUBSTR);
        boolean doRestoreWay = options.contains(MatchOption.DO_RESTORE_PATH);
        boolean doDiscountRepIns = options.contains(MatchOption.DO_DISCOUNT_REPEAT_INSERT);
        boolean doDiscountRepDel = options.contains(MatchOption.DO_DISCOUNT_REPEAT_SKIP);

        if(asSubstring) { // tests for trivial matching
            int startByTrivialSearch = superStr.indexOf(subStr);
            if (startByTrivialSearch != -1) { // exact Match exists
                return new DistResInfo(subStr, startByTrivialSearch, doConsiderStrings);
            }
        } else {
            if(subStr.equals(superStr)) {
                return new DistResInfo(subStr, 0, false);
            }
        }

        if(cheapToInsert == null) {
            initDistRules();
        }

        int[] delPrefixSum = (doConsiderStrings ? new int[subStr.length()+1] : new int[1]);
        delPrefixSum[0] = 0;
        int[][] dp = new int[subStr.length()+1][superStr.length()+1];
        Step[][] choices = (
                doRestoreWay ?
                        new Step[subStr.length()+1][superStr.length()+1] :
                        new Step[0][0]
        );
        int[] insCosts = new int[superStr.length()];
        for(int j=0; j<superStr.length(); j++) {
            insCosts[j] = calcInsOrDelCost(superStr, j+1, doDiscountRepIns);
        }
        int[] delCosts = new int[subStr.length()];
        for(int i=0; i<subStr.length(); i++) {
            delCosts[i] = calcInsOrDelCost(subStr, i+1, doDiscountRepDel);
        }
        dp[0][0] = 0;
        for(int j=1; j<superStr.length(); j++) {
            if(asSubstring && (superStr.charAt(j-1) == '\n' || SPACES.indexOf(superStr.charAt(j-1)) != -1 || QUOTES_OPEN.indexOf(superStr.charAt(j-1)) != -1)) {
                dp[0][j] = 0; // subStr can start rather anywhere, but starting at newline is extremely cheap
                if (doRestoreWay) {
                    choices[0][j] = new Step(KindOfEdit.INS, j);
                }
            } else {
                dp[0][j] = dp[0][j-1] + calcInsOrDelCost(superStr, j, doDiscountRepIns);
                if (doRestoreWay) {
                    choices[0][j] = new Step(KindOfEdit.INS, 1);
                }
            }
        }
        for(int i = 1; i <= subStr.length(); i++) {
            Integer d = cheapToInsert.get(subStr.charAt(i-1));
            if(d==null) d = COMMON_DIFF;
            if (doConsiderStrings) {
                delPrefixSum[i] = delPrefixSum[i - 1] + d;
                dp[i][0] = discountSkipFunction(delPrefixSum[i]);
            } else {
                dp[i][0] = dp[i - 1][0] + d;
            }
            if (doRestoreWay) {
                choices[i][0] = new Step(KindOfEdit.DEL, i);
            }
            for (int j = 1; j <= superStr.length(); j++) {
//                System.err.print("" + subStr.substring(0, i) + "\t" + j + "\t" + superStr.charAt(j-1));
                int thisCharDist = getCharsDist(subStr.charAt(i-1), superStr.charAt(j-1));
                int distReplace = dp[i-1][j-1] + thisCharDist;
                int copyOrReplaceLen = 1;
                if (doConsiderStrings && thisCharDist < COMMON_DIFF / 3) {
                    int thisSubstrDist = thisCharDist;
                    int iii = i - 2, jjj = j - 2;
                    while (iii>=0 && jjj>=0) {
                        thisCharDist = getCharsDist(subStr.charAt(iii), superStr.charAt(jjj));
                        if (thisCharDist >= COMMON_DIFF / 3)
                            break;





                        thisSubstrDist += thisCharDist;
                        if (thisSubstrDist >= COMMON_DIFF)
                            break;
                        int newDistReplace = dp[iii][jjj] + thisSubstrDist + (int)(Math.pow(i-iii+1, 0.75) * Math.sqrt(COMMON_DIFF) * (thisSubstrDist - COMMON_DIFF / 2.0));
                        if (newDistReplace < distReplace) {
                            distReplace = newDistReplace;
                            copyOrReplaceLen = i - iii;
                        }
                        iii--;
                        jjj--;
                    }
                }
                int distIns = dp[i][j-1] + insCosts[j-1]; // already includes doDiscountRepIns






                int insLen = 1;
                int distDel = dp[i-1][j] + delCosts[i-1]; // already includes doDiscountRepDel
                int delLen = 1;
                int minDist = Math.min(Math.min(distReplace, distDel), distIns);
                if (doConsiderStrings) {
                    int skipCostSum = insCosts[j-1];
                    for(int jjj=j-2; jjj>=0; jjj--) {
                        skipCostSum += insCosts[jjj];
                        int currSkipCost = discountSkipFunction(skipCostSum);
                        if(currSkipCost > minDist)
                            break;
                        int longerSkipDist = dp[i][jjj] + currSkipCost;
                        if (longerSkipDist < distDel) {
                            distDel = longerSkipDist;
                            minDist = Math.min(minDist, distDel);
                            insLen = j - jjj;
                        }
                    }
                }
                if (doConsiderStrings) {
                    for(int iii=i-2; iii>=0; iii--) {
                        int currInsCost = discountInsertFunction(delPrefixSum[i] - delPrefixSum[iii]);
                        if(currInsCost > minDist)
                            break;
                        int longerInsDist = dp[iii][j] + currInsCost;
                        if (longerInsDist < distDel) {
                            distDel = longerInsDist;
                            minDist = Math.min(minDist, distDel);
                            delLen = i - iii;
                        }
                    }
                }
                int distSwappedTwo = dp[i-1][j-1] + 3*COMMON_DIFF;
                if(i>1 && j>1) {
                    int thisOrderCost = getCharsDist(subStr.charAt(i-1), superStr.charAt(j-1)) + getCharsDist(subStr.charAt(i-2), superStr.charAt(j-2));
                    int swappedOrderCost = getCharsDist(subStr.charAt(i-1), superStr.charAt(j-2)) + getCharsDist(subStr.charAt(i-2), superStr.charAt(j-1));
                    distSwappedTwo = dp[i-2][j-2] + (thisOrderCost + swappedOrderCost) / 2;
                    if (distSwappedTwo < minDist) {
                        minDist = distSwappedTwo;
                    }
                }
                dp[i][j] = minDist;

                if(doRestoreWay) {
                    if(distReplace == minDist)
                        choices[i][j] = new Step (KindOfEdit.REPLACE_OR_COPY, copyOrReplaceLen);
                    else if(distIns == minDist)
                        choices[i][j] = new Step (KindOfEdit.INS, insLen);
                    else if(distDel == minDist)
                        choices[i][j] = new Step(KindOfEdit.DEL, delLen);
                    else if(minDist == distSwappedTwo)
                        choices[i][j] = new Step (KindOfEdit.SWAP, 2);
                    else
                        throw new IllegalArgumentException("Smth bad in determining kind of edit");
                }
            }
        }
        return new DistResInfo(subStr, superStr, dp, choices);
    }

    private static int calcInsOrDelCost(String str, int idx, boolean doDiscount) {
        Integer sc = cheapToInsert.get(str.charAt(idx-1));
        int howSimilarPrev = (doDiscount && idx > 1) ? getCharsDist(str.charAt(idx-1), str.charAt(idx-2)) : COMMON_DIFF;
        int sc2 = (COMMON_DIFF + howSimilarPrev) / 2;
        if (sc != null && sc < sc2)
            sc2 = sc;
        return sc2;
    }


    private static int discountSkipFunction(int sum) {
//        return sum;
//        if(sum <= 2*COMMON_DIFF)
//            return sum;
//        else
//            return COMMON_DIFF + (int)(COMMON_DIFF * Math.pow((sum - COMMON_DIFF + 1e-6)/COMMON_DIFF, 0.75));
        if(sum <= COMMON_DIFF)
            return sum;
        else
            return COMMON_DIFF + (int)Math.sqrt(COMMON_DIFF * (sum - COMMON_DIFF));
    }

    private static int discountInsertFunction(int sum) {
        return sum;
//        if(sum <= COMMON_DIFF)
//            return sum;
//        else
//            return COMMON_DIFF + (int)(COMMON_DIFF * Math.pow((sum - COMMON_DIFF + 1e-6)/COMMON_DIFF, 0.875));
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

    public static boolean canBeSpecial(char c)
    {
        return charToSimClasses.containsKey(c);
    }

    /**
     * Compares two chars (not strings), considering similarity.
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
        if(charToSimClasses.containsKey(c1) && charToSimClasses.containsKey(c2)) {
            int cMax = (int)Math.max(c1, c2);
            int cMin = (int)Math.min(c1, c2);
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
        if(Character.toLowerCase(c1) == Character.toLowerCase(c2))
            return COMMON_DIFF / 2;
        return COMMON_DIFF;
    }


    private static void initCheapToInsert() {
        cheapToInsert = new HashMap<>();
        for(char c : SPACES.toCharArray()) {
            cheapToInsert.put(c, 3);
        }
        for(char c : LINE_BREAKS.toCharArray()) {
            cheapToInsert.put(c, 3);
        }
        cheapToInsert.put('\r', 1);
        for(char c : HYPHENS.toCharArray()) {
            cheapToInsert.put(c, 9);
        }
        for(char c : DOTS.toCharArray()) {
            cheapToInsert.put(c, 9);
        }
        for(char c : QUOTES_OPEN.toCharArray()) {
            cheapToInsert.put(c, 9);
        }
        for(char c : QUOTES_CLOSE.toCharArray()) {
            cheapToInsert.put(c, 9);
        }
    }

}
