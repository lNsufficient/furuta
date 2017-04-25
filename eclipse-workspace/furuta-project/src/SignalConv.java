import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.IOChannelException;
public class SignalConv {
	private double offset, gain;
	private AnalogIn anIn;
	
	public SignalConv(int port, double offset, double gain){
		try {
			anIn = new AnalogIn(port);
		} catch (IOChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double get(){
		try {
			return anIn.get()*gain + offset;
		}
		 catch (Exception v) {
			 System.out.println(v);
			 return -100;
		 }
	}
}
