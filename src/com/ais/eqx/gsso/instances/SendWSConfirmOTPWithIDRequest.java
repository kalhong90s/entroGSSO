package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SendWSConfirmOTPWithIDRequest")
@XmlAccessorType(XmlAccessType.FIELD)

public class SendWSConfirmOTPWithIDRequest {
	
	@XmlElement(name = "msisdn")
	private String	msisdn					= null;
	
	@XmlElement(name = "password")
	private String	password				= null;
	
	@XmlElement(name = "sessionId")
	private String	sessionId				= null;
	
	@XmlElement(name = "CompanyID")
	private String	companyID				= null;
	
	@XmlElement(name = "CompanyName")
	private String	companyName				= null;

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getCompanyID() {
		return companyID;
	}

	public void setCompanyID(String companyID) {
		this.companyID = companyID;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
}
