import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import se.lth.control.*;
import se.lth.control.plot.*;

/** Class that creates and maintains a GUI for the Ball and Beam process.
Uses two internal threads to update plotters */
public class OpCom {
	public static final int OFF = 0, BEAM = 1, BALL = 2;

	private Regul regul;
	private LQParameters balancePar;
	private PIDParameters swingPar;
	private int mode;

	private PlotterPanel measurementPlotter; // has internal thread
	private PlotterPanel controlPlotter; // has internal thread

	// Declarartion of main frame.
	private JFrame frame;

	// Declarartion of panels.
	private BoxPanel guiPanel, plotterPanel, innerParPanel, outerParPanel, buttonPanel;
	private JPanel innerParLabelPanel, innerParFieldPanel, outerParLabelPanel, outerParFieldPanel, leftPanel;

	// Declaration of components.
	private DoubleField innerParKField = new DoubleField(5,3);
	private DoubleField innerParTiField = new DoubleField(5,3);
	private DoubleField innerParTrField = new DoubleField(5,3);
	private DoubleField innerParBetaField = new DoubleField(5,3);
	private DoubleField innerParHField = new DoubleField(5,3);
	private JButton innerApplyButton;

	private DoubleField outerParKField = new DoubleField(5,3);
	private DoubleField outerParTiField = new DoubleField(5,3);
	private DoubleField outerParTdField = new DoubleField(5,3);
	private DoubleField outerParTrField = new DoubleField(5,3);
	private DoubleField outerParNField = new DoubleField(5,3);
	private DoubleField outerParBetaField = new DoubleField(5,3);
	private DoubleField outerParHField = new DoubleField(5,3);
	private JButton outerApplyButton;

	private JRadioButton offModeButton;
	private JRadioButton beamModeButton;
	private JRadioButton ballModeButton;
	private JButton stopButton;

	private double range = 10.0; // Range of time axis
	private int divTicks = 5;    // Number of ticks on time axis
	private int divGrid = 5;     // Number of grids on time axis

	private boolean hChanged = false; 

	/** Constructor. Creates the plotter panels. */
	public OpCom() {
		measurementPlotter = new PlotterPanel(2, 4); // Two channels
		controlPlotter = new PlotterPanel(1, 4);
	}

	/** Starts the threads. */
	public void start() {
		measurementPlotter.start();
		controlPlotter.start();
	}

	/** Stops the threads. */
	public void stopThread() {
		measurementPlotter.stopThread();
		controlPlotter.stopThread();
	}

	/** Sets up a reference to Regul. Called by Main. */
	public void setRegul(Regul r) {
		regul = r;
	}

	/** Creates the GUI. Called from Main. */
	public void initializeGUI() {
		// Create main frame.
		frame = new JFrame("Ball and Beam GUI");

		// Create a panel for the two plotters.
		plotterPanel = new BoxPanel(BoxPanel.VERTICAL);
		// Create plot components and axes, add to plotterPanel.
		measurementPlotter.setYAxis(2, -1, 2, 2);
		measurementPlotter.setXAxis(range, divTicks, divGrid);
		measurementPlotter.setTitle("Set-point and measured variable");
		plotterPanel.add(measurementPlotter);
		plotterPanel.addFixed(10);
		controlPlotter.setYAxis(2, -1, 2, 2);
		controlPlotter.setXAxis(range, divTicks, divGrid);
		controlPlotter.setTitle("Control");
		plotterPanel.add(controlPlotter);

		// Get initail parameters from Regul
//		innerPar = regul.getInnerParameters();
		swingPar = regul.getOuterParameters();

		// Create panels for the parameter fields and labels, add labels and fields 
		innerParPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		innerParLabelPanel = new JPanel();
		innerParLabelPanel.setLayout(new GridLayout(0,1));
		innerParLabelPanel.add(new JLabel("K: "));
		innerParLabelPanel.add(new JLabel("Ti: "));
		innerParLabelPanel.add(new JLabel("Tr: "));
		innerParLabelPanel.add(new JLabel("Beta: "));
		innerParLabelPanel.add(new JLabel("h: "));
		innerParFieldPanel = new JPanel();
		innerParFieldPanel.setLayout(new GridLayout(0,1));
		innerParFieldPanel.add(innerParKField); 
		innerParFieldPanel.add(innerParTiField);
		innerParFieldPanel.add(innerParTrField);
		innerParFieldPanel.add(innerParBetaField);
		innerParFieldPanel.add(innerParHField);

		// Set initial parameter values of the fields
		innerParKField.setValue(balancePar.K);
		innerParTiField.setValue(balancePar.Ti);
		innerParTrField.setValue(balancePar.Tr);
		innerParBetaField.setValue(balancePar.Beta);
		innerParHField.setValue(balancePar.H);

		// Add action listeners to the fields
		innerParKField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				balancePar.K = innerParKField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});
		innerParTiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				balancePar.Ti = innerParTiField.getValue();
				if (balancePar.Ti==0.0) {
					balancePar.integratorOn = false;
				}
				else {
					balancePar.integratorOn = true;
				}
				innerApplyButton.setEnabled(true);
			}
		});
		innerParTrField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				balancePar.Tr = innerParTrField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});
		innerParBetaField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				balancePar.Beta = innerParBetaField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});
		innerParHField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				balancePar.H = innerParHField.getValue();
				swingPar.H = balancePar.H;
				outerParHField.setValue(balancePar.H);
				innerApplyButton.setEnabled(true);
				hChanged = true;
			}
		});

		// Add label and field panels to parameter panel
		innerParPanel.add(innerParLabelPanel);
		innerParPanel.addGlue();
		innerParPanel.add(innerParFieldPanel);
		innerParPanel.addFixed(10);

		// Create apply button and action listener.
		innerApplyButton = new JButton("Apply");
		innerApplyButton.setEnabled(false);
		innerApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setInnerParameters(balancePar);
				if (hChanged) {
					regul.setOuterParameters(swingPar);
				}	
				hChanged = false;
				innerApplyButton.setEnabled(false);
			}
		});

		// Create panel with border to hold apply button and parameter panel
		BoxPanel innerParButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
		innerParButtonPanel.setBorder(BorderFactory.createTitledBorder("Inner Parameters"));
		innerParButtonPanel.addFixed(10);
		innerParButtonPanel.add(innerParPanel);
		innerParButtonPanel.addFixed(10);
		innerParButtonPanel.add(innerApplyButton);

		// The same as above for the outer parameters
		outerParPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		outerParLabelPanel = new JPanel();
		outerParLabelPanel.setLayout(new GridLayout(0,1));
		outerParLabelPanel.add(new JLabel("K: "));
		outerParLabelPanel.add(new JLabel("Ti: "));
		outerParLabelPanel.add(new JLabel("Td: "));
		outerParLabelPanel.add(new JLabel("N: "));
		outerParLabelPanel.add(new JLabel("Tr: "));
		outerParLabelPanel.add(new JLabel("Beta: "));
		outerParLabelPanel.add(new JLabel("h: "));

		outerParFieldPanel = new JPanel();
		outerParFieldPanel.setLayout(new GridLayout(0,1));
		outerParFieldPanel.add(outerParKField); 
		outerParFieldPanel.add(outerParTiField);
		outerParFieldPanel.add(outerParTdField);
		outerParFieldPanel.add(outerParNField);
		outerParFieldPanel.add(outerParTrField);
		outerParFieldPanel.add(outerParBetaField);
		outerParFieldPanel.add(outerParHField);
		outerParKField.setValue(swingPar.K);
		outerParTiField.setValue(swingPar.Ti);
		outerParTdField.setValue(swingPar.Td);
		outerParNField.setValue(swingPar.N);
		outerParTrField.setValue(swingPar.Tr);
		outerParBetaField.setValue(swingPar.Beta);
		outerParHField.setValue(swingPar.H);
		outerParKField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.K = outerParKField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParTiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.Ti = outerParTiField.getValue();
				if (swingPar.Ti==0.0) {
					swingPar.integratorOn = false;
				}
				else {
					swingPar.integratorOn = true;
				}
				outerApplyButton.setEnabled(true);
			}
		});
		outerParTdField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.Td = outerParTdField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParNField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.N = outerParNField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParTrField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.Tr = outerParTrField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParBetaField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.Beta = outerParBetaField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParHField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.H = outerParHField.getValue();
				balancePar.H = swingPar.H;
				innerParHField.setValue(swingPar.H);
				outerApplyButton.setEnabled(true);
				hChanged = true;
			}
		});

		outerParPanel.add(outerParLabelPanel);
		outerParPanel.addGlue();
		outerParPanel.add(outerParFieldPanel);
		outerParPanel.addFixed(10);

		outerApplyButton = new JButton("Apply");
		outerApplyButton.setEnabled(false);
		outerApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setOuterParameters(swingPar);
				if (hChanged) {
					regul.setInnerParameters(balancePar);
				}	
				hChanged = false;
				outerApplyButton.setEnabled(false);
			}
		});

		BoxPanel outerParButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
		outerParButtonPanel.setBorder(BorderFactory.createTitledBorder("Outer Parameters"));
		outerParButtonPanel.addFixed(10);
		outerParButtonPanel.add(outerParPanel);
		outerParButtonPanel.addFixed(10);
		outerParButtonPanel.add(outerApplyButton);

		// Create panel for the buttons.
		buttonPanel = new BoxPanel(BoxPanel.VERTICAL);
		// Create the buttons.
		offModeButton = new JRadioButton("OFF");
		beamModeButton = new JRadioButton("BEAM");
		ballModeButton = new JRadioButton("BALL");
		stopButton = new JButton("STOP");
		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(offModeButton);
		group.add(beamModeButton);
		group.add(ballModeButton);
		// Button action listeners.
		offModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setOFFMode();
			}
		});
		beamModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setBEAMMode();
			}
		});
		ballModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setBALLMode();
			}
		});
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.shutDown();
				stopThread();
				System.exit(0);
			}
		});

		// Add buttons to button panel.
		buttonPanel.add(offModeButton);
		buttonPanel.add(beamModeButton);
		buttonPanel.add(ballModeButton);
		buttonPanel.add(stopButton);

		// Select initial mode.
		mode = regul.getMode();
		switch (mode) {
		case OFF:
			offModeButton.setSelected(true);
			break;
		case BEAM:
			beamModeButton.setSelected(true);
			break;
		case BALL:
			ballModeButton.setSelected(true);
			break;
		}

		// Create panel holding everything but the plotters.
		leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(innerParButtonPanel, BorderLayout.WEST);
		leftPanel.add(outerParButtonPanel, BorderLayout.EAST);
		leftPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Create panel for the entire GUI.
		guiPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		guiPanel.add(leftPanel);
		guiPanel.addGlue();
		guiPanel.add(plotterPanel);

		// WindowListener that exits the system if the main window is closed.
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				regul.shutDown();
				stopThread();
				System.exit(0);
			}
		});

		// Set guiPanel to be content pane of the frame.
		frame.getContentPane().add(guiPanel, BorderLayout.CENTER);

		// Pack the components of the window.
		frame.pack();

		// Position the main window at the screen center.
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension fd = frame.getSize();
		frame.setLocation((sd.width-fd.width)/2, (sd.height-fd.height)/2);

		// Make the window visible.
		frame.setVisible(true);
	}

	/** Called by Regul to put a control signal data point in the buffer. */
	public synchronized void putControlDataPoint(DoublePoint dp) {
		double x = dp.x;
		double y = dp.y;
		controlPlotter.putData(x, y);
	}

	/** Called by Regul to put a measurement data point in the buffer. */
	public synchronized void putMeasurementDataPoint(PlotData pd) {
		double x = pd.x;
		double ref = pd.ref;
		double y = pd.y;
		measurementPlotter.putData(x, ref, y);
	}
}