package eduni.distributions;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Generator;
import eduni.distributions.ParameterException;

/**
 * A random number generator based on the F-distribution.
 * @version     1.0, 2 October 2002
 * @author      F.Mallet from Costas Simatos's original
 */

public class FDistribution extends Generator implements ContinuousGenerator {
  private long num_deg_freedom, den_deg_freedom;

    /**
     * the seed is automatically provided by the <code>SeedGenerator</code>
     * @param num_deg_freedom The numerator degrees of freedom for the distribution
     * @param den_deg_freedom The denominator degrees of freedom for the distribution
     */
    public FDistribution(long num_deg_freedom, long den_deg_freedom) {
	super();
	set(num_deg_freedom, den_deg_freedom);
    }
    
    /**
     * The constructor with which a specific seed is set for the random
     * number generator
     * @param num_deg_freedom The numerator degrees of freedom for the distribution
     * @param den_deg_freedom The denominator degrees of freedom for the distribution
     * @param seed The initial seed for the generator, two instances with
     *             the same seed will generate the same sequence of numbers
     */
    public FDistribution(long num_deg_freedom, long den_deg_freedom, long seed) {
	super(seed);
	set(num_deg_freedom, den_deg_freedom);
    }
    
    private void set(long num_deg_freedom, long den_deg_freedom) {
	if ((num_deg_freedom <= 0L) || (den_deg_freedom <= 0L))
	    throw new ParameterException("FDistribution: The degrees of freedom must be positive integers.");
	this.num_deg_freedom = num_deg_freedom;
	this.den_deg_freedom = den_deg_freedom;
    }

    /**
     * Generate a new random number.
     * @return The next random number in the sequence
     */
    public double sample() { 
	return distrib.f(num_deg_freedom, den_deg_freedom); 
    }
}
