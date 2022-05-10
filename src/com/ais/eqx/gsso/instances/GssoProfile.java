package com.ais.eqx.gsso.instances;

public class GssoProfile {

	/* Relation between */
	private String	oper;
	private String	cos;
	private String	customerId;

	/*
	 * USMP RESPONSE
	 */
	private String	language	= "0";

	/*
	 * parameter "spName" from CheckPort Response
	 */
	private String	spName;

	public String getOper() {
		return oper;
	}

	public void setOper(String oper) {
		this.oper = oper;
	}

	public String getCos() {
		return cos;
	}

	public void setCos(String cos) {
		this.cos = cos;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getSpName() {
		return spName;
	}

	public void setSpName(String spName) {
		this.spName = spName;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

}
