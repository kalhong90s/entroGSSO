package com.ais.eqx.gsso.enums;

import java.util.HashMap;

public enum SubStates {

    IDLE_MANAGEMENT,

    IDLE_SEND_OTP_REQ,
    
    IDLE_WS_AUTH_OTP,
    
    IDLE_WS_AUTH_OTP_ID,
    
    IDLE_WS_CREATE_OTP,
    
    IDLE_WS_GENERATE_OTP,
    
    IDLE_WS_CONFIRM_OTP_ID,
    
    IDLE_WS_CONFIRM_OTP,
    
    IDLE_CONFIRMATION,

    IDLE_CONFIRMATION_W_PK,
    
    IDLE_WSDL_SEND_REQ,

    IDLE_AUTH_OTP,

    IDLE_GENERATE_PK,

    W_INQUIRY_VAS_SUB,

    W_INQUIRY_SUB,

    W_PORT_CHECK,

    W_SERVICE_TEMPLATE,

    W_SEND_SMS,

    W_SEND_EMAIL,

    W_DELIVERY_REPORT,
    
    W_WSDL,
    
    W_REFUND,
    
    UNKNOWN,

    END

    ;

	// Function Lookup
	private static final HashMap<String, SubStates>	lookup	= new HashMap<String, SubStates>();
	static {
		for (final SubStates e : SubStates.values()) {
			lookup.put(e.toString(), e);
		}
	}

}
