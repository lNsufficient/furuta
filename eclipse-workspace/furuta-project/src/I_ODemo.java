
import se.lth.control.DoublePoint;
import se.lth.control.realtime.*;

public class I_ODemo extends Thread {

	
	private AnalogIn yChan;
	private AnalogOut uChan;
	OpCom opcom;

	public I_ODemo() {
		try {
			uChan = new AnalogOut(1);
			yChan = new AnalogIn(1);

		} catch (Exception e) {
			System.out.println(e);
			return;
		}
	}
	/** Sets up a reference to OpCom. Called from Main. */
	public void setOpCom(OpCom o) {
		opcom = o;
	}

	
	public void run() {
		long duration;
		long t = System.currentTimeMillis();
		DoublePoint dp;
		PlotData pd;
		double y, r, u;
		while(true){
			
			pd = new PlotData();
			pd.y = y;
			pd.ref = r;
			pd.x = realTime;
			opcom.putMeasurementDataPoint(pd);
			
			dp = new DoublePoint(realTime,u);
			opcom.putControlDataPoint(dp);

		try {
			uChan.set(0.0);
		} catch (Exception b) {
			System.out.println(b);
		}

		try {
			y = yChan.get();
		} catch (Exception v) {
			System.out.println(v);
		}
		
		System.out.println(y);

	}
}
}