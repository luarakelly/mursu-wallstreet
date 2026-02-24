package eduni.distributions;

/**
 * A random number generator based on the Cauchy distribution.
 * @version     1.0, 2 October 2002
 * @author      F.Mallet from Costas Simatos's original
 */

public class Cauchy extends Generator implements ContinuousGenerator {
    private double median, scale;
    
    /**
     * the seed is automatically provided by the <code>SeedGenerator</code>
     * @param median The median of the distribution
     * @param scale The scale of the distribution
     */
    public Cauchy(double median, double scale) {
	super();
	set(median, scale);
    }

    /**
     * The constructor with which a specific seed is set for the random
     * number generator
     * @param median The median of the distribution
     * @param scale The scale of the distribution
     * @param seed The initial seed for the generator, two instances with
     *             the same seed will generate the same sequence of numbers
     */
    public Cauchy(double median, double scale, long seed) {
	super(seed);
	set(median, scale);
    }
    
    private void set(double median, double scale) {
	if (scale <= 0.0)
	    throw new ParameterException("Cauchy: The scale parameter must be greater than 0.");
	this.median = median;
	this.scale = scale;
    }

    /**
     * Generate a new random number.
     * @return The next random number in the sequence
     */
    public double sample() { return distrib.cauchy(median, scale); }
}
