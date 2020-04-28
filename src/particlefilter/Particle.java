package particlefilter;

/**
 * Class for storing pairs of parameter values (theta) weights, the 
 * combination of which is known, obviously, as a Particle. 
 * Weights are stored as a default in logarithmic form.
 * @author Joni
 *
 */

public class Particle {
	
	private double   logWeight;
	private double[] theta;
	
	Particle(){
		
	}
	
	Particle(Particle thetaToCopy){
		logWeight = thetaToCopy.getLogWeight();
		theta     = thetaToCopy.getTheta();
	}
	
	/**
	 * Returns the exponentiated weight.
	 * @return exp(log_weight)
	 */
	public double getWeight() {
		return Math.exp(logWeight);
	}
	
	public double getLogWeight() {
		return logWeight;
	}
	
	public void setLogWeight(double logWeight_) {
		logWeight = logWeight_;
	}
	
	/**
	 * Returns the set of parameter values.
	 * @return Parameter values, theta.
	 */
	public double[] getTheta() {
		return theta;
	}
	
	public void setTheta(double[] theta_) {
		theta = theta_;
	}
	

}
