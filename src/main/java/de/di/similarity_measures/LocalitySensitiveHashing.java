package de.di.similarity_measures;

import de.di.similarity_measures.helper.MinHash;
import de.di.similarity_measures.helper.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class LocalitySensitiveHashing implements SimilarityMeasure {

    // The tokenizer that is used to transform string inputs into token lists.
    private final Tokenizer tokenizer;

    // A flag indicating whether the Jaccard algorithm should use set or bag semantics for the similarity calculation.
    private final boolean bagSemantics;

    // The MinHash functions that are used to calculate the LSH signatures.
    private final List<MinHash> minHashFunctions;

    public LocalitySensitiveHashing(final Tokenizer tokenizer, final boolean bagSemantics, final int numHashFunctions) {
        assert(tokenizer.getTokenSize() >= numHashFunctions);

        this.tokenizer = tokenizer;
        this.bagSemantics = bagSemantics;
        this.minHashFunctions = new ArrayList<>(numHashFunctions);
        for (int i = 0; i < numHashFunctions; i++)
            this.minHashFunctions.add(new MinHash(i));
    }

    /**
     * Calculates the LSH similarity of the two input strings.
     * The LHS algorithm calculates the LHS signatures by first tokenizing the input strings and then applying its
     * internal MinHash functions to the tokenized strings. Then, it uses the two signatures to approximate the Jaccard
     * similarity of the two strings with their signatures by simply applying the Jaccard algorithm on the two signatures.
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The LSH similarity (= Jaccard approximation) of the two arguments.
     */
    @Override
    public double calculate(final String string1, final String string2) {
        String[] tokens1 = this.tokenizer.tokenize(string1);
        String[] tokens2 = this.tokenizer.tokenize(string2);
        return this.calculate(tokens1, tokens2);
    }

    /**
     * Calculates the LSH similarity of the two input string arrays.
     * The LSH algorithm calculates the LSH signatures by applying its internal MinHash functions to the two input string
     * lists. Then, it uses the two signatures to approximate the Jaccard similarity of the two strings with their
     * signatures by simply applying the Jaccard algorithm on the two signatures.
     * @param tokens1 The first string argument for the similarity calculation.
     * @param tokens2 The second string argument for the similarity calculation.
     * @return The LSH similarity (= Jaccard approximation) of the two arguments.
     */
    @Override
    public double calculate(final String[] strings1, final String[] strings2) {
        double lshJaccard = 0;

        String[] signature1 = calculateMinHashSignatures(strings1);
        String[] signature2 = calculateMinHashSignatures(strings2);
        Jaccard jaccard = new Jaccard(this.tokenizer, this.bagSemantics);
        lshJaccard = jaccard.calculate(signature1,signature2);
        return lshJaccard;
    }

    private String[] calculateMinHashSignatures(final String[] strings) {
        String[] signatures = new String[this.minHashFunctions.size()];

        for (int i = 0; i < this.minHashFunctions.size(); i++) {
            signatures[i] = this.minHashFunctions.get(i).hash(strings);
        }

        return signatures;
    }
}
