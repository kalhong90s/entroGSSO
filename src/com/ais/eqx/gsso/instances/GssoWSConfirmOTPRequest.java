package com.ais.eqx.gsso.instances;

public class GssoWSConfirmOTPRequest {

	private SendWSConfirmOTPRequest 	sendWSConfirmOTPReq	= null;

	private String					messageType		= "SOAP";


	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public SendWSConfirmOTPRequest getSendWSConfirmOTPReq() {
		return sendWSConfirmOTPReq;
	}

	public void setSendWSConfirmOTPReq(SendWSConfirmOTPRequest sendWSConfirmOTPReq) {
		this.sendWSConfirmOTPReq = sendWSConfirmOTPReq;
	}

	
}
