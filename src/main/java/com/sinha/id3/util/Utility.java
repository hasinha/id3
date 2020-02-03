package com.sinha.id3.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Utility class containing general functions
public class Utility {

	// Given inputset object, returns partitioned map containing entries for each
	// attribute -> possible class values -> associated attribute values for each
	// class
	public static Map<String, Map<Number, List<Number>>> partitionMap(List<InputSet> inputList,
			List<String> attributes) {
		Map<String, Map<Number, List<Number>>> attributeMap = new HashMap<>();
		for (String attribute : attributes) {
			Map<Number, List<Map<String, Number>>> groupedMap = inputList.stream().collect(Collectors.groupingBy(
					InputSet::getClassValue, Collectors.mapping(InputSet::getAttributeValues, Collectors.toList())));
			Map<Number, List<Number>> classMap = new HashMap<>();
			for (Map.Entry<Number, List<Map<String, Number>>> entry : groupedMap.entrySet()) {
				classMap.put(entry.getKey(),
						entry.getValue().stream().map(input -> input.get(attribute)).collect(Collectors.toList()));
			}
			attributeMap.put(attribute, classMap);
		}
		return attributeMap;
	}

	// Returns Possible class values in input data set
	public static List<Number> getClassValues(List<InputSet> inputList) {
		List<Number> classValues = inputList.stream().map(input -> input.getClassValue()).distinct()
				.collect(Collectors.toList());
		return classValues;
	}

	// Determine split values for list of possible values. Returns mid points
	// between all attribute values
	public static List<Number> getSplitValues(List<Number> values) {
		List<Number> splitValues = new ArrayList<>();
		for (int i = 0; i < values.size() - 1; i++) {
			splitValues.add((values.get(i).doubleValue() + values.get(i + 1).doubleValue()) / 2);
		}
		return splitValues;
	}

	// Determines split values when splitting attribute by mid
	public static Map<String, Number> getSplitValuesByMid(List<InputSet> inputList, List<String> attributes) {
		Map<String, List<Double>> attributeValues = new HashMap<>();
		Map<String, Number> splitValues = new HashMap<>();
		for (InputSet inputSet : inputList) {

			// Prepare map of attribute:list<Values>
			for (Map.Entry<String, Number> entry : inputSet.getAttributeValues().entrySet()) {
				if (attributeValues.containsKey(entry.getKey())) {
					attributeValues.get(entry.getKey()).add(entry.getValue().doubleValue());
				} else {
					attributeValues.put(entry.getKey(), new ArrayList<>(Arrays.asList(entry.getValue().doubleValue())));
				}
			}
		}

		// Get mid values for each attribute using min and max values
		for (Map.Entry<String, List<Double>> entry : attributeValues.entrySet()) {
			double min = Collections.min(entry.getValue());
			double max = Collections.max(entry.getValue());
			double splitValue = (min + max) / 2;
			splitValues.put(entry.getKey(), splitValue);
		}
		return splitValues;
	}

}
