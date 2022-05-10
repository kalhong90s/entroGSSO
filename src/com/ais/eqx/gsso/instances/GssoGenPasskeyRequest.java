package com.ais.eqx.gsso.instances;

public class GssoGenPasskeyRequest {

	private GenPasskey	generatePasskey;

	private String		messageType	= "JSON";

	public GenPasskey getGeneratePasskey() {
		return generatePasskey;
	}

	public void setGeneratePasskey(GenPasskey generatePasskey) {
		this.generatePasskey = generatePasskey;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

}
