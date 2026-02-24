package eduni.distributions;

import eduni.distributions.DiscreteGenerator;
import eduni.distributions.Generator;
import eduni.distributions.ParameterException;

/**
 * A random number generator based on the Binomial distribution.
 * @version     1.0, 2 October 2002
 * @author      F.Mallet from Costas Simatos's original
 */

public class Binomial extends Generator implements DiscreteGenerator {
  private double prob;
  private int trials;

    /**
     * the seed is automatically provided by the <code>SeedGenerator</code>
     * @param prob The probability of success in a trial
     * @param trials The number of trials
     */
    public Binomial(double prob, int trials) {
	super();
	set(prob, trials);
    }

    /**
     * The constructor with which a specific seed is set for the random
     * number generator
     * @param prob The probability of success
     * @param trials The number of trials
     * @param seed The initial seed for the generator, two instances with
     *             the same seed will generate the same sequence of numbers
     */
    public Binomial(double prob, int trials, long seed) {
	super(seed);
	set(prob, trials);
    }

    private void set(double prob, int trials) {
	if (prob <= 0.0)
	    throw new ParameterException("Binomial: The probability of success must be between 0 and 1.");
	if (trials <= 0)
	    throw new ParameterException("Binomial: The number of trials must be a positive integer.");
	this.prob = prob;
	this.trials = trials;
    }

    /**
     * Generate a new random number.
     * @return The next random number in the sequence
     */
    public long sample() { return distrib.binomial(prob, trials); }

    public String toString() { return "Binomial("+prob+", "+trials+")"; }
}

