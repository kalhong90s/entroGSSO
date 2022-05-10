package com.ais.eqx.gsso.substates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.GssoCommand;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.LogDestNodeResultDesc;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GssoProfile;
import com.ais.eqx.gsso.instances.InquiryVASSubscriber;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.OperationStatusOfVas;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.SubscriberOfVas;
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

public class W_INQUIRY_VAS_SUB implements IAFSubState {

	private EC02Instance			ec02Instance;
	private APPInstance				appInstance;

	private String					nextState;

	ArrayList<EquinoxRawData>		rawDatasOutgoing;
	private InquiryVASSubscriber	inquiryVASSubscriber;

	private GssoComposeDebugLog		composeDebugLog;
	private GssoComposeDetailsLog	composeDetailsLog;
	private GssoComposeSummaryLog	composeSummary;
	private MapDetailsAndConfigType	mapDetails;
	private EquinoxRawData			rawDataInput;
	private AbstractAF				abstractAF;

	private boolean					isSummaryEnable				= false;
	private ArrayList<String>		listCode;
	private ArrayList<String>		listDescription;
	private String					nodeCommand;
	private ArrayList<String>		listOrigInvoke;
	private long					startTimeOfInvokeIncoming;

	private String					origInvokeProcess			= "";

	private String					destNodeName				= "USMP";
	private String					destNodeResultCode			= "null";
	private String					destNodeCommand				= "InquiryVASSubscriber";
	private String					destNodeResultDescription	= "null";

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		this.nextState = SubStates.W_INQUIRY_VAS_SUB.toString();
		/** W_INQUIRY_SUB **/

		/************** INITIAL *****************/
		inqSubInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);

		/************** CODING ******************/
		 //System.out.println("Start W_INQUIRY_SUB");

		appInstance.setWaitInquirySub(false);

		/** Check Normal **/
		if (RetNumber.NORMAL.equals(equinoxRawData.getRet())) {
			
			try {
				
				this.inquiryVASSubscriber = SoapToObject.parserInquiryVasSub(rawDataInput.getRawDataMessage());
				
				/****** Valify Message ***********/
				VerifyUSMPMessage.inquiryVASSubscriberValidator(this.inquiryVASSubscriber, rawDataInput.getRawDataMessage());

				/* SET INQ SUB SUCCESS */
				appInstance.setInquirySubSuccess(true);

				String usmpCode = this.inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getCode();
				
				if ((usmpCode.equals(USMPCode.VSMP_00000000) || usmpCode.equals(USMPCode.VSMP_01040001) || usmpCode
						.equals(USMPCode.VSMP_01040003))) {

					this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_SUCCESS.getStatistic());
					
					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.messageResponseSuccess(usmpCode);
						this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_SUCCESS
								.getStatistic());
						this.composeDebugLog.messageResponseSuccess(usmpCode);
					}

					successCase(rawDataInput, abstractAF, origInvokeProcess);
				}
				else {
					String usmpDescription = inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus().getDescription();

					this.destNodeResultCode = usmpCode;
					this.destNodeResultDescription = usmpDescription;

					this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_RESULTCODE_ERROR
							.getStatistic());

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_RESULTCODE_ERROR
								.getStatistic());
						this.composeDebugLog.messageResponseFailed(usmpCode);
					}

					isSummaryEnable = true;
					ResponseMessage.returnMessage_Error(origInvokeProcess, rawDatasOutgoing, abstractAF, ec02Instance, listCode,
							listDescription, listOrigInvoke, composeDebugLog, false);

				}

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
				}

			}
			catch (ValidationException validate) {

				this.destNodeResultDescription = validate.getMandatoryPath() + " " + validate.getMessage();

				this.nextState = SubStates.END.toString();

				this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_USMP_INQUIRYVASSUBSCRIBER_RESPONSE.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.setFailureAvp(validate.getMandatoryPath() + " " + validate.getMessage());
					this.composeDebugLog.messageResponseFailed(validate.getResultCode());
					this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_USMP_INQUIRYVASSUBSCRIBER_RESPONSE.getStatistic());
					this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				}

				this.mapDetails.setNoFlow();

				isSummaryEnable = true;
				ResponseMessage.returnMessage_Error(origInvokeProcess, rawDatasOutgoing, abstractAF, ec02Instance, listCode,
						listDescription, listOrigInvoke, composeDebugLog, false);

			}

		}
		/** TIMEOUT **/
		else if (RetNumber.TIMEOUT.equals(equinoxRawData.getRet())) {

			this.nextState = SubStates.END.toString();
			this.destNodeResultDescription = LogDestNodeResultDesc.CONNECTION_TIMEOUT.getLogDestNodeResultDesc();

			ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_REREQUEST_TIMEOUT.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_REREQUEST_TIMEOUT.getStatistic());
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
			errorCase(equinoxRawData);

		}

		/* SAVE LOG */
		inqSubSaveLog();

		return this.rawDatasOutgoing;
	}

	private void successCase(EquinoxRawData rawDataInput, AbstractAF abstractAF, String origInvoke) {

		OperationStatusOfVas operation = this.inquiryVASSubscriber.getInquiryVASSubscriberResult().getOperationStatus();

		/** Code is VSMP-00000000 **/
		GssoProfile profile = this.appInstance.getProfile();
		if (operation.getCode().equals(USMPCode.VSMP_00000000)) {

			SubscriberOfVas subScriber = this.inquiryVASSubscriber.getInquiryVASSubscriberResult().getSubscriber();
			
			/** active_State from EC02 configuration **/
			String[] active_State = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.ACTIVE_STATE));

			/** State existed in Active-State **/
			if (Arrays.asList(active_State).contains(subScriber.getState())) {
				profile.setOper(OperName.AIS);

				/** Set Instance Profile(Cos,Language, CustomerId) **/
				profile.setCos(subScriber.getCos());
				profile.setLanguage(subScriber.getLanguage());
				profile.setCustomerId(subScriber.getCustomerId());

				OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvokeProcess);

				/** PermissionAccountType is Allow **/
				if (GssoDataManagement.checkPermissionAccountType(origInvokeProfile, appInstance)) {
					
					if(origInvokeProfile.getSendWSOTPRequest()!=null 
							&& (origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP) 
							|| origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID))){
						/** check USMP 2 times if it has OTPMobile **/
						if((origInvokeProfile.getSendWSOTPRequest().getOtpMobile()!=null 
								&& !origInvokeProfile.getSendWSOTPRequest().getOtpMobile().isEmpty())){
							
								String msisdnNum = origInvokeProfile.getSendWSOTPRequest().getMsisdn();
								String otpMo = origInvokeProfile.getSendWSOTPRequest().getOtpMobile();
							
							if(!origInvokeProfile.isSendUSMPSecond() && !isDuplicateNumber(msisdnNum, otpMo)){
								this.rawDatasOutgoing.add(GssoConstructMessage.createInquirySubReqToUSMPMessageFromWSAuthenOTPSecondTIme(origInvokeProfile.getOrigEquinoxRawData(), ec02Instance,
										origInvokeProfile.getSendWSOTPRequest(), composeDebugLog));
										origInvokeProfile.setSendUSMPSecond(true);
							}
							/** Msisdn and OTPMonile is duplicate or Send second time**/
							else{
								GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, origInvokeProcess, abstractAF, composeDebugLog);
							}
							
						}
						/** For only email case **/
						else{
							GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, origInvokeProcess, abstractAF, composeDebugLog);
						}
					}
					/** normal command**/
					else{
						GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, origInvokeProcess, abstractAF, composeDebugLog);
					}
				}
				/** PermissionAccountType is not Allow **/
				else {
					isSummaryEnable = ResponseMessage.composeErrorMessage(origInvokeProcess, rawDatasOutgoing, abstractAF,
							ec02Instance, listCode, listDescription, listOrigInvoke, composeDebugLog);
				}

				/** Check Permission From Account Type In ListWaiting **/
				Iterator<String> listWaitInqSub = appInstance.getListWaitInquirySub().iterator();
				while (listWaitInqSub.hasNext()) {

					String origInvokeWaiting = (String) listWaitInqSub.next();
					OrigInvokeProfile origInvokeProfileWaiting = appInstance.getMapOrigProfile().get(origInvokeWaiting);

					/** PermissionAccountType is Allow **/
					if (GssoDataManagement.checkPermissionAccountType(origInvokeProfileWaiting, appInstance)) {
						GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, origInvokeWaiting, abstractAF,
								composeDebugLog);
					}
					/** PermissionAccountType is not Allow **/
					else {
						isSummaryEnable = ResponseMessage.composeErrorMessage(origInvokeWaiting, rawDatasOutgoing, abstractAF,
								ec02Instance, listCode, listDescription, listOrigInvoke, composeDebugLog);
					}
					listWaitInqSub.remove();
				}

			}

			/** State doesn’t exist in Active-State **/
			else {

				isSummaryEnable = ResponseMessage.returnMessage_NotUseService(origInvokeProcess, rawDatasOutgoing, abstractAF,
						ec02Instance, listCode, listDescription, listOrigInvoke, composeDebugLog);
			}

		}

		/**
		 * Code is VSMP-01040001 create To USMP *** V2.0 not have function PortCheck
		 * **/
//		else if (operation.getCode().equals(USMPCode.VSMP_01040001)) {
//
//			rawDatasOutgoing.add(GssoConstructMessage.createPortCheckToUSMPMessage(origInvoke, rawDataInput, ec02Instance));
//
//			/** GSSO Send USMP PortCheck Request STATICTIC **/
//			ec02Instance.incrementsStat(Statistic.GSSO_SEND_USMP_PORTCHECK_REQUEST.getStatistic());
//
//			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
//				this.composeDebugLog.addStatisticOut(Statistic.GSSO_SEND_USMP_PORTCHECK_REQUEST.getStatistic());
//			}
//		}

		/** Code is VSMP-01040003 and VSMP-01040001 **/
		else if (operation.getCode().equals(USMPCode.VSMP_01040003) || operation.getCode().equals(USMPCode.VSMP_01040001)) {

			profile.setOper(OperName.NonAIS);

			OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvokeProcess);

			/** Check AccountType **/
			if (GssoDataManagement.checkPermissionAccountType(origInvokeProfile, appInstance)) {
				/** PermissionAccountType is Allow **/
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
				String origInvokeWaiting = (String) listWaitInqSub.next();
				OrigInvokeProfile origInvokeProfileWaiting = appInstance.getMapOrigProfile().get(origInvokeWaiting);

				/** PermissionAccountType is Allow **/
				if (GssoDataManagement.checkPermissionAccountType(origInvokeProfileWaiting, appInstance)) {
					GssoConstructMessage.createMessageQuiryE01Template(ec02Instance, origInvokeWaiting, abstractAF, composeDebugLog);
				}
				/** PermissionAccountType is not Allow **/
				else {
					isSummaryEnable = ResponseMessage.composeErrorMessage(origInvokeWaiting, rawDatasOutgoing, abstractAF,
							ec02Instance, listCode, listDescription, listOrigInvoke, composeDebugLog);
				}
				listWaitInqSub.remove();
			}

		}

		/** Code parameter and Desc parameter as "Fail" **/
		else {

			isSummaryEnable = true;
			ResponseMessage.returnMessage_Error(origInvokeProcess, rawDatasOutgoing, abstractAF, ec02Instance, listCode,
					listDescription, listOrigInvoke, composeDebugLog, false);
		}
	}

	private void errorCase(EquinoxRawData rawData) {
		String statistic = "";

		if (rawData.getRet().equals(RetNumber.ERROR)) {
			this.destNodeResultDescription = LogDestNodeResultDesc.ERROR.getLogDestNodeResultDesc();

			statistic = Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_ERROR.getStatistic();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				/** ADD STAT DEBUG INPUT LOG **/
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_ERROR.getStatistic());
			}

		}
		else if (rawData.getRet().equals(RetNumber.REJECT)) {
			this.destNodeResultDescription = LogDestNodeResultDesc.REJECT.getLogDestNodeResultDesc();
			statistic = Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_REJECT.getStatistic();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				/** ADD STAT DEBUG INPUT LOG **/
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_REJECT.getStatistic());
			}

		}
		else if (rawData.getRet().equals(RetNumber.ABORT)) {
			this.destNodeResultDescription = LogDestNodeResultDesc.ABORT.getLogDestNodeResultDesc();
			statistic = Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_ABORT.getStatistic();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				/** ADD STAT DEBUG INPUT LOG **/
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_USMP_INQUIRYVASSUBSCRIBER_RESPONSE_ABORT.getStatistic());
			}

		}

		this.ec02Instance.incrementsStat(statistic);

		isSummaryEnable = true;
		ResponseMessage.returnMessage_Error(origInvokeProcess, rawDatasOutgoing, abstractAF, ec02Instance, listCode, listDescription,
				listOrigInvoke, composeDebugLog, false);

	}

	private void inqSubInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {
		this.rawDataInput = equinoxRawData;
		this.abstractAF = abstractAF;

		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();

		/** EquinoxRawData Outgoing **/
		this.rawDatasOutgoing = new ArrayList<EquinoxRawData>();

		this.origInvokeProcess = InvokeFilter.getOriginInvoke(equinoxRawData.getInvoke());

		/** INITIAL DEBUG LOG **/
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
		}

		/** INITIAL DETAILS LOG **/
		this.composeDetailsLog = new GssoComposeDetailsLog(appInstance, abstractAF.getEquinoxProperties().getState() + "." + "BEGIN",
				abstractAF);

		this.composeDetailsLog.setDataOrig(origInvokeProcess, rawDataInput, appInstance);
		this.mapDetails = new MapDetailsAndConfigType();

		OrigInvokeProfile origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvokeProcess);
		this.startTimeOfInvokeIncoming = origInvokeProfile.getStartTimeOfInvoke();
		
		/** SET DTAILS IDENTITY **/
		this.composeDetailsLog.setIdentity(origInvokeProfile.getDetailsService());

		/** IDLE_SEND_OTP_REQ **/
		if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())
				|| origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_SOAP.getMessageType())) {
			this.nodeCommand = EventLog.SEND_OTP.getEventLog();
		}
		/** IDLE_WS_AUTHEN_OTP_REQ **/
		else if(origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_SOAP.getMessageType())){
			this.nodeCommand = EventLog.WS_AUTHEN_OTP.getEventLog();
		}
		/** IDLE_WS_AUTHEN_OTP_ID_REQ **/
		else if(origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_ID_SOAP.getMessageType())){
			this.nodeCommand = EventLog.WS_AUTHEN_OTP_ID.getEventLog();
		}
		/** IDLE_WS_CREATE_OTP_REQ **/
		else if(origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_CREATE_OTP_SOAP.getMessageType())){
			this.nodeCommand = EventLog.WS_CREATE_OTP.getEventLog();
		}
		/** IDLE_WS_GENERATE_OTP_REQ **/
		else if(origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_GENERATE_ONETIMEPW_SOAP.getMessageType())){
			this.nodeCommand = EventLog.WS_GENERATE_OTP.getEventLog();
		}
		/** generatePasskey **/
		else {
			this.nodeCommand = EventLog.GENARATE_PASSKEY.getEventLog();
		}

		/** INITIATE SUMMARY LOG **/
		this.composeSummary = new GssoComposeSummaryLog(abstractAF, origInvokeProfile.getDetailsService());

		this.listCode = new ArrayList<String>();
		this.listDescription = new ArrayList<String>();
		this.listOrigInvoke = new ArrayList<String>();

		appInstance.getMapOrigInvokeEventDetailInput().put(origInvokeProcess, EventLog.INQUIRY_VASSUBSCRIBER.getEventLog());

		appInstance.getMapOrigInvokeDetailScenario().put(origInvokeProcess,
				appInstance.getMapOrigProfile().get(origInvokeProcess).getScenarioName());

		/** ADD DETAIL INPUT LOG **/
		try {
			this.composeDetailsLog.initialIncoming(rawDataInput, appInstance);
			this.composeDetailsLog.addScenario(appInstance, equinoxRawData, origInvokeProcess);
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
	}

	private void inqSubSaveLog() {

		/** WRITE SUMMARY LOG **/
		if (isSummaryEnable) {

			for (int i = 0; i < listCode.size(); i++) {
				try {

//					OrigInvokeProfile origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvokeProcess);
//					this.composeSummary = new GssoComposeSummaryLog(abstractAF, origInvokeProfile.getDetailsService());

					if (!this.destNodeResultDescription.equals("null")) {

						this.composeSummary.addDestinationBean(destNodeName, destNodeCommand, destNodeResultCode,
								destNodeResultDescription);
					}

					this.composeSummary.setWriteSummary();
					this.composeSummary.initialSummary(this.appInstance, startTimeOfInvokeIncoming, this.listOrigInvoke.get(i),// invokeRawdata
							this.nodeCommand, this.listCode.get(i), this.listDescription.get(i));

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
		return null;
	}
	
	public boolean isDuplicateNumber(String msisdn, String otpMo){
		if(msisdn.equals(otpMo)){
			return true;
		}
		return false;
	}
}