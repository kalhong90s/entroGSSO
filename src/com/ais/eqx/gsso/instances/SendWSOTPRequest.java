package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SendWSOTPRequest")
@XmlAccessorType(XmlAccessType.FIELD)

public class SendWSOTPRequest {
	/** AuthenOTP && ID && Create && Generate **/
	private String	msisdn			= null;
	
	@XmlElement(name = "OTPmobile")
	private String	otpMobile		= null;
	
	private String	email			= null;
	private String	link			= null;
	private String	service			= null;
	private String	accountType		= null;

	@XmlElement(name = "CompanyID")
	private String	companyID			= null;
	
	@XmlElement(name = "CompanyName")
	private String	companyName			= null;
	
	private String	waitDR			= null;
	private String	otpDigit		= null;
	private String	refDigit		= null;
	
	/** Create **/
	//use lifeTimeoutMin replace addTimeoutMins cuz old code is replace when get xml
	private String	lifeTimeoutMins	= null;

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getOtpMobile() {
		return otpMobile;
	}

	public void setOtpMobile(String otpMobile) {
		this.otpMobile = otpMobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
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

	public String getAddTimeoutMins() {
		return lifeTimeoutMins;
	}

	public void setAddTimeoutMins(String addTimeoutMins) {
		this.lifeTimeoutMins = addTimeoutMins;
	}

	public String getWaitDR() {
		return waitDR;
	}

	public void setWaitDR(String waitDR) {
		this.waitDR = waitDR;
	}

	public String getOtpDigit() {
		return otpDigit;
	}

	public void setOtpDigit(String otpDigit) {
		this.otpDigit = otpDigit;
	}

	public String getRefDigit() {
		return refDigit;
	}

	public void setRefDigit(String refDigit) {
		this.refDigit = refDigit;
	}
	
}
