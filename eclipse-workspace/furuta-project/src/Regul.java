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
	private double topAngGain = 0.058, topAngVelGain = 0.068*10, armAngGain = 2.43; 
	private double armVelGain = 2, penAngGain = 0.3091, penVelGain = 3.76;
	private double topAngOffs = 0.7792-0.7792, topAngVelOffs = 0, armAngOffs = 0;
	private double armVelOffs = 0.0708, penVelOffs = -0.022, penAngOffs = 5.1763;
	private double topSwitchLimit=0.5;
	private EnergyControllerFuruta swingUpController;
	private boolean firstTry = true;

	//2 - pend top angle
	//3 - pend top anglevel
	//4 - arm pos
	//5 - arm velocity
	//6 - pendulum angle
	//7 - pendulum vel
	private AnalogOut uChan;

	private int mode;

	//private long starttime;

	private double[] balanceGains=new double[4];
	//	private double amp = 0.5; // Amplitude of sinewaves
	//	private double freq = 1.0; // Frequency of sinewaves
	private double realTime = 0.0;
	//	private double sinTime = 0.0; // between 0 and 2*pi
	//	private static final double twoPI = 2 * Math.PI;

	private boolean doIt = true;

	/** Constructor. Sets initial values of the controller parameters and initial mode. */
	public Regul() {

		try {
			uChan = new AnalogOut(0);
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

		swingUpController = new EnergyControllerFuruta(1.5, 50);

		swingParameters = new PIDParameters();
		swingParameters.K = -0.05;
		swingParameters.Ti = 0.0;
		swingParameters.Td = 2.0;
		swingParameters.Tr = 10.0;
		swingParameters.N = 10.0;
		swingParameters.Beta = 1.0;
		swingParameters.H = 0.05;
		swingParameters.integratorOn = false;

		balanceGains[0]= 8.8349;
		balanceGains[1]= 1.5804;
		balanceGains[2]= 0.2205;
		balanceGains[3]= 0.3049;
		balanceRegul = new StateFeedback(balanceGains);

		mode = OFF;
	}

	/** Sets up a reference to OpCom. Called from Main. */
	public void setOpCom(OpCom o) {
		opcom = o;
	}

	// Called in every sample in order to send plot data to OpCom
	private void sendDataToOpCom(double yref, double[] y, double u) {
		//double x = (double)(System.currentTimeMillis() - starttime) / 1000.0;
		DoublePoint dp = new DoublePoint(realTime,u);
		double y4, y5;
		if (y.length <= 4) {
			y4 = 0;
			y5 = 0;
		} else {
			y4 = y[4];
			y5 = y[5];
		}

		PlotData pdPenAng = new PlotData(realTime,y[0],y4);
		PlotData pdPenVel = new PlotData(realTime,y[1],y5);
		DoublePoint pdArmAng = new DoublePoint(realTime,y[2]);
		DoublePoint pdArmVel = new DoublePoint(realTime,y[3]);
		opcom.putControlDataPoint(dp);
		opcom.putMeasurementDataPoint(pdPenAng, pdPenVel, pdArmAng, pdArmVel);
		//		opcom.putMeasurementDataPoint(pdPenVel);
		//		opcom.putMeasurementDataPoint(pdArmAng);
		//	
		balanceGains[2]= 0;	
	}


	/** Run method. Sends data periodically to OpCom. */
	public void run() {
		//Detta borde förmodligen likna det vi gjorde i lab 1.
		final long h = 40; // period (ms)
		long duration;
		long t = System.currentTimeMillis();

		double r, u, y;
		double[] states = new double[4];
		double[] debugStates = new double[6];
		double swingAng = 0.6;

		u = 0;
		y = 0;
		r = 0;
		setPriority(7);

		while (doIt) {


			switch (mode) {
			case OFF: {
				firstTry = true;
				y = 0;
				u = 0;
				r = 0;


				try {

					debugStates[0] = (topAng.get()+ topAngOffs)*topAngGain;
					debugStates[1] = (topAngVel.get()+ topAngVelOffs)*topAngVelGain;
					debugStates[2] = (armAng.get()+ armAngOffs)*armAngGain;
					debugStates[3] = (armVel.get()+ armVelOffs)*armVelGain;
					debugStates[4] = (penAng.get()+ penAngOffs)*penAngGain;
					debugStates[5] = (penVel.get()+ penVelOffs)*penVelGain;
					//fixar antalet varv problemet
					for(int i = 0; i < 6; i+=2){
						debugStates[i] = debugStates[i] % (2*Math.PI);

						if(debugStates[i] > Math.PI){
							debugStates[i] -= 2*Math.PI;
						}else if(debugStates[i] < -Math.PI){
							debugStates[i] += 2*Math.PI;
						}
					}


				} catch (IOChannelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					uChan.set(0);
				} catch (Exception b) {
					System.out.println(b);
				}

				sendDataToOpCom(0, debugStates, u);
				break;
			}

			case SWING: {
				try {
					states[0] = (penAng.get()+ penAngOffs)*penAngGain;
					states[1] = (penVel.get()+ penVelOffs)*penVelGain;

					for(int i = 0; i < 4; i+=10){
						states[i] = states[i] % (2*Math.PI);

						if(states[i] > Math.PI){
							states[i] -= 2*Math.PI;
						}else if(states[i] < -Math.PI){
							states[i] += 2*Math.PI;
						}
					}


					if (Math.abs(states[0] )  < swingAng) {
						mode = BALANCE;
					}
				//	System.out.println("Här är vi nu 1 +" + Math.abs(states[0]));
				//	System.out.println("-0.5 +" + (Math.abs(Math.PI)-0.5));
					if(states[0] < (Math.abs(Math.PI)-0.5) && states[1] < Math.abs(0.5) && firstTry){
						u = 1;
						try {
							uChan.set(u);
							firstTry = false;
							sleep(150);
						} catch (Exception b) {
							System.out.println(b);
						}

						sendDataToOpCom(0, states, u);
						System.out.println("puff");
						break;
						
						
					}

					u = swingUpController.calculateOutput(states[0], states[1]);
					System.out.println("swing");


				} catch (IOChannelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}



				u = saturateU(u);

				try {
					uChan.set(u);
				} catch (Exception b) {
					System.out.println(b);
				}

				sendDataToOpCom(0, states, u);
				break;
			}

			case BALANCE: { 

				firstTry = true;
				try {

					states[0] = (topAng.get()+ topAngOffs)*topAngGain;
					states[1] = (topAngVel.get()+ topAngVelOffs)*topAngVelGain;
					states[2] = (armAng.get()+ armAngOffs)*armAngGain;
					states[3] = (armVel.get()+ armVelOffs)*armVelGain;
					//Switchar mellan Top & 360-mätning
					if(!((Math.abs(states[2]) < topSwitchLimit) || (states[2] > Math.PI*2-topSwitchLimit&states[2]>Math.PI*2+topSwitchLimit))){
						states[0] = (penAng.get()+ penAngOffs)*penAngGain;
						states[1] = (penVel.get()+ penVelOffs)*penVelGain;
					}
					for(int i = 0; i < 4; i+=2){
						states[i] = states[i] % (2*Math.PI);

						if(states[i] > Math.PI){
							states[i] -= 2*Math.PI;
						}else if(states[i] < -Math.PI){
							states[i] += 2*Math.PI;
						}
					}
				} catch (Exception v) {
					System.out.println("Read Problem balance");
					System.out.println(v);
				}



				u = balanceRegul.calculateOutput(states, 0);
				u = saturateU(u);

				//u = 0;
				try {
					//System.out.println("U has been set to " + u);
					uChan.set(u);
					//System.out.println("U has been set to " + u);
				} catch (Exception b) {
					System.out.println("Write problem balance");
					System.out.println(b);
				}

				y = states[0];
				//System.out.println("Reached end, BALANCE ");
				sendDataToOpCom(0, states, u);
				if (Math.abs(states[0]) > 0.6) {
					try {
						uChan.set(0);
						sleep(500);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					mode = SWING;


				}
				break;
			}

			}



			realTime += ((double) h)/1000.0;


			t += h;
			duration = (int) (t - System.currentTimeMillis());
			if (duration > 0) {
				try {
					sleep(duration);
				} catch (Exception e) {}
			}
		}
	}

	private double saturateU(double u) {
		double lim = 2;
		if (u > lim) {
			return lim;
		} else if (u < -lim) {
			return -lim;
		} else {
			return u;
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