package com.ais.eqx.gsso.utils;

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.GssoCommand;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.SoapResultCode;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GenPasskey;
import com.ais.eqx.gsso.instances.GssoConfirmOTPRequest;
import com.ais.eqx.gsso.instances.GssoGenPasskeyRequest;
import com.ais.eqx.gsso.instances.GssoInqSubRequest;
import com.ais.eqx.gsso.instances.GssoOTPRequest;
import com.ais.eqx.gsso.instances.GssoProfile;
import com.ais.eqx.gsso.instances.GssoServiceTemplate;
import com.ais.eqx.gsso.instances.GssoWSConfirmOTPRequest;
import com.ais.eqx.gsso.instances.GssoWSConfirmOTPWithIDRequest;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.PortCheckReq;
import com.ais.eqx.gsso.instances.Refund;
import com.ais.eqx.gsso.instances.SendConfirmOTPRequest;
import com.ais.eqx.gsso.instances.SendOneTimePWRequest;
import com.ais.eqx.gsso.instances.SendWSConfirmOTPRequest;
import com.ais.eqx.gsso.instances.SendWSConfirmOTPWithIDRequest;
import com.ais.eqx.gsso.instances.SendWSOTPRequest;
import com.ais.eqx.gsso.instances.TransactionData;
import com.ais.eqx.gsso.interfaces.BooleanString;
import com.ais.eqx.gsso.interfaces.EQX;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.GssoLanguage;
import com.ais.eqx.gsso.interfaces.GssoMessageType;
import com.ais.eqx.gsso.interfaces.MessageResponsePrefix;
import com.ais.eqx.gsso.interfaces.SentOTPResult;
import com.ais.eqx.gsso.interfaces.SourceAddrNpi;
import com.ais.eqx.gsso.interfaces.SourceAddrTon;
import com.ais.eqx.gsso.parser.MessageParser;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.GlobalData;
import ec02.common.data.KeyObject;



public class GssoConstructMessage {

	public static EquinoxRawData createInquirySubReqToUSMPMessage(final EquinoxRawData rawDataIncoming, EC02Instance ec02Instance,
			final GssoOTPRequest otpRequest, GssoComposeDebugLog composeDebugLog) {

		EquinoxRawData output = null;
		APPInstance appInstance = ec02Instance.getAppInstance();

		appInstance.setWaitInquirySub(true);

		/** VALID OTP REQ STATICTIC **/
		String statOut = "";
		String invokeOutgoing = "";
		String eventLog = "";
		if(appInstance.isInquiryVasSubscriber()){
			statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
			eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();
		}
		else{
			statOut = Statistic.GSSO_SEND_USMP_INQUIRYSUBSCRIBER_REQUEST.getStatistic();
			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_SUB.name());
			eventLog = EventLog.INQUIRY_SUBSCRIBER.getEventLog();
		}
		
		SendOneTimePWRequest sendOneTimePW = otpRequest.getSendOneTimePW();

		/** THE APPLICATION SHALL OUTPUT AN INQUIRYSUBSCRIBER REQUEST TO USMP **/
		GssoInqSubRequest inqSubReq = new GssoInqSubRequest();
		inqSubReq.setMsisdn(sendOneTimePW.getMsisdn());
		inqSubReq.setUserName(ConfigureTool.getConfigure(ConfigName.USMP_NODE_NAME));
		inqSubReq.setOrderDesc(ConfigureTool.getConfigure(ConfigName.USMP_ORDER_DESC));
		inqSubReq.setOrderRef(GssoGenerator.generateOrderReferenceToUSMP());

		String soapOut = GssoConstructSoapMessage.composeInqSubReqToUSMP(inqSubReq, appInstance, true);

		output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.XML);
		output.setType(EventAction.REQUEST);
		output.setTo(ConfigureTool.getConfigure(ConfigName.USMP_INQUIRYSUB_INTERFACE));
		output.setInvoke(invokeOutgoing);
		output.setRawMessage(soapOut);
		
		ec02Instance.incrementsStat(statOut);
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, eventLog);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			composeDebugLog.addStatisticOut(statOut);
			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return output;
	}

//	public static EquinoxRawData createWSInquirySubReqToUSMPMessage(final EquinoxRawData rawDataIncoming, EC02Instance ec02Instance,
//			final GssoWSAuthOTPRequest otpRequest, GssoComposeDebugLog composeDebugLog) {
//
//		EquinoxRawData output = null;
//		APPInstance appInstance = ec02Instance.getAppInstance();
//
//		appInstance.setWaitInquirySub(true);
//
//		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_SUB.name());
//
//		WSSendOTP sendWSAuthOTPRequest = otpRequest.getSendWSAuthOTPReq();
//
//		/** THE APPLICATION SHALL OUTPUT AN INQUIRYSUBSCRIBER REQUEST TO USMP **/
//		GssoInqSubRequest inqSubReq = new GssoInqSubRequest();
//		inqSubReq.setMsisdn(sendWSAuthOTPRequest.getMsisdn());
//		inqSubReq.setUserName(ConfigureTool.getConfigure(ConfigName.USMP_NODE_NAME));
//		inqSubReq.setOrderDesc(ConfigureTool.getConfigure(ConfigName.USMP_ORDER_DESC));
//		inqSubReq.setOrderRef(GssoGenerator.generateOrderReferenceToUSMP());
//
//		String soapOut = GssoConstructSoapMessage.composeInqSubReqToUSMP(inqSubReq);
//
//		output = new EquinoxRawData();
//		output.setName(EventName.HTTP);
//		output.setCType(EventCtype.XML);
//		output.setType(EventAction.REQUEST);
//		output.setTo(ConfigureTool.getConfigure(ConfigName.USMP_INQUIRYSUB_INTERFACE));
//		output.setInvoke(invokeOutgoing);
//		output.setRawMessage(soapOut);
//
//		/** VALID OTP REQ STATICTIC **/
//		ec02Instance.incrementsStat(Statistic.GSSO_SEND_USMP_INQUIRYSUBSCRIBER_REQUEST.getStatistic());
//		// ===============================================WRITE
//		// DETAILS======================================================
//		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.INQUIRY_SUBSCRIBER.getEventLog());
//		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
//		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//
//		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
//			// ===============================================DEBUG
//			// LOG==========================================================
//			composeDebugLog.addStatisticOut(Statistic.GSSO_SEND_USMP_INQUIRYSUBSCRIBER_REQUEST.getStatistic());
//			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
//			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
//			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//		}
//
//		return output;
//	}
	public static EquinoxRawData createInquirySubReqToUSMPMessageFromWSCreateOTP(final EquinoxRawData rawDataIncoming, EC02Instance ec02Instance,
			final SendWSOTPRequest otpRequest, GssoComposeDebugLog composeDebugLog) {

		EquinoxRawData output = null;
		APPInstance appInstance = ec02Instance.getAppInstance();

		appInstance.setWaitInquirySub(true);

		/** VALID OTP REQ STATICTIC **/
		String statOut = "";
		String invokeOutgoing = "";
		String eventLog = "";
//		if(appInstance.isInquiryVasSubscriber()){
//			statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
//			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
//			eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();
//		}
//		else{
//			statOut = Statistic.GSSO_SEND_USMP_INQUIRYSUBSCRIBER_REQUEST.getStatistic();
//			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_SUB.name());
//			eventLog = EventLog.INQUIRY_SUBSCRIBER.getEventLog();
//		}
		
		statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
		invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
		eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();

		/** THE APPLICATION SHALL OUTPUT AN INQUIRYSUBSCRIBER REQUEST TO USMP **/
		GssoInqSubRequest inqSubReq = new GssoInqSubRequest();
		inqSubReq.setMsisdn(otpRequest.getMsisdn());
		inqSubReq.setUserName(ConfigureTool.getConfigure(ConfigName.USMP_NODE_NAME));
		inqSubReq.setOrderDesc(ConfigureTool.getConfigure(ConfigName.USMP_ORDER_DESC));
		inqSubReq.setOrderRef(GssoGenerator.generateOrderReferenceToUSMP());

		String soapOut = GssoConstructSoapMessage.composeInqSubReqToUSMP(inqSubReq, appInstance, false);

		output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.XML);
		output.setType(EventAction.REQUEST);
		output.setTo(ConfigureTool.getConfigure(ConfigName.USMP_INQUIRYSUB_INTERFACE));
		output.setInvoke(invokeOutgoing);
		output.setRawMessage(soapOut);

		/** VALID OTP REQ STATICTIC **/
		ec02Instance.incrementsStat(statOut);
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, eventLog);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			composeDebugLog.addStatisticOut(statOut);
			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return output;
	}
	
	public static EquinoxRawData createInquirySubReqToUSMPMessageFromWSGenerateOTP(final EquinoxRawData rawDataIncoming, EC02Instance ec02Instance,
			final SendWSOTPRequest otpRequest, GssoComposeDebugLog composeDebugLog) {

		EquinoxRawData output = null;
		APPInstance appInstance = ec02Instance.getAppInstance();

		appInstance.setWaitInquirySub(true);

		/** VALID OTP REQ STATICTIC **/
		String statOut = "";
		String invokeOutgoing = "";
		String eventLog = "";
//		if(appInstance.isInquiryVasSubscriber()){
//			statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
//			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
//			eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();
//		}
//		else{
//			statOut = Statistic.GSSO_SEND_USMP_INQUIRYSUBSCRIBER_REQUEST.getStatistic();
//			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_SUB.name());
//			eventLog = EventLog.INQUIRY_SUBSCRIBER.getEventLog();
//		}
		
		statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
		invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
		eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();

		/** THE APPLICATION SHALL OUTPUT AN INQUIRYSUBSCRIBER REQUEST TO USMP **/
		GssoInqSubRequest inqSubReq = new GssoInqSubRequest();
		inqSubReq.setMsisdn(otpRequest.getMsisdn());
		inqSubReq.setUserName(ConfigureTool.getConfigure(ConfigName.USMP_NODE_NAME));
		inqSubReq.setOrderDesc(ConfigureTool.getConfigure(ConfigName.USMP_ORDER_DESC));
		inqSubReq.setOrderRef(GssoGenerator.generateOrderReferenceToUSMP());

		String soapOut = GssoConstructSoapMessage.composeInqSubReqToUSMP(inqSubReq, appInstance, false);

		output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.XML);
		output.setType(EventAction.REQUEST);
		output.setTo(ConfigureTool.getConfigure(ConfigName.USMP_INQUIRYSUB_INTERFACE));
		output.setInvoke(invokeOutgoing);
		output.setRawMessage(soapOut);

		/** VALID OTP REQ STATICTIC **/
		ec02Instance.incrementsStat(statOut);
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, eventLog);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			composeDebugLog.addStatisticOut(statOut);
			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return output;
	}
	// Already WSSendOTP
	public static EquinoxRawData createInquirySubReqToUSMPMessageFromWSAuthenOTP(final EquinoxRawData rawDataIncoming, EC02Instance ec02Instance,
			final SendWSOTPRequest otpRequest, GssoComposeDebugLog composeDebugLog) {

		EquinoxRawData output = null;
		APPInstance appInstance = ec02Instance.getAppInstance();

		appInstance.setWaitInquirySub(true);

		/** VALID OTP REQ STATICTIC **/
		String statOut = "";
		String invokeOutgoing = "";
		String eventLog = "";
//		if(appInstance.isInquiryVasSubscriber()){
//			statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
//			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
//			eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();
//		}
//		else{
//			statOut = Statistic.GSSO_SEND_USMP_INQUIRYSUBSCRIBER_REQUEST.getStatistic();
//			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_SUB.name());
//			eventLog = EventLog.INQUIRY_SUBSCRIBER.getEventLog();
//		}

		statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
		invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
		eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();

		/** THE APPLICATION SHALL OUTPUT AN INQUIRYSUBSCRIBER REQUEST TO USMP **/
		GssoInqSubRequest inqSubReq = new GssoInqSubRequest();
		inqSubReq.setMsisdn(otpRequest.getMsisdn());
		inqSubReq.setUserName(ConfigureTool.getConfigure(ConfigName.USMP_NODE_NAME));
		inqSubReq.setOrderDesc(ConfigureTool.getConfigure(ConfigName.USMP_ORDER_DESC));
		inqSubReq.setOrderRef(GssoGenerator.generateOrderReferenceToUSMP());

		String soapOut = GssoConstructSoapMessage.composeInqSubReqToUSMP(inqSubReq, appInstance, false);

		output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.XML);
		output.setType(EventAction.REQUEST);
		output.setTo(ConfigureTool.getConfigure(ConfigName.USMP_INQUIRYSUB_INTERFACE));
		output.setInvoke(invokeOutgoing);
		output.setRawMessage(soapOut);

		/** VALID OTP REQ STATICTIC **/
		ec02Instance.incrementsStat(statOut);
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, eventLog);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			composeDebugLog.addStatisticOut(statOut);
			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return output;
	}
	
	public static EquinoxRawData createInquirySubReqToUSMPMessageFromWSAuthenOTPSecondTIme(final EquinoxRawData rawDataIncoming, EC02Instance ec02Instance,
			final SendWSOTPRequest otpRequest, GssoComposeDebugLog composeDebugLog) {

		EquinoxRawData output = null;
		APPInstance appInstance = ec02Instance.getAppInstance();

		appInstance.setWaitInquirySub(true);

		/** VALID OTP REQ STATICTIC **/
		String statOut = "";
		String invokeOutgoing = "";
		String eventLog = "";
//		if(appInstance.isInquiryVasSubscriber()){
//			statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
//			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
//			eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();
//		}
//		else{
//			statOut = Statistic.GSSO_SEND_USMP_INQUIRYSUBSCRIBER_REQUEST.getStatistic();
//			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_SUB.name());
//			eventLog = EventLog.INQUIRY_SUBSCRIBER.getEventLog();
//		}

		statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
		invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
		eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();

		/** THE APPLICATION SHALL OUTPUT AN INQUIRYSUBSCRIBER REQUEST TO USMP **/
		GssoInqSubRequest inqSubReq = new GssoInqSubRequest();
		inqSubReq.setMsisdn(otpRequest.getOtpMobile());
		inqSubReq.setUserName(ConfigureTool.getConfigure(ConfigName.USMP_NODE_NAME));
		inqSubReq.setOrderDesc(ConfigureTool.getConfigure(ConfigName.USMP_ORDER_DESC));
		inqSubReq.setOrderRef(GssoGenerator.generateOrderReferenceToUSMP());

		String soapOut = GssoConstructSoapMessage.composeInqSubReqToUSMP(inqSubReq, appInstance, false);

		output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.XML);
		output.setType(EventAction.REQUEST);
		output.setTo(ConfigureTool.getConfigure(ConfigName.USMP_INQUIRYSUB_INTERFACE));
		output.setInvoke(invokeOutgoing);
		output.setRawMessage(soapOut);

		/** VALID OTP REQ STATICTIC **/
		ec02Instance.incrementsStat(statOut);
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, eventLog);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			composeDebugLog.addStatisticOut(statOut);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}
		
		return output;
	}
	
	public static EquinoxRawData createInquirySubReqToUSMPMessageFromWSAuthenOTPWithID(final EquinoxRawData rawDataIncoming, EC02Instance ec02Instance,
			final SendWSOTPRequest otpRequest, GssoComposeDebugLog composeDebugLog) {

		EquinoxRawData output = null;
		APPInstance appInstance = ec02Instance.getAppInstance();

		appInstance.setWaitInquirySub(true);

		/** VALID OTP REQ STATICTIC **/
		String statOut = "";
		String invokeOutgoing = "";
		String eventLog = "";
//		if(appInstance.isInquiryVasSubscriber()){
//			statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
//			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
//			eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();
//		}
//		else{
//			statOut = Statistic.GSSO_SEND_USMP_INQUIRYSUBSCRIBER_REQUEST.getStatistic();
//			invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_SUB.name());
//			eventLog = EventLog.INQUIRY_SUBSCRIBER.getEventLog();
//		}

		statOut = Statistic.GSSO_SEND_USMP_INQUIRYVASSUBSCRIBER_REQUEST.getStatistic();
		invokeOutgoing = InvokeSubStates.getInvokeOutgoing(rawDataIncoming.getInvoke(), SubStates.W_INQUIRY_VAS_SUB.name());
		eventLog = EventLog.INQUIRY_VASSUBSCRIBER.getEventLog();

		/** THE APPLICATION SHALL OUTPUT AN INQUIRYSUBSCRIBER REQUEST TO USMP **/
		GssoInqSubRequest inqSubReq = new GssoInqSubRequest();
		inqSubReq.setMsisdn(otpRequest.getMsisdn());
		inqSubReq.setUserName(ConfigureTool.getConfigure(ConfigName.USMP_NODE_NAME));
		inqSubReq.setOrderDesc(ConfigureTool.getConfigure(ConfigName.USMP_ORDER_DESC));
		inqSubReq.setOrderRef(GssoGenerator.generateOrderReferenceToUSMP());

		String soapOut = GssoConstructSoapMessage.composeInqSubReqToUSMP(inqSubReq, appInstance, false);

		output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.XML);
		output.setType(EventAction.REQUEST);
		output.setTo(ConfigureTool.getConfigure(ConfigName.USMP_INQUIRYSUB_INTERFACE));
		output.setInvoke(invokeOutgoing);
		output.setRawMessage(soapOut);

		/** VALID OTP REQ STATICTIC **/
		ec02Instance.incrementsStat(statOut);
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, eventLog);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			composeDebugLog.addStatisticOut(statOut);
			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return output;
	}
	
	public static EquinoxRawData createPortCheckToUSMPMessage(final String origInvokeProcess, final EquinoxRawData rawDataIncoming,
			EC02Instance ec02Instance) {

		APPInstance appInstance = ec02Instance.getAppInstance();
		appInstance.setWaitInquirySub(false);
		appInstance.setWaitPortCheck(true);

		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvokeProcess);
		PortCheckReq portCheckReq = new PortCheckReq();
		
		if(origProfile.getSendWSOTPRequest()!=null){
			portCheckReq.setMsisdn(origProfile.getSendWSOTPRequest().getMsisdn());
		}
		else if(origProfile.getGssoOTPRequest()!=null){
			portCheckReq.setMsisdn(origProfile.getGssoOTPRequest().getSendOneTimePW().getMsisdn());
		}
//		else{
//			System.err.println("No data in Instance");
//		}
		
		
		
		portCheckReq.setUserName(ConfigureTool.getConfigure(ConfigName.USMP_NODE_NAME));
		portCheckReq.setOrderDesc(ConfigureTool.getConfigure(ConfigName.USMP_ORDER_DESC));
		portCheckReq.setOrderRef(GssoGenerator.generateOrderReferenceToUSMP());

		String soapOut = GssoConstructSoapMessage.composePortCheckReqToUSMP(portCheckReq);

		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvokeProcess, SubStates.W_PORT_CHECK.name());
		EquinoxRawData output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.XML);
		output.setType(EventAction.REQUEST);
		output.setTo(ConfigureTool.getConfigure(ConfigName.USMP_PORTCHECK_INTERFACE));
		output.setInvoke(invokeOutgoing);
		output.setRawMessage(soapOut);

		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.PORT_CHECK.getEventLog());

		return output;
	}

	public static EquinoxRawData createSMSReqMessage(final String origInvoke, final GssoServiceTemplate serviceTemplate,
			EC02Instance ec02Instance, GssoComposeDebugLog composeDebugLog) {

		EquinoxRawData output = null;
		APPInstance appInstance = ec02Instance.getAppInstance();

		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvoke, SubStates.W_SEND_SMS.toString());

		output = new EquinoxRawData();
		output.setName(EventName.SMPP);
		output.setCType(EventCtype.SMS);
		output.setType(EventAction.REQUEST);
		output.setInvoke(invokeOutgoing);

		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		if (serviceTemplate.getAllowSmsRoaming().equalsIgnoreCase(SentOTPResult.FALSE)) {
			output.setTo(ConfigureTool.getConfigure(ConfigName.SMPPGW_INTERFACE));

			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.SUBMIT_SM.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			origInvokeProfile.setSMPPRoaming(false);
		}
		else {
			output.setTo(ConfigureTool.getConfigure(ConfigName.SMPPGW_ROAMING_INTERFACE));

			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.SMPPGW_ROAMING.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			origInvokeProfile.setSMPPRoaming(true);
		}
		
		if(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)){
			
			String transactionID = origInvokeProfile.getTransactionID();
			TransactionData transactionData = appInstance.getTransactionidData().get(transactionID);

			SendWSOTPRequest sendWSAuthOTPWithIDRequest = origInvokeProfile.getSendWSOTPRequest();
			String otpMobile = sendWSAuthOTPWithIDRequest.getOtpMobile();

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String expireTimeString = sdf.format(transactionData.getOtpExpireTime());

			String language = appInstance.getProfile().getLanguage();
			String smsBody = null;
			String serviceKey = null;

			
				serviceKey = appInstance.getMapE01dataofService().get(sendWSAuthOTPWithIDRequest.getService().toUpperCase()).getServiceKey();

				if (GssoLanguage.THAI.equals(language)) {
					smsBody = serviceTemplate.getSmsBodyThai();
				}
				else {
					smsBody = serviceTemplate.getSmsBodyEng();
				}
			

			smsBody = smsBody.replaceAll("<#SERVICE>", transactionData.getService());
			smsBody = smsBody.replaceAll("<#OTP>", transactionData.getOtp());
			smsBody = smsBody.replaceAll("<#REF>", transactionData.getRefNumber());
			smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", sendWSAuthOTPWithIDRequest.getAddTimeoutMins());
			
			String smsSender = serviceTemplate.getSmsSender();
			smsSender = smsSender.replaceAll("<#SERVICE>", transactionData.getService());
			smsSender = smsSender.replaceAll("<#OTP>", transactionData.getOtp());
			smsSender = smsSender.replaceAll("<#REF>", transactionData.getRefNumber());
			smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", sendWSAuthOTPWithIDRequest.getAddTimeoutMins());

			/* SAVE SMSCDeliveryReceipt */
			origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());

			output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(GssoDataManagement.convertStringToHex(smsBody, true), otpMobile,
					language, smsSender, GssoDataManagement.convertStringToHex(smsBody, false), serviceTemplate.getSmscDeliveryReceipt(),
					sendWSAuthOTPWithIDRequest.getWaitDR(), serviceKey, sendWSAuthOTPWithIDRequest.getAddTimeoutMins()));
		}
		else if(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP)){

			String transactionID = origInvokeProfile.getTransactionID();
			TransactionData transactionData = appInstance.getTransactionidData().get(transactionID);

			SendWSOTPRequest sendWSAuthOTPRequest = origInvokeProfile.getSendWSOTPRequest();
			String otpMoblie = sendWSAuthOTPRequest.getOtpMobile();

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String expireTimeString = sdf.format(transactionData.getOtpExpireTime());

			String language = appInstance.getProfile().getLanguage();
			String smsBody = null;
			String serviceKey = null;

			
				serviceKey = appInstance.getMapE01dataofService().get(sendWSAuthOTPRequest.getService().toUpperCase()).getServiceKey();

				if (GssoLanguage.THAI.equals(language)) {
					smsBody = serviceTemplate.getSmsBodyThai();
				}
				else {
					smsBody = serviceTemplate.getSmsBodyEng();
				}
			

			smsBody = smsBody.replaceAll("<#SERVICE>", transactionData.getService());
			smsBody = smsBody.replaceAll("<#OTP>", transactionData.getOtp());
			smsBody = smsBody.replaceAll("<#REF>", transactionData.getRefNumber());
			smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", sendWSAuthOTPRequest.getAddTimeoutMins());
			
			String smsSender = serviceTemplate.getSmsSender();
			smsSender = smsSender.replaceAll("<#SERVICE>", transactionData.getService());
			smsSender = smsSender.replaceAll("<#OTP>", transactionData.getOtp());
			smsSender = smsSender.replaceAll("<#REF>", transactionData.getRefNumber());
			smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", sendWSAuthOTPRequest.getAddTimeoutMins());

			/* SAVE SMSCDeliveryReceipt */
			origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());

			output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(GssoDataManagement.convertStringToHex(smsBody, true), otpMoblie,
					language, smsSender, GssoDataManagement.convertStringToHex(smsBody, false), serviceTemplate.getSmscDeliveryReceipt(),
					sendWSAuthOTPRequest.getWaitDR(), serviceKey, sendWSAuthOTPRequest.getAddTimeoutMins()));
		
		}
		else if(origInvokeProfile.getGssoOTPRequest()!=null){
			GssoOTPRequest otpRequest = origInvokeProfile.getGssoOTPRequest();
			
			String transactionID = origInvokeProfile.getTransactionID();
			TransactionData transactionData = appInstance.getTransactionidData().get(transactionID);

			SendOneTimePWRequest sendOneTimePW = otpRequest.getSendOneTimePW();
			String msisdn = sendOneTimePW.getMsisdn();

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String expireTimeString = sdf.format(transactionData.getOtpExpireTime());

			String language = appInstance.getProfile().getLanguage();
			String smsBody = null;
			String serviceKey = null;

			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
				if (sendOneTimePW.getServiceKey() == null || sendOneTimePW.getServiceKey().isEmpty()) {
					serviceKey = "GS000";
				}
				else {
					serviceKey = sendOneTimePW.getServiceKey();
				}
				smsBody = serviceTemplate.getSmsBody();
			}
			else {
				serviceKey = appInstance.getMapE01dataofService().get(sendOneTimePW.getService().toUpperCase()).getServiceKey();

				if (GssoLanguage.THAI.equals(language)) {
					smsBody = serviceTemplate.getSmsBodyThai();
				}
				else {
					smsBody = serviceTemplate.getSmsBodyEng();
				}
			}

			smsBody = smsBody.replaceAll("<#SERVICE>", transactionData.getService());
			smsBody = smsBody.replaceAll("<#OTP>", transactionData.getOtp());
			smsBody = smsBody.replaceAll("<#REF>", transactionData.getRefNumber());
			smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", sendOneTimePW.getLifeTimeoutMins());

			String smsSender = serviceTemplate.getSmsSender();
			smsSender = smsSender.replaceAll("<#SERVICE>", transactionData.getService());
			smsSender = smsSender.replaceAll("<#OTP>", transactionData.getOtp());
			smsSender = smsSender.replaceAll("<#REF>", transactionData.getRefNumber());
			smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", sendOneTimePW.getLifeTimeoutMins());

			/* SAVE SMSCDeliveryReceipt */
			origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());

			output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(GssoDataManagement.convertStringToHex(smsBody, true), msisdn,
					language, smsSender, GssoDataManagement.convertStringToHex(smsBody, false), serviceTemplate.getSmscDeliveryReceipt(),
					sendOneTimePW.getWaitDR(), serviceKey, sendOneTimePW.getLifeTimeoutMins()));
		}
		else if(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_CREAT_OTP)){

			String transactionID = origInvokeProfile.getTransactionID();
			TransactionData transactionData = appInstance.getTransactionidData().get(transactionID);

			SendWSOTPRequest sendWSCreateOTPRequest = origInvokeProfile.getSendWSOTPRequest();
			String msisdn = sendWSCreateOTPRequest.getMsisdn();

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String expireTimeString = sdf.format(transactionData.getOtpExpireTime());

			String language = appInstance.getProfile().getLanguage();
			String smsBody = null;
			String serviceKey = null;

			
				serviceKey = appInstance.getMapE01dataofService().get(sendWSCreateOTPRequest.getService().toUpperCase()).getServiceKey();

				if (GssoLanguage.THAI.equals(language)) {
					smsBody = serviceTemplate.getSmsBodyThai();
				}
				else {
					smsBody = serviceTemplate.getSmsBodyEng();
				}
			

			smsBody = smsBody.replaceAll("<#SERVICE>", transactionData.getService());
			smsBody = smsBody.replaceAll("<#OTP>", transactionData.getOtp());
			smsBody = smsBody.replaceAll("<#REF>", transactionData.getRefNumber());
			smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", sendWSCreateOTPRequest.getAddTimeoutMins());
			
			String smsSender = serviceTemplate.getSmsSender();
			smsSender = smsSender.replaceAll("<#SERVICE>", transactionData.getService());
			smsSender = smsSender.replaceAll("<#OTP>", transactionData.getOtp());
			smsSender = smsSender.replaceAll("<#REF>", transactionData.getRefNumber());
			smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", sendWSCreateOTPRequest.getAddTimeoutMins());

			/* SAVE SMSCDeliveryReceipt */
			origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());

			output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(GssoDataManagement.convertStringToHex(smsBody, true), msisdn,
					language, smsSender, GssoDataManagement.convertStringToHex(smsBody, false), serviceTemplate.getSmscDeliveryReceipt(),
					sendWSCreateOTPRequest.getWaitDR() , serviceKey, sendWSCreateOTPRequest.getAddTimeoutMins()));
		
		}
		else if(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_GENERATE_OTP)){
			
			String transactionID = origInvokeProfile.getTransactionID();
			TransactionData transactionData = appInstance.getTransactionidData().get(transactionID);

			SendWSOTPRequest sendWSGenerateOTPRequest = origInvokeProfile.getSendWSOTPRequest();
			String msisdn = sendWSGenerateOTPRequest.getMsisdn();

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String expireTimeString = sdf.format(transactionData.getOtpExpireTime());

			String language = appInstance.getProfile().getLanguage();
			String smsBody = null;
			String serviceKey = null;

			
				serviceKey = appInstance.getMapE01dataofService().get(sendWSGenerateOTPRequest.getService().toUpperCase()).getServiceKey();

				if (GssoLanguage.THAI.equals(language)) {
					smsBody = serviceTemplate.getSmsBodyThai();
				}
				else {
					smsBody = serviceTemplate.getSmsBodyEng();
				}
			

			smsBody = smsBody.replaceAll("<#SERVICE>", transactionData.getService());
			smsBody = smsBody.replaceAll("<#OTP>", transactionData.getOtp());
			smsBody = smsBody.replaceAll("<#REF>", transactionData.getRefNumber());
			smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", sendWSGenerateOTPRequest.getAddTimeoutMins());
			
			String smsSender = serviceTemplate.getSmsSender();
			smsSender = smsSender.replaceAll("<#SERVICE>", transactionData.getService());
			smsSender = smsSender.replaceAll("<#OTP>", transactionData.getOtp());
			smsSender = smsSender.replaceAll("<#REF>", transactionData.getRefNumber());
			smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", sendWSGenerateOTPRequest.getAddTimeoutMins());

			/* SAVE SMSCDeliveryReceipt */
			origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());

			output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(GssoDataManagement.convertStringToHex(smsBody, true), msisdn,
					language, smsSender, GssoDataManagement.convertStringToHex(smsBody, false), serviceTemplate.getSmscDeliveryReceipt(),
					sendWSGenerateOTPRequest.getWaitDR(), serviceKey, sendWSGenerateOTPRequest.getAddTimeoutMins()));
		
		}
		ec02Instance.incrementsStat(Statistic.GSSO_SEND_SMPPGW_SUBMITSM_REQUEST.getStatistic());

		// //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			composeDebugLog.addStatisticOut(Statistic.GSSO_SEND_SMPPGW_SUBMITSM_REQUEST.getStatistic());
		}

		return output;
	}

	public static ArrayList <EquinoxRawData> createSMSReqMessageV2(final String origInvoke, final GssoServiceTemplate serviceTemplate,
													 EC02Instance ec02Instance, GssoComposeDebugLog composeDebugLog) {
		ArrayList <EquinoxRawData> rawDataArrayList = new ArrayList<EquinoxRawData>();

		EquinoxRawData output = new EquinoxRawData();
		APPInstance appInstance = ec02Instance.getAppInstance();
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);


/*
		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvoke, SubStates.W_SEND_SMS.toString());

		output = new EquinoxRawData();
		output.setName(EventName.SMPP);
		output.setCType(EventCtype.SMS);
		output.setType(EventAction.REQUEST);
		output.setInvoke(invokeOutgoing);

		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		if (serviceTemplate.getAllowSmsRoaming().equalsIgnoreCase(SentOTPResult.FALSE)) {
			output.setTo(ConfigureTool.getConfigure(ConfigName.SMPPGW_INTERFACE));

			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.SUBMIT_SM.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			origInvokeProfile.setSMPPRoaming(false);
		}
		else {
			output.setTo(ConfigureTool.getConfigure(ConfigName.SMPPGW_ROAMING_INTERFACE));

			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.SMPPGW_ROAMING.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			origInvokeProfile.setSMPPRoaming(true);
		}
*/

		if(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)){

			String transactionID = origInvokeProfile.getTransactionID();
			TransactionData transactionData = appInstance.getTransactionidData().get(transactionID);

			SendWSOTPRequest sendWSAuthOTPWithIDRequest = origInvokeProfile.getSendWSOTPRequest();
			String otpMobile = sendWSAuthOTPWithIDRequest.getOtpMobile();

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String expireTimeString = sdf.format(transactionData.getOtpExpireTime());

			String language = appInstance.getProfile().getLanguage();
			String smsBody = null;
			String serviceKey = null;


			serviceKey = appInstance.getMapE01dataofService().get(sendWSAuthOTPWithIDRequest.getService().toUpperCase()).getServiceKey();

			smsBody = selectlanguage(language,serviceTemplate.getSmsBodyThai(),serviceTemplate.getSmsBodyEng());



			smsBody = smsBody.replaceAll("<#SERVICE>", transactionData.getService());
			smsBody = smsBody.replaceAll("<#OTP>", transactionData.getOtp());
			smsBody = smsBody.replaceAll("<#REF>", transactionData.getRefNumber());
			smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", sendWSAuthOTPWithIDRequest.getAddTimeoutMins());

			String smsSender = serviceTemplate.getSmsSender();
			smsSender = smsSender.replaceAll("<#SERVICE>", transactionData.getService());
			smsSender = smsSender.replaceAll("<#OTP>", transactionData.getOtp());
			smsSender = smsSender.replaceAll("<#REF>", transactionData.getRefNumber());
			smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", sendWSAuthOTPWithIDRequest.getAddTimeoutMins());

			/* SAVE SMSCDeliveryReceipt */
			origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());
			ArrayList<String> listOfSms = splitSmsBody(smsBody, language);
			if (listOfSms.size() > 1) {
				for (String sms : listOfSms) {
					output = new EquinoxRawData();
					setRrawDataAttr(ec02Instance,origInvoke,serviceTemplate,appInstance,output,composeDebugLog);
					output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(sms, otpMobile,
							language, smsSender, serviceTemplate.getSmscDeliveryReceipt(),
							sendWSAuthOTPWithIDRequest.getWaitDR(), serviceKey, sendWSAuthOTPWithIDRequest.getAddTimeoutMins(),true));

					rawDataArrayList.add(output);
				}
			}else{
				setRrawDataAttr(ec02Instance,origInvoke,serviceTemplate,appInstance,output,composeDebugLog);
				output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(listOfSms.get(0), otpMobile,
						language, smsSender, serviceTemplate.getSmscDeliveryReceipt(),
						sendWSAuthOTPWithIDRequest.getWaitDR(), serviceKey, sendWSAuthOTPWithIDRequest.getAddTimeoutMins(),false));
				rawDataArrayList.add(output);

			}

		}
		else if(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP)){

			String transactionID = origInvokeProfile.getTransactionID();
			TransactionData transactionData = appInstance.getTransactionidData().get(transactionID);

			SendWSOTPRequest sendWSAuthOTPRequest = origInvokeProfile.getSendWSOTPRequest();
			String otpMoblie = sendWSAuthOTPRequest.getOtpMobile();

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String expireTimeString = sdf.format(transactionData.getOtpExpireTime());

			String language = appInstance.getProfile().getLanguage();
			String smsBody = null;
			String serviceKey = null;


			serviceKey = appInstance.getMapE01dataofService().get(sendWSAuthOTPRequest.getService().toUpperCase()).getServiceKey();

			smsBody = selectlanguage(language,serviceTemplate.getSmsBodyThai(),serviceTemplate.getSmsBodyEng());



			smsBody = smsBody.replaceAll("<#SERVICE>", transactionData.getService());
			smsBody = smsBody.replaceAll("<#OTP>", transactionData.getOtp());
			smsBody = smsBody.replaceAll("<#REF>", transactionData.getRefNumber());
			smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", sendWSAuthOTPRequest.getAddTimeoutMins());

			String smsSender = serviceTemplate.getSmsSender();
			smsSender = smsSender.replaceAll("<#SERVICE>", transactionData.getService());
			smsSender = smsSender.replaceAll("<#OTP>", transactionData.getOtp());
			smsSender = smsSender.replaceAll("<#REF>", transactionData.getRefNumber());
			smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", sendWSAuthOTPRequest.getAddTimeoutMins());

			/* SAVE SMSCDeliveryReceipt */
			origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());

			ArrayList<String> listOfSms = splitSmsBody(smsBody, language);
			if (listOfSms.size() > 1) {
				for (String sms : listOfSms) {
					output = new EquinoxRawData();
					setRrawDataAttr(ec02Instance,origInvoke,serviceTemplate,appInstance,output,composeDebugLog);
					output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(sms, otpMoblie,
							language, smsSender, serviceTemplate.getSmscDeliveryReceipt(),
							sendWSAuthOTPRequest.getWaitDR(), serviceKey, sendWSAuthOTPRequest.getAddTimeoutMins(),true));

					rawDataArrayList.add(output);
				}
			}else{
				setRrawDataAttr(ec02Instance,origInvoke,serviceTemplate,appInstance,output,composeDebugLog);
				output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(listOfSms.get(0), otpMoblie,
						language, smsSender, serviceTemplate.getSmscDeliveryReceipt(),
						sendWSAuthOTPRequest.getWaitDR(), serviceKey, sendWSAuthOTPRequest.getAddTimeoutMins(),false));
				rawDataArrayList.add(output);

			}

		}
		else if(origInvokeProfile.getGssoOTPRequest()!=null){
			GssoOTPRequest otpRequest = origInvokeProfile.getGssoOTPRequest();

			String transactionID = origInvokeProfile.getTransactionID();
			TransactionData transactionData = appInstance.getTransactionidData().get(transactionID);

			SendOneTimePWRequest sendOneTimePW = otpRequest.getSendOneTimePW();
			String msisdn = sendOneTimePW.getMsisdn();

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String expireTimeString = sdf.format(transactionData.getOtpExpireTime());

			String language = appInstance.getProfile().getLanguage();
			String smsBody = null;
			String serviceKey = null;

			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
				if (sendOneTimePW.getServiceKey() == null || sendOneTimePW.getServiceKey().isEmpty()) {
					serviceKey = "GS000";
				}
				else {
					serviceKey = sendOneTimePW.getServiceKey();
				}
				smsBody = serviceTemplate.getSmsBody();
			}
			else {
				serviceKey = appInstance.getMapE01dataofService().get(sendOneTimePW.getService().toUpperCase()).getServiceKey();
				smsBody = selectlanguage(language,serviceTemplate.getSmsBodyThai(),serviceTemplate.getSmsBodyEng());
			}

			smsBody = smsBody.replaceAll("<#SERVICE>", transactionData.getService());
			smsBody = smsBody.replaceAll("<#OTP>", transactionData.getOtp());
			smsBody = smsBody.replaceAll("<#REF>", transactionData.getRefNumber());
			smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", sendOneTimePW.getLifeTimeoutMins());

			String smsSender = serviceTemplate.getSmsSender();
			smsSender = smsSender.replaceAll("<#SERVICE>", transactionData.getService());
			smsSender = smsSender.replaceAll("<#OTP>", transactionData.getOtp());
			smsSender = smsSender.replaceAll("<#REF>", transactionData.getRefNumber());
			smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", sendOneTimePW.getLifeTimeoutMins());

			/* SAVE SMSCDeliveryReceipt */
			origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());


			ArrayList<String> listOfSms = splitSmsBody(smsBody, language);
			if (listOfSms.size() > 1) {
				for (String sms : listOfSms) {
					output = new EquinoxRawData();
					setRrawDataAttr(ec02Instance,origInvoke,serviceTemplate,appInstance,output,composeDebugLog);
					output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(sms, msisdn,
							language, smsSender, serviceTemplate.getSmscDeliveryReceipt(),
							sendOneTimePW.getWaitDR(), serviceKey, sendOneTimePW.getLifeTimeoutMins(),true));

					rawDataArrayList.add(output);
				}
			}else{
				setRrawDataAttr(ec02Instance,origInvoke,serviceTemplate,appInstance,output,composeDebugLog);
				output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(listOfSms.get(0), msisdn,
						language, smsSender, serviceTemplate.getSmscDeliveryReceipt(),
						sendOneTimePW.getWaitDR(), serviceKey, sendOneTimePW.getLifeTimeoutMins(),false));
				rawDataArrayList.add(output);

			}

		}
		else if(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_CREAT_OTP)){

			String transactionID = origInvokeProfile.getTransactionID();
			TransactionData transactionData = appInstance.getTransactionidData().get(transactionID);

			SendWSOTPRequest sendWSCreateOTPRequest = origInvokeProfile.getSendWSOTPRequest();
			String msisdn = sendWSCreateOTPRequest.getMsisdn();

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String expireTimeString = sdf.format(transactionData.getOtpExpireTime());

			String language = appInstance.getProfile().getLanguage();
			String smsBody = null;
			String serviceKey = null;


			serviceKey = appInstance.getMapE01dataofService().get(sendWSCreateOTPRequest.getService().toUpperCase()).getServiceKey();

			smsBody = selectlanguage(language,serviceTemplate.getSmsBodyThai(),serviceTemplate.getSmsBodyEng());



			smsBody = smsBody.replaceAll("<#SERVICE>", transactionData.getService());
			smsBody = smsBody.replaceAll("<#OTP>", transactionData.getOtp());
			smsBody = smsBody.replaceAll("<#REF>", transactionData.getRefNumber());
			smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", sendWSCreateOTPRequest.getAddTimeoutMins());

			String smsSender = serviceTemplate.getSmsSender();
			smsSender = smsSender.replaceAll("<#SERVICE>", transactionData.getService());
			smsSender = smsSender.replaceAll("<#OTP>", transactionData.getOtp());
			smsSender = smsSender.replaceAll("<#REF>", transactionData.getRefNumber());
			smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", sendWSCreateOTPRequest.getAddTimeoutMins());

			/* SAVE SMSCDeliveryReceipt */
			origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());

			ArrayList<String> listOfSms = splitSmsBody(smsBody, language);
			if (listOfSms.size() > 1) {
				for (String sms : listOfSms) {
					output = new EquinoxRawData();
					setRrawDataAttr(ec02Instance,origInvoke,serviceTemplate,appInstance,output,composeDebugLog);
					output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(sms, msisdn,
							language, smsSender, serviceTemplate.getSmscDeliveryReceipt(),
							sendWSCreateOTPRequest.getWaitDR(), serviceKey, sendWSCreateOTPRequest.getAddTimeoutMins(),true));

					rawDataArrayList.add(output);
				}
			}else{
				setRrawDataAttr(ec02Instance,origInvoke,serviceTemplate,appInstance,output,composeDebugLog);
				output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(listOfSms.get(0), msisdn,
						language, smsSender, serviceTemplate.getSmscDeliveryReceipt(),
						sendWSCreateOTPRequest.getWaitDR(), serviceKey, sendWSCreateOTPRequest.getAddTimeoutMins(),false));
				rawDataArrayList.add(output);

			}

		}
		else if(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_GENERATE_OTP)){

			String transactionID = origInvokeProfile.getTransactionID();
			TransactionData transactionData = appInstance.getTransactionidData().get(transactionID);

			SendWSOTPRequest sendWSGenerateOTPRequest = origInvokeProfile.getSendWSOTPRequest();
			String msisdn = sendWSGenerateOTPRequest.getMsisdn();

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String expireTimeString = sdf.format(transactionData.getOtpExpireTime());

			String language = appInstance.getProfile().getLanguage();
			String smsBody = null;
			String serviceKey = null;


			serviceKey = appInstance.getMapE01dataofService().get(sendWSGenerateOTPRequest.getService().toUpperCase()).getServiceKey();

			smsBody = selectlanguage(language,serviceTemplate.getSmsBodyThai(),serviceTemplate.getSmsBodyEng());



			smsBody = smsBody.replaceAll("<#SERVICE>", transactionData.getService());
			smsBody = smsBody.replaceAll("<#OTP>", transactionData.getOtp());
			smsBody = smsBody.replaceAll("<#REF>", transactionData.getRefNumber());
			smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", sendWSGenerateOTPRequest.getAddTimeoutMins());

			String smsSender = serviceTemplate.getSmsSender();
			smsSender = smsSender.replaceAll("<#SERVICE>", transactionData.getService());
			smsSender = smsSender.replaceAll("<#OTP>", transactionData.getOtp());
			smsSender = smsSender.replaceAll("<#REF>", transactionData.getRefNumber());
			smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
			smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", sendWSGenerateOTPRequest.getAddTimeoutMins());

			/* SAVE SMSCDeliveryReceipt */
			origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());

			ArrayList<String> listOfSms = splitSmsBody(smsBody, language);
			if (listOfSms.size() > 1) {
				for (String sms : listOfSms) {
					output = new EquinoxRawData();
					setRrawDataAttr(ec02Instance,origInvoke,serviceTemplate,appInstance,output,composeDebugLog);
					output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(sms, msisdn,
							language, smsSender, serviceTemplate.getSmscDeliveryReceipt(),
							sendWSGenerateOTPRequest.getWaitDR(), serviceKey, sendWSGenerateOTPRequest.getAddTimeoutMins(),true));

					rawDataArrayList.add(output);
				}
			}else{
				setRrawDataAttr(ec02Instance,origInvoke,serviceTemplate,appInstance,output,composeDebugLog);
				output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(listOfSms.get(0), msisdn,
						language, smsSender, serviceTemplate.getSmscDeliveryReceipt(),
						sendWSGenerateOTPRequest.getWaitDR(), serviceKey, sendWSGenerateOTPRequest.getAddTimeoutMins(),false));
				rawDataArrayList.add(output);

			}

		}
		return rawDataArrayList;
	}
	public static String selectlanguage (String language ,String th,String en){
		if(GssoLanguage.ALL.equalsIgnoreCase(language)){
			return  th+" "+en;
		}else if (GssoLanguage.THAI.equals(language)) {
			return  th;
		}
		return  en;

	}
	public static void setRrawDataAttr (EC02Instance ec02Instance,final String origInvoke, final GssoServiceTemplate serviceTemplate,
										APPInstance appInstance, EquinoxRawData output ,  GssoComposeDebugLog composeDebugLog){

		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvoke, SubStates.W_SEND_SMS.toString());
		output.setName(EventName.SMPP);
		output.setCType(EventCtype.SMS);
		output.setType(EventAction.REQUEST);
		output.setInvoke(invokeOutgoing);
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		if (serviceTemplate.getAllowSmsRoaming().equalsIgnoreCase(SentOTPResult.FALSE)) {
			output.setTo(ConfigureTool.getConfigure(ConfigName.SMPPGW_INTERFACE));

			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.SUBMIT_SM.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			origInvokeProfile.setSMPPRoaming(false);
		}
		else {
			output.setTo(ConfigureTool.getConfigure(ConfigName.SMPPGW_ROAMING_INTERFACE));

			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.SMPPGW_ROAMING.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			origInvokeProfile.setSMPPRoaming(true);
		}

		ec02Instance.incrementsStat(Statistic.GSSO_SEND_SMPPGW_SUBMITSM_REQUEST.getStatistic());

		// //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			composeDebugLog.addStatisticOut(Statistic.GSSO_SEND_SMPPGW_SUBMITSM_REQUEST.getStatistic());
		}

	}

	public static EquinoxRawData createEMAILReqMessage(final String origInvoke, final GssoServiceTemplate serviceTemplate,
			EC02Instance ec02Instance, GssoComposeDebugLog composeDebugLog) {

		EquinoxRawData output = null;
		APPInstance appInstance = ec02Instance.getAppInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvoke, SubStates.W_SEND_EMAIL.toString());

		output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.PLAIN);
		output.setType(EventAction.REQUEST);
		output.setTo(ConfigureTool.getConfigure(ConfigName.MAILSERVER_INTERFACE));
		output.setInvoke(invokeOutgoing);

		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvoke);

		
		if(origProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)){
			SendWSOTPRequest sendWSOTPRequest = origProfile.getSendWSOTPRequest();
			
			String transactionID = origProfile.getTransactionID();
			TransactionData confirmOTP = appInstance.getTransactionidData().get(transactionID);

			String emailURIName = ConfigureTool.getConfigure(ConfigName.EMAIL_URINAME);
			String emailTo = origProfile.getSendWSOTPRequest().getEmail();
			
			String expireTimeString = sdf.format(confirmOTP.getOtpExpireTime());
			String emailContent = serviceTemplate.getEmailBody();
			String emailSubject = serviceTemplate.getEmailSubject();

			emailContent = emailContent.replaceAll("<#SERVICE>", confirmOTP.getService());
			if(sendWSOTPRequest.getLink()!=null){
				emailContent = emailContent.replaceAll("<#LINK>", sendWSOTPRequest.getLink());
			}
			emailContent = emailContent.replaceAll("<#OTP>", confirmOTP.getOtp());
			emailContent = emailContent.replaceAll("<#REF>", confirmOTP.getRefNumber());
			emailContent = emailContent.replaceAll("<#EXPIRETIME>", expireTimeString);
			
			emailContent = emailContent.replaceAll("<#LIFETIMEOUT>",sendWSOTPRequest.getAddTimeoutMins());

			
			emailSubject = emailSubject.replaceAll("<#SERVICE>", confirmOTP.getService());
			if(sendWSOTPRequest.getLink()!=null){
				emailSubject = emailSubject.replaceAll("<#LINK>", sendWSOTPRequest.getLink());
			}
			emailSubject = emailSubject.replaceAll("<#OTP>", confirmOTP.getOtp());
			emailSubject = emailSubject.replaceAll("<#REF>", confirmOTP.getRefNumber());
			emailSubject = emailSubject.replaceAll("<#EXPIRETIME>", expireTimeString);
			
			
			emailSubject = emailSubject.replaceAll("<#LIFETIMEOUT>", sendWSOTPRequest.getAddTimeoutMins());

			
			String json = "{" + "\"host\":\"" + emailURIName + "\"," + "\"from\": \"" + serviceTemplate.getEmailFrom() + "\","
					+ "\"recipients\": [\"" + emailTo + "\"]," + "\"subject\": \"" + emailSubject + "\"," + "\"content\": \""
					+ emailContent + "\"" + "}";

			output.addRawDataAttribute(EquinoxAttribute.VAL, json);
			
		}
		else if(origProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP)){
			SendWSOTPRequest sendWSOTPRequest = origProfile.getSendWSOTPRequest();
			
			String transactionID = origProfile.getTransactionID();
			TransactionData confirmOTP = appInstance.getTransactionidData().get(transactionID);

			String emailURIName = ConfigureTool.getConfigure(ConfigName.EMAIL_URINAME);
			String emailTo = origProfile.getSendWSOTPRequest().getEmail();
			
			String expireTimeString = sdf.format(confirmOTP.getOtpExpireTime());
			String emailContent = serviceTemplate.getEmailBody();
			String emailSubject = serviceTemplate.getEmailSubject();

			emailContent = emailContent.replaceAll("<#SERVICE>", confirmOTP.getService());
			if(sendWSOTPRequest.getLink()!=null){
				emailContent = emailContent.replaceAll("<#LINK>", sendWSOTPRequest.getLink());
			}
			emailContent = emailContent.replaceAll("<#OTP>", confirmOTP.getOtp());
			emailContent = emailContent.replaceAll("<#REF>", confirmOTP.getRefNumber());
			emailContent = emailContent.replaceAll("<#EXPIRETIME>", expireTimeString);
			
			
			emailContent = emailContent.replaceAll("<#LIFETIMEOUT>",sendWSOTPRequest.getAddTimeoutMins());

			
			emailSubject = emailSubject.replaceAll("<#SERVICE>", confirmOTP.getService());
			if(sendWSOTPRequest.getLink()!=null){
				emailSubject = emailSubject.replaceAll("<#LINK>", sendWSOTPRequest.getLink());
			}
			emailSubject = emailSubject.replaceAll("<#OTP>", confirmOTP.getOtp());
			emailSubject = emailSubject.replaceAll("<#REF>", confirmOTP.getRefNumber());
			emailSubject = emailSubject.replaceAll("<#EXPIRETIME>", expireTimeString);
			
			
			emailSubject = emailSubject.replaceAll("<#LIFETIMEOUT>", sendWSOTPRequest.getAddTimeoutMins());

			
			String json = "{" + "\"host\":\"" + emailURIName + "\"," + "\"from\": \"" + serviceTemplate.getEmailFrom() + "\","
					+ "\"recipients\": [\"" + emailTo + "\"]," + "\"subject\": \"" + emailSubject + "\"," + "\"content\": \""
					+ emailContent + "\"" + "}";

			output.addRawDataAttribute(EquinoxAttribute.VAL, json);
		}
		else if(origProfile.getGssoOTPRequest()!=null){
			GssoOTPRequest otpReq = origProfile.getGssoOTPRequest();
			
			String transactionID = origProfile.getTransactionID();
			TransactionData confirmOTP = appInstance.getTransactionidData().get(transactionID);

			String emailURIName = ConfigureTool.getConfigure(ConfigName.EMAIL_URINAME);
			String emailTo = origProfile.getGssoOTPRequest().getSendOneTimePW().getEmailAddr();
			
			String expireTimeString = sdf.format(confirmOTP.getOtpExpireTime());
			
			String emailContent = serviceTemplate.getEmailBody();
			String emailSubject = serviceTemplate.getEmailSubject();

			emailContent = emailContent.replaceAll("<#SERVICE>", confirmOTP.getService());
			emailContent = emailContent.replaceAll("<#OTP>", confirmOTP.getOtp());
			emailContent = emailContent.replaceAll("<#REF>", confirmOTP.getRefNumber());
			emailContent = emailContent.replaceAll("<#EXPIRETIME>", expireTimeString);
			
			
			emailContent = emailContent.replaceAll("<#LIFETIMEOUT>", otpReq.getSendOneTimePW().getLifeTimeoutMins());

			
			emailSubject = emailSubject.replaceAll("<#SERVICE>", confirmOTP.getService());
			emailSubject = emailSubject.replaceAll("<#OTP>", confirmOTP.getOtp());
			emailSubject = emailSubject.replaceAll("<#REF>", confirmOTP.getRefNumber());
			emailSubject = emailSubject.replaceAll("<#EXPIRETIME>", expireTimeString);
			
			
			emailSubject = emailSubject.replaceAll("<#LIFETIMEOUT>", otpReq.getSendOneTimePW().getLifeTimeoutMins());

			
			String json = "{" + "\"host\":\"" + emailURIName + "\"," + "\"from\": \"" + serviceTemplate.getEmailFrom() + "\","
					+ "\"recipients\": [\"" + emailTo + "\"]," + "\"subject\": \"" + emailSubject + "\"," + "\"content\": \""
					+ emailContent + "\"" + "}";

			output.addRawDataAttribute(EquinoxAttribute.VAL, json);
		}
		
		

		ec02Instance.incrementsStat(Statistic.GSSO_SEND_MAILSERVER_SENDEMAIL_REQUEST.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.SEND_EMAIL.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			composeDebugLog.addStatisticOut(Statistic.GSSO_SEND_MAILSERVER_SENDEMAIL_REQUEST.getStatistic());
		}

		return output;
	}

	public static EquinoxRawData createEMAILReqMessageForRetry(final String origInvoke, APPInstance appInstance) {
		EquinoxRawData output = null;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvoke, SubStates.W_SEND_EMAIL.name());
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		GssoOTPRequest otpRequest = null;
		SendWSOTPRequest sendWSOTPRequest = null;
		if(origInvokeProfile.getGssoOTPRequest()!=null){
			otpRequest = origInvokeProfile.getGssoOTPRequest();
		}
		else{
			sendWSOTPRequest = origInvokeProfile.getSendWSOTPRequest();
		}
		
		GssoServiceTemplate serviceTemplate;

		if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
			serviceTemplate = origInvokeProfile.getGssoServiceTemplate();
		}
		else {
			
			/** FIND SERVICE TEMPLATE **/
			if(sendWSOTPRequest!=null){
				serviceTemplate = GssoDataManagement.findServiceTemplateMatchAccountType(appInstance, sendWSOTPRequest.getService()
						, appInstance.getProfile().getOper());
			}
			else{
				serviceTemplate = GssoDataManagement.findServiceTemplateMatchAccountType(appInstance, otpRequest.getSendOneTimePW()
						.getService(), appInstance.getProfile().getOper());
			}
			
		}

		output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.PLAIN);
		output.setType(EventAction.REQUEST);
		output.setTo(ConfigureTool.getConfigure(ConfigName.MAILSERVER_INTERFACE));
		output.setInvoke(invokeOutgoing);

		OrigInvokeProfile origProfile = origInvokeProfile;
		SendOneTimePWRequest otpReq =null;
		SendWSOTPRequest wsOTPRequest = null;
		String lifeTimeoutMins = "";
		String destinationEmail = "" ;
		if(origInvokeProfile.getGssoOTPRequest()!=null){
			otpReq = origProfile.getGssoOTPRequest().getSendOneTimePW();
			lifeTimeoutMins = otpReq.getLifeTimeoutMins();
			destinationEmail = otpReq.getEmailAddr();
		}
		else{
			wsOTPRequest = origProfile.getSendWSOTPRequest();
			lifeTimeoutMins = wsOTPRequest.getAddTimeoutMins();
			destinationEmail = wsOTPRequest.getEmail();
		}
		
		String transactionID = origProfile.getTransactionID();
		TransactionData confirmOTP = appInstance.getTransactionidData().get(transactionID);

		String emailURIName = ConfigureTool.getConfigure(ConfigName.EMAIL_URINAME);
		String emailTo = destinationEmail;
		String expireTimeString = sdf.format(confirmOTP.getOtpExpireTime());
		String emailContent = serviceTemplate.getEmailBody();
		String emailSubject = serviceTemplate.getEmailSubject();

		emailContent = emailContent.replaceAll("<#SERVICE>", confirmOTP.getService());
		emailContent = emailContent.replaceAll("<#OTP>", confirmOTP.getOtp());
		emailContent = emailContent.replaceAll("<#REF>", confirmOTP.getRefNumber());
		emailContent = emailContent.replaceAll("<#EXPIRETIME>", expireTimeString);
		emailContent = emailContent.replaceAll("<#LIFETIMEOUT>", lifeTimeoutMins);

		emailSubject = emailSubject.replaceAll("<#SERVICE>", confirmOTP.getService());
		emailSubject = emailSubject.replaceAll("<#OTP>", confirmOTP.getOtp());
		emailSubject = emailSubject.replaceAll("<#REF>", confirmOTP.getRefNumber());
		emailSubject = emailSubject.replaceAll("<#EXPIRETIME>", expireTimeString);
		emailSubject = emailSubject.replaceAll("<#LIFETIMEOUT>", lifeTimeoutMins);

		if(sendWSOTPRequest!=null && sendWSOTPRequest.getLink()!=null){
			emailContent = emailContent.replaceAll("<#LINK>", sendWSOTPRequest.getLink());
			emailSubject = emailSubject.replaceAll("<#LINK>", sendWSOTPRequest.getLink());
		}
		
		String json = "{" + "\"host\":\"" + emailURIName + "\"," + "\"from\": \"" + serviceTemplate.getEmailFrom() + "\","
				+ "\"recipients\": [\"" + emailTo + "\"]," + "\"subject\": \"" + emailSubject + "\"," + "\"content\": \""
				+ emailContent + "\"" + "}";

		output.addRawDataAttribute(EquinoxAttribute.VAL, json);

		return output;
	}

	public static EquinoxRawData createSMSReqMessageForRetry(final String origInvoke, APPInstance appInstance) {
		EquinoxRawData output = null;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvoke, SubStates.W_SEND_SMS.name());
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		SendOneTimePWRequest sendOneTimePW = null;
		SendWSOTPRequest sendWSOTPRequest = null;
		String waitDR = null;
		
		if(origInvokeProfile.getSendWSOTPRequest()!=null){
			sendWSOTPRequest = origInvokeProfile.getSendWSOTPRequest();
			waitDR = sendWSOTPRequest.getWaitDR();
		}
		else{
			sendOneTimePW = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW();
			waitDR = sendOneTimePW.getWaitDR();
		}
		
		GssoServiceTemplate serviceTemplate;
		
		String serviceKey = null;
		if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
			serviceKey = sendOneTimePW.getServiceKey();

			if (sendOneTimePW.getServiceKey() == null || sendOneTimePW.getServiceKey().isEmpty()) {
				serviceKey = "GS000";
			}
			else {
				serviceKey = sendOneTimePW.getServiceKey();
			}
			
			serviceTemplate = origInvokeProfile.getGssoServiceTemplate();
			
		}
		else {
			if(origInvokeProfile.getSendWSOTPRequest()!=null){
				serviceKey = appInstance.getMapE01dataofService().get(sendWSOTPRequest.getService().toUpperCase()).getServiceKey();

				/** FIND SERVICE TEMPLATE **/
				serviceTemplate = GssoDataManagement.findServiceTemplateMatchAccountType(appInstance, sendWSOTPRequest.getService(),
						appInstance.getProfile().getOper());
			}
			else{
				serviceKey = appInstance.getMapE01dataofService().get(sendOneTimePW.getService().toUpperCase()).getServiceKey();

				/** FIND SERVICE TEMPLATE **/
				serviceTemplate = GssoDataManagement.findServiceTemplateMatchAccountType(appInstance, sendOneTimePW.getService(),
						appInstance.getProfile().getOper());
			}
			
		}
		
		output = new EquinoxRawData();
		output.setName(EventName.HTTP);
		output.setCType(EventCtype.XML);
		output.setType(EventAction.REQUEST);
		if (serviceTemplate.getAllowSmsRoaming().equalsIgnoreCase(SentOTPResult.FALSE)) {
			output.setTo(ConfigureTool.getConfigure(ConfigName.SMPPGW_INTERFACE));
		}
		else {
			output.setTo(ConfigureTool.getConfigure(ConfigName.SMPPGW_ROAMING_INTERFACE));
		}
		output.setInvoke(invokeOutgoing);
		
		SendOneTimePWRequest otpReq = null;
		SendWSOTPRequest wsOTPRequest = null;
		String lifeTimeoutMins = "";
		String destinationMsisdn = "" ;
		if(origInvokeProfile.getSendWSOTPRequest()!=null){
			wsOTPRequest = origInvokeProfile.getSendWSOTPRequest();
			lifeTimeoutMins = wsOTPRequest.getAddTimeoutMins();
			destinationMsisdn = wsOTPRequest.getMsisdn();
		}
		else{
			 otpReq = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW();
			 lifeTimeoutMins = otpReq.getLifeTimeoutMins();
			 destinationMsisdn = otpReq.getMsisdn();
		}
		
		
		String transactionID = origInvokeProfile.getTransactionID();
		TransactionData confirmOTP = appInstance.getTransactionidData().get(transactionID);

		String msisdn = destinationMsisdn;
		String expireTimeString = sdf.format(confirmOTP.getOtpExpireTime());

		String language = appInstance.getProfile().getLanguage();

		String smsBody = null;

		if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {

			smsBody = serviceTemplate.getSmsBody();

		}
		else {

			if (language.equals(GssoLanguage.THAI)) {
				smsBody = serviceTemplate.getSmsBodyThai();
			}
			else {
				smsBody = serviceTemplate.getSmsBodyEng();
			}
		}
		
		
		smsBody = smsBody.replaceAll("<#SERVICE>", confirmOTP.getService());
		smsBody = smsBody.replaceAll("<#OTP>", confirmOTP.getOtp());
		smsBody = smsBody.replaceAll("<#REF>", confirmOTP.getRefNumber());
		smsBody = smsBody.replaceAll("<#EXPIRETIME>", expireTimeString);
		smsBody = smsBody.replaceAll("<#LIFETIMEOUT>", lifeTimeoutMins);

		String smsSender = serviceTemplate.getSmsSender();
		smsSender = smsSender.replaceAll("<#SERVICE>", confirmOTP.getService());
		smsSender = smsSender.replaceAll("<#OTP>", confirmOTP.getOtp());
		smsSender = smsSender.replaceAll("<#REF>", confirmOTP.getRefNumber());
		smsSender = smsSender.replaceAll("<#EXPIRETIME>", expireTimeString);
		smsSender = smsSender.replaceAll("<#LIFETIMEOUT>", lifeTimeoutMins);

		/* SAVE SMSCDeliveryReceipt */
		origInvokeProfile.setRealSMSCDeliveryReceipt(serviceTemplate.getSmscDeliveryReceipt());



		ArrayList<String> listOfSms = splitSmsBody(smsBody,language);

		if(listOfSms.size()>1){
			for (String sms : listOfSms){

			}
		}

		output.setRawMessage(GssoConstructMessage.createSMSBodyMessage(GssoDataManagement.convertStringToHex(smsBody, true), msisdn,
				language, smsSender, GssoDataManagement.convertStringToHex(smsBody, false), serviceTemplate.getSmscDeliveryReceipt(),
				waitDR, serviceKey, lifeTimeoutMins));

		return output;
	}

	public static ArrayList<String> splitSmsBody(String smsBody,String language){
		ArrayList<String> listOfSms = new ArrayList<String>();
		Log.d("smsBody before Hex:"+smsBody);
		if (language.equals(GssoLanguage.THAI) ||language.equals(GssoLanguage.ALL) ) {
			smsBody = GssoDataManagement.convertStringToHexNotPrefix(smsBody, true);
		}
		else {
			smsBody = GssoDataManagement.convertStringToHexNotPrefix(smsBody, false);
		}
		Log.d("smsBody after Hex:"+smsBody);
		Log.d("Real smsBody Length after Hex:"+smsBody.length());


		int maxSms = ConfigureTool.getConfigureInteger(ConfigName.MAX_SMS_BODY)*2 <1 ?280:ConfigureTool.getConfigureInteger(ConfigName.MAX_SMS_BODY)*2;

		if(smsBody.length()>maxSms) {

			int maxSmsWithoutPrefix = maxSms-12;

			// random String 2 digit
			String AA = RandomStringUtils.random(2, "ABCDEF");

			// cal sms
			int BB = smsBody.length() / maxSmsWithoutPrefix + (smsBody.length() % maxSmsWithoutPrefix > 1 ? 1 : 0);
			int CC = 1;

			String prefix = "0x,050003" + AA + (BB>9? BB : "0" +BB) ;//Ex BB=9>09 ,BB =11>>11

			while (smsBody.length() > maxSmsWithoutPrefix) {
				String splitBody = smsBody.substring(0, maxSmsWithoutPrefix);
				listOfSms.add(prefix  +(CC>9? CC : "0" +CC) + "" +splitBody);
				smsBody = smsBody.substring(maxSmsWithoutPrefix);
				CC++;

			}
			listOfSms.add(prefix  + "0" + CC + ""+ smsBody);


		}else {
			listOfSms.add("0x," + smsBody);
		}
		return listOfSms;

	}
	public static String createSMSBodyMessage(final String messageHex, final String msisdn, final String language,
											  final String smsSender, final String smsCDeliveryReceiptE01, final String realWaitDR,
											  final String serviceKey, final String lifeTimeoutMins ,boolean isLong) {

		StringBuilder smsBody = new StringBuilder();
		boolean isFoundEC02MobileFormat = false;

		String sourceAddrTon = null;
		String sourceAddrNpi = null;
		String sourceAddr = null;

		String destAddrTon = null;
		String destAddrNpi = null;

		String messagingMode = null;
		String messageType = null;
		String gsmNetworkSpecificFeatures = null;

		String protocolId = null;
		String priorityFlag = null;
		String scheduleDeliveryTime = null;
		String validityPeriod = null;

		String smsCDeliveryReceipt = null;
		String smsCDeliveryReceiptToSMPP = null;
		String waitDR = null;
		String smeOriginatedAck = null;
		String intermediateNotification = null;

		String replaceIfPresentFlag = null;
		String dataCoding = null;
		String smDefaultMsgId = null;

		/************ MobileFormat **************/
		sourceAddrTon = chooseSourceAddrTon(smsSender, isFoundEC02MobileFormat);

		sourceAddrNpi = chooseSourceAddrNpi(smsSender, isFoundEC02MobileFormat);

		/************ MobileFormat **************/
		sourceAddr = smsSender;
		destAddrTon = SourceAddrTon.INTERNATIONAL;
		destAddrNpi = SourceAddrNpi.ISDN;

		messagingMode = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_MESSAGING_MODE);
		messageType = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_MESSAGE_TYPE);
		gsmNetworkSpecificFeatures = isLong? ConfigureTool.getConfigureSMPP(ConfigName.LONG_SMPP_GSMNETWORKSPECIFICFEATURES):ConfigureTool.getConfigureSMPP(ConfigName.SMPP_GSM_NETWORK_SPECIFIC_FEATURES);

		protocolId = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_PROTOCOL_ID);
		priorityFlag = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_PRIORITY_FLAG);
		scheduleDeliveryTime = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_SCHEDULE_DELIVERY_TIME);

		// replace with Choose default value		getLifeTimeoutMins
//		validityPeriod = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_VALIDITY_PERIOD);
//		validityPeriod = lifeTimeoutMins;
		long lifeTime = TimeUnit.MINUTES.toMillis(Integer.parseInt(lifeTimeoutMins));
		long currentime = System.currentTimeMillis()+lifeTime;
		String pattern = "yyMMddHHmmss028+";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(currentime);
		validityPeriod = date;

		/************ smsCDeliveryReceipt **************/
		if (smsCDeliveryReceiptE01 == null) {
			smsCDeliveryReceipt = BooleanString.FALSE;
		}
		else {
			smsCDeliveryReceipt = smsCDeliveryReceiptE01;
		}
		if (realWaitDR == null) {
			waitDR = BooleanString.FALSE;
		}
		else {
			waitDR = realWaitDR;
		}

		if (smsCDeliveryReceipt.equalsIgnoreCase(BooleanString.TRUE) && waitDR.equalsIgnoreCase(BooleanString.TRUE)) {
			smsCDeliveryReceiptToSMPP = "SuccessOrFailure";
		}
		else if (smsCDeliveryReceipt.equalsIgnoreCase(BooleanString.TRUE) || waitDR.equalsIgnoreCase(BooleanString.TRUE)) {
			smsCDeliveryReceiptToSMPP = "SuccessOrFailure";
		}
		else {
			smsCDeliveryReceiptToSMPP = "No";
		}
		/************ smsCDeliveryReceipt **************/

		smeOriginatedAck = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_SME_ORIGINATED_ACK);
		intermediateNotification = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_INTERMEDIATE_NOTIFICATION);
		replaceIfPresentFlag = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_REPLACE_IF_PRESENT_FLAG);

		if (language.equals(GssoLanguage.THAI) || language.equalsIgnoreCase(GssoLanguage.ALL)) {
			dataCoding = "UCS2";
		}
		else {
			dataCoding = "SMSCDefault";
		}

		smDefaultMsgId = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_SM_DEFAULT_MSG_ID);

		smsBody.append("<mandatory>");
		smsBody.append("<service_type value=\"" + serviceKey + "\"/>");
		smsBody.append("<source_addr_ton value=\"" + sourceAddrTon + "\"/>");
		smsBody.append("<source_addr_npi value=\"" + sourceAddrNpi + "\"/>");
		smsBody.append("<source_addr value=\"" + sourceAddr + "\"/>");
		smsBody.append("<dest_addr_ton value=\"" + destAddrTon + "\"/>");
		smsBody.append("<dest_addr_npi value=\"" + destAddrNpi + "\"/>");
		smsBody.append("<destination_addr value=\"" + msisdn + "\"/>");
		smsBody.append("<esm_class>");
		smsBody.append("<MessagingMode value=\"" + messagingMode + "\"/>");
		smsBody.append("<MessageType value=\"" + messageType + "\"/>");
		smsBody.append("<GSMNetworkSpecificFeatures value=\"" + gsmNetworkSpecificFeatures + "\"/>");
		smsBody.append("</esm_class>");
		smsBody.append("<protocol_id value=\"" + protocolId + "\"/>");
		smsBody.append("<priority_flag value=\"" + priorityFlag + "\"/>");
		smsBody.append("<schedule_delivery_time value=\"" + scheduleDeliveryTime + "\"/>");
		smsBody.append("<validity_period value=\"" + validityPeriod + "\"/>");
		smsBody.append("<registered_delivery>");
		smsBody.append("<SMSCDeliveryReceipt value=\"" + smsCDeliveryReceiptToSMPP + "\"/>");
		smsBody.append("<SMEOriginatedAck value=\"" + smeOriginatedAck + "\"/>");
		smsBody.append("<IntermediateNotification value=\"" + intermediateNotification + "\"/>");
		smsBody.append("</registered_delivery>");
		smsBody.append("<replace_if_present_flag value=\"" + replaceIfPresentFlag + "\"/>");
		smsBody.append("<data_coding value=\"" + dataCoding + "\"/>");
		smsBody.append("<sm_default_msg_id value=\"" + smDefaultMsgId + "\"/>");
		smsBody.append("<sm_length value=\"" + messageHex.split(",")[messageHex.split(",").length>1?1:0].length()/2 + "\"/>");
		smsBody.append("<short_message value=\"" + messageHex.replace(",","") + "\"/>");
		smsBody.append("</mandatory>");

		return smsBody.toString();
	}


	public static String createSMSBodyMessage(final String messageHex, final String msisdn, final String language,
			final String smsSender, final String messageOct, final String smsCDeliveryReceiptE01, final String realWaitDR,
			final String serviceKey, final String lifeTimeoutMins) {

		StringBuilder smsBody = new StringBuilder();
		boolean isFoundEC02MobileFormat = false;

		String sourceAddrTon = null;
		String sourceAddrNpi = null;
		String sourceAddr = null;

		String destAddrTon = null;
		String destAddrNpi = null;

		String messagingMode = null;
		String messageType = null;
		String gsmNetworkSpecificFeatures = null;

		String protocolId = null;
		String priorityFlag = null;
		String scheduleDeliveryTime = null;
		String validityPeriod = null;

		String smsCDeliveryReceipt = null;
		String smsCDeliveryReceiptToSMPP = null;
		String waitDR = null;
		String smeOriginatedAck = null;
		String intermediateNotification = null;

		String replaceIfPresentFlag = null;
		String dataCoding = null;
		String smDefaultMsgId = null;

		/************ MobileFormat **************/
		sourceAddrTon = chooseSourceAddrTon(smsSender, isFoundEC02MobileFormat);

		sourceAddrNpi = chooseSourceAddrNpi(smsSender, isFoundEC02MobileFormat);

		/************ MobileFormat **************/
		sourceAddr = smsSender;
		destAddrTon = SourceAddrTon.INTERNATIONAL;
		destAddrNpi = SourceAddrNpi.ISDN;

		messagingMode = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_MESSAGING_MODE);
		messageType = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_MESSAGE_TYPE);
		gsmNetworkSpecificFeatures = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_GSM_NETWORK_SPECIFIC_FEATURES);

		protocolId = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_PROTOCOL_ID);
		priorityFlag = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_PRIORITY_FLAG);
		scheduleDeliveryTime = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_SCHEDULE_DELIVERY_TIME);

		// replace with Choose default value		getLifeTimeoutMins 
//		validityPeriod = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_VALIDITY_PERIOD);
//		validityPeriod = lifeTimeoutMins;
		long lifeTime = TimeUnit.MINUTES.toMillis(Integer.parseInt(lifeTimeoutMins));
		long currentime = System.currentTimeMillis()+lifeTime;
		String pattern = "yyMMddHHmmss028+";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(currentime);
		validityPeriod = date;
		
		/************ smsCDeliveryReceipt **************/
		if (smsCDeliveryReceiptE01 == null) {
			smsCDeliveryReceipt = BooleanString.FALSE;
		}
		else {
			smsCDeliveryReceipt = smsCDeliveryReceiptE01;
		}
		if (realWaitDR == null) {
			waitDR = BooleanString.FALSE;
		}
		else {
			waitDR = realWaitDR;
		}

		if (smsCDeliveryReceipt.toUpperCase().equals(BooleanString.TRUE) && waitDR.toUpperCase().equals(BooleanString.TRUE)) {
			smsCDeliveryReceiptToSMPP = "SuccessOrFailure";
		}
		else if (smsCDeliveryReceipt.toUpperCase().equals(BooleanString.TRUE) || waitDR.toUpperCase().equals(BooleanString.TRUE)) {
			smsCDeliveryReceiptToSMPP = "SuccessOrFailure";
		}
		else {
			smsCDeliveryReceiptToSMPP = "No";
		}
		/************ smsCDeliveryReceipt **************/

		smeOriginatedAck = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_SME_ORIGINATED_ACK);
		intermediateNotification = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_INTERMEDIATE_NOTIFICATION);
		replaceIfPresentFlag = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_REPLACE_IF_PRESENT_FLAG);

		if (language.equals(GssoLanguage.THAI)) {
			dataCoding = "UCS2";
		}
		else {
			dataCoding = "SMSCDefault";
		}

		smDefaultMsgId = ConfigureTool.getConfigureSMPP(ConfigName.SMPP_SM_DEFAULT_MSG_ID);

		smsBody.append("<mandatory>");
		smsBody.append("<service_type value=\"" + serviceKey + "\"/>");
		smsBody.append("<source_addr_ton value=\"" + sourceAddrTon + "\"/>");
		smsBody.append("<source_addr_npi value=\"" + sourceAddrNpi + "\"/>");
		smsBody.append("<source_addr value=\"" + sourceAddr + "\"/>");
		smsBody.append("<dest_addr_ton value=\"" + destAddrTon + "\"/>");
		smsBody.append("<dest_addr_npi value=\"" + destAddrNpi + "\"/>");
		smsBody.append("<destination_addr value=\"" + msisdn + "\"/>");
		smsBody.append("<esm_class>");
		smsBody.append("<MessagingMode value=\"" + messagingMode + "\"/>");
		smsBody.append("<MessageType value=\"" + messageType + "\"/>");
		smsBody.append("<GSMNetworkSpecificFeatures value=\"" + gsmNetworkSpecificFeatures + "\"/>");
		smsBody.append("</esm_class>");
		smsBody.append("<protocol_id value=\"" + protocolId + "\"/>");
		smsBody.append("<priority_flag value=\"" + priorityFlag + "\"/>");
		smsBody.append("<schedule_delivery_time value=\"" + scheduleDeliveryTime + "\"/>");
		smsBody.append("<validity_period value=\"" + validityPeriod + "\"/>");
		smsBody.append("<registered_delivery>");
		smsBody.append("<SMSCDeliveryReceipt value=\"" + smsCDeliveryReceiptToSMPP + "\"/>");
		smsBody.append("<SMEOriginatedAck value=\"" + smeOriginatedAck + "\"/>");
		smsBody.append("<IntermediateNotification value=\"" + intermediateNotification + "\"/>");
		smsBody.append("</registered_delivery>");
		smsBody.append("<replace_if_present_flag value=\"" + replaceIfPresentFlag + "\"/>");
		smsBody.append("<data_coding value=\"" + dataCoding + "\"/>");
		smsBody.append("<sm_default_msg_id value=\"" + smDefaultMsgId + "\"/>");

		if (language.equals(GssoLanguage.THAI)) {
			DecimalFormat dfRealNumber = new DecimalFormat("##");
			String messageLength = dfRealNumber.format(((messageHex.length() - 2) / 2));

			if (Integer.parseInt(messageLength) > 254) {
				smsBody.append("<sm_length value=\"zero\"/>");
				smsBody.append("<message_payload value=\"" + messageHex + "\"/>");
			}
			else {
				smsBody.append("<sm_length value=\"" + messageLength + "\"/>");
				smsBody.append("<short_message value=\"" + messageHex + "\"/>");
			}
		}
		else {
			DecimalFormat dfRealNumber = new DecimalFormat("##");
			String messageLength = dfRealNumber.format((((messageOct.length()) - 2) / 2));

			if (Integer.parseInt(messageLength) > 254) {
				smsBody.append("<sm_length value=\"zero\"/>");
				smsBody.append("<message_payload value=\"" + messageOct + "\"/>");
			}
			else {
				smsBody.append("<sm_length value=\"" + messageLength + "\"/>");
				smsBody.append("<short_message value=\"" + messageOct + "\"/>");
			}
		}
		smsBody.append("</mandatory>");

		return smsBody.toString();
	}

	public static void createMessageQuiryE01Template(EC02Instance ec02Instance, final String origInvoke, AbstractAF abstractAF,
			GssoComposeDebugLog composeDebugLog) {

		APPInstance appInstance = ec02Instance.getAppInstance();
		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvoke);
		String serviceName = null;
		
		if(origProfile.getSendWSOTPRequest()!=null){
			serviceName = origProfile.getSendWSOTPRequest().getService();
		}
		else if(origProfile.getGssoOTPRequest()!=null){
			serviceName = origProfile.getGssoOTPRequest().getSendOneTimePW().getService();
		}
		
		HashMap<String, ArrayList<String>> maplistWQuiryService = appInstance.getMaplistWQuiryService();
		if (!maplistWQuiryService.containsKey(serviceName)) {

			String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvoke, SubStates.W_SERVICE_TEMPLATE.name());
			GlobalData globalData = abstractAF.getEquinoxUtils().getGlobalData();

			KeyObject keyobj = new KeyObject();
			keyobj.setObjectType(ConfigureTool.getConfigure(ConfigName.E01_OBJECT_TYPE));
			keyobj.setKey0("0");
			keyobj.setKey1(serviceName);
			keyobj.setKey2("def");
			keyobj.setKey3("def");
			keyobj.setKey4("def");

			globalData.setTransactionId(invokeOutgoing);
			globalData.search(keyobj, invokeOutgoing);

			/** Set MaplistWQuiryService **/
			if (!appInstance.getListInvokeProcessing().contains(origInvoke)) {
				appInstance.getListInvokeProcessing().add(origInvoke);
			}

			maplistWQuiryService.put(serviceName, new ArrayList<String>());
			maplistWQuiryService.get(serviceName).add(origInvoke);

			/** GSSO Send E01 QueryServiceTemplate Request STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_SEND_E01_QUERYSERVICETEMPLATE_REQUEST.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.QUERY_SERVICE_TEMPLATE.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.addStatisticOut(Statistic.GSSO_SEND_E01_QUERYSERVICETEMPLATE_REQUEST.getStatistic());
			}
		}
		else {
			/** Set MaplistWQuiryService **/
			maplistWQuiryService.get(serviceName).add(origInvoke);

		}
	}
	

	public static EquinoxRawData createReturnErrorMessageServiceTemplate(final String origInvoke, EC02Instance ec02Instance,
			final JsonResultCode jsonCode, GssoComposeDebugLog composeDebugLog, GssoComposeSummaryLog composeSummary) {

		EquinoxRawData output = null;
		APPInstance appInstance = ec02Instance.getAppInstance();
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		EquinoxRawData origRawDataIncoming = origInvokeProfile.getOrigEquinoxRawData();
		boolean isWS= false;
		String messageType = null;
		
		if(origInvokeProfile.getSendWSOTPRequest()!=null){
			messageType = origInvokeProfile.getMessageType();
			isWS = true;
		}
		else{
			messageType = origInvokeProfile.getGssoOTPRequest().getMessageType();
		}
		
		String orderRef = GssoGenerator.generateOrderReference(ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME),
				appInstance.getListOrderReference());
		String resCode = "";
		String resDes = "";
		String nodeCommand = "";
		String statOut = "";
		String eventOut = "";
		String rootElement = "";
		Boolean isCommandWithPasskey = false;
		if (messageType.equalsIgnoreCase(GssoMessageType.JSON)) {
			/** IDLE_SEND_OTP_REQ **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())) {
				nodeCommand = EventLog.SEND_OTP.getEventLog();
				statOut = Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic();
				eventOut = EventLog.SEND_OTP.getEventLog();
				rootElement = MessageResponsePrefix.SEND_ONETIMEPW_RESPONSE;
			}
			/** IDLE_GENERATE_PK **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.GENERATE_PASSKEY_JSON.getMessageType())) {
				nodeCommand = EventLog.GENARATE_PASSKEY.getEventLog();
				statOut = Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_ERROR.getStatistic();
				eventOut = EventLog.GENARATE_PASSKEY.getEventLog();
				rootElement = MessageResponsePrefix.GENERATE_PASSKEY_RESPONSE;
			}

			resCode = jsonCode.getCode();
			resDes = jsonCode.getDescription();

			String json = "{\"" + rootElement + "\":{" + "\"code\":\"" + resCode + "\"," + "\"description\":\"" + resDes + "\","
					+ "\"isSuccess\":\"false\"," + "\"orderRef\":\"" + orderRef + "\"" + "}" + "}";

			output = new EquinoxRawData();
			output.setName(EventName.HTTP);
			output.setCType(origRawDataIncoming.getCType());
			output.setType(EventAction.RESPONSE);
			output.setTo(origRawDataIncoming.getOrig());
			output.setInvoke(origRawDataIncoming.getInvoke());
			output.addRawDataAttribute(EquinoxAttribute.VAL, json);
		}
		else if (messageType.equalsIgnoreCase(GssoMessageType.SOAP)) {
			// String messageXML = origRawdata.getRawDataMessage();
			/** IDLE_SEND_OTP_REQ **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_SOAP.getMessageType())) {
				nodeCommand = EventLog.SEND_OTP.getEventLog();
				statOut = Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic();
				eventOut = EventLog.SEND_OTP.getEventLog();
				rootElement = MessageResponsePrefix.SEND_ONETIMEPW_RESPONSE;
			}
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.GENERATE_PASSKEY_SOAP.getMessageType())) {
				nodeCommand = EventLog.GENARATE_PASSKEY.getEventLog();
				statOut = Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_ERROR.getStatistic();
				eventOut = EventLog.GENARATE_PASSKEY.getEventLog();
				rootElement = MessageResponsePrefix.GENERATE_PASSKEY_RESPONSE;
				isCommandWithPasskey = true;
			}
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_SOAP.getMessageType())) {
				nodeCommand = EventLog.WS_AUTHEN_OTP.getEventLog();
				statOut = Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_ERROR.getStatistic();
				eventOut = EventLog.WS_AUTHEN_OTP.getEventLog();
				rootElement = MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_RESPONSE;
			}
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_ID_SOAP.getMessageType())) {
				nodeCommand = EventLog.WS_AUTHEN_OTP_ID.getEventLog();
				statOut = Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_ERROR.getStatistic();
				eventOut = EventLog.WS_AUTHEN_OTP_ID.getEventLog();
				rootElement = MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_ID_RESPONSE;
			}
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_CREATE_OTP_SOAP.getMessageType())) {
				nodeCommand = EventLog.WS_CREATE_OTP.getEventLog();
				statOut = Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic();
				eventOut = EventLog.WS_CREATE_OTP.getEventLog();
				rootElement = MessageResponsePrefix.WS_CREATE_ONETIMEPASSWORD_RESPONSE;
			}
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_GENERATE_ONETIMEPW_SOAP.getMessageType())) {
				nodeCommand = EventLog.WS_GENERATE_OTP.getEventLog();
				statOut = Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_ERROR.getStatistic();
				eventOut = EventLog.WS_GENERATE_OTP.getEventLog();
				rootElement = MessageResponsePrefix.WS_GENERATE_ONETIMEPASSWORD_RESPONSE;
			}

			/** 013(UNKNOWN_SERVICE) **/
			if (jsonCode == JsonResultCode.UNKNOWN_SERVICE) {
				resCode = SoapResultCode.UNKNOWN_SERVICE.getCode();
				resDes = SoapResultCode.UNKNOWN_SERVICE.getDescription();
			}
			/** 014(E01_ERROR) **/
			else if (jsonCode == JsonResultCode.E01_ERROR) {
				resCode = SoapResultCode.E01_ERROR.getCode();
				resDes = SoapResultCode.E01_ERROR.getDescription();
			}
			/** 032(E01_TIMEOUT) **/
			else if (jsonCode == JsonResultCode.E01_TIMEOUT) {
				resCode = SoapResultCode.E01_TIMEOUT.getCode();
				resDes = SoapResultCode.E01_TIMEOUT.getDescription();
			}
			/** 012(SERVICE_NOT_ALLOW) **/
			else if (jsonCode == JsonResultCode.SERVICE_NOT_ALLOW) {
				resCode = SoapResultCode.UNKNOWN_SERVICE.getCode();
				resDes = SoapResultCode.UNKNOWN_SERVICE.getDescription();
			}

			String soapOut = createSoapOut(appInstance.getProfile(), origRawDataIncoming, resCode, resDes, rootElement, orderRef, "",
					false, isCommandWithPasskey,isWS);

			output = new EquinoxRawData();
			output.setName(EventName.HTTP);
			output.setCType(EventCtype.XML);
			output.setType(EventAction.RESPONSE);
			output.setTo(origRawDataIncoming.getOrig());
			output.setInvoke(origRawDataIncoming.getInvoke());
			output.setRawMessage(soapOut);
		}

		ec02Instance.incrementsStat(statOut);
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, eventOut);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			/** writeLog LOG **/
			composeDebugLog.addStatisticOut(statOut);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		// ===============================================WRITE
		// SUMMARY======================================================
		try {
			composeSummary.initialSummary(appInstance, appInstance.getMapOrigProfile().get(origRawDataIncoming.getInvoke()).getStartTimeOfInvoke(),
					origInvoke, nodeCommand, resCode, resDes);
			composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), origRawDataIncoming.getInvoke());
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
		if (composeSummary.isWriteSummary()) {
			appInstance.getListSummaryLog().add(composeSummary.getSummaryLog());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		return output;
	}

	public static EquinoxRawData createReturnErrorMessageIdle(APPInstance appInstance ,final GssoProfile profile, ArrayList<String> listOrderReference,
			final long timeStampIncoming, final EquinoxRawData rawDataIncoming, final JsonResultCode jsonCode,
			final String logDescription, final String path, GssoComposeDebugLog composeDebugLog, GssoComposeSummaryLog composeSummary,
			final String currentSubState) {

		EquinoxRawData output = null;
		String resCode = "";
		String resDes = "";
		String rootElement = "";
		String orderRef = "";
		String eventLog = "";
		String transactionID = "";
		boolean isCommandWithPasskey = false;
		boolean isConfirm = false;
		boolean isWS = false;
		
		orderRef = GssoGenerator.generateOrderReference(ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME),
				listOrderReference);

		/* IDLE_SEND_OTP_REQ */
		if (currentSubState.equals(SubStates.IDLE_SEND_OTP_REQ.toString())) {
			rootElement = MessageResponsePrefix.SEND_ONETIMEPW_RESPONSE;
			eventLog = EventLog.SEND_OTP.getEventLog();
		}
		/* IDLE_GENERATE_PK */
		else if (currentSubState.equals(SubStates.IDLE_GENERATE_PK.toString())) {
			rootElement = MessageResponsePrefix.GENERATE_PASSKEY_RESPONSE;
			eventLog = EventLog.GENARATE_PASSKEY.getEventLog();
			isCommandWithPasskey = true;

			/** EXTRACT GENPASSKEY **/
			GssoGenPasskeyRequest genPasskeyReq = GssoDataManagement.extractGssoGenPasskeyRequest(rawDataIncoming);
			if (genPasskeyReq == null || genPasskeyReq.getGeneratePasskey() == null
					|| genPasskeyReq.getGeneratePasskey().getOrderRef() == null) {
			}
			else {
				orderRef = genPasskeyReq.getGeneratePasskey().getOrderRef();
			}
		}
		/* IDLE_AUTH_OTP */
		else if (currentSubState.equals(SubStates.IDLE_AUTH_OTP.toString())) {
			rootElement = MessageResponsePrefix.AUTHEN_ONETIMEPASSWORD_RESPONSE;
			eventLog = EventLog.AUTHEN_OTP.getEventLog();
		}
		/* IDLE_CONFIRMATION_W_PK */
		else if (currentSubState.equals(SubStates.IDLE_CONFIRMATION_W_PK.toString())) {
			rootElement = MessageResponsePrefix.CONFIRM_ONETIMEPW_PASSKEY_RESPONSE;
			eventLog = EventLog.CONFIRM_OTP_WITH_PASSKEY.getEventLog();
			isCommandWithPasskey = true;

			GssoConfirmOTPRequest confirmOTPReq = GssoDataManagement.extractGssoConfirmOTPRequest(rawDataIncoming);
			SendConfirmOTPRequest confirmOneTimePasswordWithPasskey = confirmOTPReq.getConfirmOneTimePasswordWithPasskey();
			if (confirmOTPReq != null && confirmOneTimePasswordWithPasskey != null
					&& confirmOneTimePasswordWithPasskey.getTransactionID() != null) {
				transactionID = confirmOneTimePasswordWithPasskey.getTransactionID();
			}
		}
		/* IDLE_CONFIRMATION */
		else if (currentSubState.equals(SubStates.IDLE_CONFIRMATION.toString())) {
			rootElement = MessageResponsePrefix.CONFIRM_ONETIMEPW_RESPONSE;
			eventLog = EventLog.CONFIRM_OTP.getEventLog();
			isConfirm = true;
			
			GssoConfirmOTPRequest confirmOTPReq = GssoDataManagement.extractGssoConfirmOTPRequest(rawDataIncoming);
			SendConfirmOTPRequest confirmOneTimePW = confirmOTPReq.getConfirmOneTimePW();
			if (confirmOTPReq != null && confirmOneTimePW != null && confirmOneTimePW.getTransactionID() != null) {
				transactionID = confirmOneTimePW.getTransactionID();
			}
		}
		/* IDLE_WS_AUTH_OTP */
		else if(currentSubState.equals(SubStates.IDLE_WS_AUTH_OTP.toString())){
			isWS = true;
			rootElement = MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_RESPONSE;
			eventLog = EventLog.WS_AUTHEN_OTP.getEventLog();
		}
		/* IDLE_WS_AUTH_OTP_ID */
		else if(currentSubState.equals(SubStates.IDLE_WS_AUTH_OTP_ID.toString())){
			isWS = true;
			rootElement = MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_ID_RESPONSE;
			eventLog = EventLog.WS_AUTHEN_OTP_ID.getEventLog();
		}
		/* IDLE_WS_CREATE_OTP */
		else if(currentSubState.equals(SubStates.IDLE_WS_CREATE_OTP.toString())){
			isWS = true;
			rootElement = MessageResponsePrefix.WS_CREATE_ONETIMEPASSWORD_RESPONSE;
			eventLog = EventLog.WS_CREATE_OTP.getEventLog();
		}
		/* IDLE_WS_GENERATE_OTP */
		else if(currentSubState.equals(SubStates.IDLE_WS_GENERATE_OTP.toString())){
			isWS = true;
			rootElement = MessageResponsePrefix.WS_GENERATE_ONETIMEPASSWORD_RESPONSE;
			eventLog = EventLog.WS_GENERATE_OTP.getEventLog();
		}
		/* IDLE_WS_CONFIRM_OTP */
		else if (currentSubState.equals(SubStates.IDLE_WS_CONFIRM_OTP.toString())) {
			isWS = true;
			rootElement = MessageResponsePrefix.WS_CONFIRM_ONETIMEPASSWORD_RESPONSE;
			eventLog = EventLog.WS_CONFIRM_OTP.getEventLog();
			isConfirm = true;

			GssoWSConfirmOTPRequest confirmOTPReq = GssoDataManagement.extractGssoWSConfirmOTPRequest(rawDataIncoming);
			SendWSConfirmOTPRequest sendWSConfirmOTPRequest = confirmOTPReq.getSendWSConfirmOTPReq();
			if (confirmOTPReq != null && sendWSConfirmOTPRequest != null && sendWSConfirmOTPRequest.getSessionId() != null) {
				transactionID = sendWSConfirmOTPRequest.getSessionId();
			}
		}
		/* IDLE_WS_CONFIRM_OTP_ID */
		else if (currentSubState.equals(SubStates.IDLE_WS_CONFIRM_OTP_ID.toString())) {
			isWS = true;
			rootElement = MessageResponsePrefix.WS_CONFIRM_ONETIMEPASSWORD_ID_RESPONSE;
			eventLog = EventLog.WS_CONFIRM_OTP_ID.getEventLog();
			isConfirm = true;

			GssoWSConfirmOTPWithIDRequest confirmOTPWithIDRequest = GssoDataManagement.extractGssoWSConfirmOTPWithIDRequest(rawDataIncoming);
			SendWSConfirmOTPWithIDRequest sendWSConfirmOTPWithIDRequest = confirmOTPWithIDRequest.getSendWSConfirmOTPWithIDReq();
			if (confirmOTPWithIDRequest != null && sendWSConfirmOTPWithIDRequest != null && sendWSConfirmOTPWithIDRequest.getSessionId() != null) {
				transactionID = sendWSConfirmOTPWithIDRequest.getSessionId();
			}
		}
		
		String cType = rawDataIncoming.getCType();
		if (!cType.equalsIgnoreCase(EventCtype.XML)) {
			StringBuilder jsonResp = new StringBuilder();
			resCode = jsonCode.getCode();
			resDes = jsonCode.getDescription();

			jsonResp.append("{\"" + rootElement + "\":");
			jsonResp.append("{\"code\":\"" + resCode + "\",");
			jsonResp.append("\"description\":\"" + resDes + "\",");
			jsonResp.append("\"isSuccess\":\"false\",");
			jsonResp.append("\"orderRef\":\"" + orderRef + "\"");
			if (isCommandWithPasskey || isConfirm) {
				jsonResp.append(",\"transactionID\":\"" + transactionID + "\"");
			}
			jsonResp.append("}").append("}");

			output = new EquinoxRawData();
			output.setName(EventName.HTTP);
			output.setCType(rawDataIncoming.getCType());
			output.setType(EventAction.RESPONSE);
			output.setTo(rawDataIncoming.getOrig());
			output.setInvoke(rawDataIncoming.getInvoke());
			output.addRawDataAttribute(EquinoxAttribute.VAL, jsonResp.toString());
		}
		else {
			/** 001(WRONG_MSISDN_FORMAT) **/
			if (jsonCode == JsonResultCode.WRONG_MSISDN_FORMAT) {
				resCode = SoapResultCode.WRONG_MSISDN_FORMAT.getCode();
				resDes = SoapResultCode.WRONG_MSISDN_FORMAT.getDescription();
			}
			/** 002(SERVICE_VAL_EMPTY) **/
			else if (jsonCode == JsonResultCode.WRONG_EMAIL_ADDR_FORMAT) {
				resCode = SoapResultCode.WRONG_EMAIL_ADDR_FORMAT.getCode();
				resDes = SoapResultCode.WRONG_EMAIL_ADDR_FORMAT.getDescription();
			}
			/** 003(SERVICE_VAL_EMPTY) **/
			else if (jsonCode == JsonResultCode.SERVICE_VAL_EMPTY) {
				resCode = SoapResultCode.SERVICE_VAL_EMPTY.getCode();
				resDes = SoapResultCode.SERVICE_VAL_EMPTY.getDescription();
			}
			/** 004(UNKNOWN_ACCOUNT_TYPE) **/
			else if (jsonCode == JsonResultCode.UNKNOWN_ACCOUNT_TYPE) {
				resCode = SoapResultCode.UNKNOWN_ACCOUNT_TYPE_ERROR.getCode();
				resDes = SoapResultCode.UNKNOWN_ACCOUNT_TYPE_ERROR.getDescription();
			}
			/** 005(UNKNOWN_OTP_CHANNEL) **/
			else if (jsonCode == JsonResultCode.UNKNOWN_OTP_CHANNEL) {
				resCode = SoapResultCode.INVALID_OTPCHANNEL.getCode();
				resDes = SoapResultCode.INVALID_OTPCHANNEL.getDescription();
			}
			/** 006(WRONG_INPUT_PARAMETER) **/
			else if (jsonCode == JsonResultCode.WRONG_INPUT_PARAMETER) {
				resCode = SoapResultCode.FAIL_UNKNOWN.getCode();
				resDes = SoapResultCode.FAIL_UNKNOWN.getDescription();
			}
			/** 007(WRONG_ONETIME_PASSWORD_FORMAT) **/
			if (jsonCode == JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT) {
				resCode = SoapResultCode.PASSWORD_WRONG_FORMAT.getCode();
				resDes = SoapResultCode.PASSWORD_WRONG_FORMAT.getDescription();
			}
			/** 008(WRONG_TRANSACTION_ID_FORMAT) **/
			else if (jsonCode == JsonResultCode.WRONG_TRANSACTION_ID_FORMAT) {
				resCode = SoapResultCode.SESSIONID_WRONG_FORMAT.getCode();
				resDes = SoapResultCode.SESSIONID_WRONG_FORMAT.getDescription();
			}
			/** 011 (SERVICE_NOT_ALLOW) **/
			else if (jsonCode == JsonResultCode.ACCOUNT_TYPE_NOT_MATCH_TYPE) {
				resCode = SoapResultCode.COS_NOT_MATCH_TYPE.getCode();
				resDes = SoapResultCode.COS_NOT_MATCH_TYPE.getDescription();
			}
			/** 012 (SERVICE_NOT_ALLOW) **/
			else if (jsonCode == JsonResultCode.SERVICE_NOT_ALLOW) {
				resCode = SoapResultCode.UNKNOWN_SERVICE.getCode();
				resDes = SoapResultCode.UNKNOWN_SERVICE.getDescription();
			}
			/** 014(AUTHAN_FAIL) **/
			else if (jsonCode == JsonResultCode.AUTHEN_FAIL) {
				resCode = SoapResultCode.CONFIRM_FAIL.getCode();
				resDes = SoapResultCode.CONFIRM_FAIL.getDescription();
			}
			/** 015(NOT_AUTHEN_BEFORE) **/
			else if (jsonCode == JsonResultCode.NOT_AUTHEN_BEFORE) {
				resCode = SoapResultCode.NOT_AUTHEN_BEFORE.getCode();
				resDes = SoapResultCode.NOT_AUTHEN_BEFORE.getDescription();
			}
			/** 016(ONETIME_PASSWORD_EXPIRE) **/
			else if (jsonCode == JsonResultCode.ONETIME_PASSWORD_EXPIRE) {
				resCode = SoapResultCode.PASSWORD_TIME_OUT.getCode();
				resDes = SoapResultCode.PASSWORD_TIME_OUT.getDescription();
			}
			/** 017(HACK_TIME_MORETHAN_3) **/
			else if (jsonCode == JsonResultCode.HACK_TIME_MORETHAN_3) {
				resCode = SoapResultCode.HACK_TIME_MORE_THAN_3.getCode();
				resDes = SoapResultCode.HACK_TIME_MORE_THAN_3.getDescription();
			}
			/** 029(MISSING_VALUE_OTPMOBILE_AND_EMAIL) **/
			else if (jsonCode == JsonResultCode.MISSING_VALUE_OTPMOBILE_AND_EMAIL) {
				resCode = SoapResultCode.MISSING_VALUE_OTPMOBILE_AND_EMAIL.getCode();
				resDes = SoapResultCode.MISSING_VALUE_OTPMOBILE_AND_EMAIL.getDescription();
			}
			/** 034(MAXIMUM_AUTHEN_TRANSACTION) **/
			else if (jsonCode == JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION) {
				resCode = SoapResultCode.GSSO_BUSY.getCode();
				resDes = SoapResultCode.GSSO_BUSY.getDescription();
			}
			/** 036(WRONG_DUMMY_ACCOUNT_TYPE) **/
			else if (jsonCode == JsonResultCode.WRONG_DUMMY_ACCOUNT_TYPE) {
				resCode = SoapResultCode.WRONG_DUMMY_ACCOUNT_TYPE.getCode();
				resDes = SoapResultCode.WRONG_DUMMY_ACCOUNT_TYPE.getDescription();
			}
			/** 043(WS_WRONG_INPUT_PARAMETER) **/
			else if (jsonCode == JsonResultCode.WS_WRONG_INPUT_PARAMETER) {
				resCode = SoapResultCode.WS_WRONG_INPUT_PARAMETER.getCode();
				resDes = SoapResultCode.WS_WRONG_INPUT_PARAMETER.getDescription();
			}
			/** 028(ADD_TIMEOUT_MINS_EXCEED) **/
			else if (jsonCode == JsonResultCode.ADD_TIMEOUT_MINS_EXCEED) {
				resCode = SoapResultCode.ADD_TIMEOUT_MINS_EXCEED.getCode();
				resDes = SoapResultCode.ADD_TIMEOUT_MINS_EXCEED.getDescription();
			}
			/** 028(STATE_NOT_USE_SERVICE) **/
			else if (jsonCode == JsonResultCode.WS_STATE_NOT_USE_SERVICE) {
				resCode = SoapResultCode.STATE_NOT_USE_SERVICE.getCode();
				resDes = SoapResultCode.STATE_NOT_USE_SERVICE.getDescription();
			}

			
			

			String soapOut = createSoapOut(profile, rawDataIncoming, resCode, resDes, rootElement, orderRef, transactionID, isConfirm,
					isCommandWithPasskey,isWS);

			output = new EquinoxRawData();
			output.setName(EventName.HTTP);
			output.setCType(EventCtype.XML);
			output.setType(EventAction.RESPONSE);
			output.setTo(rawDataIncoming.getOrig());
			output.setInvoke(rawDataIncoming.getInvoke());
			output.setRawMessage(soapOut);
		}

		// ===============================================DEBUG
		// LOG==========================================================
		/** writeLog LOG **/
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			if (logDescription.equalsIgnoreCase("GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE")) {
				composeDebugLog.serviceTemplateMisMatch();
			}
			else {
				composeDebugLog.setFailureAvp(path + " " + logDescription);
			}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			// ===============================================DEBUG
			// LOG==========================================================
			/** writeLog LOG **/
			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}
		// ===============================================WRITE
		// SUMMARY======================================================
		composeSummary.setWriteSummary();
		composeSummary.initialSummary(appInstance, timeStampIncoming, rawDataIncoming.getInvoke(), eventLog, resCode, resDes);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		return output;
	}

	public static EquinoxRawData createGenpassResp(final APPInstance appInstance, final EquinoxRawData rawDataIncoming,
			final GssoServiceTemplate thisServiceTemplate, GssoComposeDebugLog composeDebugLog,
			final GssoGenPasskeyRequest genPasskeyReq) {

		EquinoxRawData output = null;

		String splitTransactionID = "";
		String passkey = "";
		String seedkey = "";
		String dataForEncrypt = "";
		GenPasskey generatePasskey = genPasskeyReq.getGeneratePasskey();

		if (generatePasskey.getOrderRef() == null) {
			String orderRef = GssoGenerator.generateOrderReference(ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME),
					appInstance.getListOrderReference());
			generatePasskey.setOrderRef(orderRef);
		}

		/* Passkey = �msisdn��transaction Id(3-14)� */
		if (thisServiceTemplate.getSeedkey() == null || thisServiceTemplate.getSeedkey().isEmpty()) {

		}
		else {
			splitTransactionID = genPasskeyReq.getGeneratePasskey().getTransactionID().substring(2, 14);
			dataForEncrypt = genPasskeyReq.getGeneratePasskey().getMsisdn() + splitTransactionID;

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.setPasskeyDecode(dataForEncrypt);
			}

			seedkey = thisServiceTemplate.getSeedkey();
			passkey = GssoGenerator.encryptToken(dataForEncrypt, seedkey);
		}

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			composeDebugLog.setPasskeyDecode(dataForEncrypt);
			composeDebugLog.setPasskeyEncode(passkey);
		}

		String cType = rawDataIncoming.getCType();
		if (!cType.equalsIgnoreCase(EventCtype.XML)) {
			String json = "{\"generatePasskeyResponse\":" + "{" + "\"code\":\"" + JsonResultCode.SUCCESS.getCode() + "\","
					+ "\"description\":\"" + JsonResultCode.SUCCESS.getDescription() + "\"," + "\"isSuccess\":\"true\","
					+ "\"orderRef\":\"" + generatePasskey.getOrderRef() + "\"," + "\"transactionID\":\""
					+ generatePasskey.getTransactionID() + "\"," + "\"passkey\":\"" + passkey + "\"" + "}" + "}";

			output = new EquinoxRawData();
			output.setName(EventName.HTTP);
			output.setCType(rawDataIncoming.getCType());
			output.setType(EventAction.RESPONSE);
			output.setTo(rawDataIncoming.getOrig());
			output.setInvoke(rawDataIncoming.getInvoke());
			output.addRawDataAttribute(EquinoxAttribute.VAL, json);
		}
		else {
			String soapOut = createSoapGenPKSuccessMessageResp(rawDataIncoming, genPasskeyReq, appInstance.getProfile(), passkey);

			output = new EquinoxRawData();
			output.setName(EventName.HTTP);
			output.setCType(EventCtype.XML);
			output.setType(EventAction.RESPONSE);
			output.setTo(rawDataIncoming.getOrig());
			output.setInvoke(rawDataIncoming.getInvoke());
			output.setRawMessage(soapOut);
		}

		return output;
	}

	
	public static String createSoapOut(final GssoProfile profile, final EquinoxRawData rawDataIncoming, final String resCode,
			final String resDes, final String rootElement, final String orderRef, final String transactionID, boolean isConfirm,
			boolean isConfirmWithPasskey,boolean isWS) {

		StringBuilder soapOutBuilder = new StringBuilder();
		String link = "";
		soapOutBuilder.append("<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		soapOutBuilder.append("<S:Body>");
//		soapOutBuilder.append("<ns2:" + rootElement + " xmlns:ns2=\"http://ws.sso.gsso/\">");
		String[] possibleValue = new String[] { MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_RESPONSE,
				MessageResponsePrefix.WS_AUTHEN_ONETIMEPASSWORD_ID_RESPONSE,
				MessageResponsePrefix.WS_CREATE_ONETIMEPASSWORD_RESPONSE,
				MessageResponsePrefix.WS_GENERATE_ONETIMEPASSWORD_RESPONSE,
				MessageResponsePrefix.WS_CONFIRM_ONETIMEPASSWORD_RESPONSE,
				MessageResponsePrefix.WS_CONFIRM_ONETIMEPASSWORD_ID_RESPONSE, };
		if(Arrays.asList(possibleValue).contains(rootElement)&&isWS){
			link = "ws.gsso";
		}
		else{
			link = "ws.sso.gsso";
		}
		soapOutBuilder.append("<ns2:"+ rootElement + " xmlns:ns2=\"http://"+link+"/\">");
		if (isConfirmWithPasskey) {
			soapOutBuilder
					.append("<return xsi:type=\"ns2:gssoSsoResponsePassKey\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		}
		else {
			soapOutBuilder.append("<return>");
		}

		soapOutBuilder.append("<code>" + resCode + "</code>");
		soapOutBuilder.append("<description>" + resDes + "</description>");

		/* Response for only sso command */
		if(link.equals("ws.sso.gsso")){
			soapOutBuilder.append("<isSuccess>false</isSuccess>");
			soapOutBuilder.append("<operName/>");
//			if (profile == null || profile.getOper() == null || profile.getOper().isEmpty()) {
//				soapOutBuilder.append("<operName/>");
//			}
//			else {
//				soapOutBuilder.append("<operName>" + profile.getOper() + "</operName>");
//			}
		}
		/* ws command */
		else{
			soapOutBuilder.append("<isSuccess>false</isSuccess>");
		}
		soapOutBuilder.append("<orderRef>" + orderRef + "</orderRef>");
		soapOutBuilder.append("<pwd/>");

		if (isConfirmWithPasskey || isConfirm) {
			if(StringUtils.isNoneEmpty(transactionID))
				soapOutBuilder.append("<transactionID>" + transactionID + "</transactionID>");
			else
				soapOutBuilder.append("<transactionID/>");
		}
		else {
			soapOutBuilder.append("<transactionID/>");
		}
//		if(link.equals("ws.sso.gsso")){
		if (isConfirmWithPasskey) {
			soapOutBuilder.append("<passKey/>");
		}
//		soapOutBuilder.append("<passKey/>");
		soapOutBuilder.append("</return>");
		soapOutBuilder.append("</ns2:" + rootElement + ">");
		soapOutBuilder.append("</S:Body>");
		soapOutBuilder.append("</S:Envelope>");

		return soapOutBuilder.toString();
	}

	private static String createSoapGenPKSuccessMessageResp(final EquinoxRawData origEqxRawDataIncoming,
			final GssoGenPasskeyRequest genPasskeyReq, final GssoProfile profile, final String passkey) {

		StringBuilder soapOutBuilder = new StringBuilder();
		soapOutBuilder.append("<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		soapOutBuilder.append("<S:Body>");
		soapOutBuilder.append("<ns2:generatePasskeyResponse xmlns:ns2=\"http://ws.sso.gsso/\">");
		soapOutBuilder.append("<return>");
		soapOutBuilder.append("<code>" + SoapResultCode.SUCCESS.getCode() + "</code>");
		soapOutBuilder.append("<description>" + SoapResultCode.SUCCESS.getDescription() + "</description>");
		soapOutBuilder.append("<isSuccess>true</isSuccess>");

		if (profile == null || profile.getOper() == null || profile.getOper().isEmpty()) {
			soapOutBuilder.append("<operName/>");
		}
		else {
			soapOutBuilder.append("<operName>" + profile.getOper() + "</operName>");
		}

		soapOutBuilder.append("<orderRef>" + genPasskeyReq.getGeneratePasskey().getOrderRef() + "</orderRef>");
		soapOutBuilder.append("<pwd/>");
		soapOutBuilder.append("<transactionID>" + genPasskeyReq.getGeneratePasskey().getTransactionID() + "</transactionID>");
		soapOutBuilder.append("<passKey>" + passkey + "</passKey>");
		soapOutBuilder.append("</return>");
		soapOutBuilder.append("</ns2:generatePasskeyResponse>");
		soapOutBuilder.append("</S:Body>");
		soapOutBuilder.append("</S:Envelope>");

		return soapOutBuilder.toString();
	}

	private static String chooseSourceAddrTon(final String smsSender, boolean isFoundEC02MobileFormat) {
		String sourceAddrTon;
		String mobileFormat = ConfigureTool.getConfigure(ConfigName.MOBILEFORMAT);
		try {
			String[] mobileFormatSplit = mobileFormat.split(",");
			for (String mobileValue : mobileFormatSplit) {

				String[] splitPrefixAndLength = mobileValue.split("\\|");
				String[] splitPrefix = splitPrefixAndLength[0].split("\\:");
				String mobileLength = splitPrefixAndLength[1];
				for (String prefix : splitPrefix) {
					String msisdnPrefix = smsSender.substring(0, prefix.length());
					if (msisdnPrefix.equals(prefix)) {
						if (smsSender.length() == Integer.parseInt(mobileLength)) {
							isFoundEC02MobileFormat = true;
							break;
						}
					}
				}
			}
		}
		catch (Exception e) {
			sourceAddrTon = SourceAddrTon.ALPHANUMERIC;
			// sourceAddrNpi = SourceAddrNpi.UNKNOWN;
		}

		try {
			if (isFoundEC02MobileFormat) {
				sourceAddrTon = SourceAddrTon.INTERNATIONAL;
				// sourceAddrNpi = SourceAddrNpi.ISDN;
			}
			else {
				String msisdnPattern = "[0-9]+";
				Pattern typePattern = null;
				Matcher typeMatcher = null;

				typePattern = Pattern.compile(msisdnPattern);
				typeMatcher = typePattern.matcher(smsSender);
				if (typeMatcher.matches()) {
					sourceAddrTon = SourceAddrTon.NETWORKSPECIFIC;
					// sourceAddrNpi = SourceAddrNpi.TELEX;
				}
				else {
					sourceAddrTon = SourceAddrTon.ALPHANUMERIC;
					// sourceAddrNpi = SourceAddrNpi.UNKNOWN;
				}
			}
		}
		catch (Exception e) {
			sourceAddrTon = SourceAddrTon.ALPHANUMERIC;
			// sourceAddrNpi = SourceAddrNpi.UNKNOWN;
		}
		return sourceAddrTon;
	}

	private static String chooseSourceAddrNpi(final String smsSender, boolean isFoundEC02MobileFormat) {
		String sourceAddrNpi;
		String mobileFormat = ConfigureTool.getConfigure(ConfigName.MOBILEFORMAT);
		try {
			String[] mobileFormatSplit = mobileFormat.split(",");
			for (String mobileValue : mobileFormatSplit) {

				String[] splitPrefixAndLength = mobileValue.split("\\|");
				String[] splitPrefix = splitPrefixAndLength[0].split("\\:");
				String mobileLength = splitPrefixAndLength[1];
				for (String prefix : splitPrefix) {
					String msisdnPrefix = smsSender.substring(0, prefix.length());
					if (msisdnPrefix.equals(prefix)) {
						if (smsSender.length() == Integer.parseInt(mobileLength)) {
							isFoundEC02MobileFormat = true;
							break;
						}
					}
				}
			}
		}
		catch (Exception e) {
			// sourceAddrTon = SourceAddrTon.ALPHANUMERIC;
			sourceAddrNpi = SourceAddrNpi.UNKNOWN;
		}

		try {
			if (isFoundEC02MobileFormat) {
				// sourceAddrTon = SourceAddrTon.INTERNATIONAL;
				sourceAddrNpi = SourceAddrNpi.ISDN;
			}
			else {
				String msisdnPattern = "[0-9]+";
				Pattern typePattern = null;
				Matcher typeMatcher = null;

				typePattern = Pattern.compile(msisdnPattern);
				typeMatcher = typePattern.matcher(smsSender);
				if (typeMatcher.matches()) {
					// sourceAddrTon = SourceAddrTon.NETWORKSPECIFIC;
					sourceAddrNpi = SourceAddrNpi.TELEX;
				}
				else {
					// sourceAddrTon = SourceAddrTon.ALPHANUMERIC;
					sourceAddrNpi = SourceAddrNpi.UNKNOWN;
				}
			}
		}
		catch (Exception e) {
			// sourceAddrTon = SourceAddrTon.ALPHANUMERIC;
			sourceAddrNpi = SourceAddrNpi.UNKNOWN;
		}
		return sourceAddrNpi;
	}
	
	public static EquinoxRawData createRefundReqTorPCEFMessage(final EquinoxRawData rawDataIncoming, EC02Instance ec02Instance,
			final String sessionId, final String refId, final String msisdn, GssoComposeDebugLog composeDebugLog) {

		String origInvoke = InvokeFilter.getOriginInvoke(rawDataIncoming.getInvoke());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(System.currentTimeMillis());
		
		EquinoxRawData output = new EquinoxRawData();
		APPInstance appInstance = ec02Instance.getAppInstance();

		appInstance.setWaitInquirySub(true);

		/** VALID OTP REQ STATICTIC **/
		String statOut = Statistic.GSSO_SEND_RPCEF_REFUND_MANAGEMENT_REQUEST.getStatistic();
		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvoke, SubStates.W_REFUND.name());

		/** THE APPLICATION SHALL OUTPUT AN REFUND REQUEST TO rPCEF **/
		Refund refund = new Refund();
		refund.setCommand("refundManagement");
		refund.setSessionId(sessionId);
		refund.setActualTime(formatter.format(date));
		refund.setTid("gsso-" + msisdn + formatter.format(date));
		refund.setRefId(refId);
		
		/* SAVE REFUND MESSAGE REQUEST TO INSTANCE*/
		appInstance.getMapInvokeOfRefund().put(origInvoke, refund);
		
		String messageOut = MessageParser.toJson(refund);
		
		HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put(EQX.Attribute.NAME, EQX.Protocol.HTTP);
		attrs.put(EQX.Attribute.CTYPE, EQX.Ctype.TEXTPLAIN);
		attrs.put(EQX.Attribute.METHOD, EQX.MessageType.POST);
		attrs.put(EQX.Attribute.TYPE, EQX.MessageType.REQUEST);
		attrs.put(EQX.Attribute.TO, ConfigureTool.getConfigure(ConfigName.RPCEF_INTERFACE));
		attrs.put(EQX.Attribute.URL, ConfigureTool.getConfigure(ConfigName.RPCEF_REFUND_URL));
		attrs.put(EQX.Attribute.VAL, messageOut);
		output.setRawDataAttributes(attrs);
		output.setInvoke(invokeOutgoing);
		
		String refundTimeoutMin = ConfigureTool.getConfigure(ConfigName.REFUND_TIMEOUT);

		TimeoutCalculator timeoutCal = TimeoutCalculator.initialTimeout(Integer.parseInt(refundTimeoutMin));
		appInstance.getMapTimeoutOfWaitRefund().put(invokeOutgoing, timeoutCal);
		
		ec02Instance.incrementsStat(statOut);
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.REFUND.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			composeDebugLog.addStatisticOut(statOut);
			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return output;
	}
	
	public static EquinoxRawData createRefundReqTorPCEFMessageRetry(final EquinoxRawData rawDataIncoming, EC02Instance ec02Instance,
			Refund refund, GssoComposeDebugLog composeDebugLog) {

		String origInvoke = InvokeFilter.getOriginInvoke(rawDataIncoming.getInvoke());
		
		EquinoxRawData output = new EquinoxRawData();
		APPInstance appInstance = ec02Instance.getAppInstance();

		appInstance.setWaitInquirySub(true);

		/** VALID OTP REQ STATICTIC **/
		String statOut = Statistic.GSSO_SEND_RPCEF_REFUND_MANAGEMENT_REQUEST.getStatistic();
		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvoke, SubStates.W_REFUND.name());
		
		String messageOut = MessageParser.toJson(refund);
		
		HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put(EQX.Attribute.NAME, EQX.Protocol.HTTP);
		attrs.put(EQX.Attribute.CTYPE, EQX.Ctype.TEXTPLAIN);
		attrs.put(EQX.Attribute.METHOD, EQX.MessageType.POST);
		attrs.put(EQX.Attribute.TYPE, EQX.MessageType.REQUEST);
		attrs.put(EQX.Attribute.TO, ConfigureTool.getConfigure(ConfigName.RPCEF_INTERFACE));
		attrs.put(EQX.Attribute.URL, ConfigureTool.getConfigure(ConfigName.RPCEF_REFUND_URL));
		attrs.put(EQX.Attribute.VAL, messageOut);
		output.setRawDataAttributes(attrs);
		output.setInvoke(invokeOutgoing);
		
		String refundTimeoutMin = ConfigureTool.getConfigure(ConfigName.REFUND_TIMEOUT);

		TimeoutCalculator timeoutCal = TimeoutCalculator.initialTimeout(Integer.parseInt(refundTimeoutMin));
		appInstance.getMapTimeoutOfWaitRefund().put(invokeOutgoing, timeoutCal);
		
		ec02Instance.incrementsStat(statOut);
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(invokeOutgoing, EventLog.REFUND.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			composeDebugLog.addStatisticOut(statOut);
//			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return output;
	}
}
