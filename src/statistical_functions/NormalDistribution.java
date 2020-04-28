package statistical_functions;

/**
 * The prior is usually assumed to be normally distributed in  
 * addition to proposals being drawn from that exact distribution, so 
 * there are methods for dealing with those situations. 
 * 
 * Everything's static for that nice functional programming experience. 
 * 
 * @author Joni
 *
 */
public interface NormalDistribution {
	
	public static double pdf(double x, double mu, double  sd) {
		
		double pdf = 1.0 / (sd * Math.sqrt(2 * Math.PI)) * 
				Math.exp(-0.5 * (((x - mu) / sd) * ((x - mu) / sd)));
		
		return pdf;
	}
	
	public static double logPDF(double x, double mu, double  sd) {
		return Math.log(pdf(x, mu, sd));
	}
	
	
	/**
	 * Generates a random standard normal deviate using the Box-Muller method.
	 * http://www.mat.ufrgs.br/~viali/estatistica/mat2274/material/textos/p376-muller.pdf
	 */ 
	public static double genSTDNormalRand() {
		double U1 = Math.random();
		double U2 = Math.random();
		
		// TODO: Math.log(0) might cause problems.
		
		return Math.pow(-2.0 * Math.log(U1), 0.5) * Math.cos(2.0 * Math.PI * U2);
	}

}
