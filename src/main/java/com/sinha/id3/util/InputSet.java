package com.sinha.id3.util;

import java.util.Map;

/*
 * Object for storing attribute values for each instance
 */
public class InputSet {

	// Stores attribute values as attributeName/attributeValue key/value pairs
	private Map<String, Number> attributeValues;

	// Indicates class value for this instance
	private Number classValue;

	public Map<String, Number> getAttributeValues() {
		return attributeValues;
	}

	public void setAttributeValues(Map<String, Number> attributeValues) {
		this.attributeValues = attributeValues;
	}

	public Number getClassValue() {
		return classValue;
	}

	public void setClassValue(Number classValue) {
		this.classValue = classValue;
	}

	@Override
	public String toString() {
		return "InputSet [attributeValues=" + attributeValues + ", classValue=" + classValue + "]";
	}

}
