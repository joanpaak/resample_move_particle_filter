#Sequential estimation of a simple linear model
In this tutorial I will show how to estimate a linear model sequentially.

##Setting up the Java-file
First, you will have to add ParticleFilter.jar to your classpath. After this, two classes have to written that extend classes from the Particle Filter library:

LinearModel which extends Model and LinModDataPoint which extends DataPoint

Nomen est omen: LinearModel class defines the model to be used by the Particle Filter, and LinModDataPoint the type of data points it uses. The Model class will define the prior distribution, as well as the log-likelihood function. NOTE: NOT negative likelihood! DataPoint class is simply a vessel for storing data points, in this case, pairs of x and y values. Against good Java practices these are defined as public attributes; in real applications one should probably write proper constructors, getters and setters.

The Particle Filter library comes equipped with some utility functions relating to the Normal Distribution. The logPDF function is used here to define the likelihood function. Functions from any library can be used in defining the likelihood, however, since the Filter works on unconstrained space, the normal distribution is often a practical choice for the prior.

##Constrained/Unconstrained space
As already mentioned, the Particle Filter works in unconstrained space, this means that each parameter is assumed to be defined on the real number line (between negative and positive infinity). For the parameters of the linear function this is not a problem, but the third parameter, standard deviation, is by definition positive. This problem is subverted by defining it instead on the log scale. In the code this can be seen from the fact that in the likelihood function the standard deviation parameter is exponentiated--which is the inverse of log transform.

##Running the program
The program will print on each trial the current posterior means. If everything's set up properly, one can marvel at how the values get closer to the generating parameters as more observations are added.