package com.sinha.id3.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;

// Used for forest evaluation. Allows for parallel generation of trees
public class TreeGenerator implements Callable<Node> {

	public TreeGenerator(List<String> attributes, List<InputSet> inputList, int attributeSize,
			ExecutorService executor) {
		this.attributes = attributes;
		this.inputList = inputList;
		this.attributeSize = attributeSize;
		this.executor = executor;
	}

	private int attributeSize;
	private List<String> attributes;
	private List<InputSet> inputList;
	private List<String> chosenAttributes;
	private ExecutorService executor;

	@Override
	public Node call() throws Exception {
		chooseAttributes();
		Node rootNode = TreeUtility.generateTreeGreedySplit(chosenAttributes, inputList, null, null, executor);
		return rootNode;
	}

	// Randomly choose attributes from original list to use for tree generation.
	private void chooseAttributes() {
		chosenAttributes = new ArrayList<>();
		int i = 0;
		while (i < attributeSize) {
			String attr = attributes.get(ThreadLocalRandom.current().nextInt(attributes.size()));
			while (chosenAttributes.contains(attr)) {
				attr = attributes.get(ThreadLocalRandom.current().nextInt(attributes.size()));
			}
			chosenAttributes.add(attr);
			i++;
		}
	}

}
