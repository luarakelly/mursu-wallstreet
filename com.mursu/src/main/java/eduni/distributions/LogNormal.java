package eduni.distributions;

import eduni.distributions.Normal;

/**
 * A random number generator based on the lognormal distribution.
 * @version     1.0, 2 October 2002
 * @author      F.Mallet from Costas Simatos's original
 */

public class LogNormal extends Normal {
    /**
     * the seed is automatically provided by the <code>SeedGenerator</code>
     * @param mean The mean of the distribution
     * @param variance The variance of the distribution
     */
    public LogNormal(double mean, double variance) {
	super(mean, variance);
    }

    /**
     * The constructor with which a specific seed is set for the random
     * number generator
     * @param mean The mean of the distribution
     * @param variance The variance of the distribution
     * @param seed The initial seed for the generator, two instances with
     *             the same seed will generate the same sequence of numbers
     */
    public LogNormal(double mean, double variance, long seed) {
	super(mean, variance, seed);
    }
    
    /**
     * Generate a new random number.
     * @return The next random number in the sequence
     */
    public double sample() { return distrib.lognormal2(mean, std_dev); }
}
