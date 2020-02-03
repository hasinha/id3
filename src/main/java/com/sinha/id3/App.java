package com.sinha.id3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinha.id3.util.CrossFoldValidation;
import com.sinha.id3.util.ForestUtils;
import com.sinha.id3.util.InputSet;
import com.sinha.id3.util.Node;
import com.sinha.id3.util.NodeVisualizer;
import com.sinha.id3.util.ReadInput;
import com.sinha.id3.util.TreeUtility;

/*
 * Main Class. Execution begins here in main() method
 */
public class App {

	// LOG statements to Console. Specified in log4j2.xml
	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	// ExecutorService to enable multi-threaded computing when determining split
	// variable for greedy split
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(140);

	// Executor to enable multi threaded construction of trees in forest.
	private static final ExecutorService FOREST_EXECUTOR = Executors.newFixedThreadPool(40);

	// Required Arguments -
	// index - 0 -> path/To/Data/File
	// index - 1 -> comma separated attribute names
	public static void main(String[] args) throws IOException {

		// Path to data set file
		String pathToDataFile = (String) args[0];

		// Comma separated attribute names
		String names = (String) args[1];

		// List of attributes constructed using comma separated attributes given above
		List<String> attributes = new ArrayList<>(Arrays.asList(names.split(",")));

		// Data read from file specified in pathToDataFile variable above
		List<InputSet> inputList = ReadInput.readFile(pathToDataFile, attributes);
		LOGGER.info("Instances: {}", inputList.size());

		// Generate Tree with max depth = 2
		long curTimeMid2 = System.currentTimeMillis();
		Node midRootNode2 = TreeUtility.generateTreeMidSplit(new ArrayList<>(attributes), inputList, null, null, 2, 1);
		LOGGER.info("Time taken to build tree mid split(2): {}", System.currentTimeMillis() - curTimeMid2);
		// Tree visualisation
		visualiseNode(midRootNode2, "Mid Split", "2");
		// 10-Fold Cross Validation
		CrossFoldValidation.kFoldCrossEvalOnMid(attributes, inputList, 10, 2);

		// Generate tree with max depth = 3
		long curTimeMid = System.currentTimeMillis();
		Node midRootNode = TreeUtility.generateTreeMidSplit(new ArrayList<>(attributes), inputList, null, null, 3, 1);
		LOGGER.info("Time taken to build tree mid split(3): {}", System.currentTimeMillis() - curTimeMid);
		visualiseNode(midRootNode, "Mid Split", "3");
		CrossFoldValidation.kFoldCrossEvalOnMid(attributes, inputList, 10, 3);

		// Generate tree with max depth = 4
		long curTimeMidplus = System.currentTimeMillis();
		Node midRootNode4 = TreeUtility.generateTreeMidSplit(new ArrayList<>(attributes), inputList, null, null, 4, 1);
		LOGGER.info("Time taken to build tree mid split(4): {}", System.currentTimeMillis() - curTimeMidplus);
		visualiseNode(midRootNode4, "Mid Split", "4");
		CrossFoldValidation.kFoldCrossEvalOnMid(attributes, inputList, 10, 4);

		// Generate tree using greedy search for split values but with no maximum
		// depth
		long curTimeGreedy = System.currentTimeMillis();
		Node rootNodeGreedy = TreeUtility.generateTreeGreedySplit(new ArrayList<>(attributes), inputList, null, null,
				EXECUTOR);
		LOGGER.info("Time taken to build tree greedy split: {}", System.currentTimeMillis() - curTimeGreedy);
		visualiseNode(rootNodeGreedy, "Greedy", "");
		CrossFoldValidation.kFoldCrossEvalGreedy(new ArrayList<>(attributes), inputList, 10, EXECUTOR);

		ForestUtils.evaluateForest(inputList, attributes, 10, 6, 10, FOREST_EXECUTOR, EXECUTOR);
		EXECUTOR.shutdown();
		FOREST_EXECUTOR.shutdown();
	}

	/*
	 * Visualizes the tree constructed
	 * 
	 * @input: node to visualise
	 */
	private static void visualiseNode(Node node, String type, String depth) throws IOException {
		NodeVisualizer frame = new NodeVisualizer(node, type, depth);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Display frame size
		frame.setSize(1600, 900);
		frame.setVisible(true);
	}

}
