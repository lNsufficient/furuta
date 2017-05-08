public class EnergyControllerFuruta {
	private double cap, k, c;

	public EnergyControllerFuruta (double n, double k){
		double M,J,l,g,w0;
		this.k=k;
		M=0.01;
		l=0.413;
		J=0.05;
		g=9.81;
		cap=g*n;
		w0=Math.sqrt(M*g*l/J);
		c=1/(2*w0*w0);
	}

	public synchronized double calculateOutput(double theta, double thetaDot){
		double u;
		if((theta>Math.PI/2)&&(theta<Math.PI)){
			u=k*(Math.cos(theta)-1+c*thetaDot*thetaDot)*thetaDot*Math.cos(theta);
			if(u>cap){
				u=cap;
			}else if(u<-cap){
				u=-cap;
			}
		}else{
			u=0;
		}
		return u;
	}
}