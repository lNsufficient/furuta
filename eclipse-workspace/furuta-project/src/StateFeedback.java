
public class StateFeedback {
	private double[] gain;
	private double[] gainNoPos;
	
	
	public StateFeedback(double[] gain){
		this.gain=gain;
		//Härmar Matlab-koden - parametrar för en andra kontroller
		gainNoPos = new double[4];
		gainNoPos[0]=7.534263403427089;
		gainNoPos[1]=1.346498408759346;
		gainNoPos[2]=0;
		gainNoPos[3]=0.221567196742396;
	}
	
	public synchronized double calculateOutput(double[] states, double yref){
		if(states.length==gain.length){
			int i;
			double u=0;
			//Härmar Matlab - switchar mellan de två kontrollerna
			if(Math.abs(states[3])>=4){
				for(i=0;i<gainNoPos.length;i++){
					u=u+gainNoPos[i]*(yref-states[i]);
					//System.out.println(u);
				}
			}else{
				for(i=0;i<gain.length;i++){
					u=u+gain[i]*(yref-states[i]);
					//System.out.println(u);
				}
			}
			System.out.println(u);
			u = 1.4*u/3.09; //Bästa skalningen evah
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
