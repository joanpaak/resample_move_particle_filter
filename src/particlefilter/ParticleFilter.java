package particlefilter;

import java.util.ArrayList;

import statistical_functions.GenericStatsFunctions;
import statistical_functions.NormalDistribution;

/**
 * Two ways to run: 1) Add observations on trial-to-trial basis
 *                  2) Run on a whole set of ArrayList<DataPoint> datapoints
 * @author Joni
 *
 */
public class ParticleFilter {
	
	private int   NParticles;
	private Model model;	
	private ArrayList<Particle> particles;

	private double resamplingLimit = 0.5;
	
	// Data recorded during "run time":
	
	private ArrayList<Double>    effectiveSampleSizes  = new ArrayList<Double>();
	private ArrayList<DataPoint> datapoints            = new ArrayList<DataPoint>();
	private ArrayList<Integer>   wasResampledAfterObs  = new ArrayList<Integer>();
	private ArrayList<Double>    acceptanceRatios      = new ArrayList<Double>();
	
	private boolean useuniformProposalDistribution = false;

	// These attributes are used during the resample-move step to save
	// the pre-resampled estimates to be used in constructing the 
	// proposal distribution:
	
	private double[] mus;
	private double[] sds;
	
	
	/**
	 * 
	 * @param nparticles Number of particles used by the filter
	 * @param m The statistical model (type extending the class Model) used for calculating log-likelihoods and log-priors.
	 * 
	 * @throws NullPointerException if model has not been correctly set
	 * @throws IllegalStateException if prior has not been set for the model.
	 */
	public ParticleFilter(int nparticles, Model m) {
		
		if(nparticles <= 0) throw new IllegalStateException("Error while instantiating the particle filter: "
				+ " Number of particles should be a positive integer.");
		
		if(m == null) throw new NullPointerException("Error while instantiating the particle filter: "
				+ "Model has not been set correctly, was null.");
		
		if(m.getPriorMus() == null || m.getPriorSDs() == null) throw new IllegalStateException("Error while instantiating the particle filter: "
				+ "Prior has not been set for the model");
		
		model  = m;
		NParticles = nparticles;
		
		particles = drawParticles(model.getPriorMus(), model.getPriorSDs());
	}
		
	/**
	 * This method runs the particle filter on a whole data set.
	 * In practice, this just calls the addObservation-method
	 * in a loop, check documentation for it for more information.
	 * 
	 * @param dset A complete data set to run the filter on 
	 */
	public void runOnADataSet(ArrayList<DataPoint> dset)  {
		
		for(int i = 0; i < dset.size(); i++) {
			addObservation(dset.get(i));
		}
		
	}
	
	/**
	 * This method is used for adding a single observation to the filter. The
	 * weights are automatically updated. If effective sample size gets below 
	 * resamplingLimit (settable by calling setResamplingLimit()) the particle
	 * set is rejuvenated. 
	 * 
	 * The DataPoints should be defined in such a way that the Model knows how
	 * to calculate the log-likelihood for them.
	 * 
	 * @param y A type extending the class DataPoint.
	 */
	public void addObservation(DataPoint y) {
		
		datapoints.add(y);
		
		reweight(y);
		
		effectiveSampleSizes.add(getNEff());
		
	    if(effectiveSampleSizes.get(effectiveSampleSizes.size() - 1) < 10) {
	    	System.err.println("WARNING: Effective sample size dangerously low: " + getNEff());
	    	// TODO: What to do?
	    }
		
		if((effectiveSampleSizes.get(effectiveSampleSizes.size() - 1) / (double) NParticles) < resamplingLimit){
			resample();
			move();
		}
	}
	
	/**
	 * Effective sample size is defined as 1.0 / sum(w^2), in which w is the vector 
	 * of weights associated with the particles.
	 * @return Effective sample size, can be fractional. 
	 */
	public double getNEff() {
		double sumOfSquares = 0;
		
		sumOfSquares = particles.stream().mapToDouble(t -> t.getWeight() * t.getWeight()).sum();
		
		return 1.0 / sumOfSquares;
	}
	
	/**
	 * Sets the resampling limit as a fraction of the number of particles. 
	 * In most applications should be between 0.0 and 1.0. Values below 0.0
	 * imply that the set is never rejuvenated; values above 1.0 imply rejuvenation 
	 * after each step. 
	 * @param lim
	 */
	public void setResamplingLimit(double lim) {
		resamplingLimit = lim;
	}
	
	/**
	 * The idea is that one could use different proposal distributions. 
	 * The default is normal distribution. This is a clunky way of setting
	 * uniform distribution to be used as the proposal distribution. Min and
	 * max values are simply the min and max values of the particle set, not
	 * taking into account their weights. 
	 * 
	 * As indeed the idea is to have multiple different proposal distributions, 
	 * this will probably change at some point.
	 * 
	 * @param b
	 */
	public void useUniformProposals(boolean b) {
		useuniformProposalDistribution = b;
	}
	
	
	// public void writeParticlesToFile(){
	// TODO
	//  }
	//
	

	
	public double[] getMarginalMeans() {
		double[] estimates = new double[model.getNDim()];
		
		particles.stream().forEach(t -> {
			for(int i = 0; i < model.getNDim(); i++) {
				estimates[i] += t.getTheta()[i] * t.getWeight();
			}
		});
		
		return estimates;
	}
	
	public double[] getMarginalSds() {
		double[] means = getMarginalMeans();
		double[] sds   = new double[model.getNDim()];
		
		particles.stream().forEach(t -> {
			for(int i = 0; i < model.getNDim(); i++) {
				sds[i] += ((t.getTheta()[i] - means[i]) * (t.getTheta()[i] - means[i]))* t.getWeight();
			}
		});
		
		for(int i = 0; i < model.getNDim(); i++) {
			sds[i] = Math.sqrt(sds[i]);
		}
		
		return sds;
	}
	
	// Methods for getting arbitrary quantiles	
//		public double[] getQuantiles(double[] quantiles) {
//			//TODO: Get qquantiles, should call the singular
//			// getQuantile as many times as there are quantiles to get
//			return 0;
//		}
	//	
	
	/**
	 * This is called when proposals are to be drawn from the uniform
	 * distribution. See documentation for the method useUniformProposals
	 * for more information.
	 * @return
	 */
	private ArrayList<Particle> drawParticlesFromUniform() {
		
		ArrayList<Particle> newParticleList = new ArrayList<Particle>();
        
		int NDim = model.getNDim();
		
		double[] currmin = new double[NDim];
		double[] currmax = new double[NDim];
		
		double[] currtheta = particles.get(0).getTheta();
		
		for(int i = 0; i < NDim; i++) {
			currmin[i] = currtheta[i];
			currmax[i] = currtheta[i];
		}
		
		for(int j = 1; j < NParticles; j++) {
			currtheta = particles.get(j).getTheta();
			
			for(int i = 0; i < NDim; i++) {
				if(currmin[i] > currtheta[i]) currmin[i] = currtheta[i];
				if(currmax[i] < currtheta[i]) currmax[i] = currtheta[i];
			}
		}
		
		for(int i = 0; i < NParticles; i++) {
			Particle currParticle = new Particle();

			double[] currentTheta = new double[model.getNDim()];
			
			for(int j = 0; j < model.getNDim(); j++) {
				currentTheta[j] = Math.random() * (currmax[j] - currmin[j]) + currmin[j]; 

			}
			
			currParticle.setTheta(currentTheta);
			currParticle.setLogWeight(Math.log(1.0 / NParticles));
			
			newParticleList.add(currParticle);
		}

		
		return newParticleList;
	}
	
	/**
	 * Draws particles from the Normal Distribution. Used e.g. at the first step
	 * of the Particle Filter, and as a default proposal distribution. 
	 * 
	 * @param mu
	 * @param sd
	 * @return
	 */
	private ArrayList<Particle> drawParticles(double[] mu, double[] sd) {
        
		ArrayList<Particle> newParticleList = new ArrayList<Particle>();
        
		double w = Math.log(1.0/NParticles);
		
		for(int i = 0; i < NParticles; i++) {
			Particle currParticle = new Particle();

			double[] currentTheta = new double[model.getNDim()];
			
			for(int j = 0; j < model.getNDim(); j++) {
				currentTheta[j] = NormalDistribution.genSTDNormalRand() * sd[j] + mu[j]; 
//				currentTheta[j] = T_distribution.genRand() * sd[j] + mu[j]; 
			}
			
			currParticle.setTheta(currentTheta);
			currParticle.setLogWeight(w);
			
			newParticleList.add(currParticle);
		}
		
		return newParticleList;
	}
	
	private void resample() {
		
		// Estimates prior to resampling are saved and used
		// for the proposal distribution.
		mus = getMarginalMeans();
		sds = getMarginalSds();
		
//		System.out.println("Resampling...");
		
		int[] resampledIndices = new int[NParticles];
		
		double[] weights = new double[NParticles]; 

		for(int i = 0; i < NParticles; i++) {
			weights[i] = particles.get(i).getWeight();
		}
		
		for(int i = 0; i < NParticles; i++) {
			resampledIndices[i] = GenericStatsFunctions.genMultinomRandN(weights);
		}
		
		ArrayList<Particle> resampledParticles = new ArrayList<Particle>(); 
		
		for(int i = 0; i < NParticles; i++) {	
			resampledParticles.add(new Particle(particles.get(resampledIndices[i])));
		}
		
		
		particles = new ArrayList<Particle>();
		
		particles = resampledParticles;
		
		double w = Math.log(1.0 / NParticles);
		
		particles.stream().forEach(t -> t.setLogWeight(w));
		
	}
	
	private void move() {
		
		int NAccepted = 0;
		
		// Two proposal distributions are supported:
		// - Gaussian (based on the current marginal mus and sds). This is the default choice.
		// - Uniform: will generate proposals from the range of the particles.
	
		ArrayList<Particle> proposals;
		if(useuniformProposalDistribution) {
			proposals = drawParticlesFromUniform();
		} else {			
			proposals = drawParticles(mus, sds);		
		}

		// Weights are calculated for the particles and the proposals:
		// HOX: parallel stream, obviously, does not retain the ordering in these, but that
		// does not matter here, since all of the proposals are drawn from the same
		// distribution that is independent from the particles... if ordering was important, 
		// I reckon the particles should have something like "index" in them, or maybe the 
		// ratio calculating and so on should be done in the lambda expression...  
		
		particles.parallelStream().forEach(t -> {
			t.setLogWeight(model.logLikelihood(datapoints.toArray(new DataPoint[0]), 
					t.getTheta()) + model.logPrior(t.getTheta()));
		});
		
		proposals.parallelStream().forEach(t -> {
			t.setLogWeight(model.logLikelihood(datapoints.toArray(new DataPoint[0]), 
					t.getTheta()) + model.logPrior(t.getTheta()));
		});
	
		ArrayList<Particle> newSet = new ArrayList<Particle>();
		
		for(int i = 0; i < NParticles; i++) {
		
			double ratio = proposals.get(i).getLogWeight() - particles.get(i).getLogWeight();
			
			if(Math.log(Math.random()) < ratio || ratio > 0) {
				
				newSet.add(new Particle(proposals.get(i)));
				NAccepted++;
				
			} else {
				
				newSet.add(new Particle(particles.get(i)));
			}
		}
		
		double w = Math.log(1.0 / NParticles);
		
		newSet.stream().forEach(t -> t.setLogWeight(w));
		
		particles = new ArrayList<Particle>();
		
		particles = newSet;
		
		newSet = null;
		
		wasResampledAfterObs.add(datapoints.size());
		
		acceptanceRatios.add((double) NAccepted / (double) NParticles);
	}

	
	private void reweight(DataPoint y) {
		
		particles.parallelStream().forEach(t -> {
			t.setLogWeight(t.getLogWeight() + model.logLikelihood(new DataPoint[] {y}, t.getTheta()));
		});
		
		normalizeWeights();
	}
	
	private void normalizeWeights() {
		double logSumExp = Math.log(particles.stream().mapToDouble(t -> t.getWeight()).sum());
		
		particles.stream().forEach(t -> t.setLogWeight(t.getLogWeight() - logSumExp));
	}
	
	//
	
	public double getResamplingLimit() {
		return resamplingLimit;
	}
	
	public ArrayList<Particle> getParticles(){
		return particles;
	}
	
	public ArrayList<DataPoint> getDataPoints(){
		return datapoints;
	}
	
	public ArrayList<Double> getAcceptanceRatios() {
		return acceptanceRatios;
	}
	
}


