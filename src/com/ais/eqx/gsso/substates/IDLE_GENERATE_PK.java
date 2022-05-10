package com.ais.eqx.gsso.substates;

import java.util.ArrayList;
import java.util.HashMap;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.LogScenario;
import com.ais.eqx.gsso.enums.SoapResultCode;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GssoE01Datas;
import com.ais.eqx.gsso.instances.GssoGenPasskeyRequest;
import com.ais.eqx.gsso.instances.GssoOTPRequest;
import com.ais.eqx.gsso.instances.GssoServiceTemplate;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.SendOneTimePWRequest;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.GssoMessageType;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;
import com.ais.eqx.gsso.utils.GssoConstructMessage;
import com.ais.eqx.gsso.utils.GssoDataManagement;
import com.ais.eqx.gsso.utils.TimeoutManagement;
import com.ais.eqx.gsso.validator.VerifyMessage;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class IDLE_GENERATE_PK implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;

	private EquinoxRawData				rawDataIncoming;
	private ArrayList<EquinoxRawData>	rawDatasOutgoing;
	private AbstractAF					abstractAF;
	private String						nextState;

	private GssoGenPasskeyRequest		genPasskeyReq;
	private GssoOTPRequest				otpRequest;

	private JsonResultCode				jsonCode;
	private String						logDescription				= "";
	private String						path						= "";

	private String						destNodeResultCode			= "null";
	private String						destNodeResultDescription	= "null";
	private String						destNodeName				= "Client";
	private String						destNodeCommand				= EventLog.GENARATE_PASSKEY.getEventLog();
	private boolean						isNoFlow					= false;

	private GssoComposeDetailsLog		composeDetailsLog;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;
	private MapDetailsAndConfigType		mapDetails;

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		this.nextState = SubStates.IDLE_GENERATE_PK.toString();

		/************** INITIAL *****************/
		idleGenPKInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);

		/************** CODING ******************/

		/** VALID MESSAGE **/
		if (messageValidator(rawDataIncoming)) {
			writeLogSuccess(rawDataIncoming);

			/** EXTRACT GENPASSKEY **/
			this.genPasskeyReq = GssoDataManagement.extractGssoGenPasskeyRequest(rawDataIncoming);
			
			/*
			 * Set transactionID
			 */
			appInstance.getMapOrigInvokeTransactionID().put(equinoxRawData.getInvoke(), genPasskeyReq.getGeneratePasskey().getTransactionID());

			/** INITIAL INCOMEING **/
			String incomingMessageType = null;
			if (genPasskeyReq.getMessageType().equals(GssoMessageType.SOAP)) {
				incomingMessageType = IncomingMessageType.GENERATE_PASSKEY_SOAP.getMessageType();
			}
			else {
				incomingMessageType = IncomingMessageType.GENERATE_PASSKEY_JSON.getMessageType();
			}
			TimeoutManagement.initialIncomingOTPReq(appInstance, rawDataIncoming, incomingMessageType,
					appInstance.getTimeStampIncoming());

			/** SAVE TO INSTANCE **/
			this.otpRequest = updateOtpRequest();
			updateOrigInvokeProfile();
			
			/** SET DTAILS IDENTITY **/
			this.composeDetailsLog.setIdentity(this.genPasskeyReq.getGeneratePasskey().getService());
			
			// =========WRITE SUMMARY=======
			/** INITIATE SUMMARY-LOG **/
			this.composeSummary = new GssoComposeSummaryLog(abstractAF, this.genPasskeyReq.getGeneratePasskey().getService());
			// =========WRITE SUMMARY=======

			/* DO SERVICE TEMPLATE */
			if (appInstance.isInquirySubSuccess()||(appInstance.getProfile().getOper()!=null&&appInstance.getProfile().getOper().equals("INTER"))) {
				this.nextState = SubStates.W_SERVICE_TEMPLATE.toString();
				/*** CODING QUIRY E01 OR FOUND ST DO SEND EMAIL OR SMS ***/
				/* IF NOT FOUND SERVICE TEMPLATE DO QUIRY E01 */
				String service = this.otpRequest.getSendOneTimePW().getService().toUpperCase();
				HashMap<String, GssoE01Datas> mapE01dataofService = appInstance.getMapE01dataofService();
				GssoE01Datas gssoE01Datas = mapE01dataofService.get(service);
				if (mapE01dataofService == null || mapE01dataofService.size() <= 0) {
					if (gssoE01Datas == null || gssoE01Datas.getServiceTemplate() == null
							|| gssoE01Datas.getServiceTemplate().size() <= 0) {
						appInstance.getListInvokeProcessing().add(rawDataIncoming.getInvoke());

						GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, rawDataIncoming.getInvoke(), abstractAF,
								composeDebugLog);

						if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
							// =========== DEBUG LOG ==========
							/** writeLog LOG **/
							composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
							// ^^^^^^^^^^^ DEBUG LOG ^^^^^^^^^^
						}

					}
					/* FOUND SERVICE TEMPLATE DO SMS OR EMAIL */
					else {
						GssoOTPRequest otpRequest = appInstance.getMapOrigProfile().get(rawDataIncoming.getInvoke())
								.getGssoOTPRequest();
						GssoServiceTemplate thisServiceTemplate = GssoDataManagement.findServiceTemplateMatchAccountType(appInstance,
								otpRequest.getSendOneTimePW().getService(), appInstance.getProfile().getOper());

						/* ACCOUNT TYPE MATCH SERVICE TEMPLATE */
						boolean isFoundServiceTemplate = thisServiceTemplate != null;
						if (isFoundServiceTemplate) {
							writeGenPKSuccessLogAndStatistic(ec02Instance);

							/* compose gen passkey response success */
							rawDatasOutgoing.add(GssoConstructMessage.createGenpassResp(appInstance, rawDataIncoming,
									thisServiceTemplate, composeDebugLog, genPasskeyReq));

							/* REMOVE PROFILE */
							GssoDataManagement.removeProfile(rawDataIncoming.getInvoke(), appInstance);
						}
						else {
							isNoFlow = true;
						}
					}
				}
				else {
					if (gssoE01Datas == null || gssoE01Datas.getServiceTemplate() == null
							|| gssoE01Datas.getServiceTemplate().size() <= 0) {
						appInstance.getListInvokeProcessing().add(rawDataIncoming.getInvoke());

						GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, rawDataIncoming.getInvoke(), abstractAF,
								composeDebugLog);

						if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
							// =========== DEBUG LOG ==========
							/** writeLog LOG **/
							composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
							// ^^^^^^^^^^^ DEBUG LOG ^^^^^^^^^^
						}

					}
					/* FOUND SERVICE TEMPLATE DO SMS OR EMAIL */
					else {
						GssoOTPRequest otpRequest = appInstance.getMapOrigProfile().get(rawDataIncoming.getInvoke())
								.getGssoOTPRequest();
						GssoServiceTemplate thisServiceTemplate = GssoDataManagement.findServiceTemplateMatchAccountType(appInstance,
								otpRequest.getSendOneTimePW().getService(), appInstance.getProfile().getOper());

						/* ACCOUNT TYPE MATCH SERVICE TEMPLATE */
						boolean isFoundServiceTemplate = thisServiceTemplate != null;
						if (isFoundServiceTemplate) {
							writeGenPKSuccessLogAndStatistic(ec02Instance);

							/* compose gen passkey response success */
							rawDatasOutgoing.add(GssoConstructMessage.createGenpassResp(appInstance, rawDataIncoming,
									thisServiceTemplate, composeDebugLog, genPasskeyReq));

							/* REMOVE PROFILE */
							GssoDataManagement.removeProfile(rawDataIncoming.getInvoke(), appInstance);
						}
						else {
							isNoFlow = true;
						}
					}
				}

				if (isNoFlow) {
					String logDescription = "GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE";

					/** CREATE RES MESSAGE **/
					rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
							appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming,
							JsonResultCode.SERVICE_NOT_ALLOW, logDescription, path, composeDebugLog, composeSummary,
							SubStates.IDLE_GENERATE_PK.name()));

					writeGenPKServiceTemplateMissingLogAndStatistic(ec02Instance);

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfile(rawDataIncoming.getInvoke(), appInstance);
				}

			}
			/*  */
			else {

				/* IF APP DO InqSub or PortChk ADD NEW INVOKE TO WAITLIST */
//				if (appInstance.isWaitInquirySub() || appInstance.isWaitPortCheck()) {
					if (appInstance.isWaitInquirySub()) {
					appInstance.getListWaitInquirySub().add(rawDataIncoming.getInvoke());

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						// ===============================================DEBUG
						// LOG==========================================================
						/** writeLog LOG **/
						this.composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
						// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
						// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					}
				}
				/* DO InqSub IF First Messge SENT TO APP */
				else {
					this.nextState = SubStates.W_INQUIRY_VAS_SUB.toString();

					appInstance.getListInvokeProcessing().add(rawDataIncoming.getInvoke());

					/** CREATE REQ USMP MESSAGE **/
					this.rawDatasOutgoing.add(GssoConstructMessage.createInquirySubReqToUSMPMessage(rawDataIncoming, ec02Instance,
							otpRequest, composeDebugLog));

				}
			}
		}
		/** INVALID MESSAGE **/
		else {

			/** CREATE RES MESSAGE **/
			rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
					appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonCode,
					logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_GENERATE_PK.name()));

			// ===============================================SAVE
			// SUMMARY======================================================
			try {
				composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode, destNodeResultDescription);
				composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), rawDataIncoming.getInvoke());
			}
			catch (Exception e) {
				Log.e(e.getMessage());
			}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfile(rawDataIncoming.getInvoke(), appInstance);
		}

		/* SAVE LOG */
		idleGenPKSaveLog();

		return this.rawDatasOutgoing;
	}

	private void updateOrigInvokeProfile() {
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(rawDataIncoming.getInvoke());
		origInvokeProfile.setCmdName(EventLog.GENARATE_PASSKEY.getEventLog());
		origInvokeProfile.setScenarioName(LogScenario.GENARATE_PASSKEY.getLogScenario());
		origInvokeProfile.setGssoOTPRequest(this.otpRequest);
		origInvokeProfile.setTransactionID(this.genPasskeyReq.getGeneratePasskey().getTransactionID());
		origInvokeProfile.setDetailsService(this.genPasskeyReq.getGeneratePasskey().getService());
	}

	private GssoOTPRequest updateOtpRequest() {
		GssoOTPRequest otpRequest = new GssoOTPRequest();
		SendOneTimePWRequest sendOneTimePW = new SendOneTimePWRequest();
		sendOneTimePW.setMsisdn(this.genPasskeyReq.getGeneratePasskey().getMsisdn());
		sendOneTimePW.setService(this.genPasskeyReq.getGeneratePasskey().getService());
		otpRequest.setSendOneTimePW(sendOneTimePW);
		otpRequest.setMessageType(this.genPasskeyReq.getMessageType());
		return otpRequest;
	}

	// private SendOneTimePWRequest updateOrigInvokeProfile() {
	//
	// return sendOneTimePW;
	// }

	private void writeGenPKServiceTemplateMissingLogAndStatistic(EC02Instance ec02Instance) {
		ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_ERROR.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataIncoming.getInvoke(), EventLog.GENARATE_PASSKEY.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_ERROR.getStatistic());
			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		// ===============================================SAVE
		// SUMMARY======================================================
		try {
			composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode, destNodeResultDescription);
			composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), rawDataIncoming.getInvoke());
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	}

	private void writeGenPKSuccessLogAndStatistic(EC02Instance ec02Instance) {
		ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_SUCCESS.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataIncoming.getInvoke(), EventLog.GENARATE_PASSKEY.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_SUCCESS.getStatistic());
			composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		// ===============================================WRITE
		// SUMMARY======================================================
		if (rawDataIncoming.getCType().equalsIgnoreCase(EventCtype.XML)) {
			composeSummary
					.initialSummary(this.appInstance ,appInstance.getTimeStampIncoming(), rawDataIncoming.getInvoke(),
							EventLog.GENARATE_PASSKEY.getEventLog(), SoapResultCode.SUCCESS.getCode(),
							SoapResultCode.SUCCESS.getDescription());
		}
		else {
			composeSummary
					.initialSummary(this.appInstance ,appInstance.getTimeStampIncoming(), rawDataIncoming.getInvoke(),
							EventLog.GENARATE_PASSKEY.getEventLog(), JsonResultCode.SUCCESS.getCode(),
							JsonResultCode.SUCCESS.getDescription());
		}

		try {
			composeSummary.setWriteSummary();
			composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), rawDataIncoming.getInvoke());
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	}

	private boolean messageValidator(EquinoxRawData rawData) {
		boolean isMessageValid = false;

		try {
			VerifyMessage.verifyIDLE_GENERATE_PK_Req(rawData, appInstance);

			isMessageValid = true;

		}
		catch (ValidationException e) {
			isMessageValid = false;
			/** VERIFY GEN PK REQ ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_GENERATEPASSKEY_REQUEST.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.GENARATE_PASSKEY.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.GENARATE_PASSKEY.getLogScenario());

			try {
				/** Extract Message **/
				GssoGenPasskeyRequest genPasskeyReq = GssoDataManagement.extractGssoGenPasskeyRequest(rawData);
				
				/** SET DTAILS IDENTITY **/
				this.composeDetailsLog.setIdentity(genPasskeyReq.getGeneratePasskey().getService());
				
				// =========WRITE SUMMARY=======
				/** INITIATE SUMMARY-LOG **/
				this.composeSummary = new GssoComposeSummaryLog(abstractAF, genPasskeyReq.getGeneratePasskey().getService());
				// =========WRITE SUMMARY=======
			}
			catch (Exception e2) {
				/** SET DTAILS IDENTITY **/
				this.composeDetailsLog.setIdentity("unknown");
			}
			
			try {
				this.composeDetailsLog.initialIncoming(rawData, appInstance);
				this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
			}
			catch (Exception ex) {
				Log.e(ex.getMessage());
			}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			this.jsonCode = e.getJsonResultCode();
			this.logDescription = e.getMessage();
			this.path = e.getMandatoryPath();
			this.destNodeResultDescription = this.path + " " + this.logDescription;

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// ===============================================DEBUG
				// LOG==========================================================
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_GENERATEPASSKEY_REQUEST.getStatistic());
				this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				this.composeDebugLog.setFailureAvp(this.path + " " + this.logDescription);
				this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_ERROR.getStatistic());
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}

			/** SEND RESP ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_ERROR.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(rawData.getInvoke(), EventLog.GENARATE_PASSKEY.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return isMessageValid;
	}

	private void writeLogSuccess(EquinoxRawData rawData) {
		/** VALID GEN PK REQ STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_GENERATEPASSKEY_REQUEST.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.GENARATE_PASSKEY.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.GENARATE_PASSKEY.getLogScenario());

		try {
			this.composeDetailsLog.initialIncoming(rawData, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_GENERATEPASSKEY_REQUEST.getStatistic());
			this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}
	}

	private void idleGenPKInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {

		this.abstractAF = abstractAF;
		this.rawDataIncoming = equinoxRawData;
		this.rawDatasOutgoing = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
		
		boolean isInquiryVasSubscriber = Boolean.parseBoolean(ConfigureTool.getConfigureBoolean(ConfigName.INQUIRYVASSUBSCRIBER));
		this.appInstance.setInquiryVasSubscriber(isInquiryVasSubscriber);

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
		this.composeDetailsLog.thisIdleState();
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

	private void idleGenPKSaveLog() {

		// ===============================================WRITE
		// DETAILS======================================================
		int outPutSize = this.rawDatasOutgoing.size();
		for (final EquinoxRawData rawDataOut : this.rawDatasOutgoing) {
			try {
				this.composeDetailsLog.initialOutgoing(rawDataOut, appInstance, outPutSize);
			}
			catch (Exception e) {
				Log.e(e.getMessage());
			}
		}
		try {
			this.composeDetailsLog.initialOutgoingToE01(abstractAF, appInstance);
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}

		mapDetails.setDetail(this.composeDetailsLog.getDetailsLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		// ===============================================SAVE
		// DETAILS======================================================
		appInstance.getListDetailsLog().add(mapDetails);
		// ===============================================SAVE
		// SUMMARY======================================================
		if (this.composeSummary.isWriteSummary()) {
			appInstance.getListSummaryLog().add(this.composeSummary.getSummaryLog());
		}

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================WRITE DEBUG
			// LOG===================================================
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
