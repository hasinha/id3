package com.sinha.id3.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.habernal.confusionmatrix.ConfusionMatrix;
import com.google.common.collect.Lists;

import be.cylab.java.roc.Roc;
import be.cylab.java.roc.RocCoordinates;
import be.cylab.java.roc.Utils;

/*
 * Utility class for forest evaluation
 */
public class ForestUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForestUtils.class);

	// Builds forest using supplied variables. Estimators to create, attributes to
	// use, attribute size for each tree, no of folds to evaluate on and input data
	// set
	public static void evaluateForest(List<InputSet> inputList, List<String> attributes, int estimators,
			int attributeSize, int kFold, ExecutorService executor, ExecutorService treeExecutor) {

		// Partition data set according to kFold parameter
		int partitionSize = (int) Math.ceil(((double) attributes.size() / (double) kFold));
		List<List<InputSet>> partitionedList = Lists.partition(inputList, partitionSize);
		int i = 0;

		// Confusion matrix for measuring accuracy, precision etc
		ConfusionMatrix cm = new ConfusionMatrix();

		// Used to plot ROC curve
		List<Double> scores = new ArrayList<>();
		List<Boolean> truths = new ArrayList<>();

		// Perform evaluation according to k-fold parameter
		while (i < partitionedList.size()) {
			List<List<InputSet>> tempList = new ArrayList<>(partitionedList);

			// Create training and test set
			List<InputSet> testingSet = tempList.remove(i);
			List<InputSet> trainingSet = tempList.stream().flatMap(list -> list.stream()).collect(Collectors.toList());

			// Generate forest
			List<Node> forest = generateForest(trainingSet, attributes, estimators, attributeSize, executor,
					treeExecutor);

			// Perform evaluation on forest using test set
			evaluateTestOnForest(forest, testingSet, cm, scores, truths);
			i++;
		}

		// Print evaluation statistics
		CrossFoldValidation.printStatistics(cm, "Forest");

		// Generate ROC curve
		generateRoc(scores, truths);
	}

	// Generate Forest
	private static List<Node> generateForest(List<InputSet> inputList, List<String> attributes, int numEstimations,
			int attributeSize, ExecutorService executor, ExecutorService treeExecutor) {
		List<Callable<Node>> callables = prepareForestCallables(inputList, attributes, numEstimations, attributeSize,
				treeExecutor);
		List<Node> forestNodes = performForestTask(callables, executor);
		return forestNodes;
	}

	// Generate ROC curve using scores and truth values
	private static void generateRoc(List<Double> scores, List<Boolean> truths) {
		double[] scoreArr = getScoreArr(scores);
		boolean[] truthArr = getTruthArr(truths);
		Roc roc = new Roc(scoreArr, truthArr);
		LOGGER.info("ROC AUC: {}", roc.computeAUC());
		List<RocCoordinates> roc_coordinates = roc.computeRocPointsAndGenerateCurve("./Roc_curve.png");
		Utils.storeRocCoordinatesInCSVFile(roc_coordinates, "./roc.csv");
	}

	// Prepare score array from list of scores
	private static double[] getScoreArr(List<Double> scores) {
		double[] scoreArr = new double[scores.size()];
		for (int i = 0; i < scoreArr.length; i++) {
			scoreArr[i] = scores.get(i).doubleValue();
		}
		return scoreArr;
	}

	// Prepare truth array from list of truths
	private static boolean[] getTruthArr(List<Boolean> truths) {
		boolean[] truthArr = new boolean[truths.size()];
		for (int i = 0; i < truthArr.length; i++) {
			truthArr[i] = truths.get(i);
		}
		return truthArr;
	}

	// Perform evaluation on generated forest using test set
	private static void evaluateTestOnForest(List<Node> forest, List<InputSet> testSet, ConfusionMatrix cm,
			List<Double> scores, List<Boolean> truths) {
		for (InputSet input : testSet) {
			Map<String, Integer> decisionMap = new HashMap<>();
			for (Node node : forest) {
				String nodeClass = CrossFoldValidation.predict(node, input.getAttributeValues());
				if (decisionMap.containsKey(nodeClass)) {
					decisionMap.put(nodeClass, decisionMap.get(nodeClass) + 1);
				} else {
					decisionMap.put(nodeClass, 1);
				}
			}
			String predClass = getFinalDecisionClass(decisionMap);
			if (null != cm) {
				cm.increaseValue(String.valueOf(input.getClassValue()), predClass, 1);
			}
			populateScoresAndTruths(scores, truths, input, decisionMap, predClass);
		}
	}

	// Store scores for evaluation
	private static void populateScoresAndTruths(List<Double> scores, List<Boolean> truths, InputSet input,
			Map<String, Integer> decisionMap, String predClass) {
		double positive = 0d;
		double total = 0d;
		for (Map.Entry<String, Integer> entry : decisionMap.entrySet()) {
			if (entry.getKey().equals(String.valueOf(input.getClassValue()))) {
				positive = (double) entry.getValue();
			}
			total += (double) entry.getValue();
		}
		scores.add(positive / total);
		truths.add(predClass.equalsIgnoreCase(String.valueOf(input.getClassValue())));
	}

	// Returns predicted class based on majority votes from trees
	private static String getFinalDecisionClass(Map<String, Integer> decisionMap) {
		return decisionMap.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
				.get().getKey();
	}

	// Prepare callables for tree generation
	private static List<Callable<Node>> prepareForestCallables(List<InputSet> inputList, List<String> attributes,
			int numEstimations, int attributeSize, ExecutorService executor) {
		List<Callable<Node>> callables = new ArrayList<>();
		int i = 0;
		while (i < numEstimations) {
			TreeGenerator generator = new TreeGenerator(new ArrayList<>(attributes), inputList, attributeSize,
					executor);
			callables.add(generator);
			i++;
		}
		return callables;
	}

	// Process callables in parallel using ExecutorService
	private static List<Node> performForestTask(List<Callable<Node>> callables, ExecutorService executor) {
		List<Node> resultMap = new ArrayList<>();
		List<Future<Node>> futures = new ArrayList<>();
		for (Callable<Node> callable : callables) {
			futures.add(executor.submit(callable));
		}
		for (Future<Node> future : futures) {
			try {
				resultMap.add(future.get());
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Exception calculating information gain: ", e);
				LOGGER.error("Exit");
				future.cancel(true);
				executor.shutdown();
				System.exit(-1);
			}
		}
		return resultMap;
	}
}
