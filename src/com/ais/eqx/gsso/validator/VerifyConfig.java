package com.ais.eqx.gsso.validator;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.exception.ConfigException;
import com.ais.eqx.gsso.interfaces.Mode;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoDataManagement;

import ec02.af.abstracts.AbstractAF;

public class VerifyConfig {

	public static void verifyConfig(String strConfig, AbstractAF abstractAF) throws ConfigException {

		/* INTIAL CONFIG */
		ConfigureTool.initConfigureTool(abstractAF.getEquinoxUtils().getHmWarmConfig());

		/* Application-Node-Name */
		try {
			if (ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME) == null) {
				throw new ConfigException(ConfigName.APPLICATION_NODENAME.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.APPLICATION_NODENAME.getName() + " : Not Found");
		}

		/* Email-URI-Name */
		try {
			if (ConfigureTool.getConfigure(ConfigName.EMAIL_URINAME) == null) {
				throw new ConfigException(ConfigName.EMAIL_URINAME.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.EMAIL_URINAME.getName() + " : Not Found");
		}

		/* IDLE-Service */
		try {
			if (ConfigureTool.getConfigureArray(ConfigName.IDLE_SERVICE) == null) {
				throw new ConfigException(ConfigName.IDLE_SERVICE.getName() + " : Not Found");
			}
			if (ConfigureTool.getConfigureArray(ConfigName.IDLE_SERVICE).size() == 0) {
				throw new ConfigException(ConfigName.IDLE_SERVICE.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.IDLE_SERVICE.getName() + " : Not Found");
		}

		/* USMP-InquirySub-Interface */
		try {
			if (ConfigureTool.getConfigure(ConfigName.USMP_INQUIRYSUB_INTERFACE) == null) {
				throw new ConfigException(ConfigName.USMP_INQUIRYSUB_INTERFACE.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.USMP_INQUIRYSUB_INTERFACE.getName() + " : Not Found");
		}
		
		/* USMP-PortCheck-Interface */
		try {
			if (ConfigureTool.getConfigure(ConfigName.USMP_PORTCHECK_INTERFACE) == null) {
				throw new ConfigException(ConfigName.USMP_PORTCHECK_INTERFACE.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.USMP_PORTCHECK_INTERFACE.getName() + " : Not Found");
		}

		/* SMPPGW-Interface */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPPGW_INTERFACE) == null) {
				throw new ConfigException(ConfigName.SMPPGW_INTERFACE.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPPGW_INTERFACE.getName() + " : Not Found");
		}

		/* SMPPGW-Roaming-Interface */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPPGW_ROAMING_INTERFACE) == null) {
				throw new ConfigException(ConfigName.SMPPGW_ROAMING_INTERFACE.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPPGW_ROAMING_INTERFACE.getName() + " : Not Found");
		}

		/* MAIL-Server-Interface */
		try {
			if (ConfigureTool.getConfigure(ConfigName.MAILSERVER_INTERFACE) == null) {
				throw new ConfigException(ConfigName.MAILSERVER_INTERFACE.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MAILSERVER_INTERFACE.getName() + " : Not Found");
		}

		/* Max-Transaction */
		try {
			if (ConfigureTool.getConfigure(ConfigName.MAX_TRANSACTION) == null) {
				throw new ConfigException(ConfigName.MAX_TRANSACTION.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MAX_TRANSACTION.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MAX_TRANSACTION));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MAX_TRANSACTION.getName() + " : Values Is Not Digital");
		}

		/* USMP-Timeout */
		try {
			if (ConfigureTool.getConfigure(ConfigName.USMP_TIMEOUT) == null) {
				throw new ConfigException(ConfigName.USMP_TIMEOUT.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.USMP_TIMEOUT.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.USMP_TIMEOUT));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.USMP_TIMEOUT.getName() + " : Values Is Not Digital");
		}

		/* SMPPGW-Timeout */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPPGW_TIMEOUT) == null) {
				throw new ConfigException(ConfigName.SMPPGW_TIMEOUT.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPPGW_TIMEOUT.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.SMPPGW_TIMEOUT));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPPGW_TIMEOUT.getName() + " : Values Is Not Digital");
		}

		/* MAIL-SERVER-Timeout */
		try {
			if (ConfigureTool.getConfigure(ConfigName.MAILSERVER_TIMEOUT) == null) {
				throw new ConfigException(ConfigName.MAILSERVER_TIMEOUT.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MAILSERVER_TIMEOUT.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MAILSERVER_TIMEOUT));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MAILSERVER_TIMEOUT.getName() + " : Values Is Not Digital");
		}

		/* E01-Timeout */
		try {
			if (ConfigureTool.getConfigure(ConfigName.E01_TIMEOUT) == null) {
				throw new ConfigException(ConfigName.E01_TIMEOUT.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.E01_TIMEOUT.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.E01_TIMEOUT));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.E01_TIMEOUT.getName() + " : Values Is Not Digital");
		}

		/* DR-Timeout */
		try {
			if (ConfigureTool.getConfigure(ConfigName.DR_TIMEOUT) == null) {
				throw new ConfigException(ConfigName.DR_TIMEOUT.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.DR_TIMEOUT.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.DR_TIMEOUT));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.DR_TIMEOUT.getName() + " : Values Is Not Digital");
		}

		/* Password-Life-Timeout-Mins */
		try {
			if (ConfigureTool.getConfigure(ConfigName.LIFE_TIMEOUT_MINS) == null) {
				throw new ConfigException(ConfigName.LIFE_TIMEOUT_MINS.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.LIFE_TIMEOUT_MINS.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.LIFE_TIMEOUT_MINS));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.LIFE_TIMEOUT_MINS.getName() + " : Values Is Not Digital");
		}

		/* Plus-Transaction-Timeout-Mins */
		try {
			if (ConfigureTool.getConfigure(ConfigName.PLUS_TRANSACTION_TIMEOUT_MINS) == null) {
				throw new ConfigException(ConfigName.PLUS_TRANSACTION_TIMEOUT_MINS.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.PLUS_TRANSACTION_TIMEOUT_MINS.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.PLUS_TRANSACTION_TIMEOUT_MINS));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.PLUS_TRANSACTION_TIMEOUT_MINS.getName() + " : Values Is Not Digital");
		}

		/* OTP-Digit-Min-And-Max */
		try {
			if (ConfigureTool.getConfigure(ConfigName.OTP_LENGTH) == null) {
				throw new ConfigException(ConfigName.OTP_LENGTH.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.OTP_LENGTH.getName() + " : Not Found");
		}
		try {
			ConfigureTool.getConfigure(ConfigName.OTP_LENGTH).split(",");
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.OTP_LENGTH.getName() + " : Values Is Invalid");
		}
		int minOtp = 0;
		int maxOtp = 0;
		try {
			String[] otpDigitLength = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.OTP_LENGTH));
			minOtp = Integer.parseInt(otpDigitLength[0]);
			maxOtp = Integer.parseInt(otpDigitLength[1]);
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.OTP_LENGTH.getName() + " : Values Is Not Digital");
		}
		if (minOtp < 4) {
			throw new ConfigException(ConfigName.OTP_LENGTH.getName() + " : Less Than Limit");
		}
		if (maxOtp > 12) {
			throw new ConfigException(ConfigName.OTP_LENGTH.getName() + " : More Than Limit");
		}

		/* REF-Digit-Min-And-Max */
		try {
			if (ConfigureTool.getConfigure(ConfigName.REF_LENGTH) == null) {
				throw new ConfigException(ConfigName.REF_LENGTH.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.REF_LENGTH.getName() + " : Not Found");
		}
		try {
			ConfigureTool.getConfigure(ConfigName.REF_LENGTH).split(",");
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.REF_LENGTH.getName() + " : Values Is Invalid");
		}
		int minRef = 0;
		int maxRef = 0;
		try {
			String[] refDigitLength = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.REF_LENGTH));
			minRef = Integer.parseInt(refDigitLength[0]);
			maxRef = Integer.parseInt(refDigitLength[1]);
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.REF_LENGTH.getName() + " : Values Is Not Digital");
		}
		if (minRef < 4) {
			throw new ConfigException(ConfigName.REF_LENGTH.getName() + " : Less Than Limit");
		}
		if (maxRef > 12) {
			throw new ConfigException(ConfigName.REF_LENGTH.getName() + " : More Than Limit");
		}

		/* Active-State */
		try {
			if (ConfigureTool.getConfigure(ConfigName.ACTIVE_STATE) == null) {
				throw new ConfigException(ConfigName.ACTIVE_STATE.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.ACTIVE_STATE.getName() + " : Not Found");
		}
		try {
			ConfigureTool.getConfigure(ConfigName.ACTIVE_STATE).split(",");
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.ACTIVE_STATE.getName() + " : Values Is Invalid");
		}
		try {
			String[] refDigitLength = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.ACTIVE_STATE));
			for (String value : refDigitLength) {
				Integer.parseInt(value);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.ACTIVE_STATE.getName() + " : Values Is Not Digital");
		}

		/* CountryCodeList */
		try {
			if (ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST) == null) {
				throw new ConfigException(ConfigName.DOMESTIC_COUNTRY_CODE_LIST.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.DOMESTIC_COUNTRY_CODE_LIST.getName() + " : Not Found");
		}
		try {
			ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST).split(",");
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.DOMESTIC_COUNTRY_CODE_LIST.getName() + " : Values Is Invalid");
		}

		/* MSISDN_Digit_Length */
		try {
			if (ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH) == null) {
				throw new ConfigException(ConfigName.MSISDN_DIGIT_LENGTH.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MSISDN_DIGIT_LENGTH.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MSISDN_DIGIT_LENGTH.getName() + " : Values Is Not Digital");
		}

		/* Sms-Retries */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMS_RETRIES) == null) {
				throw new ConfigException(ConfigName.SMS_RETRIES.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMS_RETRIES.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.SMS_RETRIES));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMS_RETRIES.getName() + " : Values Is Not Digital");
		}

		/* Email-Retries */
		try {
			if (ConfigureTool.getConfigure(ConfigName.EMAIL_RETRIES) == null) {
				throw new ConfigException(ConfigName.EMAIL_RETRIES.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.EMAIL_RETRIES.getName() + " : Not Found");
		}
		try {
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.EMAIL_RETRIES));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.EMAIL_RETRIES.getName() + " : Values Is Not Digital");
		}

		/* PostpaidCosLists */
		try {
			if (ConfigureTool.getConfigure(ConfigName.POSTPAID_COS_LISTS) == null) {
				throw new ConfigException(ConfigName.POSTPAID_COS_LISTS.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.POSTPAID_COS_LISTS.getName() + " : Not Found");
		}
		try {
			ConfigureTool.getConfigure(ConfigName.POSTPAID_COS_LISTS).split(",");
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.POSTPAID_COS_LISTS.getName() + " : Values Is Invalid");
		}

		/* Mobile-Format */
		try {
			if (ConfigureTool.getConfigure(ConfigName.MOBILEFORMAT) == null) {
				throw new ConfigException(ConfigName.MOBILEFORMAT.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MOBILEFORMAT.getName() + " : Not Found");
		}
		try {
			ConfigureTool.getConfigure(ConfigName.MOBILEFORMAT).split(",");
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MOBILEFORMAT.getName() + " : Values Is Invalid");
		}
		try {
			String[] mobileFormatSplit = ConfigureTool.getConfigure(ConfigName.MOBILEFORMAT).split(",");
			for (String mobileValue : mobileFormatSplit) {

				String[] splitPrefixAndLength = mobileValue.split("\\|");
				String mobileLength = splitPrefixAndLength[1];
				Integer.parseInt(mobileLength);

			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MOBILEFORMAT.getName() + " Length: Length Is Not Digital");
		}

		/* E01-ObjectType */
		try {
			if (ConfigureTool.getConfigure(ConfigName.E01_OBJECT_TYPE) == null) {
				throw new ConfigException(ConfigName.E01_OBJECT_TYPE.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.E01_OBJECT_TYPE.getName() + " : Not Found");
		}

		/* SMPP-MessagingMode */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPP_MESSAGING_MODE) == null) {
				throw new ConfigException(ConfigName.SMPP_MESSAGING_MODE.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPP_MESSAGING_MODE.getName() + " : Not Found");
		}

		/* SMPP-MessageType */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPP_MESSAGE_TYPE) == null) {
				throw new ConfigException(ConfigName.SMPP_MESSAGE_TYPE.getName() + "SMPP-MessageType : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPP_MESSAGE_TYPE.getName() + " : Not Found");
		}

		/* SMPP-GSMNetworkSpecFeature */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPP_GSM_NETWORK_SPECIFIC_FEATURES) == null) {
				throw new ConfigException(ConfigName.SMPP_GSM_NETWORK_SPECIFIC_FEATURES.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPP_GSM_NETWORK_SPECIFIC_FEATURES.getName() + " : Not Found");
		}

		/* SMPP-ProtocolId */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPP_PROTOCOL_ID) == null) {
				throw new ConfigException(ConfigName.SMPP_PROTOCOL_ID.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPP_PROTOCOL_ID.getName() + " : Not Found");
		}

		/* SMPP-PriorityFlag */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPP_PRIORITY_FLAG) == null) {
				throw new ConfigException(ConfigName.SMPP_PRIORITY_FLAG.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPP_PRIORITY_FLAG.getName() + " : Not Found");
		}

		/* SMPP-ScheduleDeliveryTime */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPP_SCHEDULE_DELIVERY_TIME) == null) {
				throw new ConfigException(ConfigName.SMPP_SCHEDULE_DELIVERY_TIME.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPP_SCHEDULE_DELIVERY_TIME.getName() + " : Not Found");
		}

		/* SMPP-SMEOriginatedAck */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPP_SME_ORIGINATED_ACK) == null) {
				throw new ConfigException(ConfigName.SMPP_SME_ORIGINATED_ACK.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPP_SME_ORIGINATED_ACK.getName() + " : Not Found");
		}

		/* SMPP-IntermediateNotification */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPP_INTERMEDIATE_NOTIFICATION) == null) {
				throw new ConfigException(ConfigName.SMPP_INTERMEDIATE_NOTIFICATION.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPP_INTERMEDIATE_NOTIFICATION.getName() + " : Not Found");
		}

		/* SMPP-ReplaceIfPresentFlag */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPP_REPLACE_IF_PRESENT_FLAG) == null) {
				throw new ConfigException(ConfigName.SMPP_REPLACE_IF_PRESENT_FLAG.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPP_REPLACE_IF_PRESENT_FLAG.getName() + " : Not Found");
		}

		/* SMPP-SmDefaultMsgId */
		try {
			if (ConfigureTool.getConfigure(ConfigName.SMPP_SM_DEFAULT_MSG_ID) == null) {
				throw new ConfigException(ConfigName.SMPP_SM_DEFAULT_MSG_ID.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.SMPP_SM_DEFAULT_MSG_ID.getName() + " : Not Found");
		}

		/* USMP-UserName */
		try {
			if (ConfigureTool.getConfigure(ConfigName.USMP_NODE_NAME) == null) {
				throw new ConfigException(ConfigName.USMP_NODE_NAME.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.USMP_NODE_NAME.getName() + " : Not Found");
		}

		/* USMP-OrderDesc */
		try {
			if (ConfigureTool.getConfigure(ConfigName.USMP_ORDER_DESC) == null) {
				throw new ConfigException(ConfigName.USMP_ORDER_DESC.getName() + " : Not Found");
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.USMP_ORDER_DESC.getName() + " : Not Found");
		}
		
		/**
		 * V2.0
		 */
		String valueWrong = "";

		/* InquiryVasSubscriber */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.INQUIRYVASSUBSCRIBER) == null) {
				throw new ConfigException(ConfigName.INQUIRYVASSUBSCRIBER.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.INQUIRYVASSUBSCRIBER.getName() + valueWrong);
		}
		
		/* E01-ServiceKey-limit-digit */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.E01_SERVICEKEY_LIMIT_DIGIT) == null) {
				throw new ConfigException(ConfigName.E01_SERVICEKEY_LIMIT_DIGIT.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.E01_SERVICEKEY_LIMIT_DIGIT.getName() + valueWrong);
		}
		
		try {
			valueWrong = " : Values Is Not Digital";
			int E01_SERVICEKEY_LIMIT_DIGIT = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.E01_SERVICEKEY_LIMIT_DIGIT));
			
			valueWrong = " : Values Is Digital but must be greater than zero";
			if(E01_SERVICEKEY_LIMIT_DIGIT == 0){
				throw new ConfigException(ConfigName.E01_SERVICEKEY_LIMIT_DIGIT.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.E01_SERVICEKEY_LIMIT_DIGIT.getName() + valueWrong);
		}

		/* Mode */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.MODE) == null) {
				throw new ConfigException(ConfigName.MODE.getName() + valueWrong);
			}

			valueWrong = " : Values Is Invalid";
			if(!ConfigureTool.getConfigure(ConfigName.MODE).equalsIgnoreCase(Mode.TEST)){
				if(!ConfigureTool.getConfigure(ConfigName.MODE).equalsIgnoreCase(Mode.PRODUCTION)){
					throw new ConfigException(ConfigName.MODE.getName() + valueWrong);
				}
			}
			
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MODE.getName() + valueWrong);
		}

		/* InquirySubscriber-Uri-Override */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.INQUIRY_SUBSCRIBER_URI_OVERRIDE) == null) {
				throw new ConfigException(ConfigName.INQUIRY_SUBSCRIBER_URI_OVERRIDE.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.INQUIRY_SUBSCRIBER_URI_OVERRIDE.getName() + valueWrong);
		}

		/* InquirySubscriber-Header-Override */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.INQUIRY_SUBSCRIBER_HEADER_OVERRIDE) == null) {
				throw new ConfigException(ConfigName.INQUIRY_SUBSCRIBER_HEADER_OVERRIDE.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.INQUIRY_SUBSCRIBER_HEADER_OVERRIDE.getName() + valueWrong);
		}

		/* InquirySubscriber-Header-Override-Name */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.INQUIRY_SUBSCRIBER_HEADER_OVERRIDE_NAME) == null) {
				throw new ConfigException(ConfigName.INQUIRY_SUBSCRIBER_HEADER_OVERRIDE_NAME.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.INQUIRY_SUBSCRIBER_HEADER_OVERRIDE_NAME.getName() + valueWrong);
		}

		/* InquirySubscriber-Body-Sub */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.INQUIRYSUBSCRIBER_BODY_SUB) == null) {
				throw new ConfigException(ConfigName.INQUIRYSUBSCRIBER_BODY_SUB.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.INQUIRYSUBSCRIBER_BODY_SUB.getName() + valueWrong);
		}

		/* InquiryVASSubscriber-Body-Sub */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.INQUIRYVASSUBSCRIBER_BODY_SUB) == null) {
				throw new ConfigException(ConfigName.INQUIRYVASSUBSCRIBER_BODY_SUB.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.INQUIRYVASSUBSCRIBER_BODY_SUB.getName() + valueWrong);
		}

		/* Refund-Retry */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.REFUND_RETRY) == null) {
				throw new ConfigException(ConfigName.REFUND_RETRY.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.REFUND_RETRY.getName() + valueWrong);
		}

		/* Refund-Timeout */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.REFUND_TIMEOUT) == null) {
				throw new ConfigException(ConfigName.REFUND_TIMEOUT.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.REFUND_TIMEOUT.getName() + valueWrong);
		}
		
		try {
			valueWrong = " : Values Is Not Digital";
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.REFUND_TIMEOUT));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.REFUND_TIMEOUT.getName() + valueWrong);
		}

		/* rPCEF-Interface */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.RPCEF_INTERFACE) == null) {
				throw new ConfigException(ConfigName.RPCEF_INTERFACE.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.RPCEF_INTERFACE.getName() + valueWrong);
		}

		/* Commands-To-Refund */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.COMMANDS_TO_REFUND) == null) {
				throw new ConfigException(ConfigName.COMMANDS_TO_REFUND.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.COMMANDS_TO_REFUND.getName() + valueWrong);
		}
		
//		try {
//			ConfigureTool.getConfigure(ConfigName.COMMANDS_TO_REFUND).split("|");
//		}
//		catch (Exception e) {
//			throw new ConfigException(ConfigName.COMMANDS_TO_REFUND.getName() + " : Values Is Invalid");
//		}

		/* rPCEF-Refund-URL */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.RPCEF_REFUND_URL) == null) {
				throw new ConfigException(ConfigName.RPCEF_REFUND_URL.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.RPCEF_REFUND_URL.getName() + valueWrong);
		}

		/* Maximum-Hack-Time */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.MAXIMUM_HACK_TIME) == null) {
				throw new ConfigException(ConfigName.MAXIMUM_HACK_TIME.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MAXIMUM_HACK_TIME.getName() + valueWrong);
		}
		
		try {
			valueWrong = " : Values Is Not Digital";
			Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MAXIMUM_HACK_TIME));
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.MAXIMUM_HACK_TIME.getName() + valueWrong);
		}

		/* USMP-By-Pass-Config-Service-List */
		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.USMP_BY_PASS_CONFIG_SERVICE_LIST) == null) {
				throw new ConfigException(ConfigName.USMP_BY_PASS_CONFIG_SERVICE_LIST.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.USMP_BY_PASS_CONFIG_SERVICE_LIST.getName() + valueWrong);
		}

		try {
			valueWrong = " : Not Found";
			if (ConfigureTool.getConfigure(ConfigName.LONG_SMPP_GSMNETWORKSPECIFICFEATURES) == null) {
				throw new ConfigException(ConfigName.LONG_SMPP_GSMNETWORKSPECIFICFEATURES.getName() + valueWrong);
			}
		}
		catch (Exception e) {
			throw new ConfigException(ConfigName.LONG_SMPP_GSMNETWORKSPECIFICFEATURES.getName() + valueWrong);
		}



	}
}
