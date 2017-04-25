
public class stateFeedback {
	private double[] gain;
	
	
	public stateFeedback(double[] gain){
		this.gain=gain;
	}
	
	public synchronized double calculateOutput(double[] states, double yref){
		if(states.length==this.gain.length){
			int i;
			double u=0;
			for(i=0;i<this.gain.length;i++){
				u=u+this.gain[i]*(states[i]-yref);
				System.out.println(u);
			}
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
