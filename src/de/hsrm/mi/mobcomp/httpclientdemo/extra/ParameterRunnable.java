package de.hsrm.mi.mobcomp.httpclientdemo.extra;

/**
 * Ein generisches Runnable, dem man einen Parameter übergeben kann.
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 *
 * @param <T>
 */
abstract public class ParameterRunnable<T> implements Runnable {
	private T parameter;
	
	public ParameterRunnable() {
	}

	public ParameterRunnable(T parameter) {
		setParameter(parameter);
	}

	public void setParameter(T parameter) {
		this.parameter = parameter;
	}

	public T getParameter() {
		return parameter;
	}
}
