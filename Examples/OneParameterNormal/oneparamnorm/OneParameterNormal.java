package oneparamnorm;

import java.util.ArrayList;

import particlefilter.DataPoint;
import particlefilter.Model;
import particlefilter.ParticleFilter;
import statistical_functions.NormalDistribution;

/**
 * Observations come from a normal distribution with known variance and 
 * unknown mean. Particle Filter is compared against analytically calculated
 * posterior.
 * 
 * @author Joni
 *
 */

/**
 * Model for normal distribution with known variance (1.0) and unknown
 * mean.
 * @author Joni
 *
 */
class OneParamNormalModel extends Model{

	@Override
	public double logLikelihood(DataPoint[] y, double[] theta) {
		
		double ll = 0;
		
		for(int i = 0; i < y.length; i++) {
			NormalDataPoint dp = (NormalDataPoint) y[i];
			
			ll += NormalDistribution.logPDF(dp.y, theta[0], 1.0);
			
		}
		
		return ll;
	}
}

class NormalDataPoint extends DataPoint{
	
	public final double y;
	
	NormalDataPoint(double y_){
		y = y_;
	}
}

public class OneParameterNormal {
	
	public static void main(String[] args) {
		
		int finalN = 100;
		
		// The model is instantiated and its prior is set, after that 
		// the Particle Filter is created with the model and number of
		// particles as an input:
		
		OneParamNormalModel mdl = new OneParamNormalModel();
		mdl.setPrior(new double[] {0.0}, new double[] {2.0});
		
		ParticleFilter pf = new ParticleFilter(1000, mdl);
		
		// This all is just for the analytical calculations of the 
		// posterior distribution, and for conveniently printing 
		// the trial-by-trial estimates afterwards:
		
		ArrayList<DataPoint> datset = new ArrayList<DataPoint>();

		ArrayList<Double> pf_mus   = new ArrayList<Double>();
		ArrayList<Double> true_mus = new ArrayList<Double>();
		
		ArrayList<Double> pf_sds   = new ArrayList<Double>();
		ArrayList<Double> true_sds = new ArrayList<Double>();
		
		// Particle Filter is run in this loop. At the beginning of each iteration
		// a random observation from is generated (y ~ N(0.5, 1.0)) and the 
		// filter updated.
		
		for(int i = 0; i < finalN; i++) {
			NormalDataPoint new_y = new NormalDataPoint(NormalDistribution.genSTDNormalRand() + 0.5);

			pf.addObservation(new_y);
			
			// That is all that is needed for updating the Particle Filter.
			// This all below here is just for the analytical calculations and for 
			// storing the results in a convenient form for printing them to the
			// console later:
			
			datset.add(new_y);
			
			pf_mus.add(pf.getMarginalMeans()[0]);
			true_mus.add(getPosteriorMean(datset, mdl));
		
			pf_sds.add(pf.getMarginalSds()[0]);
			true_sds.add(getPosteriorSD(datset, mdl));
		}
		
		// This loop just prints the particle filter approximations ("PF") of means and 
		// standard deviations of the posterior distribution against analytically
		// calculated ("true"):
		
		for(int i = 0; i < finalN; i++) {
			
			System.out.format("Observation %03d: " , i+1);
			
			System.out.format("Mean:%5.2f (PF)",  pf_mus.get(i));
			System.out.format(",%5.2f (true)",  true_mus.get(i));
			
			System.out.format("| SD:%5.2f (PF)",  pf_sds.get(i));
			System.out.format(",%5.2f (true) %n", true_sds.get(i));
			
		}
	}
	
	// THESE METHODS ARE USED FOR CALCULATING POSTERIOR ANALYTICALLY
	// Formulae are from "Bayesian Data Analysis" by Gelman et al., 3rd edition, page 42.
	
	public static double getPosteriorSD(ArrayList<DataPoint> y, OneParamNormalModel mdl) {
		double sd = Math.sqrt(1.0 / ((1.0 / Math.pow(mdl.getPriorSDs()[0], 2.0)) + 
				    (y.size() / variance(y))));
		return sd;
	}
	
	public static double getPosteriorMean(ArrayList<DataPoint> y, OneParamNormalModel mdl) {
		
		double m = ((mdl.getPriorMus()[0] / Math.pow(mdl.getPriorSDs()[0], 2.0)) + 
				   ((y.size() / variance(y)) * mean(y))) /
			       ((1.0 / Math.pow(mdl.getPriorSDs()[0], 2.0)) + (y.size() / variance(y)));
				
		return m;
	}
	
	// SOME UTILITY UTILITY FUNCTIONS:
	
	public static double mean(ArrayList<DataPoint> y) {
		
		if(y.size() == 1) return ((NormalDataPoint) y.get(0)).y;
		
		return y.stream().mapToDouble(d -> ((NormalDataPoint) d).y).sum() / y.size();
	}
	
	public static double variance(ArrayList<DataPoint> y) {
		// TODO: This is a terrible hack... but seems to work.
		if(y.size() == 1) return 1.0;
		
		double mean = mean(y);
		
		return y.stream().mapToDouble(d -> Math.pow(((NormalDataPoint) d).y - mean, 2.0)).sum() / y.size();
		
	}
}
