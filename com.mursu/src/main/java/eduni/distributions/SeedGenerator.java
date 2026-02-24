package eduni.distributions;

/** A SeedGenerator is a DiscreteGenerator which produces well-spaced seeds
 *  according to its spacing attribute. It relies on a <code>RandomGenerator</code>
 */
public class SeedGenerator implements DiscreteGenerator {
    private static long root = 4851L; // The root seed
    private int spacing;
    private boolean not_sampled = true; // to use the assigned seed (id Costas)
    private RandomGenerator source;

    public SeedGenerator () { this(root, 100000); } //default spacing=100000
    public SeedGenerator (long seed, int spacing) { 
	source = new RandomGenerator(seed);
	this.spacing = spacing;
    }

    // ----- implements Seedable { -----
    public void setSeed(long seed) { source.setSeed(seed); not_sampled=true; }
    public long getSeed() { return source.getSeed(); }
    public void reseed() { source.reseed(); not_sampled=true; }
    // ----- } implements Seedable -----

    // ----- implements DiscreteGenerator { -----
    public long sample() {
	if (not_sampled) not_sampled = false;
	else
	    for (int i=0; i<spacing; i++) source.nextLong();
	return getSeed();
    }

    private static SeedGenerator defaut = new SeedGenerator();
    // package: accessible only from the generators within the package
    static SeedGenerator getDefaultSeedGenerator() { return defaut; }
    static void setDefaultSeedGenerator(long seed, int spacing) {
	defaut = new SeedGenerator(seed, spacing);
    }
}
