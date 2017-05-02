import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import se.lth.control.*;
import se.lth.control.plot.*;

/** Class that creates and maintains a GUI for the Ball and Beam process.
Uses two internal threads to update plotters */
public class OpCom {
	public static final int OFF = 0, BALANCE = 1, SWING = 2;

	private Regul regul;
	private double[] balancePar;
	private PIDParameters swingPar;
	private int mode;

	private PlotterPanel pendAngPlotter; // has internal thread
	private PlotterPanel armAngPlotter; // has internal thread
	private PlotterPanel pendVelPlotter; // has internal thread
	private PlotterPanel armVelPlotter; // has internal thread
	private PlotterPanel controlPlotter;

	// Declarartion of main frame.
	private JFrame frame;

	// Declarartion of panels.
	private BoxPanel guiPanel, plotterPanel, balanceParPanel, swingParPanel, buttonPanel;
	private JPanel balanceParLabelPanel, balanceParFieldPanel, swingParLabelPanel, swingParFieldPanel, leftPanel;

	// Declaration of components.
	private DoubleField balanceParL1Field = new DoubleField(5,3);
	private DoubleField balanceParL2Field = new DoubleField(5,3);
	private DoubleField balanceParL3Field = new DoubleField(5,3);
	private DoubleField balanceParL4Field = new DoubleField(5,3);
	private DoubleField balanceParHField = new DoubleField(5,3);
	private JButton balanceApplyButton;

	private DoubleField swingParKField = new DoubleField(5,3);
	private DoubleField swingParTiField = new DoubleField(5,3);
	private DoubleField swingParTdField = new DoubleField(5,3);
	private DoubleField swingParTrField = new DoubleField(5,3);
	private DoubleField swingParNField = new DoubleField(5,3);
	private DoubleField swingParBetaField = new DoubleField(5,3);
	private DoubleField swingParHField = new DoubleField(5,3);
	private JButton swingApplyButton;

	private JRadioButton offModeButton;
	private JRadioButton balanceModeButton;
	private JRadioButton swingModeButton;
	private JButton stopButton;

	private double range = 10.0; // Range of time axis
	private int divTicks = 5;    // Number of ticks on time axis
	private int divGrid = 5;     // Number of grids on time axis

	private boolean hChanged = false; 

	/** Constructor. Creates the plotter panels. */
	public OpCom() {
		pendAngPlotter = new PlotterPanel(2, 4); // Two channels
		pendVelPlotter = new PlotterPanel(2, 4);
		armAngPlotter = new PlotterPanel(1, 4);
		armVelPlotter = new PlotterPanel(1, 4);
		controlPlotter = new PlotterPanel(1, 4);
	}

	/** Starts the threads. */
	public void start() {
		pendAngPlotter.start();
		pendVelPlotter.start();
		armAngPlotter.start();
		armVelPlotter.start();
		controlPlotter.start();
	}

	/** Stops the threads. */
	public void stopThread() {
		pendAngPlotter.stopThread();
		pendVelPlotter.stopThread();
		armVelPlotter.stopThread();
		armAngPlotter.stopThread();
		controlPlotter.stopThread();
	}

	/** Sets up a reference to Regul. Called by Main. */
	public void setRegul(Regul r) {
		regul = r;
	}

	/** Creates the GUI. Called from Main. */
	public void initializeGUI() {
		// Create main frame.
		frame = new JFrame("Furuta GUI");

		// Create a panel for the two plotters.
		plotterPanel = new BoxPanel(BoxPanel.VERTICAL);
		// Create plot components and axes, add to plotterPanel.
		pendAngPlotter.setYAxis(4-(-4), -4, 10, 10);
		pendAngPlotter.setXAxis(range, divTicks, divGrid);
		pendAngPlotter.setTitle("Pendelum Angle (both)");
		plotterPanel.add(pendAngPlotter);
		plotterPanel.addFixed(5);
		
		pendVelPlotter.setYAxis(10, -5, 10, 10);
		pendVelPlotter.setXAxis(range, divTicks, divGrid);
		pendVelPlotter.setTitle("Pendelum Velocity (both)");
		plotterPanel.add(pendVelPlotter);
		plotterPanel.addFixed(5);
		
		armAngPlotter.setYAxis(8, -4, 2, 2);
		armAngPlotter.setXAxis(range, divTicks, divGrid);
		armAngPlotter.setTitle("Arm Angle");
		plotterPanel.add(armAngPlotter);
		
		armVelPlotter.setYAxis(40, -20, 10, 10);
		armVelPlotter.setXAxis(range, divTicks, divGrid);
		armVelPlotter.setTitle("Arm Velocity");
		plotterPanel.add(armVelPlotter);
		
		controlPlotter.setYAxis(4, -2, 1, 1);
		controlPlotter.setXAxis(range, divTicks, divGrid);
		controlPlotter.setTitle("Control");
		plotterPanel.add(controlPlotter);

		// Get initial parameters from Regul
		balancePar = regul.getBalanceParameters();
		swingPar = regul.getSwingParameters();

		// Create panels for the parameter fields and labels, add labels and fields 
		balanceParPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		balanceParLabelPanel = new JPanel();
		balanceParLabelPanel.setLayout(new GridLayout(0,1));
		balanceParLabelPanel.add(new JLabel("K: "));
		balanceParLabelPanel.add(new JLabel("Ti: "));
		balanceParLabelPanel.add(new JLabel("Tr: "));
		balanceParLabelPanel.add(new JLabel("Beta: "));
		balanceParLabelPanel.add(new JLabel("h: "));
		balanceParFieldPanel = new JPanel();
		balanceParFieldPanel.setLayout(new GridLayout(0,1));
		balanceParFieldPanel.add(balanceParL1Field); 
		balanceParFieldPanel.add(balanceParL2Field);
		balanceParFieldPanel.add(balanceParL3Field);
		balanceParFieldPanel.add(balanceParL4Field);
		balanceParFieldPanel.add(balanceParHField);

		// Set initial parameter values of the fields
		balanceParL1Field.setValue(balancePar[0]);
		balanceParL2Field.setValue(balancePar[1]);
		balanceParL3Field.setValue(balancePar[2]);
		balanceParL4Field.setValue(balancePar[3]);
		//balanceParHField.setValue(balancePar.H);

		// Add action listeners to the fields
		balanceParL1Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				balancePar[0] = balanceParL1Field.getValue();
				balanceApplyButton.setEnabled(true);
			}
		});
		balanceParL2Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				balancePar[1] = balanceParL2Field.getValue();
				balanceApplyButton.setEnabled(true);
			}
		});
		balanceParL3Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				balancePar[2] = balanceParL3Field.getValue();
				balanceApplyButton.setEnabled(true);
			}
		});
		balanceParL4Field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				balancePar[3] = balanceParL4Field.getValue();
				balanceApplyButton.setEnabled(true);
			}
		});

		// Add label and field panels to parameter panel
		balanceParPanel.add(balanceParLabelPanel);
		balanceParPanel.addGlue();
		balanceParPanel.add(balanceParFieldPanel);
		balanceParPanel.addFixed(10);

		// Create apply button and action listener.
		balanceApplyButton = new JButton("Apply");
		balanceApplyButton.setEnabled(false);
		balanceApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setBalanceParameters(balancePar);
				if (hChanged) {
					regul.setSwingParameters(swingPar);
				}	
				hChanged = false;
				balanceApplyButton.setEnabled(false);
			}
		});

		// Create panel with border to hold apply button and parameter panel
		BoxPanel balanceParButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
		balanceParButtonPanel.setBorder(BorderFactory.createTitledBorder("Balance Parameters"));
		balanceParButtonPanel.addFixed(10);
		balanceParButtonPanel.add(balanceParPanel);
		balanceParButtonPanel.addFixed(10);
		balanceParButtonPanel.add(balanceApplyButton);

		// The same as above for the swing parameters
		swingParPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		swingParLabelPanel = new JPanel();
		swingParLabelPanel.setLayout(new GridLayout(0,1));
		swingParLabelPanel.add(new JLabel("K: "));
		swingParLabelPanel.add(new JLabel("Ti: "));
		swingParLabelPanel.add(new JLabel("Td: "));
		swingParLabelPanel.add(new JLabel("N: "));
		swingParLabelPanel.add(new JLabel("Tr: "));
		swingParLabelPanel.add(new JLabel("Beta: "));
		swingParLabelPanel.add(new JLabel("h: "));

		swingParFieldPanel = new JPanel();
		swingParFieldPanel.setLayout(new GridLayout(0,1));
		swingParFieldPanel.add(swingParKField); 
		swingParFieldPanel.add(swingParTiField);
		swingParFieldPanel.add(swingParTdField);
		swingParFieldPanel.add(swingParNField);
		swingParFieldPanel.add(swingParTrField);
		swingParFieldPanel.add(swingParBetaField);
		swingParFieldPanel.add(swingParHField);
		swingParKField.setValue(swingPar.K);
		swingParTiField.setValue(swingPar.Ti);
		swingParTdField.setValue(swingPar.Td);
		swingParNField.setValue(swingPar.N);
		swingParTrField.setValue(swingPar.Tr);
		swingParBetaField.setValue(swingPar.Beta);
		swingParHField.setValue(swingPar.H);
		swingParKField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.K = swingParKField.getValue();
				swingApplyButton.setEnabled(true);
			}
		});
		swingParTiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.Ti = swingParTiField.getValue();
				if (swingPar.Ti==0.0) {
					swingPar.integratorOn = false;
				}
				else {
					swingPar.integratorOn = true;
				}
				swingApplyButton.setEnabled(true);
			}
		});
		swingParTdField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.Td = swingParTdField.getValue();
				swingApplyButton.setEnabled(true);
			}
		});
		swingParNField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.N = swingParNField.getValue();
				swingApplyButton.setEnabled(true);
			}
		});
		swingParTrField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.Tr = swingParTrField.getValue();
				swingApplyButton.setEnabled(true);
			}
		});
		swingParBetaField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.Beta = swingParBetaField.getValue();
				swingApplyButton.setEnabled(true);
			}
		});
		swingParHField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swingPar.H = swingParHField.getValue();
				//balancePar.H = swingPar.H;
				balanceParHField.setValue(swingPar.H);
				swingApplyButton.setEnabled(true);
				hChanged = true;
			}
		});

		swingParPanel.add(swingParLabelPanel);
		swingParPanel.addGlue();
		swingParPanel.add(swingParFieldPanel);
		swingParPanel.addFixed(10);

		swingApplyButton = new JButton("Apply");
		swingApplyButton.setEnabled(false);
		swingApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setSwingParameters(swingPar);
				if (hChanged) {
					regul.setBalanceParameters(balancePar);
				}	
				hChanged = false;
				swingApplyButton.setEnabled(false);
			}
		});

		BoxPanel swingParButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
		swingParButtonPanel.setBorder(BorderFactory.createTitledBorder("Swing Parameters"));
		swingParButtonPanel.addFixed(10);
		swingParButtonPanel.add(swingParPanel);
		swingParButtonPanel.addFixed(10);
		swingParButtonPanel.add(swingApplyButton);

		// Create panel for the buttons.
		buttonPanel = new BoxPanel(BoxPanel.VERTICAL);
		// Create the buttons.
		offModeButton = new JRadioButton("OFF");
		balanceModeButton = new JRadioButton("BALANCE");
		swingModeButton = new JRadioButton("SWING");
		stopButton = new JButton("STOP");
		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(offModeButton);
		group.add(balanceModeButton);
		group.add(swingModeButton);
		// Button action listeners.
		offModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setOFFMode();
			}
		});
		balanceModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setBalanceMode();
			}
		});
		swingModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setSwingMode();
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
		buttonPanel.add(balanceModeButton);
		buttonPanel.add(swingModeButton);
		buttonPanel.add(stopButton);

		// Select initial mode.
		mode = regul.getMode();
		switch (mode) {
		case OFF:
			offModeButton.setSelected(true);
			break;
		case BALANCE:
			balanceModeButton.setSelected(true);
			break;
		case SWING:
			swingModeButton.setSelected(true);
			break;
		}

		// Create panel holding everything but the plotters.
		leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(balanceParButtonPanel, BorderLayout.WEST);
		leftPanel.add(swingParButtonPanel, BorderLayout.EAST);
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
	public synchronized void putMeasurementDataPoint(PlotData pendAng, PlotData pendAngVel, DoublePoint armAng, DoublePoint armVel) {
		double xAng = pendAng.x;
		double topAng = pendAng.ref;
		double ang = pendAng.y;
		pendAngPlotter.putData(xAng, topAng, ang);
		
		double xAngVel = pendAngVel.x;
		double topAngVel = pendAngVel.ref;
		double angVel = pendAngVel.y;
		pendVelPlotter.putData(xAngVel, topAngVel, angVel);
		
		double xArmAng = armAng.x;
		double yArmAng = armAng.y;
		armAngPlotter.putData(xArmAng, yArmAng);
		
		double xArmVel = armVel.x;
		double yArmVel = armVel.y;
		armVelPlotter.putData(xArmVel, yArmVel);
		
	}
}