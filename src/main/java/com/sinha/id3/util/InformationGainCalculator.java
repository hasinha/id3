package com.sinha.id3.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Calculate Information Gain for input columnValues by deciding on split
public class InformationGainCalculator implements Callable<Map<Double, String>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(InformationGainCalculator.class);

	// Attribute being considered
	private String attribute;

	// Map containing possible class values and their respective attribute values
	private Map<Number, List<Number>> columnValues;

	public InformationGainCalculator(String attribute, Map<Number, List<Number>> columnValues) {
		this.attribute = attribute;
		this.columnValues = columnValues;
	}

	// Execution begins here
	@Override
	public Map<Double, String> call() throws Exception {
		Map<Double, String> result = new HashMap<>();
		LOGGER.debug("Calculating gain for attribute: {}", attribute);

		// Determine entropy before split is performed
		double entropyBeforeSplit = EntropyUtil.calculateEntropy(columnValues);

		// Get list of all possible values for this attribute
		List<Number> valuesList = columnValues.values().stream().flatMap(input -> input.stream())
				.collect(Collectors.toList());
		LOGGER.debug("Values: {}", valuesList);

		// Removes Duplicates
		valuesList = new ArrayList<>(new HashSet<>(valuesList));

		// Sort in ascending order
		sortNumbers(valuesList);

		// Get mid points between successive attribute values. To be cosidered as
		// possible split values
		List<Number> splitValues = Utility.getSplitValues(valuesList);

		// Determine Information Gain for each split value
		Map<Double, String> splitGains = EntropyUtil.gainBySplit(columnValues, splitValues, entropyBeforeSplit);
		LOGGER.debug("Split Gains: {}", splitGains);
		if (MapUtils.isEmpty(splitGains)) {

			// If empty split gain obtained, return output as -1.0 so that other attributes
			// are considered for splitting.(Max(gain) used to determine attribute for
			// splitting)
			result.put(-1d, attribute);
			return result;
		}

		// Get split value with maximum information gain
		double maxGain = Collections.max(new ArrayList<>(splitGains.keySet()));
		String splitVal = splitGains.get(maxGain);

		// Return output as maxGain -> attribute:splitValue key value pair
		result.put(maxGain, attribute + ":" + splitVal);
		return result;
	}

	// Method to sort list of numbers. Required since normal sort methods do not
	// work with java.lang.Number class
	private static void sortNumbers(List<Number> valuesList) {
		Collections.sort(valuesList, new Comparator<Number>() {
			@Override
			public int compare(Number o1, Number o2) {
				Double d1 = (o1 == null) ? Double.POSITIVE_INFINITY : o1.doubleValue();
				Double d2 = (o2 == null) ? Double.POSITIVE_INFINITY : o2.doubleValue();
				return d1.compareTo(d2);
			}
		});
	}

}
