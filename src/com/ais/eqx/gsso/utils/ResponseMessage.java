package com.ais.eqx.gsso.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.GssoCommand;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.SoapResultCode;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.ResponseResultCode;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.GssoMessageType;
import com.ais.eqx.gsso.interfaces.MessageResponsePrefix;
import com.ais.eqx.gsso.jaxb.InstanceContext;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;

public class ResponseMessage {

	/** Response ResultCode Message **/
	public static String ResponseResultCodeMessage(ResponseResultCode response) {

		String ResultCodeMessage = null;
		try {
			ResultCodeMessage = InstanceContext.getGson().toJson(response);
		}
		catch (Exception e) {
			ResultCodeMessage = "";
		}

		return ResultCodeMessage;

	}

	/** Response ResultCode Message (SOAP) **/
	public static String responseUSMPErrorMessage(APPInstance appInstance, ResponseResultCode resultCode, EquinoxRawData origRawdata,
			String rootName) {

		StringBuilder soapOutBuilder = new StringBuilder();
		String link = "";
		soapOutBuilder.append("<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		soapOutBuilder.append("<S:Body>");
//		soapOutBuilder.append("<ns2:" + rootName + " xmlns:ns2=\"http://ws.sso.gsso/\">");
		
		String[] possibleValue = new String[] { MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_RESPONSE,
				MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_ID_RESPONSE,
				MessageResponsePrefix.WS_CREATE_ONETIMEPASSWORD_RESPONSE,
				MessageResponsePrefix.WS_GENERATE_ONETIMEPASSWORD_RESPONSE,
				MessageResponsePrefix.WS_CONFIRM_ONETIMEPASSWORD_RESPONSE,
				MessageResponsePrefix.WS_CONFIRM_ONETIMEPASSWORD_ID_RESPONSE, };
		if(Arrays.asList(possibleValue).contains(rootName)){
			link = "ws.gsso";
			resultCode.setIsSuccess(resultCode.getIsSuccess().toLowerCase());
		}
		else{
			link = "ws.sso.gsso";
		}
		soapOutBuilder.append("<ns2:"+ rootName + " xmlns:ns2=\"http://"+link+"/\">");
		soapOutBuilder.append("<return>");
		soapOutBuilder.append("<code>" + resultCode.getCode() + "</code>");
		soapOutBuilder.append("<description>" + resultCode.getDescription() + "</description>");
		soapOutBuilder.append("<isSuccess>" + resultCode.getIsSuccess() + "</isSuccess>");

		/* Response for only sso command */
		if(link.equals("ws.sso.gsso")){
			if (appInstance.getProfile() == null || appInstance.getProfile().getOper() == null
					|| appInstance.getProfile().getOper().isEmpty()) {
				soapOutBuilder.append("<operName/>");
			}
			else {
				soapOutBuilder.append("<operName>" + appInstance.getProfile().getOper() + "</operName>");
			}
		}

		soapOutBuilder.append("<orderRef>" + resultCode.getOrderRef() + "</orderRef>");
		soapOutBuilder.append("<pwd/>");
		soapOutBuilder.append("<transactionID/>");
		if(rootName.equals(MessageResponsePrefix.GENERATE_PASSKEY_RESPONSE)){
			soapOutBuilder.append("<passKey/>");
		}
		soapOutBuilder.append("</return>");
		soapOutBuilder.append("</ns2:" + rootName + ">");
		soapOutBuilder.append("</S:Body>");
		soapOutBuilder.append("</S:Envelope>");

		return soapOutBuilder.toString();
	}

	public static EquinoxRawData returnMessage_SOAP(OrigInvokeProfile origProfile, String code, String description,
			APPInstance appInstance) {
		EquinoxRawData origRawdata = origProfile.getOrigEquinoxRawData();
		String orderRef = GssoGenerator.generateOrderReference(ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME),
				appInstance.getListOrderReference());

		ResponseResultCode resultCode = new ResponseResultCode();
		resultCode.setCode(code);
		resultCode.setDescription(description);
		resultCode.setIsSuccess("false");
		resultCode.setOrderRef(orderRef);
		resultCode.setTransactionID("");

		String resultcodeMessage = ResponseMessage.responseUSMPErrorMessage(appInstance, resultCode, origRawdata,
				IncomingMessageType.getResponseFormatFrom(origProfile.getIncomingMessageType()));

		EquinoxRawData output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.XML);
		output.setType(EventAction.RESPONSE);
		output.setTo(origRawdata.getOrig());
		output.setInvoke(origRawdata.getInvoke());
		output.setRawMessage(resultcodeMessage);

		return output;
	}

	public static EquinoxRawData returnMessage_JSON(OrigInvokeProfile origProfile, String code, String description,
			APPInstance appInstance) {

		EquinoxRawData origRawdata = origProfile.getOrigEquinoxRawData();
		String orderRef = GssoGenerator.generateOrderReference(ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME),
				appInstance.getListOrderReference());

		ResponseResultCode resultCode = new ResponseResultCode();
		resultCode.setCode(code);
		resultCode.setDescription(description);
		resultCode.setIsSuccess("false");
		resultCode.setOrderRef(orderRef);

		String resultcodeMessage = ResponseMessage.ResponseResultCodeMessage(resultCode);

		String messageType = origProfile.getGssoOTPRequest().getMessageType();
		String rootElement = "";
		if (messageType.equalsIgnoreCase(GssoMessageType.JSON)) {
			/** IDLE_SEND_OTP_REQ **/
			if (origProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())) {
				rootElement = "{\"sendOneTimePWResponse\":";
			}
			/** IDLE_GENERATE_PK **/
			if (origProfile.getIncomingMessageType().equals(IncomingMessageType.GENERATE_PASSKEY_JSON.getMessageType())) {
				rootElement = "{\"generatePasskeyResponse\":";
			}
			resultcodeMessage = rootElement + resultcodeMessage + "}";
		}

		EquinoxRawData output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(origRawdata.getCType());
		output.setType(EventAction.RESPONSE);
		output.setTo(origRawdata.getOrig());
		output.setInvoke(origRawdata.getInvoke());
		output.addRawDataAttribute(EquinoxAttribute.VAL, resultcodeMessage);

		return output;
	}

	public static boolean composeErrorMessage(String origInvoke, ArrayList<EquinoxRawData> rawDatasOutgoing, AbstractAF abstractAF,
			EC02Instance ec02Instance, ArrayList<String> listCode, ArrayList<String> listDescription,
			ArrayList<String> listOrigInvoke, GssoComposeDebugLog composeDebugLog) {

		APPInstance appInstance = ec02Instance.getAppInstance();
		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvoke);
		boolean isSummaryEnable = true;
		listOrigInvoke.add(origInvoke);
		String messageType = null;
		
		if(origProfile.getSendWSOTPRequest()!=null){
			messageType = origProfile.getMessageType();
		}
		else{
			messageType = origProfile.getGssoOTPRequest().getMessageType();
		}
		

		/** MessageType JSON **/
		if (messageType.equals(GssoMessageType.JSON)) {
			EquinoxRawData output = ResponseMessage.returnMessage_JSON(origProfile,
					JsonResultCode.ACCOUNT_TYPE_NOT_MATCH_TYPE.getCode(), JsonResultCode.ACCOUNT_TYPE_NOT_MATCH_TYPE.getDescription(),
					appInstance);
			rawDatasOutgoing.add(output);
			listCode.add(JsonResultCode.ACCOUNT_TYPE_NOT_MATCH_TYPE.getCode());
			listDescription.add(JsonResultCode.ACCOUNT_TYPE_NOT_MATCH_TYPE.getDescription());
		}
		/** MessageType SOAP **/
		else {
			if(origProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)&&(!origProfile.isSendUSMPSecond())){
				EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.WRONG_DUMMY_NUMBER.getCode(),
						SoapResultCode.WRONG_DUMMY_NUMBER.getDescription(), appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(SoapResultCode.WRONG_DUMMY_NUMBER.getCode());
				listDescription.add(SoapResultCode.WRONG_DUMMY_NUMBER.getDescription());
			}
			else if(origProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)&&(origProfile.isSendUSMPSecond())){
				EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.WRONG_DUMMY_NUMBER.getCode(),
						SoapResultCode.WRONG_DUMMY_NUMBER.getDescription(), appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(SoapResultCode.WRONG_DUMMY_NUMBER.getCode());
				listDescription.add(SoapResultCode.WRONG_DUMMY_NUMBER.getDescription());
			}
			else{
				EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.COS_NOT_MATCH_TYPE.getCode(),
						SoapResultCode.COS_NOT_MATCH_TYPE.getDescription(), appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(SoapResultCode.COS_NOT_MATCH_TYPE.getCode());
				listDescription.add(SoapResultCode.COS_NOT_MATCH_TYPE.getDescription());
			}
			
		}

		GssoDataManagement.raiseStatOutErrorForInqSubAndPortChk(origProfile, ec02Instance, composeDebugLog);

		/* REMOVE PROFILE */
		GssoDataManagement.removeProfile(origInvoke, appInstance);

		return isSummaryEnable;
	}

	public static boolean returnMessage_NotUseService(String origInvoke, ArrayList<EquinoxRawData> rawDatasOutgoing,
			AbstractAF abstractAF, EC02Instance ec02Instance, ArrayList<String> listCode, ArrayList<String> listDescription,
			ArrayList<String> listOrigInvoke, GssoComposeDebugLog composeDebugLog) {

		APPInstance appInstance = ec02Instance.getAppInstance();
		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvoke);
		String messageType ;
		if(origProfile.getGssoOTPRequest()!=null){
			messageType = origProfile.getGssoOTPRequest().getMessageType();
		}
		else{
			messageType = origProfile.getMessageType();
		}
		
		boolean isSummaryEnable = true;
		listOrigInvoke.add(origInvoke);
		/** MessageType JSON **/
		if (messageType.equals(GssoMessageType.JSON)) {
			EquinoxRawData output = ResponseMessage.returnMessage_JSON(origProfile, JsonResultCode.STATE_NOT_USE_SERVICE.getCode(),
					JsonResultCode.STATE_NOT_USE_SERVICE.getDescription(), appInstance);
			rawDatasOutgoing.add(output);
			listCode.add(JsonResultCode.STATE_NOT_USE_SERVICE.getCode());
			listDescription.add(JsonResultCode.STATE_NOT_USE_SERVICE.getDescription());
		}
		/** MessageType SOAP **/
		else {
			EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.STATE_NOT_USE_SERVICE.getCode(),
					SoapResultCode.STATE_NOT_USE_SERVICE.getDescription(), appInstance);
			rawDatasOutgoing.add(output);
			listCode.add(SoapResultCode.STATE_NOT_USE_SERVICE.getCode());
			listDescription.add(SoapResultCode.STATE_NOT_USE_SERVICE.getDescription());
		}

		GssoDataManagement.raiseStatOutErrorForInqSubAndPortChk(origProfile, ec02Instance, composeDebugLog);

		/* REMOVE PROFILE */
		GssoDataManagement.removeProfile(origInvoke, appInstance);

		/** List Wait InquirySubscriber **/
		Iterator<String> listWaitInqSub = appInstance.getListWaitInquirySub().iterator();
		while (listWaitInqSub.hasNext()) {
			String origInvokeWaiting = (String) listWaitInqSub.next();
			origProfile = appInstance.getMapOrigProfile().get(origInvokeWaiting);
			if(origProfile.getGssoOTPRequest()!=null){
				messageType = origProfile.getGssoOTPRequest().getMessageType();
			}
			else{
				messageType = origProfile.getMessageType();
			}
			listOrigInvoke.add(origInvokeWaiting);
			/** MessageType JSON **/
			if (messageType.equals(GssoMessageType.JSON)) {
				EquinoxRawData output = ResponseMessage.returnMessage_JSON(origProfile,
						JsonResultCode.STATE_NOT_USE_SERVICE.getCode(), JsonResultCode.STATE_NOT_USE_SERVICE.getDescription(),
						appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(JsonResultCode.STATE_NOT_USE_SERVICE.getCode());
				listDescription.add(JsonResultCode.STATE_NOT_USE_SERVICE.getDescription());
			}
			/** MessageType SOAP **/
			else {
				EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile,
						SoapResultCode.STATE_NOT_USE_SERVICE.getCode(), SoapResultCode.STATE_NOT_USE_SERVICE.getDescription(),
						appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(SoapResultCode.STATE_NOT_USE_SERVICE.getCode());
				listDescription.add(SoapResultCode.STATE_NOT_USE_SERVICE.getDescription());
			}

			GssoDataManagement.raiseStatOutErrorForInqSubAndPortChk(origProfile, ec02Instance, composeDebugLog);

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfile(origInvokeWaiting, appInstance);

			/** remove ListWaitInqSub **/
			listWaitInqSub.remove();
		}

		return isSummaryEnable;
	}

	public static void returnMessage_Error(String origInvokeProcess, ArrayList<EquinoxRawData> rawDatasOutgoing,
			AbstractAF abstractAF, EC02Instance ec02Instance, ArrayList<String> listCode, ArrayList<String> listDescription,
			ArrayList<String> listOrigInvoke, GssoComposeDebugLog composeDebugLog, boolean isCheckDoVas) {

		APPInstance appInstance = ec02Instance.getAppInstance();
		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvokeProcess);
		String messageType ;
		if(origProfile.getGssoOTPRequest()!=null){
			messageType = origProfile.getGssoOTPRequest().getMessageType();
		}
		else{
			messageType = origProfile.getMessageType();
		}
		listOrigInvoke.add(origInvokeProcess);

		// Message Type JSON
		if (messageType.equals(GssoMessageType.JSON)) {
			EquinoxRawData output = ResponseMessage.returnMessage_JSON(origProfile, JsonResultCode.USMP_ERROR.getCode(),
					JsonResultCode.USMP_ERROR.getDescription(), appInstance);
			rawDatasOutgoing.add(output);
			listCode.add(JsonResultCode.USMP_ERROR.getCode());
			listDescription.add(JsonResultCode.USMP_ERROR.getDescription());
		}
		// Message Type SOAP
		else {
			
			/** GSSO WS **/
			if(origProfile.getSendWSOTPRequest()!=null){
				
				if(composeDebugLog.getStatisticIn().get(0)
						.contains(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_ERROR.getStatistic())
						|| composeDebugLog.getStatisticIn().get(0)
						.contains(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_REJECT.getStatistic())
						|| composeDebugLog.getStatisticIn().get(0)
						.contains(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_ABORT.getStatistic())
						|| composeDebugLog.getStatisticIn().get(0)
						.contains(Statistic.GSSO_RECEIVED_USMP_INQUIRYSUBSCRIBER_RESPONSE_ERROR.getStatistic())
						|| composeDebugLog.getStatisticIn().get(0)
						.contains(Statistic.GSSO_RECEIVED_USMP_INQUIRYSUBSCRIBER_RESPONSE_REJECT.getStatistic())
						|| composeDebugLog.getStatisticIn().get(0)
						.contains(Statistic.GSSO_RECEIVED_USMP_INQUIRYSUBSCRIBER_RESPONSE_ABORT.getStatistic())){
					EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.USMP_ERROR.getCode(),
							SoapResultCode.USMP_ERROR.getDescription(), appInstance);
					rawDatasOutgoing.add(output);
					listCode.add(SoapResultCode.USMP_ERROR.getCode());
					listDescription.add(SoapResultCode.USMP_ERROR.getDescription());
				}
				else if(composeDebugLog.getStatisticIn().get(0)
						.contains(Statistic.GSSO_RECEIVED_BAD_USMP_INQUIRYVASSUBSCRIBER_RESPONSE.getStatistic())
						|| composeDebugLog.getStatisticIn().get(0)
						.contains(Statistic.GSSO_RECEIVED_BAD_USMP_INQUIRYSUBSCRIBER_RESPONSE.getStatistic())){
					EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.USMP_ERROR.getCode(),
							SoapResultCode.USMP_ERROR.getDescription(), appInstance);
					rawDatasOutgoing.add(output);
					listCode.add(SoapResultCode.USMP_ERROR.getCode());
					listDescription.add(SoapResultCode.USMP_ERROR.getDescription());
				}
				else if(origProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)&&(!origProfile.isSendUSMPSecond())){
					EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.WRONG_DUMMY_NUMBER.getCode(),
							SoapResultCode.WRONG_DUMMY_NUMBER.getDescription(), appInstance);
					rawDatasOutgoing.add(output);
					listCode.add(SoapResultCode.WRONG_DUMMY_NUMBER.getCode());
					listDescription.add(SoapResultCode.WRONG_DUMMY_NUMBER.getDescription());
				}
				else{
					EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.UNKNOWN_MSISDN.getCode(),
							SoapResultCode.UNKNOWN_MSISDN.getDescription(), appInstance);
					rawDatasOutgoing.add(output);
					listCode.add(SoapResultCode.UNKNOWN_MSISDN.getCode());
					listDescription.add(SoapResultCode.UNKNOWN_MSISDN.getDescription());
				}
				
			}
			/** GSSO OLD SSO **/
			else{
				EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.USMP_ERROR.getCode(),
						SoapResultCode.USMP_ERROR.getDescription(), appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(SoapResultCode.USMP_ERROR.getCode());
				listDescription.add(SoapResultCode.USMP_ERROR.getDescription());
			}
					
		}

		GssoDataManagement.raiseStatOutErrorForInqSubAndPortChk(origProfile, ec02Instance, composeDebugLog);

		/* REMOVE PROFILE */
		GssoDataManagement.removeProfile(origInvokeProcess, appInstance);

		/** List Wait InquirySubscriber **/
		Iterator<String> listWaitInqSub = appInstance.getListWaitInquirySub().iterator();
		while (listWaitInqSub.hasNext()) {
			String origInvoke = (String) listWaitInqSub.next();
			origProfile = appInstance.getMapOrigProfile().get(origInvoke);
			if(origProfile.getGssoOTPRequest()!=null){
				messageType = origProfile.getGssoOTPRequest().getMessageType();
			}
			else{
				messageType = origProfile.getMessageType();
			}
			listOrigInvoke.add(origInvoke);
			
			String eventLog = "";
			if(isCheckDoVas){
				if(appInstance.isInquiryVasSubscriber()){
					eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();
				}
				else{
					eventLog = EventLog.INQUIRY_SUBSCRIBER.getEventLog();
				}
			}
			else{
				eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();
			}
			
			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, eventLog);

			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

			/** MessageType JSON **/
			if (messageType.equals(GssoMessageType.JSON)) {
				EquinoxRawData output = ResponseMessage.returnMessage_JSON(origProfile, JsonResultCode.USMP_ERROR.getCode(),
						JsonResultCode.USMP_ERROR.getDescription(), appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(JsonResultCode.USMP_ERROR.getCode());
				listDescription.add(JsonResultCode.USMP_ERROR.getDescription());
			}
			/** MessageType SOAP **/
			else {
				EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.USMP_ERROR.getCode(),
						SoapResultCode.USMP_ERROR.getDescription(), appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(SoapResultCode.USMP_ERROR.getCode());
				listDescription.add(SoapResultCode.USMP_ERROR.getDescription());
			}

			GssoDataManagement.raiseStatOutErrorForInqSubAndPortChk(origProfile, ec02Instance, composeDebugLog);

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfile(origInvoke, appInstance);

			/** remove ListWaitInqSub **/
			listWaitInqSub.remove();
		}

	}

	public static boolean returnMessage_TimeOut(String origInvokeProcess, ArrayList<EquinoxRawData> rawDatasOutgoing,
			AbstractAF abstractAF, EC02Instance ec02Instance, ArrayList<String> listCode, ArrayList<String> listDescription,
			ArrayList<String> listOrigInvoke, GssoComposeDebugLog composeDebugLog, boolean isCheckDoVas) {

		APPInstance appInstance = ec02Instance.getAppInstance();
		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvokeProcess);
		String messageType ;
		if(origProfile.getGssoOTPRequest()!=null){
			messageType = origProfile.getGssoOTPRequest().getMessageType();
		}else{
			messageType = origProfile.getMessageType();
		}
		
		boolean isSummaryEnable = true;
		listOrigInvoke.add(origInvokeProcess);
		// Message Type JSON
		if (messageType.equals(GssoMessageType.JSON)) {
			EquinoxRawData output = ResponseMessage.returnMessage_JSON(origProfile, JsonResultCode.USMP_TIMEOUT.getCode(),
					JsonResultCode.USMP_TIMEOUT.getDescription(), appInstance);
			rawDatasOutgoing.add(output);
			listCode.add(JsonResultCode.USMP_TIMEOUT.getCode());
			listDescription.add(JsonResultCode.USMP_TIMEOUT.getDescription());
		}
		// Message Type SOAP
		else {
			EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.USMP_TIMEOUT.getCode(),
					SoapResultCode.USMP_TIMEOUT.getDescription(), appInstance);
			rawDatasOutgoing.add(output);
			listCode.add(SoapResultCode.USMP_TIMEOUT.getCode());
			listDescription.add(SoapResultCode.USMP_TIMEOUT.getDescription());
		}

		GssoDataManagement.raiseStatOutErrorForInqSubAndPortChk(origProfile, ec02Instance, composeDebugLog);

		/* REMOVE PROFILE */
		GssoDataManagement.removeProfile(origInvokeProcess, appInstance);

		/** List Wait InquirySubscriber **/
		Iterator<String> listWaitInqSub = appInstance.getListWaitInquirySub().iterator();
		while (listWaitInqSub.hasNext()) {
			String origInvoke = (String) listWaitInqSub.next();
			origProfile = appInstance.getMapOrigProfile().get(origInvoke);
			if(origProfile.getGssoOTPRequest().getMessageType()!=null){
				messageType = origProfile.getGssoOTPRequest().getMessageType();
			}
			else{
				messageType = origProfile.getMessageType();
			}
			listOrigInvoke.add(origInvoke);

			String eventLog = "";
			if(isCheckDoVas){
				if(appInstance.isInquiryVasSubscriber()){
					eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();
				}
				else{
					eventLog = EventLog.INQUIRY_SUBSCRIBER.getEventLog();
				}
			}
			else{
				eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();
			}
			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, eventLog);
			
			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());
			/** MessageType JSON **/
			if (messageType.equals(GssoMessageType.JSON)) {
				EquinoxRawData output = ResponseMessage.returnMessage_JSON(origProfile, JsonResultCode.USMP_TIMEOUT.getCode(),
						JsonResultCode.USMP_TIMEOUT.getDescription(), appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(JsonResultCode.USMP_TIMEOUT.getCode());
				listDescription.add(JsonResultCode.USMP_TIMEOUT.getDescription());
			}
			/** MessageType SOAP **/
			else {
				EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.USMP_TIMEOUT.getCode(),
						SoapResultCode.USMP_TIMEOUT.getDescription(), appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(SoapResultCode.USMP_TIMEOUT.getCode());
				listDescription.add(SoapResultCode.USMP_TIMEOUT.getDescription());
			}

			GssoDataManagement.raiseStatOutErrorForInqSubAndPortChk(origProfile, ec02Instance, composeDebugLog);

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfile(origInvoke, appInstance);

			/** remove ListWaitInqSub **/
			listWaitInqSub.remove();
		}
		return isSummaryEnable;
	}

	public static boolean returnMessage_UnknownMSISDN(String origInvokeProcess, ArrayList<EquinoxRawData> rawDatasOutgoing,
			AbstractAF abstractAF, EC02Instance ec02Instance, ArrayList<String> listCode, ArrayList<String> listDescription,
			ArrayList<String> listOrigInvoke, GssoComposeDebugLog composeDebugLog) {

		boolean isSummaryEnable = true;
		APPInstance appInstance = ec02Instance.getAppInstance();
		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvokeProcess);
		String messageType;
		if(origProfile.getGssoOTPRequest()!=null){
			messageType = origProfile.getGssoOTPRequest().getMessageType();
		}
		else{
			messageType = origProfile.getMessageType();
		}
		
		
		listOrigInvoke.add(origInvokeProcess);

		/** MessageType JSON **/
		if (messageType.equals(GssoMessageType.JSON)) {
			EquinoxRawData output = ResponseMessage.returnMessage_JSON(origProfile, JsonResultCode.UNKNOWN_MSISDN.getCode(),
					JsonResultCode.UNKNOWN_MSISDN.getDescription(), appInstance);
			rawDatasOutgoing.add(output);
			listCode.add(JsonResultCode.UNKNOWN_MSISDN.getCode());
			listDescription.add(JsonResultCode.UNKNOWN_MSISDN.getDescription());
		}
		/** MessageType SOAP **/
		else {
			EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.UNKNOWN_MSISDN.getCode(),
					SoapResultCode.UNKNOWN_MSISDN.getDescription(), appInstance);
			rawDatasOutgoing.add(output);
			listCode.add(SoapResultCode.UNKNOWN_MSISDN.getCode());
			listDescription.add(SoapResultCode.UNKNOWN_MSISDN.getDescription());
		}

		GssoDataManagement.raiseStatOutErrorForInqSubAndPortChk(origProfile, ec02Instance, composeDebugLog);

		/* REMOVE PROFILE */
		GssoDataManagement.removeProfile(origInvokeProcess, appInstance);

		/** ListWaitInqSub **/
		Iterator<String> listWaitInqSub = appInstance.getListWaitInquirySub().iterator();
		while (listWaitInqSub.hasNext()) {

			String origInvoke = (String) listWaitInqSub.next();
			origProfile = appInstance.getMapOrigProfile().get(origInvoke);
			if(origProfile.getGssoOTPRequest()!=null){
				messageType = origProfile.getGssoOTPRequest().getMessageType();
			}
			else{
				messageType = origProfile.getMessageType();
			}
			listOrigInvoke.add(origInvoke);
			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.PORT_CHECK.getEventLog());

			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());
			/** MessageType JSON **/
			if (messageType.equals(GssoMessageType.JSON)) {
				EquinoxRawData output = ResponseMessage.returnMessage_JSON(origProfile, JsonResultCode.UNKNOWN_MSISDN.getCode(),
						JsonResultCode.UNKNOWN_MSISDN.getDescription(), appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(JsonResultCode.UNKNOWN_MSISDN.getCode());
				listDescription.add(JsonResultCode.UNKNOWN_MSISDN.getDescription());
			}
			/** MessageType SOAP **/
			else {
				EquinoxRawData output = ResponseMessage.returnMessage_SOAP(origProfile, SoapResultCode.UNKNOWN_MSISDN.getCode(),
						SoapResultCode.UNKNOWN_MSISDN.getDescription(), appInstance);
				rawDatasOutgoing.add(output);
				listCode.add(SoapResultCode.UNKNOWN_MSISDN.getCode());
				listDescription.add(SoapResultCode.UNKNOWN_MSISDN.getDescription());
			}

			isSummaryEnable = true;

			GssoDataManagement.raiseStatOutErrorForInqSubAndPortChk(origProfile, ec02Instance, composeDebugLog);

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfile(origInvoke, appInstance);

			/** remove ListWaitInqSub **/
			listWaitInqSub.remove();
		}

		return isSummaryEnable;
	}

}
