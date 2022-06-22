package com.ais.eqx.gsso.substates;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.GssoCommand;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.LogDestNodeResultDesc;
import com.ais.eqx.gsso.enums.LogScenario;
import com.ais.eqx.gsso.enums.SMPPResultCode;
import com.ais.eqx.gsso.enums.SoapResultCode;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.DeliveryReportLog;
import com.ais.eqx.gsso.instances.DeliveryReportRequest;
import com.ais.eqx.gsso.instances.DeliveryReportRes;
import com.ais.eqx.gsso.instances.DestinationBean;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GssoOTPRequest;
import com.ais.eqx.gsso.instances.MapDestinationBean;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.SendOneTimePWRequest;
import com.ais.eqx.gsso.instances.SendWSOTPRequest;
import com.ais.eqx.gsso.instances.SubmitSMJsonFormatRes;
import com.ais.eqx.gsso.instances.SubmitSMXMLFormatRes;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.GssoMessageType;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.interfaces.OTPChannel;
import com.ais.eqx.gsso.interfaces.RetNumber;
import com.ais.eqx.gsso.interfaces.SentOTPResult;
import com.ais.eqx.gsso.jaxb.InstanceContext;
import com.ais.eqx.gsso.jaxb.JAXBHandler;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;
import com.ais.eqx.gsso.utils.GssoConstructMessage;
import com.ais.eqx.gsso.utils.GssoDataManagement;
import com.ais.eqx.gsso.utils.GssoServiceManagement;
import com.ais.eqx.gsso.utils.InvokeFilter;
import com.ais.eqx.gsso.utils.TimeoutCalculator;
import com.ais.eqx.gsso.validator.SmsEmailDeliveryValidator;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class W_DELIVERY_REPORT implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;
	private DeliveryReportRequest		deliveryReportReq;
	private ArrayList<EquinoxRawData>	rawDatasOut;
	private ArrayList<EquinoxRawData>	rawDatasOutRefund = new ArrayList<EquinoxRawData>();
	private String						nextState;
	private String						origInvoke;
	private EquinoxRawData				rawDataInput;
	private String						isSuccessTrue				= "true";
	private String						isSuccessFalse				= "false";
	private EquinoxRawData				rawDataOrig;
	private String						isEmailDone;
	private String						otpChannel;
	private String						messageType;
	private OrigInvokeProfile			origInvokeProfile;

	private String						drId;
	private String						drErr;

	private GssoComposeDetailsLog		composeDetailsLog;
	private MapDetailsAndConfigType		mapDetails;
	private GssoComposeSummaryLog		composeSummary;
	private GssoComposeDebugLog			composeDebugLog;

	private String						code;
	private String						description;
	private String						nodeCommand					= "";
	private String						compareLog					= "";

	private boolean						isWaitDREnable;
	private String						waitDR;
	private boolean						isRefundFlag;
	private String						refundFlag;

	private String						destNodeResultCode			= "null";
	private String						destNodeName				= "";
	private String						destNodeCommand				= "DeliveryReport";
	private String						destNodeResultDescription	= "null";
	private boolean						isWriteSummary				= false;
	private String						invoke						= "";
	long								startTimeOfInvoke;

	private String						event;

	private String						msisdn						= "";
	private String						serviceName					= "";
	private String						transactionID				= "";
	private String						orderRef					= "";
	private String						messageId					= "";
	private String						totalResponseTime			= "";
	private String						errorMessage				= "";
	private String						serviceKey					= "";

	private long						submitSmRequestTime			= 0;
	private long						submitSmRespTime			= 0;

	private boolean						isWriteDRSuccessLog			= false;
	private boolean						waitDRTimeout				= false;

	private String						sessionId;
	private String						refId;
	private boolean 					completely					= false;
	private ArrayList<String>			enableCommandsToRefund		= new ArrayList<String>();

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData) {

		/************** INITIAL *****************/
		deliveryInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);

		/************** CODING ******************/
		// System.out.println("Start W_DELIVERY_REPORT");

		if (RetNumber.NORMAL.equals(equinoxRawData.getRet())) {

			try {
				this.deliveryReportReq = (DeliveryReportRequest) JAXBHandler.createInstance(
						InstanceContext.getDeliveryReportRequestContext(), this.rawDataInput.getRawDataMessage(),
						DeliveryReportRequest.class);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			if (this.appInstance.getMapTimeoutOfWaitDR().isEmpty()) {
				// .size()
																		// <= 0

				// this.deliveryReportReq = (DeliveryReportRequest)
				// JAXBHandler.createInstance(
				// InstanceContext.getDeliveryReportRequestContext(),
				// this.rawDataInput.getRawDataMessage(),
				// DeliveryReportRequest.class);

				// // GET MESSAGE ID
				// splitMessageid(GssoDataManagement.convertHexToString(this.deliveryReportReq.getShortMessage()));
				//
				// ArrayList<String> listSMPPRoamingService =
				// ConfigureTool.getConfigureArray(ConfigName.SMPPGW_ROAMING_INTERFACE);
				//
				// if
				// (GssoServiceManagement.containService(listSMPPRoamingService,
				// equinoxRawData.getOrig())) {
				// event =
				// EventLog.SMPPGW_ROAMING_DELIVERY_REPORT.getEventLog();
				// destNodeName = "SMPPGWROAMING";
				// }
				// else {
				// event = EventLog.SMPPGW_DELIVERY_REPORT.getEventLog();
				// destNodeName = "SMPPGW";
				// }
				//
				// appInstance.getMapOrigInvokeEventDetailInput().put(this.rawDataInput.getInvoke(),
				// event);
				//
				// appInstance.getMapOrigInvokeDetailScenario().put(this.rawDataInput.getInvoke(),
				// LogScenario.UNKNOWN.getLogScenario());
				//
				// /** SET DTAILS IDENTITY **/
				// this.composeDetailsLog.setIdentity("unknown");
				//
				// try {
				// this.composeDetailsLog.initialIncoming(rawDataInput,
				// appInstance);
				// this.composeDetailsLog.addScenario(appInstance, rawDataInput,
				// rawDataInput.getInvoke());
				// }
				// catch (Exception e) {
				// Log.e(e.getMessage());
				// }
				//
				// this.mapDetails.setNoFlow();
				//
				// this.code = "008";
				// this.description = "System Error";
				//
				// if (drId == null) {
				// drId = "";
				// }
				// this.rawDatasOut.add(new
				// DeliveryReportRes(rawDataInput).toRawDatas(drId));
				//
				// this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());
				//
				// if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				// this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_UNEXPECTED_SMPPGW_DELIVERYREPORT_REQUEST
				// .getStatistic());
				// this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());
				// this.composeDebugLog.setMessageValidator("-");
				// }
				//
				// appInstance.getMapOrigInvokeEventDetailOutput().put(this.rawDataInput.getInvoke(),
				// event);
				//
				// invoke = rawDataInput.getInvoke();
				//
				// nodeCommand = event;
				// destNodeCommand = event;
				//
				// this.destNodeResultCode = this.drErr;
				//
				// if (errorMessage == null || errorMessage.isEmpty()) {
				// String smppErrorMessage =
				// SMPPResultCode.getErrorMessageFrom(drErr);
				// this.destNodeResultDescription = smppErrorMessage;
				// }
				// else {
				// this.destNodeResultDescription = errorMessage;
				// }
				//
				// isWriteSummary = true;

				/** Collapse code **/
				setUnexpectAndInvalidOutputDetail(equinoxRawData, abstractAF);

				if (errorMessage == null || errorMessage.isEmpty()) {
					String smppErrorMessage = SMPPResultCode.getErrorMessageFrom(drErr);
					this.destNodeResultDescription = smppErrorMessage;
				}
				else {
					this.destNodeResultDescription = errorMessage;
				}

				try {
					this.composeDetailsLog.initialIncoming(rawDataInput, appInstance);
					this.composeDetailsLog.addScenario(appInstance, rawDataInput, rawDataInput.getInvoke());
				}
				catch (Exception e) {
					Log.e(e.getMessage());
				}

				destNodeCommand = event;
				this.destNodeResultCode = this.drErr;

				this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_UNEXPECTED_SMPPGW_DELIVERYREPORT_REQUEST.getStatistic());
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_UNEXPECTED_SMPPGW_DELIVERYREPORT_REQUEST.getStatistic());
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());
					this.composeDebugLog.setMessageValidator("-");
				}

			}
			else {
				try {

					// this.deliveryReportReq = (DeliveryReportRequest)
					// JAXBHandler.createInstance(
					// InstanceContext.getDeliveryReportRequestContext(),
					// this.rawDataInput.getRawDataMessage(),
					// DeliveryReportRequest.class);

					SmsEmailDeliveryValidator.deliveryReportValidator(this.deliveryReportReq, rawDataInput);

					// GET MESSAGE ID
					splitMessageid(GssoDataManagement.convertHexToString(this.deliveryReportReq.getShortMessage()));
					if (drId == null) {
						drId = "";
					}

					// FINE MESSAGE ID
					if (compareMessageId()) {
						isWriteDRSuccessLog = true;

						// SET REFUND
						this.origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvoke);
						this.rawDataOrig = this.origInvokeProfile.getOrigEquinoxRawData();

						if (origInvokeProfile.getGssoOTPRequest() != null) {
							this.refundFlag = origInvokeProfile.getRefundFlag();
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

						if (origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)) {

							SendWSOTPRequest sendWSAuthOTPWithIDRequest = origInvokeProfile.getSendWSOTPRequest();

							origInvokeProfile.getMapSentOTPResult().put(SentOTPResult.IS_DR, SentOTPResult.SUCCESS);

							startTimeOfInvoke = origInvokeProfile.getStartTimeOfInvoke();

							this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
							isEmailDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_EMAIL);

							/* DR log */
							msisdn = sendWSAuthOTPWithIDRequest.getMsisdn();

							boolean isSms = false;
							boolean isEmail = false;

							if (sendWSAuthOTPWithIDRequest.getOtpMobile() != null
									&& (!sendWSAuthOTPWithIDRequest.getOtpMobile().isEmpty())) {
								isSms = true;
								/* DR log for OtpMobile */
								msisdn = sendWSAuthOTPWithIDRequest.getOtpMobile();
							}
							if (sendWSAuthOTPWithIDRequest.getEmail() != null && (!sendWSAuthOTPWithIDRequest.getEmail().isEmpty())) {
								isEmail = true;
							}
							/** FOR SMS **/
							if (isSms == true && isEmail == false) {
								otpChannel = "sms";

							}
							/** FOR EMAIL **/
							else if (isEmail == true && isSms == false) {
								otpChannel = "email";

							}
							/** FOR ALL **/
							else if (isSms == true && isEmail == true) {
								otpChannel = "all";

							}

							messageType = origInvokeProfile.getMessageType();
							this.waitDR = sendWSAuthOTPWithIDRequest.getWaitDR();

							this.isWaitDREnable = Boolean.parseBoolean(waitDR);

							invoke = rawDataOrig.getInvoke();

							if (origInvokeProfile.isSMPPRoaming()) {
								event = EventLog.SMPPGW_ROAMING_DELIVERY_REPORT.getEventLog();
								destNodeName = "SMPPGWROAMING";
							}
							else {
								event = EventLog.SMPPGW_DELIVERY_REPORT.getEventLog();
								destNodeName = "SMPPGW";
							}

							appInstance.getMapOrigInvokeEventDetailInput().put(this.rawDataInput.getInvoke(), event);

							appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
									appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

							/* Dr log */
							serviceName = sendWSAuthOTPWithIDRequest.getService();
							transactionID = origInvokeProfile.getTransactionID();
							orderRef = origInvokeProfile.getOrderRefLog();
						}
						else if (origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP)) {

							SendWSOTPRequest sendWSAuthOTPRequest = origInvokeProfile.getSendWSOTPRequest();

							origInvokeProfile.getMapSentOTPResult().put(SentOTPResult.IS_DR, SentOTPResult.SUCCESS);

							startTimeOfInvoke = origInvokeProfile.getStartTimeOfInvoke();

							this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
							isEmailDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_EMAIL);

							/* DR log */
							msisdn = sendWSAuthOTPRequest.getMsisdn();

							boolean isSms = false;
							boolean isEmail = false;

							if (sendWSAuthOTPRequest.getOtpMobile() != null && (!sendWSAuthOTPRequest.getOtpMobile().isEmpty())) {
								isSms = true;
								/* DR log for OtpMobile */
								msisdn = sendWSAuthOTPRequest.getOtpMobile();
							}
							if (sendWSAuthOTPRequest.getEmail() != null && (!sendWSAuthOTPRequest.getEmail().isEmpty())) {
								isEmail = true;
							}
							/** FOR SMS **/
							if (isSms == true && isEmail == false) {
								otpChannel = "sms";

							}
							/** FOR EMAIL **/
							else if (isEmail == true && isSms == false) {
								otpChannel = "email";

							}
							/** FOR ALL **/
							else if (isSms == true && isEmail == true) {
								otpChannel = "all";

							}

							messageType = origInvokeProfile.getMessageType();
							this.waitDR = sendWSAuthOTPRequest.getWaitDR();

							this.isWaitDREnable = Boolean.parseBoolean(waitDR);

							invoke = rawDataOrig.getInvoke();

							if (origInvokeProfile.isSMPPRoaming()) {
								event = EventLog.SMPPGW_ROAMING_DELIVERY_REPORT.getEventLog();
								destNodeName = "SMPPGWROAMING";
							}
							else {
								event = EventLog.SMPPGW_DELIVERY_REPORT.getEventLog();
								destNodeName = "SMPPGW";
							}

							appInstance.getMapOrigInvokeEventDetailInput().put(this.rawDataInput.getInvoke(), event);

							appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
									appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

							/* Dr log */
							serviceName = sendWSAuthOTPRequest.getService();
							transactionID = origInvokeProfile.getTransactionID();
							orderRef = origInvokeProfile.getOrderRefLog();

						}
						else if (origInvokeProfile.getGssoOTPRequest() != null) {

							GssoOTPRequest gssoOTPRequest = origInvokeProfile.getGssoOTPRequest();
							SendOneTimePWRequest sendOneTimePW = gssoOTPRequest.getSendOneTimePW();

							origInvokeProfile.getMapSentOTPResult().put(SentOTPResult.IS_DR, SentOTPResult.SUCCESS);

							startTimeOfInvoke = origInvokeProfile.getStartTimeOfInvoke();

							this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
							isEmailDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_EMAIL);
							otpChannel = sendOneTimePW.getOtpChannel();
							messageType = gssoOTPRequest.getMessageType();
							this.waitDR = sendOneTimePW.getWaitDR();

							this.isWaitDREnable = Boolean.parseBoolean(waitDR);

							invoke = rawDataOrig.getInvoke();

							if (origInvokeProfile.isSMPPRoaming()) {
								event = EventLog.SMPPGW_ROAMING_DELIVERY_REPORT.getEventLog();
								destNodeName = "SMPPGWROAMING";
							}
							else {
								event = EventLog.SMPPGW_DELIVERY_REPORT.getEventLog();
								destNodeName = "SMPPGW";
							}

							appInstance.getMapOrigInvokeEventDetailInput().put(this.rawDataInput.getInvoke(), event);

							appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
									appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

							msisdn = sendOneTimePW.getMsisdn();
							serviceName = sendOneTimePW.getService();
							transactionID = origInvokeProfile.getTransactionID();
							orderRef = origInvokeProfile.getOrderRefLog();
						}
						else if (origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_CREAT_OTP)) {

							SendWSOTPRequest sendWSCreateOTPRequest = origInvokeProfile.getSendWSOTPRequest();

							origInvokeProfile.getMapSentOTPResult().put(SentOTPResult.IS_DR, SentOTPResult.SUCCESS);

							startTimeOfInvoke = origInvokeProfile.getStartTimeOfInvoke();

							this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
							isEmailDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_EMAIL);

							otpChannel = "sms";
							messageType = origInvokeProfile.getMessageType();
							this.waitDR = sendWSCreateOTPRequest.getWaitDR();

							this.isWaitDREnable = Boolean.parseBoolean(waitDR);

							invoke = rawDataOrig.getInvoke();

							if (origInvokeProfile.isSMPPRoaming()) {
								event = EventLog.SMPPGW_ROAMING_DELIVERY_REPORT.getEventLog();
								destNodeName = "SMPPGWROAMING";
							}
							else {
								event = EventLog.SMPPGW_DELIVERY_REPORT.getEventLog();
								destNodeName = "SMPPGW";
							}

							appInstance.getMapOrigInvokeEventDetailInput().put(this.rawDataInput.getInvoke(), event);

							appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
									appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

							msisdn = sendWSCreateOTPRequest.getMsisdn();
							serviceName = sendWSCreateOTPRequest.getService();
							transactionID = origInvokeProfile.getTransactionID();
							orderRef = origInvokeProfile.getOrderRefLog();
						}
						else if (origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_GENERATE_OTP)) {

							SendWSOTPRequest sendWSGenerateOTPRequest = origInvokeProfile.getSendWSOTPRequest();

							origInvokeProfile.getMapSentOTPResult().put(SentOTPResult.IS_DR, SentOTPResult.SUCCESS);

							startTimeOfInvoke = origInvokeProfile.getStartTimeOfInvoke();

							this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
							isEmailDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_EMAIL);

							otpChannel = "sms";
							messageType = origInvokeProfile.getMessageType();
							this.waitDR = sendWSGenerateOTPRequest.getWaitDR();

							this.isWaitDREnable = Boolean.parseBoolean(waitDR);

							invoke = rawDataOrig.getInvoke();

							if (origInvokeProfile.isSMPPRoaming()) {
								event = EventLog.SMPPGW_ROAMING_DELIVERY_REPORT.getEventLog();
								destNodeName = "SMPPGWROAMING";
							}
							else {
								event = EventLog.SMPPGW_DELIVERY_REPORT.getEventLog();
								destNodeName = "SMPPGW";
							}

							appInstance.getMapOrigInvokeEventDetailInput().put(this.rawDataInput.getInvoke(), event);

							appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
									appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

							msisdn = sendWSGenerateOTPRequest.getMsisdn();
							serviceName = sendWSGenerateOTPRequest.getService();
							transactionID = origInvokeProfile.getTransactionID();
							orderRef = origInvokeProfile.getOrderRefLog();
						}

						this.composeDetailsLog.setDataOrig(rawDataInput.getInvoke(), rawDataInput, appInstance);

						/** SET DTAILS IDENTITY **/
						this.composeDetailsLog.setIdentity(appInstance.getMapOrigProfile().get(origInvoke).getDetailsService());
						this.composeSummary = new GssoComposeSummaryLog(abstractAF,
								appInstance.getMapOrigProfile().get(origInvoke).getDetailsService());

						try {
							this.composeDetailsLog.initialIncoming(rawDataInput, appInstance);
							this.composeDetailsLog.addScenario(appInstance, rawDataOrig, this.nextState);
						}
						catch (Exception e) {
							Log.e(e.getMessage());
						}

						/* DR LOG */
						totalResponseTime = ""
								+ (this.composeDetailsLog.getDetailTimeIncoming() - origInvokeProfile.getSubmitSmRequestTime());
						submitSmRequestTime = origInvokeProfile.getSubmitSmRequestTime();
						submitSmRespTime = origInvokeProfile.getSubmitSmRespTime();
						serviceKey = origInvokeProfile.getServiceKey();

						// CHECK ERR CODE
						if (this.drErr.equals("000") && this.errorMessage.equals("DELIVRD")) {
							// errorMessage = "Success";
							this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_SMPPGW_DELIVERYREPORT_REQUEST.getStatistic());

							normalCaseSuccess();

							if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
								this.composeDebugLog
										.addStatisticIn(Statistic.GSSO_RECEIVED_SMPPGW_DELIVERYREPORT_REQUEST.getStatistic());
								this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
								this.composeDebugLog.drIDMatch(this.drId);
							}

						}
						else {
							this.destNodeResultCode = this.drErr;

							if (errorMessage == null || errorMessage.isEmpty()) {
								String smppErrorMessage = SMPPResultCode.getErrorMessageFrom(drErr);
								this.destNodeResultDescription = smppErrorMessage;
							}
							else {
								this.destNodeResultDescription = errorMessage;
							}

							this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_SMPPGW_DELIVERYREPORT_REQUEST.getStatistic());

							this.mapDetails.setNoFlow();
							if(origInvokeProfile.getRawDatasOutStateDr().size() ==0 ) {
								if(completely) {
									this.origInvokeProfile.getRawDatasOutStateDr().add(new DeliveryReportRes(rawDataInput).toRawDatas(drId));
								}else {
									this.rawDatasOut.add(new DeliveryReportRes(rawDataInput).toRawDatas(drId));
								}
								errorCase();

							}else {
								if(completely){
									this.origInvokeProfile.getRawDatasOutStateDr().add(new DeliveryReportRes(rawDataInput).toRawDatas(drId));

								}else {
									this.rawDatasOut.add(new DeliveryReportRes(rawDataInput).toRawDatas(drId));
									isWriteSummary = true;
								}

							}

							appInstance.getMapOrigInvokeEventDetailOutput().put(this.rawDataInput.getInvoke(), event);

							this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());


							if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
								this.composeDebugLog
										.addStatisticIn(Statistic.GSSO_RECEIVED_SMPPGW_DELIVERYREPORT_REQUEST.getStatistic());
								this.composeDebugLog
										.addStatisticOut(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());
								this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
								this.composeDebugLog.drIDMatchAndError(drId, drErr);
							}

						}
					}
					else {
//						try {
//							// SET REFUND
//
//							ArrayList<String> listOfInvoke = this.appInstance.getListInvokeProcessing();
//							for (String invoke : listOfInvoke) {
//								this.origInvoke = InvokeFilter.getOriginInvoke(invoke);
//								this.origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvoke);
//
//								if (origInvokeProfile.getGssoOTPRequest() != null) {
//									this.refundFlag = origInvokeProfile.getRefundFlag();
//									try {
//										this.isRefundFlag = Boolean.parseBoolean(refundFlag);
//									}
//									catch (Exception e) {
//										this.isRefundFlag = false;
//									}
//									this.sessionId = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getSessionId();
//									this.refId = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getRefId();
//									this.msisdn = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getMsisdn();
//								}
//							}
//						}
//						catch (Exception e) {
//							// TODO: handle exception
//						}

						this.destNodeResultDescription = LogDestNodeResultDesc.RESULT_CODE_ERROR.getLogDestNodeResultDesc();

						this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_SMPPGW_ERROR_DELIVERYREPORT_REQUEST.getStatistic());

						ArrayList<String> listSMPPRoamingService = ConfigureTool
								.getConfigureArray(ConfigName.SMPPGW_ROAMING_INTERFACE);

						if (GssoServiceManagement.containService(listSMPPRoamingService, equinoxRawData.getOrig())) {
							event = EventLog.SMPPGW_ROAMING_DELIVERY_REPORT.getEventLog();
							destNodeName = "SMPPGWROAMING";
						}
						else {
							event = EventLog.SMPPGW_DELIVERY_REPORT.getEventLog();
							destNodeName = "SMPPGW";
						}

						appInstance.getMapOrigInvokeEventDetailInput().put(this.rawDataInput.getInvoke(), event);
						appInstance.getMapOrigInvokeDetailScenario().put(this.rawDataInput.getInvoke(),
								LogScenario.UNKNOWN.getLogScenario());

						/** SET DTAILS IDENTITY **/
						this.composeDetailsLog.setIdentity("unknown");
						try {
							this.composeDetailsLog.initialIncoming(rawDataInput, appInstance);
							this.composeDetailsLog.addScenario(appInstance, rawDataInput, nextState);
						}
						catch (Exception e) {
							Log.e(e.getMessage());
						}

						this.mapDetails.setNoFlow();

						this.rawDatasOut.add(new DeliveryReportRes(rawDataInput).toRawDatas(drId));

						this.code = "008";
						this.description = "System Error";

						appInstance.getMapOrigInvokeEventDetailOutput().put(this.rawDataInput.getInvoke(), event);

						this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());

						invoke = rawDataInput.getInvoke();
						isWriteSummary = true;
						nodeCommand = event;

						if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
							this.composeDebugLog
									.addStatisticIn(Statistic.GSSO_RECEIVED_SMPPGW_ERROR_DELIVERYREPORT_REQUEST.getStatistic());
							this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
							this.composeDebugLog
									.addStatisticOut(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());
							this.composeDebugLog.drIDMismatch(drId);
						}

						/* Refund */
//						if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
//							this.rawDatasOut.add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance,
//									this.sessionId, this.refId, this.msisdn, composeDebugLog));
//							this.nextState = SubStates.W_REFUND.toString();
//						}
					}

				}
				catch (ValidationException validate) {

					// this.deliveryReportReq = (DeliveryReportRequest)
					// JAXBHandler.createInstance(
					// InstanceContext.getDeliveryReportRequestContext(),
					// this.rawDataInput.getRawDataMessage(),
					// DeliveryReportRequest.class);

					// GET MESSAGE ID
					// splitMessageid(GssoDataManagement.convertHexToString(this.deliveryReportReq.getShortMessage()));
					// if (drId == null) {
					// drId = "";
					// }
					//
					// isWriteSummary = true;
					// this.destNodeResultDescription =
					// validate.getMandatoryPath() + " " +
					// validate.getMessage();
					// invoke = rawDataInput.getInvoke();
					// nodeCommand = event;
					//
					// if
					// (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED))
					// {
					// this.composeDebugLog.setFailureAvp(validate.getMandatoryPath()
					// + " " + validate.getMessage());
					//
					// if
					// (validate.getMandatoryPath().equalsIgnoreCase("deliveryReportReq")
					// ||
					// validate.getMandatoryPath().equalsIgnoreCase("short_message"))
					// {
					// this.composeDebugLog.drIDMissing();
					// }
					// else {
					// if
					// (validate.getMandatoryPath().equalsIgnoreCase(EquinoxAttribute.CTYPE))
					// {
					// this.composeDebugLog.drIDInvalid();
					// }
					// else {
					// this.composeDebugLog.drIDMismatch(validate.getResultCode());
					// }
					// }
					// }
					//
					// this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_SMPPGW_DELIVERYREPORT_REQUEST.getStatistic());
					//
					// ArrayList<String> listSMPPRoamingService =
					// ConfigureTool.getConfigureArray(ConfigName.SMPPGW_ROAMING_INTERFACE);
					//
					// if
					// (GssoServiceManagement.containService(listSMPPRoamingService,
					// equinoxRawData.getOrig())) {
					// event =
					// EventLog.SMPPGW_ROAMING_DELIVERY_REPORT.getEventLog();
					// destNodeName = "SMPPGWROAMING";
					// }
					// else {
					// event = EventLog.SMPPGW_DELIVERY_REPORT.getEventLog();
					// destNodeName = "SMPPGW";
					// }
					//
					// appInstance.getMapOrigInvokeEventDetailInput().put(this.rawDataInput.getInvoke(),
					// event);
					// appInstance.getMapOrigInvokeDetailScenario().put(this.rawDataInput.getInvoke(),
					// LogScenario.UNKNOWN.getLogScenario());
					//
					// /** SET DTAILS IDENTITY **/
					// this.composeDetailsLog.setIdentity("unknown");
					// try {
					// this.composeDetailsLog.initialIncoming(rawDataInput,
					// appInstance);
					// this.composeDetailsLog.addScenario(appInstance,
					// rawDataInput, nextState);
					// }
					// catch (Exception e) {
					// Log.e(e.getMessage());
					// }
					//
					// this.mapDetails.setNoFlow();
					//
					// this.code = "008";
					// this.description = "System Error";
					//
					// this.rawDatasOut.add(new
					// DeliveryReportRes(rawDataInput).toRawDatas(drId));
					//
					// this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());
					//
					// appInstance.getMapOrigInvokeEventDetailOutput().put(this.rawDataInput.getInvoke(),
					// event);
					//
					// origInvoke = rawDataInput.getInvoke();
					//
					// if
					// (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED))
					// {
					// this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_SMPPGW_DELIVERYREPORT_REQUEST.getStatistic());
					// this.composeDebugLog
					// .addStatisticOut(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());
					// this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
					// }

					/** Collapse code **/
					setUnexpectAndInvalidOutputDetail(equinoxRawData, abstractAF);
					this.destNodeResultDescription = validate.getMandatoryPath() + " " + validate.getMessage();

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.setFailureAvp(validate.getMandatoryPath() + " " + validate.getMessage());

						if (validate.getMandatoryPath().equalsIgnoreCase("deliveryReportReq")
								|| validate.getMandatoryPath().equalsIgnoreCase("short_message")) {
							this.composeDebugLog.drIDMissing();
						}
						else {
							if (validate.getMandatoryPath().equalsIgnoreCase(EquinoxAttribute.CTYPE)) {
								this.composeDebugLog.drIDInvalid();
							}
							else {
								this.composeDebugLog.drIDMismatch(validate.getResultCode());
							}
						}
					}

					try {
						this.composeDetailsLog.initialIncoming(rawDataInput, appInstance);

						OrigInvokeProfile origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvoke);
						if (origInvokeProfile != null) {
							this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
							/** WS **/
							if (origInvokeProfile.getSendWSOTPRequest() != null && rawDataOrig != null) {
								this.composeDetailsLog.addScenario(appInstance, rawDataOrig, rawDataInput.getInvoke());
							}
							else {
								this.composeDetailsLog.addScenario(appInstance, rawDataInput, rawDataInput.getInvoke());
							}
						}
						/** SSO **/
						else {
							this.composeDetailsLog.addScenario(appInstance, rawDataInput, rawDataInput.getInvoke());
						}
					}
					catch (Exception e) {
						Log.e(e.getMessage());
					}

					origInvoke = rawDataInput.getInvoke();

					this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_SMPPGW_DELIVERYREPORT_REQUEST.getStatistic());
					this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_SMPPGW_DELIVERYREPORT_REQUEST.getStatistic());
						this.composeDebugLog
								.addStatisticOut(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_ERROR.getStatistic());
						this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
					}
				}
			}
		}
		else if (RetNumber.TIMEOUT.equals(equinoxRawData.getRet())) {
			this.origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvoke);

			origInvokeProfile.setDrIncoming(origInvokeProfile.getDrIncoming()+1);
			completely = origInvokeProfile.getDrIncoming()==origInvokeProfile.getSmsOutgoing();
			Log.d("###########  Count Dr IncommingMsg :"+origInvokeProfile.getDrIncoming()+"/"+origInvokeProfile.getSmsOutgoing()+" ########### ");

			this.messageId = origInvokeProfile.getSmMessageId();

			this.destNodeResultDescription = LogDestNodeResultDesc.CONNECTION_TIMEOUT.getLogDestNodeResultDesc();

			this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_SMPPGW_DELIVERYREPORT_REQUEST_TIMEOUT.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_SMPPGW_DELIVERYREPORT_REQUEST_TIMEOUT.getStatistic());
			}

			// InvokeFilter invokeFilter = new
			// InvokeFilter(rawDataInput.getInvoke());
			this.origInvoke = InvokeFilter.getOriginInvoke(rawDataInput.getInvoke());
			this.origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvoke);

			if (origInvokeProfile.getGssoOTPRequest() != null) {
				this.refundFlag = origInvokeProfile.getRefundFlag();
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

			origInvokeProfile.getMapSentOTPResult().put(SentOTPResult.IS_DR, SentOTPResult.TIMEOUT);

			origInvokeProfile.getMapSentOTPResult().put(SentOTPResult.IS_SMS, SentOTPResult.TIMEOUT);

			this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
			isEmailDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_EMAIL);

			GssoOTPRequest gssoOTPRequest = null;
			SendOneTimePWRequest sendOneTimePW = null;
			SendWSOTPRequest sendWSOTPRequest = null;
			if (origInvokeProfile.getSendWSOTPRequest() != null) {
				sendWSOTPRequest = origInvokeProfile.getSendWSOTPRequest();

				/* DR Report */
				msisdn = sendWSOTPRequest.getMsisdn();

				if (origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP)
						|| origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)) {
					boolean isSms = false;
					boolean isEmail = false;
					if ((sendWSOTPRequest.getOtpMobile() != null) && (!sendWSOTPRequest.getOtpMobile().isEmpty())) {
						isSms = true;
						/* DR Report for OtpMobile */
						msisdn = sendWSOTPRequest.getOtpMobile();
					}
					if ((sendWSOTPRequest.getEmail() != null) && (!sendWSOTPRequest.getEmail().isEmpty())) {
						isEmail = true;
					}
					if (isSms == true && isEmail == false) {
						otpChannel = OTPChannel.SMS;
					}
					else if (isSms == false && isEmail == true) {
						otpChannel = OTPChannel.EMAIL;
					}
					else if (isSms == true && isEmail == true) {
						otpChannel = OTPChannel.ALL;
					}
				}
				else {
					otpChannel = OTPChannel.SMS;
				}
				messageType = origInvokeProfile.getMessageType();
				this.waitDR = sendWSOTPRequest.getWaitDR();
				/* DR Report */
				serviceName = sendWSOTPRequest.getService();
			}
			else {
				gssoOTPRequest = origInvokeProfile.getGssoOTPRequest();
				sendOneTimePW = gssoOTPRequest.getSendOneTimePW();
				otpChannel = sendOneTimePW.getOtpChannel();
				messageType = gssoOTPRequest.getMessageType();
				this.waitDR = sendOneTimePW.getWaitDR();
				/* DR Report */
				msisdn = sendOneTimePW.getMsisdn();
				serviceName = sendOneTimePW.getService();
			}

			startTimeOfInvoke = origInvokeProfile.getStartTimeOfInvoke();

			this.isWaitDREnable = Boolean.parseBoolean(waitDR);

			invoke = rawDataOrig.getInvoke();

			this.composeDetailsLog.setDataOrig(origInvoke, rawDataInput, appInstance);

			if (origInvokeProfile.isSMPPRoaming()) {
				event = EventLog.SMPPGW_ROAMING_DELIVERY_REPORT.getEventLog();
				destNodeName = "SMPPGWROAMING";
			}
			else {
				event = EventLog.SMPPGW_DELIVERY_REPORT.getEventLog();
				destNodeName = "SMPPGW";
			}

			appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, event);

			appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
					appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

			/** SET DTAILS IDENTITY **/
			this.composeDetailsLog.setIdentity(appInstance.getMapOrigProfile().get(origInvoke).getDetailsService());
			this.composeSummary = new GssoComposeSummaryLog(abstractAF,
					appInstance.getMapOrigProfile().get(origInvoke).getDetailsService());
			try {
				this.composeDetailsLog.initialIncoming(rawDataInput, appInstance);
				this.composeDetailsLog.addScenario(appInstance, rawDataOrig, rawDataOrig.getInvoke());
			}
			catch (Exception e) {
				Log.e(e.getMessage());
			}

			/* DR Report */
			this.waitDRTimeout = true;
			transactionID = origInvokeProfile.getTransactionID();
			orderRef = origInvokeProfile.getOrderRefLog();

			/* DR LOG */
			totalResponseTime = "" + (this.composeDetailsLog.getDetailTimeIncoming() - origInvokeProfile.getSubmitSmRequestTime());
			submitSmRequestTime = origInvokeProfile.getSubmitSmRequestTime();
			submitSmRespTime = origInvokeProfile.getSubmitSmRespTime();
			serviceKey = origInvokeProfile.getServiceKey();

//			Iterator<Entry<String, TimeoutCalculator>> iteratorTimeOutDR = this.appInstance.getMapTimeoutOfWaitDR().entrySet()
//					.iterator();
//
//			while (iteratorTimeOutDR.hasNext()) {
//				Entry<String, TimeoutCalculator> entryTimeOutDR = (Entry<String, TimeoutCalculator>) iteratorTimeOutDR.next();
//
//				String invokeTimeoutDR = InvokeFilter.getOriginInvoke(entryTimeOutDR.getKey());
//				if (origInvoke.equals(invokeTimeoutDR)) {
//					iteratorTimeOutDR.remove();
//					break;
//				}
//
//			}
			if(origInvokeProfile.getRawDatasOutStateDr().size()==0){
				errorCaseTimeout();
			}else {
				isWriteSummary = true;
			}
		}

		/* SAVE LOG */
		deliverySaveLog();

		if(completely && null != origInvokeProfile && origInvokeProfile.getRawDatasOutStateDr().size()>0){
			return origInvokeProfile.getRawDatasOutStateDr();
		}else {
			return this.rawDatasOut;
		}
	}

	public static String getCurrentTimeStamp() {

		// SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd:HHmmss,SSS");// dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
		// return "";
	}

	private void splitMessageid(String shortMessage) {

		String id[] = shortMessage.trim().split("id:");
		if (id.length >= 2) {
			String val[] = id[1].split(" ");
			this.drId = val[0];
		}
		else {
			this.drId = null;
		}

		String err[] = shortMessage.split("err:");
		if (err.length >= 2) {
			String valErr[] = err[1].split(" ");
			this.drErr = valErr[0];
		}
		else {
			this.drErr = null;
		}

		try {
			String errorMessagge[] = shortMessage.split("stat:");
			if (errorMessagge.length >= 2) {
				String valErr[] = errorMessagge[1].split(" ");
				this.errorMessage = valErr[0];
			}
			else {
				this.errorMessage = "";
			}
		}
		catch (Exception e) {
			this.errorMessage = "";
		}
	}

	private boolean compareMessageId() {
		boolean foundMessageId = false;
		Log.d("DeliveryReport Id :"+drId);
		for (Entry<String, OrigInvokeProfile> entry : this.appInstance.getMapOrigProfile().entrySet()) {
			if (entry.getValue().getMsgIdList().contains(drId)) {
				entry.getValue().getMsgIdList().remove(drId);
				this.messageId = drId;
				this.origInvoke = entry.getKey();
				entry.getValue().setDrIncoming(entry.getValue().getDrIncoming() + 1);
				completely = entry.getValue().getDrIncoming() == entry.getValue().getSmsOutgoing();
				Log.d("###########  Count DR IncommingMsg :" + entry.getValue().getDrIncoming() + "/" + entry.getValue().getSmsOutgoing() + " ########### ");

				Iterator<Entry<String, TimeoutCalculator>> iteratorTimeOutDR = this.appInstance.getMapTimeoutOfWaitDR().entrySet()
						.iterator();

				while (iteratorTimeOutDR.hasNext()) {
					Entry<String, TimeoutCalculator> entryTimeOutDR = (Entry<String, TimeoutCalculator>) iteratorTimeOutDR.next();
					// InvokeFilter invokeFilter = new
					// InvokeFilter(entryTimeOutDR.getKey());

					String invokeTimeoutDR = InvokeFilter.getOriginInvoke(entryTimeOutDR.getKey());
					if (origInvoke.equals(invokeTimeoutDR)) {
						iteratorTimeOutDR.remove();
						break;
					}
				}

				return   true;

			}
		}
		return false;
	}

	private void normalCaseSuccess() {

		// DeliveryReport response success
		if (drId == null) {
			drId = "";
		}
		if(origInvokeProfile.getRawDatasOutStateDr().size()==0 ){
			this.rawDatasOut.add(new DeliveryReportRes(rawDataInput).toRawDatas(drId));
		}else {
			if(completely){
				this.origInvokeProfile.getRawDatasOutStateDr().add(new DeliveryReportRes(rawDataInput).toRawDatas(drId));
			}else {
				this.rawDatasOut.add(new DeliveryReportRes(rawDataInput).toRawDatas(drId));
			}
		}

		this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_SUCCESS.getStatistic());

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SMPPGW_DELIVERYREPORT_RESPONSE_SUCCESS.getStatistic());
		}

		appInstance.getMapOrigInvokeEventDetailOutput().put(this.rawDataInput.getInvoke(), event);

		if(origInvokeProfile.getRawDatasOutStateDr().size()==0) {
			if (this.isWaitDREnable) {

				if (OTPChannel.SMS.equalsIgnoreCase(otpChannel)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						if (completely)
							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SUCCESS.getCode(),
									JsonResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

						this.code = JsonResultCode.SUCCESS.getCode();
						this.description = JsonResultCode.SUCCESS.getDescription();

					} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						if (completely)
							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SUCCESS.getCode(),
									SoapResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

						this.code = SoapResultCode.SUCCESS.getCode();
						this.description = SoapResultCode.SUCCESS.getDescription();
					}

					this.nextState = SubStates.END.toString();
					if (completely) raiseStatoutSUCCESS();

					/* REMOVE PROFILE */
					if (completely) GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;

				} else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {
					// DR Success SMS Success Email remain
					if (isEmailDone == null) {

						this.nextState = SubStates.W_SEND_EMAIL.toString();

						this.appInstance.getMapOrigProfile().get(origInvoke).getMapSentOTPResult().put(SentOTPResult.IS_DR,
								SentOTPResult.SUCCESS);
						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.code = JsonResultCode.SUCCESS.getCode();
							this.description = JsonResultCode.SUCCESS.getDescription();

						} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.code = SoapResultCode.SUCCESS.getCode();
							this.description = SoapResultCode.SUCCESS.getDescription();
						}
					}
					// DR Success SMS Success Email ERROR
					else if (SentOTPResult.ERROR.equalsIgnoreCase(isEmailDone)) {

						GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							if (completely) this.rawDatasOut
									.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode(),
											JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
											.toRawDatas(appInstance));

							this.code = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
							this.description = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

						} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							if (completely) this.rawDatasOut
									.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode(),
											SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
											.toRawDatas(appInstance));

							this.code = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
							this.description = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

						}

						if (completely) raiseStatoutSUCCESS();

						this.nextState = SubStates.END.toString();

						/* REMOVE PROFILE */
						if (completely) GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;

					}
					// DR Success SMS Success Email Timeout
					else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isEmailDone)) {

						GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							if (completely) this.rawDatasOut.add(
									new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getCode(),
											JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription(), isSuccessTrue)
											.toRawDatas(appInstance));

							this.code = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getCode();
							this.description = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription();

						} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							if (completely) this.rawDatasOut
									.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode(),
											SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
											.toRawDatas(appInstance));

							this.code = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
							this.description = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

						}

						if (completely) raiseStatoutSUCCESS();

						this.nextState = SubStates.END.toString();

						/* REMOVE PROFILE */
						if (completely) GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;

					}
					// DR Success SMS Success Email Success
					else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isEmailDone)) {

						GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							if (completely)
								this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SUCCESS.getCode(),
										JsonResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

							this.code = JsonResultCode.SUCCESS.getCode();
							this.description = JsonResultCode.SUCCESS.getDescription();
						} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							if (completely)
								this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SUCCESS.getCode(),
										SoapResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

							this.code = SoapResultCode.SUCCESS.getCode();
							this.description = SoapResultCode.SUCCESS.getDescription();

						}

						if (completely) raiseStatoutSUCCESS();

						this.nextState = SubStates.END.toString();

						/* REMOVE PROFILE */
						if (completely) GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;

					}
				}
			} else {

				if (OTPChannel.SMS.equalsIgnoreCase(otpChannel)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.code = JsonResultCode.SUCCESS.getCode();
						this.description = JsonResultCode.SUCCESS.getDescription();

					} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.code = SoapResultCode.SUCCESS.getCode();
						this.description = SoapResultCode.SUCCESS.getDescription();
					}

				} else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {
					// DR Success SMS Success Email remain
					if (isEmailDone == null) {

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.code = JsonResultCode.SUCCESS.getCode();
							this.description = JsonResultCode.SUCCESS.getDescription();

						} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.code = SoapResultCode.SUCCESS.getCode();
							this.description = SoapResultCode.SUCCESS.getDescription();
						}
					}
					// DR Success SMS Success Email ERROR
					else if (SentOTPResult.ERROR.equalsIgnoreCase(isEmailDone)) {

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.code = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
							this.description = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

						} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.code = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
							this.description = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

						}

					}
					// DR Success SMS Success Email Timeout
					else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isEmailDone)) {

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.code = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getCode();
							this.description = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription();

						} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.code = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
							this.description = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

						}

					}
					// DR Success SMS Success Email Success
					else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isEmailDone)) {

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.code = JsonResultCode.SUCCESS.getCode();
							this.description = JsonResultCode.SUCCESS.getDescription();
						} else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.code = SoapResultCode.SUCCESS.getCode();
							this.description = SoapResultCode.SUCCESS.getDescription();

						}

					}
				}

				cmdName();
				/* REMOVE PROFILE */
				if (completely) GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
				isWriteSummary = true;
			}
		}
	}

	private void errorCaseTimeout() {

		if (this.isWaitDREnable) {

			if (OTPChannel.SMS.equalsIgnoreCase(otpChannel)) {
				// DR Timeout SMS SUCCESS

				if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

					origInvokeProfile.getRawDatasOutStateDr().add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT.getCode(),
							JsonResultCode.SEND_SMS_TIMEOUT.getDescription(), isSuccessFalse).toRawDatas(appInstance));

					this.code = JsonResultCode.SEND_SMS_TIMEOUT.getCode();
					this.description = JsonResultCode.SEND_SMS_TIMEOUT.getDescription();

				}
				else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

					origInvokeProfile.getRawDatasOutStateDr().add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL.getCode(),
							SoapResultCode.SEND_SMS_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

					this.code = SoapResultCode.SEND_SMS_FAIL.getCode();
					this.description = SoapResultCode.SEND_SMS_FAIL.getDescription();

				}

				raiseStatoutERROR();

				/* Refund */
				if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
					origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance, this.sessionId,
							this.refId, this.msisdn, composeDebugLog));
					this.nextState = SubStates.W_REFUND.toString();
					isWriteSummary = true;
				}
				else {
					this.nextState = SubStates.END.toString();

					/* REMOVE PROFILE */
					if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;
				}

			}
			else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {

				// DR Success SMS Success Email remain
				if (isEmailDone == null) {

					this.nextState = SubStates.W_SEND_EMAIL.toString();
					this.appInstance.getMapOrigProfile().get(origInvoke).getMapSentOTPResult().put(SentOTPResult.IS_DR,
							SentOTPResult.TIMEOUT);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.code = JsonResultCode.SEND_SMS_TIMEOUT.getCode();
						this.description = JsonResultCode.SEND_SMS_TIMEOUT.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.code = SoapResultCode.SEND_SMS_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_FAIL.getDescription();

					}

					/* Refund */
					if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
						origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance, this.sessionId,
								this.refId, this.msisdn, composeDebugLog));
						this.nextState = SubStates.W_REFUND.toString();
						isWriteSummary = true;
					}
				}

				// DR Success SMS Success Email ERROR
				else if (SentOTPResult.ERROR.equalsIgnoreCase(isEmailDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						origInvokeProfile.getRawDatasOutStateDr().add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getCode(),
										JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

						this.code = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getCode();
						this.description = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						origInvokeProfile.getRawDatasOutStateDr()
								.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode(),
										SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
												.toRawDatas(appInstance));

						this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

					}

					raiseStatoutERROR();

					/* Refund */
					if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
						origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance,
								this.sessionId, this.refId, this.msisdn, composeDebugLog));
						this.nextState = SubStates.W_REFUND.toString();
						isWriteSummary = true;
					}
					else {
						this.nextState = SubStates.END.toString();

						/* REMOVE PROFILE */
						if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;
					}

				}
				// DR Success SMS Success Email Timeout
				else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isEmailDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.origInvokeProfile.getRawDatasOutStateDr().add(
								new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getCode(),
										JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getDescription(), isSuccessFalse)
												.toRawDatas(appInstance));

						this.code = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getCode();
						this.description = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.origInvokeProfile.getRawDatasOutStateDr()
								.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode(),
										SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
												.toRawDatas(appInstance));

						this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

					}

					raiseStatoutERROR();

					/* Refund */
					if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
						this.origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance,
								this.sessionId, this.refId, this.msisdn, composeDebugLog));
						this.nextState = SubStates.W_REFUND.toString();
						isWriteSummary = true;
					}
					else {
						this.nextState = SubStates.END.toString();

						/* REMOVE PROFILE */
						if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;
					}

				}
				else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isEmailDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.origInvokeProfile.getRawDatasOutStateDr().add(
								new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getCode(),
										JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
												.toRawDatas(appInstance));

						this.code = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getCode();
						this.description = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.origInvokeProfile.getRawDatasOutStateDr()
								.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode(),
										SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
												.toRawDatas(appInstance));

						this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
						this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

					}

					raiseStatoutSUCCESS();

					/* Refund */
					if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
						this.origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance,
								this.sessionId, this.refId, this.msisdn, composeDebugLog));
						this.nextState = SubStates.W_REFUND.toString();
						isWriteSummary = true;
					}
					else {
						this.nextState = SubStates.END.toString();

						/* REMOVE PROFILE */
						if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;
					}

				}
			}
		}
		else {

			if (OTPChannel.SMS.equalsIgnoreCase(otpChannel)) {
				// DR Timeout SMS SUCCESS
				if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

					this.code = JsonResultCode.SUCCESS.getCode();
					this.description = JsonResultCode.SUCCESS.getDescription();

				}
				else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

					this.code = SoapResultCode.SUCCESS.getCode();
					this.description = SoapResultCode.SUCCESS.getDescription();

				}

			}
			else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {

				// DR Success SMS Success Email remain
				if (isEmailDone == null) {

				}

				// DR Success SMS Success Email ERROR
				else if (SentOTPResult.ERROR.equalsIgnoreCase(isEmailDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.code = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						this.description = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.code = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

					}

				}
				// DR Success SMS Success Email Timeout
				else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isEmailDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.code = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getCode();
						this.description = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.code = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

					}

				}
				else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isEmailDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.code = JsonResultCode.SUCCESS.getCode();
						this.description = JsonResultCode.SUCCESS.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.code = SoapResultCode.SUCCESS.getCode();
						this.description = SoapResultCode.SUCCESS.getDescription();
					}
				}
			}

			cmdName();
			/* REMOVE PROFILE */
			if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
			isWriteSummary = true;
		}

	}

	private void errorCase() {

		if (this.isWaitDREnable == true) {

			if (OTPChannel.SMS.equalsIgnoreCase(otpChannel)) {
				// DR Timeout SMS SUCCESS
				if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

					this.origInvokeProfile.getRawDatasOutStateDr().add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL.getCode(),
							JsonResultCode.SEND_SMS_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

					this.code = JsonResultCode.SEND_SMS_FAIL.getCode();
					this.description = JsonResultCode.SEND_SMS_FAIL.getDescription();

				}
				else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

					this.origInvokeProfile.getRawDatasOutStateDr().add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL.getCode(),
							SoapResultCode.SEND_SMS_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

					this.code = SoapResultCode.SEND_SMS_FAIL.getCode();
					this.description = SoapResultCode.SEND_SMS_FAIL.getDescription();
				}

				raiseStatoutERROR();

				/* Refund */
				if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
					this.origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance, this.sessionId,
							this.refId, this.msisdn, composeDebugLog));
					this.nextState = SubStates.W_REFUND.toString();
					isWriteSummary = true;
				}
				else {
					this.nextState = SubStates.END.toString();

					/* REMOVE PROFILE */
					if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;
				}
			}
			else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {

				// DR Success SMS Success Email remain
				if (isEmailDone == null) {

					this.nextState = SubStates.W_SEND_EMAIL.toString();

					this.appInstance.getMapOrigProfile().get(origInvoke).getMapSentOTPResult().put(SentOTPResult.IS_DR,
							SentOTPResult.ERROR);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.code = JsonResultCode.SEND_SMS_FAIL.getCode();
						this.description = JsonResultCode.SEND_SMS_FAIL.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.code = SoapResultCode.SEND_SMS_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_FAIL.getDescription();
					}

					/* Refund */
					if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
						this.origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance, this.sessionId,
								this.refId, this.msisdn, composeDebugLog));
						this.nextState = SubStates.W_REFUND.toString();
						isWriteSummary = true;
					}

				}

				// DR Success SMS Success Email ERROR
				else if (SentOTPResult.ERROR.equalsIgnoreCase(isEmailDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.origInvokeProfile.getRawDatasOutStateDr()
								.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode(),
										JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
												.toRawDatas(appInstance));

						this.code = JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						this.description = JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.origInvokeProfile.getRawDatasOutStateDr()
								.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode(),
										SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
												.toRawDatas(appInstance));

						this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

					}

					raiseStatoutERROR();

					/* Refund */
					if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
						this.origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance, this.sessionId,
								this.refId, this.msisdn, composeDebugLog));
						this.nextState = SubStates.W_REFUND.toString();
						isWriteSummary = true;
					}
					else {
						this.nextState = SubStates.END.toString();

						/* REMOVE PROFILE */
						if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;
					}
				}
				// DR Success SMS Success Email Timeout
				else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isEmailDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.origInvokeProfile.getRawDatasOutStateDr()
								.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getCode(),
										JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getDescription(), isSuccessFalse)
												.toRawDatas(appInstance));

						this.code = JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getCode();
						this.description = JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.origInvokeProfile.getRawDatasOutStateDr()
								.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode(),
										SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
												.toRawDatas(appInstance));

						this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

					}

					raiseStatoutERROR();

					/* Refund */
					if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
						this.origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance, this.sessionId,
								this.refId, this.msisdn, composeDebugLog));
						this.nextState = SubStates.W_REFUND.toString();
						isWriteSummary = true;
					}
					else {
						this.nextState = SubStates.END.toString();

						/* REMOVE PROFILE */
						if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;
					}
				}
				else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isEmailDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.origInvokeProfile.getRawDatasOutStateDr()
								.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode(),
										JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
												.toRawDatas(appInstance));

						this.code = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
						this.description = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.origInvokeProfile.getRawDatasOutStateDr()
								.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode(),
										SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
												.toRawDatas(appInstance));

						this.code = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
						this.description = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

					}
					raiseStatoutSUCCESS();

					/* Refund */
					if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
						this.origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance, this.sessionId,
								this.refId, this.msisdn, composeDebugLog));
						this.nextState = SubStates.W_REFUND.toString();
						isWriteSummary = true;
					}
					else {
						this.nextState = SubStates.END.toString();

						/* REMOVE PROFILE */
						if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;
					}
				}				
			}
		}
		else {

			if (OTPChannel.SMS.equalsIgnoreCase(otpChannel)) {
				// DR Timeout SMS SUCCESS
				if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

					this.code = JsonResultCode.SUCCESS.getCode();
					this.description = JsonResultCode.SUCCESS.getDescription();

				}
				else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

					this.code = SoapResultCode.SUCCESS.getCode();
					this.description = SoapResultCode.SUCCESS.getDescription();
				}

				/* Refund */
				if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
					this.origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance, this.sessionId,
							this.refId, this.msisdn, composeDebugLog));
					this.nextState = SubStates.W_REFUND.toString();
					isWriteSummary = true;
				}
				else {
					this.nextState = SubStates.END.toString();
					/* REMOVE PROFILE */
					if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;
				}
			}
			else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {

				// DR Success SMS Success Email remain
				if (isEmailDone == null) {

				}

				// DR Success SMS Success Email ERROR
				else if (SentOTPResult.ERROR.equalsIgnoreCase(isEmailDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.code = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						this.description = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.code = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

					}

				}
				// DR Success SMS Success Email Timeout
				else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isEmailDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.code = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getCode();
						this.description = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.code = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						this.description = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

					}

				}
				else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isEmailDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.code = JsonResultCode.SUCCESS.getCode();
						this.description = JsonResultCode.SUCCESS.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.code = SoapResultCode.SUCCESS.getCode();
						this.description = SoapResultCode.SUCCESS.getDescription();

					}

				}
			}
			cmdName();
			/* Refund *//*
			if (!(this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand()))) {
				*//* REMOVE PROFILE *//*
				if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
			}
			isWriteSummary = true;*/

			/* Refund */
			if (this.isRefundFlag && enableCommandsToRefund.contains(this.appInstance.getOrigCommand())) {
				this.origInvokeProfile.getRawDatasOutStateDr().add(GssoConstructMessage.createRefundReqTorPCEFMessage(this.rawDataOrig, ec02Instance, this.sessionId,
						this.refId, this.msisdn, composeDebugLog));
				this.nextState = SubStates.W_REFUND.toString();
				isWriteSummary = true;
			}
			else {
				this.nextState = SubStates.END.toString();
				/* REMOVE PROFILE */
				if(completely)GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
				isWriteSummary = true;
			}
		}
	}

	private void raiseStatoutSUCCESS() {

		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(this.origInvoke);
		EquinoxRawData origRawData = origInvokeProfile.getOrigEquinoxRawData();

		if (origRawData.getCType().equals(EventCtype.PLAIN)) {
			/** IDLE_SEND_OTP_REQ **/

			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())) {

				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.SEND_OTP.getEventLog());

				nodeCommand = EventLog.SEND_OTP.getEventLog();
			}

			/** IDLE_AUTH_OTP **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.AUTHEN_OTP.getEventLog());

				nodeCommand = EventLog.AUTHEN_OTP.getEventLog();

			}
		}
		else {

			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_ID_SOAP.getMessageType())) {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.WS_AUTHEN_OTP_ID.getEventLog());

				nodeCommand = EventLog.WS_AUTHEN_OTP_ID.getEventLog();
			}
			else if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_SOAP.getMessageType())) {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.WS_AUTHEN_OTP.getEventLog());

				nodeCommand = EventLog.WS_AUTHEN_OTP.getEventLog();
			}
			else if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_CREATE_OTP_SOAP.getMessageType())) {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.WS_CREATE_OTP.getEventLog());

				nodeCommand = EventLog.WS_CREATE_OTP.getEventLog();
			}
			else if (origInvokeProfile.getIncomingMessageType()
					.equals(IncomingMessageType.WS_GENERATE_ONETIMEPW_SOAP.getMessageType())) {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.WS_GENERATE_OTP.getEventLog());

				nodeCommand = EventLog.WS_GENERATE_OTP.getEventLog();
			}
			else {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.SEND_OTP.getEventLog());

				nodeCommand = EventLog.SEND_OTP.getEventLog();
			}

		}

	}

	private void cmdName() {

		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(this.origInvoke);
		EquinoxRawData origRawData = origInvokeProfile.getOrigEquinoxRawData();

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
			if (origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP)) {
				nodeCommand = EventLog.WS_AUTHEN_OTP.getEventLog();
			}
			else if (origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)) {
				nodeCommand = EventLog.WS_AUTHEN_OTP_ID.getEventLog();
			}
			else if (origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_CREAT_OTP)) {
				nodeCommand = EventLog.WS_CREATE_OTP.getEventLog();
			}
			else if (origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_GENERATE_OTP)) {
				nodeCommand = EventLog.WS_GENERATE_OTP.getEventLog();
			}
			else {
				nodeCommand = EventLog.SEND_OTP.getEventLog();
			}

		}
	}

	private void raiseStatoutERROR() {

		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(this.origInvoke);
		EquinoxRawData origRawData = origInvokeProfile.getOrigEquinoxRawData();

		if (origRawData.getCType().equals(EventCtype.PLAIN)) {

			/** IDLE_SEND_OTP_REQ **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())) {

				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.SEND_OTP.getEventLog());

				nodeCommand = EventLog.SEND_OTP.getEventLog();
			}

			/** IDLE_AUTH_OTP **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.AUTHEN_OTP.getEventLog());

				nodeCommand = EventLog.AUTHEN_OTP.getEventLog();
			}
		}
		else {
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_SOAP.getMessageType())) {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.WS_AUTHEN_OTP.getEventLog());

				nodeCommand = EventLog.WS_AUTHEN_OTP.getEventLog();
			}
			else if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_ID_SOAP.getMessageType())) {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.WS_AUTHEN_OTP_ID.getEventLog());

				nodeCommand = EventLog.WS_AUTHEN_OTP_ID.getEventLog();
			}
			else if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_CREATE_OTP_SOAP.getMessageType())) {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.WS_CREATE_OTP.getEventLog());

				nodeCommand = EventLog.WS_CREATE_OTP.getEventLog();
			}
			else if (origInvokeProfile.getIncomingMessageType()
					.equals(IncomingMessageType.WS_GENERATE_ONETIMEPW_SOAP.getMessageType())) {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.WS_GENERATE_OTP.getEventLog());

				nodeCommand = EventLog.WS_GENERATE_OTP.getEventLog();
			}
			else {
				this.ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(origInvoke, EventLog.SEND_OTP.getEventLog());

				nodeCommand = EventLog.SEND_OTP.getEventLog();
			}

		}
	}

	private void deliveryInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {
		this.startTimeOfInvoke = System.currentTimeMillis();
		this.rawDataInput = (EquinoxRawData) equinoxRawData;
		this.nextState = SubStates.W_DELIVERY_REPORT.name();
		this.rawDatasOut = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();
		this.deliveryReportReq = new DeliveryReportRequest();

		this.enableCommandsToRefund = GssoDataManagement
				.enableCommandsToRefund(ConfigureTool.getConfigure(ConfigName.COMMANDS_TO_REFUND));

		/** INITIAL DEBUG LOG **/
		this.origInvoke = InvokeFilter.getOriginInvoke(equinoxRawData.getInvoke());

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			/** INITIAL DEBUG LOG **/
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(this.nextState);
		}

		/** INITIAL DETAILS LOG **/
		this.composeDetailsLog = new GssoComposeDetailsLog(appInstance, abstractAF.getEquinoxProperties().getState() + "." + "BEGIN",
				abstractAF);

		this.composeDetailsLog.thisIdleState();

		this.mapDetails = new MapDetailsAndConfigType();

		this.composeSummary = new GssoComposeSummaryLog(abstractAF, "");

	}

	private void deliverySaveLog() {

		if (waitDRTimeout) {
			DeliveryReportLog deliveryReportLog = new DeliveryReportLog();

			deliveryReportLog.setDateTime(getCurrentTimeStamp());
			deliveryReportLog.setMsisdn(msisdn);
			deliveryReportLog.setServiceName(serviceName);
			deliveryReportLog.setServiceKey(serviceKey);
			deliveryReportLog.setTransactionID(transactionID);
			deliveryReportLog.setOrderRef(orderRef);

			deliveryReportLog.setMessageId(messageId);
			deliveryReportLog.setSmResultCode(SMPPResultCode.ESME_ROK.getCode());
			deliveryReportLog.setSmErrorMessage(SMPPResultCode.ESME_ROK.getErrorMessage());
			deliveryReportLog.setSmResponseTime((submitSmRespTime - submitSmRequestTime) + "");

			deliveryReportLog.setDrResultCode("");
			deliveryReportLog.setDrErrorMessage("DR Timeout");
			deliveryReportLog.setDrReport("");
			deliveryReportLog.setDrResponseTime((this.composeDetailsLog.getDetailTimeIncoming() - submitSmRespTime) + "");

			deliveryReportLog.setResponseTime(totalResponseTime);

			ec02Instance.writeLog(true, ConfigureTool.getConfigureLogName(ConfigName.DELIVERY_REPORT_LOG_NAME.getName()),
					deliveryReportLog.toString());
		}

		if (isWriteDRSuccessLog) {
			DeliveryReportLog deliveryReportLog = new DeliveryReportLog();
			deliveryReportLog.setDateTime(getCurrentTimeStamp());
			deliveryReportLog.setMsisdn(msisdn);
			deliveryReportLog.setServiceName(serviceName);
			deliveryReportLog.setServiceKey(serviceKey);
			deliveryReportLog.setTransactionID(transactionID);
			deliveryReportLog.setOrderRef(orderRef);

			deliveryReportLog.setMessageId(messageId);
			deliveryReportLog.setSmResultCode(SMPPResultCode.ESME_ROK.getCode());
			deliveryReportLog.setSmErrorMessage(SMPPResultCode.ESME_ROK.getErrorMessage());
			deliveryReportLog.setSmResponseTime((submitSmRespTime - submitSmRequestTime) + "");

			deliveryReportLog.setDrResultCode(drErr);
			deliveryReportLog.setDrErrorMessage(errorMessage);

			String message = GssoDataManagement.convertHexToString(this.deliveryReportReq.getShortMessage());
			/* Replace After Text: */
			String replaceMessageWithX = replaceDrMessage(message);

			// System.out.println(replaceMessageWithX);

			deliveryReportLog.setDrReport(replaceMessageWithX);
			deliveryReportLog.setDrResponseTime((this.composeDetailsLog.getDetailTimeIncoming() - submitSmRespTime) + "");

			deliveryReportLog.setResponseTime(totalResponseTime);

			ec02Instance.writeLog(true, ConfigureTool.getConfigureLogName(ConfigName.DELIVERY_REPORT_LOG_NAME.getName()),
					deliveryReportLog.toString());
		}

		if (!isWriteSummary) {

			if (!destNodeResultDescription.equals("null")) {

				DestinationBean destinationBean = new DestinationBean();
				destinationBean.setNodeName(destNodeName);
				destinationBean.setNodeCommand(destNodeCommand);
				destinationBean.setNodeResultCode(destNodeResultCode);
				destinationBean.setNodeResultDesc(destNodeResultDescription);

				if (appInstance.getMapDestinationBean().get(invoke) == null) {
					MapDestinationBean mapDestinationBean = new MapDestinationBean();
					ArrayList<DestinationBean> destinationBeanArrayList = new ArrayList<DestinationBean>();
					destinationBeanArrayList.add(destinationBean);
					mapDestinationBean.setDestinationBeanList(destinationBeanArrayList);
					appInstance.getMapDestinationBean().put(invoke, mapDestinationBean);
				}
				else {
					appInstance.getMapDestinationBean().get(invoke).getDestinationBeanList().add(destinationBean);
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

					if (appInstance.getMapDestinationBean().get(invoke) == null) {
						MapDestinationBean mapDestinationBean = new MapDestinationBean();
						ArrayList<DestinationBean> destinationBeanArrayList = new ArrayList<DestinationBean>();
						destinationBeanArrayList.add(destinationBean);
						mapDestinationBean.setDestinationBeanList(destinationBeanArrayList);
						appInstance.getMapDestinationBean().put(invoke, mapDestinationBean);

					}
					else {
						appInstance.getMapDestinationBean().get(invoke).getDestinationBeanList().add(destinationBean);
					}
				}

				this.composeSummary.initialSummary(this.appInstance, startTimeOfInvoke, invoke, nodeCommand, this.code,
						this.description);
				this.composeSummary.getSummaryLog(appInstance.getMapDestinationBean(), invoke);

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
			this.composeDebugLog.initialGssoSubStateLog(rawDataInput);
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

	private String replaceDrMessage(String message) {
		String prefix = "";
		String middle = "";
		String suffix = "";
		String primaryWord = " text:";
		boolean foundText = false;

		int lastIndex = 0;

		while (lastIndex != -1) {

			int currentTextIndex = message.toLowerCase().indexOf(primaryWord, lastIndex);
			try {
				prefix = message.substring(0, currentTextIndex);
				foundText = true;
			}
			catch (Exception e) {
			}

			if (foundText) {
				try {
					prefix = message.substring(0, currentTextIndex);
					middle = message.substring(currentTextIndex, message.length());
					suffix = "";

					int index = middle.indexOf(":", middle.toLowerCase().indexOf(":") + 1);
					boolean foundSpace = false;
					while (!foundSpace) {
						String word = middle.substring(index - 1, index);
						if (word.equalsIgnoreCase(" ")) {
							foundSpace = true;
						}
						else {
							index--;
						}
					}

					suffix = middle.substring(index, middle.length());

					middle = middle.substring(0, middle.indexOf(":", middle.toLowerCase().indexOf(":") + 1));

					middle = middle.substring(0, middle.lastIndexOf(" ") + 1);
				}
				catch (Exception e) {
				}

				middle = middle.replaceAll("[0-9]", "X");
				message = prefix + middle + suffix;
			}

			lastIndex = currentTextIndex;

			if (lastIndex != -1) {
				lastIndex += primaryWord.length();
			}
		}
		// System.out.println(count);

		// try {
		// prefix =
		// message.substring(0,message.toLowerCase().indexOf(primaryWord));
		// foundText = true;
		// }
		// catch (Exception e) {
		// outputMessage = message;
		// }
		//
		// if (foundText) {
		// try {
		// prefix =
		// message.substring(0,message.toLowerCase().indexOf(primaryWord));
		// middle =
		// message.substring(message.toLowerCase().indexOf(primaryWord),message.length());
		// suffix = "";
		//
		// int index = middle.indexOf(":", middle.toLowerCase().indexOf(":") +1
		// );
		// boolean foundSpace = false;
		// while (!foundSpace) {
		// String word = middle.substring( index-1, index );
		// if (word.equalsIgnoreCase(" ")) {
		// foundSpace = true;
		// } else {
		// index --;
		// }
		// }
		//
		// suffix = middle.substring(index, middle.length());
		//
		// middle = middle.substring(0, middle.indexOf(":",
		// middle.toLowerCase().indexOf(":") + 1));
		//
		// middle = middle.substring(0, middle.lastIndexOf(" ") +1 );
		// }
		// catch (Exception e) {}
		//
		// middle = middle.replaceAll("[0-9]+", "X");
		// outputMessage = prefix + middle + suffix;
		// }

		return message;
	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setUnexpectAndInvalidOutputDetail(EquinoxRawData equinoxRawData, AbstractAF abstractAF) {

		splitMessageid(GssoDataManagement.convertHexToString(this.deliveryReportReq.getShortMessage()));
		if (drId == null) {
			drId = "";
		}

		Iterator<Entry<String, OrigInvokeProfile>> iterator = this.appInstance.getMapOrigProfile().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, OrigInvokeProfile> entry = (Entry<String, OrigInvokeProfile>) iterator.next();

			if (entry.getValue().getSmMessageId().equals(drId)) {

				this.messageId = entry.getValue().getSmMessageId();

				this.origInvoke = entry.getKey();

			}

		}

		isWriteSummary = true;
		invoke = rawDataInput.getInvoke();
		// nodeCommand = event;

		ArrayList<String> listSMPPRoamingService = ConfigureTool.getConfigureArray(ConfigName.SMPPGW_ROAMING_INTERFACE);
		if (GssoServiceManagement.containService(listSMPPRoamingService, equinoxRawData.getOrig())) {
			event = EventLog.SMPPGW_ROAMING_DELIVERY_REPORT.getEventLog();
			destNodeName = "SMPPGWROAMING";
		}
		else {
			event = EventLog.SMPPGW_DELIVERY_REPORT.getEventLog();
			destNodeName = "SMPPGW";
		}
		nodeCommand = event;

		appInstance.getMapOrigInvokeEventDetailInput().put(this.rawDataInput.getInvoke(), event);
		if (appInstance.getMapOrigProfile().get(origInvoke) != null) {
			if (appInstance.getMapOrigProfile().get(origInvoke).getSendWSOTPRequest().getService() != null) {
				appInstance.getMapOrigInvokeDetailScenario().put(origInvoke,
						appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

				/** SET DTAILS IDENTITY **/
				this.composeDetailsLog.setIdentity(appInstance.getMapOrigProfile().get(origInvoke).getSendWSOTPRequest().getService());
				this.composeSummary = new GssoComposeSummaryLog(abstractAF,
						appInstance.getMapOrigProfile().get(origInvoke).getSendWSOTPRequest().getService());
			}

		}
		/** SSO and WS Unexpect **/
		else {
			appInstance.getMapOrigInvokeDetailScenario().put(this.rawDataInput.getInvoke(), LogScenario.UNKNOWN.getLogScenario());

			/** SET DTAILS IDENTITY **/
			this.composeDetailsLog.setIdentity("unknown");
		}
		// appInstance.getMapOrigInvokeDetailScenario().put(this.rawDataInput.getInvoke(),
		// LogScenario.UNKNOWN.getLogScenario());

		this.mapDetails.setNoFlow();

		this.code = "008";
		this.description = "System Error";

		this.rawDatasOut.add(new DeliveryReportRes(rawDataInput).toRawDatas(drId));

		appInstance.getMapOrigInvokeEventDetailOutput().put(this.rawDataInput.getInvoke(), event);

	}
}