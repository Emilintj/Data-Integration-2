package de.di.schema_matching;

import de.di.schema_matching.structures.CorrespondenceMatrix;
import de.di.schema_matching.structures.SimilarityMatrix;

import java.util.Arrays;

public class SecondLineSchemaMatcher {
    public CorrespondenceMatrix match(SimilarityMatrix similarityMatrix) {
        double[][] similarityScores = similarityMatrix.getMatrix();

        int numRows = similarityScores.length;
        int numCols = similarityScores[0].length;
        double[][] costMatrix = new double[numRows][numCols];

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                costMatrix[row][col] = 1.0 - similarityScores[row][col];
            }
        }

        HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(costMatrix, numRows, numCols);
        int[] optimalAssignments = hungarianAlgorithm.execute();

        int[][] correspondenceMatrix = convertAssignmentsToMatrix(optimalAssignments, numRows, numCols);
        return new CorrespondenceMatrix(correspondenceMatrix, similarityMatrix.getSourceRelation(), similarityMatrix.getTargetRelation());
    }

    private int[][] convertAssignmentsToMatrix(int[] assignments, int numRows, int numCols) {
        int[][] correspondenceMatrix = new int[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            Arrays.fill(correspondenceMatrix[row], 0);
        }
        for (int row = 0; row < assignments.length; row++) {
            if (assignments[row] >= 0 && assignments[row] < numCols) {
                correspondenceMatrix[row][assignments[row]] = 1;
            }
        }
        return correspondenceMatrix;
    }

    private class HungarianAlgorithm {
        private final double[][] costMatrix;
        private final int numRows;
        private final int numCols;
        private final int dimension;
        private final double[] labelByWorker, labelByJob;
        private final int[] minSlackWorkerByJob;
        private final double[] minSlackValueByJob;
        private final int[] matchJobByWorker, matchWorkerByJob, parentWorkerByCommittedJob;
        private final boolean[] committedWorkers;

        public HungarianAlgorithm(double[][] costMatrix, int numRows, int numCols) {
            this.numRows = numRows;
            this.numCols = numCols;
            this.dimension = Math.max(numRows, numCols);
            this.costMatrix = new double[this.dimension][this.dimension];
            for (int i = 0; i < this.dimension; i++) {
                if (i < costMatrix.length) {
                    this.costMatrix[i] = Arrays.copyOf(costMatrix[i], this.dimension);
                } else {
                    this.costMatrix[i] = new double[this.dimension];
                }
            }
            this.labelByWorker = new double[this.dimension];
            this.labelByJob = new double[this.dimension];
            this.minSlackWorkerByJob = new int[this.dimension];
            this.minSlackValueByJob = new double[this.dimension];
            this.committedWorkers = new boolean[this.dimension];
            this.parentWorkerByCommittedJob = new int[this.dimension];
            this.matchJobByWorker = new int[this.dimension];
            Arrays.fill(this.matchJobByWorker, -1);
            this.matchWorkerByJob = new int[this.dimension];
            Arrays.fill(this.matchWorkerByJob, -1);
        }

        public int[] execute() {
            reduceMatrix();
            computeInitialLabels();
            performGreedyMatch();
            int unassignedWorker = fetchUnassignedWorker();
            while (unassignedWorker < dimension) {
                initializePhase(unassignedWorker);
                executePhase();
                unassignedWorker = fetchUnassignedWorker();
            }
            int[] result = Arrays.copyOf(matchJobByWorker, numRows);
            for (int i = 0; i < result.length; i++) {
                if (result[i] >= numCols) {
                    result[i] = -1;
                }
            }
            return result;
        }

        private void reduceMatrix() {
            for (int worker = 0; worker < dimension; worker++) {
                double minValue = Double.POSITIVE_INFINITY;
                for (int job = 0; job < dimension; job++) {
                    if (costMatrix[worker][job] < minValue) {
                        minValue = costMatrix[worker][job];
                    }
                }
                for (int job = 0; job < dimension; job++) {
                    costMatrix[worker][job] -= minValue;
                }
            }
            double[] minColumnValues = new double[dimension];
            Arrays.fill(minColumnValues, Double.POSITIVE_INFINITY);
            for (int worker = 0; worker < dimension; worker++) {
                for (int job = 0; job < dimension; job++) {
                    if (costMatrix[worker][job] < minColumnValues[job]) {
                        minColumnValues[job] = costMatrix[worker][job];
                    }
                }
            }
            for (int worker = 0; worker < dimension; worker++) {
                for (int job = 0; job < dimension; job++) {
                    costMatrix[worker][job] -= minColumnValues[job];
                }
            }
        }

        private void computeInitialLabels() {
            Arrays.fill(labelByJob, Double.POSITIVE_INFINITY);
            for (int worker = 0; worker < dimension; worker++) {
                for (int job = 0; job < dimension; job++) {
                    if (costMatrix[worker][job] < labelByJob[job]) {
                        labelByJob[job] = costMatrix[worker][job];
                    }
                }
            }
        }

        private void performGreedyMatch() {
            for (int worker = 0; worker < dimension; worker++) {
                for (int job = 0; job < dimension; job++) {
                    if (matchJobByWorker[worker] == -1 && matchWorkerByJob[job] == -1
                            && costMatrix[worker][job] - labelByWorker[worker] - labelByJob[job] == 0) {
                        match(worker, job);
                    }
                }
            }
        }

        private int fetchUnassignedWorker() {
            int worker;
            for (worker = 0; worker < dimension; worker++) {
                if (matchJobByWorker[worker] == -1) {
                    break;
                }
            }
            return worker;
        }

        private void initializePhase(int worker) {
            Arrays.fill(committedWorkers, false);
            Arrays.fill(parentWorkerByCommittedJob, -1);
            committedWorkers[worker] = true;
            for (int job = 0; job < dimension; job++) {
                minSlackValueByJob[job] = costMatrix[worker][job] - labelByWorker[worker] - labelByJob[job];
                minSlackWorkerByJob[job] = worker;
            }
        }

        private void executePhase() {
            while (true) {
                int minSlackWorker = -1, minSlackJob = -1;
                double minSlackValue = Double.POSITIVE_INFINITY;
                for (int job = 0; job < dimension; job++) {
                    if (parentWorkerByCommittedJob[job] == -1) {
                        if (minSlackValueByJob[job] < minSlackValue) {
                            minSlackValue = minSlackValueByJob[job];
                            minSlackWorker = minSlackWorkerByJob[job];
                            minSlackJob = job;
                        }
                    }
                }
                if (minSlackValue > 0) {
                    updateLabels(minSlackValue);
                }
                parentWorkerByCommittedJob[minSlackJob] = minSlackWorker;
                if (matchWorkerByJob[minSlackJob] == -1) {
                    augmentMatching(minSlackJob);
                    return;
                }
                int worker = matchWorkerByJob[minSlackJob];
                committedWorkers[worker] = true;
                for (int job = 0; job < dimension; job++) {
                    if (parentWorkerByCommittedJob[job] == -1) {
                        double slack = costMatrix[worker][job] - labelByWorker[worker] - labelByJob[job];
                        if (minSlackValueByJob[job] > slack) {
                            minSlackValueByJob[job] = slack;
                            minSlackWorkerByJob[job] = worker;
                        }
                    }
                }
            }
        }

        private void updateLabels(double slack) {
            for (int worker = 0; worker < dimension; worker++) {
                if (committedWorkers[worker]) {
                    labelByWorker[worker] += slack;
                }
            }
            for (int job = 0; job < dimension; job++) {
                if (parentWorkerByCommittedJob[job] != -1) {
                    labelByJob[job] -= slack;
                } else {
                    minSlackValueByJob[job] -= slack;
                }
            }
        }

        private void augmentMatching(int minSlackJob) {
            int committedJob = minSlackJob;
            int parentWorker;
            while (true) {
                parentWorker = parentWorkerByCommittedJob[committedJob];
                int temp = matchJobByWorker[parentWorker];
                match(parentWorker, committedJob);
                committedJob = temp;
                if (committedJob == -1) {
                    break;
                }
            }
        }

        private void match(int worker, int job) {
            matchJobByWorker[worker] = job;
            matchWorkerByJob[job] = worker;
        }
    }
}
