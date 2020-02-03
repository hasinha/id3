package com.sinha.id3.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Class for calculating entropy and information gains
public class EntropyUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntropyUtil.class);

	// Returns entropy for given attributes and values
	public static double calculateEntropy(Map<Number, List<Number>> columnValues) {
		int totalCount = columnValues.values().stream().mapToInt(i -> i.size()).sum();
		double initialEntropy = 0d;
		for (Map.Entry<Number, List<Number>> entry : columnValues.entrySet()) {
			LOGGER.debug("Calculating entropy for class: {}", entry.getKey());
			LOGGER.debug("Count for class: {}", entry.getValue().size());
			if (CollectionUtils.isEmpty(entry.getValue())) {
				LOGGER.debug("No instances for class. Continue");
				continue;
			}

			// Entropy for each class value
			// (-1 * count/totalCount * logBase2(count/totalCount))
			double classEntropy = (-1 * (double) entry.getValue().size() / (double) totalCount)
					* logBase2((double) entry.getValue().size() / (double) totalCount);

			// Added to initialEntropy
			initialEntropy += classEntropy;
		}
		LOGGER.debug("Entropy for columnValues: {}", initialEntropy);
		return initialEntropy;
	}

	private static double logBase2(double value) {
		return Math.log(value) / Math.log(2);
	}

	// Returns Information Gain for splitValues specified in splitValues variable on
	// partitionedMap data set
	public static Map<Double, String> gainForSplits(Map<String, Number> splitValues,
			Map<String, Map<Number, List<Number>>> partitionedMap) {
		Map<Double, String> gainsForAttributes = new HashMap<>();

		// Iterate over each attribute entry
		for (Map.Entry<String, Number> entry : splitValues.entrySet()) {

			// Get possible class values and associated attribute values
			Map<Number, List<Number>> classValuesForAttr = partitionedMap.get(entry.getKey());

			// Determine entropy before splitting
			double entropyBeforeSplit = EntropyUtil.calculateEntropy(classValuesForAttr);

			// Determine Information Gain for split value for attribute
			Map<Double, String> gains = gainBySplit(classValuesForAttr,
					new ArrayList<>(Arrays.asList(entry.getValue())), entropyBeforeSplit);

			// Obtain InfoGain value
			double gain = gains.keySet().iterator().next();

			// Add to result map
			gainsForAttributes.put(gain, entry.getKey() + ":" + String.valueOf(entry.getValue()));
		}
		return gainsForAttributes;
	}

	// Determine Info Gain for each split value in splitValues variable on valuesMap
	// data set given initial entropy = initEntropy
	public static Map<Double, String> gainBySplit(Map<Number, List<Number>> valuesMap, List<Number> splitValues,
			double initEntropy) {
		Map<Double, String> splitInfoGains = new HashMap<>();

		// Iterate over splitValues. Determine InfoGain for each
		for (Number splitVal : splitValues) {

			// Initialise left and right split maps
			Map<Number, List<Number>> leftMap = new HashMap<>();
			Map<Number, List<Number>> rightMap = new HashMap<>();

			// Split valuesMap using splitValue for each possible classValue -> leftMap &
			// rightMap
			for (Map.Entry<Number, List<Number>> entry : valuesMap.entrySet()) {
				List<Number> leftEntry = entry.getValue().stream()
						.filter(x -> x.doubleValue() <= splitVal.doubleValue()).collect(Collectors.toList());
				if (leftMap.containsKey(entry.getKey())) {
					leftMap.get(entry.getKey()).addAll(leftEntry);
				} else {
					leftMap.put(entry.getKey(), leftEntry);
				}
				List<Number> rightEntry = entry.getValue().stream()
						.filter(x -> x.doubleValue() > splitVal.doubleValue()).collect(Collectors.toList());
				if (rightMap.containsKey(entry.getKey())) {
					rightMap.get(entry.getKey()).addAll(rightEntry);
				} else {
					rightMap.put(entry.getKey(), rightEntry);
				}
			}

			// Calculate entropy for left & right maps
			double leftEntropy = EntropyUtil.calculateEntropy(leftMap);
			double rightEntropy = EntropyUtil.calculateEntropy(rightMap);

			// Get counts in left & right maps
			double leftCount = (double) leftMap.values().stream().flatMap(input -> input.stream()).count();
			double rightCount = (double) rightMap.values().stream().flatMap(input -> input.stream()).count();

			// Determine InfoGain
			double infoGain = calculateInfoGain(initEntropy, leftEntropy, rightEntropy, leftCount, rightCount);

			// Add to result map
			splitInfoGains.put(infoGain, String.valueOf(splitVal));
		}
		return splitInfoGains;
	}

	// Calculate InfoGain using supplied variables
	private static double calculateInfoGain(double initEntropy, double leftEntropy, double rightEntropy,
			double leftCount, double rightCount) {
		double totalCount = leftCount + rightCount;
		double weightedEntropy = ((leftCount / totalCount) * leftEntropy) + ((rightCount / totalCount) * rightEntropy);
		double infoGain = initEntropy - weightedEntropy;
		return infoGain;
	}
}
