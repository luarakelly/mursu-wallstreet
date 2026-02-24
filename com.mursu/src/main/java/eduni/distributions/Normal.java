package eduni.distributions;

/**
 * A random number generator based on the normal distribution.
 * @version     1.0, 2 October 2002
 * @author      F.Mallet from Costas Simatos's original
 */

public class Normal extends Generator implements ContinuousGenerator {
    protected double mean, std_dev;
    
    /**
     * the seed is automatically provided by the <code>SeedGenerator</code>
     * @param mean The mean of the distribution
     * @param variance The variance of the distribution
     */
    public Normal(double mean, double variance) {
	super();
	set(mean, variance);
    }

    /**
     * The constructor with which a specific seed is set for the random
     * number generator
     * @param mean The mean of the distribution
     * @param variance The variance of the distribution
     * @param seed The initial seed for the generator, two instances with
     *             the same seed will generate the same sequence of numbers
     */
    public Normal(double mean, double variance, long seed) {
	super(seed);
	set(mean, variance);
    }
    
    private void set(double mean, double variance) {
	if (variance <= 0.0)
	    throw new ParameterException("Normal: The variance must be greater than 0.");
	this.mean = mean;
	this.std_dev = Math.sqrt(variance);
    }

    /**
     * Generate a new random number.
     * @return The next random number in the sequence
     */
    public double sample() { return distrib.normal2(mean, std_dev); }
}
