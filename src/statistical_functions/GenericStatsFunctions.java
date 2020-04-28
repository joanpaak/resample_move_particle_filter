package statistical_functions;

public interface GenericStatsFunctions {
	
	/**
	 * Generates a random distribution from a multinomial distribution. 
	 * Expects an array of probabilities as an input; the probabilities should 
	 * sum to one.
	 * The returned integer is between 0 and and the length of the input minus one. 
	 * @param p Array of probabilities.
	 * @return An integer between 0 and length of the input minus one. 
	 */
	public static int genMultinomRandN(double[] p) {
			
//		double sumOfP = DoubleStream.of(p).boxed()
//                .collect(Collectors.toList())
//                .stream().mapToDouble(d -> d)
//                .sum();
//		
//		if(Math.abs(1.0 - sumOfP) > 1e-4) {
//			System.err.println("Warning from multinomial sampler: sum was " + sumOfP);
//		}
		
		// Create cumulative sum: 
		
		double[] q = new double[p.length];
		
		q[0] = p[0];
		
		for(int i = 1; i < p.length; i++) {
			q[i] = q[i-1] + p[i];
		}
		
		q[p.length-1] = 1.00; // Ensuring that the cumulative sum reaches unity:
		
		double s = Math.random();
		
		int i = 0;
		
		while(s > q[i]) {
			i++;
		}

		return i;
	}
}
