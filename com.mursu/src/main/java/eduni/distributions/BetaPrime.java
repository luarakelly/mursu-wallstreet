package eduni.distributions;

import eduni.distributions.Beta;

/**
 * A random number generator based on the beta prime distribution.
 * @version     1.0, 2 October 2002
 * @author      F.Mallet from Costas Simatos's original
 */

public class BetaPrime extends Beta {
    /**
     * the seed is automatically provided by the <code>SeedGenerator</code>
     * @param shape_a The a shape parameter of the distribution
     * @param shape_b The b shape parameter of the distribution
     */
    public BetaPrime(double shape_a, double shape_b) { 
	super(shape_a, shape_b);
    }

    /**
     * The constructor with which a specific seed is set for the random
     * number generator
     * @param shape_a The a shape parameter of the distribution
     * @param shape_b The b shape parameter of the distribution
     * @param seed The initial seed for the generator, two instances with
     *             the same seed will generate the same sequence of numbers
     */
    public BetaPrime(double shape_a, double shape_b, long seed) {
	super(shape_a, shape_b, seed);
    }

  /**
   * Generate a new random number.
   * @return The next random number in the sequence
   */
    public double sample() { return distrib.betaprime(shape_a, shape_b); }
}
