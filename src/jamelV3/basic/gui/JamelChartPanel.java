package jamelV3.basic.gui;

import java.awt.Color;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;

/**
 * A convenient extension of ChartPanel.
 */
public class JamelChartPanel extends ChartPanel {

	/** background */
	private static final Color background = new Color(229,229,229);

	/** A flag that indicates if the chart is a scatter chart or not. */
	private final boolean isScatter;

	/**
	 * Constructs a panel that displays the specified chart.
	 * @param chart the chart to be displayed.
	 * @param isScatter a flag that indicates if the chart is a scatter chart or not.
	 */
	public JamelChartPanel(JFreeChart chart, boolean isScatter) {
		super(chart);
		this.isScatter = isScatter;
		this.setBackground(background);
	}
	
	/**
	 * Adds a marker to the chart.
	 * @param marker the marker to be added.
	 */
	public void addMarker(ValueMarker marker) {
		if (!isScatter) {
			this.getChart().getXYPlot().addDomainMarker(marker);
		}
	}

}

// ***