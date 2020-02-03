package com.sinha.id3.util;

import java.io.IOException;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinha.id3.ClassMetrics;

// Class for plotting individual ROC for classes
public class PlotGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlotGraph.class);

	public PlotGraph(String title, ClassMetrics classMetric, String typeDecision) throws IOException {

		// Calculate FPR and TPR
		double fpr = classMetric.getFpCount() / (classMetric.getFpCount() + classMetric.getTnCount());
		double[] xdata = new double[] { 0d, fpr, 1d };
		double tpr = classMetric.getTpCount() / (classMetric.getTpCount() + classMetric.getFnCount());
		double[] ydata = new double[] { 0d, tpr, 1d };
		LOGGER.info("{} FPR: {}, TPR: {}", title, fpr, tpr);

		// Initialise Chart
		XYChart chart = QuickChart.getChart(title + " ROC", "FPR", "TPR", "ROC Curve " + title + " " + typeDecision,
				xdata, ydata);
		chart.getStyler().setLegendPosition(LegendPosition.InsideSE);
		chart.getStyler().setLegendVisible(true);

		// Display Chart
		new SwingWrapper<XYChart>(chart).displayChart();

		// Save chart to file
		BitmapEncoder.saveBitmapWithDPI(chart, "./" + title + " " + typeDecision,
				BitmapFormat.PNG, 300);
	}

}