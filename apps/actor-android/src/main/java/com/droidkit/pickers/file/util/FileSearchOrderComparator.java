package com.droidkit.pickers.file.util;

import com.droidkit.pickers.file.items.ExplorerItem;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 07/10/2014.
 */
public class FileSearchOrderComparator extends FileOrderComparator {

    private final String strongRegex;
    private final String mostStrongRegex;
    private final String searchQuery;

    public FileSearchOrderComparator(String searchQuery) {
        this.searchQuery = searchQuery;
        String tempRegex = searchQuery.toLowerCase();
        String[] splitedTempRegex = tempRegex.split("\\s+");
        ArrayList<String> filtered = new ArrayList<String>();
        for (String s : splitedTempRegex) {
            if (s != null && !s.equals("")) {
                filtered.add(s);
            }
        }
        tempRegex = filtered.toString()
                .replaceAll("\\[", "(").replaceAll("]", ")")
                .replaceAll(",", "|")
                .replaceAll("\\s+", "")
                .replaceAll("\\.", "\\\\.");

        this.strongRegex = "(((.*)(\\s+))|(^))" + tempRegex + ".*"; // strong regex
        this.mostStrongRegex = "^" + tempRegex + ".*"; // THE MOST STRONG REGEX CAREFUL!!1
    }

    @Override
    protected int compareFiles(ExplorerItem explorerItem, ExplorerItem explorerItem2) {
        boolean firstStronk = false;
        boolean secondStronk = false;
        String firstTitle = explorerItem.getTitle().toLowerCase();
        String secondTitle = explorerItem2.getTitle().toLowerCase();
        if (searchQuery.length() < 3) {
            return (firstTitle.compareTo(secondTitle));
        }
        if (firstTitle.matches(strongRegex)) {
            firstStronk = true;
        }
        if (secondTitle.matches(strongRegex)) {
            secondStronk = true;
        }
        if (firstStronk && secondStronk) {
            boolean firstVeryStronk = false;
            boolean secondVeryStronk = false;

            if (firstTitle.matches(mostStrongRegex)) {
                firstVeryStronk = true;
            }
            if (secondTitle.matches(mostStrongRegex)) {
                secondVeryStronk = true;
            }

            if (firstVeryStronk && secondVeryStronk) {
                return (firstTitle.compareTo(secondTitle));
            }

            if (!firstVeryStronk && !secondVeryStronk) {
                return (firstTitle.compareTo(secondTitle));
            }
            return firstVeryStronk ? -1 : 1;

        }
        if (!firstStronk && !secondStronk) {
            return (firstTitle.compareTo(secondTitle));
        }
        return firstStronk ? -1 : 1;
    }
}
