
public class StateFeedback {
	private double[] gain;
	
	
	public StateFeedback(double[] gain){
		this.gain=gain;
	}
	
	public synchronized double calculateOutput(double[] states, double yref){
		if(states.length==this.gain.length){
			int i;
			double u=0;
			for(i=0;i<this.gain.length;i++){
				u=u+this.gain[i]*(states[i]-yref);
				//System.out.println(u);
			}
			u = -1.4*u;
			return u;
		}else{
			return 0;
		}
				
	}
	
	public synchronized void setGain(double[] gain){
		this.gain=gain;
	}
	
	public synchronized double[] getGain(){
		return this.gain;
	}

}
