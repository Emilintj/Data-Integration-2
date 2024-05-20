package de.di.similarity_measures;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public class Levenshtein implements SimilarityMeasure {

    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    // The choice of whether Levenshtein or DamerauLevenshtein should be calculated.
    private final boolean withDamerau;

    /**
     * Calculates the Levenshtein similarity of the two input strings.
     * The Levenshtein similarity is defined as "1 - normalized Levenshtein distance".
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The (Damerau) Levenshtein similarity of the two arguments.
     */
    @Override
    public double calculate(final String string1, final String string2) {
        double levenshteinSimilarity = 0.0;

        // Converting strings to arrays of single-character strings
        String[] strings1 = string1.chars().mapToObj(c -> String.valueOf((char) c)).toArray(String[]::new);
        String[] strings2 = string2.chars().mapToObj(c -> String.valueOf((char) c)).toArray(String[]::new);

        // Call the string[] calculate method and reuse the logic
        levenshteinSimilarity = calculate(strings1, strings2);
        return levenshteinSimilarity;
    }

    /**
     * Calculates the Levenshtein similarity of the two input string lists.
     * The Levenshtein similarity is defined as "1 - normalized Levenshtein distance".
     * For string lists, we consider each list as an ordered list of tokens and calculate the distance as the number of
     * token insertions, deletions, replacements (and swaps) that transform one list into the other.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The (multiset) Levenshtein similarity of the two arguments.
     */
    @Override
    public double calculate(final String[] strings1, final String[] strings2) {
        double levenshteinSimilarity = 0.0;

        int m = strings1.length;
        int n = strings2.length;

        int[] upperupperLine = new int[m + 1];   // line for Damerau lookups
        int[] upperLine = new int[m + 1];        // line for regular Levenshtein lookups
        int[] lowerLine = new int[m + 1];        // line to be filled next by the algorithm

        for (int i = 0; i <= m; i++) {
            upperLine[i] = i;
        }

        for (int j = 1; j <= n; j++) {
            lowerLine[0] = j;
            for (int i = 1; i <= m; i++) {
                int cost = (strings1[i - 1].equals(strings2[j - 1])) ? 0 : 1;
                lowerLine[i] = min(lowerLine[i - 1] + 1, // insertion
                        upperLine[i] + 1,     // deletion
                        upperLine[i - 1] + cost); // substitution
                if (withDamerau && i > 1 && j > 1 &&
                        strings1[i - 1].equals(strings2[j - 2]) &&
                        strings1[i - 2].equals(strings2[j - 1])) {
                    lowerLine[i] = min(lowerLine[i], upperupperLine[i - 2] + cost);
                }
            }
            System.arraycopy(upperLine, 0, upperupperLine, 0, m + 1);
            System.arraycopy(lowerLine, 0, upperLine, 0, m + 1);
        }

        double levenshteinDistance = upperLine[m];
        levenshteinSimilarity = 1.0 - (levenshteinDistance / Math.max(m, n));
        return levenshteinSimilarity;
}}
