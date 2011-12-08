package de.hsrm.mi.mobcomp.httpclientdemo.extra;

public class ExtraRunnable<T> implements Runnable {
	private T extra;

	public ExtraRunnable(T extra) {
		setExtra(extra);
	}

	public void setExtra(T extra) {
		this.extra = extra;
	}

	public T getExtra() {
		return extra;
	}

	@Override
	public void run() {
	}
}
