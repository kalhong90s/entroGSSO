package com.ais.eqx.gsso.instances;

public class GssoAuthOTPRequest {

	private GssoAuthOTP	authenOnetimePassword;

	private String		messageType	= "JSON";

	public GssoAuthOTP getAuthenOnetimePassword() {
		return authenOnetimePassword;
	}

	public void setAuthenOnetimePassword(GssoAuthOTP authenOnetimePassword) {
		this.authenOnetimePassword = authenOnetimePassword;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

}
