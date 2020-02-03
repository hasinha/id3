package com.sinha.id3.util;

import java.util.List;

/*
 * Stores details of tree constructed
 */
public class Node {
	// Attribute used to split on
	private String label;

	private double impurity;

	// Available data considered when splitting attributes
	private List<InputSet> data;

	// Left Child Node
	private Node leftNode;

	// Right Child Node
	private Node rightNode;

	private String leftLabel;

	private String rightLabel;

	// Attribute used to split on
	private String splitAttr;

	// Attribute Value used to split
	private Number splitVal;

	// Available attributes considered when splitting
	private List<String> availableAttributes;

	// Assigned Class value for left
	private String leftClassification;

	// Assigned Class value for right
	private String rightClassification;

	// Is node a leaf node
	private boolean leafNode;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getImpurity() {
		return impurity;
	}

	public void setImpurity(double impurity) {
		this.impurity = impurity;
	}

	public List<InputSet> getData() {
		return data;
	}

	public void setData(List<InputSet> data) {
		this.data = data;
	}

	public Node getLeftNode() {
		return leftNode;
	}

	public void setLeftNode(Node leftNode) {
		this.leftNode = leftNode;
	}

	public Node getRightNode() {
		return rightNode;
	}

	public void setRightNode(Node rightNode) {
		this.rightNode = rightNode;
	}

	public String getLeftLabel() {
		return leftLabel;
	}

	public void setLeftLabel(String leftLabel) {
		this.leftLabel = leftLabel;
	}

	public String getRightLabel() {
		return rightLabel;
	}

	public void setRightLabel(String rightLabel) {
		this.rightLabel = rightLabel;
	}

	public String getSplitAttr() {
		return splitAttr;
	}

	public void setSplitAttr(String splitAttr) {
		this.splitAttr = splitAttr;
	}

	public Number getSplitVal() {
		return splitVal;
	}

	public void setSplitVal(Number splitVal) {
		this.splitVal = splitVal;
	}

	public List<String> getAvailableAttributes() {
		return availableAttributes;
	}

	public void setAvailableAttributes(List<String> availableAttributes) {
		this.availableAttributes = availableAttributes;
	}

	public String getLeftClassification() {
		return leftClassification;
	}

	public void setLeftClassification(String leftClassification) {
		this.leftClassification = leftClassification;
	}

	public String getRightClassification() {
		return rightClassification;
	}

	public void setRightClassification(String rightClassification) {
		this.rightClassification = rightClassification;
	}

	public boolean isLeafNode() {
		return leafNode;
	}

	public void setLeafNode(boolean leafNode) {
		this.leafNode = leafNode;
	}

	@Override
	public String toString() {
		return "Node [label=" + label + ", leftNode=" + leftNode + ", rightNode=" + rightNode + ", splitAttr="
				+ splitAttr + ", leftClassification=" + leftClassification + ", rightClassification="
				+ rightClassification + ", leafNode=" + leafNode + "]";
	}

	// Returns count of nodes present in this tree. Recursively calls nodeCount() on
	// child nodes until leaf node encountered where 1 is returned
	public int nodeCount() {
		if (this.leafNode == true) {
			return 1;
		} else if (this.leftNode == null && this.rightNode == null) {
			return 1;
		} else if (this.leftNode == null) {
			return 1 + rightNode.nodeCount();
		} else if (this.rightNode == null) {
			return 1 + leftNode.nodeCount();
		}
		return leftNode.nodeCount() + rightNode.nodeCount();
	}

}
