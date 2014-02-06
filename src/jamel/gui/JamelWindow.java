/* =========================================================
 * JAMEL : a Java (tm) Agent-based MacroEconomic Laboratory.
 * =========================================================
 *
 * (C) Copyright 2007-2014, Pascal Seppecher and contributors.
 * 
 * Project Info <http://p.seppecher.free.fr/jamel/javadoc/index.html>. 
 *
 * This file is a part of JAMEL (Java Agent-based MacroEconomic Laboratory).
 * 
 * JAMEL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JAMEL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JAMEL. If not, see <http://www.gnu.org/licenses/>.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.]
 * [JAMEL uses JFreeChart, copyright by Object Refinery Limited and Contributors. See <http://www.jfree.org>.]
 */

package jamel.gui;

import jamel.Jamel;
import jamel.Circuit;
import jamel.JamelObject;
import jamel.gui.charts.JamelChart;
import jamel.util.data.SimulationReport;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.time.Month;
import org.jfree.ui.TextAnchor;

/**
 * The window.
 */
public class JamelWindow extends JFrame {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("javadoc")
	public static final String COMMAND_ADD_PANEL = "windowAddPanel";

	@SuppressWarnings("javadoc")
	public static final String COMMAND_MARKER = "windowMarker";

	@SuppressWarnings("javadoc")
	public static final String COMMAND_SELECT_PANEL = "windowSelectPanel";

	@SuppressWarnings("javadoc")
	public static final String COMMAND_ZOOM = "windowSetZoom";

	/** The button bar. */
	private final ButtonBar buttonBar;

	/** The console panel. */
	private JEditorPane consolePane;

	/** The text of the console panel. */
	private final StringBuffer consoleText = new StringBuffer();

	/** A String that gives some infos about Jamel. */
	private final String infoString;

	/** The matrix panel. */
	private JEditorPane matrixPane;

	/** The tabbed pane. */
	private final JTabbedPane tabbedPane ;

	/** The view manager. */
	private final ViewManager viewManager;

	/**
	 * Creates a new window.
	 * @param name  the string that is to be this window's name.
	 */
	public JamelWindow(String name) {
		infoString = Jamel.readFile("info.html");
		viewManager = new ViewManager() ;
		tabbedPane = new JTabbedPane() ;
		setVisible(false);
		this.setName(name);
		this.setTitle(name);
		setMinimumSize(new Dimension(400,200));
		setPreferredSize(new Dimension(800,400));
		pack();
		setExtendedState(Frame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE) ;
		// ********
		getContentPane().add( tabbedPane ) ;
		this.tabbedPane.add("Matrix",getMatrixPanel());
		this.tabbedPane.add("Console",getConsolePanel());
		this.tabbedPane.add("Info",getInfoPanel());
		this.tabbedPane.setSelectedIndex(1);
		this.buttonBar = new ButtonBar(this) ;
		getContentPane().add( this.buttonBar, "South" );
		this.buttonBar.pause(false);
		this.setVisible(true);
	}

	/**
	 * Creates and returns the consol panel. 
	 * @return the consol panel.
	 */
	private JScrollPane getConsolePanel() {
		consolePane = new JEditorPane("text/html","<h2>The console panel.</h2>");
		Font font = new Font("Monaco", Font.PLAIN, 12);
		String bodyRule = "body { font-family: " + font.getFamily() + "; " +
				"font-size: " + font.getSize() + "pt; }";
		((HTMLDocument)consolePane.getDocument()).getStyleSheet().addRule(bodyRule);
		consolePane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(consolePane) ;
		return scrollPane;
	}

	/**
	 * Returns the info panel.
	 * @return the info panel.
	 */
	private Component getInfoPanel() {
		final JEditorPane editorPane = new JEditorPane("text/html","<center>"+this.infoString+"</center>");
		editorPane.addHyperlinkListener(new HyperlinkListener()
		{
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					try {
						java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (Exception ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(null,
								"<html>" +
										"Error.<br>"+
										"Cause: "+e.toString()+".<br>"+
										"Please see server.log for more details.</html>",
										"Warning",
										JOptionPane.WARNING_MESSAGE);
					}	        
			}
		});		
		editorPane.setEditable(false);
		final JScrollPane scrollPane = new JScrollPane(editorPane) ;
		return scrollPane;
	}

	/**
	 * Returns the matrix panel.
	 * @return the matrix panel.
	 */
	private JScrollPane getMatrixPanel() {
		matrixPane = new JEditorPane("text/html","<H2>The balance sheet matrix panel.</H2>");
		matrixPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(matrixPane) ;
		return scrollPane;
	}

	/**
	 * Sets the zoom.
	 * @param z the zoom to set.
	 */
	void zoom(int z) {
		viewManager.setRange(z) ;
		update() ;		
	}

	/**
	 * Adds a marker to all time charts.
	 * @param label the label of the marker.
	 * @param aMonth the month of the marker.
	 */
	public void addMarker(String label, Month aMonth) {
		final ValueMarker marker = new ValueMarker(aMonth.getFirstMillisecond()) ;
		marker.setLabel(label);
		marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		marker.setOutlinePaint(Color.WHITE);
		final int max = tabbedPane.getTabCount();
		for (int i = 0; i<max; i++) { 
			final Component currentTab = tabbedPane.getComponentAt(i);
			if (JPanel.class.isInstance(currentTab)) {
				int chartPanelCount = ((JPanel)currentTab).getComponentCount() ;
				for (int index=0 ; index<chartPanelCount ; index++) {
					final JamelChart chart = (JamelChart) (((ChartPanel)((JPanel)currentTab).getComponent(index)).getChart()) ;
					if (chart!=null) 
						chart.addMarker(marker);
				}
			}
		}

	}

	/**
	 * Receives an event.
	 * @param key  the instruction.
	 * @param val  An object that represents some parameters.
	 */
	public void doEvent(String key, Object val) {
		if (key.equals(COMMAND_MARKER)) {
			this.addMarker((String)val,JamelObject.getCurrentPeriod().getMonth());
		}
		else if (key.equals(COMMAND_ADD_PANEL)) {
			this.tabbedPane.add((Component) val,0);
		}
		else if (key.equals(COMMAND_SELECT_PANEL)) {
			this.tabbedPane.setSelectedIndex((Integer) val);
		}
		else if (key.equals(COMMAND_ZOOM)) {
			this.zoom((Integer) val);
		}
		else {
			throw new IllegalArgumentException("Unexpected command: "+key);			
		}
	}

	/**
	 * Exports a report.
	 */
	public void exportHtmlReport() {
		final int max = tabbedPane.getTabCount();
		final ProgressMonitor progressMonitor = new ProgressMonitor(this,
				"Exporting",
				"", 0,max);
		progressMonitor.setMillisToDecideToPopup(0);
		final String rc = System.getProperty("line.separator");
		final File outputDirectory = new File("exports/"+this.getTitle()+"-"+(new Date()).getTime());
		outputDirectory.mkdir();
		try {
			final FileWriter writer = new FileWriter(new File(outputDirectory,"Report.html"));
			writer.write("<HTML>"+rc);
			writer.write("<HEAD>");
			writer.write("<TITLE>"+this.getTitle()+"</TITLE>"+rc);
			writer.write("</HEAD>"+rc);
			writer.write("<BODY>"+rc);
			writer.write("<H1>"+this.getTitle()+"</H1>"+rc);
			writer.write("<HR>"+rc);
			final Date start = viewManager.getStart().getDate();
			final Date end = viewManager.getEnd().getDate();
			for (int tabIndex = 0; tabIndex < max; tabIndex ++) {
				try {
					final JPanel currentTab = (JPanel)tabbedPane.getComponentAt(tabIndex) ;
					final String tabTitle = tabbedPane.getTitleAt(tabIndex);
					writer.write("<H2>"+tabTitle+"</H2>"+rc);
					writer.write("<TABLE>"+rc);
					final int chartPanelCount = currentTab.getComponentCount() ;
					for (int chartIndex=0 ; chartIndex<chartPanelCount ; chartIndex++) {
						if ((chartIndex==3)|(chartIndex==6)) writer.write("<TR>"+rc);
						final ChartPanel aChartPanel = (ChartPanel)currentTab.getComponent(chartIndex);
						final JamelChart chart = (JamelChart) aChartPanel.getChart() ;
						if (chart != null) {
							final String chartTitle = chart.getTitle().getText();
							if (!chartTitle.equals("Empty")) {
								try {
									chart.setTitle("");
									chart.setTimeRange(start, end) ;
									String imageName = (tabTitle+"-"+chartIndex+"-"+chartTitle+".png").replace(" ", "_").replace("&", "and");
									ChartUtilities.saveChartAsPNG(new File(outputDirectory,imageName), chart, aChartPanel.getWidth(), aChartPanel.getHeight());
									writer.write("<TD><IMG src=\""+imageName+"\" title=\""+chartTitle+"\">"+rc);			
									chart.setTitle(chartTitle);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
					writer.write("</TABLE>"+rc);
					writer.write("<HR>"+rc);
				} catch (ClassCastException e) {
				}
				progressMonitor.setProgress(tabIndex);
			}
			writer.write("<H2>Scenario</H2>"+rc);
			writer.write(this.consoleText.toString());
			writer.write("</BODY>"+rc);
			writer.write("</HTML>"+rc);
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		progressMonitor.close();
	}

	/**
	 * Exports a simulation report in the latex format.
	 */
	public void exportLatexReport() {
		final SimulationReport report = new SimulationReport();
		final int max = tabbedPane.getTabCount();
		report.setTitle((this.getTitle().split("\\."))[0]);
		report.setDates(viewManager.getStart().getDate(),viewManager.getEnd().getDate());
		for (int tabIndex = 0; tabIndex < max; tabIndex ++) {
			try {
				final JPanel currentTab = (JPanel)tabbedPane.getComponentAt(tabIndex) ;
				final String tabTitle = tabbedPane.getTitleAt(tabIndex);					
				final int chartPanelCount = currentTab.getComponentCount() ;
				for (int chartIndex=0 ; chartIndex<chartPanelCount ; chartIndex++) {
					final ChartPanel aChartPanel = (ChartPanel)currentTab.getComponent(chartIndex);
					final JamelChart chart = (JamelChart) aChartPanel.getChart() ;
					report.addChart(tabTitle,chart);
				}
			} catch (ClassCastException e) {
				// The current panel is not a chart panel: nothing to do.
			}
		}
		report.setParameters(Circuit.getParameters());
		report.export();
	}

	/**
	 * Shows a dialog that indicates the bank failure.
	 */
	public void failure() {
		println("<font color=red>"+JamelObject.getCurrentPeriod().toString()+" Bank Failure</font>");
		buttonBar.pause(true);
		JOptionPane.showMessageDialog(this, "Bank Failure", "Failure", JOptionPane.WARNING_MESSAGE) ;
	}

	/**
	 * Prints a String in the console panel.
	 * @param s the String to print.
	 */
	public void println(final String s) {
		final String cr = "<br>";//System.getProperty("line.separator" );
		if (SwingUtilities.isEventDispatchThread()) {
			consoleText.append(s);
			consoleText.append(cr);
			consolePane.setText(consoleText.toString());
		}
		else {
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					consoleText.append(s);
					consoleText.append(cr);
					consolePane.setText(consoleText.toString());
				}}
					);
		}
	}

	/**
	 * 
	 */
	public void update() {
		viewManager.update() ;
		this.buttonBar.setMinMax(viewManager.getStart().toString(),viewManager.getEnd().toString()) ;
		final int max = tabbedPane.getTabCount();
		for (int i = 0; i<max; i++) { 
			final Component currentTab = tabbedPane.getComponentAt(i);
			if (JPanel.class.isInstance(currentTab)) {
				int chartPanelCount = ((JPanel)currentTab).getComponentCount() ;
				for (int index=0 ; index<chartPanelCount ; index++) {
					JamelChart chart = (JamelChart) (((ChartPanel)((JPanel)currentTab).getComponent(index)).getChart()) ;
					if (chart != null) {
						chart.setTimeRange(viewManager.getStart().getDate(), viewManager.getEnd().getDate()) ;
					}
				}
			}
		}
		this.matrixPane.setText((String) Circuit.getResource(Circuit.GET_HTML_MATRIX));
	}

	/**
	 * Updates the pause/run buttons.
	 */
	public void updatePauseButton() {
		this.buttonBar.updatePauseButton();
	}

	/**
	 * 
	 */
	public void zoomAll() {
		viewManager.update() ;
		viewManager.zoomAll() ;
		update() ;
	}	

}
