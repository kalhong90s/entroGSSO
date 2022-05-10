package com.ais.eqx.gsso.validator;

import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.DeliveryReportRequest;
import com.ais.eqx.gsso.instances.SendEmailResponse;
import com.ais.eqx.gsso.instances.SubmitSMResponse;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.utils.GssoDataManagement;

import ec02.af.data.EquinoxRawData;

public class SmsEmailDeliveryValidator {

	public static void deliveryReportValidator(final DeliveryReportRequest deliveryReportRequest, final EquinoxRawData equinoxRawData)
			throws ValidationException {

		final String defaultValue = null;

		String mandatoryPath = null;
		String code = "";
		String isMissing = "Missing";
		String isInvalid = "Invalid";
		String isNull = "Null";

		mandatoryPath = "ctype";
		if (!equinoxRawData.getCType().equalsIgnoreCase(EventCtype.DR)) {
			throw new ValidationException(mandatoryPath, defaultValue, isInvalid);
		}

		/** TYPE **/
		mandatoryPath = "type";
		if (!equinoxRawData.getType().equalsIgnoreCase(EventAction.REQUEST)) {
			throw new ValidationException(mandatoryPath, defaultValue, isInvalid);
		}

		mandatoryPath = "mandatory";
		/** mandatory **/
		if (deliveryReportRequest == null) {
			throw new ValidationException(mandatoryPath, defaultValue, isMissing);
		}

		/** 1short_message value **/
		mandatoryPath = "short_message";
		if (deliveryReportRequest.getShortMessage() == null) {
			throw new ValidationException(mandatoryPath, defaultValue, isMissing);
		}
		else {
			if (deliveryReportRequest.getShortMessage().isEmpty()) {
				throw new ValidationException(mandatoryPath, defaultValue, isNull);
			}

			try {
				String message = GssoDataManagement.convertHexToString(deliveryReportRequest.getShortMessage());
				String id[] = message.trim().split("id:");
				code = "" + Long.parseLong(id[1].trim().split(" ")[0]);

			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, defaultValue, isInvalid);

			}
		}

		/** 2service_type value **/
		mandatoryPath = "service_type";
		if (deliveryReportRequest.getServiceType() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}

		/** 3source_addr_ton value **/
		mandatoryPath = "source_addr_ton";
		if (deliveryReportRequest.getSourceAddrTon() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}

		/** 4source_addr_npi value **/
		mandatoryPath = "source_addr_npi";
		if (deliveryReportRequest.getSourceAddrNpi() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}

		/** 5source_addr value **/
		mandatoryPath = "source_addr";
		if (deliveryReportRequest.getSourceAddr() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}

		/** 6dest_addr_ton value **/
		mandatoryPath = "dest_addr_ton";
		if (deliveryReportRequest.getDestAddrTon() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}
		else if (deliveryReportRequest.getDestAddrTon().isEmpty()) {
			throw new ValidationException(mandatoryPath, code, isNull);
		}

		/** dest_addr_npi value **/
		mandatoryPath = "dest_addr_npi";
		if (deliveryReportRequest.getDestAddrNpi() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}
		else if (deliveryReportRequest.getDestAddrNpi().isEmpty()) {
			throw new ValidationException(mandatoryPath, code, isNull);
		}

		/** destination_addr value **/
		mandatoryPath = "destination_addr";
		if (deliveryReportRequest.getDestinationAddr() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}
		else if (deliveryReportRequest.getDestinationAddr().isEmpty()) {
			throw new ValidationException(mandatoryPath, code, isNull);
		}

		mandatoryPath = "esm_class";
		if ((deliveryReportRequest.getEmmessagingMode() == null) && (deliveryReportRequest.getEmmessageType() == null)
				&& (deliveryReportRequest.getEmgsmNetworkSpecificFeatures() == null)) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}
		else {
			/** esm_class/MessagingMode **/
			mandatoryPath = "MessagingMode";
			if (deliveryReportRequest.getEmmessagingMode() == null) {
				throw new ValidationException(mandatoryPath, code, isMissing);
			}
			else if (deliveryReportRequest.getEmmessagingMode().isEmpty()) {
				throw new ValidationException(mandatoryPath, code, isNull);
			}
			/** esm_class/MessageType **/
			mandatoryPath = "MessageType";
			if (deliveryReportRequest.getEmmessageType() == null) {
				throw new ValidationException(mandatoryPath, code, isMissing);
			}
			else if (deliveryReportRequest.getEmmessageType().isEmpty()) {
				throw new ValidationException(mandatoryPath, code, isNull);
			}

			/** esm_class/GSMNetworkSpecificFeatures **/
			mandatoryPath = "GSMNetworkSpecificFeatures";
			if (deliveryReportRequest.getEmgsmNetworkSpecificFeatures() == null) {
				throw new ValidationException(mandatoryPath, code, isMissing);
			}
			else if (deliveryReportRequest.getEmgsmNetworkSpecificFeatures().isEmpty()) {
				throw new ValidationException(mandatoryPath, code, isNull);
			}
		}

		/** protocol_id value **/
		mandatoryPath = "protocol_id";
		if (deliveryReportRequest.getProtocolId() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}
		else if (deliveryReportRequest.getProtocolId().isEmpty()) {
			throw new ValidationException(mandatoryPath, code, isNull);
		}

		/** priority_flag value **/
		mandatoryPath = "priority_flag";
		if (deliveryReportRequest.getPriorityFlag() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}
		else if (deliveryReportRequest.getPriorityFlag().isEmpty()) {
			throw new ValidationException(mandatoryPath, code, isNull);
		}

		/** schedule_delivery_time value **/
		mandatoryPath = "schedule_delivery_time";
		if (deliveryReportRequest.getScheduleDeliveryTime() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}

		/** validity_period value **/
		mandatoryPath = "validity_period";
		if (deliveryReportRequest.getValidityPeriod() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}

		/** validity_period value **/
		mandatoryPath = "registered_delivery";
		if (deliveryReportRequest.getRdsmsCDeliveryReceipt() == null && deliveryReportRequest.getRdsmeOriginatedAck() == null
				&& deliveryReportRequest.getRdintermediateNotification() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}
		else {

			/** registered_delivery/SMSCDeliveryReceipt **/
			mandatoryPath = "SMSCDeliveryReceipt";
			if (deliveryReportRequest.getRdsmsCDeliveryReceipt() == null) {
				throw new ValidationException(mandatoryPath, code, isMissing);
			}
			else if (deliveryReportRequest.getRdsmsCDeliveryReceipt().isEmpty()) {
				throw new ValidationException(mandatoryPath, code, isNull);
			}

			/** registered_delivery/SMEOriginatedAck **/
			mandatoryPath = "SMSCDeliveryReceipt";
			if (deliveryReportRequest.getRdsmeOriginatedAck() == null) {
				throw new ValidationException(mandatoryPath, code, isMissing);
			}
			else if (deliveryReportRequest.getRdsmeOriginatedAck().isEmpty()) {
				throw new ValidationException(mandatoryPath, code, isNull);
			}

			/** registered_delivery/IntermediateNotification **/
			mandatoryPath = "IntermediateNotification";
			if (deliveryReportRequest.getRdintermediateNotification() == null) {
				throw new ValidationException(mandatoryPath, code, isMissing);
			}
			else if (deliveryReportRequest.getRdintermediateNotification().isEmpty()) {
				throw new ValidationException(mandatoryPath, code, isNull);
			}
		}

		/** replace_if_present_flag value **/
		mandatoryPath = "replace_if_present_flag";
		if (deliveryReportRequest.getReplaceIfPresentFlag() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}

		/** data_coding value **/
		mandatoryPath = "data_coding";
		if (deliveryReportRequest.getDataCoding() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}

		else if (deliveryReportRequest.getDataCoding().isEmpty()) {
			throw new ValidationException(mandatoryPath, code, isNull);
		}

		/** sm_default_msg_id value **/
		mandatoryPath = "sm_default_msg_id";
		if (deliveryReportRequest.getSmDefaultMsgId() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}

		/** sm_length value **/
		mandatoryPath = "sm_length";
		if (deliveryReportRequest.getSmLength() == null) {
			throw new ValidationException(mandatoryPath, code, isMissing);
		}
		else if (deliveryReportRequest.getSmLength().isEmpty()) {
			throw new ValidationException(mandatoryPath + ":sm_length", code, isNull);
		}

	}

	public static void sendSMSValidator(final SubmitSMResponse submitSMResponse, final EquinoxRawData equinoxRawData)
			throws ValidationException {

		final String defaultValue = null;

		String mandatoryPath = null;
		String isNull = "Null";
		String isMissing = "Missing";
		String isInvalid = "Invalid";

		/** CTYPE **/
		mandatoryPath = "ctype";
		if (!equinoxRawData.getCType().equalsIgnoreCase(EventCtype.SMS)) {
			throw new ValidationException(mandatoryPath, defaultValue, isInvalid);
		}

		/** TYPE **/
		mandatoryPath = "type";
		if (!equinoxRawData.getType().equalsIgnoreCase(EventAction.RESPONSE)) {
			throw new ValidationException(mandatoryPath, defaultValue, isInvalid);
		}

		/** ECODE **/
		mandatoryPath = "ecode";
		if (!equinoxRawData.getRawDataAttribute("ecode").isEmpty()) {
			throw new ValidationException(mandatoryPath, defaultValue, isInvalid);
		}

		/** mandatory **/
		mandatoryPath = "mandatory";
		if (submitSMResponse == null) {
			throw new ValidationException(mandatoryPath, isMissing, isMissing);
		}
		else {

			mandatoryPath = "message_id";
			/** message_id value **/
			if (submitSMResponse.getMessageId() == null) {
				throw new ValidationException(mandatoryPath, isMissing, isMissing);
			}
			else if (submitSMResponse.getMessageId().isEmpty()) {
				throw new ValidationException(mandatoryPath, isNull, isNull);
			}

			try {
				Long.parseLong(submitSMResponse.getMessageId().trim(), 16);
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, submitSMResponse.getMessageId(), isInvalid);
			}

		}

	}

	public static void sendEmailValidator(final SendEmailResponse sendEmailResponse, final EquinoxRawData equinoxRawData)
			throws ValidationException {

		final String defaultValue = null;

		String mandatoryPath = null;
		String isNull = "Null";
		String isMissing = "Missing";
		String isInvalid = "Invalid";

		/** CTYPE **/
		mandatoryPath = "ctype";
		if (!equinoxRawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {
			throw new ValidationException(mandatoryPath, defaultValue, isInvalid);
		}

		/** TYPE **/
		mandatoryPath = "type";
		if (!equinoxRawData.getType().equalsIgnoreCase(EventAction.RESPONSE)) {
			throw new ValidationException(mandatoryPath, defaultValue, isInvalid);
		}

		mandatoryPath = "resultCode";
		if (sendEmailResponse == null) {
			throw new ValidationException(mandatoryPath, defaultValue, isMissing);
		}

		/** resultCode **/
		if (sendEmailResponse.getResultCode() == null) {
			throw new ValidationException(mandatoryPath, defaultValue, isMissing);
		}
		else if (sendEmailResponse.getResultCode().isEmpty()) {
			throw new ValidationException(mandatoryPath, defaultValue, isNull);
		}

	}
}
