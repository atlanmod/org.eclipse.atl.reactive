package org.eclipse.m2m.atl.reactive.model;

public abstract class LazyModelRuntimeException extends RuntimeException{
	
	

	private static final long serialVersionUID = 1L;

		/**
		 * Constructs a new execution exception with the specified detail message.
		 * 
		 * @param message
		 *            the detail message
		 */
	public LazyModelRuntimeException(String message) {
		super(message);
	}

		/**
		 * Constructs a new execution exception with the specified detail message.
		 * 
		 * @param message
		 *            the detail message
		 * @param cause
		 *            the cause
		 */
	public LazyModelRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}

