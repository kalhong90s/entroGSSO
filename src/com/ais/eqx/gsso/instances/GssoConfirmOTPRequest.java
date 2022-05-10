package com.ais.eqx.gsso.instances;

import com.google.gson.annotations.SerializedName;

public class GssoConfirmOTPRequest {

	private SendConfirmOTPRequest	confirmOneTimePW					= null;
	
	@SerializedName("confirmOneTimePW_PassKey")
	private SendConfirmOTPRequest	confirmOneTimePasswordWithPasskey	= null;

	private String					messageType							= "JSON";

	public SendConfirmOTPRequest getConfirmOneTimePW() {
		return confirmOneTimePW;
	}

	public void setConfirmOneTimePW(SendConfirmOTPRequest confirmOneTimePW) {
		this.confirmOneTimePW = confirmOneTimePW;
	}

	public SendConfirmOTPRequest getConfirmOneTimePasswordWithPasskey() {
		return confirmOneTimePasswordWithPasskey;
	}

	public void setConfirmOneTimePasswordWithPasskey(SendConfirmOTPRequest confirmOneTimePasswordWithPasskey) {
		this.confirmOneTimePasswordWithPasskey = confirmOneTimePasswordWithPasskey;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
}
