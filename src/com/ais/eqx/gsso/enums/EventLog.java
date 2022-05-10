package com.ais.eqx.gsso.enums;

public enum EventLog {

	SEND_OTP("Client.SendOTP")
	
	,WS_AUTHEN_OTP("Client.WSAuthenOTP")
	
	,WS_AUTHEN_OTP_ID("Client.WSAuthenOTPWithID")
	
	,WS_CREATE_OTP("Client.WSCreateOTP")
	
	,WS_GENERATE_OTP("Client.WSGenerateOTP")
	
	,WS_CONFIRM_OTP("Client.WSConfirmOTP")
	
	,WS_CONFIRM_OTP_ID("Client.WSConfirmOTPWithID")

	, CONFIRM_OTP("Client.ConfirmOTP")

	, CONFIRM_OTP_WITH_PASSKEY("Client.ConfirmOTPWithPasskey")

	, GENARATE_PASSKEY("Client.GenaratePasskey")
	
	, GENARATE_OTP("Client.GenerateOnetimePW")

	, AUTHEN_OTP("Client.AuthenOTP")

	, CLIENT_UNKNOWN("Client.Unknown")

	, NULL_UNKNOWN("Null.Unknown")

	, INQUIRY_VASSUBSCRIBER("USMP.InquiryVASSubscriber")

	, INQUIRY_SUBSCRIBER("USMP.InquirySubscriber")

	, PORT_CHECK("USMP.PortCheck")

	, QUERY_SERVICE_TEMPLATE("E01.QueryServiceTemplate")

	, SUBMIT_SM("SMPPGW.SubmitSM")

	, SMPPGW_ROAMING("SMPPGWROAMING.SubmitSM")

	, SMPPGW_DELIVERY_REPORT("SMPPGW.DeliveryReport")

	, SMPPGW_ROAMING_DELIVERY_REPORT("SMPPGWROAMING.DeliveryReport")

	, SMPPGW_UNKNOWN("SMPPGW.Unknown")

	, SEND_EMAIL("MAILSERVER.SendEmail")

	, REFUND("rPCEF.Refund")
	
	/** WSDL **/
	
	, SEND_WSDL("Client.SendWSDLQueryTemplate")
	
	, QUERY_WSDL_TEMPLATE("E01.WSDLQueryTemplate")

	;

	private String	eventLog;

	private EventLog(final String eventLog) {
		this.setEventLog(eventLog);
	}

	public String getEventLog() {
		return eventLog;
	}

	public void setEventLog(final String eventLog) {
		this.eventLog = eventLog;
	}

}
