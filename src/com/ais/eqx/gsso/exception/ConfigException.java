package com.ais.eqx.gsso.exception;

public class ConfigException extends Exception {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private String				writeLog;

	public ConfigException(final String writeLog) {
		super();
		this.writeLog = writeLog;
	}

	public ConfigException() {
	}

	public String getWriteLog() {
		return writeLog;
	}

	public void setWriteLog(final String writeLog) {
		this.writeLog = writeLog;
	}

}
