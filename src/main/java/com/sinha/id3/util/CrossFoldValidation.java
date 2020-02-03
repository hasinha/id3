package com.sinha.id3.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.habernal.confusionmatrix.ConfusionMatrix;
import com.google.common.collect.Lists;
import com.sinha.id3.ClassMetrics;

/*
 * Class containing methods for performing K-Fold Cross Validation
 */
public class CrossFoldValidation {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrossFoldValidation.class);

	// Perform k-Fold cross validation for greedy split search tree
	public static void kFoldCrossEvalGreedy(List<String> attributes, List<InputSet> inputList, int k,
			ExecutorService executor) throws IOException {
		LOGGER.info("Performing {}-Fold Cross-Validation Greedy Split", k);

		// Determine partition size for test set
		int partitionSize = (int) Math.ceil(((double) attributes.size() / (double) k));

		// Partition data set according partition size
		List<List<InputSet>> partitionedList = Lists.partition(inputList, partitionSize);
		int i = 0;

		// Initialise confusion matrix used to measure accuracy etc.
		ConfusionMatrix cm = new ConfusionMatrix();

		// ClassMetrics used to count FP, FN, TP and TN
		ClassMetrics class1 = new ClassMetrics("1");
		ClassMetrics class2 = new ClassMetrics("2");
		ClassMetrics class3 = new ClassMetrics("3");
		while (i < partitionedList.size()) {
			List<List<InputSet>> tempList = new ArrayList<>(partitionedList);

			// Obtain training and test set
			List<InputSet> testingSet = tempList.remove(i);
			List<InputSet> trainingSet = tempList.stream().flatMap(list -> list.stream()).collect(Collectors.toList());

			// Generate Tree
			Node rootNode = TreeUtility.generateTreeGreedySplit(new ArrayList<>(attributes), trainingSet, null, null,
					executor);

			// Evaluate test set on generated tree
			evaluateOnSet(rootNode, testingSet, cm, class1, class2, class3);
			i++;
		}

		// Plot ROC Curve for each class value using FPR and TPR
		new PlotGraph("Class 1", class1, "Greedy Split");
		new PlotGraph("Class 2", class2, "Greedy Split");
		new PlotGraph("Class 3", class3, "Greedy Split");

		// Plot ROC curves for all classes together
		new AreaChart(class1, class2, class3, "Greedy Split");

		// Print Accuracy etc using confusion matrix library
		printStatistics(cm, "ID3 Greedy Split");
	}

	// Perform k-Fold cross validation for mid split tree
	public static void kFoldCrossEvalOnMid(List<String> attributes, List<InputSet> inputList, int k, int maxDepth)
			throws IOException {
		LOGGER.info("Performing {}-Fold Cross-Validation Mode Split", k);

		// Determine partition size for test set
		int partitionSize = (int) Math.ceil(((double) attributes.size() / (double) k));
		List<List<InputSet>> partitionedList = Lists.partition(inputList, partitionSize);
		int i = 0;

		// Initialise confusion matrix used to measure accuracy etc.
		ConfusionMatrix cm = new ConfusionMatrix();

		// ClassMetrics used to count FP, FN, TP and TN
		ClassMetrics class1 = new ClassMetrics("1");
		ClassMetrics class2 = new ClassMetrics("2");
		ClassMetrics class3 = new ClassMetrics("3");
		while (i < partitionedList.size()) {
			List<List<InputSet>> tempList = new ArrayList<>(partitionedList);

			// Obtain training and test set
			List<InputSet> testingSet = tempList.remove(i);
			List<InputSet> trainingSet = tempList.stream().flatMap(list -> list.stream()).collect(Collectors.toList());

			// Generate Tree
			Node rootNode = TreeUtility.generateTreeMidSplit(new ArrayList<>(attributes), trainingSet, null, null,
					maxDepth, 1);

			// Evaluate test set on generated tree
			evaluateOnSet(rootNode, testingSet, cm, class1, class2, class3);
			i++;
		}

		// Plot ROC Curve for each class value using FPR and TPR
		new PlotGraph("Class 1", class1, "Mode Split w Max Depth " + maxDepth);
		new PlotGraph("Class 2", class2, "Mode Split w Max Depth " + maxDepth);
		new PlotGraph("Class 3", class3, "Mode Split w Max Depth " + maxDepth);

		// Plot ROC curves for all classes together
		new AreaChart(class1, class2, class3, "Mode Split w Max Depth " + maxDepth);

		// Print Accuracy etc using confusion matrix library
		printStatistics(cm, "ID3 Mode Split(" + maxDepth + ")");
	}

	// Evaluate tree on test set specified
	private static void evaluateOnSet(Node rootNode, List<InputSet> inputList, ConfusionMatrix confusionMatrix,
			ClassMetrics class1, ClassMetrics class2, ClassMetrics class3) {
		for (InputSet input : inputList) {

			// Get prediction for given input values from test set
			String predClass = predict(rootNode, input.getAttributeValues());
			if (null != confusionMatrix) {

				// Increment values for confusion matrix
				confusionMatrix.increaseValue(String.valueOf(input.getClassValue()), predClass, 1);
			}

			// Increment FP, TP, TN, FN counts as necessary for each class value
			updateCounts(class1, predClass, String.valueOf(input.getClassValue()));
			updateCounts(class2, predClass, String.valueOf(input.getClassValue()));
			updateCounts(class3, predClass, String.valueOf(input.getClassValue()));
		}
	}

	private static void updateCounts(ClassMetrics classMetric, String predClass, String actualClass) {

		// if class metric is null. nothing to do. return
		if (null == classMetric) {
			return;
		}
		if (actualClass.equals(predClass)) {
			if (actualClass.equals(classMetric.getClassValue())) {

				// Increment TP count
				classMetric.tpCountPlus();
			} else {

				// Increase TN count
				classMetric.tnCountPlus();
			}
		} else {
			if (actualClass.equals(classMetric.getClassValue())) {

				// Increase FN count
				classMetric.fnCountPlus();
			}
			if (predClass.equals(classMetric.getClassValue())) {

				// Increase FP count
				classMetric.fpCountPlus();
			}
		}
	}

	// Gets predicted class for input on tree. Call recursively on child nodes based
	// on split attribute and split value until class value at leaf node reached
	public static String predict(Node rootNode, Map<String, Number> attrValues) {
		String checkAttr = rootNode.getLabel();
		Number attrVal = attrValues.get(checkAttr);
		if (null != rootNode.getLeftNode() && attrVal.doubleValue() <= rootNode.getSplitVal().doubleValue()) {
			return predict(rootNode.getLeftNode(), attrValues);
		} else if (null != rootNode.getRightNode() && attrVal.doubleValue() > rootNode.getSplitVal().doubleValue()) {
			return predict(rootNode.getRightNode(), attrValues);
		} else if (null == rootNode.getLeftNode() && attrVal.doubleValue() <= rootNode.getSplitVal().doubleValue()) {
			return rootNode.getLeftClassification();
		} else if (null == rootNode.getRightNode() && attrVal.doubleValue() > rootNode.getSplitVal().doubleValue()) {
			return rootNode.getRightClassification();
		}
		return StringUtils.EMPTY;
	}

	// Print Evaluation Statistics from Confusion Matrix
	public static void printStatistics(ConfusionMatrix cm, String id) {
		LOGGER.info("Confusion Matrix for {}: \n{}", id, cm);
		LOGGER.info("F-Measure: {}", cm.getFMeasureForLabels());
		LOGGER.info("Accuracy: {}", cm.getAccuracy());
		LOGGER.info("Precision: {}", cm.getPrecisionForLabels());
		LOGGER.info("Average Precisions: {}", cm.getAvgPrecision());
		LOGGER.info("Recall: {}", cm.getRecallForLabels());
		LOGGER.info("Average Recall: {}", cm.getAvgRecall());
		LOGGER.info("Probabilistic Dist: \n{}", cm.toStringProbabilistic());
	}
}
