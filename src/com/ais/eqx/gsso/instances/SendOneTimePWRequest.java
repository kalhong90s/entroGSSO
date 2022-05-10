package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SendOneTimePWRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class SendOneTimePWRequest {

	private String	msisdn			= null;
	private String	emailAddr		= null;
	private String	service			= null;
	private String	serviceKey		= null;
	private String	accountType		= null;
	private String	waitDR			= null;
	private String	otpChannel		= null;

	private String	lifeTimeoutMins	= null;
	private String	otpDigit		= null;
	private String	refDigit		= null;
	private String	sessionId		= null;
	private String	refId			= null;
	private String	state			= null;
	private String	smsLanguage		= null;


	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getWaitDR() {
		return waitDR;
	}

	public void setWaitDR(String waitDR) {
		this.waitDR = waitDR;
	}

	public String getOtpChannel() {
		return otpChannel;
	}

	public void setOtpChannel(String otpChannel) {
		this.otpChannel = otpChannel;
	}

	public String getLifeTimeoutMins() {
		return lifeTimeoutMins;
	}

	public void setLifeTimeoutMins(String lifeTimeoutMins) {
		this.lifeTimeoutMins = lifeTimeoutMins;
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

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getSmsLanguage() {
		return smsLanguage;
	}

	public void setSmsLanguage(String smsLanguage) {
		this.smsLanguage = smsLanguage;
	}
}
