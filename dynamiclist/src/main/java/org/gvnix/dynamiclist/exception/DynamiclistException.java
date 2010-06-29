package org.gvnix.dynamiclist.exception;

import java.util.Date;

public class DynamiclistException extends Exception {

	private static final long serialVersionUID = 1L;	
	private int code;
	private String exception;
	private String className;
	private String methodName;
	private Date date;
	
	
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	
	/**
	 * 
	 * @param code
	 * @param exception
	 * @param className
	 * @param method
	 */
	public DynamiclistException(int code, String exception, String className, String method) {
		super();
		this.code = code;
		this.exception = exception;
		this.className = className;
		this.methodName = method;
		this.date = new Date();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 */
	public String toString() {
		String lStrError = "\n#####################################################";
		lStrError += "\nFecha: " + date;
		lStrError += "\nCODIGO ERROR: " + code;
		lStrError += "\nClase: " + className;
		lStrError += "\nM�todo: " + methodName;
		lStrError += "\nExcepci�n:\n" + exception;
		lStrError += "\n#####################################################\n";

		return lStrError;
	}
}
