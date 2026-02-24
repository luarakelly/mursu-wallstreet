package eduni.distributions;

/** represents classes which has a seed
 * @see Generator
 * @see ContinuousGenerator
 * @see DiscreteGenerator
 */
public interface Seedable {
    /**
     * Set the random number generator's seed.
     * @param seed The new seed for the generator
     */
    void setSeed(long seed);
    
    /**
     * Get the random number generator's seed.
     * @return The generator's seed
     */
    long getSeed();

    /**
     * Get another seed well-spaced (from the default <code>SeedGenerator</code>)
     */
    void reseed();
}
