package com.ais.eqx.gsso.enums;

import java.util.HashMap;

public enum SMPPResultCode {

    ESME_ROK		("000", "No Error") 
  , ESME_RINVMSGLEN	("001", "Message Length is invalid")
  , ESME_RINVCMDLEN	("002", "Command Length is invalid")
  , ESME_RINVCMDID	("003", "Invalid Command ID")
  , ESME_RINVBNDSTS	("004", "Incorrect BIND Status for given command")
  , ESME_RALYBND	("005", "ESME Already in Bound State")
  , ESME_RINVPRTFLG	("006", "Invalid Priority Flag")
  , ESME_RINVREGDLVFLG	("007", "Invalid Registered Delivery Flag")
  , ESME_RSYSERR	("008", "System Error")
  , ESME_RINVSRCADR	("00A", "Invalid Source Address")
  , ESME_RINVDSTADR	("00B", "Invalid Dest Addr")
  , ESME_RINVMSGID	("00C", "Message ID is invalid")
  , ESME_RBINDFAIL	("00D", "Bind Failed")
  , ESME_RINVPASWD	("00E", "Invalid Password")
  , ESME_RINVSYSID	("00F", "Invalid System ID")
  , ESME_RCANCELFAIL	("011", "Cancel SM Failed")
  , ESME_RREPLACEFAIL	("013", "Replace SM Failed")
  , ESME_RMSGQFUL	("014", "Message Queue Full")
  , ESME_RINVSERTYP	("015", "Invalid Service Type")
  , ESME_RINVNUMDESTS	("033", "Invalid number of destinations")
  , ESME_RINVDLNAME	("034", "Invalid Distribution List name")
  , ESME_RINVDESTFLAG	("040", "Destination flag is invalid (submit_multi)")
  , ESME_RINVSUBREP	("042", "Invalid ‘submit with replace’ request")
  , ESME_RINVESMCLASS	("043", "Invalid esm_class field data")
  , ESME_RCNTSUBDL	("044", "Cannot Submit to Distribution List")
  , ESME_RSUBMITFAIL	("045", "submit_sm or submit_multi failed")
  , ESME_RINVSRCTON	("048", "Invalid Source address TON")
  , ESME_RINVSRCNPI	("049", "Invalid Source address NPI")
  , ESME_RINVDSTTON	("050", "Invalid Destination address TON")
  , ESME_RINVDSTNPI	("051", "Invalid Destination address NPI")
  , ESME_RINVSYSTYP	("053", "Invalid system_type field")
  , ESME_RINVREPFLAG	("054", "Invalid replace_if_present flag")
  , ESME_RINVNUMMSGS	("055", "Invalid number of messages")
  , ESME_RTHROTTLED	("058", "Throttling error (ESME has exceeded allowed message limits)")
  , ESME_RINVSCHED	("061", "Invalid Scheduled Delivery Time")
  , ESME_RINVEXPIRY	("062", "Invalid message validity period (Expiry time)")
  , ESME_RINVDFTMSGID	("063", "Predefined Message Invalid or Not Found")
  , ESME_RX_T_APPN	("064", "ESME Receiver Temporary App Error Code")
  , ESME_RX_P_APPN	("065", "ESME Receiver Permanent App Error Code")
  , ESME_RX_R_APPN	("066", "ESME Receiver Reject Message Error Code")
  , ESME_RQUERYFAIL	("067", "query_sm request failed")
  , ESME_RINVOPTPARSTREAM("0C0", "Error in the optional part of the PDU Body")
  , ESME_ROPTPARNOTALLWD("0C1", "Optional Parameter not allowed")
  , ESME_RINVPARLEN	("0C2", "Invalid Parameter Length")
  , ESME_RMISSINGOPTPARAM("0C3", "Expected Optional Parameter missing")
  , ESME_RINVOPTPARAMVAL("0C4", "Invalid Optional Parameter Value")
  , ESME_RDELIVERYFAILURE("0FE", "Delivery Failure (used for data_sm_resp)")
  , ESME_RUNKNOWNERR	("0FF", "Unknown Error")
    
    ;

	private String	code;
	private String	errorMessage;

	private SMPPResultCode(final String code, final String errorMessage) {
		this.setCode(code);
		this.setErrorMessage(errorMessage);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private static final HashMap<String, SMPPResultCode>	lookup	= new HashMap<String, SMPPResultCode>();
	static {
		for (final SMPPResultCode e : SMPPResultCode.values()) {
			lookup.put(e.getCode(), e);
		}
	}

	public static String getErrorMessageFrom(final String code) {
		try {
			if (lookup.get(code.toUpperCase()).getErrorMessage() != null
					&& !lookup.get(code.toUpperCase()).getErrorMessage().isEmpty()) {
				return lookup.get(code.toUpperCase()).getErrorMessage();
			}
			else {
				return "UNKNOWN";
			}
		}
		catch (Exception e) {
			return "UNKNOWN";
		}
	}

}
