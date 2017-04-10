public class PlotData implements Cloneable {
	double ref, y;
	double x; // holds the current time 
	
	public PlotData(double x, double yref, double y) {
		this.ref = yref;
		this.x = x;
		this.y = y;
	}
	
	public PlotData() {
		
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (Exception e) {
			return null;
		}
	}
}
