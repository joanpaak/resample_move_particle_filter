package simplelinarmdl;

import statistical_functions.NormalDistribution;
import particlefilter.DataPoint;
import particlefilter.Model;
import particlefilter.ParticleFilter;

class LinearModel extends Model{
	
	/**
	 * The log-likelihood is defined in such a way that the array theta
	 * has always the parameters in this order: {intercept, slope, log standard deviation}
	 */
	@Override
	public double logLikelihood(DataPoint[] datset, double[] theta) {
		
		double ll = 0;
		
		for(int i = 0; i < datset.length; i++) {
			
			LinModDataPoint d = (LinModDataPoint) datset[i]; 
			
			ll += NormalDistribution.logPDF(d.y, theta[0] + d.x * theta[1], Math.exp(theta[2]));
			
		}
		
		return ll;
	}
}

class LinModDataPoint extends DataPoint{
	
	public final double x;
	public final double y;
	
	public LinModDataPoint(double x_, double y_) {
		x = x_;
		y = y_;
	}
}

public class SimpleLinearModel {
	
	public static void  main(String[] args)  {
		
		// Final number of observations:
		final int N  = 100;
		
		// Generating parameters for the simulated data:
		final double[] genTheta = new double[] {-0.5,  1.5, Math.log(0.5)}; // log(0.5) ~ -0.69
		
		// Create an instance of the LinearModel class and set its prior. The marginal means (first argument) and
		// standard deviations (second argument) should be in the same order as they are in the log-likelihood
		// function.
		
		LinearModel lm = new LinearModel();
		lm.setPrior(new double[] {0.0, 0.0, 0.0}, new double[] {5.0, 5.0, 1.0});
		
		// ParticleFilter is instantiated by supplying it with the desired amount of
		// particles and the model it should use:
		
		ParticleFilter pf = new ParticleFilter(1000, lm);
		
		// The actual filtering is done in a loop:
		for(int i = 0; i < N; i++) {
			
			// First a data point is generated randomly:

			LinModDataPoint newdatapoint = generateDataPoint(genTheta);
			
			// The data point is added to the filter. This will automatically
			// update the filter, and rejuvenate it if effective sample size
			// gets too small:
			
			pf.addObservation(newdatapoint);
			
			printCurrentState(pf);
		
		}
	}
	
	/**
	 * Generates random observations for a simple linear model.
	 * @param genTheta Generating parameters. Should be {intercept, slope, log standard deviation}
	 * @return
	 */
	public static LinModDataPoint generateDataPoint(double[] genTheta) {
		
		double x = Math.random() * 10 - 5; 
		double y = (genTheta[0] + x * genTheta[1]) + 
				NormalDistribution.genSTDNormalRand() * Math.exp(genTheta[2]); 
		
		LinModDataPoint newdp = new LinModDataPoint(x, y);
		
		return newdp;
	}
	
	public static void printCurrentState(ParticleFilter pf) {
		System.out.format("Marginals of posterior after %d observations:%n", pf.getDataPoints().size());
		
		System.out.format("     Intercept  Slope  log_SD %n");
		
		System.out.format("Means: ");
		
		for(double e : pf.getMarginalMeans()) {
			System.out.format(" %5.2f ", e);
		}
		
		System.out.format("%nSDs:   ");

		for(double e : pf.getMarginalSds()) {
			System.out.format(" %5.2f ", e);
		}
		
		System.out.format("%n--------%n");
	}
}
