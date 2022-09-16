package com.ais.eqx.gsso.enums;

public enum ConfigName {

	SUMMARY_LOG_NAME("GSSO_Summary")

	, DETAIL_LOG_NAME("GSSO_Details")

	, DELIVERY_REPORT_LOG_NAME("GSSO_DeliveryReport")

	, REFUND_LOG_NAME("GSSO_Refund")

	, LOG_DETAIL_ENABLED("DETAIL_LOG-Enabled")

	, LOG_DETAIL_RAWDATA("DETAIL_LOG-RawData-Enabled")

	, LOG_DETAIL_DATA("DETAIL_LOG-Data-Enabled")

	, DEBUG_LOG_ENABLED("DEBUG_LOG_Enabled")

	, IDLE_SERVICE("IDLE-Service")

	, USMP_INQUIRYSUB_INTERFACE("USMP-InquirySub-Interface")
	
	, USMP_PORTCHECK_INTERFACE("USMP-PortCheck-Interface")

	, USMP_TIMEOUT("USMP-Timeout")

	, SMPPGW_INTERFACE("SMPPGW-Interface")

	, SMPPGW_ROAMING_INTERFACE("SMPPGW-Roaming-Interface")

	, SMPPGW_TIMEOUT("SMPPGW-Timeout")

	, MAILSERVER_INTERFACE("MAIL-Server-Interface")

	, MAILSERVER_TIMEOUT("MAIL-Server-Timeout")

	, E01_TIMEOUT("E01-Timeout")

	, PLUS_TRANSACTION_TIMEOUT_MINS("Plus-Transaction-Timeout-Mins")

	, DR_TIMEOUT("DR-Timeout")

	, OTP_LENGTH("OTP-Digit-Min-And-Max")

	, REF_LENGTH("REF-Digit-Min-And-Max")

	, LIFE_TIMEOUT_MINS("Password-Life-Timeout-Mins")

	, APPLICATION_NODENAME("Application-Node-Name")

	, EMAIL_URINAME("Email-URI-Name")

	, ACTIVE_STATE("Active-State")

	, DOMESTIC_COUNTRY_CODE_LIST("DomesticCountryCodeList")

	, MSISDN_DIGIT_LENGTH("MSISDN_Digit_Length")

	, SMS_RETRIES("Sms-Retries")

	, EMAIL_RETRIES("Email-Retries")

	, POSTPAID_COS_LISTS("PostpaidCosLists")
	
	, DUMMYNUMBER_LISTS ("DummyNumberLists")

	, MAX_TRANSACTION("Max-Transaction")

	, WAIT_DR("Wait-DR-Def")

	, MOBILEFORMAT("Mobile-Format")

	, SMPP_MESSAGING_MODE("SMPP-MessagingMode")

	, SMPP_MESSAGE_TYPE("SMPP-MessageType")

	, SMPP_GSM_NETWORK_SPECIFIC_FEATURES("SMPP-GSMNetworkSpecificFeatures")

	, SMPP_PROTOCOL_ID("SMPP-ProtocolId")

	, SMPP_PRIORITY_FLAG("SMPP-PriorityFlag")

	, SMPP_SCHEDULE_DELIVERY_TIME("SMPP-ScheduleDeliveryTime")

	, SMPP_VALIDITY_PERIOD("SMPP-ValidityPeriod")

	, SMPP_SME_ORIGINATED_ACK("SMPP-SMEOriginatedAck")

	, SMPP_INTERMEDIATE_NOTIFICATION("SMPP-IntermediateNotification")

	, SMPP_REPLACE_IF_PRESENT_FLAG("SMPP-ReplaceIfPresentFlag")

	, SMPP_SM_DEFAULT_MSG_ID("SMPP-SmDefaultMsgId")

	, USMP_NODE_NAME("USMP-UserName")

	, USMP_ORDER_DESC("USMP-OrderDesc")

	, E01_OBJECT_TYPE("E01-ObjectType")
	
	, E01_WSDL("E01-WSDL")
	
	, INQUIRY_VAS_SUBSCRIBER_URI_OVERRIDE("InquiryVASSubscriber-Uri-Override")
	
	, INQUIRY_VAS_SUBSCRIBER_HEADER_OVERRIDE("InquiryVASSubscriber-Header-Override")
	
    , INQUIRY_VAS_SUBSCRIBER_HEADER_OVERRIDE_NAME("InquiryVASSubscriber-Header-Override-Name")

	, PORTCHECK_SUBSCRIBER_URI_OVERRIDE("PortCheck-Uri-Override")
	
	, PORTCHECK_SUBSCRIBER_HEADER_OVERRIDE("PortCheck-Header-Override")
	
	, PORTCHECK_SUBSCRIBER_HEADER_OVERRIDE_NAME("PortCheck-Header-Override-Name")
	
	, WSDL_IP_ADDRESS("WSDL-IP-Address")
	
	, WSDL_PORT("WSDL-Port")
	
	/*
	 ******************************************** V2.0 ******************************************************  
	 */
    , MODE("Mode")
    
	, INQUIRYVASSUBSCRIBER("InquiryVasSubscriber")
	
	, INQUIRY_SUBSCRIBER_URI_OVERRIDE("InquirySubscriber-Uri-Override")
	
	, INQUIRY_SUBSCRIBER_HEADER_OVERRIDE("InquirySubscriber-Header-Override")
	
    , INQUIRY_SUBSCRIBER_HEADER_OVERRIDE_NAME("InquirySubscriber-Header-Override-Name")
    
    , INQUIRYSUBSCRIBER_BODY_SUB("InquirySubscriber-Body-Sub")
    
    , INQUIRYVASSUBSCRIBER_BODY_SUB("InquiryVASSubscriber-Body-Sub")
    
    , REFUND_RETRY("Refund-Retry")

	, REFUND_TIMEOUT("Refund-Timeout")
    
    , E01_SERVICEKEY_LIMIT_DIGIT("E01-ServiceKey-limit-digit")

	, RPCEF_INTERFACE("rPCEF-Interface")

	, COMMANDS_TO_REFUND("Commands-To-Refund")

	, RPCEF_REFUND_URL("rPCEF-Refund-URL")

	, MAXIMUM_HACK_TIME("Maximum-Hack-Time")

	, USMP_BY_PASS_CONFIG_SERVICE_LIST("USMP-By-Pass-Config-Service-List")

	, LONG_SMPP_GSMNETWORKSPECIFICFEATURES("Long-SMPP-GSMNetworkSpecificFeatures")

	, MAX_SMS_BODY("Max-Sms-Body-length")

	, DUMMY_EMAILLISTS_BY_SERVICE("Dummy-Email-Config-Service-Lists")

	, DUMMY_MSISDNLISTS_FOR_CONFIRM_OTP("Dummy-Msisdn-Lists-For-Confirm-Otp")



	;

	private String	name;

	private ConfigName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
