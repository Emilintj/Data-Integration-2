package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.IND;

import java.util.*;
import java.util.stream.Collectors;
public class INDProfiler {

    /**
     * Discovers all non-trivial unary (and n-ary) inclusion dependencies in the provided relations.
     * @param relations The relations that should be profiled for inclusion dependencies.
     * @param discoverNary A flag to indicate whether to discover only unary or both unary and n-ary INDs.
     * @return The list of all non-trivial unary (and n-ary) inclusion dependencies in the provided relations.
     */
    public List<IND> profile(List<Relation> relations, boolean discoverNary) {
        if (discoverNary) {
            throw new RuntimeException("Sorry, n-ary IND discovery is not supported by this solution.");
        }

        List<IND> inclusionDependencies = new ArrayList<>();

        for (Relation relation1 : relations) {
            String[][] columns1 = relation1.getColumns();
            for (int i = 0; i < columns1.length; i++) {
                Set<String> column1Values = Arrays.stream(columns1[i])
                        .map(String::trim)
                        .filter(val -> !val.isEmpty())
                        .collect(Collectors.toSet());

                // Skip if column1Values is empty
                if (column1Values.isEmpty()) {
                    continue;
                }

                for (Relation relation2 : relations) {
                    String[][] columns2 = relation2.getColumns();
                    for (int j = 0; j < columns2.length; j++) {
                        // Skip trivial inclusion dependencies (column includes itself)
                        if (relation1 == relation2 && i == j) {
                            continue;
                        }

                        Set<String> column2Values = Arrays.stream(columns2[j])
                                .map(String::trim)
                                .filter(val -> !val.isEmpty())
                                .collect(Collectors.toSet());

                        if (column2Values.containsAll(column1Values)) {
                            inclusionDependencies.add(new IND(relation1, i, relation2, j));
                        }
                    }
                }
            }
        }

        return inclusionDependencies;
    }
}
