package eduni.distributions;

import eduni.distributions.Seedable;

/** A continuous generator provides a long value according to the distribution it relies on.
 */
public interface DiscreteGenerator extends Seedable {
    long sample(); 
}
