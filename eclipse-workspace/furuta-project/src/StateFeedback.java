
public class StateFeedback {
	private double[] gain;
	private double[] gainNoPos;
	
	
	public StateFeedback(double[] gain){
		this.gain=gain;
	}
	
	public synchronized double calculateOutput(double[] states, double yref){
		if(states.length==gain.length){
			int i;
			double u=0;
			
				for(i=0;i<gain.length;i++){
					u=u+gain[i]*(yref-states[i]);
				
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
		double[] gainCopy = new double[4];
		for (int i = 0; i < 4; i++) {
			gainCopy[i] = gain[i];
		}
		return gainCopy;
	}

}
