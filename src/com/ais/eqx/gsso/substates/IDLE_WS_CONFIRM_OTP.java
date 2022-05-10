package com.ais.eqx.gsso.substates;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.LogScenario;
import com.ais.eqx.gsso.enums.SoapResultCode;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GssoWSConfirmOTPRequest;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.SendWSConfirmOTPRequest;
import com.ais.eqx.gsso.instances.TransactionData;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.utils.CheckTransactionData;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;
import com.ais.eqx.gsso.utils.GssoConstructMessage;
import com.ais.eqx.gsso.utils.GssoDataManagement;
import com.ais.eqx.gsso.utils.GssoGenerator;
import com.ais.eqx.gsso.validator.VerifyMessage;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class IDLE_WS_CONFIRM_OTP implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;
	private AbstractAF 					abstractAF;
	private EquinoxRawData				rawDataIncoming;
	private ArrayList<EquinoxRawData>	rawDatasOutgoing;
	private String						nextState;

	GssoWSConfirmOTPRequest				confirmOTPReq;
	
	private JsonResultCode				jsonCode;
	private String						logDescription				= "";
	private String						path						= "";

	private String						destNodeResultCode			= "null";
	private String						destNodeResultDescription	= "null";
	private String						destNodeName				= "Client";
	private String						destNodeCommand				= EventLog.WS_CONFIRM_OTP.getEventLog();

	private boolean						serviceMisMatch				= false;

	private GssoComposeDetailsLog		composeDetailsLog;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;
	private MapDetailsAndConfigType		mapDetails;
	

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		this.nextState = SubStates.IDLE_WS_CONFIRM_OTP.toString();

		/************** INITIAL *****************/
		idleConfirmInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);

		/************** CODING ******************/
//		 System.out.println("Start IDLE_WS_CONFIRM_OTP");
		 
		/** VALID MESSAGE **/
		if (messageValidator(rawDataIncoming)) {
			writeLogSuccess(rawDataIncoming);

			this.confirmOTPReq = GssoDataManagement.extractGssoWSConfirmOTPRequest(rawDataIncoming);

			/** SET DTAILS IDENTITY **/
			
			this.composeDetailsLog.setIdentity("unknown");

			/** CHK TRANSACTION ID AND INCOMING TRANSACTION ID **/
			TransactionData transactionData = appInstance.getTransactionidData().get(this.confirmOTPReq.getSendWSConfirmOTPReq().getSessionId());
			/** MATCH **/
			if (CheckTransactionData.chkMatchTransaction(transactionData)) {
				String service = transactionData.getService();
				this.composeDetailsLog.setIdentity(service);
				
				// =========WRITE SUMMARY=======
				/** INITIATE SUMMARY-LOG **/
				this.composeSummary = new GssoComposeSummaryLog(abstractAF, service);
				// =========WRITE SUMMARY=======
				
				chkOTPHacktimeAndExpireTime(rawDataIncoming);
			}
			/** NOT MATCH **/
			else {
				this.jsonCode = JsonResultCode.NOT_AUTHEN_BEFORE;

				writeConfirmTransactionIDNotMatchLogAndStatistic(ec02Instance, equinoxRawData);

				/** CREATE RES MESSAGE **/
				rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
						appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonCode,
						logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_WS_CONFIRM_OTP.name()));

				/* REMOVE PROFILE */
				GssoDataManagement.removeProfile(rawDataIncoming.getInvoke(), appInstance);
			}
		}
		/** INVALID MESSAGE **/
		else {
			/** CREATE RES MESSAGE **/
			rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
					appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonCode,
					logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_WS_CONFIRM_OTP.name()));

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
		idleConfirmSaveLog();

		return this.rawDatasOutgoing;
	}

	private void writeConfirmTransactionIDNotMatchLogAndStatistic(EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		/* SET DEBUG TRANSACTION ID MIS MATCH */
		if (this.serviceMisMatch) {
			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.confirmServiceFailure();
			}
		}
		else {
			SendWSConfirmOTPRequest sendWSConfirmOTPRequest = this.confirmOTPReq.getSendWSConfirmOTPReq();
			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.confirmTransactionFailure(sendWSConfirmOTPRequest.getSessionId());
			}
		}
		/** VERIFY ERROR STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_ERROR_CONFIRMONETIMEPW_REQUEST.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(rawDataIncoming.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(rawDataIncoming.getInvoke(), LogScenario.WS_CONFIRM_OTP.getLogScenario());

		try {
			this.composeDetailsLog.initialIncoming(rawDataIncoming, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawDataIncoming, this.nextState);
		}
		catch (Exception ex) {
			Log.e(ex.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

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

		/** SEND RESP ERROR STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataIncoming.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			/** writeLog LOG **/
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_ERROR_CONFIRMONETIMEPW_REQUEST.getStatistic());
			this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}
	}

	private boolean messageValidator(EquinoxRawData rawData) {
		boolean isMessageValid = false;
		Log.d("   Strat  messageValidator ");
		try {
			VerifyMessage.verifyIDLE_WS_CONFIRM_OTP_Req(rawData, appInstance);

			isMessageValid = true;
		}
		catch (ValidationException e) {
			isMessageValid = false;
			/** VERIFY ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_CONFIRMONETIMEPW_REQUEST.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.WS_CONFIRM_OTP.getLogScenario());

			try {
				/** Extract Message **/
				GssoWSConfirmOTPRequest confirmOTPReq = GssoDataManagement.extractGssoWSConfirmOTPRequest(rawData);
				TransactionData transactionData = appInstance.getTransactionidData().get(confirmOTPReq.getSendWSConfirmOTPReq().getSessionId());
				/** SET DTAILS IDENTITY **/
				this.composeDetailsLog.setIdentity(transactionData.getService());
				
				// =========WRITE SUMMARY=======
				/** INITIATE SUMMARY-LOG **/
				this.composeSummary = new GssoComposeSummaryLog(abstractAF, transactionData.getService());
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

			this.path = e.getMandatoryPath();
			this.jsonCode = e.getJsonResultCode();
			this.logDescription = e.getMessage();
			this.destNodeResultDescription = this.path + " " + this.logDescription;

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// ===============================================DEBUG
				// LOG==========================================================
				/** writeLog LOG **/
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_CONFIRMONETIMEPW_REQUEST.getStatistic());
				this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				this.composeDebugLog.setFailureAvp(this.path + " " + this.logDescription);
				this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}

			/** SEND RESP ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailOutput().put(rawData.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		}
		Log.d("   End  messageValidator ");
		return isMessageValid;
	}

//	private boolean chkMatchTransaction(TransactionData transactionData) {
//		boolean isFondTransactionID = false;
//
//		try {
//			
//			if (transactionData != null) {
//					isFondTransactionID = true;
//			}
//			else {
//				isFondTransactionID = false;
//			}
//		}
//		catch (Exception e) {
//			isFondTransactionID = false;
//		}
//
//		return isFondTransactionID;
//	}

	private void chkOTPHacktimeAndExpireTime(EquinoxRawData rawData) {
		Log.d("   Strat  chkOTPHacktimeAndExpireTime ");
		/* CHK OTP PASSWORD AND EXPIRE TIME */
		SendWSConfirmOTPRequest sendWSConfirmOTPRequest = this.confirmOTPReq.getSendWSConfirmOTPReq();
		TransactionData confirmOTP = appInstance.getTransactionidData().get(sendWSConfirmOTPRequest.getSessionId());

		long otpExpireTime = confirmOTP.getOtpExpireTime();
		/* NOT EXPIRE */
		if (otpExpireTime > System.currentTimeMillis()) {
			if (sendWSConfirmOTPRequest.getPassword().equals(confirmOTP.getOtp())) {

				if (confirmOTP.getHackTime() >= 3) {
					this.jsonCode = JsonResultCode.HACK_TIME_MORETHAN_3;

					writeHackTimeMoreThanThreeLogAndStatistic(rawData, sendWSConfirmOTPRequest);

					/** CREATE RES MESSAGE **/
					rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
							appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonCode,
							logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_WS_CONFIRM_OTP.name()));

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfile(rawData.getInvoke(), appInstance);
				}
				else {
					this.jsonCode = JsonResultCode.SUCCESS;

					writePwdMatchLogAndStatistic(rawData, sendWSConfirmOTPRequest);

					/** CREATE SUCCESS RES MESSAGE **/
					createReturnSuccessMessage(rawData, confirmOTP);

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfile(rawData.getInvoke(), appInstance);
					/* REMOVE TRANSACTION PROFILE */
					appInstance.getTransactionidData().remove(sendWSConfirmOTPRequest.getSessionId());
					appInstance.getMapTimeoutOfTransactionID().remove(sendWSConfirmOTPRequest.getSessionId());
				}

			}
			/* OTP NOT MATCH */
			else {

				if (confirmOTP.getHackTime() >= 3) {
					this.jsonCode = JsonResultCode.HACK_TIME_MORETHAN_3;

					writeHackTimeMoreThanThreeLogAndStatistic(rawData, sendWSConfirmOTPRequest);

					/** CREATE RES MESSAGE **/
					rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
							appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonCode,
							logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_WS_CONFIRM_OTP.name()));
				}
				else {
					confirmOTP.setHackTime(confirmOTP.getHackTime() + 1);
					this.jsonCode = JsonResultCode.AUTHEN_FAIL;

					writePwdMisMatchLogAndStatistic(rawData, sendWSConfirmOTPRequest);

					/** CREATE RES MESSAGE **/
					rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
							appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonCode,
							logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_WS_CONFIRM_OTP.name()));
				}

				/* REMOVE PROFILE */
				GssoDataManagement.removeProfile(rawData.getInvoke(), appInstance);
			}
		}
		/* OTP EXPIRE */
		else {
			this.jsonCode = JsonResultCode.ONETIME_PASSWORD_EXPIRE;

			writeOTPExpireLogAndStatistic(rawData, sendWSConfirmOTPRequest);

			/** CREATE RES MESSAGE **/
			rawDatasOutgoing.add(GssoConstructMessage.createReturnErrorMessageIdle(this.appInstance, appInstance.getProfile(),
					appInstance.getListOrderReference(), appInstance.getTimeStampIncoming(), rawDataIncoming, jsonCode,
					logDescription, path, composeDebugLog, composeSummary, SubStates.IDLE_WS_CONFIRM_OTP.name()));

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfile(rawData.getInvoke(), appInstance);
		}
		Log.d("   End  chkOTPHacktimeAndExpireTime ");
	}

	private void writePwdMatchLogAndStatistic(EquinoxRawData rawData, SendWSConfirmOTPRequest sendWSConfirmOTPRequest) {
		/* SET DEBUG PWD MATCH */
		/** VERIFY SUCCESS STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_CONFIRMONETIMEPW_REQUEST.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.WS_CONFIRM_OTP.getLogScenario());

		try {
			this.composeDetailsLog.initialIncoming(rawData, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
		}
		catch (Exception ex) {
			Log.e(ex.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		/** SEND RESP SUCCESS STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_SUCCESS.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(rawData.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			/** writeLog LOG **/
			this.composeDebugLog.confirmPwdSuccess(sendWSConfirmOTPRequest.getSessionId());
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_CONFIRMONETIMEPW_REQUEST.getStatistic());
			this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_SUCCESS.getStatistic());
			this.composeDebugLog.initialGssoSubStateLog(rawData);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}
	}

	private void writeOTPExpireLogAndStatistic(EquinoxRawData rawData, SendWSConfirmOTPRequest sendWSConfirmOTPRequest) {
		/* SET DEBUG PWD EXPIRE */
		/** VERIFY ERROR STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_ERROR_CONFIRMONETIMEPW_REQUEST.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.WS_CONFIRM_OTP.getLogScenario());

		try {
			this.composeDetailsLog.initialIncoming(rawData, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
		}
		catch (Exception ex) {
			Log.e(ex.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

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

		/** SEND RESP ERROR STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(rawData.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			/** writeLog LOG **/
			this.composeDebugLog.confirmPwdExpire(sendWSConfirmOTPRequest.getSessionId());
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_ERROR_CONFIRMONETIMEPW_REQUEST.getStatistic());
			this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
			// this.composeDebugLog.initialGssoSubStateLog(rawData);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}
	}

	private void writePwdMisMatchLogAndStatistic(EquinoxRawData rawData, SendWSConfirmOTPRequest sendWSConfirmOTPRequest) {
		/** VERIFY ERROR STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_ERROR_CONFIRMONETIMEPW_REQUEST.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.WS_CONFIRM_OTP.getLogScenario());

		try {
			this.composeDetailsLog.initialIncoming(rawData, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
		}
		catch (Exception ex) {
			Log.e(ex.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

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

		/** SEND RESP ERROR STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(rawData.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog.confirmPwdFailure(sendWSConfirmOTPRequest.getSessionId());
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_ERROR_CONFIRMONETIMEPW_REQUEST.getStatistic());
			this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
		}
	}

	private void writeHackTimeMoreThanThreeLogAndStatistic(EquinoxRawData rawData, SendWSConfirmOTPRequest sendWSConfirmOTPRequest) {
		/* SET DEBUG PWD MIS MATCH AND OVER LIMIT */

		/** VERIFY ERROR STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_ERROR_CONFIRMONETIMEPW_REQUEST.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
		appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.WS_CONFIRM_OTP.getLogScenario());

		try {
			this.composeDetailsLog.initialIncoming(rawData, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawData, this.nextState);
		}
		catch (Exception ex) {
			Log.e(ex.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

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

		/** SEND RESP ERROR STATICTIC **/
		ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
		// ===============================================WRITE
		// DETAILS======================================================
		appInstance.getMapOrigInvokeEventDetailOutput().put(rawData.getInvoke(), EventLog.WS_CONFIRM_OTP.getEventLog());
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog.confirmOverLimit(sendWSConfirmOTPRequest.getSessionId());
			this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_ERROR_CONFIRMONETIMEPW_REQUEST.getStatistic());
			this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
		}
	}

	private void writeLogSuccess(EquinoxRawData rawData) {

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
		}

	}

	private void createReturnSuccessMessage(EquinoxRawData rawData, TransactionData confirmOTP) {
		EquinoxRawData output = null;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		String orderRef = GssoGenerator.generateOrderReference(ConfigureTool.getConfigure(ConfigName.APPLICATION_NODENAME),
				appInstance.getListOrderReference());
		SendWSConfirmOTPRequest sendWSConfirmOTPRequest = this.confirmOTPReq.getSendWSConfirmOTPReq();
		String transactionID = sendWSConfirmOTPRequest.getSessionId();
		String operName = "";
		if (appInstance.getProfile() == null || appInstance.getProfile().getOper() == null
				|| appInstance.getProfile().getOper().isEmpty()) {
		}
		else {
			operName = appInstance.getProfile().getOper();
		}

		String expireTimeString = sdf.format(confirmOTP.getOtpExpireTime());

		String cType = rawData.getCType();
		if (!cType.equalsIgnoreCase(EventCtype.XML)) {
			String json = "{\"confirmOneTimePWResponse\":" + "{" + "\"code\":\"" + this.jsonCode.getCode() + "\","
					+ "\"description\":\"" + this.jsonCode.getDescription() + "\"," + "\"isSuccess\":\"true\"," + "\"orderRef\":\""
					+ orderRef + "\"," + "\"operName\":\"" + operName + "\"," + "\"expirePassword\":\"" + expireTimeString + "\","
					+ "\"transactionID\":\"" + transactionID + "\"" + "}" + "}";

			output = new EquinoxRawData();
			output.setName(EventName.HTTP);
			output.setCType(rawData.getCType());
			output.setType(EventAction.RESPONSE);
			output.setTo(rawData.getOrig());
			output.setInvoke(rawData.getInvoke());
			output.addRawDataAttribute(EquinoxAttribute.VAL, json);

			this.composeSummary.initialSummary(this.appInstance ,appInstance.getTimeStampIncoming(), rawData.getInvoke(),
					EventLog.WS_CONFIRM_OTP.getEventLog(), this.jsonCode.getCode(), this.jsonCode.getDescription());
		}
		else {

			StringBuilder soapOutBuilder = new StringBuilder();
			soapOutBuilder.append("<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">");
			soapOutBuilder.append("<S:Body>");
			soapOutBuilder.append("<ns2:confirmOneTimePWResponse xmlns:ns2=\"http://ws.gsso/\">");
			soapOutBuilder.append("<return>");
			soapOutBuilder.append("<code>" + SoapResultCode.SUCCESS.getCode() + "</code>");
			soapOutBuilder.append("<description>" + SoapResultCode.SUCCESS.getDescription() + "</description>");
			soapOutBuilder.append("<isSuccess>true</isSuccess>");

//			if (appInstance.getProfile() == null || appInstance.getProfile().getOper() == null
//					|| appInstance.getProfile().getOper().isEmpty()) {
//				soapOutBuilder.append("<operName/>");
//			}
//			else {
//				soapOutBuilder.append("<operName>" + appInstance.getProfile().getOper() + "</operName>");
//			}

			soapOutBuilder.append("<orderRef>" + orderRef + "</orderRef>");
//			soapOutBuilder.append("<pwd>"+sendWSConfirmOTPRequest.getPassword()+"</pwd>");
			soapOutBuilder.append("<pwd/>");
			soapOutBuilder.append("<transactionID>" + transactionID + "</transactionID>");
			soapOutBuilder.append("</return>");
			soapOutBuilder.append("</ns2:confirmOneTimePWResponse>");
			soapOutBuilder.append("</S:Body>");
			soapOutBuilder.append("</S:Envelope>");

			output = new EquinoxRawData();
			output.setName(EventName.HTTP);
			output.setCType(EventCtype.XML);
			output.setType(EventAction.RESPONSE);
			output.setTo(rawData.getOrig());
			output.setInvoke(rawData.getInvoke());
			output.setRawMessage(soapOutBuilder.toString());

			this.composeSummary.initialSummary(this.appInstance ,appInstance.getTimeStampIncoming(), rawData.getInvoke(),
					EventLog.WS_CONFIRM_OTP.getEventLog(), SoapResultCode.SUCCESS.getCode(), SoapResultCode.SUCCESS.getDescription());
		}

		// ===============================================WRITE
		// SUMMARY======================================================
		try {
			this.composeSummary.setWriteSummary();
			this.composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), rawDataIncoming.getInvoke());
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		this.rawDatasOutgoing.add(output);
	}

	private void idleConfirmInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {
		Log.d("   Start  idleConfirmInitInstanceAndLog ");
		this.rawDataIncoming = equinoxRawData;
		this.rawDatasOutgoing = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
		this.abstractAF = abstractAF;
		
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			// ===============================================DEBUG
			// LOG==========================================================
			/** INITIAL LOG **/
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
			// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			// ===============================================WRITE
		}

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
		
		Log.d("   End  idleConfirmInitInstanceAndLog ");
	}

	private void idleConfirmSaveLog() {
		Log.d("   Start  idleConfirmSaveLog ");
		// ===============================================WRITE
		// DETAILS======================================================
		int outPutSize = this.rawDatasOutgoing.size();
		for (EquinoxRawData rawDataOut : this.rawDatasOutgoing) {
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
		Log.d("   End  idleConfirmSaveLog ");
	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data) {
		// TODO Auto-generated method stub
		return null;
	}
}