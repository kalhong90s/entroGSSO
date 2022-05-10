package com.ais.eqx.gsso.substates;

import java.util.ArrayList;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.LogScenario;
import com.ais.eqx.gsso.enums.SoapResultCode;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.interfaces.EQX;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.interfaces.MessageResponsePrefix;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;
import com.ais.eqx.gsso.utils.GssoConstructMessage;
import com.ais.eqx.gsso.utils.GssoGenerator;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class UNKNOWN implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;

	private EquinoxRawData				rawDataIncoming;
	private ArrayList<EquinoxRawData>	rawDatasOut;
	private String						nextState;

	private String						code;
	private String						description;

	private String						destNodeResultCode			= "null";
	private String						destNodeResultDescription	= "null";
	private String						destNodeName				= "null";
	private String						destNodeCommand				= EventLog.NULL_UNKNOWN.getEventLog();

	private GssoComposeDetailsLog		composeDetailsLog;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;
	private MapDetailsAndConfigType		mapDetails;

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		this.nextState = SubStates.UNKNOWN.toString();
		/** UNKNOWN **/

		/************** INITIAL *****************/
		unknownInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);

		/************** CODING ******************/
		// System.out.println("Start UNKNOWN");

		createMessage(rawDataIncoming);

		/* SAVE LOG */
		unknownSaveLog();

		return this.rawDatasOut;
	}

	private void createMessage(EquinoxRawData rawData) {
		/** GSSO Received UnknownMessage Request **/
		String origInvoke = rawData.getInvoke();
		
		// ===============================================WRITE
		// DETAILS======================================================

		try {
			if(appInstance.isTimeoutOfConfirmReq()){
				appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.CONFIRM_OTP.getEventLog());
				appInstance.getMapOrigInvokeDetailScenario().put(origInvoke, LogScenario.CONFIRM_OTP.getLogScenario());
				
				this.destNodeName = "Client";
				this.destNodeResultCode = "null"; // ResultCode From Destination Node If Not Set "null"
				this.destNodeCommand = EventLog.CONFIRM_OTP.getEventLog();
				this.destNodeResultDescription = "Timeout"; // Description From Destination Node If Not Like Timeout Set Timeout Description
			}
			else{
				ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_UNKNOWN_MESSAGE.getStatistic());
				appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.NULL_UNKNOWN.getEventLog());
				
				this.destNodeResultCode = "null"; // ResultCode From Destination Node If Not Set "null"
				this.destNodeCommand = EventLog.NULL_UNKNOWN.getEventLog();
				this.destNodeResultDescription = "Unknown"; // Description From Destination Node If Not Like Timeout Set Timeout Description
			}
			
			this.composeDetailsLog.initialIncoming(rawData, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (rawData.getType().equalsIgnoreCase(EventAction.REQUEST) && !appInstance.isTimeoutOfConfirmReq()) {
			
			String resCode = "";
			String resDes = "";
			String rootElement = MessageResponsePrefix.UNKNOWN_REQUEST;
			String orderRef = "";
			
			orderRef = GssoGenerator.generateOrderReference(ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME),
					this.appInstance.getListOrderReference());
			
			EquinoxRawData output = null;
			output = new EquinoxRawData();
//			output = rawData;
			output.setType(EventAction.RESPONSE);
			output.setTo(rawData.getOrig());
			output.setInvoke(rawData.getInvoke());
			output.setName(rawData.getName());
			output.setCType(rawData.getCType());
			output.addRawDataAttribute(EquinoxAttribute.METHOD, rawData.getRawDataAttribute(EquinoxAttribute.METHOD));
			output.addRawDataAttribute(EquinoxAttribute.URL, rawData.getRawDataAttribute(EquinoxAttribute.URL));
			
			try {
				output.removeAttr("orig");
			}
			catch (Exception e) {
				Log.e(e.getMessage());
			}
			
			if (EQX.Ctype.TEXTPLAIN.equalsIgnoreCase(rawData.getCType())) {

				StringBuilder jsonResp = new StringBuilder();
				resCode = JsonResultCode.WRONG_INPUT_PARAMETER.getCode();
				resDes = JsonResultCode.WRONG_INPUT_PARAMETER.getDescription();

				jsonResp.append("{\"" + rootElement + "\":");
				jsonResp.append("{\"code\":\"" + resCode + "\",");
				jsonResp.append("\"description\":\"" + resDes + "\",");
				jsonResp.append("\"isSuccess\":\"false\",");
				jsonResp.append("\"orderRef\":\"" + orderRef + "\"");
				jsonResp.append("}").append("}");
				
				try {
					output.removeAttr(EquinoxAttribute.VAL);
					output.setRawMessage("");
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
				output.addRawDataAttribute(EquinoxAttribute.VAL, jsonResp.toString());
			}
			else if (EQX.Ctype.TEXTXML.equalsIgnoreCase(rawData.getCType())) {

				resCode = SoapResultCode.WS_WRONG_INPUT_PARAMETER.getCode();
				resDes = SoapResultCode.WS_WRONG_INPUT_PARAMETER.getDescription();
				
				String soapOut = GssoConstructMessage.createSoapOut(null, rawDataIncoming, resCode, resDes, rootElement, orderRef, "", false,
						false, false);

				output.setRawMessage(soapOut);
			}
			
			this.rawDatasOut.add(output);

			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_UNKNOWN_MESSAGE_RESPONSE_ERROR.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.NULL_UNKNOWN.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_UNKNOWN_MESSAGE_RESPONSE_ERROR.getStatistic());
			}
		}
		else {

		}

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			/** writeLog LOG **/
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_UNKNOWN_MESSAGE.getStatistic());
			this.composeDebugLog.initialGssoSubStateLog(rawData);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		// ===============================================WRITE
		// SUMMARY======================================================
		this.composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode, destNodeResultDescription); // Add When Message Response

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
	}

	private void unknownInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {	
		
		this.rawDatasOut = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
		
		this.rawDataIncoming = equinoxRawData;
		
		if(this.appInstance.isTransaction()){
			this.rawDataIncoming.setInvoke("invoke_unknown");
		}
		
		/* FIX RET */
		if (rawDataIncoming.getRet().isEmpty()) {
			rawDataIncoming.setRet("0");
		}	
		
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
		this.composeDetailsLog.setIdentity("Unknown");

		this.composeDetailsLog.setDataOrig(equinoxRawData.getInvoke(), equinoxRawData, appInstance);
		this.composeDetailsLog.thisUnknownState();
		this.mapDetails = new MapDetailsAndConfigType();
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		// ===============================================WRITE
		// SUMMARY======================================================
		/** INITIATE SUMMARY-LOG **/
		this.composeSummary = new GssoComposeSummaryLog(abstractAF, appInstance.isTimeoutOfConfirmReq() ? "Unknown" : "");
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	}

	private void unknownSaveLog() {
		// ===============================================WRITE
		// DETAILS======================================================
		int outPutSize = this.rawDatasOut.size();
		for (EquinoxRawData rawDataOut : this.rawDatasOut) {
			try {
				this.composeDetailsLog.initialOutgoing(rawDataOut, appInstance, outPutSize);
			}
			catch (Exception e) {
				Log.e(e.getMessage());
			}
		}
		mapDetails.setDetail(this.composeDetailsLog.getDetailsLog());
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
			// ===============================================WRITE DEBUG
			// LOG===================================================
			/** writeLog LOG **/
			this.composeDebugLog.writeDebugSubStateLog();
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}
	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data) {
		// TODO Auto-generated method stub
		return null;
	}
}
