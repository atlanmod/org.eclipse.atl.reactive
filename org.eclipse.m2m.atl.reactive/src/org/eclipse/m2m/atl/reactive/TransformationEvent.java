package org.eclipse.m2m.atl.reactive;

public class TransformationEvent {
	String message;
	long time;

	public TransformationEvent(String message, long time) {
		super();
		this.message = message;
		this.time = time;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String toString() {
		return message + ", " + time;
	}
}
