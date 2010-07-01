/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures     
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.dynamiclist.exception;

import java.util.Date;

public class DynamiclistDaoException extends Exception {

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
	public DynamiclistDaoException(int code, String exception, String className, String method) {
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
