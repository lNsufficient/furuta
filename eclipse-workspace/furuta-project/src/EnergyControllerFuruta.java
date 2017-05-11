public class EnergyControllerFuruta {
	private double k, c;

	public EnergyControllerFuruta (double k){
		double M,J,l,g,w0;
		this.k = k;
		M = 0.01;
		l = 0.413;
		J = 0.05;
		J = 0.0009;
		g = 9.81;
		w0 = Math.sqrt(M*g*l/J);
		System.out.println("omega0 init: " + w0);
		c = 1/(2*w0*w0);
	}
	
	public synchronized double calculateOutput(double theta, double thetaDot){
		double u;
		int sign;
		//double signThing = thetaDot*Math.cos(theta);
		double minAngle = Math.PI*0;
		if((Math.abs(theta)>minAngle)){
			if(thetaDot*Math.cos(theta)>0){
				sign = -1;
			}else{
				sign = 1;
			}
			
			u=k*(Math.cos(theta)-1+c*thetaDot*thetaDot)*sign;
		}else{
			u = 0;
		}
		return u;
	}
	
	
	public synchronized void setK(double k){
		
		this.k = k;
	}
	
	public synchronized double getK(){
		
		return this.k;
	}
}