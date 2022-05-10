package com.ais.eqx.gsso.instances;

public class GssoOTPRequest {

	private SendOneTimePWRequest	sendOneTimePW	= null;

	private String					messageType		= "JSON";
	private boolean 				isBypass 		= false;

	public SendOneTimePWRequest getSendOneTimePW() {
		return sendOneTimePW;
	}

	public void setSendOneTimePW(SendOneTimePWRequest sendOneTimePW) {
		this.sendOneTimePW = sendOneTimePW;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public boolean isBypass() {
		return isBypass;
	}

	public void setBypass(boolean bypass) {
		isBypass = bypass;
	}
}
