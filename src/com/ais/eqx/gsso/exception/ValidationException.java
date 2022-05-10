package com.ais.eqx.gsso.exception;

import com.ais.eqx.gsso.enums.JsonResultCode;

public class ValidationException extends Exception {

	/**
     * 
     */
	private static final long	serialVersionUID	= 1L;

	private String				code;
	private String				mandatoryPath;
	private JsonResultCode		jsonCode;

	public ValidationException(final String path, final String resultCode, final String description) {
		super(description);
		this.setResultCode(resultCode);
		this.setMandatoryPath(path + " is");
	}

	public ValidationException(final String path, final JsonResultCode jsonCode, final String description) {
		super(description);
		this.setJsonResultCode(jsonCode);
		this.setMandatoryPath(path + " is");
	}

	public String getMandatoryPath() {
		return mandatoryPath;
	}

	public void setMandatoryPath(final String mandatoryPath) {
		this.mandatoryPath = mandatoryPath;
	}

	public JsonResultCode getJsonResultCode() {
		return jsonCode;
	}

	public void setJsonResultCode(final JsonResultCode jsonCode) {
		this.jsonCode = jsonCode;
	}

	public String getResultCode() {
		return code;
	}

	public void setResultCode(final String resultCode) {
		this.code = resultCode;
	}

}
