A Java implementation of the Resample/Move algorithm. The main source is

Chopin, N. (2002): A sequential particle filter method for static models. Biometrika, 89, 3, pp. 539–551

## How, what: a short description of getting started

Get the jar-file, check out the examples. 

## Steps to run the filter:

- Add ParticleFilter.jar to your classpath
- Create a type extending the abstract class DataPoint
- Create a type extending the abstract class Model
  - This should have the overriden method logLikelihood that calculates, well, the log likelihood
- Create an instance of your model class, and set its prior using the inherited setPrior-method 
- Create an instance of the ParticleFilter, using the number of particles and your model object as arguments
- Using the filter's addObservation-method add observations
- Look at e.g. the marginal means and standard deviations by using the filter's methods

### Details of implementation

The DataPoint classes in the examples are implemented by using public final attributes. This is in disagreement in so-called good Java practices, but I find this to be more in line with their intented purpose as pure data structures. 

Inside the log likelihood function - in the Model class - one needs to explicitly cast the DataPoint input into the specific type, in order to be able to access its fields and so on. I find this to be minor annoyance; maybe a better way exists. 

# Examples

There are two "getting started" type examples: OneParameterNormal and SimpleLinearModel. You should start by reading the descriptions below and then head down to the examples-folder and attempt to run them.

## One parameter normal:

Shows how to sequantially approximate a simple normal model with one unknown parameter (mean) using ParticleFilter.jar.

Topics:
 - How to set up the Particle Filter
 - How to run a particle filter sequentially and print estimates

The most basic example. Random observations are generated from a normal distribution with known variance and unknown mean. The posterior distribution for the mean parameter is approximated after each observation and compared against analytically calculated posterior distribution. 

## Simple linear model

Shows how to sequantially approximate a standard linear model using ParticleFilter.jar.

Topics:
 - Using particle filter when the parameter space is partially constrained
 - How to run a particle filter sequentially
 
A very basic example, as the name implies. Observations arrive sequantially from a linear model (y ~ N(a + bx, SD)), and the posterior distribution is approximated after each observation. Assumes that you are comfortable with defining log-likelihood for a standard linear model, and know the basics of Bayesian inference. 

# Some aspects of the Filter

## Two modes of operation

The main intended purpose is to be used in sequential approximation, where fast approximations of posterior densities are needed after each observation. This is accomplished by using the addObservation method which will update the filter. 

Another possibility is to use the runOnADataSet method which takes a whole dataset as an input. However, this is really just calls the addObservation method in a loop. 

## Resampling limit

The default is at 0.5, this means that when effective sample size (1.0 / sum(w^2)) drops beyond half of the number of particles, the particle set will be rejuvenated. If resampling limit is set to 0.0 (or negative) the particle set is never resampled, and the algorithm functions essentially like (sequential) importance sampling. 
