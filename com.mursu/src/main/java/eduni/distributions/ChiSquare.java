package eduni.distributions;

/**
 * A random number generator based on the Chi-Square distribution.
 * @version     1.0, 2 October 2002
 * @author      F.Mallet from Costas Simatos's original
 */

public class ChiSquare extends Generator implements ContinuousGenerator {
    private long deg_freedom;
    
    /**
     * the seed is automatically provided by the <code>SeedGenerator</code>
     * @param deg_freedom The degrees of freedom for the distribution
     */
    public ChiSquare(long deg_freedom) {
	super();
	set(deg_freedom);
    }

    /**
     * The constructor with which a specific seed is set for the random
     * number generator
     * @param deg_freedom The degrees of freedom for the distribution
     * @param seed The initial seed for the generator, two instances with
     *             the same seed will generate the same sequence of numbers
     */
    public ChiSquare(long deg_freedom, long seed) {
	super(seed);
	set(deg_freedom);
    }
    
    private void set(long deg_freedom) {
	if (deg_freedom <= 0L)
	    throw new ParameterException("ChiSquare: The degrees of freedom must be a positive integer.");
	this.deg_freedom = deg_freedom;
    }
    
    /**
     * Generate a new random number.
     * @return The next random number in the sequence
     */
    public double sample() { return distrib.chisquare(deg_freedom); }
}
