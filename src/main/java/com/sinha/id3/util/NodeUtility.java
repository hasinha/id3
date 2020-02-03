package com.sinha.id3.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Utility class for node construction
 */
public class NodeUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeUtility.class);

	// Prepare node by populating values
	public static Node prepareNode(String splitAttr, double splitVal, List<InputSet> inputList,
			List<String> attributeList) {
		Node node = new Node();
		node.setLabel(splitAttr);
		node.setSplitAttr(splitAttr);
		node.setSplitVal(splitVal);
		node.setData(inputList);
		node.setLeftLabel("<=" + splitVal);
		node.setRightLabel(">" + splitVal);
		node.setAvailableAttributes(attributeList);
		return node;
	}

	// Perform classification on input node using weights of class in left and right
	// input sets to determine output class
	public static void performClassification(Node node, List<InputSet> leftInputSet, List<InputSet> rightInputSet,
			List<String> attributes) {
		Map<Number, List<Number>> leftClassValues = Utility.partitionMap(leftInputSet, attributes)
				.get(attributes.get(0));
		Map<Number, List<Number>> rightClassValues = Utility.partitionMap(rightInputSet, attributes)
				.get(attributes.get(0));

		// Determine weights for each class in left set
		Map<Number, Integer> leftClassWeights = leftClassValues.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().size()));

		// Determine weights for each class in right set
		Map<Number, Integer> rightClassWeights = rightClassValues.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().size()));

		// Classify left with class with maximum count
		Number leftClass = leftClassWeights.entrySet().stream()
				.max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

		// Classify right with class with maximum count
		Number rightClass = rightClassWeights.entrySet().stream()
				.max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

		// Mark as leaf node
		node.setLeafNode(Boolean.TRUE);
		node.setLeftClassification(String.valueOf(leftClass));
		node.setRightClassification(String.valueOf(rightClass));
		LOGGER.debug("Exit Classification");
	}
}
