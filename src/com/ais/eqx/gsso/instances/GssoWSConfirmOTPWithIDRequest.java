package com.ais.eqx.gsso.instances;

public class GssoWSConfirmOTPWithIDRequest {

	private SendWSConfirmOTPWithIDRequest 	sendWSConfirmOTPWithIDReq	= null;

	private String					messageType		= "SOAP";


	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public SendWSConfirmOTPWithIDRequest getSendWSConfirmOTPWithIDReq() {
		return sendWSConfirmOTPWithIDReq;
	}

	public void setSendWSConfirmOTPWithIDReq(SendWSConfirmOTPWithIDRequest sendWSConfirmOTPWithIDReq) {
		this.sendWSConfirmOTPWithIDReq = sendWSConfirmOTPWithIDReq;
	}


	
}
