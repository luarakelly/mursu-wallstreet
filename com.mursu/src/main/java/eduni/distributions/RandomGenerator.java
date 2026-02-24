package eduni.distributions;

/**
 * A random number generator producing pseudorandom numbers based
 * on the value of a specific seed. The generator is a multiplicative
 * linear congruential generator. It generates numbers through the
 * following structure:
 * <p>
 * <code>Y[1] = (742938285*Y[0]) mod (2<sup>31</sup>-1)</code>
 * <p>
 * The seed (<code>Y[0]</code>) provided is used to generate a sequence of pseudorandom
 * numbers uniformly distributed between <code>0</code> and <code>1</code>. The cycle of the generator
 * is <code>2<sup>31</sup>-2</code>.
 * @version 1.0, 2 October 2002
 * @author F.Mallet from Costas Simatos original
 */

public class RandomGenerator implements ContinuousGenerator {
    // The multiplier
    private final long a = 742938285;
    // The modulus
    private final long m = 2147483647;
    // The last computed random number
    private long seed;
    
    public RandomGenerator () { reseed(); }
    public RandomGenerator (long seed) { setSeed(seed); }
    
    // ----- implements ContinuousGenerator { -----
    public double sample() { 
	return ((double)nextLong()) / m;
    }
    // ----- } implements ContinuousGenerator -----

    /**
     * @return The next long random number in the sequence
     */
    public long nextLong() {
	return seed = (a * seed) % m;
    }
    
    // ----- implements Seedable { -----
    public void setSeed(long seed) { this.seed = seed; }
    public long getSeed() { return seed; }
    public void reseed() { this.seed = SeedGenerator.getDefaultSeedGenerator().sample(); }
    // ----- } implements Seedable -----
}
