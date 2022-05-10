package com.ais.eqx.gsso.substates;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ais.eqx.gsso.controller.SubStateManager;
import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.interfaces.IdleMessageFormat;
import com.ais.eqx.gsso.interfaces.RetNumber;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class IDLE_MANAGEMENT implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;

	private EquinoxRawData				rawDataIncoming;
	private ArrayList<EquinoxRawData>	rawDatasOutgoing;
	private String						nextState;

	private String						code;
	private String						description;
	private String						destNodeResultCode			= "null";
	private String						destNodeResultDescription	= "null";
	private String						destNodeName				= "Client";
	private String						destNodeCommand				= EventLog.CLIENT_UNKNOWN.getEventLog();

	private GssoComposeDetailsLog		composeDetailsLog;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;
	private MapDetailsAndConfigType		mapDetails;

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		this.nextState = SubStates.IDLE_MANAGEMENT.toString();
		/** END **/

		/************** INITIAL *****************/
		idleIDLEMANAGEMENTInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);

		/************** CODING ******************/
		// System.out.println("Start IDLE_MANAGEMENT");

		/** NORMAL FLOW **/
		if (RetNumber.NORMAL.equals(rawDataIncoming.getRet())) {

			/* FIND SUB STATES */
			SubStateManager subStateManager = new SubStateManager(findStateIdle(rawDataIncoming));
			this.rawDatasOutgoing = subStateManager.doActionSubState(abstractAF, ec02Instance, rawDataIncoming);

		}
		/** NO FLOW **/
		else {
			errorCase(rawDataIncoming);
		}

		return this.rawDatasOutgoing;
	}

	private void errorCase(EquinoxRawData rawData) {
		/** GSSO Received UnknownMessage Request **/
		String origInvoke = rawData.getInvoke();
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_UNKNOWN_MESSAGE.getStatistic());

		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.CLIENT_UNKNOWN.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		// ===============================================WRITE
		// SUMMARY======================================================
		destNodeResultCode = "null"; // ResultCode From Destination Node If Not
		// Set "null"
		this.destNodeCommand = EventLog.CLIENT_UNKNOWN.getEventLog();
		destNodeResultDescription = "Unknown"; // Description From Destination
		// Node If Not Like Timeout Set
		// Timeout Description
		this.composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode, destNodeResultDescription); // Add
		// When
		// Message
		// Response

		try {
			this.composeSummary.setWriteSummary();
			this.composeSummary.initialSummary(this.appInstance ,appInstance.getTimeStampIncoming(), origInvoke, destNodeCommand, this.code,
					this.description);
			this.composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), rawDataIncoming.getInvoke());
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		// ===============================================WRITE
		// DETAILS======================================================
		/** SET DTAILS IDENTITY **/
		this.composeDetailsLog.setIdentity("unknown");
		try {
			this.composeDetailsLog.initialIncoming(rawData, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		// ===============================================SAVE
		// DETAILS======================================================
		mapDetails.setDetail(this.composeDetailsLog.getDetailsLog());
		appInstance.getListDetailsLog().add(mapDetails);
		// ===============================================SAVE
		// SUMMARY======================================================
		if (this.composeSummary.isWriteSummary()) {
			appInstance.getListSummaryLog().add(this.composeSummary.getSummaryLog());
		}

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_UNKNOWN_MESSAGE.getStatistic());
			this.composeDebugLog.initialGssoSubStateLog(rawData);
			this.composeDebugLog.writeDebugSubStateLog();
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}
	}

	private String findStateIdle(EquinoxRawData rawData) {
		String subState = null;

		/** TEXT/XML **/
		if (rawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
			boolean isFoundState = false;
			try {
				String messageXML = rawData.getRawDataMessage();
				
				/** SSO Interface **/
				if (messageXML.contains(IdleMessageFormat.URL_SSO_INTERFACE)){

					/** IDLE_SEND_OTP_REQ **/
					if (checkCommand(messageXML, IdleMessageFormat.SOAP_SEND_OTP_REQ)) {
						subState = SubStates.IDLE_SEND_OTP_REQ.name();
						isFoundState = true;
					}
					/** IDLE_GENERATE_PK **/
					else if (checkCommand(messageXML, IdleMessageFormat.SOAP_GENERATE_PK)) {
						subState = SubStates.IDLE_GENERATE_PK.name();
						isFoundState = true;
					}
					/** IDLE_CONFIRMATION_W_PK **/
					else if (checkCommand(messageXML, IdleMessageFormat.SOAP_CONFIRM_OTP_W_PK)) {
						subState = SubStates.IDLE_CONFIRMATION_W_PK.name();
						isFoundState = true;
					}
					/** IDLE_CONFIRMATION **/
					else if (checkCommand(messageXML, IdleMessageFormat.SOAP_CONFIRM_OTP)) {
						subState = SubStates.IDLE_CONFIRMATION.name();
						isFoundState = true;
					}
					
				}
				/** WS Interface **/
				else if (messageXML.contains(IdleMessageFormat.URL_WS_INTERFACE)){
					/** IDLE_WS_CREATE_OTP **/
					if (checkCommand(messageXML, IdleMessageFormat.SOAP_WS_CREATE_OTP_REQ)){
						subState = SubStates.IDLE_WS_CREATE_OTP.name();
						isFoundState = true;
					}
					/** IDLE_WS_GENERATE_OTP **/
					else if (checkCommand(messageXML, IdleMessageFormat.SOAP_WS_GENERATE_OTP_REQ)){ 
						subState = SubStates.IDLE_WS_GENERATE_OTP.name();
						isFoundState = true;
					}
					/** IDLE_WS_AUTH_OTP_ID **/	
					else if (checkCommand(messageXML, IdleMessageFormat.SOAP_WS_AUTH_OTP_ID_REQ)){ 
						subState = SubStates.IDLE_WS_AUTH_OTP_ID.name();
						isFoundState = true;
					}					
					/** IDLE_WS_AUTH_OTP **/	
					else if (checkCommand(messageXML, IdleMessageFormat.SOAP_WS_AUTH_OTP_REQ)){ 
						subState = SubStates.IDLE_WS_AUTH_OTP.name();
						isFoundState = true;
					}
					/** IDLE_WS_CONFIRM_OTP_ID **/
					else if (checkCommand(messageXML, IdleMessageFormat.SOAP_WS_CONFIRM_OTP_ID_REQ)){ 
						subState = SubStates.IDLE_WS_CONFIRM_OTP_ID.name();
						isFoundState = true;
					}
					/** IDLE_WS_CONFIRM_OTP **/
					else if (checkCommand(messageXML, IdleMessageFormat.SOAP_WS_CONFIRM_OTP_REQ)){ 
						subState = SubStates.IDLE_WS_CONFIRM_OTP.name();
						isFoundState = true;
					}
					
				}
				
				/** UNKNOWN **/
				if (!isFoundState) {
					subState = SubStates.UNKNOWN.name();
				}
				
			}
			catch (Exception e) {
				/** UNKNOWN **/
				subState = SubStates.UNKNOWN.name();
			}
		}
		/** TEXT/PLAIN **/
		else if (rawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {
			boolean isFoundState = false;
			Pattern typePattern = null;
			Matcher typeMatcher = null;
			String urlSendOTPREQPattern = IdleMessageFormat.URL_SEND_OTP_REQ_PATTERN;
			String urlConfirmationPattern = IdleMessageFormat.URL_CONFIRMATION_PATTERN;
			String urlConfirmationWPKPattern = IdleMessageFormat.URL_CONFIRMATION_W_PK_PATTERN;
			String urlGeneratePKPattern = IdleMessageFormat.URL_GENERATE_PK_PATTERN;
			String urlAuthOTPPattern = IdleMessageFormat.URL_AUTH_OTP_PATTERN;
			String urlWSDLSendReqPattern = IdleMessageFormat.URL_WSDL_SEND_REQ_PATTERN;
			try {
				String url = rawData.getRawDataAttribute(EquinoxAttribute.URL);
				/** IDLE_SEND_OTP_REQ **/
				typePattern = Pattern.compile(urlSendOTPREQPattern);
				typeMatcher = typePattern.matcher(url);
				if (typeMatcher.matches()) {
					subState = SubStates.IDLE_SEND_OTP_REQ.name();
					isFoundState = true;
				}
				/** IDLE_CONFIRMATION **/
				typePattern = Pattern.compile(urlConfirmationPattern);
				typeMatcher = typePattern.matcher(url);
				if (typeMatcher.matches()) {
					subState = SubStates.IDLE_CONFIRMATION.name();
					isFoundState = true;
				}
				/** IDLE_CONFIRMATION_W_PK **/
				typePattern = Pattern.compile(urlConfirmationWPKPattern);
				typeMatcher = typePattern.matcher(url);
				if (typeMatcher.matches()) {
					subState = SubStates.IDLE_CONFIRMATION_W_PK.name();
					isFoundState = true;
				}
				/** IDLE_GENERATE_PK **/
				typePattern = Pattern.compile(urlGeneratePKPattern);
				typeMatcher = typePattern.matcher(url);
				if (typeMatcher.matches()) {
					subState = SubStates.IDLE_GENERATE_PK.name();
					isFoundState = true;
				}
				/** IDLE_AUTH_OTP **/
				typePattern = Pattern.compile(urlAuthOTPPattern);
				typeMatcher = typePattern.matcher(url);
				if (typeMatcher.matches()) {
					subState = SubStates.IDLE_AUTH_OTP.name();
					isFoundState = true;
				}
				/** IDLE_WSDL_SEND_REQ **/
				typePattern = Pattern.compile(urlWSDLSendReqPattern);
				typeMatcher = typePattern.matcher(url);
				if (typeMatcher.matches()) {
					subState = SubStates.IDLE_WSDL_SEND_REQ.name();
					isFoundState = true;
				}
				/** UNKNOWN **/
				if (!isFoundState) {
					subState = SubStates.UNKNOWN.name();
				}
			}
			catch (Exception e) {
				/** UNKNOWN **/
				subState = SubStates.UNKNOWN.name();
			}
		}
		/** UNKNOWN **/
		else {
			/** UNKNOWN **/
			subState = SubStates.UNKNOWN.name();
		}

		return subState;
	}

	private boolean checkCommand(String msgXML, String commandName) {
		
		boolean isCorrect= false;
			
		if (msgXML.contains("</"+commandName+">") || msgXML.contains(":"+commandName+">")) {
				isCorrect = true;
			}
		
		return isCorrect;
	}
	
	private void idleIDLEMANAGEMENTInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {

		this.rawDataIncoming = equinoxRawData;
		this.rawDatasOutgoing = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			/** INITIAL LOG **/
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		// ===============================================WRITE
		// DETAILS======================================================
		/** INITIAL LOG **/
		this.composeDetailsLog = new GssoComposeDetailsLog(appInstance, abstractAF.getEquinoxProperties().getState() + "." + "BEGIN",
				abstractAF);
		this.composeDetailsLog.setDataOrig(rawDataIncoming.getInvoke(), rawDataIncoming, appInstance);
		this.composeDetailsLog.thisUnknownState();
		this.mapDetails = new MapDetailsAndConfigType();
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		// ===============================================WRITE
		// SUMMARY======================================================
		/** INITIATE SUMMARY-LOG **/
		this.composeSummary = new GssoComposeSummaryLog(abstractAF, "");
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data) {
		// TODO Auto-generated method stub
		return null;
	}
}
