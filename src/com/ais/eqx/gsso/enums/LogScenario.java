package com.ais.eqx.gsso.enums;

public enum LogScenario {

	SEND_OTP("SEND OTP")
	
	,WS_AUTHEN_OTP("WS_AUTHEN_OTP")
	
	,WS_AUTHEN_OTP_ID("WS_AUTHEN_OTP_ID")
	
	,WS_CREATE_OTP("WS_CREATE_OTP")
	
	,WS_GENERATE_OTP("WS_GENERATE_OTP")
	
	,WS_CONFIRM_OTP("WS_CONFIRM_OTP")
	
	,WS_CONFIRM_OTP_ID("WS_CONFIRM_OTP_ID")

	, GENARATE_PASSKEY("GENERATE PASSKEY")
	
	, GENARATE_OTP("GENARATE ONE TIME PASSWARD")

	, CONFIRM_OTP("CONFIRM OTP")

	, CONFIRM_OTP_WITH_PASSKEY("CONFIRM OTP WITH PASSKEY")

	, AUTHEN_OTP("AUTHEN OTP")

	, UNKNOWN("UNKNOWN")
	
	
	/**WSDL**/
	, SEND_WSDL("SEND WSDL")
	
	;

	private String	logScenario;

	private LogScenario(final String logScenario) {
		this.setLogScenario(logScenario);
	}

	public String getLogScenario() {
		return logScenario;
	}

	public void setLogScenario(final String logScenario) {
		this.logScenario = logScenario;
	}

}
