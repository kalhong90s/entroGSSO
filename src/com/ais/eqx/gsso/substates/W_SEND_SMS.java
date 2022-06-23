package com.ais.eqx.gsso.substates;

import java.text.SimpleDateFormat;
import java.util.*;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.GssoCommand;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.LogDestNodeResultDesc;
import com.ais.eqx.gsso.enums.SMPPResultCode;
import com.ais.eqx.gsso.enums.SoapResultCode;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.DeliveryReportLog;
import com.ais.eqx.gsso.instances.DestinationBean;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.MapDestinationBean;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.SubmitSMJsonFormatRes;
import com.ais.eqx.gsso.instances.SubmitSMResponse;
import com.ais.eqx.gsso.instances.SubmitSMXMLFormatRes;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.GssoMessageType;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.interfaces.OTPChannel;
import com.ais.eqx.gsso.interfaces.RetNumber;
import com.ais.eqx.gsso.interfaces.SentOTPResult;
import com.ais.eqx.gsso.jaxb.InstanceContext;
import com.ais.eqx.gsso.jaxb.JAXBHandler;
import com.ais.eqx.gsso.utils.*;
import com.ais.eqx.gsso.validator.SmsEmailDeliveryValidator;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class W_SEND_SMS implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;
	private SubmitSMResponse			sendsmReq;
	private ArrayList<EquinoxRawData>	rawDatasOut;
	private String						nextState;
	private String						messageType;
	private String						otpChannel;

	private String						isEmailDone;
	private String						waitDR;
	private String						origInvoke;
	private String						smscDeliveryReceipt;
	private String						refundFlag;

	private EquinoxRawData				rawDataInput;
	private EquinoxRawData				rawDataOrig;
	private OrigInvokeProfile			origInvokeProfile;

	private String						isSuccessTrue				= "true";
	private String						isSuccessFalse				= "false";
	private long						startTimeOfInvokeIncoming;

	private boolean						isWaitDREnable;
	private boolean						isSMSCDeliveryReceipt;
	private boolean 					isRefundFlag;

	private String						destNodeName				= "SMPPGW";

	private GssoComposeDetailsLog		composeDetailsLog;
	private MapDetailsAndConfigType		mapDetails;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;

	private String						code;
	private String						description					= "";
	private String						nodeCommand					= "";

	private String						destNodeResultDescription	= "null";
	private String						destNodeResultCode			= "null";
	private String						destNodeCommand				= "SubmitSM";

	private boolean						isWriteSummary				= false;
	private String						event;
	
	private String						sessionId;
	private String						refId;
	private String						msisdn;
	
	private ArrayList<String> 			enableCommandsToRefund 		= new ArrayList<String>();
	private boolean completely			= false;


	// MAIN WAIT SMS RESPONSE
	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		this.nextState = SubStates.W_SEND_SMS.toString();

		/************** INITIAL *****************/
		sendSmsInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);
		rawDatasOut = origInvokeProfile.getRawDatasOut();

		origInvokeProfile.setSmsIncoming(origInvokeProfile.getSmsIncoming()+1);
		completely = origInvokeProfile.getSmsIncoming()==origInvokeProfile.getSmsOutgoing();
		Log.d("###########  Count SMS IncommingMsg :"+origInvokeProfile.getSmsIncoming()+"/"+origInvokeProfile.getSmsOutgoing()+" ########### ");

		/************** CODING ******************/
//		System.out.println("Start W_SEND_SMS");

		// NORMAL
		if (RetNumber.NORMAL.equals(equinoxRawData.getRet())) {

			try {
				sendsmReq = (SubmitSMResponse) JAXBHandler.createInstance(InstanceContext.getSubmitSMResponseContext(),
						rawDataInput.getRawDataMessage(), SubmitSMResponse.class);

				// EXTRACT THE FOLLOWING MANDATORY FIELDS
				SmsEmailDeliveryValidator.sendSMSValidator(sendsmReq, equinoxRawData);

				// SET MESSAGE ID TO INSTANCE
				origInvokeProfile.setSmMessageId("" + Long.parseLong(sendsmReq.getMessageId(), 16));
				origInvokeProfile.getMsgIdList().add("" + Long.parseLong(sendsmReq.getMessageId(), 16));
				this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_SMPPGW_SUBMITSM_RESPONSE_SUCCESS.getStatistic());

				// RECEIVING THE VALID SUBMITSM RESPONSE
				if(completely){
					if(rawDatasOut.size() == 0 && !origInvokeProfile.isSmsError()){
						normalCaseSuccess();
					}else {
						removeWaitDr();
					}

				}else if(isSMSCDeliveryReceipt || isWaitDREnable) {
					GssoDataManagement.setTimeoutOfWaitDR(appInstance, origInvoke);
					origInvokeProfile.setSubmitSmRespTime(this.composeDetailsLog.getDetailTimeIncoming());
				}


				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_SMPPGW_SUBMITSM_RESPONSE_SUCCESS.getStatistic());
					this.composeDebugLog.smsResponseSuccess(sendsmReq.getMessageId());
					this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
				}

			}
			catch (ValidationException validate) {

				if (validate.getMandatoryPath().equalsIgnoreCase("ecode is")) {

					String ecodeDec = rawDataInput.getRawDataAttribute("ecode");
					String ecodeHex = Integer.toHexString(Integer.parseInt(ecodeDec));
					String statusCode = "";
					String smppErrorMessage = "";
					if (ecodeHex.length() >= 3) {
						statusCode = ecodeHex;
						smppErrorMessage = SMPPResultCode.getErrorMessageFrom(statusCode);
					}
					else if (ecodeHex.length() >= 2) {
						statusCode = "0" + ecodeHex;
						smppErrorMessage = SMPPResultCode.getErrorMessageFrom(statusCode);
					}
					else if (ecodeHex.length() >= 1) {
						statusCode = "00" + ecodeHex;
						smppErrorMessage = SMPPResultCode.getErrorMessageFrom(statusCode);
					}
					else {
						statusCode = "000" + ecodeHex;
						smppErrorMessage = SMPPResultCode.getErrorMessageFrom(statusCode);
					}

					this.destNodeResultCode = statusCode;
					this.destNodeResultDescription = smppErrorMessage;
				}
				else {
					this.destNodeResultDescription = validate.getMandatoryPath() + " " + validate.getMessage();
				}

				this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_SMPPGW_SUBMITSM_RESPONSE.getStatistic());

				this.mapDetails.setNoFlow();

				// MANDATORY PARAMETERS IS MISSING OR ANY OF OPTIONAL PARAMETERS
				// IS CONFLICTION
				if(rawDatasOut.size() ==0){
					errorCase();
					removeWaitDr();
				}else {
					removeWaitDr();
					if(completely && appInstance.getMapTimeoutOfWaitRefund().isEmpty()){
						GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					}
				}

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_SMPPGW_SUBMITSM_RESPONSE.getStatistic());
					this.composeDebugLog.setFailureAvp(validate.getMandatoryPath() + " " + validate.getMessage());
					this.composeDebugLog.smsResponseFailed(validate.getResultCode());
					this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				}
			}
		}
		// TIMEOUT ERROR REJECT ABOUT
		else {
			// UNNORMAL

			this.mapDetails.setNoFlow();
			unNormalCase();
			removeWaitDr();


			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.setMessageValidator("-");
			}

		}

		/* SAVE LOG */
		sendSmsSaveLog();

		origInvokeProfile.setRawDatasOut(rawDatasOut);
		if(completely){
			return this.rawDatasOut;

		}
		return null;
	}

	private void normalCaseSuccess() {
		boolean isWriteDRLog = false;

		this.appInstance.getMapOrigProfile().get(origInvoke).getMapSentOTPResult().put(SentOTPResult.IS_SMS, SentOTPResult.SUCCESS);
		// OPTCHANNEL IS SMS
		if (OTPChannel.SMS.equalsIgnoreCase(otpChannel)) {
			if (!this.isWaitDREnable) {
				GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

				if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {
					this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SUCCESS.getCode(),
							JsonResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

					this.code = JsonResultCode.SUCCESS.getCode();
					this.description = JsonResultCode.SUCCESS.getDescription();

				} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {
					this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SUCCESS.getCode(),
							SoapResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

					this.code = SoapResultCode.SUCCESS.getCode();
					this.description = SoapResultCode.SUCCESS.getDescription();

				}
				GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

				if (isSMSCDeliveryReceipt) {
					GssoDataManagement.setTimeoutOfWaitDR(appInstance, origInvoke);
					origInvokeProfile.setSubmitSmRespTime(this.composeDetailsLog.getDetailTimeIncoming());
				} else {
					isWriteDRLog = true;
					isWriteSummary = true;
				}
			} else {
				GssoDataManagement.setTimeoutOfWaitDR(appInstance, origInvoke);
				origInvokeProfile.setSubmitSmRespTime(this.composeDetailsLog.getDetailTimeIncoming());
			}

		}
		// OPTCHANNAL IS 'E-MAIL' AND 'SMS'
		else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {
			// 'WAITDR' IS OFF
			if (!this.isWaitDREnable) {
				// SMS SUCCESS and DOES NOT RECEIVE E-MAIL YET
				if (isEmailDone == null) {
					// this.nextState = SubStates.W_SEND_EMAIL.toString();

				}
				// SMS SUCCESS and E-MAIL ERROR
				else if (SentOTPResult.ERROR.equalsIgnoreCase(isEmailDone)) {
					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL
								.getCode(), JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));

						this.code = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						this.description = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

					} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL
								.getCode(), SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));

						this.code = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

					}

					GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);
				}
				// SMS SUCCESS and RECEIVE E-MAIL 'TIMEOUT'
				else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isEmailDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT
								.getCode(), JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));

						this.code = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getCode();
						this.description = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription();

					} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL
								.getCode(), SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));

						this.code = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

					}

					GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					this.nextState = SubStates.END.toString();
				}
				// SMS SUCCESS and E-MAIL SUCCESS
				else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isEmailDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SUCCESS.getCode(),
								JsonResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

						this.code = JsonResultCode.SUCCESS.getCode();
						this.description = JsonResultCode.SUCCESS.getDescription();

					} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SUCCESS.getCode(),
								SoapResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

						this.code = SoapResultCode.SUCCESS.getCode();
						this.description = SoapResultCode.SUCCESS.getDescription();

					}

					GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					this.nextState = SubStates.END.toString();
				}
				if (isSMSCDeliveryReceipt) {
					GssoDataManagement.setTimeoutOfWaitDR(appInstance, origInvoke);
					origInvokeProfile.setSubmitSmRespTime(this.composeDetailsLog.getDetailTimeIncoming());
				} else {
					isWriteDRLog = true;
					if (isEmailDone != null) {
						isWriteSummary = true;
					}
				}
			}
			// 'WAITDR' IS ON
			else {
				GssoDataManagement.setTimeoutOfWaitDR(appInstance, origInvoke);
				origInvokeProfile.setSubmitSmRespTime(this.composeDetailsLog.getDetailTimeIncoming());

				// SMS SUCCESS and remain Email Response
				if (isEmailDone == null) {
					this.nextState = SubStates.W_SEND_EMAIL.toString();
				}
				// SMS SUCCESS and Email Error
				else if (SentOTPResult.ERROR.equalsIgnoreCase(isEmailDone)) {
					this.nextState = SubStates.W_DELIVERY_REPORT.toString();
				}
				// SMS SUCCESS and Email Timeout
				else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isEmailDone)) {
					this.nextState = SubStates.W_DELIVERY_REPORT.toString();
				}
				// SMS SUCCESS and Email Success
				else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isEmailDone)) {
					this.nextState = SubStates.W_DELIVERY_REPORT.toString();
				}
			}
		}
		if (isWriteDRLog) {

			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd:HHmmss,SSS");// dd/MM/yyyy
			Date now = new Date();
			String dateToString = sdfDate.format(now);
			DeliveryReportLog deliveryReportLog = new DeliveryReportLog();
			if(origInvokeProfile.getSendWSOTPRequest()!=null){
				deliveryReportLog.setMsisdn(origInvokeProfile.getSendWSOTPRequest().getMsisdn());
				deliveryReportLog.setServiceName(origInvokeProfile.getSendWSOTPRequest().getService());
			}else{
				deliveryReportLog.setMsisdn(origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getMsisdn());
				deliveryReportLog.setServiceName(origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getService());
			}
			
			deliveryReportLog.setDateTime(dateToString);
			deliveryReportLog.setServiceKey(origInvokeProfile.getServiceKey());
			deliveryReportLog.setTransactionID(origInvokeProfile.getTransactionID());
			deliveryReportLog.setOrderRef(origInvokeProfile.getOrderRefLog());
			deliveryReportLog.setMessageId(origInvokeProfile.getSmMessageId());
			deliveryReportLog.setSmResultCode(SMPPResultCode.ESME_ROK.getCode());
			deliveryReportLog.setSmErrorMessage(SMPPResultCode.ESME_ROK.getErrorMessage());
			deliveryReportLog.setSmResponseTime((this.composeDetailsLog.getDetailTimeIncoming() - origInvokeProfile
					.getSubmitSmRequestTime()) + "");

			deliveryReportLog.setDrResultCode("");
			deliveryReportLog.setDrErrorMessage("No SMSC Delivery Receipt requested");
			deliveryReportLog.setDrReport("");
			deliveryReportLog.setDrResponseTime("0");

			deliveryReportLog.setResponseTime((this.composeDetailsLog.getDetailTimeIncoming() - origInvokeProfile
					.getSubmitSmRequestTime()) + "");

			ec02Instance.writeLog(true, ConfigureTool.getConfigureLogName(ConfigName.DELIVERY_REPORT_LOG_NAME.getName()),
					deliveryReportLog.toString());

			/* ALL AND PASS EMAIl FLOW */
			if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {
				if (isEmailDone != null) {
					/* REMOVE PROFILE */
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
				}
			}
			else {
				GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
			}
		}

	}

	private void unNormalCase() {

		if (RetNumber.TIMEOUT.equalsIgnoreCase(rawDataInput.getRet())) {

			this.destNodeResultDescription = LogDestNodeResultDesc.CONNECTION_TIMEOUT.getLogDestNodeResultDesc();

			this.appInstance.getMapOrigProfile().get(origInvoke).getMapSentOTPResult()
					.put(SentOTPResult.IS_SMS, SentOTPResult.TIMEOUT);

			this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_SMPPGW_SUBMITSM_REQUEST_TIMEOUT.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_SMPPGW_SUBMITSM_REQUEST_TIMEOUT.getStatistic());
			}
			if(rawDatasOut.size() ==0){
				if (OTPChannel.SMS.equalsIgnoreCase(otpChannel)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT.getCode(),
								JsonResultCode.SEND_SMS_TIMEOUT.getDescription(), isSuccessFalse).toRawDatas(appInstance));

						this.code = JsonResultCode.SEND_SMS_TIMEOUT.getCode();
						this.description = JsonResultCode.SEND_SMS_TIMEOUT.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL.getCode(),
								SoapResultCode.SEND_SMS_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

						this.code = SoapResultCode.SEND_SMS_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_FAIL.getDescription();

					}

					GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					/* Refund */
					if(this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())){
						this.rawDatasOut.add(GssoConstructMessage.createRefundReqTorPCEFMessage(rawDataInput, ec02Instance, this.sessionId, this.refId, this.msisdn, composeDebugLog));
						this.nextState = SubStates.W_REFUND.toString();
						isWriteSummary = true;
					}
					else{
						this.nextState = SubStates.END.toString();

						/* REMOVE PROFILE */
						if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;
					}
				}
				else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {

					if (isEmailDone == null) {

						this.nextState = SubStates.W_SEND_EMAIL.toString();
						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.code = JsonResultCode.SEND_SMS_TIMEOUT.getCode();
							this.description = JsonResultCode.SEND_SMS_TIMEOUT.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.code = SoapResultCode.SEND_SMS_FAIL.getCode();
							this.description = SoapResultCode.SEND_SMS_FAIL.getDescription();

						}

					}
					else if (SentOTPResult.ERROR.equalsIgnoreCase(isEmailDone)) {

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL
									.getCode(), JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getDescription(), isSuccessFalse)
									.toRawDatas(appInstance));
							this.code = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getCode();
							this.description = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
									.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
									.toRawDatas(appInstance));
							this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
							this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();
						}

						GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

						/* Refund */
						if(this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())){
							this.rawDatasOut.add(GssoConstructMessage.createRefundReqTorPCEFMessage(rawDataInput, ec02Instance, this.sessionId, this.refId, this.msisdn, composeDebugLog));
							this.nextState = SubStates.W_REFUND.toString();
							isWriteSummary = true;
						}
						else{
							this.nextState = SubStates.END.toString();

							/* REMOVE PROFILE */
							if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
							isWriteSummary = true;
						}
					}
					else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isEmailDone)) {

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT
									.getCode(), JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getDescription(), isSuccessFalse)
									.toRawDatas(appInstance));
							this.code = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getCode();
							this.description = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
									.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
									.toRawDatas(appInstance));
							this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
							this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();
						}

						GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

						/* Refund */
						if(this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())){
							this.rawDatasOut.add(GssoConstructMessage.createRefundReqTorPCEFMessage(rawDataInput, ec02Instance, this.sessionId, this.refId, this.msisdn, composeDebugLog));
							this.nextState = SubStates.W_REFUND.toString();
							isWriteSummary = true;
						}
						else{
							this.nextState = SubStates.END.toString();

							/* REMOVE PROFILE */
							if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
							isWriteSummary = true;
						}

					}
					else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isEmailDone)) {

						GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);
						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS
									.getCode(), JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
									.toRawDatas(appInstance));
							this.code = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getCode();
							this.description = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
									.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
									.toRawDatas(appInstance));
							this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
							this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

						}

						GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

						/* Refund */
						if(this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())){
							this.rawDatasOut.add(GssoConstructMessage.createRefundReqTorPCEFMessage(rawDataInput, ec02Instance, this.sessionId, this.refId, this.msisdn, composeDebugLog));
							this.nextState = SubStates.W_REFUND.toString();
							isWriteSummary = true;
						}
						else{
							this.nextState = SubStates.END.toString();

							/* REMOVE PROFILE */
							if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
							isWriteSummary = true;
						}
					}
				}
			}else {
				removeWaitDr();
				if(completely && appInstance.getMapTimeoutOfWaitRefund().isEmpty()){
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
				}
			}

		}
		else if (RetNumber.ERROR.equalsIgnoreCase(rawDataInput.getRet())) {
			this.destNodeResultDescription = LogDestNodeResultDesc.ERROR.getLogDestNodeResultDesc();

			this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_SMPPGW_SUBMITSM_RESPONSE_ERROR.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_SMPPGW_SUBMITSM_RESPONSE_ERROR.getStatistic());
			}

			if(rawDatasOut.size() ==0){
				errorCase();
				removeWaitDr();
			}else {
				removeWaitDr();
				if(completely && appInstance.getMapTimeoutOfWaitRefund().isEmpty()){
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
				}
			}

		}
		else if (RetNumber.ABORT.equalsIgnoreCase(rawDataInput.getRet())) {
			this.destNodeResultDescription = LogDestNodeResultDesc.ABORT.getLogDestNodeResultDesc();

			this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_SMPPGW_SUBMITSM_RESPONSE_ABORT.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_SMPPGW_SUBMITSM_RESPONSE_ABORT.getStatistic());
			}

			if(rawDatasOut.size() ==0){
				errorCase();
				removeWaitDr();
			}else {
				removeWaitDr();
				if(completely && appInstance.getMapTimeoutOfWaitRefund().isEmpty()){
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
				}
			}
		}
		else if (RetNumber.REJECT.equalsIgnoreCase(rawDataInput.getRet())) {
			this.destNodeResultDescription = LogDestNodeResultDesc.REJECT.getLogDestNodeResultDesc();

			this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_SMPPGW_SUBMITSM_RESPONSE_REJECT.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_SMPPGW_SUBMITSM_RESPONSE_REJECT.getStatistic());
			}

			if(!origInvokeProfile.isBypassUSMP()) {
				Log.d("###########  SmsRetry :"+Integer.parseInt(ConfigureTool.getConfigure(ConfigName.SMS_RETRIES))+"/"+this.appInstance.getMapOrigProfile().get(origInvoke).getSmsRetryLimit()+" ########### ");

				if (this.appInstance.getMapOrigProfile().get(origInvoke).getSmsRetryLimit() >= (Integer.parseInt(ConfigureTool.getConfigure(ConfigName.SMS_RETRIES)))) {
					this.rawDatasOut.clear();
					errorCase();

				} else {
					this.rawDatasOut.clear();
					origInvokeProfile.setSmsIncoming(origInvokeProfile.getSmsIncoming()-1);

					this.appInstance.getMapOrigProfile().get(origInvoke).increaseSMSRetryLimit();

					this.rawDatasOut.add(GssoConstructMessage.createSMSReqMessageForRetry(origInvoke, appInstance));

					this.ec02Instance.incrementsStat(Statistic.GSSO_SEND_SMPPGW_SUBMITSM_REQUEST.getStatistic());

					this.nextState = SubStates.W_SEND_SMS.toString();

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.addStatisticOut(Statistic.GSSO_SEND_SMPPGW_SUBMITSM_REQUEST.getStatistic());
					}

					appInstance.getMapOrigInvokeEventDetailOutput().put(this.rawDatasOut.get(0).getInvoke(), event);

				}
			}else {
				if(rawDatasOut.size() ==0){
					errorCase();
					removeWaitDr();
				}else {
					removeWaitDr();
					if(completely && appInstance.getMapTimeoutOfWaitRefund().isEmpty()){
						GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					}
				}
			}

		}

	}

	private void errorCase() {

		this.appInstance.getMapOrigProfile().get(origInvoke).getMapSentOTPResult().put(SentOTPResult.IS_SMS, SentOTPResult.ERROR);

		if (OTPChannel.SMS.equalsIgnoreCase(otpChannel)) {

			if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

				this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL.getCode(),
						JsonResultCode.SEND_SMS_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

				this.code = JsonResultCode.SEND_SMS_FAIL.getCode();
				this.description = JsonResultCode.SEND_SMS_FAIL.getDescription();

			}
			else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

				this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL.getCode(),
						SoapResultCode.SEND_SMS_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

				this.code = SoapResultCode.SEND_SMS_FAIL.getCode();
				this.description = SoapResultCode.SEND_SMS_FAIL.getDescription();

			}

			GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);
			
			/* Refund */
			if(this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())){
				this.rawDatasOut.add(GssoConstructMessage.createRefundReqTorPCEFMessage(rawDataInput, ec02Instance, this.sessionId, this.refId, this.msisdn, composeDebugLog));
				this.nextState = SubStates.W_REFUND.toString();
				isWriteSummary = true;
			}
			else{
				this.nextState = SubStates.END.toString();

				/* REMOVE PROFILE */
				if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
				isWriteSummary = true;
			}
		}
		else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {

			// E-MAIL REMAIN
			if (isEmailDone == null) {
				origInvokeProfile.setSmsError(true);
				this.nextState = SubStates.W_SEND_EMAIL.toString();

				if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

					this.code = JsonResultCode.SEND_SMS_FAIL.getCode();
					this.description = JsonResultCode.SEND_SMS_FAIL.getDescription();

				}
				else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

					this.code = SoapResultCode.SEND_SMS_FAIL.getCode();
					this.description = SoapResultCode.SEND_SMS_FAIL.getDescription();

				}

			}
			// E-MAIL ERROR
			else if (SentOTPResult.ERROR.equalsIgnoreCase(isEmailDone)) {

				if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

					this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig,
							JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode(), JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL
									.getDescription(), isSuccessFalse).toRawDatas(appInstance));

					this.code = JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
					this.description = JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

				}
				else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

					this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode(),
							SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

					this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
					this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

				}

				GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);
				
				/* Refund */
				if(this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())){
					this.rawDatasOut.add(GssoConstructMessage.createRefundReqTorPCEFMessage(rawDataInput, ec02Instance, this.sessionId, this.refId, this.msisdn, composeDebugLog));
					this.nextState = SubStates.W_REFUND.toString();
					isWriteSummary = true;
				}
				else{
					this.nextState = SubStates.END.toString();

					/* REMOVE PROFILE */
					if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;
				}

			}
			// E-MAIL TIMEOUT
			else if (isEmailDone.equalsIgnoreCase(SentOTPResult.TIMEOUT)) {

				if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

					this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT
							.getCode(), JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getDescription(), isSuccessFalse)
							.toRawDatas(appInstance));

					this.code = JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getCode();
					this.description = JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getDescription();

				}
				else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

					this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode(),
							SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

					this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
					this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

				}

				GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);
				
				/* Refund */
				if(this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())){
					this.rawDatasOut.add(GssoConstructMessage.createRefundReqTorPCEFMessage(rawDataInput, ec02Instance, this.sessionId, this.refId, this.msisdn, composeDebugLog));
					this.nextState = SubStates.W_REFUND.toString();
					isWriteSummary = true;
				}
				else{
					this.nextState = SubStates.END.toString();

					/* REMOVE PROFILE */
					if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;
				}

			}
			// E-MAIL SUCCESS
			else if (isEmailDone.equalsIgnoreCase(SentOTPResult.SUCCESS)) {

				GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

				if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

					this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
							.getCode(), JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
							.toRawDatas(appInstance));

					this.code = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
					this.description = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

				}
				else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

					this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
							.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
							.toRawDatas(appInstance));
					this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
					this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();
				}


				GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);
				
				/* Refund */
				if(this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())){
					this.rawDatasOut.add(GssoConstructMessage.createRefundReqTorPCEFMessage(rawDataInput, ec02Instance, this.sessionId, this.refId, this.msisdn, composeDebugLog));
					this.nextState = SubStates.W_REFUND.toString();
					isWriteSummary = true;
				}
				else{
					this.nextState = SubStates.END.toString();

					/* REMOVE PROFILE */
					if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;
				}
			}
		}
	}

	private void sendSmsInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {
		this.rawDataInput = equinoxRawData;
		this.rawDatasOut = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();

		this.origInvoke = InvokeFilter.getOriginInvoke(equinoxRawData.getInvoke());
		this.origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvoke);
		this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
		
		this.enableCommandsToRefund = GssoDataManagement.enableCommandsToRefund(ConfigureTool.getConfigure(ConfigName.COMMANDS_TO_REFUND));
		
		if(origInvokeProfile.getGssoOTPRequest()!=null){
			this.otpChannel = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getOtpChannel();
			this.messageType = origInvokeProfile.getGssoOTPRequest().getMessageType();
			this.isEmailDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_EMAIL);
			this.waitDR = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getWaitDR();
			this.smscDeliveryReceipt = origInvokeProfile.getRealSMSCDeliveryReceipt();
			this.refundFlag = origInvokeProfile.getRefundFlag();
			this.isWaitDREnable = Boolean.parseBoolean(waitDR);
			this.isSMSCDeliveryReceipt = Boolean.parseBoolean(smscDeliveryReceipt);
			try {
				this.isRefundFlag = Boolean.parseBoolean(refundFlag);
			}
			catch (Exception e) {
				this.isRefundFlag = false;
			}
			this.sessionId = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getSessionId();
			this.refId = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getRefId();
			this.msisdn = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getMsisdn();
		}
		else if((origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP))||
				(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID))){
			String otpMobile = origInvokeProfile.getSendWSOTPRequest().getOtpMobile();
			String email = origInvokeProfile.getSendWSOTPRequest().getEmail();
			boolean isSms = false;
			boolean isEmail = false;
			
			if(otpMobile!=null&&(!otpMobile.isEmpty())){
					isSms = true;
				}
			if(email!=null&&(!email.isEmpty())){
					isEmail = true;
				}
			
			if(isSms == true && isEmail == false){
				this.otpChannel = "sms";
			}else if(isSms == false && isEmail == true){
				this.otpChannel = "email";
			}else if(isSms == true && isEmail == true){
				this.otpChannel = "all";
			}
			
			this.messageType = origInvokeProfile.getMessageType();
			this.isEmailDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_EMAIL);
			this.waitDR = origInvokeProfile.getSendWSOTPRequest().getWaitDR();
			this.smscDeliveryReceipt = origInvokeProfile.getRealSMSCDeliveryReceipt();

			this.isWaitDREnable = Boolean.parseBoolean(waitDR);

			this.isSMSCDeliveryReceipt = Boolean.parseBoolean(smscDeliveryReceipt);
		}
		else if((origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_CREAT_OTP))||
				(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_GENERATE_OTP))){
			this.otpChannel = "sms";
			this.messageType = origInvokeProfile.getMessageType();
			this.isEmailDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_EMAIL);
			this.waitDR = origInvokeProfile.getSendWSOTPRequest().getWaitDR();
			this.smscDeliveryReceipt = origInvokeProfile.getRealSMSCDeliveryReceipt();
			this.isWaitDREnable = Boolean.parseBoolean(waitDR);
			this.isSMSCDeliveryReceipt = Boolean.parseBoolean(smscDeliveryReceipt);
		}

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			/** INITIAL DEBUG LOG **/
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
		}

		/** INITIAL DETAILS LOG **/
		this.composeDetailsLog = new GssoComposeDetailsLog(appInstance, abstractAF.getEquinoxProperties().getState() + "." + "BEGIN",
				abstractAF);
		/** SET DTAILS IDENTITY **/
		this.composeDetailsLog.setIdentity(origInvokeProfile.getDetailsService());

		this.composeDetailsLog.setDataOrig(origInvoke, this.rawDataInput, appInstance);

		this.mapDetails = new MapDetailsAndConfigType();

		/** INITIATE SUMMARY LOG **/
		this.composeSummary = new GssoComposeSummaryLog(abstractAF, origInvokeProfile.getDetailsService());

		if (origInvokeProfile.isSMPPRoaming()) {
			event = EventLog.SMPPGW_ROAMING.getEventLog();
			destNodeName = "SMPPGWROAMING";
		}
		else {
			event = EventLog.SUBMIT_SM.getEventLog();
			destNodeName = "SMPPGW";
		}

		appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, event);

		appInstance.getMapOrigInvokeDetailScenario()
				.put(origInvoke, appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

		this.startTimeOfInvokeIncoming = origInvokeProfile.getStartTimeOfInvoke();

		EquinoxRawData origRawData = appInstance.getMapOrigProfile().get(this.origInvoke).getOrigEquinoxRawData();

		//*******************************************
		//Are we have to change nodeCommand or not?
		
		if (origRawData.getCType().equals(EventCtype.PLAIN)) {
			/** IDLE_SEND_OTP_REQ **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())) {

				nodeCommand = EventLog.SEND_OTP.getEventLog();
			}

			/** IDLE_AUTH_OTP **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
				nodeCommand = EventLog.AUTHEN_OTP.getEventLog();

			}
		}
		else {
			/** IDLE_WS_AUTHEN_OTP_REQ **/
			if(origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_SOAP.getMessageType())){
				nodeCommand = EventLog.WS_AUTHEN_OTP.getEventLog();
			}
			/** IDLE_WS_AUTHEN_OTP_ID_REQ **/
			else if(origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_ID_SOAP.getMessageType())){
				nodeCommand = EventLog.WS_AUTHEN_OTP_ID.getEventLog();
			}
			/** IDLE_WS_CREATE_OTP_REQ **/
			else if(origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_CREATE_OTP_SOAP.getMessageType())){
				nodeCommand = EventLog.WS_CREATE_OTP.getEventLog();
			}
			/** IDLE_WS_GENERATE_OTP_REQ **/
			else if(origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_GENERATE_ONETIMEPW_SOAP.getMessageType())){
				nodeCommand = EventLog.WS_GENERATE_OTP.getEventLog();
			}
			/** IDLE_SEND_OTP_REQ **/
			else{
				nodeCommand = EventLog.SEND_OTP.getEventLog();
			}
				
		}

		try {

			this.composeDetailsLog.initialIncoming(rawDataInput, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawDataInput, nextState);

		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}
	}

	private void sendSmsSaveLog() {
		if (isWriteSummary == false) {

			if (!destNodeResultDescription.equals("null")) {
				DestinationBean destinationBean = new DestinationBean();
				destinationBean.setNodeName(destNodeName);
				destinationBean.setNodeCommand(destNodeCommand);
				destinationBean.setNodeResultCode(destNodeResultCode);
				destinationBean.setNodeResultDesc(destNodeResultDescription);

				if (appInstance.getMapDestinationBean().get(origInvoke) == null) {
					MapDestinationBean mapDestinationBean = new MapDestinationBean();
					ArrayList<DestinationBean> destinationBeanArrayList = new ArrayList<DestinationBean>();
					destinationBeanArrayList.add(destinationBean);
					mapDestinationBean.setDestinationBeanList(destinationBeanArrayList);
					appInstance.getMapDestinationBean().put(origInvoke, mapDestinationBean);
					
				}
				else {
					appInstance.getMapDestinationBean().get(origInvoke).getDestinationBeanList().add(destinationBean);
				}
			}
		}
		else {

			this.composeSummary.setWriteSummary();
			try {

				if (!destNodeResultDescription.equals("null")) {
					DestinationBean destinationBean = new DestinationBean();
					destinationBean.setNodeName(destNodeName);
					destinationBean.setNodeCommand(destNodeCommand);
					destinationBean.setNodeResultCode(destNodeResultCode);
					destinationBean.setNodeResultDesc(destNodeResultDescription);

					if (appInstance.getMapDestinationBean().get(origInvoke) == null) {
						MapDestinationBean mapDestinationBean = new MapDestinationBean();
						ArrayList<DestinationBean> destinationBeanArrayList = new ArrayList<DestinationBean>();
						destinationBeanArrayList.add(destinationBean);
						mapDestinationBean.setDestinationBeanList(destinationBeanArrayList);
						appInstance.getMapDestinationBean().put(origInvoke, mapDestinationBean);
					}
					else {
						appInstance.getMapDestinationBean().get(origInvoke).getDestinationBeanList().add(destinationBean);
					}
				}

				this.composeSummary.initialSummary(this.appInstance, startTimeOfInvokeIncoming, origInvoke, nodeCommand, this.code, this.description);
				this.composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), origInvoke);

			}
			catch (Exception e) {
				Log.e(e.getMessage());
			}
		}

		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^WRITE
		// SUMMARY^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		if (this.composeSummary.isWriteSummary()) {
			appInstance.getListSummaryLog().add(this.composeSummary.getSummaryLog());
		}
		// ===============================================SAVE
		// SUMMARY======================================================

		/** WRITE DETAILS **/
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
		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			/** WRITELOG DEBUG **/
			this.composeDebugLog.initialGssoSubStateLog(rawDataInput);
			this.composeDebugLog.writeDebugSubStateLog();
		}

	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data) {
		// TODO Auto-generated method stub
		return null;
	}
	private void removeWaitDr() {
		origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().setWaitDR("false");
		Iterator<Map.Entry<String, TimeoutCalculator>> iteratorTimeOutDR = this.appInstance.getMapTimeoutOfWaitDR().entrySet().iterator();

		while (iteratorTimeOutDR.hasNext()) {
			Map.Entry<String, TimeoutCalculator> entryTimeOutDR = iteratorTimeOutDR.next();
			String invokeTimeoutDR = InvokeFilter.getOriginInvoke(entryTimeOutDR.getKey());
			if (origInvoke.equals(invokeTimeoutDR)) {
				iteratorTimeOutDR.remove();
			}
		}
	}

}


