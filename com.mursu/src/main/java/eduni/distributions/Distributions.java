package eduni.distributions;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.RandomGenerator;

/**
 * offers several discrete and continuous distributions all seeded by a common
 * well-spaced pseudo random number generator (PRNG).
 */ 
public class Distributions {
    /** A reference to the internal random generator.
     */
    ContinuousGenerator source;
    
    /** The seed is automatically provided by a well-spaced <code>SeedGenerator</code>
     */
    public Distributions() { source = new RandomGenerator(); }
    /** The seed is manually set */
    public Distributions(long seed) { source = new RandomGenerator(seed); }
    /** This constructor allows for manually specially the continous generator 
     * shared to compute the distribution function.
     */
    public Distributions(ContinuousGenerator gen) { source = gen; }

    // discrete distributions
    /** computes a Bernoulli's distribution
     * @return 1 iif <i>sample</i> &le; prob, 0 otherwise; <i>sample</i> is provided by the PRGN
     */
    public long bernoulli(double prob) { return source.sample()<=prob?1:0; }

    /** computes a binomial distribution
     * @return &sum;(i=0; i&lt;trials-1; bernoulli(prob))
     */
    public long binomial(double prob, int trials) {
	long sum = 0l;
	for (int i=0; i<trials; i++) sum += bernoulli(prob);
	return sum;
    }

    /** computes a geometric distribution
     * @return &lceil; log(<i>sample</i>)/log(1-prob) &rceil;; <i>sample</i> is provided by the PRGN
     */    
    public long geometric(double prob) {
	return (long)Math.ceil(Math.log(source.sample()) / Math.log(1.0-prob));
    }

    /** computes a Pascal's distribution
     * @return &sum;(i=0; i&lt;successes; geometric(prob))
     */
    public long pascal(double prob, int successes) {
	long sum = 0L;
	for (int i=0; i < successes; i++)
	    sum += geometric(prob);
	return sum;
    }

    /** computes a poisson distribution
     * @return the smallest integer x so as &prod;(i=0; i&lt;x; <i>sample</i>) &le; exp(-mean) 
     */
    public long poisson(double mean) {
	long x = -1L;
	double m = Math.exp(-mean), product=1;
	do {
	    x++;
	    product *= source.sample();
	} while(m < product);
	return x;
    }
    
    // continuous distributions
    private double power(double a) {
	return Math.pow(source.sample(), 1.0/a);
    }
    
    /** computes a beta distribution using Cheng's method (1978) when shape_a&gt;1 or shape_b&gt;1
     * and Berman's method (1970) otherwise
     */
    public double beta(double shape_a, double shape_b) {
	if (shape_a == 1.0)
	    return 1.0-power(shape_b);
	if (shape_b == 1.0)
	    return power(shape_a);
	
	if ((shape_a > 1.0) || (shape_b > 1.0)) {
	    // Cheng's method (1978)
	    double alpha = shape_a + shape_b;
	    double min = Math.min(shape_a, shape_b);
	    double beta;
	    if (min <= 1.0) beta = 1.0/min;
	    else
		beta = Math.sqrt((alpha - 2.0)/(2.0*shape_a*shape_b - alpha));
	    
	    double gamma = shape_a + 1.0/shape_b;
	    double w, el1, el2;
	    do {
		double u1 = source.sample(), 
		    u2 = source.sample(),
		    u3 = source.sample();
		double v = beta*Math.log(u1/(1.0-u1));
		w = alpha*Math.exp(v);
		el1 = alpha*Math.log(alpha/(beta+w)) + gamma*v - Math.log(4.0);
		el2 = Math.log(u1*u2*u3);
	    } while(el1 < el2);
	    return w/(beta+w);
	} 
	
	// Berman's method (1970)
	double x, y;
	do {
	    x = power(shape_a);
	    y = power(shape_b);
	} while((x + y) > 1.0);
	return x/(x+y);
    }

    /** computes a beta prime distribution
     * @return 1/beta(shape_a, shape_b) - 1
     */
    public double betaprime(double shape_a, double shape_b) {
	return 1.0/beta(shape_a, shape_b) - 1.0;
    }
    
    /** computes a Cauchy's distribution
     * @return median + scale/tan(&Pi; * <i>sample</i>); <i>sample</i> is provided by the PRGN
     */
    public double cauchy(double median, double scale) {
	return median + scale/Math.tan(Math.PI*source.sample());
    }

    /** computes a chisquare distribution
     * @return &sum;(i=0; i&lt;deg_freedom; normal(0,1))
     */
    public double chisquare(long deg_freedom) {
	// use normal2 because sqrt(1)=1
	double result = 0.0;
	for (long i=0L; i < deg_freedom; i++)
	    result += normal2(0.0, 1.0);
	return result;
    }
    
    /** computes an Erlang's distribution
     * @return -scale * log ( &prod;(i=0; i&lt;shape; <i>sample</i>) ); <i>sample</i> is provided by the PRGN
     */
    public double erlang(double scale, double shape) {
	double product = 1.0;
	for (int i=0; i<shape; i++)
	    product *= source.sample();
	return -scale * Math.log(product);
    }

    /** computes a F distribution
     * @return chisquare(num_deg_freedom)/num_deg_freedom / chisquare(den_deg_freedom)/den_deg_freedom
     */
    public double f(long num_deg_freedom, long den_deg_freedom) {
	return (chisquare(num_deg_freedom)/num_deg_freedom) 
	    / (chisquare(den_deg_freedom)/den_deg_freedom);
    }

    /** computes a gamma distribution
     */
    public double gamma(double scale, double shape) {
	if (shape == Math.floor(shape)) {
	    // The shape is an integer
	    double product = 1.0;
	    for (long i=0L; i < shape; i++)
	    product *= source.sample();
	    
	    return -scale*Math.log(product);
	} 
	// not an integer
	if (shape < 1.0)
	    return scale * beta(shape, 1.0-shape) * negexp(1.0);
	
	// shape >= 1.0
	double floor = Math.floor(shape);
	return gamma(scale, floor) + gamma(scale, shape-floor);
    }
    
    /** computes an invgamma distribution
     * @return 1.0/gamma(scale, shape)
     */
    public double invgamma(double scale, double shape) { 
	return 1.0/gamma(scale, shape); 
    }
    
    /** computes a logistic distribution
     * @return location - scale * log (1/<i>sample</i> - 1)
     */
    public double logistic(double location, double scale) {
	return location-scale*Math.log((1/source.sample())-1);
    }
    
    /** computes a lognormal distribution
     * @return exp(mean + &radic;variance * normal(0,1))
     */
    public double lognormal(double mean, double variance) { 
	return lognormal2(mean, Math.sqrt(variance));
    }

    /** computes a lognormal distribution
     * @param std_dev is assumed to be the square root of the variance.
     * @return exp(mean + std_dev * normal(0,1))
     */
    public double lognormal2(double mean, double std_dev) { 
	// use normal2 because sqrt(1)=1
	return Math.exp(mean+std_dev*normal2(0.0, 1.0));
    }

    /** computes a negexp distribution
     * @return -mean * log (<i>sample</i>);  where <i>sample</i> is provided by the PRGN
     */
    public double negexp(double mean) {
	return -mean * Math.log(source.sample());
    }
    
    /** computes a normal distribution; <i>sample</i> are provided by the same PRGN
     * @return mean + &radic;(variance) * cos (2&Pi; * <i>sample</i>) * &radic;(-2 * log (<i>sample</i>))
     */
    public double normal(double mean, double variance) { 
	return normal2(mean, Math.sqrt(variance));
    }

    /** computes a normal distribution; <i>sample</i> are provided by the same PRGN
     * @param std_dev is assumed to be the square root of the variance.
     * @return mean + std_dev * cos (2&Pi; * <i>sample</i>) * &radic;(-2 * log (<i>sample</i>))
     */
    public double normal2(double mean, double std_dev) {
	double u1 = source.sample(), u2 = source.sample();
	return mean + std_dev * Math.cos(2 * Math.PI * u1) * Math.sqrt(-2 * Math.log(u2));
    }

    /** computes a pareto distribution; <i>sample</i> is provided by the PRGN
     * @return scale / (<i>sample</i>^(1/shape))
     */
    public double pareto(double scale, double shape) { 
	return scale / Math.pow(source.sample(), 1/shape);
    }

    /** computes a tstudent distribution; <i>sample</i> is provided by the PRGN
     * @return normal(0,1)/&radic;(chisquare(deg_freedom)/deg_freedom)
     */
    public double tstudent(long deg_freedom) {
	// use normal2 because sqrt(1)=1
	return normal2(0.0, 1.0)/Math.sqrt(chisquare(deg_freedom)/deg_freedom);
    }

    /** computes a uniform distribution; <i>sample</i> is provided by the PRGN
     * @return min + (max-min) * <i>sample</i>
     */
    public double uniform(double min, double max) {
	return (max-min) * source.sample() + min;
    }

    /** computes a weibull distribution; <i>sample</i> is provided by the PRGN
     * @return scale * (<i>sample</i> ^ (1/shape))
     */
    public double weibull(double scale, double shape) {
	return scale * Math.pow(Math.log(source.sample()), 1/shape);
    }
}

