package de.di.schema_matching;

import de.di.Relation;
import de.di.schema_matching.structures.SimilarityMatrix;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.helper.Tokenizer;

public class FirstLineSchemaMatcher {

    /**
     * Matches the attributes of the source and target table and produces a #source_attributes x #target_attributes
     * sized similarity matrix that represents the attribute-to-attribute similarities of the two relations.
     * @param sourceRelation The first relation for the matching that determines the first (= y) dimension of the
     *                       similarity matrix, i.e., double[*][].
     * @param targetRelation The second relation for the matching that determines the second (= x) dimension of the
     *                       similarity matrix, i.e., double[][*].
     * @return The similarity matrix that describes the attribute-to-attribute similarities of the two relations.
     */
    public SimilarityMatrix match(Relation sourceRelation, Relation targetRelation) {
        String[][] sourceAttributes = sourceRelation.getColumns();
        String[][] targetAttributes = targetRelation.getColumns();


        double[][] similarityScores = new double[sourceAttributes.length][targetAttributes.length];


        Tokenizer tokenizer = new Tokenizer(3, false);
        Jaccard jaccard = new Jaccard(tokenizer, false);


        for (int sourceIndex = 0; sourceIndex < sourceAttributes.length; sourceIndex++) {
            for (int targetIndex = 0; targetIndex < targetAttributes.length; targetIndex++) {
                similarityScores[sourceIndex][targetIndex] = jaccard.calculate(sourceAttributes[sourceIndex], targetAttributes[targetIndex]);
            }
        }

        return new SimilarityMatrix(similarityScores, sourceRelation, targetRelation);
    }
}
