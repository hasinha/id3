package com.sinha.id3;

// Class maintaining FP, FN, TP, TN counts. Used to plot ROC for each class value
public class ClassMetrics {

	public ClassMetrics(String classValue) {
		this.classValue = classValue;
	}

	private String classValue;
	private double tpCount = 0.01d;
	private double fpCount = 0.01d;
	private double tnCount = 0.01d;
	private double fnCount = 0.01d;

	public void tpCountPlus() {
		this.tpCount += 1d;
	}

	public void fpCountPlus() {
		this.fpCount += 1d;
	}

	public void tnCountPlus() {
		this.tnCount += 1d;
	}

	public void fnCountPlus() {
		this.fnCount += 1d;
	}

	public String getClassValue() {
		return classValue;
	}

	public void setClassValue(String classValue) {
		this.classValue = classValue;
	}

	public double getTpCount() {
		return tpCount;
	}

	public double getFpCount() {
		return fpCount;
	}

	public double getTnCount() {
		return tnCount;
	}

	public double getFnCount() {
		return fnCount;
	}

	@Override
	public String toString() {
		return "ClassMetrics [classValue=" + classValue + ", tpCount=" + tpCount + ", fpCount=" + fpCount + ", tnCount="
				+ tnCount + ", fnCount=" + fnCount + "]";
	}
}
