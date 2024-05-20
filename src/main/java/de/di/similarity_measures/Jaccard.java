package de.di.similarity_measures;

import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class Jaccard implements SimilarityMeasure {

    // The tokenizer that is used to transform string inputs into token lists.
    private final Tokenizer tokenizer;

    // A flag indicating whether the Jaccard algorithm should use set or bag semantics for the similarity calculation.
    private final boolean bagSemantics;

    /**
     * Calculates the Jaccard similarity of the two input strings. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String string1, String string2) {
        string1 = (string1 == null) ? "" : string1;
        string2 = (string2 == null) ? "" : string2;

        String[] strings1 = this.tokenizer.tokenize(string1);
        String[] strings2 = this.tokenizer.tokenize(string2);
        return this.calculate(strings1, strings2);
    }

    /**
     * Calculates the Jaccard similarity of the two string lists. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String[] strings1, String[] strings2) {
        if (bagSemantics) {
            // We need to consider the frequency of elements for bag semantics
            Map<String, Integer> frequencyMap1 = new HashMap<>();
            for (String s : strings1) {
                frequencyMap1.put(s, frequencyMap1.getOrDefault(s, 0) + 1);
            }

            Map<String, Integer> frequencyMap2 = new HashMap<>();
            for (String s : strings2) {
                frequencyMap2.put(s, frequencyMap2.getOrDefault(s, 0) + 1);
            }

            // By considering the minimum frequency of each element, calculate the intersection size
            int intersectionSize = 0;
            for (String key : frequencyMap1.keySet()) {
                if (frequencyMap2.containsKey(key)) {
                    intersectionSize += Math.min(frequencyMap1.get(key), frequencyMap2.get(key));
                }
            }

            // By considering the maximum frequency of each element, calculate the union size
            int unionSize = 0;
            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(frequencyMap1.keySet());
            allKeys.addAll(frequencyMap2.keySet());
            for (String key : allKeys) {
                unionSize += Math.max(frequencyMap1.getOrDefault(key, 0), frequencyMap2.getOrDefault(key, 0));
            }

            return (double) intersectionSize / (strings1.length + strings2.length);
        } else {
            // We only consider unique elements for set semantics
            Set<String> set1 = new HashSet<>(Arrays.asList(strings1));
            Set<String> set2 = new HashSet<>(Arrays.asList(strings2));

            Set<String> intersection = new HashSet<>(set1);
            intersection.retainAll(set2); //  Calculating the Intersection

            Set<String> union = new HashSet<>(set1);
            union.addAll(set2); // Calculating the Union

            return (double) intersection.size() / union.size();
        }
    }
}
