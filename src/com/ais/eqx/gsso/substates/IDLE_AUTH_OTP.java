

package com.ais.eqx.gsso.substates;

import java.util.ArrayList;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.LogScenario;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GssoAuthOTP;
import com.ais.eqx.gsso.instances.GssoAuthOTPRequest;
import com.ais.eqx.gsso.instances.GssoOTPRequest;
import com.ais.eqx.gsso.instances.GssoServiceTemplate;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.TransactionData;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.interfaces.OTPChannel;
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

public class IDLE_AUTH_OTP implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;
	private AbstractAF					abstractAF;

	private EquinoxRawData				rawDataIncoming;
	private ArrayList<EquinoxRawData>	rawDatasOutgoing;
	private String						nextState;

	private GssoAuthOTPRequest			authOTPReq;

	private JsonResultCode				jsonCode;
	private String						logDescription				= "";
	private String						path						= "";

	private String						destNodeResultCode			= "null";
	private String						destNodeResultDescription	= "null";
	private String						destNodeName				= "Client";
	private String						destNodeCommand				= EventLog.AUTHEN_OTP.getEventLog();

	private GssoComposeDetailsLog		composeDetailsLog;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;
	private MapDetailsAndConfigType		mapDetails;

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		this.nextState = SubStates.IDLE_AUTH_OTP.toString();

		/************** INITIAL INSTANCE AND LOG *****************/
		idleAuthInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);

		/************** CODING ******************/
		// System.out.println("Start IDLE_AUTH_OTP");

		/** VALID MESSAGE **/
		if (messageValidator(rawDataIncoming)) {
			writeLogSuccess(rawDataIncoming);

			/** EXTRACT OTP REQUEST **/
			this.authOTPReq = GssoDataManagement.extractGssoAuthOTPRequest(rawDataIncoming);

			/** INITIAL INCOMEING **/
			String incomingMessageType = IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType();
			
			TimeoutManagement.initialIncomingOTPReq(appInstance, rawDataIncoming, incomingMessageType, 
					appInstance.getTimeStampIncoming());

			/** SAVE TO INSTANCE **/
			GssoAuthOTP authenOnetimePassword = this.authOTPReq.getAuthenOnetimePassword();
			OrigInvokeProfile origInvokeProfile = updateOrigInvokeProfile(authenOnetimePassword);
			GssoServiceTemplate authServiceTemplate = updateSmscDeliveryReceipt(authenOnetimePassword, origInvokeProfile);

			/** SET DTAILS IDENTITY **/
			origInvokeProfile.setDetailsService(this.authOTPReq.getAuthenOnetimePassword().getRequest().getService());
			this.composeDetailsLog.setIdentity(origInvokeProfile.getDetailsService());
			
			// =========WRITE SUMMARY=======
			/** INITIATE SUMMARY-LOG **/
			this.composeSummary = new GssoComposeSummaryLog(abstractAF, origInvokeProfile.getDetailsService());
			// =========WRITE SUMMARY=======
			
			if (authServiceTemplate.getSeedkey() == null) {
				origInvokeProfile.setAuthAndMissingSeedKey(true);
			}
			appInstance.getListInvokeProcessing().add(rawDataIncoming.getInvoke());

			/*  */
			smsORemailFlow(rawDataIncoming);
		}
		/** INVALID MESSAGE **/
		else {
			/** CREATE RES MESSAGE **/
			rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
					appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonCode,
					logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_AUTH_OTP.name()));

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
		idleAuthSaveLog();

		return this.rawDatasOutgoing;
	}

	private GssoServiceTemplate updateSmscDeliveryReceipt(GssoAuthOTP authenOnetimePassword, OrigInvokeProfile origInvokeProfile) {
		GssoServiceTemplate authServiceTemplate = authenOnetimePassword.getServiceTemplate();
		GssoServiceTemplate gssoServiceTemplate = origInvokeProfile.getGssoServiceTemplate();
		if (authServiceTemplate.getSmscDeliveryReceipt() != null) {
			gssoServiceTemplate.setSmscDeliveryReceipt(authServiceTemplate.getSmscDeliveryReceipt());
		}
		else {
			gssoServiceTemplate.setSmscDeliveryReceipt("true");
		}

		return authServiceTemplate;
	}

	private OrigInvokeProfile updateOrigInvokeProfile(GssoAuthOTP authenOnetimePassword) {
		GssoOTPRequest otpRequest = new GssoOTPRequest();
		otpRequest.setSendOneTimePW(authenOnetimePassword.getRequest());
		otpRequest.setMessageType(this.authOTPReq.getMessageType());
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(rawDataIncoming.getInvoke());
		origInvokeProfile.setCmdName(EventLog.AUTHEN_OTP.getEventLog());
		origInvokeProfile.setScenarioName(LogScenario.AUTHEN_OTP.getLogScenario());
		origInvokeProfile.setGssoOTPRequest(otpRequest);
		origInvokeProfile.setGssoServiceTemplate(authenOnetimePassword.getServiceTemplate());
		return origInvokeProfile;
	}

	private boolean messageValidator(EquinoxRawData rawData) {
		boolean isMessageValid = false;

		try {
			VerifyMessage.verifyIDLE_AUTH_OTP_Req(rawData, appInstance);

			isMessageValid = true;

		}
		catch (ValidationException e) {
			isMessageValid = false;
			/** VERIFY OTP REQ ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_AUTHENONETIMEPASSWORD_REQUEST.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.AUTHEN_OTP.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.AUTHEN_OTP.getLogScenario());

			try {
				/** Extract Message **/
				GssoAuthOTPRequest authotpRequest = GssoDataManagement.extractGssoAuthOTPRequest(rawData);
				
				/** SET DTAILS IDENTITY **/
				this.composeDetailsLog.setIdentity(authotpRequest.getAuthenOnetimePassword().getRequest().getService());
				
				// =========WRITE SUMMARY=======
				/** INITIATE SUMMARY-LOG **/
				this.composeSummary = new GssoComposeSummaryLog(abstractAF, authotpRequest.getAuthenOnetimePassword().getRequest().getService());
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
				Log.e(e.getMessage());
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
				/** writeLog LOG **/
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_AUTHENONETIMEPASSWORD_REQUEST.getStatistic());
				this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				this.composeDebugLog.setFailureAvp(this.path + " " + this.logDescription);
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				if (this.jsonCode == JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION) {
					this.composeDebugLog.transactionIsOverLimit();
				}
				this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
			}

			/** SEND RESP ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(rawData.getInvoke(), EventLog.AUTHEN_OTP.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

		return isMessageValid;
	}

	private void writeLogSuccess(EquinoxRawData rawData) {

		/** VALID OTP REQ STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_AUTHENONETIMEPASSWORD_REQUEST.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.AUTHEN_OTP.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.AUTHEN_OTP.getLogScenario());

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
			/** writeLog LOG **/
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_AUTHENONETIMEPASSWORD_REQUEST.getStatistic());
			this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

	}

	private void smsORemailFlow(EquinoxRawData rawData) {
		String origInvoke = rawDataIncoming.getInvoke();

		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		GssoOTPRequest otpRequest = origInvokeProfile.getGssoOTPRequest();
		/** FIND SERVICE TEMPLATE **/
		GssoServiceTemplate thisServiceTemplate = origInvokeProfile.getGssoServiceTemplate();
		String otpChannel = otpRequest.getSendOneTimePW().getOtpChannel();

		/** CHOOSE Defult Value **/
		GssoDataManagement.chooseDefaultValues(otpRequest, thisServiceTemplate);

		/* CREATE TRANSACTION ID PROFILE */
		GssoAuthOTP authenOnetimePassword = authOTPReq.getAuthenOnetimePassword();
		origInvokeProfile.setTransactionID(GssoDataManagement.createNewTransaction(appInstance, origInvoke, authenOnetimePassword
				.getServiceTemplate().getSeedkey()));
		origInvokeProfile.setServiceKey(authenOnetimePassword.getRequest().getServiceKey());

		/* SET FoundSeedKey */
		TransactionData confirmOTP = appInstance.getTransactionidData().get(origInvokeProfile.getTransactionID());
		confirmOTP.setAuthAndMissingSeedKey(origInvokeProfile.isAuthAndMissingSeedKey());

		origInvokeProfile.setOrderRefLog(GssoGenerator.generateOrderReference(
				ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME), appInstance.getListOrderReference()));

		/** FOR SMS **/
		if (otpChannel.equalsIgnoreCase(OTPChannel.SMS)) {

			if(origInvokeProfile.isBypassUSMP()){
				rawDatasOutgoing.addAll(GssoConstructMessage.createSMSReqMessageV2(origInvoke, thisServiceTemplate, ec02Instance,composeDebugLog));

			}else {
				rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,composeDebugLog));
			}
			origInvokeProfile.setSmsOutgoing(rawDatasOutgoing.size());

		}
		/** FOR EMAIL **/
		else if (otpChannel.equalsIgnoreCase(OTPChannel.EMAIL)) {

			rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
					composeDebugLog));

		}
		/** FOR ALL **/
		else {

			if(origInvokeProfile.isBypassUSMP()){
				rawDatasOutgoing.addAll(GssoConstructMessage.createSMSReqMessageV2(origInvoke, thisServiceTemplate, ec02Instance,composeDebugLog));

			}else {
				rawDatasOutgoing.add(GssoConstructMessage.createSMSReqMessage(origInvoke, thisServiceTemplate, ec02Instance,composeDebugLog));
			}
			origInvokeProfile.setSmsOutgoing(rawDatasOutgoing.size());

			rawDatasOutgoing.add(GssoConstructMessage.createEMAILReqMessage(origInvoke, thisServiceTemplate, ec02Instance,
					composeDebugLog));

		}

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			/** writeLog LOG **/
			this.composeDebugLog.initialGssoSubStateLog(rawData);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}

	}

	private void idleAuthInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {

		this.rawDataIncoming = equinoxRawData;
		this.rawDatasOutgoing = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
		this.abstractAF = abstractAF;

		// ===============================================DEBUG
		// LOG==========================================================
		/** INITIAL LOG **/
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
		// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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

	private void idleAuthSaveLog() {
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
					this.appInstance.getMapOrigProfile().get(origInvoke)
							.setSubmitSmRequestTime(this.composeDetailsLog.getDetailTimeOutgoing());
				}
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
