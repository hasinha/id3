package com.sinha.id3.util;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

/*
 * Class with methods for visualising tree constructed
 */
public class NodeVisualizer extends JFrame {

	private static final long serialVersionUID = -2707712944901661771L;

	// X coordinate start point
	private int initX = 720;

	// Y coordinate start point
	private int initY = 20;

	// Width for new cell insertion
	private static final int CELLWIDTH = 100;

	// Height for new cell insertion
	private static final int CELLHEIGHT = 30;

	// Delta value for X coordinate when building child nodes
	private static final int X_DELTA = 400;

	// Delta value for Y coordinate when building child nodes
	private static final int Y_DELTA = 200;

	// Decimal Format to remove trailing zeros when printing split values
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.####");

	public NodeVisualizer(Node node, String type, String targetDepth) throws IOException {
		super("ID3 Tree Visualization");

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();

		// Initialize depth variable
		int depth = 1;
		try {

			// Insert root node at initX and initY
			Object v1 = graph.insertVertex(parent, null, node.getLabel(), initX, initY, CELLWIDTH, CELLHEIGHT);
			if (null != node.getLeftNode()) {

				// Draw left node at initX - delta and inity - delta
				drawLeftNode(graph, parent, node.getLeftNode(), initX - X_DELTA, initY + Y_DELTA, v1, node, depth);
			}
			if (null != node.getRightNode()) {

				// Draw right node at initX - delta and inity - delta
				drawRightNode(graph, parent, node.getRightNode(), initX + X_DELTA, initY + Y_DELTA, v1, node, depth);
			}
		} finally {
			graph.getModel().endUpdate();
		}

		File file = new File("./Tree-" + type + " " + targetDepth);
		file.createNewFile();
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		RenderedImage img = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, false, null);
		ImageIO.write(img, "png", file);
		getContentPane().add(graphComponent);
	}

	// Draw left node
	private static void drawLeftNode(mxGraph graph, Object parent, Node node, int x, int y, Object parentCell,
			Node parentNode, int depth) {
		depth += 2;
		Object cell = graph.insertVertex(parent, null, node.getLabel(), x, y, CELLWIDTH, CELLHEIGHT);
		graph.insertEdge(parent, null, "<=" + DECIMAL_FORMAT.format(parentNode.getSplitVal()), parentCell, cell);
		if (null == node.getLeftNode() && null == node.getRightNode()) {

			// If current node has no child nodes, draw left and right classification nodes
			addLeftClass(graph, parent, node, x, y, cell, depth);
			addRightClass(graph, parent, node, x, y, cell, depth);
			return;
		}
		if (null != node.getLeftNode()) {

			// Node has left child node. Recursively call drawLeftNode
			drawLeftNode(graph, parent, node.getLeftNode(), x - (X_DELTA / (depth)), y + Y_DELTA, cell, node, depth);

			// Node has no right child node. draw right classification node
			addRightClass(graph, parent, node, x, y, cell, depth);
		}
		if (null != node.getRightNode()) {

			// Node has right child node. Call drawRightNode
			drawRightNode(graph, parent, node.getRightNode(), x + (X_DELTA / (depth)), y + Y_DELTA, cell, node, depth);

			// Node has no left child node. draw left classification node
			addLeftClass(graph, parent, node, x, y, cell, depth);
		}
	}

	// Draw right classification node
	private static void addRightClass(mxGraph graph, Object parent, Node node, int x, int y, Object cell, int depth) {
		if (null != node.getRightClassification()) {
			Object cellRight = graph.insertVertex(parent, null, "Class: " + node.getRightClassification(),
					x + (X_DELTA / depth), y + (Y_DELTA / 2), CELLWIDTH, CELLHEIGHT);
			graph.insertEdge(parent, null, ">" + DECIMAL_FORMAT.format(node.getSplitVal()), cell, cellRight);
			graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "#E27E40", new Object[] { cellRight });
			graph.setCellStyles(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE, new Object[] { cellRight });
		}
	}

	// Draw left classification node
	private static void addLeftClass(mxGraph graph, Object parent, Node node, int x, int y, Object cell, int depth) {
		if (null != node.getLeftClassification()) {
			Object cellLeft = graph.insertVertex(parent, null, "Class: " + node.getLeftClassification(),
					x - (X_DELTA / depth), y + (Y_DELTA / 2), CELLWIDTH, CELLHEIGHT);
			graph.insertEdge(parent, null, "<=" + DECIMAL_FORMAT.format(node.getSplitVal()), cell, cellLeft);
			graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "#E27E40", new Object[] { cellLeft });
			graph.setCellStyles(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE, new Object[] { cellLeft });
		}
	}

	// Draw right node
	private static void drawRightNode(mxGraph graph, Object parent, Node node, int x, int y, Object parentCell,
			Node parentNode, int depth) {
		depth += 2;
		Object cell = graph.insertVertex(parent, null, node.getLabel(), x, y, CELLWIDTH, CELLHEIGHT);
		graph.insertEdge(parent, null, ">" + DECIMAL_FORMAT.format(parentNode.getSplitVal()), parentCell, cell);
		if (null == node.getLeftNode() && null == node.getRightNode()) {

			// If current node has no child nodes, draw left and right classification nodes
			addLeftClass(graph, parent, node, x, y, cell, depth);
			addRightClass(graph, parent, node, x, y, cell, depth);
			return;
		}
		if (null != node.getLeftNode()) {

			// Node has left child node. Call drawLeftNode
			drawLeftNode(graph, parent, node.getLeftNode(), x - (X_DELTA / (depth)), y + Y_DELTA, cell, node, depth);

			// Node has no right child node. draw right classification node
			addRightClass(graph, parent, node, x, y, cell, depth);
		}
		if (null != node.getRightNode()) {

			// Node has right child node. Recursively call drawRightNode
			drawRightNode(graph, parent, node.getRightNode(), x + (X_DELTA / (depth)), y + Y_DELTA, cell, node, depth);

			// Node has no left child node. draw left classification node
			addLeftClass(graph, parent, node, x, y, cell, depth);
		}
	}

}