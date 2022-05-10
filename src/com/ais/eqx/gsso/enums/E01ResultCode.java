package com.ais.eqx.gsso.enums;

public enum E01ResultCode {

	SUCCESS("0", "Success"), NO_SUCH_OBJECT("32", "Not found"), OTHER_CODE("00", "Error");

	private String	code;
	private String	description;

	private E01ResultCode(final String codeNumber, final String description) {
		this.setCodeNumber(codeNumber);
		this.setDescription(description);
	}

	public String getCode() {
		return code;
	}

	public void setCodeNumber(final String codeNumber) {
		this.code = codeNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

}
