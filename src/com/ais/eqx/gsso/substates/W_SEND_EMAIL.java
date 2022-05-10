package com.ais.eqx.gsso.substates;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.GssoCommand;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.LogDestNodeResultDesc;
import com.ais.eqx.gsso.enums.MailServerResultCode;
import com.ais.eqx.gsso.enums.SoapResultCode;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.DestinationBean;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.MapDestinationBean;
import com.ais.eqx.gsso.instances.MapDetailsAndConfigType;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.SendEmailResponse;
import com.ais.eqx.gsso.instances.SubmitSMJsonFormatRes;
import com.ais.eqx.gsso.instances.SubmitSMXMLFormatRes;
import com.ais.eqx.gsso.interfaces.EQX;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.GssoMessageType;
import com.ais.eqx.gsso.interfaces.IAFSubState;
import com.ais.eqx.gsso.interfaces.OTPChannel;
import com.ais.eqx.gsso.interfaces.RetNumber;
import com.ais.eqx.gsso.interfaces.SentOTPResult;
import com.ais.eqx.gsso.jaxb.InstanceContext;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoComposeDebugLog;
import com.ais.eqx.gsso.utils.GssoComposeDetailsLog;
import com.ais.eqx.gsso.utils.GssoComposeSummaryLog;
import com.ais.eqx.gsso.utils.GssoConstructMessage;
import com.ais.eqx.gsso.utils.GssoDataManagement;
import com.ais.eqx.gsso.utils.InvokeFilter;
import com.ais.eqx.gsso.validator.SmsEmailDeliveryValidator;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;
import ec02.common.data.E01Data;

public class W_SEND_EMAIL implements IAFSubState {

	private EC02Instance				ec02Instance;
	private APPInstance					appInstance;

	private SendEmailResponse			sendEmailReq;

	private ArrayList<EquinoxRawData>	rawDatasOut;
	private OrigInvokeProfile			origInvokeProfile;

	private String						messageType;
	private String						otpChannel;
	private String						isSMSDone;
	private String						isDRDone;
	private String						waitDR;
	private String						origInvoke;
	private String						isSuccessTrue				= "true";
	private String						isSuccessFalse				= "false";
	private String						smscDeliveryReceipt;

	private long						startTimeOfInvokeIncoming;

	private EquinoxRawData				rawDataInput;
	private EquinoxRawData				rawDataOrig;

	private boolean						isWaitDREnable;
	private boolean						isSMSCDeliveryReceipt;

	private GssoComposeDetailsLog		composeDetailsLog;
	private MapDetailsAndConfigType		mapDetails;
	private GssoComposeDebugLog			composeDebugLog;

	private GssoComposeSummaryLog		composeSummary;
	private String						destNodeName				= "MailServer";
	private String						destNodeResultDescription	= "null";
	private String						destNodeResultCode			= "null";
	private String						destNodeCommand				= "SendEmail";

	private String						resCode						= "null";
	private String						resDes						= "null";
	private String						nodeCommand;

	private boolean						isWriteSummary				= false;

	// MAIN WAIT EMAIL RESPONSE
	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {

		/************** INITIAL *****************/
		sendEmailInitInstanceAndLog(equinoxRawData, abstractAF, ec02Instance);

		/************** CODING ******************/
		// System.out.println("Start W_SEND_Email");

		if (RetNumber.NORMAL.equals(equinoxRawData.getRet())) {
			try {
				sendEmailReq = InstanceContext.getGson().fromJson(rawDataInput.getRawDataAttribute(EquinoxAttribute.VAL),
						SendEmailResponse.class);
				SmsEmailDeliveryValidator.sendEmailValidator(sendEmailReq, equinoxRawData);

				String eCode = equinoxRawData.getRawDataAttribute(EQX.Attribute.ECODE);
				String resultCode = sendEmailReq.getResultCode();
				
				if (EQX.Ecode.ECODE_200.equals(eCode) && (EQX.ResultCode.RESULTCODE_200.equals(resultCode) || EQX.ResultCode.RESULTCODE_250.equals(resultCode))) {
					this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_RESPONSE_SUCCESS.getStatistic());

					normalCaseSuccess();

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_RESPONSE_SUCCESS
								.getStatistic());
						this.composeDebugLog.messageResponseSuccess(resultCode);
						this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
					}
				}
				else {
					
					if (EQX.Ecode.ECODE_200.equals(eCode) && !(EQX.ResultCode.RESULTCODE_200.equals(resultCode) || EQX.ResultCode.RESULTCODE_250.equals(resultCode))) {
						this.destNodeResultCode = resultCode;
					}
					else if (!EQX.Ecode.ECODE_200.equals(eCode) && (EQX.ResultCode.RESULTCODE_200.equals(resultCode) || EQX.ResultCode.RESULTCODE_250.equals(resultCode))) {
						this.destNodeResultCode = eCode;
					}
					else{
						this.destNodeResultCode = eCode;
					}

					String resultString = sendEmailReq.getResultString();
					if (StringUtils.isEmpty(resultString)) {
						String mailServerErrorMessage = MailServerResultCode.getErrorMessageFrom(this.destNodeResultCode);
						this.destNodeResultDescription = mailServerErrorMessage;
					}
					else {
						this.destNodeResultDescription = resultString;
					}

					this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_RESPONSE_RESULTCODE_ERROR
							.getStatistic());

					this.mapDetails.setNoFlow();

					errorCase();

					if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
						this.composeDebugLog.messageResponseFailed(resultCode);
						this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_RESPONSE_RESULTCODE_ERROR
								.getStatistic());
						this.composeDebugLog.setMessageValidator(EventName.COMPLETE);
					}

				}

			}
			catch (ValidationException validate) {
				this.destNodeResultDescription = validate.getMandatoryPath() + " " + validate.getMessage();
				this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_BAD_MAILSERVER_SENDEMAIL_RESPONSE.getStatistic());

				this.mapDetails.setNoFlow();

				errorCase();

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.messageResponseFailed(validate.getMessage());
					this.composeDebugLog.setFailureAvp(validate.getMandatoryPath() + " " + validate.getMessage());
					this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_BAD_MAILSERVER_SENDEMAIL_RESPONSE.getStatistic());
					this.composeDebugLog.setMessageValidator(EventName.INCOMPLETE);
				}

			}
		}
		// TIMEOUT ERROR REJECT ABOUT
		else {
			unNormalCase();

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.setMessageValidator("-");
			}

		}

		/* SAVE LOG */
		sendEmailSaveLog();

		return this.rawDatasOut;
	}

	private void normalCaseSuccess() {

		if (OTPChannel.EMAIL.equalsIgnoreCase(otpChannel)) {

			GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

			if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

				this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SUCCESS.getCode(),
						JsonResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

				resCode = JsonResultCode.SUCCESS.getCode();
				resDes = JsonResultCode.SUCCESS.getDescription();
			}
			else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

				this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SUCCESS.getCode(),
						SoapResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

				resCode = SoapResultCode.SUCCESS.getCode();
				resDes = SoapResultCode.SUCCESS.getDescription();
			}

			GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
			isWriteSummary = true;

		}
		else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {

			this.appInstance.getMapOrigProfile().get(origInvoke).getMapSentOTPResult()
					.put(SentOTPResult.IS_EMAIL, SentOTPResult.SUCCESS);

			// 'WAITDR' IS OFF
			if (!isWaitDREnable) {
				// Email Success and remain SMS Response
				if (isSMSDone == null) {

					// this.nextState = SubStates.W_SEND_SMS.toString();

				}
				// Email Success and SMS Error
				else if (SentOTPResult.ERROR.equalsIgnoreCase(isSMSDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
								.getCode(), JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));
						resCode = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
						resDes = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
								.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));
						resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
						resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

					}

					GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;

				}
				// Email Success and SMS Timeout
				else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isSMSDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS
								.getCode(), JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));
						resCode = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getCode();
						resDes = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
								.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));

						resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
						resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();
					}

					GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;

				}
				// SMS Success
				else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isSMSDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SUCCESS.getCode(),
								JsonResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));
						resCode = JsonResultCode.SUCCESS.getCode();
						resDes = JsonResultCode.SUCCESS.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SUCCESS.getCode(),
								SoapResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

						resCode = SoapResultCode.SUCCESS.getCode();
						resDes = SoapResultCode.SUCCESS.getDescription();
					}

					GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					/* REMOVE PROFILE */
					if (isDRDone != null || !isSMSCDeliveryReceipt) {

						GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;
					}

				}
			}
			// 'WAITDR' IS ON
			else {

				// Email Success and remain sms Response
				if (isSMSDone == null) {

					// this.nextState = SubStates.W_SEND_SMS.toString();

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						resCode = JsonResultCode.SUCCESS.getCode();
						resDes = JsonResultCode.SUCCESS.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						resCode = SoapResultCode.SUCCESS.getCode();
						resDes = SoapResultCode.SUCCESS.getDescription();
					}

				}
				// Email Success and sms Error
				else if (SentOTPResult.ERROR.equalsIgnoreCase(isSMSDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
								.getCode(), JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));
						resCode = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
						resDes = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
								.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));
						resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
						resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

					}

					GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;

				}
				// Email Success and sms Timeout
				else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isSMSDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS
								.getCode(), JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));

						resCode = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
						resDes = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();
					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
								.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));
						resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
						resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();
					}

					GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;

				}
				// Email Success and sms Success
				else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isSMSDone)) {

					// Doesn’t receive DeliveryReport Request
					if (isDRDone == null) {

						// this.nextState =
						// SubStates.W_DELIVERY_REPORT.toString();

					}
					// Receive DeliveryReport Request ERROR
					else if (SentOTPResult.ERROR.equalsIgnoreCase(isDRDone)) {

						GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig,
									JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode(), JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
											.getDescription(), isSuccessTrue).toRawDatas(appInstance));
							resCode = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
							resDes = JsonResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
									.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
									.toRawDatas(appInstance));

							resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
							resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();
						}

						GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

						/* REMOVE PROFILE */
						GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;

					}
					// DR TIMEOUT
					else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isDRDone)) {

						GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig,
									JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getCode(),
									JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
									.toRawDatas(appInstance));
							resCode = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getCode();
							resDes = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_SUCCESS.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS
									.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription(), isSuccessTrue)
									.toRawDatas(appInstance));
							resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getCode();
							resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_SUCCESS.getDescription();

						}

						GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

						/* REMOVE PROFILE */
						GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;

					}
					// DR SUCCESS
					else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isDRDone)) {

						GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SUCCESS.getCode(),
									JsonResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));

							resCode = JsonResultCode.SUCCESS.getCode();
							resDes = JsonResultCode.SUCCESS.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SUCCESS.getCode(),
									SoapResultCode.SUCCESS.getDescription(), isSuccessTrue).toRawDatas(appInstance));
							resCode = SoapResultCode.SUCCESS.getCode();
							resDes = SoapResultCode.SUCCESS.getDescription();

						}

						GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

						/* REMOVE PROFILE */
						GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;

					}
				}
			}
		}
	}

	private void unNormalCase() {

		if (RetNumber.TIMEOUT.equalsIgnoreCase(rawDataInput.getRet())) {

			this.destNodeResultDescription = LogDestNodeResultDesc.CONNECTION_TIMEOUT.getLogDestNodeResultDesc();
			this.appInstance.getMapOrigProfile().get(origInvoke).getMapSentOTPResult()
					.put(SentOTPResult.IS_EMAIL, SentOTPResult.TIMEOUT);
			this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_REQUEST_TIMEOUT.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_REQUEST_TIMEOUT.getStatistic());
			}

			if (OTPChannel.EMAIL.equalsIgnoreCase(otpChannel)) {

				if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

					this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_EMAIL_TIMEOUT.getCode(),
							JsonResultCode.SEND_EMAIL_TIMEOUT.getDescription(), isSuccessFalse).toRawDatas(appInstance));

					resCode = JsonResultCode.SEND_EMAIL_TIMEOUT.getCode();
					resDes = JsonResultCode.SEND_EMAIL_TIMEOUT.getDescription();

				}
				else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

					this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_EMAIL_FAIL.getCode(),
							SoapResultCode.SEND_EMAIL_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));
					resCode = SoapResultCode.SEND_EMAIL_FAIL.getCode();
					resDes = SoapResultCode.SEND_EMAIL_FAIL.getDescription();

				}

				GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

				/* REMOVE PROFILE */
				GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
				isWriteSummary = true;

			}
			else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {

				// 'WAITDR' IS OFF
				if (!isWaitDREnable) {

					if (isSMSDone == null) {

					}
					// SMS Error
					else if (SentOTPResult.ERROR.equalsIgnoreCase(isSMSDone)) {

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig,
									JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getCode(), JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT
											.getDescription(), isSuccessFalse).toRawDatas(appInstance));

							resCode = JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getCode();
							resDes = JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
									.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
									.toRawDatas(appInstance));

							resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
							resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

						}

						GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

						/* REMOVE PROFILE */
						GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;

					}
					// SMS Timeout
					else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isSMSDone)) {

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig,
									JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getCode(),
									JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getDescription(), isSuccessFalse)
									.toRawDatas(appInstance));

							resCode = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getCode();
							resDes = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
									.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
									.toRawDatas(appInstance));
							resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
							resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

						}

						GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

						/* REMOVE PROFILE */
						GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;

					}
					// SMS Success
					else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isSMSDone)) {

						GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig,
									JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getCode(),
									JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription(), isSuccessTrue)
									.toRawDatas(appInstance));

							resCode = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getCode();
							resDes = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL
									.getCode(), SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
									.toRawDatas(appInstance));
							resCode = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
							resDes = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

						}

						GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

						if (isDRDone != null || !isSMSCDeliveryReceipt) {
							/* REMOVE PROFILE */
							GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
							isWriteSummary = true;
						}

					}
				}
				// 'WAITDR' IS ON
				else {

					// Doesn’t receive DeliveryReport Request
					if (isSMSDone == null) {

						// this.nextState =
						// SubStates.W_DELIVERY_REPORT.toString();

					}
					// Receive DeliveryReport Request ERROR
					else if (SentOTPResult.ERROR.equalsIgnoreCase(isSMSDone)) {

						if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig,
									JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getCode(), JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT
											.getDescription(), isSuccessFalse).toRawDatas(appInstance));
							resCode = JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getCode();
							resDes = JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getDescription();

						}
						else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

							this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
									.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
									.toRawDatas(appInstance));
							resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
							resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

						}

						GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

						/* REMOVE PROFILE */
						GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;

					}
					// Receive DeliveryReport Request SUCCESS
					else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isSMSDone)) {

						// Doesn’t receive DeliveryReport Request
						if (isDRDone == null) {

							// this.nextState =
							// SubStates.W_DELIVERY_REPORT.toString();

						}
						// Receive DeliveryReport Request ERROR
						else if (SentOTPResult.ERROR.equalsIgnoreCase(isDRDone)) {

							if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

								this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig,
										JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getCode(),
										JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getDescription(), isSuccessFalse)
										.toRawDatas(appInstance));
								resCode = JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getCode();
								resDes = JsonResultCode.SEND_SMS_FAIL_EMAIL_TIMEOUT.getDescription();

							}
							else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

								this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig,
										SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
												.getDescription(), isSuccessFalse).toRawDatas(appInstance));

								resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
								resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

							}

							GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

							/* REMOVE PROFILE */
							GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
							isWriteSummary = true;

						}
						// DR TIMEOUT
						else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isDRDone)) {

							if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

								this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig,
										JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getCode(),
										JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getDescription(), isSuccessFalse)
										.toRawDatas(appInstance));

								resCode = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getCode();
								resDes = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_TIMEOUT.getDescription();
							}
							else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

								this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig,
										SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
												.getDescription(), isSuccessFalse).toRawDatas(appInstance));

								resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
								resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();
							}

							GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

							/* REMOVE PROFILE */
							GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
							isWriteSummary = true;

						}
						// DR SUCCESS
						else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isDRDone)) {

							GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

							if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

								this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig,
										JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getCode(),
										JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription(), isSuccessTrue)
										.toRawDatas(appInstance));

								resCode = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getCode();
								resDes = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_TIMEOUT.getDescription();

							}
							else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

								this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig,
										SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode(),
										SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
										.toRawDatas(appInstance));

								resCode = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
								resDes = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();
							}

							GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

							/* REMOVE PROFILE */
							GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
							isWriteSummary = true;

						}

					}
				}
			}
		}
		else if (RetNumber.ERROR.equalsIgnoreCase(rawDataInput.getRet())) {
			this.destNodeResultDescription = LogDestNodeResultDesc.ERROR.getLogDestNodeResultDesc();
			this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_RESPONSE_ERROR.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_RESPONSE_ERROR.getStatistic());
			}

			errorCase();
		}
		else if (RetNumber.ABORT.equalsIgnoreCase(rawDataInput.getRet())) {
			this.destNodeResultDescription = LogDestNodeResultDesc.ABORT.getLogDestNodeResultDesc();
			this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_RESPONSE_ABORT.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_RESPONSE_ABORT.getStatistic());
			}

			errorCase();
		}
		else if (RetNumber.REJECT.equalsIgnoreCase(rawDataInput.getRet())) {
			this.destNodeResultDescription = LogDestNodeResultDesc.REJECT.getLogDestNodeResultDesc();
			this.ec02Instance.incrementsStat(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_RESPONSE_REJECT.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				this.composeDebugLog.addStatisticIn(Statistic.GSSO_RECEIVED_MAILSERVER_SENDEMAIL_RESPONSE_REJECT.getStatistic());
			}

			if (this.appInstance.getMapOrigProfile().get(origInvoke).getEmailRetryLimit() >= (Integer.parseInt(ConfigureTool
					.getConfigure(ConfigName.EMAIL_RETRIES)))) {

				errorCase();

			}
			else {

				this.appInstance.getMapOrigProfile().get(origInvoke).increaseEmailRetryLimit();

				this.rawDatasOut.add(GssoConstructMessage.createEMAILReqMessageForRetry(origInvoke, appInstance));

				this.ec02Instance.incrementsStat(Statistic.GSSO_SEND_MAILSERVER_SENDEMAIL_REQUEST.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					this.composeDebugLog.addStatisticOut(Statistic.GSSO_SEND_MAILSERVER_SENDEMAIL_REQUEST.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(this.rawDatasOut.get(0).getInvoke(),
						EventLog.SEND_EMAIL.getEventLog());
			}
		}

	}

	private void errorCase() {

		this.appInstance.getMapOrigProfile().get(origInvoke).getMapSentOTPResult().put(SentOTPResult.IS_EMAIL, SentOTPResult.ERROR);

		if (OTPChannel.EMAIL.equalsIgnoreCase(otpChannel)) {

			if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

				this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_EMAIL_FAIL.getCode(),
						JsonResultCode.SEND_EMAIL_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

				resCode = JsonResultCode.SEND_EMAIL_FAIL.getCode();
				resDes = JsonResultCode.SEND_EMAIL_FAIL.getDescription();

			}
			else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {
				this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_EMAIL_FAIL.getCode(),
						SoapResultCode.SEND_EMAIL_FAIL.getDescription(), isSuccessFalse).toRawDatas(appInstance));

				resCode = SoapResultCode.SEND_EMAIL_FAIL.getCode();
				resDes = SoapResultCode.SEND_EMAIL_FAIL.getDescription();

			}

			GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

			/* REMOVE PROFILE */
			GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
			isWriteSummary = true;

		}
		else if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {

			// 'WAITDR' IS OFF
			if (!isWaitDREnable) {
				// SMS Error and remain Email Response
				if (isSMSDone == null) {

					// this.nextState = SubStates.W_SEND_SMS.toString();

				}
				// SMS Error
				else if (SentOTPResult.ERROR.equalsIgnoreCase(isSMSDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL
								.getCode(), JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
								.toRawDatas(appInstance));
						resCode = JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						resDes = JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
								.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
								.toRawDatas(appInstance));
						resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();
					}

					GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;

				}
				// SMS Timeout
				else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isSMSDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL
								.getCode(), JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getDescription(), isSuccessFalse)
								.toRawDatas(appInstance));

						resCode = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getCode();
						resDes = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
								.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
								.toRawDatas(appInstance));
						resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();
					}

					GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;

				}
				// SMS Error and Email Success
				else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isSMSDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL
								.getCode(), JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));

						resCode = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						resDes = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();
					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL
								.getCode(), SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));

						resCode = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						resDes = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();
					}

					GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					if (isDRDone != null || !isSMSCDeliveryReceipt) {
						/* REMOVE PROFILE */
						GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
						isWriteSummary = true;
					}

				}
			}
			// 'WAITDR' IS ON
			else {

				// Doesn’t receive DeliveryReport Request
				if (isDRDone == null) {

					// this.nextState = SubStates.W_DELIVERY_REPORT.toString();

				}
				// Receive DeliveryReport Request ERROR
				else if (SentOTPResult.ERROR.equalsIgnoreCase(isDRDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL
								.getCode(), JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
								.toRawDatas(appInstance));

						resCode = JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						resDes = JsonResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
								.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
								.toRawDatas(appInstance));

						resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();
					}

					GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;

				}
				// DR TIMEOUT
				else if (SentOTPResult.TIMEOUT.equalsIgnoreCase(isDRDone)) {

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL
								.getCode(), JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getDescription(), isSuccessFalse)
								.toRawDatas(appInstance));

						resCode = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getCode();
						resDes = JsonResultCode.SEND_SMS_TIMEOUT_EMAIL_FAIL.getDescription();
					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL
								.getCode(), SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription(), isSuccessFalse)
								.toRawDatas(appInstance));
						resCode = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getCode();
						resDes = SoapResultCode.SEND_SMS_FAIL_EMAIL_FAIL.getDescription();
					}

					GssoDataManagement.raiseStatoutErrorForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;

				}
				// DR SUCCESS
				else if (SentOTPResult.SUCCESS.equalsIgnoreCase(isDRDone)) {

					GssoDataManagement.setTimeoutOfTransaction(appInstance, origInvoke);

					if (GssoMessageType.JSON.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMJsonFormatRes(this.rawDataOrig, JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL
								.getCode(), JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));

						resCode = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						resDes = JsonResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();

					}
					else if (GssoMessageType.SOAP.equalsIgnoreCase(messageType)) {

						this.rawDatasOut.add(new SubmitSMXMLFormatRes(this.rawDataOrig, SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL
								.getCode(), SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription(), isSuccessTrue)
								.toRawDatas(appInstance));

						resCode = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getCode();
						resDes = SoapResultCode.SEND_SMS_SUCCESS_EMAIL_FAIL.getDescription();
					}

					GssoDataManagement.raiseStatoutSuccessForSmsAndEmail(origInvokeProfile, ec02Instance, composeDebugLog);

					/* REMOVE PROFILE */
					GssoDataManagement.removeProfileAndTransaction(origInvoke, appInstance);
					isWriteSummary = true;

				}
			}
		}
	}

	private void sendEmailInitInstanceAndLog(EquinoxRawData equinoxRawData, AbstractAF abstractAF, EC02Instance ec02Instance) {
		this.rawDataInput = equinoxRawData;

		this.rawDatasOut = new ArrayList<EquinoxRawData>();
		this.ec02Instance = (EC02Instance) ec02Instance;
		this.appInstance = this.ec02Instance.getAppInstance();

		this.origInvoke = InvokeFilter.getOriginInvoke(equinoxRawData.getInvoke());
		this.origInvokeProfile = this.appInstance.getMapOrigProfile().get(origInvoke);
		
		if(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)){
			this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
			
			boolean isSms = false;
			boolean isEmail = false;
			if(origInvokeProfile.getSendWSOTPRequest().getOtpMobile()!=null && 
					!(origInvokeProfile.getSendWSOTPRequest().getOtpMobile().isEmpty())){
					isSms = true;
			}
			if(origInvokeProfile.getSendWSOTPRequest().getEmail()!=null && 
					!(origInvokeProfile.getSendWSOTPRequest().getEmail().isEmpty())){
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
			this.isSMSDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_SMS);
			this.isDRDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_DR);
//			this.waitDR = ConfigureTool.getConfigure(ConfigName.WAIT_DR);
			this.waitDR = origInvokeProfile.getSendWSOTPRequest().getWaitDR();
		}
		else if(origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP)){
			
			this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
			
			boolean isSms = false;
			boolean isEmail = false;
			if(origInvokeProfile.getSendWSOTPRequest().getOtpMobile()!=null && 
					!(origInvokeProfile.getSendWSOTPRequest().getOtpMobile().isEmpty())){
					isSms = true;
			}
			if(origInvokeProfile.getSendWSOTPRequest().getEmail()!=null && 
					!(origInvokeProfile.getSendWSOTPRequest().getEmail().isEmpty())){
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
			this.isSMSDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_SMS);
			this.isDRDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_DR);
//			this.waitDR = ConfigureTool.getConfigure(ConfigName.WAIT_DR);
			this.waitDR = origInvokeProfile.getSendWSOTPRequest().getWaitDR();
		}
		else if(origInvokeProfile.getGssoOTPRequest() !=null){

			this.rawDataOrig = origInvokeProfile.getOrigEquinoxRawData();
			this.otpChannel = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getOtpChannel();
			this.messageType = origInvokeProfile.getGssoOTPRequest().getMessageType();
			this.isSMSDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_SMS);
			this.isDRDone = origInvokeProfile.getMapSentOTPResult().get(SentOTPResult.IS_DR);
			this.waitDR = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getWaitDR();
		}
		
		if (OTPChannel.ALL.equalsIgnoreCase(otpChannel)) {
			this.smscDeliveryReceipt = origInvokeProfile.getRealSMSCDeliveryReceipt();
			this.isSMSCDeliveryReceipt = Boolean.parseBoolean(smscDeliveryReceipt);
		}

		this.isWaitDREnable = Boolean.parseBoolean(waitDR);

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			/** INITIAL DEBUG LOG **/
			this.composeDebugLog = new GssoComposeDebugLog(appInstance, ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED));
			this.composeDebugLog.setSubState(SubStates.W_SEND_EMAIL.name());
		}

		/** INITIAL DETAILS LOG **/
		this.composeDetailsLog = new GssoComposeDetailsLog(appInstance, abstractAF.getEquinoxProperties().getState() + "." + "BEGIN",
				abstractAF);
		/** SET DTAILS IDENTITY **/
		this.composeDetailsLog.setIdentity(origInvokeProfile.getDetailsService());

		this.composeDetailsLog.setDataOrig(origInvoke, rawDataInput, appInstance);
		this.mapDetails = new MapDetailsAndConfigType();

		appInstance.getMapOrigInvokeEventDetailInput().put(origInvoke, EventLog.SEND_EMAIL.getEventLog());

		appInstance.getMapOrigInvokeDetailScenario()
				.put(origInvoke, appInstance.getMapOrigProfile().get(origInvoke).getScenarioName());

		try {
			this.composeDetailsLog.initialIncoming(rawDataInput, appInstance);
			this.composeDetailsLog.addScenario(appInstance, rawDataInput, origInvoke);
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}

		this.startTimeOfInvokeIncoming = origInvokeProfile.getStartTimeOfInvoke();
		/** INITIATE SUMMARY-LOG **/
		this.composeSummary = new GssoComposeSummaryLog(abstractAF, origInvokeProfile.getDetailsService());

		EquinoxRawData origRawData = appInstance.getMapOrigProfile().get(this.origInvoke).getOrigEquinoxRawData();

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
			/** IDLE_WS_AUTHEN_OTP **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_SOAP.getMessageType())){
				nodeCommand = EventLog.WS_AUTHEN_OTP.getEventLog();
			}
			/** IDLE_WS_AUTHEN_OTP_ID **/
			else if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_ID_SOAP.getMessageType())){
				nodeCommand = EventLog.WS_AUTHEN_OTP_ID.getEventLog();
			}
			/** IDLE_SEND_OTP_REQ **/
			else{
				nodeCommand = EventLog.SEND_OTP.getEventLog();
			}
			
		}
	}

	private void sendEmailSaveLog() {
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

		if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
			/** WRITE DEBUG LOG **/
			this.composeDebugLog.initialGssoSubStateLog(rawDataInput);
			this.composeDebugLog.writeDebugSubStateLog();
		}

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
			this.composeSummary.setWriteSummary();
			try {
				this.composeSummary.initialSummary(this.appInstance, startTimeOfInvokeIncoming, origInvoke, nodeCommand, this.resCode, this.resDes);
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
	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data) {
		// TODO Auto-generated method stub
		return null;
	}

}