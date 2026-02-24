package eduni.distributions;

/**
 * A random number generator based on the Erlang distribution.
 * @version     1.0, 2 October 2002
 * @author      F.Mallet from Costas Simatos's original
 */

public class Erlang extends Generator implements ContinuousGenerator {
  private double scale, shape;
    
    /**
     * the seed is automatically provided by the <code>SeedGenerator</code>
     * @param scale The scale of the distribution
     * @param shape The shape of the distribution
     */
    public Erlang(double shape, double scale) {
	super();
	set(scale, shape);
    }

    /**
     * The constructor with which a specific seed is set for the random
     * number generator
     * @param scale The scale of the distribution
     * @param shape The shape of the distribution
     * @param seed The initial seed for the generator, two instances with
     *             the same seed will generate the same sequence of numbers
     */
    public Erlang(double shape, double scale, long seed) {
	super(seed);
	set(scale, shape);
    }
    
    private void set(double scale, double shape) {
	if (scale <= 0.0)
	    throw new ParameterException("Erlang: The scale parameter must be greater than 0.");
	this.scale = scale;
	this.shape = shape;
    }

    /**
     * Generate a new random number.
     * @return The next random number in the sequence
     */
    public double sample() { return distrib.erlang(shape, scale); }
}
