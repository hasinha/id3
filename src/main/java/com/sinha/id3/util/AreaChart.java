package com.sinha.id3.util;

import java.io.IOException;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.Styler.LegendPosition;

import com.sinha.id3.ClassMetrics;

// Used to plot ROC curve for all possible classes on one graph
public class AreaChart {

	public AreaChart(ClassMetrics class1, ClassMetrics class2, ClassMetrics Class3, String typeDecision)
			throws IOException {

		// Calculate FPR and TPR for each class value
		double fpr1 = class1.getFpCount() / (class1.getFpCount() + class1.getTnCount());
		double[] xdata1 = new double[] { 0d, fpr1, 1d };
		double tpr1 = class1.getTpCount() / (class1.getTpCount() + class1.getFnCount());
		double[] ydata1 = new double[] { 0d, tpr1, 1d };

		double fpr2 = class2.getFpCount() / (class2.getFpCount() + class2.getTnCount());
		double[] xdata2 = new double[] { 0d, fpr2, 1d };
		double tpr2 = class2.getTpCount() / (class2.getTpCount() + class2.getFnCount());
		double[] ydata2 = new double[] { 0d, tpr2, 1d };

		double fpr3 = Class3.getFpCount() / (Class3.getFpCount() + Class3.getTnCount());
		double[] xdata3 = new double[] { 0d, fpr3, 1d };
		double tpr3 = Class3.getTpCount() / (Class3.getTpCount() + Class3.getFnCount());
		double[] ydata3 = new double[] { 0d, tpr3, 1d };

		// Initialise Chart
		XYChart chart = new XYChartBuilder().width(800).height(600).title("ROC for all classes").xAxisTitle("FPR")
				.yAxisTitle("TPR").build();

		chart.getStyler().setLegendPosition(LegendPosition.InsideSE);
		chart.getStyler().setLegendVisible(true);
		chart.getStyler().setAxisTitlesVisible(true);

		// Populate coordinates
		chart.addSeries("Class 1", xdata1, ydata1);
		chart.addSeries("Class 2", xdata2, ydata2);
		chart.addSeries("Class 3", xdata3, ydata3);

		// Display chart
		new SwingWrapper<XYChart>(chart).displayChart();

		// Save to file
		BitmapEncoder.saveBitmapWithDPI(chart, "./AllClassesROC" + typeDecision, BitmapFormat.PNG, 300);
	}
}
