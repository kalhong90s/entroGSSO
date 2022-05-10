package com.ais.eqx.gsso.substates;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.LogScenario;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GssoWSConfirmOTPRequest;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.Refund;
import com.ais.eqx.gsso.instances.RefundLog;
import com.ais.eqx.gsso.instances.TransactionData;
import com.ais.eqx.gsso.interfaces.EQX;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.interfaces.RetNumber;
import com.ais.eqx.gsso.parser.MessageParser;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoConstructMessage;
import com.ais.eqx.gsso.utils.GssoDataManagement;
import com.ais.eqx.gsso.utils.InvokeFilter;
import com.ais.eqx.gsso.validator.VerifyMessage;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class W_REFUND implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;
	private Refund						refundRes;
	private Refund						refundReq;
	private ArrayList<EquinoxRawData>	rawDatasOut;
	private String						nextState;
	private String						origInvoke;
	private EquinoxRawData				rawDataIncoming;

//	private EquinoxRawData				rawDataOrig;
	private OrigInvokeProfile			origInvokeProfile;

	private GssoComposeDetailsLog		composeDetailsLog;
	private MapDetailsAndConfigType		mapDetails;
	private GssoComposeDebugLog			composeDebugLog;
	
	long								startTimeOfInvoke;
	private String						compareLog					= "";
	
	private String						logDescription				= "";
	private String						path						= "";


	private String						reqTimeStamp;
	private String						session						= "";
	private String						initInvoke					= "";
	private String						cmdName						= "";
	private String						identity					= "";
	private String						sessionId					= "";
	private String						tid							= "";
	private String						refId						= "";
	private String						resultCode					= "";
	private String						resultDesc					= "";


	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		
		String statIn = "";
		
		/************** INITIAL *****************/
		refundInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);
		
		if (RetNumber.NORMAL.equals(equinoxRawData.getRet())) {
			/** VALID MESSAGE **/
			if (messageValidator(rawDataIncoming)) {
				writeLogSuccess(rawDataIncoming);
				
				String val = this.rawDataIncoming.getRawDataAttribute(EQX.Attribute.VAL);
				this.refundRes = (Refund) MessageParser.fromJson(val, Refund.class);
				
				this.resultCode = this.refundRes.getStatus();
				this.resultDesc = this.refundRes.getDevMessage();
				
				if(EQX.Ecode.ECODE_200.equals(refundRes.getStatus())){
					statIn = Statistic.GSSO_RECEIVED_RPCEF_REFUND_MANAGEMENT_RESPONSE_SUCCESS.getStatistic();
				}
				else{
					statIn = Statistic.GSSO_RECEIVED_RPCEF_REFUND_MANAGEMENT_RESPONSE_ERROR.getStatistic();
				}
			}
			
			if(StringUtils.isNoneEmpty(statIn)) ec02Instance.incrementsStat(statIn);
			this.nextState = SubStates.END.name();
		}
		else if (RetNumber.TIMEOUT.equals(equinoxRawData.getRet())) {
			statIn = Statistic.GSSO_RECEIVED_RPCEF_REFUND_MANAGEMENT_REQUEST_TIMEOUT.getStatistic();
			ec02Instance.incrementsStat(statIn);
			
			this.resultCode = "ret=" + RetNumber.TIMEOUT;
			this.resultDesc = "Timeout";
			this.nextState = SubStates.END.name();
		}
		else if (RetNumber.ERROR.equals(equinoxRawData.getRet())) {
			statIn = Statistic.GSSO_RECEIVED_RPCEF_REFUND_MANAGEMENT_RESPONSE_ERROR.getStatistic();
			ec02Instance.incrementsStat(statIn);

			this.resultCode = "ret=" + RetNumber.ERROR;
			this.resultDesc = "Error";
			this.nextState = SubStates.END.name();
		}
		else if (RetNumber.REJECT.equals(equinoxRawData.getRet())) {
			statIn = Statistic.GSSO_RECEIVED_RPCEF_REFUND_MANAGEMENT_RESPONSE_REJECT.getStatistic();
			ec02Instance.incrementsStat(statIn);

			this.resultCode = "ret=" + RetNumber.REJECT;
			this.resultDesc = "Reject";
			
			if (!(this.appInstance.getRefundRetryLimit() >= (Integer.parseInt(ConfigureTool.getConfigure(ConfigName.REFUND_RETRY))))) {

				this.appInstance.increaseRefundRetryLimit();

				this.rawDatasOut.add(GssoConstructMessage.createRefundReqTorPCEFMessageRetry(rawDataIncoming, ec02Instance, refundReq, composeDebugLog));
				this.nextState = SubStates.W_REFUND.toString();

				appInstance.getMapOrigInvokeEventDetailOutput().put(this.rawDatasOut.get(0).getInvoke(), EventLog.REFUND.getEventLog());
			}
			else{
				this.nextState = SubStates.END.name();
			}
		}
		else if (RetNumber.ABORT.equals(equinoxRawData.getRet())) {
			statIn = Statistic.GSSO_RECEIVED_RPCEF_REFUND_MANAGEMENT_RESPONSE_ABORT.getStatistic();
			ec02Instance.incrementsStat(statIn);

			this.resultCode = "ret=" + RetNumber.ABORT;
			this.resultDesc = "Abort";
			this.nextState = SubStates.END.name();
		}
		
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog.addStatisticIn(statIn);
		}
		
		if(this.nextState != SubStates.W_REFUND.toString()){
			this.appInstance.getMapTimeoutOfWaitRefund().clear();
			this.appInstance.getMapTimeoutOfTransactionID().clear();
			this.nextState = SubStates.END.name();
			
			/* REMOVE PROFILE */
			GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
			
			/* REMOVE TIMEOUT WAIT RF */
//			if (appInstance.getMapTimeoutOfWaitRefund().size() > 0) {
//				Iterator<Entry<String, TimeoutCalculator>> iteratorWRefund = appInstance.getMapTimeoutOfWaitRefund().entrySet().iterator();
//				while (iteratorWRefund.hasNext()) {
//					Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iteratorWRefund.next();
//					String invokeTimeoutRefund = InvokeFilter.getOriginInvoke(entry.getKey());
//
//					if (invokeTimeoutRefund.equals(origInvoke)) {
//						iteratorWRefund.remove();
//					}
//				}
//			}
		}

		/* SAVE LOG */
		refundSaveLog();
		
		return this.rawDatasOut;
	}

	private void writeLogSuccess(EquinoxRawData rawData) {

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
		}

	}
	
	private void refundInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance){
		this.startTimeOfInvoke = System.currentTimeMillis();
		this.rawDataIncoming = (EquinoxRawData) equinoxRawData;
		this.nextState = SubStates.W_REFUND.name();
		this.rawDatasOut = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
		this.origInvoke = InvokeFilter.getOriginInvoke(rawDataIncoming.getInvoke());
		
		this.refundReq = this.appInstance.getMapInvokeOfRefund().get(origInvoke);
		this.refundRes = new Refund();
		
		this.origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvoke);
		
		/** INITAIL REFUND LOG **/
		if(origInvokeProfile.getGssoOTPRequest()!=null){
			this.sessionId = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getSessionId();
			this.refId = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getRefId();
			this.identity = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getService();
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
		Date date = new Date(this.appInstance.getFirstTimeStampIncoming());
		
		this.reqTimeStamp = formatter.format(date);
		this.session = abstractAF.getEquinoxProperties().getSession();
		try {
			String[] invoke = origInvoke.split("@");
			this.initInvoke = invoke[0];
		}
		catch (Exception e) {
			this.initInvoke = origInvoke;
		}
		this.cmdName = "Refund";
		this.tid = this.refundReq.getTid();
		

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			/** INITIAL DEBUG LOG **/
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
		}

		/** INITIAL DETAILS LOG **/
		this.composeDetailsLog = new GssoComposeDetailsLog(appInstance, abstractAF.getEquinoxProperties().getState() + "." + "BEGIN",
				abstractAF);

		this.mapDetails = new MapDetailsAndConfigType();
		
		/** SET DTAILS IDENTITY **/
		this.composeDetailsLog.setIdentity(origInvokeProfile.getDetailsService());

		this.composeDetailsLog.setDataOrig(origInvoke, this.rawDataIncoming, appInstance);

		appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.REFUND.getEventLog());

		appInstance.getMapOrigInvokeDetailScenario()
				.put(origInvoke, appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

//		this.startTimeOfInvokeIncoming = origInvokeProfile.getStartTimeOfInvoke();

		try {

			this.composeDetailsLog.initialIncoming(rawDataIncoming, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawDataIncoming, nextState);

		}
		catch (Exception e) {
			e.printStackTrace();
			Log.e(e.getMessage());
		}

	}
	


	private boolean messageValidator(EquinoxRawData rawData) {
		boolean isMessageValid = false;
		Log.d("   Strat  messageValidator ");
		try {
			VerifyMessage.verifyRefundRes(rawData, appInstance, this.origInvoke);

			isMessageValid = true;
		}
		catch (ValidationException e) {
			isMessageValid = false;
			/** VERIFY ERROR STATICTIC **/
			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_RPCEF_REFUND_MANAGEMENT_RESPONSE.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
			appInstance.getMapOrigInvokeEventDetailInput().put(rawData.getInvoke(), EventLog.REFUND.getEventLog());
			appInstance.getMapOrigInvokeDetailScenario().put(rawData.getInvoke(), LogScenario.SEND_OTP.getLogScenario());

			try {
				/** Extract Message **/
				GssoWSConfirmOTPRequest confirmOTPReq = GssoDataManagement.extractGssoWSConfirmOTPRequest(rawData);
				TransactionData transactionData = appInstance.getTransactionidData().get(confirmOTPReq.getSendWSConfirmOTPReq().getSessionId());
				/** SET DTAILS IDENTITY **/
				this.composeDetailsLog.setIdentity(transactionData.getService());
				
				// =========WRITE SUMMARY=======
				/** INITIATE SUMMARY-LOG **/
//				this.composeSummary = new GssoComposeSummaryLog(abstractAF, transactionData.getService());
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
			this.logDescription = e.getMessage();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// ===============================================DEBUG
				// LOG==========================================================
				/** writeLog LOG **/
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_RPCEF_REFUND_MANAGEMENT_RESPONSE.getStatistic());
				this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				this.composeDebugLog.setFailureAvp(this.path + " " + this.logDescription);
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DEBUG
				// LOG^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			}

			/** SEND RESP ERROR STATICTIC **/
//			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CONFIRMONETIMEPW_RESPONSE_ERROR.getStatistic());
			// ===============================================WRITE
			// DETAILS======================================================
//			appInstance.getMapOrigInvokeEventDetailOutput().put(rawData.getInvoke(), EventLog.REFUND.getEventLog());
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
			// DETAILS^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		}
		Log.d("   End  messageValidator ");
		return isMessageValid;
	}

	private void refundSaveLog() {
		
		RefundLog refundLog = new RefundLog();
		refundLog.setReqTimeStamp(reqTimeStamp);
		refundLog.setSession(session);
		refundLog.setInitInvoke(initInvoke);
		refundLog.setCmdName(cmdName);
		refundLog.setIdentity(identity);
		refundLog.setSessionId(sessionId);
		refundLog.setTid(tid);
		refundLog.setRefId(refId);
		refundLog.setResultCode(resultCode);
		refundLog.setResultDesc(resultDesc);
		
		ec02Instance.writeLog(true, ConfigureTool.getConfigureLogName(ConfigName.REFUND_LOG_NAME.getName()),
				refundLog.toString());
		
		/** WRITE DETAIL LOG **/
		int outPutSize = this.rawDatasOut.size();
		for (EquinoxRawData rawDataOut : this.rawDatasOut) {

			try {
				this.composeDetailsLog.initialOutgoing(rawDataOut, appInstance, outPutSize);
			}
			catch (Exception e) {
				Log.e(e.getMessage());
			}

		}
		/** SAVE DETAILS **/
		this.mapDetails.setDetail(this.composeDetailsLog.getDetailsLog());
		this.appInstance.getListDetailsLog().add(mapDetails);

		/** WRITE DEBUG LOG **/
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog.initialGssoSubStateLog(rawDataIncoming);
			this.composeDebugLog.writeDebugSubStateLog();
		}

		if (!this.compareLog.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("\n=======================================================================");
			sb.append("\n" + this.compareLog);
			sb.append("\n=======================================================================");
			Log.d(sb.toString());
		}

	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data) {
		// TODO Auto-generated method stub
		return null;
	}

}
