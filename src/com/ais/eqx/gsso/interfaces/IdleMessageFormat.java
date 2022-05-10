package com.ais.eqx.gsso.interfaces;

public interface IdleMessageFormat {

	public static final String	URL_SEND_OTP_REQ_PATTERN		= "/api/v1/gsso/sendOneTimePW(\\.)json(|\\?)(.*)";
	public static final String	URL_CONFIRMATION_PATTERN		= "/api/v1/gsso/confirmOneTimePassword(\\.)json(|\\?)(.*)";
	public static final String	URL_CONFIRMATION_W_PK_PATTERN	= "/api/v1/gsso/confirmOneTimePasswordWithPasskey(\\.)json(|\\?)(.*)";
	public static final String	URL_GENERATE_PK_PATTERN			= "/api/v1/gsso/generatePasskey(\\.)json(|\\?)(.*)";
	public static final String	URL_AUTH_OTP_PATTERN			= "/api/v1/gsso/authenOnetimePassword(\\.)json(|\\?)(.*)";
	public static final String	URL_WSDL_SEND_REQ_PATTERN		= "/GSSO_WS/GSSOWeb(\\?)wsdl";
	
	public static final String	URL_SSO_INTERFACE				= "ws.sso.gsso";
	public static final String	URL_WS_INTERFACE				= "ws.gsso";
	
	public static final String	SOAP_SEND_OTP_REQ				= "sendOneTimePW";
	public static final String	SOAP_CONFIRM_OTP_W_PK			= "confirmOneTimePW_PassKey";
	public static final String	SOAP_CONFIRM_OTP				= "confirmOneTimePW";
	public static final String	SOAP_GENERATE_PK				= "generatePasskey";
	
	public static final String	SOAP_WS_AUTH_OTP_REQ			= "authenOneTimePW";
	public static final String	SOAP_WS_AUTH_OTP_ID_REQ			= "authenOneTimePWwithID";
	public static final String	SOAP_WS_CREATE_OTP_REQ			= "createOneTimePW";
	public static final String	SOAP_WS_GENERATE_OTP_REQ		= "generateOneTimePW";
	public static final String	SOAP_WS_CONFIRM_OTP_REQ			= "confirmOneTimePW";
	public static final String	SOAP_WS_CONFIRM_OTP_ID_REQ		= "confirmOneTimePWwithID";

}
