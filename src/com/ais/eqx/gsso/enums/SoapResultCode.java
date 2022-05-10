package com.ais.eqx.gsso.enums;

import java.util.HashMap;

public enum SoapResultCode {

    SUCCESS("000", "STATUS_OK"),

    WRONG_MSISDN_FORMAT("001", "WRONG_MSISDN_FORMAT"),
    
    WRONG_EMAIL_ADDR_FORMAT("002","WRONG_EMAIL_ADDR_FORMAT"),

    HACK_TIME_MORE_THAN_3("003", "HACK_TIME_MORE_THAN_3"),

    SERVICE_VAL_EMPTY("004", "SERVICE_VAL_EMPTY"),

    SESSIONID_WRONG_FORMAT("005", "SESSIONID_WRONG_FORMAT"),

    PASSWORD_WRONG_FORMAT("006", "PASSWORD_WRONG_FORMAT"),

    CONFIRM_FAIL("007", "CONFIRM_FAIL"),

    PASSWORD_TIME_OUT("008", "PASSWORD_TIME_OUT"),

    NOT_AUTHEN_BEFORE("009", "NOT_AUTHEN_BEFORE"),

    UNKNOWN_MSISDN("020", "UNKNOWN_MSISDN"),

    STATE_NOT_USE_SERVICE("021", "STATE_NOT_USE_SERVICE"),

    COS_NOT_MATCH_TYPE("022", "COS_NOT_MATCH_TYPE"),

    UNKNOWN_ACCOUNT_TYPE_ERROR("023", "UNKNOWN_ACCOUNT_TYPE_ERROR"),

    SEND_SMS_FAIL("025", "SEND_SMS_FAIL"),

    GSSO_BUSY("027", "GSSO_BUSY"),
    
    ADD_TIMEOUT_MINS_EXCEED("028", "ADD_TIMEOUT_MINS_EXCEED"),
    
    MISSING_VALUE_OTPMOBILE_AND_EMAIL("029","MISSING_VALUE_OTPMOBILE_AND_EMAIL"),

    SEND_SMS_SUCCESS_EMAIL_FAIL("030", "SEND_SMS_SUCCESS_EMAIL_FAIL"),

    SEND_SMS_FAIL_EMAIL_SUCCESS("031", "SEND_SMS_FAIL_EMAIL_SUCCESS"),

    SEND_SMS_FAIL_EMAIL_FAIL("032", "SEND_SMS_FAIL_EMAIL_FAIL"),

    SEND_EMAIL_FAIL("033", "SEND_EMAIL_FAIL"),
    
    WRONG_DUMMY_NUMBER("035", "WRONG_DUMMY_NUMBER"),
    
    WRONG_DUMMY_ACCOUNT_TYPE("036", "WRONG_DUMMY_ACCOUNT_TYPE"),
    
    INVALID_OTPCHANNEL("037", "INVALID_OTPCHANNEL"),

    UNKNOWN_SERVICE("040", "UNKNOWN_SERVICE"),

    USMP_TIMEOUT("042", "USMP_TIMEOUT"),
    
    WS_WRONG_INPUT_PARAMETER("043","WRONG_INPUT_PARAMETER"),

    FAIL_UNKNOWN("999", "FAIL_UNKNOWN"),

    USMP_ERROR("999", "USMP_ERROR"),

    E01_TIMEOUT("999", "E01_TIMEOUT"),

    E01_ERROR("999", "E01_ERROR"),

    ;

	private String	code;
	private String	description;

	private SoapResultCode(final String code, final String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	private static final HashMap<String, SoapResultCode>	lookup	= new HashMap<String, SoapResultCode>();
	static {
		for (SoapResultCode e : SoapResultCode.values()) {
			lookup.put(e.getCode(), e);
		}
	}

	public static SoapResultCode getSoapResultCodeFrom(final String code) {
		return lookup.get(code);
	}

}
