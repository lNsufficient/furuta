import se.lth.control.*;
import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;

/** Dummy Regul class for exercise 4. Generates and sends sinewaves to OpCom
and replies with print-outs when the set methods are called. */
public class Regul extends Thread {
	public static final int OFF=0, BALANCE=1, SWING=2;

	private PIDParameters swingParameters;
	private LQParameters balanceParameters;
	private OpCom opcom;
	private AnalogIn yChan;
	private AnalogOut uChan;

	private int mode;

	private long starttime;

	private double amp = 0.5; // Amplitude of sinewaves
	private double freq = 1.0; // Frequency of sinewaves
	private double realTime = 0.0;
	private double sinTime = 0.0; // between 0 and 2*pi
	private static final double twoPI = 2 * Math.PI;

	private boolean doIt = true;

	/** Constructor. Sets initial values of the controller parameters and initial mode. */
	public Regul() {
		
		try {
			uChan = new AnalogOut(1);
			yChan = new AnalogIn(3);
		} catch (IOChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		swingParameters = new PIDParameters();
		swingParameters.K = -0.05;
		swingParameters.Ti = 0.0;
		swingParameters.Td = 2.0;
		swingParameters.Tr = 10.0;
		swingParameters.N = 10.0;
		swingParameters.Beta = 1.0;
		swingParameters.H = 0.05;
		swingParameters.integratorOn = false;

		balanceParameters = new LQParameters();
		balanceParameters.K = -0.05;
		balanceParameters.Ti = 0.0;
		balanceParameters.Td = 2.0;
		balanceParameters.Tr = 10.0;
		balanceParameters.N = 10.0;
		balanceParameters.Beta = 1.0;
		balanceParameters.H = 0.05;
		balanceParameters.integratorOn = false;

		mode = OFF;
	}

	/** Sets up a reference to OpCom. Called from Main. */
	public void setOpCom(OpCom o) {
		opcom = o;
	}

	// Called in every sample in order to send plot data to OpCom
	private void sendDataToOpCom(double yref, double y, double u) {
		double x = (double)(System.currentTimeMillis() - starttime) / 1000.0;
		DoublePoint dp = new DoublePoint(x,u);
		PlotData pd = new PlotData(x,yref,y);
		opcom.putControlDataPoint(dp);
		opcom.putMeasurementDataPoint(pd);
	}

	/** Run method. Sends data periodically to OpCom. */
	public void run() {
		//Detta borde förmodligen likna det vi gjorde i lab 1.
		final long h = 100; // period (ms)
		long duration;
		long t = System.currentTimeMillis();
		DoublePoint dp;
		PlotData pd;
		double y, r, u;
		y = 0;
		u = 0;

		setPriority(7);

		while (doIt) {
			try {
				y = yChan.get();
			} catch (Exception v) {
				System.out.println(v);
			}
			
			try {
				uChan.set(0.0);
			} catch (Exception b) {
				System.out.println(b);
			}
			//y = amp * Math.sin(sinTime);
			
			
			r = amp * Math.cos(sinTime);
			//u = amp * Math.sin(sinTime);

			pd = new PlotData();
			pd.y = y;
			pd.ref = r;
			pd.x = realTime;
			opcom.putMeasurementDataPoint(pd);

			dp = new DoublePoint(realTime,u);
			opcom.putControlDataPoint(dp);

			realTime += ((double) h)/1000.0;
			sinTime += freq*((double) h)/1000.0;
			while (sinTime > twoPI) {sinTime -= twoPI; }

			t += h;
			duration = (int) (t - System.currentTimeMillis());
			if (duration > 0) {
				try {
					sleep(duration);
				} catch (Exception e) {}
			}
		}
	}

	/** Stops the thread. */
	private void stopThread() {
		doIt = false;
	}



	/** Called by OpCom to set the parameter values of the inner loop. */
	public synchronized void setBalanceParameters(LQParameters p) {
		System.out.println("Parameters changed for inner loop");
	}

	/** Called by OpCom during initialization to get the parameter values of the inner loop. */
	public synchronized LQParameters getBalanceParameters() {
		return (LQParameters) balanceParameters.clone(); 
	}


	/** Called by OpCom to set the parameter values of the outer loop */
	public synchronized void setSwingParameters(PIDParameters p) {
		System.out.println("Parameters changed for outer loop");
	}

	/** Called by OpCom during initialization to get the parameter values of the outer loop. */
	public synchronized PIDParameters getSwingParameters() {
		return (PIDParameters) swingParameters.clone(); 
	}

	/** Called by OpCom to turn off the controller. */
	public synchronized void setOFFMode() {
		System.out.println("Controller turned OFF");
	}

	/** Called by OpCom to set the Controller in BEAM mode. */
	public synchronized void setBalanceMode() {
		System.out.println("Controller in BALANCE mode");
	}

	/** Called by OpCom to set the Controller in BALL mode. */
	public synchronized void setSwingMode() {
		System.out.println("Controller in SWING mode");
	}

	/** Called by OpCom during initialization to get the initial mode of the controller. */
	public synchronized int getMode() {
		return mode;
	}

	/** Called by OpCom when the Stop button is pressed. */
	public synchronized void shutDown() {
		stopThread();
	}
}