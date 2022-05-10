package com.ais.eqx.gsso.interfaces;

public interface MessageResponsePrefix {

	public static final String	SEND_ONETIMEPW_RESPONSE				= "sendOneTimePWResponse";

	public static final String	CONFIRM_ONETIMEPW_RESPONSE			= "confirmOneTimePWResponse";

	public static final String	CONFIRM_ONETIMEPW_PASSKEY_RESPONSE	= "confirmOneTimePW_PassKeyResponse";

	public static final String	GENERATE_PASSKEY_RESPONSE			= "generatePasskeyResponse";

	public static final String	AUTHEN_ONETIMEPASSWORD_RESPONSE		= "authenOnetimePasswordResponse";
	
	public static final String	WS_AUTHEN_ONETIMEPASSWORD_RESPONSE		= "authenOneTimePWResponse";

	public static final String	WS_CREATE_ONETIMEPASSWORD_RESPONSE		= "createOneTimePWResponse";

	public static final String	WS_AUTHEN_ONETIMEPASSWORD_ID_RESPONSE		= "authenOneTimePWwithIDResponse";
	
	public static final String	WS_GENERATE_ONETIMEPASSWORD_RESPONSE		= "generateOneTimePWResponse";
	
	public static final String	WS_CONFIRM_ONETIMEPASSWORD_RESPONSE		= "confirmOneTimePWResponse";
	
	public static final String	WS_CONFIRM_ONETIMEPASSWORD_ID_RESPONSE		= "confirmOneTimePWwithIDResponse";
	
	public static final String	UNKNOWN_REQUEST		= "unknownRequest";

}
