package com.sinha.id3.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Utility class containing methods for generating trees
 */
public class TreeUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(TreeUtility.class);

	// Generates tree by using split value = mid value of attribute values
	// Involves recursive calls to same method for constructing child nodes once
	// split attribute and value is determined
	public static Node generateTreeMidSplit(List<String> attributes, List<InputSet> inputList, Node prevNode,
			String direction, int maxDepth, int curDepth) {

		// when generating child node, considered leaf node if attributes size = 1 or
		// depth = maxDepth
		boolean isLeafNode = attributes.size() == 1 || curDepth == maxDepth;

		// handle corner case when after splitting, child node has only 1 instance of
		// attribute values left to classify. In this case, classification is performed
		// instead of determining split attribute and value
		if (inputList.size() == 1) {

			// Direction required to perform classification for parent (left/right)
			if (Constants.DIRECTION_LEFT.equals(direction)) {
				prevNode.setLeftClassification(String.valueOf(inputList.get(0).getClassValue()));
			} else {
				prevNode.setRightClassification(String.valueOf(inputList.get(0).getClassValue()));
			}
			return null;
		}

		// Determine possible class values from input data set(in this case, 1,2,3)
		// May change for child nodes
		List<Number> possibleClassValues = Utility.getClassValues(inputList);
		if (possibleClassValues.size() == 1) {
			if (null == prevNode) {

				// Case when current node is root node and only 1 possible class value is
				// present in data set, no classification is needed
				LOGGER.error("No classification needed");
				System.exit(-1);
			}

			// Since only 1 possible class value present, assign class value to parent node
			// based on direction
			if (Constants.DIRECTION_LEFT.equals(direction)) {
				prevNode.setLeftClassification(String.valueOf(possibleClassValues.get(0)));
			} else {
				prevNode.setRightClassification(String.valueOf(possibleClassValues.get(0)));
			}
			return null;
		}

		// Determine split values for available attributes
		Map<String, Number> splitValues = Utility.getSplitValuesByMid(inputList, attributes);
		LOGGER.debug("Obtained Split Map: {}", splitValues);

		// Partition input data set into attribute:(class:List<values>) pairs
		// allows for easier manipulation of data set
		Map<String, Map<Number, List<Number>>> partitionedMap = Utility.partitionMap(inputList, attributes);

		// Determine Information Gain for each split value of attributes obtained above
		Map<Double, String> gainResults = EntropyUtil.gainForSplits(splitValues, partitionedMap);
		LOGGER.debug("Gain Results: {}", gainResults);
		if (MapUtils.isEmpty(gainResults)
				|| (gainResults.size() == 1 && gainResults.keySet().iterator().next() == -1d)) {

			// If gain results empty or only attribute present in gain result & split value
			// = -1, perform classification for parent node according to direction. If more
			// than one possible class values present, classification assigned randomly
			// using ThreadLocalRandom
			if (Constants.DIRECTION_LEFT.equals(direction)) {
				prevNode.setLeftClassification(
						String.valueOf(ThreadLocalRandom.current().nextInt(possibleClassValues.size())));
			} else {
				prevNode.setRightClassification(
						String.valueOf(ThreadLocalRandom.current().nextInt(possibleClassValues.size())));
			}
			return null;
		}

		// Determine Max Information Gain from gainResults map
		double maxGain = Collections.max(gainResults.keySet());
		LOGGER.debug("Split Variable: {}, Gain: {}", gainResults.get(maxGain), maxGain);

		// Determine corresponding attribute
		String splitAttr = gainResults.get(maxGain).split(":")[0];

		// Determine split value to use
		double splitVal = Double.valueOf(gainResults.get(maxGain).split(":")[1]);

		// Prepare Node by populating values
		Node node = NodeUtility.prepareNode(splitAttr, splitVal, inputList, attributes);

		// Obtain input set for left child by splitting data set on split attribute and
		// split value. Values <= splitValue
		List<InputSet> leftInputSet = inputList.stream()
				.filter(input -> input.getAttributeValues().get(splitAttr).doubleValue() <= splitVal)
				.collect(Collectors.toList());

		// Obtain input set for right child by splitting data set on split attribute and
		// split value. Values > splitValue
		List<InputSet> rightInputSet = inputList.stream()
				.filter(input -> input.getAttributeValues().get(splitAttr).doubleValue() > splitVal)
				.collect(Collectors.toList());
		LOGGER.debug("Left Input Set Count: {}", leftInputSet.size());
		LOGGER.debug("Right Input Set Count: {}", rightInputSet.size());
		if (!isLeafNode) {

			// If current node is not leaf node, generate left and right child
			// nodes.Recursive call to generateTreeMidSplit
			List<String> leftAttributes = new ArrayList<>(attributes);
			List<String> rightAttributes = new ArrayList<>(attributes);
			LOGGER.debug("Attributes Size: {}", attributes.size());
			node.setLeftNode(generateTreeMidSplit(leftAttributes, leftInputSet, node, Constants.DIRECTION_LEFT,
					maxDepth, new Integer(curDepth) + 1));
			node.setRightNode(generateTreeMidSplit(rightAttributes, rightInputSet, node, Constants.DIRECTION_RIGHT,
					maxDepth, new Integer(curDepth) + 1));
		} else {

			// Current Node is leaf node. perform classification on left and right children
			// nodes
			LOGGER.debug("Classifying Node");
			NodeUtility.performClassification(node, leftInputSet, rightInputSet, attributes);
		}

		// Returned to parent node
		return node;
	}

	// Generate tree by using greedy search for split value. Sort attribute values
	// and determined mid points between them. Determine InfoGain for each possible
	// split value. Use split value with highest information gain to perform split.
	// Recursively call same method to generate left and right child nodes
	public static Node generateTreeGreedySplit(List<String> attributes, List<InputSet> inputList, Node prevNode,
			String direction, ExecutorService executor) {

		// Partition input data set into attribute:(class:List<values>) pairs.
		// allows for easier manipulation of data set
		Map<String, Map<Number, List<Number>>> partitionedMap = Utility.partitionMap(inputList, attributes);

		// assign leaf node if only attribute present
		boolean isLeafNode = attributes.size() == 1;
		if (inputList.size() == 1) {

			// handle corner case when after splitting, child node has only 1 instance of
			// attribute values left to classify. In this case, classification is performed
			// instead of determining split attribute and value
			if (Constants.DIRECTION_LEFT.equals(direction)) {
				prevNode.setLeftClassification(String.valueOf(inputList.get(0).getClassValue()));
			} else {
				prevNode.setRightClassification(String.valueOf(inputList.get(0).getClassValue()));
			}
			return null;
		}

		// Determine possible class values
		List<Number> possibleClassValues = Utility.getClassValues(inputList);
		if (possibleClassValues.size() == 1) {
			if (null == prevNode) {

				// Case when current node is root node and only 1 possible class value is
				// present in data set, no classification is needed
				LOGGER.error("No classification needed");
				System.exit(-1);
			}

			// Since only 1 possible class value present, assign class value to parent node
			// based on direction
			if (Constants.DIRECTION_LEFT.equals(direction)) {
				prevNode.setLeftClassification(String.valueOf(possibleClassValues.get(0)));
			} else {
				prevNode.setRightClassification(String.valueOf(possibleClassValues.get(0)));
			}
			return null;
		}
		LOGGER.debug("Obtained Partitioned Map: {}", partitionedMap);

		// Prepare callables to be used for determining split attribute and split value
		// in parallel
		List<Callable<Map<Double, String>>> callables = prepareCallables(partitionedMap);

		// Submit callables to ExecutorService and obtain results
		Map<Double, String> gainResults = performTask(callables, executor);
		LOGGER.debug("Gain Results: {}", gainResults);
		if (MapUtils.isEmpty(gainResults)
				|| (gainResults.size() == 1 && gainResults.keySet().iterator().next() == -1d)) {

			// If gain results empty or only attribute present in gain result & split value
			// = -1, perform classification for parent node according to direction. Logic
			// further explained in EntropyUtil class
			if (Constants.DIRECTION_LEFT.equals(direction)) {
				prevNode.setLeftClassification(
						String.valueOf(ThreadLocalRandom.current().nextInt(possibleClassValues.size())));
			} else {
				prevNode.setRightClassification(
						String.valueOf(ThreadLocalRandom.current().nextInt(possibleClassValues.size())));
			}
			return null;
		}

		// Determine Max Information Gain from gainResults map
		double maxGain = Collections.max(gainResults.keySet());
		LOGGER.debug("Split Variable: {}, Gain: {}", gainResults.get(maxGain), maxGain);

		// Determine corresponding attribute
		String splitAttr = gainResults.get(maxGain).split(":")[0];

		// Determine split value to use
		double splitVal = Double.valueOf(gainResults.get(maxGain).split(":")[1]);

		// Prepare node by populating values
		Node node = NodeUtility.prepareNode(splitAttr, splitVal, inputList, attributes);

		// Obtain input set for left child by splitting data set on split attribute and
		// split value. Values <= splitValue
		List<InputSet> leftInputSet = inputList.stream()
				.filter(input -> input.getAttributeValues().get(splitAttr).doubleValue() <= splitVal)
				.collect(Collectors.toList());

		// Obtain input set for right child by splitting data set on split attribute and
		// split value. Values > splitValue
		List<InputSet> rightInputSet = inputList.stream()
				.filter(input -> input.getAttributeValues().get(splitAttr).doubleValue() > splitVal)
				.collect(Collectors.toList());
		LOGGER.debug("Left Input Set Count: {}", leftInputSet.size());
		LOGGER.debug("Right Input Set Count: {}", rightInputSet.size());
		if (!isLeafNode) {

			// If current node is not leaf node, generate left and right child
			// nodes.Recursive call to generateTreeGreedySplit
			List<String> leftAttributes = new ArrayList<>(attributes);
			List<String> rightAttributes = new ArrayList<>(attributes);
			LOGGER.debug("Attributes Size: {}", attributes.size());
			node.setLeftNode(
					generateTreeGreedySplit(leftAttributes, leftInputSet, node, Constants.DIRECTION_LEFT, executor));
			node.setRightNode(
					generateTreeGreedySplit(rightAttributes, rightInputSet, node, Constants.DIRECTION_RIGHT, executor));
		} else {

			// Current Node is leaf node. perform classification on left and right children
			// nodes
			LOGGER.debug("Classifying Node");
			NodeUtility.performClassification(node, leftInputSet, rightInputSet, attributes);
		}

		// Returned to parent node
		return node;
	}

	// Submits callables to executor service. Waits for execution to finish and then
	// returns results
	private static Map<Double, String> performTask(List<Callable<Map<Double, String>>> callables,
			ExecutorService executor) {
		Map<Double, String> resultMap = new HashMap<>();
		List<Future<Map<Double, String>>> futures = new ArrayList<>();
		for (Callable<Map<Double, String>> callable : callables) {
			futures.add(executor.submit(callable));
		}
		for (Future<Map<Double, String>> future : futures) {
			try {
				resultMap.putAll(future.get());
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

	// Prepare callables to be used for determining InformationGain for attributes
	// in greedy split search
	private static List<Callable<Map<Double, String>>> prepareCallables(
			Map<String, Map<Number, List<Number>>> partitionedMap) {
		List<Callable<Map<Double, String>>> callables = new ArrayList<>();
		for (Map.Entry<String, Map<Number, List<Number>>> entry : partitionedMap.entrySet()) {

			// Possible Class Values and attribute values for each attribute submitted to a
			// callable
			InformationGainCalculator cal = new InformationGainCalculator(entry.getKey(), entry.getValue());
			callables.add(cal);
		}
		return callables;
	}
}
