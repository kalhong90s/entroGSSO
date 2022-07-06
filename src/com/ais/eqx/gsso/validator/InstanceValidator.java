package com.ais.eqx.gsso.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ais.eqx.gsso.enums.SoapResultCode;
import com.ais.eqx.gsso.interfaces.*;
import ec02.af.utils.Log;
import org.apache.commons.lang3.StringUtils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.GssoAuthOTP;
import com.ais.eqx.gsso.instances.GssoAuthOTPRequest;
import com.ais.eqx.gsso.instances.GssoConfirmOTPRequest;
import com.ais.eqx.gsso.instances.GssoE01Datas;
import com.ais.eqx.gsso.instances.GssoGenPasskeyRequest;
import com.ais.eqx.gsso.instances.GssoOTPRequest;
import com.ais.eqx.gsso.instances.GssoProfile;
import com.ais.eqx.gsso.instances.GssoServiceTemplate;
import com.ais.eqx.gsso.instances.GssoWSConfirmOTPRequest;
import com.ais.eqx.gsso.instances.GssoWSConfirmOTPWithIDRequest;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.Refund;
import com.ais.eqx.gsso.instances.SendOneTimePWRequest;
import com.ais.eqx.gsso.instances.SendWSOTPRequest;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoDataManagement;
import com.ais.eqx.gsso.utils.InvokeFilter;

import ec02.af.abstracts.AbstractAF;
import ec02.common.data.E01Data;
import ec02.common.data.KeyObject;


public class InstanceValidator {

	@SuppressWarnings("unused")
	public static void gssoOTPValidator(String val, GssoOTPRequest otpRequest, APPInstance appInstance) throws ValidationException {

		Pattern typePattern = null;
		Matcher typeMatcher = null;
		String isCommend = "";

		/**
		 * Parameter
		 * 
		 * Val [M](CountryCodeList+MSISDN_Digit_Length)msisdn[?]
		 * [Oc](String(256))emailAddr[abc@ais.co.th] [M](String(60))service[]
		 * [M](String(?))accountType[prepaid,postpaid,ais,non-ais,all]
		 * [O](Int)lifeTimeoutMins[5,15,30] < Change [M] to [O] follow GSSO
		 * Softw.. Req.. V4.0.0 [M](String)otpChannel[sms,email,all]
		 * [O](Boolean)waitDR[Def=True][True,False]
		 * [O](Int)otpDigit[4-12][Def=4] [O](Int)refDigit[4-12][Def=4]
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;
		String[] possibleValue = null;

		if (otpRequest.getMessageType().equals(GssoMessageType.JSON)) {
			/** val **/
			mandatoryPath = EquinoxAttribute.VAL;
			if (val.equals("") || val.equals("{}") || !VerifyMessage.isJSONValid(val))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
		}
		else {
			if (val.equals("") || val.equals("{}"))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}


		/** service **/
		mandatoryPath = "service";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		/** accountType **/
		mandatoryPath = "accountType";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_MISSING);

		/** otpChannel **/
		mandatoryPath = "otpChannel";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_OTP_CHANNEL, VerifyMessageType.IS_MISSING);


		SendOneTimePWRequest sendOneTimePW = otpRequest.getSendOneTimePW();



		/** service **/
		mandatoryPath = "service";
		if (sendOneTimePW.getService() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		if (sendOneTimePW.getService().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_NULL);
		}

		if (sendOneTimePW.getService().length() > 60) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_INVALID);
		}

		/** accountType **/
		mandatoryPath = "accountType";
		possibleValue = new String[] { "PREPAID", "POSTPAID", "AIS", "NON-AIS", "ALL", "INTER" };
		if (sendOneTimePW.getAccountType() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_MISSING);

		if (sendOneTimePW.getAccountType().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_NULL);
		}

		if (!Arrays.asList(possibleValue).contains(sendOneTimePW.getAccountType().toUpperCase())) {
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_INVALID);
		}

		/** otpChannel **/
		mandatoryPath = "otpChannel";
		possibleValue = new String[] { "SMS", "EMAIL", "ALL" };
		if (sendOneTimePW.getOtpChannel() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_OTP_CHANNEL, VerifyMessageType.IS_MISSING);

		if (sendOneTimePW.getOtpChannel().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_OTP_CHANNEL, VerifyMessageType.IS_NULL);
		}

		if (!Arrays.asList(possibleValue).contains(sendOneTimePW.getOtpChannel().toUpperCase())) {
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_OTP_CHANNEL, VerifyMessageType.IS_INVALID);
		}

		possibleValue = new String[] { "SMS", "ALL" };
		if (Arrays.asList(possibleValue).contains(sendOneTimePW.getOtpChannel().toUpperCase())) {

			/** msisdn **/
			mandatoryPath = "msisdn";
			if (!val.contains(mandatoryPath))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

			/** msisdn **/
			mandatoryPath = "msisdn";
			int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
			String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
			if (sendOneTimePW.getMsisdn() == null)
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

			else if (sendOneTimePW.getMsisdn().isEmpty())
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);


			String msisdn = sendOneTimePW.getMsisdn();
			boolean foundCountry = false;
			try {
				for (String countryCode : countryCodeList) {
					String msisdnPrefix = msisdn.substring(0, countryCode.length());

					if (msisdnPrefix.contains(countryCode)) {

						if (msisdn.length() < msisdnDigitLength){
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
						}

						String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());

						String msisdnPattern = "[0-9]+";
						typePattern = Pattern.compile(msisdnPattern);
						typeMatcher = typePattern.matcher(realMsisdn);
						if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
							foundCountry = true;
							break;
						}
						else {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
						}
					}else{
						// is Inter
						GssoProfile gssoProfile = new GssoProfile();
						gssoProfile.setOper("INTER");
						gssoProfile.setLanguage("2");
						appInstance.setProfile(gssoProfile);

						String msisdnPattern = "[0-9]+";
						typePattern = Pattern.compile(msisdnPattern);
						typeMatcher = typePattern.matcher(msisdn);
						if (typeMatcher.matches()) {
							break;
						}
						else {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
						}
					}
				}
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
			}
//		if (!foundCountry) {
//			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
//		}
		}


		/** emailAddr **/
		if (otpRequest.getMessageType().equals(GssoMessageType.JSON)) {

			possibleValue = new String[] { "EMAIL", "ALL" };
			if (Arrays.asList(possibleValue).contains(sendOneTimePW.getOtpChannel().toUpperCase())) {
				mandatoryPath = "emailAddr";
				if (!val.contains(mandatoryPath)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT, VerifyMessageType.IS_MISSING);
				}

				if (sendOneTimePW.getEmailAddr() != null) {
					if (!sendOneTimePW.getEmailAddr().isEmpty()) {

						if (sendOneTimePW.getEmailAddr().contains(":")) {
							String[] arrayEmail = sendOneTimePW.getEmailAddr().split(":");
							for (String email : arrayEmail) {
								/** Verify Email Format **/
								String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
								typePattern = Pattern.compile(emailPattern);
								typeMatcher = typePattern.matcher(email);
								if (!typeMatcher.matches()) {
									throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT,
											VerifyMessageType.IS_INVALID);
								}
								else {
									/** EMAIL LENGTH > 256 **/
									if (email.length() > 256) {
										throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT,
												VerifyMessageType.IS_INVALID);
									}
								}
							}
						}
						else {
							/** Verify Email Format **/
							String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
							typePattern = Pattern.compile(emailPattern);
							typeMatcher = typePattern.matcher(sendOneTimePW.getEmailAddr());
							if (!typeMatcher.matches()) {
								throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT,
										VerifyMessageType.IS_INVALID);
							}
							else {
								/** EMAIL LENGTH > 256 **/
								if (sendOneTimePW.getEmailAddr().length() > 256) {
									throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT,
											VerifyMessageType.IS_INVALID);
								}
							}
						}
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT, VerifyMessageType.IS_NULL);
					}
				}
				else {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT, VerifyMessageType.IS_MISSING);
				}
			}
		}
		else if (otpRequest.getMessageType().equals(GssoMessageType.SOAP)) {
			
			possibleValue = new String[] { "EMAIL", "ALL" };
			if (Arrays.asList(possibleValue).contains(sendOneTimePW.getOtpChannel().toUpperCase())) {
				mandatoryPath = "emailAddr";
				if (!val.contains(mandatoryPath)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (sendOneTimePW.getEmailAddr() != null) {
					if (!sendOneTimePW.getEmailAddr().isEmpty()) {

						if (sendOneTimePW.getEmailAddr().contains(":")) {
							String[] arrayEmail = sendOneTimePW.getEmailAddr().split(":");
							for (String email : arrayEmail) {
								/** Verify Email Format **/
								String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
								typePattern = Pattern.compile(emailPattern);
								typeMatcher = typePattern.matcher(email);
								if (!typeMatcher.matches()) {
									throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
											VerifyMessageType.IS_INVALID);
								}
								else {
									/** EMAIL LENGTH > 256 **/
									if (email.length() > 256) {
										throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
												VerifyMessageType.IS_INVALID);
									}
								}
							}
						}
						else {
							/** Verify Email Format **/
							String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
							typePattern = Pattern.compile(emailPattern);
							typeMatcher = typePattern.matcher(sendOneTimePW.getEmailAddr());
							if (!typeMatcher.matches()) {
								throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
										VerifyMessageType.IS_INVALID);
							}
							else {
								/** EMAIL LENGTH > 256 **/
								if (sendOneTimePW.getEmailAddr().length() > 256) {
									throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
											VerifyMessageType.IS_INVALID);
								}
							}
						}
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
					}
				}
				else {
					throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}
		}

		if (sendOneTimePW.getMsisdn() == null || sendOneTimePW.getMsisdn().isEmpty() || "".equals(sendOneTimePW.getMsisdn().trim())) {
		}else {

			/** msisdn **/
			mandatoryPath = "msisdn";
			int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
			String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));


			String msisdn = sendOneTimePW.getMsisdn();
			boolean foundCountry = false;
			try {
				for (String countryCode : countryCodeList) {
					String msisdnPrefix = msisdn.substring(0, countryCode.length());

					if (msisdnPrefix.contains(countryCode)) {

						if (msisdn.length() < msisdnDigitLength) {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
						}

						String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());

						String msisdnPattern = "[0-9]+";
						typePattern = Pattern.compile(msisdnPattern);
						typeMatcher = typePattern.matcher(realMsisdn);
						if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
							foundCountry = true;
							break;
						} else {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
						}
					} else {
						// is Inter
						GssoProfile gssoProfile = new GssoProfile();
						gssoProfile.setOper("INTER");
						gssoProfile.setLanguage("2");
						appInstance.setProfile(gssoProfile);

						String msisdnPattern = "[0-9]+";
						typePattern = Pattern.compile(msisdnPattern);
						typeMatcher = typePattern.matcher(msisdn);
						if (typeMatcher.matches()) {
							break;
						} else {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
						}
					}
				}
			} catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
			}

		}

		/** waitDR **/
		mandatoryPath = "waitDR";
		possibleValue = new String[] { "TRUE", "FALSE" };
		if (sendOneTimePW.getWaitDR() != null) {
			if (sendOneTimePW.getWaitDR().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
			}

			if (!Arrays.asList(possibleValue).contains(sendOneTimePW.getWaitDR().toUpperCase())) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			}
		}

		/** otpDigit **/
		mandatoryPath = "otpDigit";
		isCommend = "Out of range";
		if (sendOneTimePW.getOtpDigit() == null) {

		}
		else {
			if (sendOneTimePW.getOtpDigit().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
			}
			try {
				String[] otpDigitLength = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.OTP_LENGTH));
				int minOtp = Integer.parseInt(otpDigitLength[0]);
				int maxOtp = Integer.parseInt(otpDigitLength[1]);

				int otpLength = Integer.parseInt(sendOneTimePW.getOtpDigit());
				if (otpLength < minOtp || otpLength > maxOtp) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, isCommend);
				}
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			}
		}

		// serviceIsBypass
		// require state and smsLanguage
		if(Arrays.asList(GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.USMP_BY_PASS_CONFIG_SERVICE_LIST).toUpperCase())).contains(sendOneTimePW.getService().toUpperCase())){

			mandatoryPath = "state";
			Log.d("EC02 <Active-State ="+ConfigureTool.getConfigure(ConfigName.ACTIVE_STATE)+" : sendOneTimePW.state="+sendOneTimePW.getState());

			if(null == sendOneTimePW.getState() || sendOneTimePW.getState().isEmpty() || "".equals(sendOneTimePW.getState().trim()) ){

			}else if(!Arrays.asList(GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.ACTIVE_STATE))).contains(sendOneTimePW.getState()))
			{
				throw new ValidationException(mandatoryPath, otpRequest.getMessageType().equals(GssoMessageType.SOAP)?JsonResultCode.WS_STATE_NOT_USE_SERVICE :JsonResultCode.STATE_NOT_USE_SERVICE , "not use service");
			}
			String reqSmsLanguage = otpRequest.getSendOneTimePW().getSmsLanguage();
			appInstance.getProfile().setOper(OperName.AIS);
			appInstance.getProfile().setLanguage(null != reqSmsLanguage && reqSmsLanguage.matches("0|1")?reqSmsLanguage:GssoLanguage.ALL);
		}



		/** refDigit **/
		mandatoryPath = "refDigit";
		if (sendOneTimePW.getRefDigit() == null) {

		}
		else {
			if (sendOneTimePW.getRefDigit().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
			}
			try {
				String[] refDigitLength = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.REF_LENGTH));
				int minRef = Integer.parseInt(refDigitLength[0]);
				int maxRef = Integer.parseInt(refDigitLength[1]);

				int refLength = Integer.parseInt(sendOneTimePW.getRefDigit());
				if (refLength < minRef || refLength > maxRef) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, isCommend);
				}
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			}
		}

		if (otpRequest.getMessageType().equals(GssoMessageType.SOAP)) {
			mandatoryPath = "addTimeoutMins";
		}
		else {
			mandatoryPath = "lifeTimeoutMins";
		}
		
		if (sendOneTimePW.getLifeTimeoutMins() != null) {
			if (sendOneTimePW.getLifeTimeoutMins().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
			}
			try {
				Integer.parseInt(sendOneTimePW.getLifeTimeoutMins());
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			}
		}
		
		/*
		 * APPLICATION CHECK SERVICETEMPLATE INFORMATION FROM THE INSTANCE IS EXISTED
		 */
		/* DO SERVICE TEMPLATE */
		if (appInstance.isInquirySubSuccess() || 
			(appInstance.getProfile().getOper() != null && appInstance.getProfile().getOper().equals("INTER"))) 
		{

			/*** CODING QUIRY E01 OR FOUND ST DO SEND EMAIL OR SMS ***/
			/* IF NOT FOUND SERVICE TEMPLATE DO QUIRY E01 */
			String service = sendOneTimePW.getService().toUpperCase();
			HashMap<String, GssoE01Datas> mapE01dataofService = appInstance.getMapE01dataofService();
			GssoE01Datas gssoE01Datas = mapE01dataofService.get(service);
			boolean isCheck = false;
			if (mapE01dataofService == null || mapE01dataofService.size() <= 0) {
				/* FOUND SERVICE TEMPLATE DO SMS OR EMAIL */
				if (!(gssoE01Datas == null || gssoE01Datas.getServiceTemplate() == null || gssoE01Datas.getServiceTemplate().size() <= 0)) {
					isCheck = true;
				}
			}
			else {
				/* FOUND SERVICE TEMPLATE DO SMS OR EMAIL */
				if (!(gssoE01Datas == null || gssoE01Datas.getServiceTemplate() == null || gssoE01Datas.getServiceTemplate().size() <= 0)) {
					isCheck = true;
				}
			}
			
			if(isCheck){
				for(GssoServiceTemplate gssoServiceTemplate : gssoE01Datas.getServiceTemplate()){

					boolean isRefund = false;	
					try {
						isRefund = Boolean.parseBoolean(gssoServiceTemplate.getRefundFlag());}
					catch (Exception e) {
						// TODO: handle exception
					}
					
					if(isRefund){
						String sessionId = sendOneTimePW.getSessionId();
						String refId = sendOneTimePW.getRefId();
						
						/** sessionId from Send OTP Request **/
						mandatoryPath = "sessionId from Send OTP Request";
						if (sessionId == null) {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
						}
						if (sessionId.isEmpty()) {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
						}
						
						/** refId from Send OTP Request **/
						mandatoryPath = "refId from Send OTP Request";
						if (refId == null) {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
						}
						if (refId.isEmpty()) {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	public static void gssoAuthOTPValidator(String val, GssoAuthOTPRequest authOtpRequest) throws ValidationException {

		Pattern typePattern = null;
		Matcher typeMatcher = null;
		String isCommend = "";

		/**
		 * Parameter
		 * 
		 * Val [M](CountryCodeList+MSISDN_Digit_Length)msisdn[?]
		 * [Oc](String(256))emailAddr[abc@ais.co.th] [M](String(60))service[]
		 * [M](Int)lifeTimeoutMins[5,15,30] < Change [M] to [O] follow GSSO
		 * Softw.. Req.. V4.0.0 [M](String)otpChannel[sms,email,all]
		 * [O](Boolean)waitDR[Def=True][True,False]
		 * [O](Int)otpDigit[4-12][Def=4] [O](Int)refDigit[4-12][Def=4]
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;
		String[] possibleValue = null;

		if (authOtpRequest.getMessageType().equals(GssoMessageType.JSON)) {
			/** val **/
			mandatoryPath = EquinoxAttribute.VAL;
			if (val.equals("") || val.equals("{}") || !VerifyMessage.isJSONValid(val))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
		}
		else {
			if (val.equals("") || val.equals("{}"))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}

		/** request **/
		mandatoryPath = "request";
		if (!val.contains("\"" + mandatoryPath + "\""))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);

		/** serviceTemplate **/
		mandatoryPath = "serviceTemplate";
		if (!val.contains("\"" + mandatoryPath + "\""))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);

		/** msisdn **/
		mandatoryPath = "msisdn";
		if (!val.contains("\"" + mandatoryPath + "\""))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		/** service **/
		mandatoryPath = "service";
		if (!val.contains("\"" + mandatoryPath + "\""))
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		/** otpChannel **/
		mandatoryPath = "otpChannel";
		if (!val.contains("\"" + mandatoryPath + "\""))
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_OTP_CHANNEL, VerifyMessageType.IS_MISSING);

		/** request **/
		mandatoryPath = "request";
		GssoAuthOTP authenOnetimePassword = authOtpRequest.getAuthenOnetimePassword();
		SendOneTimePWRequest otpRequest = authenOnetimePassword.getRequest();
		if (otpRequest == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);

		/** serviceTemplate **/
		mandatoryPath = "serviceTemplate";
		GssoServiceTemplate serviceTemplate = authenOnetimePassword.getServiceTemplate();
		if (serviceTemplate == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);

		/** msisdn **/
		mandatoryPath = "msisdn";
		int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
		if (otpRequest.getMsisdn() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		else if (otpRequest.getMsisdn().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);


		String msisdn = otpRequest.getMsisdn();
		boolean foundCountry = false;
		try {
			for (String countryCode : countryCodeList) {
				String msisdnPrefix = msisdn.substring(0, countryCode.length());
				if (msisdnPrefix.contains(countryCode)) {

					if (msisdn.length() < msisdnDigitLength){
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
					
					String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());

					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(realMsisdn);
					if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}
				else{
					/** INTER CASE **/
					foundCountry = true;
					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(msisdn);
					if (typeMatcher.matches()) {
						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
//		if (!foundCountry) {
//			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
//		}

		/** service **/
		mandatoryPath = "service";
		if (otpRequest.getService() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		if (otpRequest.getService().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_NULL);
		}

		if (otpRequest.getService().length() > 60) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_INVALID);
		}

		/** serviceKey **/
		mandatoryPath = "serviceKey";
		if (otpRequest.getServiceKey() != null) {

			if (otpRequest.getServiceKey().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
			}

			String msisdnPattern = "[G][S][0-9][0-9][0-9]";
			Pattern typePatternServiceKey = null;
			Matcher typeMatcherServiceKey = null;
			typePatternServiceKey = Pattern.compile(msisdnPattern);
			typeMatcherServiceKey = typePatternServiceKey.matcher(otpRequest.getServiceKey());
			if (!typeMatcherServiceKey.matches()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			}
		}

		mandatoryPath = "lifeTimeoutMins";
		if (otpRequest.getLifeTimeoutMins() != null) {
			if (otpRequest.getLifeTimeoutMins().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
			}
			try {
				Integer.parseInt(otpRequest.getLifeTimeoutMins());
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			}
		}

		/** otpChannel **/
		mandatoryPath = "otpChannel";
		possibleValue = new String[] { "SMS", "EMAIL", "ALL" };
		if (otpRequest.getOtpChannel() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_OTP_CHANNEL, VerifyMessageType.IS_MISSING);

		if (otpRequest.getOtpChannel().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_OTP_CHANNEL, VerifyMessageType.IS_NULL);
		}

		if (!Arrays.asList(possibleValue).contains(otpRequest.getOtpChannel().toUpperCase())) {
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_OTP_CHANNEL, VerifyMessageType.IS_INVALID);
		}

		/** emailAddr **/
		mandatoryPath = "emailAddr";
		possibleValue = new String[] { "EMAIL", "ALL" };
		if (Arrays.asList(possibleValue).contains(otpRequest.getOtpChannel().toUpperCase())) {

			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT, VerifyMessageType.IS_MISSING);
			}

			if (otpRequest.getEmailAddr() != null) {
				if (!otpRequest.getEmailAddr().isEmpty()) {

					if (otpRequest.getEmailAddr().contains(":")) {
						String[] arrayEmail = otpRequest.getEmailAddr().split(":");
						for (String email : arrayEmail) {
							/** Verify Email Format **/
							String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
							typePattern = Pattern.compile(emailPattern);
							typeMatcher = typePattern.matcher(email);
							if (!typeMatcher.matches()) {
								throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT,
										VerifyMessageType.IS_INVALID);
							}
							else {
								/** EMAIL LENGTH > 256 **/
								if (email.length() > 256) {
									throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT,
											VerifyMessageType.IS_INVALID);
								}
							}
						}
					}
					else {
						/** Verify Email Format **/
						String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
						typePattern = Pattern.compile(emailPattern);
						typeMatcher = typePattern.matcher(otpRequest.getEmailAddr());
						if (!typeMatcher.matches()) {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT,
									VerifyMessageType.IS_INVALID);
						}
						else {
							/** EMAIL LENGTH > 256 **/
							if (otpRequest.getEmailAddr().length() > 256) {
								throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT,
										VerifyMessageType.IS_INVALID);
							}
						}
					}
				}
				else {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT, VerifyMessageType.IS_NULL);
				}
			}
			else {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_EMAIL_ADDR_FORMAT, VerifyMessageType.IS_MISSING);
			}
		}

		/** waitDR **/
		mandatoryPath = "waitDR";
		possibleValue = new String[] { "TRUE", "FALSE" };
		if (otpRequest.getWaitDR() != null) {
			if (otpRequest.getWaitDR().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
			}

			if (!Arrays.asList(possibleValue).contains(otpRequest.getWaitDR().toUpperCase())) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			}
		}

		/** otpDigit **/
		mandatoryPath = "otpDigit";
		isCommend = "Out of range";
		if (otpRequest.getOtpDigit() == null) {

		}
		else {
			if (otpRequest.getOtpDigit().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
			}
			try {
				String[] otpDigitLength = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.OTP_LENGTH));
				int minOtp = Integer.parseInt(otpDigitLength[0]);
				int maxOtp = Integer.parseInt(otpDigitLength[1]);

				int otpLength = Integer.parseInt(otpRequest.getOtpDigit());
				if (otpLength < minOtp || otpLength > maxOtp) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, isCommend);
				}
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			}
		}

		/** refDigit **/
		mandatoryPath = "refDigit";
		if (otpRequest.getRefDigit() == null) {

		}
		else {
			if (otpRequest.getRefDigit().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
			}
			try {
				String[] refDigitLength = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.REF_LENGTH));
				int minRef = Integer.parseInt(refDigitLength[0]);
				int maxRef = Integer.parseInt(refDigitLength[1]);

				int refLength = Integer.parseInt(otpRequest.getRefDigit());
				if (refLength < minRef || refLength > maxRef) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, isCommend);
				}
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			}
		}

		/** seedkey **/
		mandatoryPath = "seedkey";
		if (val.contains("\"" + mandatoryPath + "\"")) {
			try {
				if (serviceTemplate.getSeedkey() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			if (serviceTemplate.getSeedkey().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
			}
			if (serviceTemplate.getSeedkey().length() != 24) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			}
		}

		/** otpChannel **/
		if (otpRequest.getOtpChannel().equalsIgnoreCase(OTPChannel.ALL)) {
			/** allowSmsRoaming **/
			mandatoryPath = "allowSmsRoaming";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getAllowSmsRoaming() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getAllowSmsRoaming().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}

				possibleValue = new String[] { "TRUE", "FALSE" };
				if (!Arrays.asList(possibleValue).contains(serviceTemplate.getAllowSmsRoaming().toUpperCase())) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
				}
			}

			/** smsSender **/
			mandatoryPath = "smsSender";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getSmsSender() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getSmsSender().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}
			}

			/** smsBody **/
			mandatoryPath = "smsBody";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getSmsBody() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getSmsBody().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}
			}

			/** emailFrom **/
			mandatoryPath = "emailFrom";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getEmailFrom() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getEmailFrom().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}

				/** Verify Email Format **/
				if (serviceTemplate.getEmailFrom().contains(":")) {
					String[] arrayEmail = serviceTemplate.getEmailFrom().split(":");
					for (String email : arrayEmail) {
						String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
						typePattern = Pattern.compile(emailPattern);
						typeMatcher = typePattern.matcher(email);
						if (!typeMatcher.matches()) {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER,
									VerifyMessageType.IS_INVALID);
						}
					}
				}
				else {
					String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
					typePattern = Pattern.compile(emailPattern);
					typeMatcher = typePattern.matcher(serviceTemplate.getEmailFrom());
					if (!typeMatcher.matches()) {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER,
								VerifyMessageType.IS_INVALID);
					}
				}

			}

			/** emailSubject **/
			mandatoryPath = "emailSubject";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getEmailSubject() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getEmailSubject().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}

			}

			/** emailBody **/
			mandatoryPath = "emailBody";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getEmailBody() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getEmailBody().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}
			}

			/** smscDeliveryReceipt **/
			mandatoryPath = "smscDeliveryReceipt";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				
			}
			else {
				if (serviceTemplate.getSmscDeliveryReceipt() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getSmscDeliveryReceipt().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}

				possibleValue = new String[] { "TRUE", "FALSE" };
				if (!Arrays.asList(possibleValue).contains(serviceTemplate.getSmscDeliveryReceipt().toUpperCase())) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
				}
			}

		}
		else if (otpRequest.getOtpChannel().equalsIgnoreCase(OTPChannel.SMS)) {
			/** allowSmsRoaming **/
			mandatoryPath = "allowSmsRoaming";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getAllowSmsRoaming() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getAllowSmsRoaming().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}

				possibleValue = new String[] { "TRUE", "FALSE" };
				if (!Arrays.asList(possibleValue).contains(serviceTemplate.getAllowSmsRoaming().toUpperCase())) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
				}
			}
			/** smsSender **/
			mandatoryPath = "smsSender";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getSmsSender() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getSmsSender().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}
			}

			/** smsBody **/
			mandatoryPath = "smsBody";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getSmsBody() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getSmsBody().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}
			}

			/** smscDeliveryReceipt **/
			mandatoryPath = "smscDeliveryReceipt";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				
			}
			else {
				if (serviceTemplate.getSmscDeliveryReceipt() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getSmscDeliveryReceipt().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}

				possibleValue = new String[] { "TRUE", "FALSE" };
				if (!Arrays.asList(possibleValue).contains(serviceTemplate.getSmscDeliveryReceipt().toUpperCase())) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
				}
			}
		}
		else if (otpRequest.getOtpChannel().equalsIgnoreCase(OTPChannel.EMAIL)) {
			/** emailFrom **/
			mandatoryPath = "emailFrom";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getEmailFrom() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getEmailFrom().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}

				/** Verify Email Format **/
				String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
				typePattern = Pattern.compile(emailPattern);
				typeMatcher = typePattern.matcher(serviceTemplate.getEmailFrom());
				if (!typeMatcher.matches()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
				}
			}

			/** emailSubject **/
			mandatoryPath = "emailSubject";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getEmailSubject() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getEmailSubject().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}

			}

			/** emailBody **/
			mandatoryPath = "emailBody";
			if (!val.contains("\"" + mandatoryPath + "\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			else {
				if (serviceTemplate.getEmailBody() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

				if (serviceTemplate.getEmailBody().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
				}

			}
		}
	}

	public static void gssoGenPasskeyValidator(String val, GssoGenPasskeyRequest genPasskeyRequest, APPInstance appInstance) throws ValidationException {

		Pattern typePattern = null;
		Matcher typeMatcher = null;
		Pattern typePatternTransaction = null;
		Matcher typeMatcherTransaction = null;

		/**
		 * Parameter
		 * 
		 * Val
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;

		if (genPasskeyRequest.getMessageType().equals(GssoMessageType.JSON)) {
			/** val **/
			mandatoryPath = EquinoxAttribute.VAL;
			if (val.equals("") || val.equals("{}") || !VerifyMessage.isJSONValid(val))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		}
		else {
			if (val.equals("") || val.equals("{}"))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}

		/** msisdn **/
		mandatoryPath = "msisdn";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		/** transactionID **/
		mandatoryPath = "transactionID";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);

		/** service **/
		mandatoryPath = "service";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		/** msisdn **/
		mandatoryPath = "msisdn";
		int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
		if (genPasskeyRequest.getGeneratePasskey().getMsisdn() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		else if (genPasskeyRequest.getGeneratePasskey().getMsisdn().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);


		String msisdn = genPasskeyRequest.getGeneratePasskey().getMsisdn();
//		boolean foundCountry = false;
		try {
			for (String countryCode : countryCodeList) {
				String msisdnPrefix = msisdn.substring(0, countryCode.length());
				if (msisdnPrefix.contains(countryCode)) {

					if (msisdn.length() < msisdnDigitLength){
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
					
					String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());

					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(realMsisdn);
					if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
//						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}else{
					// is Inter
					GssoProfile gssoProfile = new GssoProfile();
					gssoProfile.setOper("INTER");
					gssoProfile.setLanguage("2");
					appInstance.setProfile(gssoProfile);
					
					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(msisdn);
					if (typeMatcher.matches()) {
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
		
//		if (!foundCountry) {
//			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
//		}

		/** transactionID **/
		mandatoryPath = "transactionID";
		if (genPasskeyRequest.getGeneratePasskey().getTransactionID() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (genPasskeyRequest.getGeneratePasskey().getTransactionID().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_NULL);
		}
		/* CHK FORMAT */
		String patternTransactionID =
		/* yyyyMMdd */ 
		"((([0-9]{4})(0[13578]|10|12)(0[1-9]|[12][0-9]|3[01]))"

		+ "|(([0-9]{4})(0[469]|11)([0][1-9]|[12][0-9]|30))"

		+ "|(([0-9]{4})(02)(0[1-9]|1[0-9]|2[0-8]))" + "|(([02468][048]00)(02)(29))" + "|(([13579][26]00)(02)(29))"
				+ "|(([0-9][0-9][0][48])(02)(29))" + "|(([0-9][0-9][2468][048])(02)(29))" + "|(([0-9][0-9][13579][26])(02)(29)))"
				/* HHMMSS */
				+ "([0-1][0-9]|2[0-3])([0-5][0-9])([0-5][0-9])"
				/* ssss */
				+ "(0[0-9]{3})";
		String transactionID = genPasskeyRequest.getGeneratePasskey().getTransactionID();

		typePatternTransaction = Pattern.compile(patternTransactionID);
		typeMatcherTransaction = typePatternTransaction.matcher(transactionID);
		if (!typeMatcherTransaction.matches()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_INVALID);
		}

		/** service **/
		mandatoryPath = "service";
		if (genPasskeyRequest.getGeneratePasskey().getService() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);
		if (genPasskeyRequest.getGeneratePasskey().getService().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_NULL);
		}
		if (genPasskeyRequest.getGeneratePasskey().getService().length() > 60) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}

	}

	public static void e01ServiceTemplateValidator(AbstractAF abstractAF, String origInvoke, final APPInstance appInstance)
			throws ValidationException {

		/**
		 * Parameter
		 * 
		 * [M]Key0 [M]Key1 [M]Key2 [M]Key3 [M]Key4 [M]data
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;
		String[] possibleValue = null;

		/** Key0 **/
		mandatoryPath = "key0";
		E01Data e01Data = abstractAF.getEquinoxUtils().getGlobalData().getDataResult().get(origInvoke);
		KeyObject keyObject = e01Data.getKeyObject();
		if (keyObject.getKey0() == null || keyObject.getKey0().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);

		/** Key1 **/
		mandatoryPath = "serviceName";
		if (keyObject.getKey1() == null || keyObject.getKey1().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);

		/** Key2 **/
		mandatoryPath = "key2";
		if (keyObject.getKey2() == null || keyObject.getKey2().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);

		/** Key3 **/
		mandatoryPath = "key3";
		if (keyObject.getKey3() == null || keyObject.getKey3().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);

		/** Key4 **/
		mandatoryPath = "key4";
		if (keyObject.getKey4() == null || keyObject.getKey4().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);

		/** Data **/
		mandatoryPath = "data";
		if (e01Data.getData() == null || e01Data.getData().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);

		GssoE01Datas gssoE01Datas = GssoDataManagement.extractGssoServiceTemplate(e01Data.getData());

		if (gssoE01Datas == null || gssoE01Datas.getServiceTemplate() == null || gssoE01Datas.getServiceTemplate().size() <= 0) {
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
		}

//		if (gssoE01Datas.getServiceKey() == null || gssoE01Datas.getServiceKey().isEmpty()) {
//			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
//		}

		/*
		 * V.10
		 */
//		mandatoryPath = "serviceKey";
//		String msisdnPattern = "[G][S][0-9][0-9][0-9]";
//		Pattern typePatternServiceKey = null;
//		Matcher typeMatcherServiceKey = null;
//		typePatternServiceKey = Pattern.compile(msisdnPattern);
//		typeMatcherServiceKey = typePatternServiceKey.matcher(gssoE01Datas.getServiceKey());
//		if (!typeMatcherServiceKey.matches()) {
//			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
//		}
		
		/*
		 * V2.0
		 */
		mandatoryPath = "serviceKey";
		
		if (gssoE01Datas.getServiceKey() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
		}
		if (gssoE01Datas.getServiceKey().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
		}
		
		int E01_ServiceKey_limit_digit = ConfigureTool.getConfigureInteger(ConfigName.E01_SERVICEKEY_LIMIT_DIGIT);
		
		if (gssoE01Datas.getServiceKey().length() > E01_ServiceKey_limit_digit) {
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
		}

		/** Data **/
		for (GssoServiceTemplate gssoServiceTemplate : gssoE01Datas.getServiceTemplate()) {

			mandatoryPath = "serviceTemplate";
			if (gssoServiceTemplate == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}

			mandatoryPath = "oper";
			if (gssoServiceTemplate.getOper() == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}
			if (gssoServiceTemplate.getOper().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
			}

			mandatoryPath = "allowSmsRoaming";
			if (gssoServiceTemplate.getAllowSmsRoaming() == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}
			else {
				if (gssoServiceTemplate.getAllowSmsRoaming().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
				}
			}
			possibleValue = new String[] { "TRUE", "FALSE" };
			if (!Arrays.asList(possibleValue).contains(gssoServiceTemplate.getAllowSmsRoaming().toUpperCase())) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
			}

			/** smsSender **/
			mandatoryPath = "smsSender";
			if (gssoServiceTemplate.getSmsSender() == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}
			if (gssoServiceTemplate.getSmsSender().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
			}

			/** smsBodyThai **/
			mandatoryPath = "smsBodyThai";
			if (gssoServiceTemplate.getSmsBodyThai() == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}
			if (gssoServiceTemplate.getSmsBodyThai().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
			}

			/** smsBodyEng **/
			mandatoryPath = "smsBodyEng";
			if (gssoServiceTemplate.getSmsBodyEng() == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}
			if (gssoServiceTemplate.getSmsBodyEng().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
			}

			/** emailFrom **/
			mandatoryPath = "emailFrom";
			if (gssoServiceTemplate.getEmailFrom() == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}
			if (gssoServiceTemplate.getEmailFrom().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
			}

			/** Verify Email Format **/
			Pattern typePatternEmail = null;
			Matcher typeMatcherEmail = null;
			if (gssoServiceTemplate.getEmailFrom().contains(":")) {
				String[] arrayEmail = gssoServiceTemplate.getEmailFrom().split(":");
				for (String email : arrayEmail) {
					String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
					typePatternEmail = Pattern.compile(emailPattern);
					typeMatcherEmail = typePatternEmail.matcher(email);
					if (!typeMatcherEmail.matches()) {
						throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
					}
				}
			}
			else {
				String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
				typePatternEmail = Pattern.compile(emailPattern);
				typeMatcherEmail = typePatternEmail.matcher(gssoServiceTemplate.getEmailFrom());
				if (!typeMatcherEmail.matches()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
				}
			}

			/** emailSubject **/
			mandatoryPath = "emailSubject";
			if (gssoServiceTemplate.getEmailSubject() == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}
			if (gssoServiceTemplate.getEmailSubject().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
			}

			/** emailBody **/
			mandatoryPath = "emailBody";
			if (gssoServiceTemplate.getEmailBody() == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}
			if (gssoServiceTemplate.getEmailBody().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
			}

			/** waitDR **/
			mandatoryPath = "waitDR";
			possibleValue = new String[] { "TRUE", "FALSE" };
			if (gssoServiceTemplate.getWaitDR() != null) {
				if (gssoServiceTemplate.getWaitDR().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
				}

				if (!Arrays.asList(possibleValue).contains(gssoServiceTemplate.getWaitDR().toUpperCase())) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
				}
			}

			mandatoryPath = "otpDigit";
			if (gssoServiceTemplate.getOtpDigit() != null) {
				if (gssoServiceTemplate.getOtpDigit().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
				}
				try {
					if (gssoServiceTemplate.getOtpDigit() != null && !gssoServiceTemplate.getOtpDigit().isEmpty()) {
						String errorDes = "Out of range";
						String[] otpDigitLength = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.OTP_LENGTH));
						int minOtpDigit = Integer.parseInt(otpDigitLength[0]);
						int maxOtpDigit = Integer.parseInt(otpDigitLength[1]);
						int stOtpDigit = Integer.parseInt(gssoServiceTemplate.getOtpDigit());
						if (minOtpDigit > stOtpDigit || maxOtpDigit < stOtpDigit) {
							throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, errorDes);
						}
					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
				}
			}

			mandatoryPath = "refDigit";
			if (gssoServiceTemplate.getRefDigit() != null) {
				if (gssoServiceTemplate.getRefDigit().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
				}
				try {
					if (gssoServiceTemplate.getRefDigit() != null && !gssoServiceTemplate.getRefDigit().isEmpty()) {
						String errorDes = "Out of range";
						String[] refDigitLength = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.REF_LENGTH));
						int minRefDigit = Integer.parseInt(refDigitLength[0]);
						int maxRefDigit = Integer.parseInt(refDigitLength[1]);
						int stRefDigit = Integer.parseInt(gssoServiceTemplate.getRefDigit());
						if (minRefDigit > stRefDigit || maxRefDigit < stRefDigit) {
							throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, errorDes);
						}
					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
				}
			}

			mandatoryPath = "lifeTimeoutMins";
			if (gssoServiceTemplate.getLifeTimeoutMins() != null) {
				if (gssoServiceTemplate.getLifeTimeoutMins().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
				}
				try {
					if (gssoServiceTemplate.getLifeTimeoutMins() != null && !gssoServiceTemplate.getLifeTimeoutMins().isEmpty()) {
						Integer.parseInt(gssoServiceTemplate.getLifeTimeoutMins());
					}

				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
				}
			}

			/** seedkey **/
			mandatoryPath = "seedkey";
			if (gssoServiceTemplate.getSeedkey() == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}
			else {
				if (gssoServiceTemplate.getSeedkey().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
				}
				if (gssoServiceTemplate.getSeedkey().length() != 24) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
				}
			}

			/** smscDeliveryReceipt **/
			mandatoryPath = "smscDeliveryReceipt";
			if (gssoServiceTemplate.getSmscDeliveryReceipt() == null) {
				throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
			}
			else {
				if (gssoServiceTemplate.getSmscDeliveryReceipt() == null) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
				}

				if (gssoServiceTemplate.getSmscDeliveryReceipt().isEmpty()) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
				}

				possibleValue = new String[] { "TRUE", "FALSE" };
				if (!Arrays.asList(possibleValue).contains(gssoServiceTemplate.getSmscDeliveryReceipt().toUpperCase())) {
					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
				}
			}

			/** refundFlag **/			
			mandatoryPath = "refundFlag";
			if (StringUtils.isNotEmpty(gssoServiceTemplate.getRefundFlag())) {
//				if (gssoServiceTemplate.getRefundFlag().isEmpty()) {
//					throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
//				}
				
				boolean isRefund = false;	
				try {
					isRefund = Boolean.parseBoolean(gssoServiceTemplate.getRefundFlag());}
				catch (Exception e) {
					// TODO: handle exception
				}
				
				if(isRefund){
					String _origInvoke = InvokeFilter.getOriginInvoke(origInvoke);
					OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(_origInvoke);
					String sessionId = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getSessionId();
					String refId = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getRefId();
//					String waitDR = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getWaitDR();
					
					/** sessionId from Send OTP Request **/
					mandatoryPath = "sessionId from Send OTP Request";
					if (sessionId == null) {
						throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
					}
					if (sessionId.isEmpty()) {
						throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
					}
					
					/** refId from Send OTP Request **/
					mandatoryPath = "refId from Send OTP Request";
					if (refId == null) {
						throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
					}
					if (refId.isEmpty()) {
						throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
					}
					
//					/** waitDR from Send OTP Request **/
//					mandatoryPath = "waitDR from Send OTP Request";
//					possibleValue = new String[] { "TRUE" };
//					if (!Arrays.asList(possibleValue).contains(waitDR.toUpperCase())) {
//						throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID);
//					}
//					else{
//						/** sessionId from Send OTP Request **/
//						mandatoryPath = "sessionId from Send OTP Request";
//						if (sessionId == null) {
//							throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
//						}
//						if (sessionId.isEmpty()) {
//							throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
//						}
//						
//						/** refId from Send OTP Request **/
//						mandatoryPath = "refId from Send OTP Request";
//						if (refId == null) {
//							throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_MISSING);
//						}
//						if (refId.isEmpty()) {
//							throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
//						}
//					}
				}
			}
		}
	}

	public static void gssoConfirmOTPValidator(String val, GssoConfirmOTPRequest confirmOTPReq) throws ValidationException {

		Pattern typePattern = null;
		Matcher typeMatcher = null;

		/**
		 * Parameter
		 * 
		 * Val [M](String)msisdn[66818888888] [M](String)pwd[00000]
		 * [M](String)transactionID[210841345814586509]
		 * [O](String)service[OnlineShopping]
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;

		if (confirmOTPReq.getMessageType().equals(GssoMessageType.JSON)) {
			/** val **/
			mandatoryPath = EquinoxAttribute.VAL;
			if (val.equals("") || val.equals("{}") || !VerifyMessage.isJSONValid(val))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
		}
		else {
			if (val.equals("") || val.equals("{}"))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}


		/** pwd **/
		mandatoryPath = "pwd";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_MISSING);

		/** transactionID **/
		mandatoryPath = "transactionID";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);

//		if (confirmOTPReq.getMessageType().equals(GssoMessageType.JSON)) {
//			/** service **/
//			mandatoryPath = "service";
//			if (!val.contains(mandatoryPath))
//				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);
//
//			/** service **/
//			mandatoryPath = "service";
//			if (confirmOTPReq.getConfirmOneTimePW().getService() == null)
//				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);
//
//			if (confirmOTPReq.getConfirmOneTimePW().getService().isEmpty()) {
//				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_NULL);
//			}
//
//			if (confirmOTPReq.getConfirmOneTimePW().getService().length() > 60) {
//				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_INVALID);
//			}
//		}
/*		*//** msisdn **//*
		mandatoryPath = "msisdn";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
		if (confirmOTPReq.getConfirmOneTimePW().getMsisdn() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		else if (confirmOTPReq.getConfirmOneTimePW().getMsisdn().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);


		String msisdn = confirmOTPReq.getConfirmOneTimePW().getMsisdn();
		boolean foundCountry = false;
		try {
			for (String countryCode : countryCodeList) {
				String msisdnPrefix = msisdn.substring(0, countryCode.length());
				if (msisdnPrefix.contains(countryCode)) {
					
					if (msisdn.length() < msisdnDigitLength){
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
					
					String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());

					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(realMsisdn);
					if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}
				//** INTER Case //*
				else{
					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(msisdn);
					if (typeMatcher.matches()) {
						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}
					
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
		if (!foundCountry) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}*/

		/** pwd **/
		mandatoryPath = "pwd";
		if (confirmOTPReq.getConfirmOneTimePW().getPwd() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (confirmOTPReq.getConfirmOneTimePW().getPwd().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_NULL);
		}
		try {
			if (confirmOTPReq.getConfirmOneTimePW().getPwd().length() < 4
					|| confirmOTPReq.getConfirmOneTimePW().getPwd().length() > 12) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT,
						VerifyMessageType.IS_INVALID);
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_INVALID);
		}

		/** transactionID **/
		mandatoryPath = "transactionID";
		if (confirmOTPReq.getConfirmOneTimePW().getTransactionID() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (confirmOTPReq.getConfirmOneTimePW().getTransactionID().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_NULL);
		}
		if (confirmOTPReq.getConfirmOneTimePW().getTransactionID().length() > 18) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_INVALID);
		}

	}

	public static void gssoConfirmOTPWPasskeyValidator(String val, GssoConfirmOTPRequest confirmOTPReq) throws ValidationException {

		Pattern typePattern = null;
		Matcher typeMatcher = null;

		/**
		 * Parameter
		 * 
		 * Val [M](String)msisdn[66818888888] [M](String)pwd[00000]
		 * [M](String)transactionID[210841345814586509]
		 * [O](String)service[OnlineShopping]
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;

		if (confirmOTPReq.getMessageType().equals(GssoMessageType.JSON)) {
			/** val **/
			mandatoryPath = EquinoxAttribute.VAL;
			if (val.equals("") || val.equals("{}") || !VerifyMessage.isJSONValid(val))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);
		}
		else {
			if (val.equals("") || val.equals("{}"))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}

		/** msisdn **/
		mandatoryPath = "msisdn";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		/** pwd **/
		mandatoryPath = "pwd";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_MISSING);

		/** transactionID **/
		mandatoryPath = "transactionID";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);

		if (confirmOTPReq.getMessageType().equals(GssoMessageType.JSON)) {

			/** service **/
			mandatoryPath = "service";
			if (!val.contains(mandatoryPath))
				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

			/** service **/
			mandatoryPath = "service";
			if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getService() == null)
				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

			if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getService().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_NULL);
			}

			if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getService().length() > 60) {
				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_INVALID);
			}

		}

		/** msisdn **/
		mandatoryPath = "msisdn";
		int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
		if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getMsisdn() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		else if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getMsisdn().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);

		String msisdn = confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getMsisdn();
		boolean foundCountry = false;
		try {
			for (String countryCode : countryCodeList) {
				String msisdnPrefix = msisdn.substring(0, countryCode.length());
				if (msisdnPrefix.contains(countryCode)) {
					
					if (msisdn.length() < msisdnDigitLength){
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
					
					String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());

					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(realMsisdn);
					if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}
				/** INTER CASE**/
				else{
					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(msisdn);
					if (typeMatcher.matches()) {
						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
		if (!foundCountry) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}

		/** pwd **/
		mandatoryPath = "pwd";
		if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getPwd() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getPwd().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_NULL);
		}
		try {
			if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getPwd().length() < 4
					|| confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getPwd().length() > 12) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT,
						VerifyMessageType.IS_INVALID);
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_INVALID);
		}

		/** transactionID **/
		mandatoryPath = "transactionID";
		if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getTransactionID() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getTransactionID().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_NULL);
		}
		if (confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getTransactionID().length() > 18) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_INVALID);
		}

	}
	
	public static void gssoWSConfirmOTPValidator(String val, GssoWSConfirmOTPRequest wsconfirmOTPReq) throws ValidationException {

		Pattern typePattern = null;
		Matcher typeMatcher = null;

		/**
		 * Parameter
		 * 
		 * Val [M](String)msisdn[66818888888] [M](String)pwd[00000]
		 * [M](String)transactionID[210841345814586509]
		 * [O](String)service[OnlineShopping]
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;

	
		if (val.equals("") || val.equals("{}"))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
	

		/** msisdn **/
		mandatoryPath = "msisdn";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		/** password **/
		mandatoryPath = "password";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_MISSING);

		/** sessionId **/
		mandatoryPath = "sessionId";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);

		/** msisdn **/
		mandatoryPath = "msisdn";
		int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
		if (wsconfirmOTPReq.getSendWSConfirmOTPReq().getMsisdn() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		else if (wsconfirmOTPReq.getSendWSConfirmOTPReq().getMsisdn().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);

		else if (wsconfirmOTPReq.getSendWSConfirmOTPReq().getMsisdn().length() < msisdnDigitLength)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);

		String msisdn = wsconfirmOTPReq.getSendWSConfirmOTPReq().getMsisdn();
//		boolean foundCountry = false;
		try {
			for (String countryCode : countryCodeList) {
				String msisdnPrefix = msisdn.substring(0, countryCode.length());
				if (msisdnPrefix.contains(countryCode)) {

					String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());

					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(realMsisdn);
					if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
//						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}else{
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
				}
					
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
//		if (!foundCountry) {
//			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
//		}

		/** password **/
		mandatoryPath = "password";
		if (wsconfirmOTPReq.getSendWSConfirmOTPReq().getPassword() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (wsconfirmOTPReq.getSendWSConfirmOTPReq().getPassword().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_NULL);
		}
		try {
			if (wsconfirmOTPReq.getSendWSConfirmOTPReq().getPassword().length() < 4
					|| wsconfirmOTPReq.getSendWSConfirmOTPReq().getPassword().length() > 12) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT,
						VerifyMessageType.IS_INVALID);
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_INVALID);
		}

		/** sessionId **/
		mandatoryPath = "sessionId";
		if (wsconfirmOTPReq.getSendWSConfirmOTPReq().getSessionId() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (wsconfirmOTPReq.getSendWSConfirmOTPReq().getSessionId().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_NULL);
		}
		if (wsconfirmOTPReq.getSendWSConfirmOTPReq().getSessionId().length() > 18) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_INVALID);
		}

	}
	
	public static void gssoWSConfirmOTPWithIDValidator(String val, GssoWSConfirmOTPWithIDRequest wsConfirmOTPWithIDReq) throws ValidationException {

		Pattern typePattern = null;
		Matcher typeMatcher = null;

		/**
		 * Parameter
		 * 
		 * Val [M](String)msisdn[66818888888] [M](String)pwd[00000]
		 * [M](String)transactionID[210841345814586509]
		 * [O](String)service[OnlineShopping]
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;

		if (val.equals("") || val.equals("{}"))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
	
		/** msisdn **/
		mandatoryPath = "msisdn";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		/** password **/
		mandatoryPath = "password";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_MISSING);

		/** sessionId **/
		mandatoryPath = "sessionId";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);

		/** msisdn **/
		mandatoryPath = "msisdn";
		int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
		if (wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getMsisdn() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		else if (wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getMsisdn().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);

		else if (wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getMsisdn().length() < msisdnDigitLength)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);

		String msisdn = wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getMsisdn();
//		boolean foundCountry = false;
		try {
			for (String countryCode : countryCodeList) {
				String msisdnPrefix = msisdn.substring(0, countryCode.length());
				if (msisdnPrefix.contains(countryCode)) {

					String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());

					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(realMsisdn);
					if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
//						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}else{
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
				}
					
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
//		if (!foundCountry) {
//			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
//		}

		/** password **/
		mandatoryPath = "password";
		if (wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getPassword() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getPassword().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_NULL);
		}
		try {
			if (wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getPassword().length() < 4
					|| wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getPassword().length() > 12) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT,
						VerifyMessageType.IS_INVALID);
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_ONETIME_PASSWORD_FORMAT, VerifyMessageType.IS_INVALID);
		}

		/** sessionId **/
		mandatoryPath = "sessionId";
		if (wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getSessionId() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getSessionId().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_NULL);
		}
		if (wsConfirmOTPWithIDReq.getSendWSConfirmOTPWithIDReq().getSessionId().length() > 18) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_INVALID);
		}

	}
	
	
	public static void gssoWSAuthenOTPValidator(String val, SendWSOTPRequest sendWSOTPRequest, APPInstance appInstance) throws ValidationException {
	
			Pattern typePattern = null;
			Matcher typeMatcher = null;
			Boolean isOTPMobile = false;
			Boolean isEmail = false;
			/**
			 * Parameter
			 * 
			 * Val [M](CountryCodeList+MSISDN_Digit_Length)msisdn[?]
			 * [Oc](String(256))emailAddr[abc@ais.co.th] [M](String(60))service[]
			 * [M](String(?))accountType[prepaid,postpaid,ais,non-ais,all]
			 * [O](Int)lifeTimeoutMins[5,15,30] < Change [M] to [O] follow GSSO
			 * Softw.. Req.. V4.0.0 [M](String)otpChannel[sms,email,all]
			 * [O](Boolean)waitDR[Def=True][True,False]
			 * [O](Int)otpDigit[4-12][Def=4] [O](Int)refDigit[4-12][Def=4]
			 * 
			 * **/
	
			/* Validate Attribute */
			String mandatoryPath = null;
			String[] possibleValue = null;
	
			
			if (val.equals("") || val.equals("{}"))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
			
	
			/** msisdn **/
			mandatoryPath = "msisdn";
			if (!val.contains(mandatoryPath))
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);
	
			/** service **/
			mandatoryPath = "service";
			if (!val.contains(mandatoryPath))
				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);
	
			/** accountType **/
			mandatoryPath = "accountType";
			if (!val.contains(mandatoryPath))
				throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_MISSING);

			/** msisdn **/
			mandatoryPath = "msisdn";
			int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
			String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
			
			if (sendWSOTPRequest.getMsisdn() == null)
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);
	
			else if (sendWSOTPRequest.getMsisdn().isEmpty())
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);
	
			else if (sendWSOTPRequest.getMsisdn().length() < msisdnDigitLength)
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
	
			String msisdn = sendWSOTPRequest.getMsisdn();
			boolean foundCountry = false;
			try {
				for (String countryCode : countryCodeList) {
					String msisdnPrefix = msisdn.substring(0, countryCode.length());
					
					if (msisdnPrefix.contains(countryCode)) {
						String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());
						String msisdnPattern = "[0-9]+";
						typePattern = Pattern.compile(msisdnPattern);
						typeMatcher = typePattern.matcher(realMsisdn);
						if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
							foundCountry = true;
							break;
						}
						else {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
						}
					}
				}
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
			}
			if (!foundCountry) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
			}
			
			/** OTPmobile **/
			mandatoryPath = "OTPmobile";
			int otpMobileDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
			if (sendWSOTPRequest.getOtpMobile() != null && !sendWSOTPRequest.getOtpMobile().isEmpty()){
				if (sendWSOTPRequest.getOtpMobile().length() < otpMobileDigitLength){
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
				}
				
				String otpMobile = sendWSOTPRequest.getOtpMobile();
				foundCountry = false;
				try {
					for (String countryCode : countryCodeList) {
						String otpMobliePrefix = otpMobile.substring(0, countryCode.length());
						
						if (otpMobliePrefix.contains(countryCode)) {
							String realOtpMobile = otpMobile.substring(countryCode.length(), otpMobile.length());
							String otpMobilePattern = "[0-9]+";
							typePattern = Pattern.compile(otpMobilePattern);
							typeMatcher = typePattern.matcher(realOtpMobile);
							if (typeMatcher.matches() && realOtpMobile.length() == otpMobileDigitLength) {
								foundCountry = true;
								break;
							}
							else {
								throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
							}
						}
					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
				}
				if (!foundCountry) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
				}
				isOTPMobile = true;
			}
			
			/** service **/
			mandatoryPath = "service";
			if (sendWSOTPRequest.getService() == null)
				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);
	
			if (sendWSOTPRequest.getService().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_NULL);
			}
	
			if (sendWSOTPRequest.getService().length() > 60) {
				throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_INVALID);
			}
	
			/** accountType **/
			mandatoryPath = "accountType";
			possibleValue = new String[] { "PREPAID", "POSTPAID", "ALL" };
			if (sendWSOTPRequest.getAccountType() == null)
				throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_MISSING);
	
			if (sendWSOTPRequest.getAccountType().isEmpty()) {
				throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_NULL);
			}
	
			if (!Arrays.asList(possibleValue).contains(sendWSOTPRequest.getAccountType().toUpperCase())) {
				throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_INVALID);
			}
	
			/** email **/
			mandatoryPath = "email";
			if (sendWSOTPRequest.getEmail() != null && !sendWSOTPRequest.getEmail().isEmpty()){

				if (sendWSOTPRequest.getEmail().contains(";")) {
						String[] arrayEmail = sendWSOTPRequest.getEmail().split(":");
						for (String email : arrayEmail) {
							/** Verify Email Format **/
							String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
							typePattern = Pattern.compile(emailPattern);
							typeMatcher = typePattern.matcher(email);
							if (!typeMatcher.matches()) {
								throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
										VerifyMessageType.IS_INVALID);
							}
							else {
								/** EMAIL LENGTH > 256 **/
								if (email.length() > 256) {
									throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
											VerifyMessageType.IS_INVALID);
								}
							}
						}
					}
					else {
						/** Verify Email Format **/
						String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
						typePattern = Pattern.compile(emailPattern);
						typeMatcher = typePattern.matcher(sendWSOTPRequest.getEmail());
						if (!typeMatcher.matches()) {
							throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
									VerifyMessageType.IS_INVALID);
						}
						else {
							/** EMAIL LENGTH > 256 **/
							if (sendWSOTPRequest.getEmail().length() > 256) {
								throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
										VerifyMessageType.IS_INVALID);
							}
						}
					}
					
				isEmail = true;
				}
			
			mandatoryPath = "OTPMobile,Email";
			if(isOTPMobile==false && isEmail == false){
				throw new ValidationException(mandatoryPath, JsonResultCode.MISSING_VALUE_OTPMOBILE_AND_EMAIL,
						VerifyMessageType.IS_MISSING);
			}
		}
	

	public static void gssoWSAuthenOTPWithIDValidator(String val, SendWSOTPRequest sendWSOTPRequest, APPInstance appInstance) throws ValidationException {
		Pattern typePattern = null;
		Matcher typeMatcher = null;
		Boolean isOTPMobile = false;
		Boolean isEmail 	= false;
		/**
		 * Parameter
		 * 
		 * Val [M](CountryCodeList+MSISDN_Digit_Length)msisdn[?]
		 * [Oc](String(256))emailAddr[abc@ais.co.th] [M](String(60))service[]
		 * [M](String(?))accountType[prepaid,postpaid,ais,non-ais,all]
		 * [O](Int)lifeTimeoutMins[5,15,30] < Change [M] to [O] follow GSSO
		 * Softw.. Req.. V4.0.0 [M](String)otpChannel[sms,email,all]
		 * [O](Boolean)waitDR[Def=True][True,False]
		 * [O](Int)otpDigit[4-12][Def=4] [O](Int)refDigit[4-12][Def=4]
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;
		String[] possibleValue = null;

		if (val.equals("") || val.equals("{}"))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		
		/** msisdn **/
		mandatoryPath = "msisdn";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		/** service **/
		mandatoryPath = "service";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		/** accountType **/
		mandatoryPath = "accountType";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_DUMMY_ACCOUNT_TYPE, VerifyMessageType.IS_MISSING);

		/** msisdn **/
		mandatoryPath = "msisdn";
		int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
		
		if (sendWSOTPRequest.getMsisdn() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		else if (sendWSOTPRequest.getMsisdn().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);

		else if (sendWSOTPRequest.getMsisdn().length() < msisdnDigitLength)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);

		String msisdn = sendWSOTPRequest.getMsisdn();
		boolean foundCountry = false;
		try {
			for (String countryCode : countryCodeList) {
				String msisdnPrefix = msisdn.substring(0, countryCode.length());
				
				if (msisdnPrefix.contains(countryCode)) {
					String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());
					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(realMsisdn);
					if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
		if (!foundCountry) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
		
		/** OTPmobile **/
		mandatoryPath = "OTPmobile";
		int otpMobileDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		if (sendWSOTPRequest.getOtpMobile() != null && !sendWSOTPRequest.getOtpMobile().isEmpty()){
			if (sendWSOTPRequest.getOtpMobile().length() < otpMobileDigitLength){
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
			}
			
			String otpMobile = sendWSOTPRequest.getOtpMobile();
			foundCountry = false;
			try {
				for (String countryCode : countryCodeList) {
					String otpMobliePrefix = otpMobile.substring(0, countryCode.length());
					
					if (otpMobliePrefix.contains(countryCode)) {
						String realOtpMobile = otpMobile.substring(countryCode.length(), otpMobile.length());
						String otpMobilePattern = "[0-9]+";
						typePattern = Pattern.compile(otpMobilePattern);
						typeMatcher = typePattern.matcher(realOtpMobile);
						if (typeMatcher.matches() && realOtpMobile.length() == otpMobileDigitLength) {
							foundCountry = true;
							break;
						}
						else {
							throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
						}
					}
				}
			}
			catch (Exception e) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
			}
			if (!foundCountry) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
			}
			
			isOTPMobile = true;
		}
		
		/** email **/
		mandatoryPath = "email";
		if (sendWSOTPRequest.getEmail() != null && !sendWSOTPRequest.getEmail().isEmpty()){
			
				if (sendWSOTPRequest.getEmail().contains(";")) {
					String[] arrayEmail = sendWSOTPRequest.getEmail().split(":");
					for (String email : arrayEmail) {
						/** Verify Email Format **/
						String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
						typePattern = Pattern.compile(emailPattern);
						typeMatcher = typePattern.matcher(email);
						if (!typeMatcher.matches()) {
							throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
									VerifyMessageType.IS_INVALID);
						}
						else {
							/** EMAIL LENGTH > 256 **/
							if (email.length() > 256) {
								throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
										VerifyMessageType.IS_INVALID);
							}
						}
					}
				}
				else {
					/** Verify Email Format **/
					String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
					typePattern = Pattern.compile(emailPattern);
					typeMatcher = typePattern.matcher(sendWSOTPRequest.getEmail());
					if (!typeMatcher.matches()) {
						throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
								VerifyMessageType.IS_INVALID);
					}
					else {
						/** EMAIL LENGTH > 256 **/
						if (sendWSOTPRequest.getEmail().length() > 256) {
							throw new ValidationException(mandatoryPath, JsonResultCode.WS_WRONG_INPUT_PARAMETER,
									VerifyMessageType.IS_INVALID);
						}
					}
				}
				isEmail = true;
		}
		
		/** service **/
		mandatoryPath = "service";
		if (sendWSOTPRequest.getService() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		if (sendWSOTPRequest.getService().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_NULL);
		}

		if (sendWSOTPRequest.getService().length() > 60) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_INVALID);
		}

		/** accountType **/
		mandatoryPath = "accountType";
		possibleValue = new String[] { "PREPAID", "POSTPAID", "ALL" };
		if (sendWSOTPRequest.getAccountType() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_DUMMY_ACCOUNT_TYPE, VerifyMessageType.IS_MISSING);

		if (sendWSOTPRequest.getAccountType().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_DUMMY_ACCOUNT_TYPE, VerifyMessageType.IS_NULL);
		}

		if (!Arrays.asList(possibleValue).contains(sendWSOTPRequest.getAccountType().toUpperCase())) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_DUMMY_ACCOUNT_TYPE, VerifyMessageType.IS_INVALID);
		}
		
		mandatoryPath = "OTPMobile,Email";
		if(isOTPMobile==false && isEmail == false){
			throw new ValidationException(mandatoryPath, JsonResultCode.MISSING_VALUE_OTPMOBILE_AND_EMAIL,
					VerifyMessageType.IS_MISSING);
		}
		
	}
	
	public static void gssoWSCreateOTPValidator(String val, SendWSOTPRequest sendWSOTPRequest, APPInstance appInstance) throws ValidationException {
		Pattern typePattern = null;
		Matcher typeMatcher = null;
		
		/**
		 * Parameter
		 * 
		 * Val [M](CountryCodeList+MSISDN_Digit_Length)msisdn[?]
		 * [Oc](String(256))emailAddr[abc@ais.co.th] [M](String(60))service[]
		 * [M](String(?))accountType[prepaid,postpaid,ais,non-ais,all]
		 * [O](Int)lifeTimeoutMins[5,15,30] < Change [M] to [O] follow GSSO
		 * Softw.. Req.. V4.0.0 [M](String)otpChannel[sms,email,all]
		 * [O](Boolean)waitDR[Def=True][True,False]
		 * [O](Int)otpDigit[4-12][Def=4] [O](Int)refDigit[4-12][Def=4]
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;
		String[] possibleValue = null;

		if (val.equals("") || val.equals("{}"))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		
		/** msisdn **/
		mandatoryPath = "msisdn";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		/** service **/
		mandatoryPath = "service";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		/** accountType **/
		mandatoryPath = "accountType";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_MISSING);
		
		/** addTimeoutMins **/
		mandatoryPath = "addTimeoutMins";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.ADD_TIMEOUT_MINS_EXCEED, VerifyMessageType.IS_MISSING);


		/** msisdn **/
		mandatoryPath = "msisdn";
		int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
		
		if (sendWSOTPRequest.getMsisdn() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		else if (sendWSOTPRequest.getMsisdn().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);

		else if (sendWSOTPRequest.getMsisdn().length() < msisdnDigitLength)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);

		String msisdn = sendWSOTPRequest.getMsisdn();
		boolean foundCountry = false;
		try {
			for (String countryCode : countryCodeList) {
				String msisdnPrefix = msisdn.substring(0, countryCode.length());
				
				if (msisdnPrefix.contains(countryCode)) {
					String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());
					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(realMsisdn);
					if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
		if (!foundCountry) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
		
		/** service **/
		mandatoryPath = "service";
		if (sendWSOTPRequest.getService() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		if (sendWSOTPRequest.getService().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_NULL);
		}

		if (sendWSOTPRequest.getService().length() > 60) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_INVALID);
		}

		/** accountType **/
		mandatoryPath = "accountType";
		possibleValue = new String[] { "PREPAID", "POSTPAID", "ALL" };
		if (sendWSOTPRequest.getAccountType() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_MISSING);

		if (sendWSOTPRequest.getAccountType().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_NULL);
		}

		if (!Arrays.asList(possibleValue).contains(sendWSOTPRequest.getAccountType().toUpperCase())) {
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_INVALID);
		}
		
		/** addTimeoutMins **/
		mandatoryPath = "addTimeoutMins";
		if (sendWSOTPRequest.getAddTimeoutMins() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.ADD_TIMEOUT_MINS_EXCEED, VerifyMessageType.IS_MISSING);
		
		if (sendWSOTPRequest.getAddTimeoutMins().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.ADD_TIMEOUT_MINS_EXCEED, VerifyMessageType.IS_NULL);
		}
		
		try {
			Integer.parseInt(sendWSOTPRequest.getAddTimeoutMins());
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.ADD_TIMEOUT_MINS_EXCEED, VerifyMessageType.IS_INVALID);
		}
	
		
	}
	
	public static void gssoWSGenerateOTPValidator(String val, SendWSOTPRequest sendWSOTPRequest, APPInstance appInstance) throws ValidationException {
		Pattern typePattern = null;
		Matcher typeMatcher = null;
		
		/**
		 * Parameter
		 * 
		 * Val [M](CountryCodeList+MSISDN_Digit_Length)msisdn[?]
		 * [Oc](String(256))emailAddr[abc@ais.co.th] [M](String(60))service[]
		 * [M](String(?))accountType[prepaid,postpaid,ais,non-ais,all]
		 * [O](Int)lifeTimeoutMins[5,15,30] < Change [M] to [O] follow GSSO
		 * Softw.. Req.. V4.0.0 [M](String)otpChannel[sms,email,all]
		 * [O](Boolean)waitDR[Def=True][True,False]
		 * [O](Int)otpDigit[4-12][Def=4] [O](Int)refDigit[4-12][Def=4]
		 * 
		 * **/

		/* Validate Attribute */
		String mandatoryPath = null;
		String[] possibleValue = null;

		if (val.equals("") || val.equals("{}"))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		
		/** msisdn **/
		mandatoryPath = "msisdn";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		/** service **/
		mandatoryPath = "service";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		/** accountType **/
		mandatoryPath = "accountType";
		if (!val.contains(mandatoryPath))
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_MISSING);

		/** msisdn **/
		mandatoryPath = "msisdn";
		int msisdnDigitLength = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MSISDN_DIGIT_LENGTH));
		String[] countryCodeList = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DOMESTIC_COUNTRY_CODE_LIST));
		
		if (sendWSOTPRequest.getMsisdn() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_MISSING);

		else if (sendWSOTPRequest.getMsisdn().isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_NULL);

		else if (sendWSOTPRequest.getMsisdn().length() < msisdnDigitLength)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);

		String msisdn = sendWSOTPRequest.getMsisdn();
		boolean foundCountry = false;
		try {
			for (String countryCode : countryCodeList) {
				String msisdnPrefix = msisdn.substring(0, countryCode.length());
				
				if (msisdnPrefix.contains(countryCode)) {
					String realMsisdn = msisdn.substring(countryCode.length(), msisdn.length());
					String msisdnPattern = "[0-9]+";
					typePattern = Pattern.compile(msisdnPattern);
					typeMatcher = typePattern.matcher(realMsisdn);
					if (typeMatcher.matches() && realMsisdn.length() == msisdnDigitLength) {
						foundCountry = true;
						break;
					}
					else {
						throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
					}
				}
			}
		}
		catch (Exception e) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}
		if (!foundCountry) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_MSISDN_FORMAT, VerifyMessageType.IS_INVALID);
		}

		/** service **/
		mandatoryPath = "service";
		if (sendWSOTPRequest.getService() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_MISSING);

		if (sendWSOTPRequest.getService().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_NULL);
		}

		if (sendWSOTPRequest.getService().length() > 60) {
			throw new ValidationException(mandatoryPath, JsonResultCode.SERVICE_VAL_EMPTY, VerifyMessageType.IS_INVALID);
		}

		/** accountType **/
		mandatoryPath = "accountType";
		possibleValue = new String[] { "PREPAID", "POSTPAID", "ALL" };
		if (sendWSOTPRequest.getAccountType() == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_MISSING);

		if (sendWSOTPRequest.getAccountType().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_NULL);
		}

		if (!Arrays.asList(possibleValue).contains(sendWSOTPRequest.getAccountType().toUpperCase())) {
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_ACCOUNT_TYPE, VerifyMessageType.IS_INVALID);
		}
	}
	
	public static void gssoRefundValidator(Refund refundRes, Refund refundReq) throws ValidationException {

		/* Validate Attribute */
		String mandatoryPath = null;

		/** command **/
		mandatoryPath = "command";
		if (refundRes.getCommand() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (refundRes.getCommand().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_NULL);
		}
		if(!refundReq.getCommand().equals(refundRes.getCommand())){
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_INVALID);
		}

		/** tid **/
		mandatoryPath = "tid";
		if (refundRes.getTid() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (refundRes.getTid().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_NULL);
		}
		if(!refundReq.getTid().equals(refundRes.getTid())){
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_INVALID);
		}

		/** status **/
		mandatoryPath = "status";
		if (refundRes.getStatus() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (refundRes.getStatus().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_NULL);
		}

		/** devMessage **/
		mandatoryPath = "devMessage";
		if (refundRes.getDevMessage() == null) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_MISSING);
		}
		if (refundRes.getDevMessage().isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_TRANSACTION_ID_FORMAT, VerifyMessageType.IS_NULL);
		}
	}

}
