package com.ais.eqx.gsso.substates;

import java.util.ArrayList;
import java.util.Iterator;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.LogDestNodeResultDesc;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.PortCheckOperationStatus;
import com.ais.eqx.gsso.instances.PortCheckResponse;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.interfaces.OperName;
import com.ais.eqx.gsso.interfaces.RetNumber;
import com.ais.eqx.gsso.interfaces.USMPCode;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;
import com.ais.eqx.gsso.utils.GssoConstructMessage;
import com.ais.eqx.gsso.utils.GssoDataManagement;
import com.ais.eqx.gsso.utils.InvokeFilter;
import com.ais.eqx.gsso.utils.ResponseMessage;
import com.ais.eqx.gsso.utils.SoapToObject;
import com.ais.eqx.gsso.validator.VerifyUSMPMessage;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class W_PORT_CHECK implements IAFSubState {

	private EC02Instance			ec02Instance;
	private APPInstance				appInstance;

	private String					nextState;

	ArrayList<EquinoxRawData>		rawDatasOutgoing;
	private PortCheckResponse		portCheck;

	private GssoComposeDetailsLog	composeDetailsLog;

	private MapDetailsAndConfigType	mapDetails;
	private EquinoxRawData			rawDataInput;
	private String					origInvokeProcess			= "";
	private AbstractAF				abstractAF;

	private GssoComposeDebugLog		composeDebugLog;
	private GssoComposeSummaryLog	composeSummary;

	private boolean					isSummaryEnable				= false;
	private String					nodeCommand;
	private ArrayList<String>		listCode;
	private ArrayList<String>		listDescription;
	private ArrayList<String>		listOrigInvoke;
	private long					startTimeOfInvokeIncoming;

	private String					destNodeResultCode			= "null";
	private String					destNodeName				= "USMP";
	private String					destNodeCommand				= "PortCheck";
	private String					destNodeResultDescription	= "null";

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {

		this.nextState = SubStates.W_PORT_CHECK.toString();
		/** W_PORT_CHECK **/

		/************** INITIAL *****************/
		portChkInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);

		/************** CODING ******************/
		// System.out.println("Start W_PORT_CHECK");

		appInstance.setWaitPortCheck(false);

		/** Check Normal **/
		if (RetNumber.NORMAL.equals(equinoxRawData.getRet())) {

			try {
				this.portCheck = SoapToObject.parserPortCheck(rawDataInput.getRawDataMessage(), rawDataInput);

				/****** Valify Message ***********/
				VerifyUSMPMessage.portCheckValidator(this.portCheck, rawDataInput.getRawDataMessage());

				/* SET INQ SUB SUCCESS */
				appInstance.setInquirySubSuccess(true);

				String usmpCode = portCheck.getOperationStatus().getCode();
				if ((usmpCode.equals(USMPCode.VSMP_08030000) || usmpCode.equals(USMPCode.VSMP_08030001))) {

					this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_USMP_PORTCHECK_RESPONSE_SUCCESS.getStatistic());

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_PORTCHECK_RESPONSE_SUCCESS.getStatistic());
						this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
						this.composeDebugLog.messageResponseSuccess(this.portCheck.getOperationStatus().getCode());
					}

					successCase(rawDataInput, abstractAF);
				}
				else {
					String usmpDescription = portCheck.getOperationStatus().getDescription();

					this.destNodeResultCode = usmpCode;
					this.destNodeResultDescription = usmpDescription;

					this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_USMP_PORTCHECK_RESPONSE_RESULTCODE_ERROR.getStatistic());

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_PORTCHECK_RESPONSE_RESULTCODE_ERROR
								.getStatistic());
						this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
						this.composeDebugLog.messageResponseFailed(this.portCheck.getOperationStatus().getCode());
					}

					isSummaryEnable = true;
					ResponseMessage.returnMessage_Error(origInvokeProcess, rawDatasOutgoing, abstractAF, ec02Instance, listCode,
							listDescription, listOrigInvoke, composeDebugLog, false);
				}

			}
			catch (ValidationException validate) {
				this.destNodeResultDescription = validate.getMandatoryPath() + " " + validate.getMessage();

				this.nextState = SubStates.END.toString();

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_USMP_PORTCHECK_RESPONSE.getStatistic());
					this.composeDebugLog.messageResponseFailed(validate.getResultCode());
					this.composeDebugLog.setFailureAvp(validate.getMandatoryPath() + " " + validate.getMessage());
					this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				}

				this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_USMP_PORTCHECK_RESPONSE.getStatistic());

				isSummaryEnable = true;
				ResponseMessage.returnMessage_Error(origInvokeProcess, rawDatasOutgoing, abstractAF, ec02Instance, listCode,
						listDescription, listOrigInvoke, composeDebugLog, false);

				this.mapDetails.setNoFlow();
			}

		}
		/** TIMEOUT **/
		else if (RetNumber.TIMEOUT.equals(this.rawDataInput.getRet())) {
			this.destNodeResultDescription = LogDestNodeResultDesc.CONNECTION_TIMEOUT.getLogDestNodeResultDesc();

			this.nextState = SubStates.END.toString();

			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_USMP_PORTCHECK_REQUEST_TIMEOUT.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_PORTCHECK_REQUEST_TIMEOUT.getStatistic());
				this.composeDebugLog.setMessageValidator("-");
			}

			/** Message Time Out */
			isSummaryEnable = ResponseMessage.returnMessage_TimeOut(origInvokeProcess, rawDatasOutgoing, abstractAF, ec02Instance,
					listCode, listDescription, listOrigInvoke, composeDebugLog, false);
		}
		/** ERROR, REJECT, ABORT **/
		else {

			this.nextState = SubStates.END.toString();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.setMessageValidator("-");
			}

			/** not RetNumber NORMAL */
			errorCase(rawDataInput);

		}

		/* SAVE LOG */
		portChkSaveLog();

		return this.rawDatasOutgoing;
	}

	private void successCase(EquinoxRawData rawData, AbstractAF abstractAF) {

		PortCheckOperationStatus operation = this.portCheck.getOperationStatus();

		/** Code is VSMP-08030000 **/
		if (operation.getCode().equals(USMPCode.VSMP_08030000)) {

			/** parameter "spName" from USMP Response */
			String spName = this.portCheck.getPortJournal().getSpName();
			this.appInstance.getProfile().setSpName(spName);
			this.nextState = SubStates.W_SERVICE_TEMPLATE.toString();
			this.appInstance.getProfile().setOper(OperName.NonAIS);

			OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvokeProcess);

			/** PermissionAccountType is Allow **/
			if (GssoDataManagement.checkPermissionAccountType(origInvokeProfile, appInstance)) {
				GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, origInvokeProcess, abstractAF, composeDebugLog);
			}
			/** PermissionAccountType is not Allow **/
			else {
				isSummaryEnable = ResponseMessage.composeErrorMessage(origInvokeProcess, rawDatasOutgoing, abstractAF, ec02Instance,
						listCode, listDescription, listOrigInvoke, composeDebugLog);
			}

			/** Check Permission From Account Type In ListWaiting **/
			Iterator<String> listWaitInqSub = appInstance.getListWaitInquirySub().iterator();
			while (listWaitInqSub.hasNext()) {
				String origInvoke = (String) listWaitInqSub.next();
				OrigInvokeProfile origInvokeProfileWaiting = appInstance.getMapOrigProfile().get(origInvoke);

				/** PermissionAccountType is Allow **/
				if (GssoDataManagement.checkPermissionAccountType(origInvokeProfileWaiting, appInstance)) {
					GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, origInvoke, abstractAF, composeDebugLog);
				}
				/** PermissionAccountType is not Allow **/
				else {
					isSummaryEnable = ResponseMessage.composeErrorMessage(origInvoke, rawDatasOutgoing, abstractAF, ec02Instance,
							listCode, listDescription, listOrigInvoke, composeDebugLog);
				}
				listWaitInqSub.remove();
			}

		}

		/** Code is VSMP-08030001 **/
		else if (operation.getCode().equals(USMPCode.VSMP_08030001)) {
			this.nextState = SubStates.END.toString();

			isSummaryEnable = ResponseMessage.returnMessage_UnknownMSISDN(origInvokeProcess, rawDatasOutgoing, abstractAF,
					ec02Instance, listCode, listDescription, listOrigInvoke, composeDebugLog);

		}

		/** parameter as "Fail" **/
		else {
			this.nextState = SubStates.END.toString();

			/** return Message ( EquinoxRawData,Statistic ) **/
			isSummaryEnable = true;
			ResponseMessage.returnMessage_Error(origInvokeProcess, rawDatasOutgoing, abstractAF, ec02Instance, listCode,
					listDescription, listOrigInvoke, composeDebugLog, false);
		}
	}

	private void errorCase(EquinoxRawData rawData) {

		String statistic = "";

		if (rawData.getRet().equals(RetNumber.ERROR)) {

			this.destNodeResultDescription = LogDestNodeResultDesc.ERROR.getLogDestNodeResultDesc();

			statistic = Statistic.GSSO_RECEIVED_USMP_PORTCHECK_RESPONSE_ERROR.getStatistic();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				/** ADD STAT DEBUG INPUT LOG **/
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_PORTCHECK_RESPONSE_ERROR.getStatistic());
			}

		}
		else if (rawData.getRet().equals(RetNumber.REJECT)) {

			this.destNodeResultDescription = LogDestNodeResultDesc.REJECT.getLogDestNodeResultDesc();

			statistic = Statistic.GSSO_RECEIVED_USMP_PORTCHECK_RESPONSE_REJECT.getStatistic();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				/** ADD STAT DEBUG INPUT LOG **/
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_PORTCHECK_RESPONSE_REJECT.getStatistic());
			}

		}
		else if (rawData.getRet().equals(RetNumber.ABORT)) {

			this.destNodeResultDescription = LogDestNodeResultDesc.ABORT.getLogDestNodeResultDesc();

			statistic = Statistic.GSSO_RECEIVED_USMP_PORTCHECK_RESPONSE_ABORT.getStatistic();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				/** ADD STAT DEBUG INPUT LOG **/
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_PORTCHECK_RESPONSE_ABORT.getStatistic());
			}

		}

		this.ec02Instance.incrementsStat(statistic);

		isSummaryEnable = true;
		ResponseMessage.returnMessage_Error(origInvokeProcess, rawDatasOutgoing, abstractAF, ec02Instance, listCode, listDescription,
				listOrigInvoke, composeDebugLog, false);

	}

	private void portChkInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {
		this.rawDataInput = equinoxRawData;

		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
		this.abstractAF = abstractAF;

		/** EquinoxRawData Outgoing **/
		this.rawDatasOutgoing = new ArrayList<EquinoxRawData>();

		this.listCode = new ArrayList<String>();
		this.listDescription = new ArrayList<String>();
		this.listOrigInvoke = new ArrayList<String>();
		this.origInvokeProcess = InvokeFilter.getOriginInvoke(rawDataInput.getInvoke());

		/** INITIAL DETAILS LOG **/
		this.composeDetailsLog = new GssoComposeDetailsLog(appInstance, abstractAF.getEquinoxProperties().getState() + "." + "BEGIN",
				abstractAF);

		this.composeDetailsLog.setDataOrig(origInvokeProcess, this.rawDataInput, appInstance);
		this.mapDetails = new MapDetailsAndConfigType();

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			/** INITIAL DEBUG LOG **/
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
		}


		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvokeProcess);
		startTimeOfInvokeIncoming = origInvokeProfile.getStartTimeOfInvoke();
		
		/** SET DTAILS IDENTITY **/
		this.composeDetailsLog.setIdentity(origInvokeProfile.getDetailsService());


		/** INITIATE SUMMARY LOG **/
		this.composeSummary = new GssoComposeSummaryLog(abstractAF, origInvokeProfile.getDetailsService());
		
		/** IDLE_SEND_OTP_REQ **/
		if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())
				|| origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_SOAP.getMessageType())) {
			this.nodeCommand = EventLog.SEND_OTP.getEventLog();
		}
		/** generatePasskey **/
		else {
			this.nodeCommand = EventLog.GENARATE_PASSKEY.getEventLog();
		}

		appInstance.getMapOrigInvokeEventDetailInput().put(origInvokeProcess, EventLog.PORT_CHECK.getEventLog());

		appInstance.getMapOrigInvokeDetailScenario().put(origInvokeProcess, origInvokeProfile.getScenarioName());

		/** ADD DETAIL INPUT LOG **/
		try {
			this.composeDetailsLog.initialIncoming(rawDataInput, appInstance);
			this.composeDetailsLog.addScenario(appInstance, equinoxRawData, origInvokeProcess);
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
	}

	private void portChkSaveLog() {

		/** WRITE SUMMARY LOG **/
		if (isSummaryEnable) {

			for (int i = 0; i < listCode.size(); i++) {

				try {
//					this.composeSummary = new GssoComposeSummaryLog(abstractAF);

					if (!this.destNodeResultDescription.equals("null")) {

						this.composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode,
								destNodeResultDescription);
					}

					this.composeSummary.setWriteSummary();
					this.composeSummary.initialSummary(this.appInstance, 

					startTimeOfInvokeIncoming, this.listOrigInvoke.get(i),// invokeRawdata
							this.nodeCommand, //
							this.listCode.get(i), this.listDescription.get(i));

					this.composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), origInvokeProcess);

				}
				catch (Exception e) {
					Log.e(e.getMessage());
				}

				if (this.composeSummary.isWriteSummary()) {
					this.appInstance.getListSummaryLog().add(this.composeSummary.getSummaryLog());
				}
			}
		}

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog.initialGssoSubStateLog(rawDataInput);
			this.composeDebugLog.writeDebugSubStateLog();
		}

		/** ADD DETAIL OUTPUT LOG **/
		int outPutSize = this.rawDatasOutgoing.size();

		if (outPutSize != 0) {
			for (EquinoxRawData rawDataOut : this.rawDatasOutgoing) {
				try {
					this.composeDetailsLog.initialOutgoing(rawDataOut, appInstance, outPutSize);
					this.composeDetailsLog.addScenario(appInstance, rawDataOut, this.nextState);
				}
				catch (Exception e) {
					Log.e(e.getMessage());
				}
			}
		}
		else {
			try {
				this.composeDetailsLog.initialOutgoingToE01(abstractAF, appInstance);
			}
			catch (Exception e) {
				Log.e(e.getMessage());
			}
		}

		/** SAVE DETAILS **/
		this.mapDetails.setDetail(this.composeDetailsLog.getDetailsLog());
		this.appInstance.getListDetailsLog().add(mapDetails);
	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data) {
		// TODO Auto-generated method stub
		return null;
	}

}
