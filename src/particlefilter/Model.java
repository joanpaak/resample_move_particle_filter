package particlefilter;

import statistical_functions.NormalDistribution;

/**
 * The user should create their own extending class, supplying a method for
 * calculating the log-likelihood; everything else works by using these pre-
 * made methods. NOTE: the log-likelihood function is the log-likelihood func-
 * tion, NOT the negative log-likelihood!
 * 
 * @author Joni
 *
 */

public abstract class Model{
	
	private double[] priorMus;
	private double[] priorSDs;
	
	public abstract double logLikelihood(DataPoint[] y, double[] theta);
	
	public double logPrior(double[] theta) {
		
		double logprior = 0;
		
		for(int i = 0; i < getNDim(); i++) {
			logprior += NormalDistribution.logPDF(theta[i], priorMus[i], priorSDs[i]);
		}
		
		return logprior;
	}
	
	
	public int getNDim() {
		return getPriorMus().length;
	}
	
	public double[] getPriorMus() {
		return priorMus;
	}
	
	public double[] getPriorSDs() {
		return priorSDs;
	}
	
	/**
	 * 
	 * @param priorMus_
	 * @param priorSDs_
	 * @throws IllegalArgumentException if the lengths of the arguments don't match.
	 * @throws IllegalArgumentException if any of the prior means is NaN or infinite.
	 * @throws IllegalArgumentException if any of the prior standard deviatinos is >= 0.0.
	 */
	public void setPrior(double[] priorMus_, double[] priorSDs_) {
		
		if(priorMus_.length != priorSDs_.length) {
			throw new IllegalArgumentException("Error while setting the prior: "
					+ "Different amount of parameters implied by prior means and sds.");
		}
		
		for(double x : priorMus_) {
			if(Double.isInfinite(x) || Double.isNaN(x)) {
				throw new IllegalArgumentException("Error while setting the prior: "
						+ "Marginal mean of prior was inifinite or NaN");
			}
		}
		
		for(double x : priorSDs_) {
			if(Double.compare(x, 0) <= 0) {
				throw new IllegalArgumentException("Error while setting the prior: "
						+ "Marginal standard deviation of the prior was <= 0.0");
			}
		}
		
		priorMus = priorMus_;
		priorSDs = priorSDs_;
	}
	
}