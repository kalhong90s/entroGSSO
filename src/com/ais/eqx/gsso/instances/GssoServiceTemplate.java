package com.ais.eqx.gsso.instances;

import com.google.gson.annotations.SerializedName;

public class GssoServiceTemplate {

	private String	oper				= null;
	private String	allowRoaming		= null;
	private String	allowSmsRoaming		= null;

	private String	smsSender			= null;
	private String	smsBodyThai			= null;
	private String	smsBodyEng			= null;
	private String	smsBody				= null;

	private String	emailFrom			= null;
	private String	emailSubject		= null;
	private String	emailBody			= null;

	private String	waitDR				= null;
	private String	otpDigit			= null;
	private String	refDigit			= null;

	private String	lifeTimeoutMins		= null;
	private String	seedkey				= null;
	private String	refundFlag			= null;

	@SerializedName("smscDeliveryReceipt")
	private String	smscDeliveryReceipt	= null;

	public String getOper() {
		return oper;
	}

	public void setOper(String oper) {
		this.oper = oper;
	}

	public String getAllowRoaming() {
		return allowRoaming;
	}

	public void setAllowRoaming(String allowRoaming) {
		this.allowRoaming = allowRoaming;
	}

	public String getSmsSender() {
		return smsSender;
	}

	public void setSmsSender(String smsSender) {
		this.smsSender = smsSender;
	}

	public String getSmsBodyThai() {
		return smsBodyThai;
	}

	public void setSmsBodyThai(String smsBodyThai) {
		this.smsBodyThai = smsBodyThai;
	}

	public String getSmsBodyEng() {
		return smsBodyEng;
	}

	public void setSmsBodyEng(String smsBodyEng) {
		this.smsBodyEng = smsBodyEng;
	}

	public String getSmsBody() {
		return smsBody;
	}

	public void setSmsBody(String smsBody) {
		this.smsBody = smsBody;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	public String getLifeTimeoutMins() {
		return lifeTimeoutMins;
	}

	public void setLifeTimeoutMins(String lifeTimeoutMins) {
		this.lifeTimeoutMins = lifeTimeoutMins;
	}

	public String getAllowSmsRoaming() {
		return allowSmsRoaming;
	}

	public void setAllowSmsRoaming(String allowSmsRoaming) {
		this.allowSmsRoaming = allowSmsRoaming;
	}

	public String getSeedkey() {
		return seedkey;
	}

	public void setSeedkey(String seedkey) {
		this.seedkey = seedkey;
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

	public String getSmscDeliveryReceipt() {
		return smscDeliveryReceipt;
	}

	public void setSmscDeliveryReceipt(String smscDeliveryReceipt) {
		this.smscDeliveryReceipt = smscDeliveryReceipt;
	}

	public String getRefundFlag() {
		return refundFlag;
	}

	public void setRefundFlag(String refundFlag) {
		this.refundFlag = refundFlag;
	}

}
