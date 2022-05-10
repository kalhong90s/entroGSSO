package com.ais.eqx.gsso.enums;

import java.util.HashMap;

import com.ais.eqx.gsso.interfaces.MessageResponsePrefix;

public enum IncomingMessageType {

	SEND_OTP_JSON("SEND_OTP_JSON", MessageResponsePrefix.SEND_ONETIMEPW_RESPONSE)
	
	, SEND_OTP_SOAP("SEND_OTP_SOAP", MessageResponsePrefix.SEND_ONETIMEPW_RESPONSE)

	, WS_AUTHEN_OTP_JSON("WS_AUTHEN_ONETIMEPASSWORD_JSON", MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_RESPONSE)
	
	, WS_AUTHEN_OTP_SOAP("WS_AUTHEN_ONETIMEPASSWORD_SOAP", MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_RESPONSE)
	
	, WS_CREATE_OTP_JSON("WS_CREATE_OTP_JSON", MessageResponsePrefix.WS_CREATE_ONETIMEPASSWORD_RESPONSE)
	
	, WS_CREATE_OTP_SOAP("WS_CREATE_OTP_SOAP", MessageResponsePrefix.WS_CREATE_ONETIMEPASSWORD_RESPONSE)
	
	, WS_AUTHEN_OTP_ID_JSON("WS_AUTHEN_ONETIMEPASSWORD_ID_JSON", MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_ID_RESPONSE)
	
	, WS_AUTHEN_OTP_ID_SOAP("WS_AUTHEN_ONETIMEPASSWORD_ID_SOAP", MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_ID_RESPONSE)

	, CONFIRM_ONETIMEPW_JSON("CONFIRM_ONETIMEPW_JSON", MessageResponsePrefix.CONFIRM_ONETIMEPW_RESPONSE)

	, CONFIRM_ONETIMEPW_SOAP("CONFIRM_ONETIMEPW_SOAP", MessageResponsePrefix.CONFIRM_ONETIMEPW_RESPONSE)

	, CONFIRM_ONETIMEPW_PASSKEY_JSON("CONFIRM_ONETIMEPW_PASSKEY_JSON", MessageResponsePrefix.CONFIRM_ONETIMEPW_PASSKEY_RESPONSE)

	, CONFIRM_ONETIMEPW_PASSKEY_SOAP("CONFIRM_ONETIMEPW_PASSKEY_SOAP", MessageResponsePrefix.CONFIRM_ONETIMEPW_PASSKEY_RESPONSE)

	, GENERATE_PASSKEY_JSON("GENERATE_PASSKEY_JSON", MessageResponsePrefix.GENERATE_PASSKEY_RESPONSE)

	, GENERATE_PASSKEY_SOAP("GENERATE_PASSKEY_SOAP", MessageResponsePrefix.GENERATE_PASSKEY_RESPONSE)
	
	, WS_GENERATE_ONETIMEPW_SOAP("WS_GENERATE_ONETIMEPASSWORD_SOAP", MessageResponsePrefix.WS_GENERATE_ONETIMEPASSWORD_RESPONSE)

	, AUTHEN_ONETIMEPASSWORD_JSON("AUTHEN_ONETIMEPASSWORD_JSON", MessageResponsePrefix.AUTHEN_ONETIMEPASSWORD_RESPONSE)
	
	, AUTHEN_ONETIMEPASSWORD_SOAP("AUTHEN_ONETIMEPASSWORD_SOAP", MessageResponsePrefix.AUTHEN_ONETIMEPASSWORD_RESPONSE)
	
	, WS_CONFIRM_ONETIMEPW_SOAP("WS_CONFIRM_ONETIMEPW_SOAP", MessageResponsePrefix.WS_CONFIRM_ONETIMEPASSWORD_RESPONSE)

	, WS_CONFIRM_ONETIMEPW_ID_SOAP("WS_CONFIRM_ONETIMEPW_ID_SOAP", MessageResponsePrefix.WS_CONFIRM_ONETIMEPASSWORD_ID_RESPONSE)
	
	;

	private String	messageType;
	private String	responseFormat;

	private IncomingMessageType(final String messageType, final String responseFormat) {
		this.messageType = messageType;
		this.responseFormat = responseFormat;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(final String messageType) {
		this.messageType = messageType;
	}

	public String getResponseFormat() {
		return responseFormat;
	}

	public void setResponseFormat(final String responseFormat) {
		this.responseFormat = responseFormat;
	}

	// Method Lookup
	private static final HashMap<String, IncomingMessageType>	lookup	= new HashMap<String, IncomingMessageType>();
	static {
		for (final IncomingMessageType e : IncomingMessageType.values()) {
			lookup.put(e.getMessageType(), e);
		}
	}

	public static String getResponseFormatFrom(final String messageType) {
		return lookup.get(messageType).getResponseFormat();
	}

}
