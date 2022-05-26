package com.ais.eqx.gsso.substates;

import java.util.ArrayList;
import java.util.HashMap;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.GssoCommand;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.LogScenario;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GssoE01Datas;
import com.ais.eqx.gsso.instances.GssoServiceTemplate;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.SendWSOTPRequest;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.GssoMessageType;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;
import com.ais.eqx.gsso.utils.GssoConstructMessage;
import com.ais.eqx.gsso.utils.GssoDataManagement;
import com.ais.eqx.gsso.utils.GssoGenerator;
import com.ais.eqx.gsso.utils.InvokeFilter;
import com.ais.eqx.gsso.utils.TimeoutManagement;
import com.ais.eqx.gsso.validator.VerifyMessage;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class IDLE_WS_CREATE_OTP implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;

	private EquinoxRawData				rawDataIncoming;
	private ArrayList<EquinoxRawData>	rawDatasOutgoing;
	private AbstractAF					abstractAF;
	private String						nextState;

	private SendWSOTPRequest			sendWSOTPRequest;
	private JsonResultCode				jsonResultCode;
	private String						logDescription				= "";
	private String						path						= "";

	private String						destNodeResultCode			= "null";
	private String						destNodeResultDescription	= "null";
	private String						destNodeName				= "Client";
	private String						destNodeCommand				= EventLog.WS_CREATE_OTP.getEventLog();
	private boolean						isNoFlow					= false;

	private GssoComposeDetailsLog		composeDetailsLog;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;
	private MapDetailsAndConfigType		mapDetails;
	private GssoServiceTemplate			thisServiceTemplate;

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		this.nextState = SubStates.IDLE_WS_CREATE_OTP.toString();

		/************** INITIAL *****************/
		idleWSCreateOTPInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);

		/************** CODING ******************/
//		System.out.println("Start IDLE_WS_CREATE_OTP");

		/** VALID MESSAGE **/
		if (messageValidator(rawDataIncoming)) {
			writeLogSuccess(rawDataIncoming);
			
			/** INITIAL INCOMEING **/
			String incomingMessageType = null;
			
			/** EXTRACT OTP REQUEST **/
			this.sendWSOTPRequest = GssoDataManagement.extractGssoWSCreateOTPRequest(rawDataIncoming);
			
			/** INITIAL INCOMEING **/
			incomingMessageType = IncomingMessageType.WS_CREATE_OTP_SOAP.getMessageType();
			TimeoutManagement.initialIncomingOTPReq(appInstance, rawDataIncoming, incomingMessageType,
					appInstance.getTimeStampIncoming());

			/** SAVE TO INSTANCE **/
			OrigInvokeProfile origInvokeProfile = updateOrigInvokeProfile();
			
			/** SET DTAILS IDENTITY **/
			this.composeDetailsLog.setIdentity(origInvokeProfile.getDetailsService());

			// =========WRITE SUMMARY=======
			/** INITIATE SUMMARY-LOG **/
			this.composeSummary = new GssoComposeSummaryLog(abstractAF, origInvokeProfile.getDetailsService());
			// =========WRITE SUMMARY=======
			
			/* DO SERVICE TEMPLATE */
			if (appInstance.isInquirySubSuccess()) {
				appInstance.getListInvokeProcessing().add(rawDataIncoming.getInvoke());

				/*** CODING QUIRY E01 OR FOUND ST DO SEND EMAIL OR SMS ***/
				/* IF NOT FOUND SERVICE TEMPLATE DO QUIRY E01 */
				String service = this.sendWSOTPRequest.getService().toUpperCase();
				HashMap<String, GssoE01Datas> mapE01dataofService = appInstance.getMapE01dataofService();
				GssoE01Datas gssoE01Datas = mapE01dataofService.get(service);
				if (mapE01dataofService == null || mapE01dataofService.size() <= 0) {
					if (gssoE01Datas == null || gssoE01Datas.getServiceTemplate() == null
							|| gssoE01Datas.getServiceTemplate().size() <= 0) {
						/** PermissionAccountType is Allow **/
						if (GssoDataManagement.checkPermissionAccountType(origInvokeProfile, appInstance)) {

							GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, rawDataIncoming.getInvoke(), abstractAF,
									composeDebugLog);

							if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
								// =========== DEBUG LOG ==========
								/** writeLog LOG **/
								composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
								// ^^^^^^^^^^^ DEBUG LOG ^^^^^^^^^^
							}

						}
						/** PermissionAccountType is not Allow **/
						else {
							isNoFlow = true;
						}
					}
					else {
						/* FOUND SERVICE TEMPLATE DO SMS OR EMAIL */
						/** PermissionAccountType is Allow **/
						if (GssoDataManagement.checkPermissionAccountType(origInvokeProfile, appInstance)) {
							smsOrEmailFlow(rawDataIncoming);
						}
						/** PermissionAccountType is not Allow **/
						else {
							isNoFlow = true;
						}
					}
				}
				else {
					/* FOUND SERVICE TEMPLATE DO SMS OR EMAIL */
					if (gssoE01Datas == null || gssoE01Datas.getServiceTemplate() == null
							|| gssoE01Datas.getServiceTemplate().size() <= 0) {
						/** PermissionAccountType is Allow **/
						if (GssoDataManagement.checkPermissionAccountType(origInvokeProfile, appInstance)) {

							GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, rawDataIncoming.getInvoke(), abstractAF,
									composeDebugLog);

							if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
								// =========== DEBUG LOG ==========
								/** writeLog LOG **/
								composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
								// ^^^^^^^^^^^ DEBUG LOG ^^^^^^^^^^
							}

						}
						/** PermissionAccountType is not Allow **/
						else {
							isNoFlow = true;
						}
					}
					else {
						/* FOUND SERVICE TEMPLATE DO SMS OR EMAIL */
						/** PermissionAccountType is Allow **/
						if (GssoDataManagement.checkPermissionAccountType(origInvokeProfile, appInstance)) {
							smsOrEmailFlow(rawDataIncoming);
						}
						/** PermissionAccountType is not Allow **/
						else {
							isNoFlow = true;
						}
					}
				}

				if (isNoFlow) {
					rawDatasOutgoing.add(GssoDataManagement.accountTypeIsNotAllow(origInvokeProfile.getOrigEquinoxRawData()
							.getInvoke(), appInstance, rawDataIncoming, jsonResultCode, logDescription, path, composeDebugLog,
							composeSummary, SubStates.IDLE_WS_CREATE_OTP.name()));

					/** GSSO Return CreateOnetimePassword Response Error STATICTIC **/
					ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());

					// ===============================================SAVE
					// SUMMARY======================================================
					try {
						composeSummary
								.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode, destNodeResultDescription);
						composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), rawDataIncoming.getInvoke());
					}
					catch (Exception e) {
						Log.e(e.getMessage());
					}
					// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
					// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				}

			}
			/* PROFILE NOT FOUND */
			else {
				/* IF APP DO InqSub or PortChk ADD NEW INVOKE TO WAITLIST */
//				if (appInstance.isWaitInquirySub() || appInstance.isWaitPortCheck()) {
				if (appInstance.isWaitInquirySub()) {

					appInstance.getListWaitInquirySub().add(rawDataIncoming.getInvoke());

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						// ===============================================DEBUG
						// LOG==========================================================
						/** writeLog LOG **/
						this.composeDebugLog.initialGssoSubStateLog(equinoxRawData);
						// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
						// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					}
				}
				/* DO InqSub IF First Messge SENT TO APP */
				else {

					appInstance.getListInvokeProcessing().add(rawDataIncoming.getInvoke());
						
					/** CREATE REQ USMP MESSAGE **/
					this.rawDatasOutgoing.add(GssoConstructMessage.createInquirySubReqToUSMPMessageFromWSCreateOTP(rawDataIncoming, ec02Instance,
							sendWSOTPRequest, composeDebugLog));

				}
			}
		}
		/** INVALID MESSAGE **/
		else {

			/** CREATE RES MESSAGE **/
			rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
					appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonResultCode,
					logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_WS_CREATE_OTP.name()));

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
		idleWSCreateOTPSaveLog();

		return this.rawDatasOutgoing;
	}

	private OrigInvokeProfile updateOrigInvokeProfile() {
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(rawDataIncoming.getInvoke());
			origInvokeProfile.setCmdName(EventLog.WS_CREATE_OTP.getEventLog());
			origInvokeProfile.setScenarioName(LogScenario.WS_CREATE_OTP.getLogScenario());
			origInvokeProfile.setDetailsService(this.sendWSOTPRequest.getService());
			origInvokeProfile.setSendWSOTPRequest(this.sendWSOTPRequest);
			origInvokeProfile.setGssoOrigCommand(GssoCommand.WS_CREAT_OTP);
			origInvokeProfile.setMessageType(GssoMessageType.SOAP);
		return origInvokeProfile;
	}

	private boolean messageValidator(EquinoxRawData rawData) {
		boolean isMessageValid = false;

		try {
			VerifyMessage.verifyIDLE_WS_CREATE_OTP_Req(rawData, appInstance);

			isMessageValid = true;

		}
		catch (ValidationException e) {
			isMessageValid = false;
			/** VERIFY OTP REQ ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_CREATEONETIMEPW_REQUEST.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.WS_CREATE_OTP.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.WS_CREATE_OTP.getLogScenario());

			try {
				/** Extract Message **/
				SendWSOTPRequest sendWSOTPRequest = GssoDataManagement.extractGssoWSCreateOTPRequest(rawData);
				
				/** SET DTAILS IDENTITY **/
				this.composeDetailsLog.setIdentity(sendWSOTPRequest.getService());
				
				// =========WRITE SUMMARY=======
				/** INITIATE SUMMARY-LOG **/
				this.composeSummary = new GssoComposeSummaryLog(abstractAF, sendWSOTPRequest.getService());
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

			this.jsonResultCode = e.getJsonResultCode();
			this.logDescription = e.getMessage();
			this.path = e.getMandatoryPath();
			this.destNodeResultDescription = this.path + " " + this.logDescription;

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// ===============================================DEBUG
				// LOG==========================================================
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_CREATEONETIMEPW_REQUEST.getStatistic());
				this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				this.composeDebugLog.setFailureAvp(this.path + " " + this.logDescription);
				if (this.jsonResultCode == JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION) {
					this.composeDebugLog.transactionIsOverLimit();
				}
				this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}

			/** SEND RESP ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(rawData.getInvoke(), EventLog.WS_CREATE_OTP.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return isMessageValid;
	}

	private void writeLogSuccess(EquinoxRawData rawData) {

		/** VALID OTP REQ STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_CREATEONETIMEPW_REQUEST.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.WS_CREATE_OTP.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.WS_CREATE_OTP.getLogScenario());

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
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_CREATEONETIMEPW_REQUEST.getStatistic());
			this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

	}

	private void smsOrEmailFlow(EquinoxRawData rawData) {
		String origInvoke = rawDataIncoming.getInvoke();

		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		
		/** FIND SERVICE TEMPLATE **/
		String oper = appInstance.getProfile().getOper();
		SendWSOTPRequest sendWSAuthOTPRequest = origInvokeProfile.getSendWSOTPRequest();
		thisServiceTemplate = GssoDataManagement.findServiceTemplateMatchAccountType(appInstance, sendWSAuthOTPRequest.getService(), oper);
		
		/** WS_CREATE_OTP_SOAP no have otp chanel **/
		
		/** CHOOSE LIFE TIMEOUT MIN **/
		GssoDataManagement.chooseDefaultValuesWSCommand(sendWSAuthOTPRequest, thisServiceTemplate);

		/* ACCOUNT TYPE MATCH SERVICE TEMPLATE */
		boolean isFoundServiceTemplate = thisServiceTemplate != null;
		if (isFoundServiceTemplate) {

			/* CREATE TRANSACTION ID PROFILE */
			origInvokeProfile.setTransactionID(GssoDataManagement.createNewTransaction(appInstance, origInvoke,
					thisServiceTemplate.getSeedkey()));
			
			origInvokeProfile.setServiceKey(appInstance.getMapE01dataofService().get(sendWSAuthOTPRequest.getService().toUpperCase())
					.getServiceKey());

			origInvokeProfile.setOrderRefLog(GssoGenerator.generateOrderReference(
					ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME), appInstance.getListOrderReference()));

			/** FOR SMS **/
			if(origInvokeProfile.isBypassUSMP()){
				rawDatasOutgoing.addAll(GssoConstructMessage.createSMSReqMessageV2(origInvoke, thisServiceTemplate, ec02Instance,composeDebugLog));

			}else {
				rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,composeDebugLog));
			}


			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// ===============================================DEBUG
				// LOG==========================================================
				this.composeDebugLog.initialGssoSubStateLog(rawData);
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}
		}
		else {
			this.jsonResultCode = JsonResultCode.SERVICE_NOT_ALLOW;
			this.logDescription = "GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE";

			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.WS_CREATE_OTP.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// ===============================================DEBUG
				// LOG==========================================================
				/** writeLog LOG **/
				this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}

			/** CREATE RES MESSAGE **/
			rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
					appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonResultCode,
					logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_WS_CREATE_OTP.name()));

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
			GssoDataManagement.removeProfile(origInvoke, appInstance);
		}
	}

	private void idleWSCreateOTPInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {
		this.rawDataIncoming = equinoxRawData;
		this.rawDatasOutgoing = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
		this.abstractAF = abstractAF;
		
		boolean isInquiryVasSubscriber = Boolean.parseBoolean(ConfigureTool.getConfigureBoolean(ConfigName.INQUIRYVASSUBSCRIBER));
		this.appInstance.setInquiryVasSubscriber(isInquiryVasSubscriber);

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
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

	private void idleWSCreateOTPSaveLog() {
		// ===============================================WRITE
		// DETAILS======================================================
		int outPutSize = this.rawDatasOutgoing.size();
		for (EquinoxRawData rawDataOut : this.rawDatasOutgoing) {
			try {
				this.composeDetailsLog.initialOutgoing(rawDataOut, appInstance, outPutSize);

				/** IF SMS **/
				String origInvoke = InvokeFilter.getOriginInvoke(rawDataOut.getInvoke());
				String subState = InvokeFilter.getSubState(rawDataOut.getInvoke());
				if (subState != null && subState.equals(SubStates.W_SEND_SMS.name())) {
					OrigInvokeProfile origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvoke);
					origInvokeProfile.setSubmitSmRequestTime(this.composeDetailsLog.getDetailTimeOutgoing());
				}
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