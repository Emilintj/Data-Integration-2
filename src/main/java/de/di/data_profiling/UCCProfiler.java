package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.PositionListIndex;
import de.di.data_profiling.structures.UCC;

import java.util.*;

public class UCCProfiler {

    /**
     * Discovers all minimal, non-trivial unique column combinations in the provided relation.
     * @param relation The relation that should be profiled for unique column combinations.
     * @return The list of all minimal, non-trivial unique column combinations in ths provided relation.
     */
    public List<UCC> profile(Relation relation) {
        int numAttributes = relation.getAttributes().length;
        List<UCC> uniques = new ArrayList<>();
        List<PositionListIndex> currentNonUniques = new ArrayList<>();

        // Calculate all unary UCCs and unary non-UCCs
        for (int attribute = 0; attribute < numAttributes; attribute++) {
            AttributeList attributes = new AttributeList(attribute);
            PositionListIndex pli = new PositionListIndex(attributes, relation.getColumns()[attribute]);
            if (pli.isUnique())
                uniques.add(new UCC(relation, attributes));
            else
                currentNonUniques.add(pli);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Discover all unique column combinations of size n>1 by traversing the lattice level-wise. Make sure to     //
        // generate only minimal candidates while moving upwards and to prune non-minimal ones. Hint: The class       //
        // AttributeList offers some helpful functions to test for sub- and superset relationships. Use PLI           //
        // intersection to validate the candidates in every lattice level. Advances techniques, such as random walks, //
        // hybrid search strategies, or hitting set reasoning can be used, but are mandatory to pass the assignment.  //



        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        List<PositionListIndex> nextNonUniques = new ArrayList<>();
        Set<AttributeList> checkedCombinations = new HashSet<>();

        for (int size = 2; !currentNonUniques.isEmpty(); size++) {
            nextNonUniques.clear();
            for (int i = 0; i < currentNonUniques.size(); i++) {
                for (int j = i + 1; j < currentNonUniques.size(); j++) {
                    AttributeList combinedAttributes = currentNonUniques.get(i).getAttributes().union(currentNonUniques.get(j).getAttributes());
                    if (combinedAttributes.size() == size && !checkedCombinations.contains(combinedAttributes)) {
                        checkedCombinations.add(combinedAttributes);
                        String[] combinedData = combineColumns(combinedAttributes, relation);
                        PositionListIndex combinedPli = new PositionListIndex(combinedAttributes, combinedData);
                        if (combinedPli.isUnique()) {
                            if (isMinimal(combinedAttributes, uniques)) {
                                uniques.add(new UCC(relation, combinedAttributes));
                            }
                        } else {
                            nextNonUniques.add(combinedPli);
                        }
                    }
                }
            }
            currentNonUniques = new ArrayList<>(nextNonUniques);
        }

        return uniques;
    }

    private boolean isMinimal(AttributeList candidate, List<UCC> uniques) {
        for (UCC ucc : uniques) {
            if (isSuperset(candidate, ucc.getAttributeList())) {
                return false;
            }
        }
        return true;
    }

    private boolean isSuperset(AttributeList superset, AttributeList subset) {
        for (int attr : subset.getAttributes()) {
            boolean found = false;
            for (int supAttr : superset.getAttributes()) {
                if (supAttr == attr) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private String[] combineColumns(AttributeList attributes, Relation relation) {
        int numRows = relation.getColumns()[0].length;
        String[][] columns = relation.getColumns();
        String[] combinedData = new String[numRows];

        for (int row = 0; row < numRows; row++) {
            StringBuilder sb = new StringBuilder();
            for (int attribute : attributes.getAttributes()) {
                sb.append(columns[attribute][row]).append("|"); // Use a delimiter to separate values
            }
            combinedData[row] = sb.toString();
        }

        return combinedData;
    }

}
