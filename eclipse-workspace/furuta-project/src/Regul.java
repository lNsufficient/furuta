import se.lth.control.*;
import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;

/** Dummy Regul class for exercise 4. Generates and sends sinewaves to OpCom
and replies with print-outs when the set methods are called. */
public class Regul extends Thread {
	public static final int OFF=0, BALANCE=1, SWING=2;

	private PIDParameters swingParameters;
	private StateFeedback balanceRegul;
	private OpCom opcom;
	private AnalogIn topAng, topAngVel, armAng, armVel, penAng, penVel;
	//2 - pend top angle
	//3 - pend top anglevel
	//4 - arm pos
	//5 - arm velocity
	//6 - pendulum angle
	//7 - pendulum vel
	private AnalogOut uChan;

	
	
	private int mode;

	private long starttime;
	
	private double[] balanceGains=new double[4];
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
			
			topAng = new AnalogIn(2);
			topAngVel = new AnalogIn(3);
			armAng = new AnalogIn(4);
			armVel = new AnalogIn(5);
			penAng = new AnalogIn(6);
			penVel = new AnalogIn(7);
			
			//2 - pend top angle
			//3 - pend top anglevel
			//4 - arm pos
			//5 - arm velocity
			//6 - pendulum angle
			//7 - pendulum vel
			
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

		balanceGains[0]=-2.0758;
		balanceGains[1]=-0.3676;
		balanceGains[2]=-0.2456;
		balanceGains[3]=-0.1584;
		balanceRegul = new StateFeedback(balanceGains);

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
		double r, u, y;
		double[] states = new double[4];
		u = 0;
		y = 0;
		setPriority(7);

		while (doIt) {
			
			//states[0]
			System.out.println("Mode: " + mode);
			switch (mode) {
				case OFF: {
					y = 0;
					u = 0;
					try {
						uChan.set(0);
					} catch (Exception b) {
						System.out.println(b);
					}
					System.out.println("Reached end, OFF ");
					
					break;
				}
			
				case SWING: {
			
					
					
					try {
						uChan.set(0.0);
					} catch (Exception b) {
						System.out.println(b);
					}
					System.out.println("Reached end, SWING ");
					
					break;
				}
				
				case BALANCE: {
					System.out.println("Inside Balance");
					//states = 
					
					//u = balanceRegul.calculateOutput(states, yref);
					try {
						states[0] = topAng.get();
						states[1] = topAngVel.get();
						states[2] = armAng.get();
						states[3] = armVel.get();
					} catch (Exception v) {
						System.out.println("Read Problem balance");
						System.out.println(v);
					}
					
					u = balanceRegul.calculateOutput(states, 0);
					u = 1;
					try {
						uChan.set(u);
					} catch (Exception b) {
						System.out.println("Write problem balance");
						System.out.println(b);
					}
					
					y = states[0];
					System.out.println("Reached end, BALANCE ");
					
					break;
				}
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
	public synchronized void setBalanceParameters(double[] gain) {
		balanceRegul.setGain(gain);
		System.out.println("Parameters changed for balance-controller");
	}

	/** Called by OpCom during initialization to get the parameter values of the inner loop. */
	public synchronized double[] getBalanceParameters() {
		return balanceRegul.getGain(); 
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
		mode = OFF;
	}

	/** Called by OpCom to set the Controller in BEAM mode. */
	public synchronized void setBalanceMode() {
		System.out.println("Controller in BALANCE mode");
		mode = BALANCE;
	}

	/** Called by OpCom to set the Controller in BALL mode. */
	public synchronized void setSwingMode() {
		System.out.println("Controller in SWING mode");
		mode = SWING; 
	}

	/** Called by OpCom during initialization to get the initial mode of the controller. */
	public synchronized int getMode() {
		return mode;
	}

	/** Called by OpCom when the Stop button is pressed. */
	public synchronized void shutDown() {
		try {
			uChan.set(0.0);
		} catch (Exception b) {
			System.out.println(b);
		}
		
		stopThread();
	}
}