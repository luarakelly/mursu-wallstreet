package eduni.distributions;

/**
 * A random number generator based on the Bernoulli distribution.
 * @version     1.0, 2 October 2002
 * @author      F.Mallet from Costas Simatos's original
 */

public class Bernoulli extends Generator implements DiscreteGenerator {
    private double prob;

    /**
     * the seed is automatically provided by the <code>SeedGenerator</code>
     * @param prob The probability of success
     */
    public Bernoulli(double prob) {
	super();
	set(prob);
    }

    /**
     * The constructor with which a specific seed is set for the random
     * number generator
     * @param prob The probability of success
     * @param seed The initial seed for the generator, two instances with
     *             the same seed will generate the same sequence of numbers
     */
    public Bernoulli(double prob, long seed) {
	super(seed);
	set(prob);
    }

    private void set(double prob) throws ParameterException {
	if ((prob < 0.0) || (prob > 1.0))
	    throw new ParameterException("Bernouilli: The probability of success must be between 0 and 1.");
	this.prob = prob;
    }

    /**
     * Generate a new random number.
     * @return The next random number in the sequence
     */
    public long sample() { return distrib.bernoulli(prob); }

    public String toString() { return "Bernoulli("+prob+")"; }
}

