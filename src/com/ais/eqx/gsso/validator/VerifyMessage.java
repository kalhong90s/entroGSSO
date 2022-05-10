package com.ais.eqx.gsso.validator;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.E01ResultCode;
import com.ais.eqx.gsso.enums.EventMethod;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.exception.ValidationException;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.GssoAuthOTPRequest;
import com.ais.eqx.gsso.instances.GssoConfirmOTPRequest;
import com.ais.eqx.gsso.instances.GssoGenPasskeyRequest;
import com.ais.eqx.gsso.instances.GssoOTPRequest;
import com.ais.eqx.gsso.instances.GssoWSConfirmOTPRequest;
import com.ais.eqx.gsso.instances.GssoWSConfirmOTPWithIDRequest;
import com.ais.eqx.gsso.instances.OrigInvokeProfile;
import com.ais.eqx.gsso.instances.Refund;
import com.ais.eqx.gsso.instances.SendWSOTPRequest;
import com.ais.eqx.gsso.instances.TransactionData;
import com.ais.eqx.gsso.interfaces.EQX;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.IdleMessageFormat;
import com.ais.eqx.gsso.interfaces.VerifyMessageType;
import com.ais.eqx.gsso.jaxb.InstanceContext;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.GssoDataManagement;
import com.google.gson.JsonSyntaxException;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;

public class VerifyMessage {

	/**
	 * JSON FORMAT
	 * 
	 * @param JSON
	 * 
	 */

	public static boolean isJSONValid(final String JSON_STRING) {
		try {
//			InstanceContext.getGson().fromJson(JSON_STRING, Object.class);
			InstanceContext.getGson().fromJson(StringEscapeUtils.unescapeXml(JSON_STRING), Object.class);
			return true;
		}
		catch (JsonSyntaxException ex) {
			return false;
		}
	}

	/**
	 * VERIFY IDLE_OTP
	 **/
	public static void verifyIDLE_OTP_Req(final EquinoxRawData rawDatas, final APPInstance appInstance) throws ValidationException {

		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String isComment = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML, EventCtype.PLAIN };
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID
					+ isComment);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		mandatoryPath = "maxTransaction";
		isComment = "Exception";
		int totalTransactionInGsso = 0;
		int ec02MaxTransaction = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MAX_TRANSACTION));
		/** TRANSACTION SIZE **/
		if (appInstance.getTransactionidData() != null) {
			for (Entry<String, TransactionData> entry : appInstance.getTransactionidData().entrySet()) {
				if (entry.getValue().isActive()) {
					totalTransactionInGsso++;
				}
			}
		}
		
		/** OTP REQ SIZE **/
		for (Entry<String, OrigInvokeProfile> entry : appInstance.getMapOrigProfile().entrySet()) {
			EquinoxRawData origRawData = entry.getValue().getOrigEquinoxRawData();
			String transactionID;
			try {
				transactionID = entry.getValue().getTransactionID();
			}
			catch (Exception e) {
				transactionID = "";
			}
			/** TEXT/XML **/
			if (origRawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
				try {
					String messageXML = origRawData.getRawDataMessage();
					/** IDLE_SEND_OTP_REQ **/
					if (messageXML.contains(IdleMessageFormat.SOAP_SEND_OTP_REQ)) {

						if (transactionID == null || transactionID.isEmpty()) {
							totalTransactionInGsso++;
						}
						else {
							try {
								/* NOT MATCH COUNT + 1 */
								if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
									totalTransactionInGsso++;
								}
							}
							catch (Exception e) {
								totalTransactionInGsso++;
							}
						}

					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
				}
			}
			/** TEXT/PLAIN **/
			else if (origRawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {
				Pattern typePattern = null;
				Matcher typeMatcher = null;
				String urlSendOTPREQPattern = IdleMessageFormat.URL_SEND_OTP_REQ_PATTERN;
				String urlAuthOTPPattern = IdleMessageFormat.URL_AUTH_OTP_PATTERN;
				try {
					String url = origRawData.getRawDataAttribute(EquinoxAttribute.URL);
					/** IDLE_SEND_OTP_REQ **/
					typePattern = Pattern.compile(urlSendOTPREQPattern);
					typeMatcher = typePattern.matcher(url);
					if (typeMatcher.matches()) {

						if (transactionID == null || transactionID.isEmpty()) {
							totalTransactionInGsso++;
						}
						else {
							try {
								/* NOT MATCH COUNT + 1 */
								if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
									totalTransactionInGsso++;
								}
							}
							catch (Exception e) {
								totalTransactionInGsso++;
							}
						}

					}
					/** IDLE_AUTH_OTP **/
					typePattern = Pattern.compile(urlAuthOTPPattern);
					typeMatcher = typePattern.matcher(url);
					if (typeMatcher.matches()) {

						if (transactionID == null || transactionID.isEmpty()) {
							totalTransactionInGsso++;
						}
						else {
							try {
								/* NOT MATCH COUNT + 1 */
								if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
									totalTransactionInGsso++;
								}
							}
							catch (Exception e) {
								totalTransactionInGsso++;
							}
						}

					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
				}
			}
		}

		isComment = "Over limit";
		if (totalTransactionInGsso >= ec02MaxTransaction) {
			throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
		}

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {
			String val = "";

			
			mandatoryPath = "sendOneTimePW";
			if (cType.equalsIgnoreCase(EventCtype.PLAIN)) {
				val = rawDatas.getRawDataAttribute(EquinoxAttribute.VAL);

				if (!val.contains("\"sendOneTimePW\"")) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}

			}
			else if (cType.equalsIgnoreCase(EventCtype.XML)) {
				val = rawDatas.getRawDataMessage();
				
				if (!val.contains(IdleMessageFormat.SOAP_SEND_OTP_REQ)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}
			
			val = rawDatas.getRawDataMessage();
			if(val.contains("\"sendOneTimePW\"")||val.contains(IdleMessageFormat.SOAP_SEND_OTP_REQ)){
				/** JSON && XML **/
				GssoOTPRequest otpRequest = GssoDataManagement.extractGssoOTPRequest(rawDatas);
				InstanceValidator.gssoOTPValidator(val, otpRequest, appInstance);
			}
			else{
				throw new ValidationException("RootElement", JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}

		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}
		
	}

	/**
	 * VERIFY IDLE_AUTH_OTP
	 **/
	public static void verifyIDLE_AUTH_OTP_Req(final EquinoxRawData rawDatas, final APPInstance appInstance)
			throws ValidationException {
		// Rawdata can check type of massage "SOAP or JSON here and verify"
		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String val = "";
		String isCommend = "";
		
		if(rawDatas.getCType().equals(EventCtype.XML)){
//			System.err.println("Success SOPE");
		}
		
		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.PLAIN };
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		mandatoryPath = "maxTransaction";
		isCommend = "Exception";
		int totalTransactionInGsso = 0;
		int ec02MaxTransaction = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MAX_TRANSACTION));
		/** TRANSACTION SIZE **/
		if (appInstance.getTransactionidData() != null) {
			for (Entry<String, TransactionData> entry : appInstance.getTransactionidData().entrySet()) {
				if (entry.getValue().isActive()) {
					totalTransactionInGsso++;
				}
			}
		}
		/** OTP REQ SIZE **/
		for (Entry<String, OrigInvokeProfile> entry : appInstance.getMapOrigProfile().entrySet()) {
			EquinoxRawData origRawData = entry.getValue().getOrigEquinoxRawData();
			String transactionID;
			try {
				transactionID = entry.getValue().getTransactionID();
			}
			catch (Exception e) {
				transactionID = "";
			}
			/** TEXT/XML **/
			if (origRawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
				try {
					String messageXML = origRawData.getRawDataMessage();
					/** IDLE_SEND_OTP_REQ **/
					if (messageXML.contains(IdleMessageFormat.SOAP_SEND_OTP_REQ)) {

						if (transactionID == null || transactionID.isEmpty()) {
							totalTransactionInGsso++;
						}
						else {
							try {
								/* NOT MATCH COUNT + 1 */
								if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
									totalTransactionInGsso++;
								}
							}
							catch (Exception e) {
								totalTransactionInGsso++;
							}
						}

					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isCommend);
				}
			}
			/** TEXT/PLAIN **/
			else if (origRawData.getCType().equalsIgnoreCase(EventCtype.PLAIN)) {
				Pattern typePattern = null;
				Matcher typeMatcher = null;
				String urlSendOTPREQPattern = IdleMessageFormat.URL_SEND_OTP_REQ_PATTERN;
				String urlAuthOTPPattern = IdleMessageFormat.URL_AUTH_OTP_PATTERN;
				try {
					String url = origRawData.getRawDataAttribute(EquinoxAttribute.URL);
					/** IDLE_SEND_OTP_REQ **/
					typePattern = Pattern.compile(urlSendOTPREQPattern);
					typeMatcher = typePattern.matcher(url);
					if (typeMatcher.matches()) {

						if (transactionID == null || transactionID.isEmpty()) {
							totalTransactionInGsso++;
						}
						else {
							try {
								/* NOT MATCH COUNT + 1 */
								if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
									totalTransactionInGsso++;
								}
							}
							catch (Exception e) {
								totalTransactionInGsso++;
							}
						}

					}
					/** IDLE_AUTH_OTP **/
					typePattern = Pattern.compile(urlAuthOTPPattern);
					typeMatcher = typePattern.matcher(url);
					if (typeMatcher.matches()) {

						if (transactionID == null || transactionID.isEmpty()) {
							totalTransactionInGsso++;
						}
						else {
							try {
								/* NOT MATCH COUNT + 1 */
								if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
									totalTransactionInGsso++;
								}
							}
							catch (Exception e) {
								totalTransactionInGsso++;
							}
						}

					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isCommend);
				}
			}
		}

		isCommend = "Over limit";
		if (totalTransactionInGsso >= ec02MaxTransaction) {
			throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isCommend);
		}

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {

			val = rawDatas.getRawDataAttribute(EquinoxAttribute.VAL);

			mandatoryPath = "authenOnetimePassword";
			if (!val.contains("\"authenOnetimePassword\"")) {
				throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}

			/** JSON && XML **/
			GssoAuthOTPRequest authotpRequest = GssoDataManagement.extractGssoAuthOTPRequest(rawDatas);
			InstanceValidator.gssoAuthOTPValidator(val, authotpRequest);
		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}
	}

	/**
	 * VERIFY IDLE_CONFIRM_OTP
	 **/
	public static void verifyIDLE_CONFIRM_OTP_Req(final EquinoxRawData rawDatas, final APPInstance appInstance)
			throws ValidationException {

		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String val = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML, EventCtype.PLAIN };
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {

			if (cType.equalsIgnoreCase(EventCtype.PLAIN)) {
				val = rawDatas.getRawDataAttribute(EquinoxAttribute.VAL);

				mandatoryPath = "confirmOneTimePW";
				if (!val.contains("\"confirmOneTimePW\"")) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}
			else if (cType.equalsIgnoreCase(EventCtype.XML)) {
				val = rawDatas.getRawDataMessage();

				mandatoryPath = "confirmOneTimePW";
				if (!val.contains(IdleMessageFormat.SOAP_CONFIRM_OTP)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}

			/** JSON && XML **/
			GssoConfirmOTPRequest confirmOTPReq = GssoDataManagement.extractGssoConfirmOTPRequest(rawDatas);
			/*
			 * Set transactionID
			 */
			appInstance.getMapOrigInvokeTransactionID().put(rawDatas.getInvoke(), confirmOTPReq.getConfirmOneTimePW().getTransactionID());
			InstanceValidator.gssoConfirmOTPValidator(val, confirmOTPReq);

			/** OTHER **/

		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}

	}

	/**
	 * VERIFY IDLE_GENERATE_PK
	 **/
	public static void verifyIDLE_GENERATE_PK_Req(final EquinoxRawData rawDatas, final APPInstance appInstance)
			throws ValidationException {

		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String val = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML, EventCtype.PLAIN };
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {

			if (cType.equalsIgnoreCase(EventCtype.PLAIN)) {
				val = rawDatas.getRawDataAttribute(EquinoxAttribute.VAL);

				mandatoryPath = "generatePasskey";
				if (!val.contains("\"generatePasskey\"")) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}
			else if (cType.equalsIgnoreCase(EventCtype.XML)) {
				val = rawDatas.getRawDataMessage();

				mandatoryPath = "generatePasskey";
				if (!val.contains(IdleMessageFormat.SOAP_GENERATE_PK)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}

			/** JSON && XML **/
			GssoGenPasskeyRequest genPasskeyReq = GssoDataManagement.extractGssoGenPasskeyRequest(rawDatas);
			/*
			 * Set transactionID
			 */
//			appInstance.getMapOrigInvokeTransactionID().put(rawDatas.getInvoke(), genPasskeyReq.getGeneratePasskey().getTransactionID());
			InstanceValidator.gssoGenPasskeyValidator(val, genPasskeyReq , appInstance); 

		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}

	}

	/**
	 * VERIFY IDLE_CONFIRM_OTP_W_PK
	 **/
	public static void verifyIDLE_CONFIRM_OTP_W_PK_Req(final EquinoxRawData rawDatas, final APPInstance appInstance)
			throws ValidationException {

		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String val = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML, EventCtype.PLAIN };
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {

			if (cType.equalsIgnoreCase(EventCtype.PLAIN)) {
				val = rawDatas.getRawDataAttribute(EquinoxAttribute.VAL);

				mandatoryPath = "confirmOneTimePW_PassKey";
				if (!val.contains("\"confirmOneTimePW_PassKey\"")) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}
			else if (cType.equalsIgnoreCase(EventCtype.XML)) {
				val = rawDatas.getRawDataMessage();

				mandatoryPath = "confirmOneTimePW_PassKeyResponse";
				if (!val.contains(IdleMessageFormat.SOAP_CONFIRM_OTP_W_PK)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}

			/** JSON && XML **/
			GssoConfirmOTPRequest confirmOTPReq = GssoDataManagement.extractGssoConfirmOTPRequest(rawDatas);
			/*
			 * Set transactionID
			 */
			appInstance.getMapOrigInvokeTransactionID().put(rawDatas.getInvoke(), confirmOTPReq.getConfirmOneTimePasswordWithPasskey().getTransactionID());
			InstanceValidator.gssoConfirmOTPWPasskeyValidator(val, confirmOTPReq);
		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}

	}
	/**
	 * VERIFY IDLE_WS_CONFIRM_OTP
	 **/
	public static void verifyIDLE_WS_CONFIRM_OTP_Req(final EquinoxRawData rawDatas, final APPInstance appInstance)
			throws ValidationException {

		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String val = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML};
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {

			if (cType.equalsIgnoreCase(EventCtype.XML)) {
				val = rawDatas.getRawDataMessage();

				mandatoryPath = "confirmOneTimePW";
				if (!val.contains(IdleMessageFormat.SOAP_WS_CONFIRM_OTP_REQ)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}

			/** JSON && XML **/
			GssoWSConfirmOTPRequest wsconfirmOTPReq = GssoDataManagement.extractGssoWSConfirmOTPRequest(rawDatas);
			InstanceValidator.gssoWSConfirmOTPValidator(val, wsconfirmOTPReq);

			/** OTHER **/

		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}

	}
	
	
	
	/**
	 * VERIFY IDLE_WS_CONFIRM_OTP_ID
	 **/
	public static void verifyIDLE_WS_CONFIRM_OTP_ID_Req(final EquinoxRawData rawDatas, final APPInstance appInstance)
			throws ValidationException {
		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String val = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML};
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {

			if (cType.equalsIgnoreCase(EventCtype.XML)) {
				val = rawDatas.getRawDataMessage();

				mandatoryPath = "confirmOneTimePWwithID";
				if (!val.contains(IdleMessageFormat.SOAP_WS_CONFIRM_OTP_ID_REQ)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}

			/** JSON && XML **/
			GssoWSConfirmOTPWithIDRequest wsConfirmOTPWithIDRequest = GssoDataManagement.extractGssoWSConfirmOTPWithIDRequest(rawDatas);
			InstanceValidator.gssoWSConfirmOTPWithIDValidator(val, wsConfirmOTPWithIDRequest);

			/** OTHER **/

		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}

	}
	
	
	

	/**
	 * VERIFY W_SERVICE_TEMPLATE
	 **/
	@SuppressWarnings("unused")
	public static void verifyE01ServiceTemplate(final AbstractAF abstractAF, final APPInstance appInstance,
			final EquinoxRawData equinoxRawData, final String origInvoke) throws ValidationException {
		
		String mandatoryPath = null;
		String isCommend = "";

		/** CONDITION FOR EXTRACT MESSAGE SOAP OR JSON **/
		EquinoxRawData origRawData = appInstance.getMapOrigProfile().get(origInvoke).getOrigEquinoxRawData();
		
		/** GET VALUE FROM GLOBAL DATA **/
		String resultCode = abstractAF.getEquinoxUtils().getGlobalData().getDataResult().get(equinoxRawData.getInvoke()).getResultCode();

		/** RESULT CODE **/
		mandatoryPath = "resultCode";
		if (resultCode == null || resultCode.isEmpty()) {
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_NULL);
		}
		else if (resultCode.equals(E01ResultCode.NO_SUCH_OBJECT.getCode())) {
			isCommend = " (No such object)";
			throw new ValidationException(mandatoryPath, JsonResultCode.UNKNOWN_SERVICE, VerifyMessageType.IS_INVALID + isCommend);
		}
		else if (!resultCode.equals(E01ResultCode.SUCCESS.getCode())) {
			throw new ValidationException(mandatoryPath, JsonResultCode.E01_ERROR, VerifyMessageType.IS_INVALID + isCommend);
		}


//		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvoke);
		
//		GssoOTPRequest otpRequest = GssoDataManagement.extractGssoOTPRequest(origRawData);
		
		InstanceValidator.e01ServiceTemplateValidator(abstractAF, equinoxRawData.getInvoke(), appInstance);
		
		
	}

	/**
	 * VERIFY IDLE_OTP
	 **/
	public static void verifyIDLE_WS_AUTHEN_OTP_Req(final EquinoxRawData rawDatas, final APPInstance appInstance) throws ValidationException {

		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String isComment = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML };
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID
					+ isComment);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		mandatoryPath = "maxTransaction";
		isComment = "Exception";
		int totalTransactionInGsso = 0;
		int ec02MaxTransaction = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MAX_TRANSACTION));
		/** TRANSACTION SIZE **/
		if (appInstance.getTransactionidData() != null) {
			for (Entry<String, TransactionData> entry : appInstance.getTransactionidData().entrySet()) {
				if (entry.getValue().isActive()) {
					totalTransactionInGsso++;
				}
			}
		}
		
		/** OTP REQ SIZE **/
		for (Entry<String, OrigInvokeProfile> entry : appInstance.getMapOrigProfile().entrySet()) {
			EquinoxRawData origRawData = entry.getValue().getOrigEquinoxRawData();
			String transactionID;
			try {
				transactionID = entry.getValue().getTransactionID();
			}
			catch (Exception e) {
				transactionID = "";
			}
			/** TEXT/XML **/
			if (origRawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
				try {
					String messageXML = origRawData.getRawDataMessage();
					/** IDLE_SEND_OTP_REQ **/
					if (messageXML.contains(IdleMessageFormat.SOAP_WS_AUTH_OTP_REQ)) {

						if (transactionID == null || transactionID.isEmpty()) {
							totalTransactionInGsso++;
						}
						else {
							try {
								/* NOT MATCH COUNT + 1 */
								if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
									totalTransactionInGsso++;
								}
							}
							catch (Exception e) {
								totalTransactionInGsso++;
							}
						}

					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
				}
			}
			
		}

		isComment = "Over limit";
		if (totalTransactionInGsso >= ec02MaxTransaction) {
			throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
		}

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {
			String val = "";
			
			mandatoryPath = "authenOneTimePW";
			if (cType.equalsIgnoreCase(EventCtype.XML)) {
				val = rawDatas.getRawDataMessage();
				
				if (!val.contains(IdleMessageFormat.SOAP_WS_AUTH_OTP_REQ)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}
			
			val = rawDatas.getRawDataMessage();
			if(val.contains(IdleMessageFormat.SOAP_WS_AUTH_OTP_REQ)){
				/** JSON && XML **/
				SendWSOTPRequest sendWSOTPRequest = GssoDataManagement.extractGssoWSAuthOTPRequest(rawDatas);
				InstanceValidator.gssoWSAuthenOTPValidator(val, sendWSOTPRequest, appInstance);
			}
			else{
				throw new ValidationException("RootElement", JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			
		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}
		
	}
	
	public static void verifyIDLE_WS_AUTHEN_OTP_ID_Req(final EquinoxRawData rawDatas, final APPInstance appInstance) throws ValidationException {

		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String isComment = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML };
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID
					+ isComment);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		mandatoryPath = "maxTransaction";
		isComment = "Exception";
		int totalTransactionInGsso = 0;
		int ec02MaxTransaction = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MAX_TRANSACTION));
		/** TRANSACTION SIZE **/
		if (appInstance.getTransactionidData() != null) {
			for (Entry<String, TransactionData> entry : appInstance.getTransactionidData().entrySet()) {
				if (entry.getValue().isActive()) {
					totalTransactionInGsso++;
				}
			}
		}
		
		/** OTP REQ SIZE **/
		for (Entry<String, OrigInvokeProfile> entry : appInstance.getMapOrigProfile().entrySet()) {
			EquinoxRawData origRawData = entry.getValue().getOrigEquinoxRawData();
			String transactionID;
			try {
				transactionID = entry.getValue().getTransactionID();
			}
			catch (Exception e) {
				transactionID = "";
			}
			/** TEXT/XML **/
			if (origRawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
				try {
					String messageXML = origRawData.getRawDataMessage();
					/** IDLE_WS_AUTH_OTP_ID_REQ **/
					if (messageXML.contains(IdleMessageFormat.SOAP_WS_AUTH_OTP_ID_REQ)) {

						if (transactionID == null || transactionID.isEmpty()) {
							totalTransactionInGsso++;
						}
						else {
							try {
								/* NOT MATCH COUNT + 1 */
								if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
									totalTransactionInGsso++;
								}
							}
							catch (Exception e) {
								totalTransactionInGsso++;
							}
						}

					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
				}
			}
			
		}

		isComment = "Over limit";
		if (totalTransactionInGsso >= ec02MaxTransaction) {
			throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
		}

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {
			String val = "";

			mandatoryPath = "authenOneTimePWwithID";
			if (cType.equalsIgnoreCase(EventCtype.XML)) {
				val = rawDatas.getRawDataMessage();
				
				if (!val.contains(IdleMessageFormat.SOAP_WS_AUTH_OTP_ID_REQ)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}
			
			val = rawDatas.getRawDataMessage();
			if(val.contains(IdleMessageFormat.SOAP_WS_AUTH_OTP_ID_REQ)){
				/** JSON && XML **/
				SendWSOTPRequest sendWSOTPRequest = GssoDataManagement.extractGssoWSAuthOTPWithIDRequest(rawDatas);
				InstanceValidator.gssoWSAuthenOTPWithIDValidator(val, sendWSOTPRequest, appInstance);
			}
			else{
				throw new ValidationException("RootElement", JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			
		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}
		
	}
	
	public static void verifyIDLE_WS_GENERATE_OTP_Req(final EquinoxRawData rawDatas, final APPInstance appInstance) throws ValidationException {

		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String isComment = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML };
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID
					+ isComment);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		mandatoryPath = "maxTransaction";
		isComment = "Exception";
		int totalTransactionInGsso = 0;
		int ec02MaxTransaction = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MAX_TRANSACTION));
		/** TRANSACTION SIZE **/
		if (appInstance.getTransactionidData() != null) {
			for (Entry<String, TransactionData> entry : appInstance.getTransactionidData().entrySet()) {
				if (entry.getValue().isActive()) {
					totalTransactionInGsso++;
				}
			}
		}
		
		/** OTP REQ SIZE **/
		for (Entry<String, OrigInvokeProfile> entry : appInstance.getMapOrigProfile().entrySet()) {
			EquinoxRawData origRawData = entry.getValue().getOrigEquinoxRawData();
			String transactionID;
			try {
				transactionID = entry.getValue().getTransactionID();
			}
			catch (Exception e) {
				transactionID = "";
			}
			/** TEXT/XML **/
			if (origRawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
				try {
					String messageXML = origRawData.getRawDataMessage();
					/** IDLE_WS_GENERATE_OTP_REQ **/
					if (messageXML.contains(IdleMessageFormat.SOAP_WS_GENERATE_OTP_REQ)) {

						if (transactionID == null || transactionID.isEmpty()) {
							totalTransactionInGsso++;
						}
						else {
							try {
								/* NOT MATCH COUNT + 1 */
								if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
									totalTransactionInGsso++;
								}
							}
							catch (Exception e) {
								totalTransactionInGsso++;
							}
						}

					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
				}
			}
			
		}

		isComment = "Over limit";
		if (totalTransactionInGsso >= ec02MaxTransaction) {
			throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
		}

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {
			String val = "";

			mandatoryPath = "generateOneTimePW";
			if (cType.equalsIgnoreCase(EventCtype.XML)) {
				val = rawDatas.getRawDataMessage();
				
				if (!val.contains(IdleMessageFormat.SOAP_WS_GENERATE_OTP_REQ)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}
			
			
			val = rawDatas.getRawDataMessage();
			if(val.contains(IdleMessageFormat.SOAP_WS_GENERATE_OTP_REQ)){
				/** JSON && XML **/
				SendWSOTPRequest sendWSOTPRequest = GssoDataManagement.extractGssoWSGenerateOTPRequest(rawDatas);
				InstanceValidator.gssoWSGenerateOTPValidator(val, sendWSOTPRequest, appInstance);
			}
			else{
				throw new ValidationException("RootElement", JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			
		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}
		
	}
	

	public static void verifyIDLE_WS_CREATE_OTP_Req(final EquinoxRawData rawDatas, final APPInstance appInstance) throws ValidationException {

		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String method = rawDatas.getRawDataAttribute("method");
		String isComment = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML };
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID
					+ isComment);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.REQUEST };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		mandatoryPath = "maxTransaction";
		isComment = "Exception";
		int totalTransactionInGsso = 0;
		int ec02MaxTransaction = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.MAX_TRANSACTION));
		/** TRANSACTION SIZE **/
		if (appInstance.getTransactionidData() != null) {
			for (Entry<String, TransactionData> entry : appInstance.getTransactionidData().entrySet()) {
				if (entry.getValue().isActive()) {
					totalTransactionInGsso++;
				}
			}
		}
		
		/** OTP REQ SIZE **/
		for (Entry<String, OrigInvokeProfile> entry : appInstance.getMapOrigProfile().entrySet()) {
			EquinoxRawData origRawData = entry.getValue().getOrigEquinoxRawData();
			String transactionID;
			try {
				transactionID = entry.getValue().getTransactionID();
			}
			catch (Exception e) {
				transactionID = "";
			}
			/** TEXT/XML **/
			if (origRawData.getCType().equalsIgnoreCase(EventCtype.XML)) {
				try {
					String messageXML = origRawData.getRawDataMessage();
					/** IDLE_WS_CREATE_OTP_REQ **/
					if (messageXML.contains(IdleMessageFormat.SOAP_WS_CREATE_OTP_REQ)) {

						if (transactionID == null || transactionID.isEmpty()) {
							totalTransactionInGsso++;
						}
						else {
							try {
								/* NOT MATCH COUNT + 1 */
								if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
									totalTransactionInGsso++;
								}
							}
							catch (Exception e) {
								totalTransactionInGsso++;
							}
						}

					}
				}
				catch (Exception e) {
					throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
				}
			}
			
		}

		isComment = "Over limit";
		if (totalTransactionInGsso >= ec02MaxTransaction) {
			throw new ValidationException(mandatoryPath, JsonResultCode.MAXIMUM_AUTHEN_TRANSACTION, isComment);
		}

		/** METHOD **/
		mandatoryPath = "method";
		/** POST **/
		if (method.equals(EventMethod.POST.getMethod())) {
			String val = "";

			mandatoryPath = "createOneTimePW";
			if (cType.equalsIgnoreCase(EventCtype.XML)) {
				val = rawDatas.getRawDataMessage();
				
				if (!val.contains(IdleMessageFormat.SOAP_WS_CREATE_OTP_REQ)) {
					throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
				}
			}
			
			val = rawDatas.getRawDataMessage();
			if(val.contains(IdleMessageFormat.SOAP_WS_CREATE_OTP_REQ)){
				/** JSON && XML **/
				SendWSOTPRequest sendWSOTPRequest = GssoDataManagement.extractGssoWSCreateOTPRequest(rawDatas);
				InstanceValidator.gssoWSCreateOTPValidator(val, sendWSOTPRequest, appInstance);
			}
			else{
				throw new ValidationException("RootElement", JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_MISSING);
			}
			
		}
		/** OTHER METHOD **/
		else {
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}
		
	}
	/**
	 * VERIFY REFUND
	 **/
	public static void verifyRefundRes(final EquinoxRawData rawDatas, final APPInstance appInstance, final String invoke)
			throws ValidationException {

		String mandatoryPath = null;
		String[] possibleValue = null;

		/** GET VALUE FROM EQUINOX RAWDATA **/
		String type = rawDatas.getType();
		String cType = rawDatas.getCType();
		String val = "";

		/** CTYPE **/
		mandatoryPath = "ctype";
		possibleValue = new String[] { EventCtype.XML, EventCtype.PLAIN};
		if (cType == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (cType.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(cType.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** TYPE **/
		mandatoryPath = "type";
		possibleValue = new String[] { EventAction.RESPONSE };
		if (type == null)
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (type.isEmpty())
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_NULL);

		else if (!Arrays.asList(possibleValue).contains(type.toLowerCase()))
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);

		/** DATA REFUND RESPONSE **/
		mandatoryPath = "data refund response";
		val = rawDatas.getRawDataAttribute(EQX.Attribute.VAL);
		Refund refundRes = null;
		Refund refundReq = appInstance.getMapInvokeOfRefund().get(invoke);
		
		if(StringUtils.isNotEmpty(val)){
			refundRes = GssoDataManagement.extractGssoRefund(val);
			InstanceValidator.gssoRefundValidator(refundRes, refundReq);
		}
		else{
			throw new ValidationException(mandatoryPath, JsonResultCode.WRONG_INPUT_PARAMETER, VerifyMessageType.IS_INVALID);
		}

	}
	
}
