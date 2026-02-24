package eduni.distributions;

/**
 * Error thrown when a distribution is passed incorrect parameters.
 * @version     1.0, 2 october 2002
 * @author      F.Mallet from Costas Simatos's original
 */
public class ParameterException extends Error {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * The constructor for errors with a message.
     * @param name The error's message
     */
    public ParameterException(String msg) { super(msg); }

    /** The constructor for errors without a message.
     */
    public ParameterException() { super(); }
}
