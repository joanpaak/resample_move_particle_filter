package tests;

import statistical_functions.NormalDistribution;

public class TestNormalDistribution {

	public static void main(String[] args) {
		testlogPDF();
	}
	
	public static void testlogPDF() {
		
		double tolerance = 0.0001;
		
		double[] testvals = new double[] {-3,-2, -1, 0, 1, 2, 3};
		
		// Mean 0, sd =  1
		double[] trueval_0_1 = new double[] {-5.4189385, -2.9189385, 
				-1.4189385, -0.9189385, -1.4189385, -2.9189385, -5.4189385};
		// Mean 1, sd  = 0.5
		double[] trueval_1_05 = new double[] {-32.2257914, -18.2257914,  
				-8.2257914,  -2.2257914,  -0.2257914,  -2.2257914, -8.2257914};
		
		for(int i = 0; i < testvals.length; i++) {
			if(Math.abs(NormalDistribution.logPDF(testvals[i], 0, 1) - trueval_0_1[i]) > tolerance) {
				System.out.println("FAIL");
			} else {
				System.out.println("PASS");
			}
			
			if(Math.abs(NormalDistribution.logPDF(testvals[i], 1, 0.5) - trueval_1_05[i]) > tolerance) {
				System.out.println("FAIL");
				System.out.println("Expected: " + trueval_1_05[i]);
				System.out.println("But was: " + NormalDistribution.logPDF(testvals[i], 1, 0.50));
			} else {
				System.out.println("PASS");
			}
		}
	}
	
}
