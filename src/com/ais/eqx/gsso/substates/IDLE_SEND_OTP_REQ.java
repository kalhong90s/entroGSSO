package com.ais.eqx.gsso.substates;

import com.ais.eqx.gsso.enums.*;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.*;
import com.ais.eqx.gsso.interfaces.*;
import com.ais.eqx.gsso.utils.*;
import com.ais.eqx.gsso.validator.VerifyMessage;
import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class IDLE_SEND_OTP_REQ implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;

	private EquinoxRawData				rawDataIncoming;
	private ArrayList<EquinoxRawData>	rawDatasOutgoing;
	private AbstractAF					abstractAF;
	private String						nextState;

	private GssoOTPRequest				otpRequest;
	private JsonResultCode				jsonResultCode;
	private String						logDescription				= "";
	private String						path						= "";

	private String						destNodeResultCode			= "null";
	private String						destNodeResultDescription	= "null";
	private String						destNodeName				= "Client";
	private String						destNodeCommand				= EventLog.SEND_OTP.getEventLog();
	private boolean						isNoFlow					= false;

	private GssoComposeDetailsLog		composeDetailsLog;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;
	private MapDetailsAndConfigType		mapDetails;
	private GssoServiceTemplate			thisServiceTemplate;

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		this.nextState = SubStates.IDLE_SEND_OTP_REQ.toString();

		/************** INITIAL *****************/
		idleSendOTPInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);


		/************** CODING ******************/
//		System.out.println("Start IDLE_SEND_OTP_REQ");

		/** VALID MESSAGE **/
		if (messageValidator(rawDataIncoming)) {
			writeLogSuccess(rawDataIncoming);

//			String messageXML = rawDataIncoming.getRawDataMessage();

			/** INITIAL INCOMEING **/
			String incomingMessageType = null;

			/** EXTRACT OTP REQUEST **/
			this.otpRequest = GssoDataManagement.extractGssoOTPRequest(rawDataIncoming);

			/** INITIAL INCOMEING **/
			if (otpRequest.getMessageType().equals(GssoMessageType.SOAP)) {
				incomingMessageType = IncomingMessageType.SEND_OTP_SOAP.getMessageType();
			} else {
				incomingMessageType = IncomingMessageType.SEND_OTP_JSON.getMessageType();
			}

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

			String service = this.otpRequest.getSendOneTimePW().getService().toUpperCase();
			HashMap<String, GssoE01Datas> mapE01dataofService = appInstance.getMapE01dataofService();
			GssoE01Datas gssoE01Datas = mapE01dataofService.get(service);

			/** SERVICE IS BYPASS **/
			if(Arrays.asList(GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.USMP_BY_PASS_CONFIG_SERVICE_LIST).toUpperCase())).contains(service)){
				appInstance.getListInvokeProcessing().add(rawDataIncoming.getInvoke());

				/*** CODING QUIRY E01 OR FOUND ST DO SEND EMAIL OR SMS ***/
				/* IF NOT FOUND SERVICE TEMPLATE DO QUIRY E01 */
				if (mapE01dataofService == null || mapE01dataofService.size() <= 0) {
					if (gssoE01Datas == null || gssoE01Datas.getServiceTemplate() == null
							|| gssoE01Datas.getServiceTemplate().size() <= 0) {
						GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, rawDataIncoming.getInvoke(), abstractAF,
								composeDebugLog);

						if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
							// =========== DEBUG LOG ==========
							/** writeLog LOG **/
							composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
							// ^^^^^^^^^^^ DEBUG LOG ^^^^^^^^^^
						}
					} else {
						/* FOUND SERVICE TEMPLATE DO SMS OR EMAIL */
						smsOrEmailFlow(rawDataIncoming);

					}
				} else {
					/* FOUND SERVICE TEMPLATE DO SMS OR EMAIL */
					if (gssoE01Datas == null || gssoE01Datas.getServiceTemplate() == null
							|| gssoE01Datas.getServiceTemplate().size() <= 0) {
						GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, rawDataIncoming.getInvoke(), abstractAF,
								composeDebugLog);

						if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
							// =========== DEBUG LOG ==========
							/** writeLog LOG **/
							composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
							// ^^^^^^^^^^^ DEBUG LOG ^^^^^^^^^^
						}
					} else {
						/* FOUND SERVICE TEMPLATE DO SMS OR EMAIL */
						smsOrEmailFlow(rawDataIncoming);

					}
				}

			}/* DO SERVICE TEMPLATE */
			else if (appInstance.isInquirySubSuccess() ||
					(appInstance.getProfile().getOper() != null && appInstance.getProfile().getOper().equals("INTER"))) {
				appInstance.getListInvokeProcessing().add(rawDataIncoming.getInvoke());

				/*** CODING QUIRY E01 OR FOUND ST DO SEND EMAIL OR SMS ***/
				/* IF NOT FOUND SERVICE TEMPLATE DO QUIRY E01 */
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
					} else {
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
				} else {
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
					} else {
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
							composeSummary, SubStates.IDLE_SEND_OTP_REQ.name()));

					/** GSSO Return SendOnetimePassword Response Error STATICTIC **/
					ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());

					// ===============================================SAVE
					// SUMMARY======================================================
					try {
						composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode, destNodeResultDescription);
						composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), rawDataIncoming.getInvoke());
					} catch (Exception e) {
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
					this.rawDatasOutgoing.add(GssoConstructMessage.createInquirySubReqToUSMPMessage(rawDataIncoming, ec02Instance,
							otpRequest, composeDebugLog));

				}
			}
		}
		/** INVALID MESSAGE **/
		else {

			/** CREATE RES MESSAGE **/
			rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
					appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonResultCode,
					logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_SEND_OTP_REQ.name()));

			// ===============================================SAVE
			// SUMMARY======================================================
			try {
				composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode, destNodeResultDescription);
				composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), rawDataIncoming.getInvoke());
			} catch (Exception e) {
				Log.e(e.getMessage());
			}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfile(rawDataIncoming.getInvoke(), appInstance);
		}

		/* SAVE LOG */
		idleSendOTPSaveLog();

		return this.rawDatasOutgoing;
	}

	private OrigInvokeProfile updateOrigInvokeProfile() {
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(rawDataIncoming.getInvoke());

		origInvokeProfile.setCmdName(EventLog.SEND_OTP.getEventLog());
		origInvokeProfile.setScenarioName(LogScenario.SEND_OTP.getLogScenario());
		origInvokeProfile.setGssoOTPRequest(this.otpRequest);
		origInvokeProfile.setDetailsService(this.otpRequest.getSendOneTimePW().getService());

		return origInvokeProfile;
	}

	private boolean messageValidator(EquinoxRawData rawData) {
		boolean isMessageValid = false;

		try {
			VerifyMessage.verifyIDLE_OTP_Req(rawData, appInstance);

			isMessageValid = true;

		} catch (ValidationException e) {
			isMessageValid = false;
			/** VERIFY OTP REQ ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_SENDONETIMEPASSWORD_REQUEST.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.SEND_OTP.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.SEND_OTP.getLogScenario());

			try {
				/** Extract Message **/
				GssoOTPRequest otpRequest = GssoDataManagement.extractGssoOTPRequest(rawData);

				/** SET DTAILS IDENTITY **/
				this.composeDetailsLog.setIdentity(otpRequest.getSendOneTimePW().getService());

				// =========WRITE SUMMARY=======
				/** INITIATE SUMMARY-LOG **/
				this.composeSummary = new GssoComposeSummaryLog(abstractAF, otpRequest.getSendOneTimePW().getService());
				// =========WRITE SUMMARY=======

			} catch (Exception e2) {
				/** SET DTAILS IDENTITY **/
				this.composeDetailsLog.setIdentity("unknown");
			}

			try {
				this.composeDetailsLog.initialIncoming(rawData, appInstance);
				this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
			} catch (Exception ex) {
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
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_SENDONETIMEPASSWORD_REQUEST.getStatistic());
				this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				this.composeDebugLog.setFailureAvp(this.path + " " + this.logDescription);
				if (this.jsonResultCode == JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION) {
					this.composeDebugLog.transactionIsOverLimit();
				}
				this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}

			/** SEND RESP ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(rawData.getInvoke(), EventLog.SEND_OTP.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return isMessageValid;
	}

	private void writeLogSuccess(EquinoxRawData rawData) {

		/** VALID OTP REQ STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_SENDONETIMEPASSWORD_REQUEST.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.SEND_OTP.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.SEND_OTP.getLogScenario());

		try {
			this.composeDetailsLog.initialIncoming(rawData, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
		} catch (Exception e) {
			Log.e(e.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_SENDONETIMEPASSWORD_REQUEST.getStatistic());
			this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

	}

	private void smsOrEmailFlow(EquinoxRawData rawData) {
		String origInvoke = rawDataIncoming.getInvoke();

		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		GssoOTPRequest otpRequest = origInvokeProfile.getGssoOTPRequest();

		/** FIND SERVICE TEMPLATE **/
		String oper = appInstance.getProfile().getOper();
		SendOneTimePWRequest sendOneTimePW = otpRequest.getSendOneTimePW();
		thisServiceTemplate = GssoDataManagement.findServiceTemplateMatchAccountType(appInstance, sendOneTimePW.getService(), oper);

		String otpChannel = sendOneTimePW.getOtpChannel();

		/** CHOOSE LIFE TIMEOUT MIN **/
		GssoDataManagement.chooseDefaultValues(otpRequest, thisServiceTemplate);

		/* ACCOUNT TYPE MATCH SERVICE TEMPLATE */
		boolean isFoundServiceTemplate = thisServiceTemplate != null;
		if (isFoundServiceTemplate) {

			/* CREATE TRANSACTION ID PROFILE */
			origInvokeProfile.setTransactionID(GssoDataManagement.createNewTransaction(appInstance, origInvoke,
					thisServiceTemplate.getSeedkey()));

			origInvokeProfile.setServiceKey(appInstance.getMapE01dataofService().get(sendOneTimePW.getService().toUpperCase())
					.getServiceKey());

			origInvokeProfile.setOrderRefLog(GssoGenerator.generateOrderReference(
					ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME), appInstance.getListOrderReference()));

			/** FOR SMS **/
			if (otpChannel.equalsIgnoreCase(OTPChannel.SMS)) {

				rawDatasOutgoing.addAll(GssoConstructMessage.createSMSReqMessageV2(origInvoke, thisServiceTemplate, ec02Instance,
						composeDebugLog));

			}
			/** FOR EMAIL **/
			else if (otpChannel.equalsIgnoreCase(OTPChannel.EMAIL)) {

				rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
						composeDebugLog));

			}
			/** FOR ALL **/
			else {

				rawDatasOutgoing.addAll(GssoConstructMessage.createSMSReqMessageV2(origInvoke, thisServiceTemplate, ec02Instance,
						composeDebugLog));

				rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
						composeDebugLog));

			}

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// ===============================================DEBUG
				// LOG==========================================================
				this.composeDebugLog.initialGssoSubStateLog(rawData);
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}
		} else {
			this.jsonResultCode = JsonResultCode.SERVICE_NOT_ALLOW;
			this.logDescription = "GSSO_MISSING_SERVICE_TEMPLATE_WHEN_COMPARE";

			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.SEND_OTP.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// ===============================================DEBUG
				// LOG==========================================================
				/** writeLog LOG **/
				this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}

			/** CREATE RES MESSAGE **/
			rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
					appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonResultCode,
					logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_SEND_OTP_REQ.name()));

			// ===============================================SAVE
			// SUMMARY======================================================
			try {
				composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode, destNodeResultDescription);
				composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), rawDataIncoming.getInvoke());
			} catch (Exception e) {
				Log.e(e.getMessage());
			}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfile(origInvoke, appInstance);
		}
	}

	private void idleSendOTPInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {
		this.rawDataIncoming = equinoxRawData;
		this.rawDatasOutgoing = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
		this.abstractAF = abstractAF;

//		String invoke = rawDataIncoming.getRawDataAttribute(EquinoxAttribute.INVOKE);
//		appInstance.setOrigInvoke(invoke);

		/** SET COMMAND TO REFUND **/
		this.appInstance.setOrigCommand(IdleMessageFormat.SOAP_SEND_OTP_REQ);

		/** SET isInquiryVasSubscriber FROM EC02 TO INSTANCE **/
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

		/** SET FIRST TIMESTAMP **/
		this.appInstance.setFirstTimeStampIncoming(this.appInstance.getTimeStampIncoming());

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

	private void idleSendOTPSaveLog() {
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
			} catch (Exception e) {
				Log.e(e.getMessage());
			}
		}
		try {
			this.composeDetailsLog.initialOutgoingToE01(abstractAF, appInstance);
		} catch (Exception e) {
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