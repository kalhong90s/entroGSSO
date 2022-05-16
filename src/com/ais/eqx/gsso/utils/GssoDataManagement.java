package com.ais.eqx.gsso.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.EventLog;
import com.ais.eqx.gsso.enums.GssoCommand;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.enums.JsonResultCode;
import com.ais.eqx.gsso.enums.Statistic;
import com.ais.eqx.gsso.enums.SubStates;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.instances.GenPasskey;
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
import com.ais.eqx.gsso.instances.SendConfirmOTPRequest;
import com.ais.eqx.gsso.instances.SendOneTimePWRequest;
import com.ais.eqx.gsso.instances.SendWSConfirmOTPRequest;
import com.ais.eqx.gsso.instances.SendWSConfirmOTPWithIDRequest;
import com.ais.eqx.gsso.instances.SendWSOTPRequest;
import com.ais.eqx.gsso.instances.TransactionData;
import com.ais.eqx.gsso.interfaces.AccountType;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.GssoMessageType;
import com.ais.eqx.gsso.interfaces.IdleMessageFormat;
import com.ais.eqx.gsso.interfaces.OperName;
import com.ais.eqx.gsso.jaxb.InstanceContext;
import com.ais.eqx.gsso.jaxb.JAXBHandler;
import com.ais.eqx.gsso.parser.MessageParser;

import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;


public class GssoDataManagement {

	public static int indexOfStartWord(Pattern pattern, String message) {
		Matcher matcher = pattern.matcher(message);
		return matcher.find() ? matcher.start() : -1;
	}

	public static int indexOfEndWord(Pattern pattern, String message) {
		Matcher matcher = pattern.matcher(message);
		return matcher.find() ? matcher.end() : -1;
	}

	/** EXTRACT GSSO OTP REQUEST **/
	public static GssoOTPRequest extractGssoOTPRequest(final EquinoxRawData equinoxRawData) {

		GssoOTPRequest otpRequest = new GssoOTPRequest();

		/** CHECK JSON OR SOAP **/
		/** IF SOAP **/
		if (equinoxRawData.getCType().contains(EventCtype.XML)) {
			try {
				String incomingMessage = equinoxRawData.getRawDataMessage();

				String prefix = "<SendOneTimePWRequest>";
				String subfix = "</SendOneTimePWRequest>";
				String msisdn = GssoDataManagement.findXmlValue("msisdn", incomingMessage);
				String emailAddr = GssoDataManagement.findXmlValue("emailAddr", incomingMessage);
				String otpChannel = GssoDataManagement.findXmlValue("otpChannel", incomingMessage);
				String service = GssoDataManagement.findXmlValue("service", incomingMessage);
				String accountType = GssoDataManagement.findXmlValue("accountType", incomingMessage);
				String addTimeoutMins = GssoDataManagement.findXmlValue("addTimeoutMins", incomingMessage);
				String waitDR = GssoDataManagement.findXmlValue("waitDR", incomingMessage);
				String otpDigit = GssoDataManagement.findXmlValue("otpDigit", incomingMessage);
				String refDigit = GssoDataManagement.findXmlValue("refDigit", incomingMessage);
				String sessionId = GssoDataManagement.findXmlValue("sessionId", incomingMessage);
				String refId = GssoDataManagement.findXmlValue("refId", incomingMessage);
				String state = GssoDataManagement.findXmlValue("state", incomingMessage);
				String smsLanguage = GssoDataManagement.findXmlValue("smsLanguage", incomingMessage);

				String soapIncoming = prefix + msisdn + emailAddr + otpChannel + service + accountType + addTimeoutMins + waitDR
						+ otpDigit + refDigit + sessionId + refId + state + smsLanguage + subfix;

				SendOneTimePWRequest sendOneTimePWRequest = (SendOneTimePWRequest) JAXBHandler.createInstance(
						InstanceContext.getSendOneTimePWRequestContext(), soapIncoming, SendOneTimePWRequest.class);

				otpRequest.setMessageType(GssoMessageType.SOAP);
				otpRequest.setSendOneTimePW(sendOneTimePWRequest);
			}
			catch (Exception e) {
				otpRequest = new GssoOTPRequest();
				otpRequest.setMessageType(GssoMessageType.SOAP);
				otpRequest.setSendOneTimePW(new SendOneTimePWRequest());
			}
		}
		/** IF JSON **/
		else if (equinoxRawData.getCType().contains(EventCtype.PLAIN)) {
			/** JSON TO JAVA **/
			String jsonVal = null;
			try {
				jsonVal = equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL);
			}
			catch (Exception e) {
				jsonVal = "";
			}

			try {
				otpRequest = InstanceContext.getGson().fromJson(jsonVal, GssoOTPRequest.class);
				otpRequest.setMessageType(GssoMessageType.JSON);
			}
			catch (Exception e) {
				otpRequest = new GssoOTPRequest();
				otpRequest.setMessageType(GssoMessageType.JSON);
				otpRequest.setSendOneTimePW(new SendOneTimePWRequest());
			}
		}
		return otpRequest;
	}

	/** EXTRACT GSSO AUTH OTP REQUEST **/
	public static GssoAuthOTPRequest extractGssoAuthOTPRequest(final EquinoxRawData equinoxRawData) {

		GssoAuthOTPRequest authOtpRequest = new GssoAuthOTPRequest();

		/** JSON TO JAVA **/
		String jsonVal = null;
		try {
			jsonVal = equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL);
		}
		catch (Exception e) {
			jsonVal = "";
		}

		try {
			authOtpRequest = InstanceContext.getGson().fromJson(jsonVal, GssoAuthOTPRequest.class);
		}
		catch (Exception e) {
			authOtpRequest = new GssoAuthOTPRequest();
			authOtpRequest.setMessageType(GssoMessageType.JSON);
			authOtpRequest.setAuthenOnetimePassword(new GssoAuthOTP());
		}
		return authOtpRequest;
	}

	/** EXTRACT GSSO CONFIRM OTP OR CONFIRM OTP PASSKEY REQUEST **/
	public static GssoConfirmOTPRequest extractGssoConfirmOTPRequest(final EquinoxRawData equinoxRawData) {
		GssoConfirmOTPRequest confirmOTPReq = new GssoConfirmOTPRequest();

		/** CHECK JSON OR SOAP **/
		/** IF SOAP **/
		if (equinoxRawData.getCType().contains(EventCtype.XML)) {
			try {
				String incomingMessage = equinoxRawData.getRawDataMessage();
				confirmOTPReq.setConfirmOneTimePasswordWithPasskey(new SendConfirmOTPRequest());
				confirmOTPReq.setConfirmOneTimePW(new SendConfirmOTPRequest());

				/** IDLE_CONFIRMATION_W_PK **/
				if (incomingMessage.contains(IdleMessageFormat.SOAP_CONFIRM_OTP_W_PK)) {

					String prefix = "<SendConfirmOTPRequest>";
					String subfix = "</SendConfirmOTPRequest>";
					String msisdn = GssoDataManagement.findXmlValue("msisdn", incomingMessage);
					String pwd = GssoDataManagement.findXmlValue("pwd", incomingMessage);
					String transactionID = GssoDataManagement.findXmlValue("transactionID", incomingMessage);
					String service = GssoDataManagement.findXmlValue("service", incomingMessage);
					String soapIncoming = prefix + msisdn + pwd + transactionID + service + subfix;

					SendConfirmOTPRequest sendConfirmOTPRequestWPK = (SendConfirmOTPRequest) JAXBHandler.createInstance(
							InstanceContext.getConfirmOneTimePWRequestContext(), soapIncoming, SendConfirmOTPRequest.class);

					confirmOTPReq.setConfirmOneTimePasswordWithPasskey(sendConfirmOTPRequestWPK);

				}
				/** IDLE_CONFIRMATION **/
				else if (incomingMessage.contains(IdleMessageFormat.SOAP_CONFIRM_OTP)) {

					String prefix = "<SendConfirmOTPRequest>";
					String subfix = "</SendConfirmOTPRequest>";
					String msisdn = GssoDataManagement.findXmlValue("msisdn", incomingMessage);
					String pwd = GssoDataManagement.findXmlValue("pwd", incomingMessage);
					String transactionID = GssoDataManagement.findXmlValue("transactionID", incomingMessage);
					String service = GssoDataManagement.findXmlValue("service", incomingMessage);
					String soapIncoming = prefix + msisdn + pwd + transactionID + service + subfix;

					SendConfirmOTPRequest sendConfirmOTPRequest = (SendConfirmOTPRequest) JAXBHandler.createInstance(
							InstanceContext.getConfirmOneTimePWRequestContext(), soapIncoming, SendConfirmOTPRequest.class);

					confirmOTPReq.setConfirmOneTimePW(sendConfirmOTPRequest);

				}
				else {
					confirmOTPReq = new GssoConfirmOTPRequest();
				}

				confirmOTPReq.setMessageType(GssoMessageType.SOAP);

			}
			catch (Exception e) {
				confirmOTPReq = new GssoConfirmOTPRequest();
				confirmOTPReq.setMessageType(GssoMessageType.SOAP);
				confirmOTPReq.setConfirmOneTimePW(new SendConfirmOTPRequest());
				confirmOTPReq.setConfirmOneTimePasswordWithPasskey(new SendConfirmOTPRequest());
			}
		}
		/** IF JSON **/
		else if (equinoxRawData.getCType().contains(EventCtype.PLAIN)) {
			/** JSON TO JAVA **/
			String jsonVal = null;
			try {
				jsonVal = equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL);
			}
			catch (Exception e) {
				jsonVal = "";
			}

			try {
				confirmOTPReq = InstanceContext.getGson().fromJson(jsonVal, GssoConfirmOTPRequest.class);
				confirmOTPReq.setMessageType(GssoMessageType.JSON);
			}
			catch (Exception e) {
				confirmOTPReq = new GssoConfirmOTPRequest();
				confirmOTPReq.setMessageType(GssoMessageType.JSON);
				confirmOTPReq.setConfirmOneTimePW(new SendConfirmOTPRequest());
				confirmOTPReq.setConfirmOneTimePasswordWithPasskey(new SendConfirmOTPRequest());
			}
		}

		return confirmOTPReq;
	}

	/** EXTRACT GSSO GENPASSKEY REQUEST **/
	public static GssoGenPasskeyRequest extractGssoGenPasskeyRequest(final EquinoxRawData equinoxRawData) {
		GssoGenPasskeyRequest genPasskeyReq = new GssoGenPasskeyRequest();

		/** CHECK JSON OR SOAP **/
		/** IF SOAP **/
		if (equinoxRawData.getCType().contains(EventCtype.XML)) {
			try {
				String incomingMessage = equinoxRawData.getRawDataMessage();

				String prefix = "<GenPasskeyRequest>";
				String subfix = "</GenPasskeyRequest>";
				String msisdn = GssoDataManagement.findXmlValue("msisdn", incomingMessage);
				String transactionID = GssoDataManagement.findXmlValue("transactionID", incomingMessage);
				String service = GssoDataManagement.findXmlValue("service", incomingMessage);
				String orderName = GssoDataManagement.findXmlValue("orderName", incomingMessage);
				String orderRef = GssoDataManagement.findXmlValue("orderRef", incomingMessage);
				String soapIncoming = prefix + msisdn + transactionID + service + orderName + orderRef + subfix;

				GenPasskey genPasskeyRequest = (GenPasskey) JAXBHandler.createInstance(
						InstanceContext.getGeneratePasskeyRequestContext(), soapIncoming, GenPasskey.class);

				genPasskeyReq.setGeneratePasskey(genPasskeyRequest);
				genPasskeyReq.setMessageType(GssoMessageType.SOAP);

			}
			catch (Exception e) {
				genPasskeyReq = new GssoGenPasskeyRequest();
				genPasskeyReq.setMessageType(GssoMessageType.SOAP);
				genPasskeyReq.setGeneratePasskey(new GenPasskey());
			}
		}
		/** IF JSON **/
		else if (equinoxRawData.getCType().contains(EventCtype.PLAIN)) {
			/** JSON TO JAVA **/
			String jsonVal = null;
			try {
				jsonVal = equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL);
			}
			catch (Exception e) {
				jsonVal = "";
			}

			try {
				genPasskeyReq = InstanceContext.getGson().fromJson(jsonVal, GssoGenPasskeyRequest.class);
				genPasskeyReq.setMessageType(GssoMessageType.JSON);
			}
			catch (Exception e) {
				genPasskeyReq = new GssoGenPasskeyRequest();
				genPasskeyReq.setMessageType(GssoMessageType.JSON);
				genPasskeyReq.setGeneratePasskey(new GenPasskey());
			}
		}

		return genPasskeyReq;
	}

	/** EXTRACT GSSO SERVICE TEMPLATE **/
	public static GssoE01Datas extractGssoServiceTemplate(final String datas) {
		GssoE01Datas e01Datas = new GssoE01Datas();

		/** IF JSON **/
		String serviceKey = null;
		try {
			serviceKey = datas.substring(0, datas.indexOf("[")).replaceAll("\\s+", "");
			if(serviceKey.contains("\""))
				serviceKey = serviceKey.substring(serviceKey.indexOf("\"") + 1, serviceKey.lastIndexOf("\""));
			else
				serviceKey = null;
		}
		catch (Exception e) {
		}

		/** JSON TO JAVA **/
		String jsonVal = null;
		String policy = null;
		try {
			policy = datas.substring(datas.indexOf("["), datas.length());

			jsonVal = "{\"e01datas\":" + policy + "}";
		}
		catch (Exception e) {
			jsonVal = "";
		}

		try {
			e01Datas = InstanceContext.getGson().fromJson(jsonVal, GssoE01Datas.class);
			e01Datas.setServiceKey(serviceKey);
		}
		catch (Exception e) {
			e01Datas = new GssoE01Datas();
			e01Datas.setServiceTemplate(new ArrayList<GssoServiceTemplate>());
			e01Datas.setServiceKey(serviceKey);
		}

		return e01Datas;

	}

	public static GssoServiceTemplate findServiceTemplateMatchAccountType(APPInstance appInstance, final String serviceName,
			final String oper) {
		/** CONDITION FOR EXTRACT MESSAGE SOAP OR JSON **/
		ArrayList<GssoServiceTemplate> serviceTemplates = appInstance.getMapE01dataofService().get(serviceName.toUpperCase())
				.getServiceTemplate();

		for (GssoServiceTemplate serviceTemplate : serviceTemplates) {
			if (serviceTemplate.getOper().equalsIgnoreCase(oper)) {
				return serviceTemplate;
			}
		}
		return null;
	}

	public static boolean containService(final ArrayList<String> listConfigService, final String orig) {
		boolean isFound = false;

		for (String service : listConfigService) {
			if (orig.contains(service)) {
				isFound = true;
				break;
			}
			if (service.contains(orig)) {
				isFound = true;
				break;
			}
		}
		return isFound;
	}

	public static String[] configToArray(final String configValue) {
		String[] listConfig = null;

		try {
			listConfig = configValue.split(",");
		}
		catch (Exception e) {
			Log.e(e.getMessage());
		}

		return listConfig;
	}

	public static String convertStringToHex(final String text, boolean isThai) {
		String outputString = "";

		try {
			/** OLD CONVERT HEX **/
			// outputString = Hex.encodeHexString(text.getBytes());
			/** OLE CONVERT HEX **/

			/** NEW CONVERT HEX **/
			ArrayList<String> listEncodeText = GssoTranformGMSMessage.tranformMessageSending(text, isThai);
			for (String lineText : listEncodeText) {
				outputString += lineText;
			}
			/** NEW CONVERT HEX **/

			if (outputString.length() > 2) {
				String hexPrefix = outputString.substring(0, 2);
				if (!hexPrefix.equalsIgnoreCase("0x")) {
					outputString = "0x" + outputString;
				}
			}

		}
		catch (Exception e) {
			Log.d("Wrong HEX Format");
		}

		return outputString;
	}
	public static String convertStringToHexNotPrefix(final String text, boolean isThai) {
		String outputString = "";

		try {
			/** OLD CONVERT HEX **/
			// outputString = Hex.encodeHexString(text.getBytes());
			/** OLE CONVERT HEX **/

			/** NEW CONVERT HEX **/
			ArrayList<String> listEncodeText = GssoTranformGMSMessage.tranformMessageSending(text, isThai);
			for (String lineText : listEncodeText) {
				outputString += lineText;
			}


		}
		catch (Exception e) {
			Log.d("Wrong HEX Format");
		}

		return outputString;
	}
	
	public static String convertHexToString(String hex) {
		String outputString = "";

		try {
			if (hex.length() > 2) {

				String hexPrefix = hex.substring(0, 2);
				if (hexPrefix.equalsIgnoreCase("0x")) {
					hex = hex.substring(2, hex.length());
				}

				StringBuilder output = new StringBuilder();
				for (int i = 0; i < hex.length(); i += 2) {
					try {
						String str = hex.substring(i, i + 2);
						output.append((char) Integer.parseInt(str, 16));
					}
					catch (Exception e) {
					}
				}
				outputString = output.toString();
			}
		}
		catch (Exception e) {
			Log.d("Wrong String Format");
		}

		return outputString;
	}

	public static void removeProfile(final String origInvoke, APPInstance appInstance) {

		/* REMOVE INVOKE PROCESSING */
		appInstance.getListInvokeProcessing().remove(origInvoke);

		/* REMOVE ORIG PROFILE */
		appInstance.getMapOrigProfile().remove(origInvoke);

	}

	public static void removeProfileAndTransaction(final String origInvoke, APPInstance appInstance) {

		try {
			String transactionID = appInstance.getMapOrigProfile().get(origInvoke).getTransactionID();
			if (!appInstance.getTransactionidData().get(transactionID).isActive()) {
				appInstance.getTransactionidData().remove(transactionID);
			}
		}
		catch (Exception e) {
		}

		/* REMOVE INVOKE PROCESSING */
		appInstance.getListInvokeProcessing().remove(origInvoke);

		/* REMOVE ORIG PROFILE */
		appInstance.getMapOrigProfile().remove(origInvoke);

		/* REMOVE TIMEOUT WAIT DR */
		if (appInstance.getMapTimeoutOfWaitDR().size() > 0) {
			Iterator<Entry<String, TimeoutCalculator>> iteratorWDR = appInstance.getMapTimeoutOfWaitDR().entrySet().iterator();
			while (iteratorWDR.hasNext()) {
				Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iteratorWDR.next();
				String invokeTimeoutDR = InvokeFilter.getOriginInvoke(entry.getKey());

				if (invokeTimeoutDR.equals(origInvoke)) {
					iteratorWDR.remove();
				}
			}
		}

		/* REMOVE TIMEOUT WAIT RF */
		if (appInstance.getMapTimeoutOfWaitRefund().size() > 0) {
			Iterator<Entry<String, TimeoutCalculator>> iteratorWRefund = appInstance.getMapTimeoutOfWaitRefund().entrySet().iterator();
			while (iteratorWRefund.hasNext()) {
				Entry<String, TimeoutCalculator> entry = (Entry<String, TimeoutCalculator>) iteratorWRefund.next();
				String invokeTimeoutRefund = InvokeFilter.getOriginInvoke(entry.getKey());

				if (invokeTimeoutRefund.equals(origInvoke)) {
					iteratorWRefund.remove();
				}
			}
		}

	}

	public static String createNewTransaction(APPInstance appInstance, final String origInvoke, final String seedKey) {
		String transactionID = "";
		String transactionTimeoutMin = ConfigureTool.getConfigure(ConfigName.PLUS_TRANSACTION_TIMEOUT_MINS);
		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(origInvoke);
		String cType = origInvokeProfile.getOrigEquinoxRawData().getCType();
		
		String service 		= null;
		String lifeTimeout 	= null;
		
		String otpNumber 	= null;
		String otpDigit 	= null;
		String refNumber 	= null;
		String refDigit 	= null;
		
			
		if(!origInvokeProfile.getGssoOrigCommand().equals(GssoCommand.UNKNOWN)){
			SendWSOTPRequest wsSendOTP = origInvokeProfile.getSendWSOTPRequest();
			service = wsSendOTP.getService();
			lifeTimeout = wsSendOTP.getAddTimeoutMins();
			otpDigit =  wsSendOTP.getOtpDigit();
			refDigit =  wsSendOTP.getRefDigit();
		}
		else if(origInvokeProfile.getGssoOTPRequest()!=null){
			SendOneTimePWRequest sendOneTimePW = origInvokeProfile.getGssoOTPRequest().getSendOneTimePW();
			service = sendOneTimePW.getService();
			lifeTimeout = sendOneTimePW.getLifeTimeoutMins();
			
			/* GENERATE OTP B4 SENT EMAIL OR SMS */
			otpDigit = sendOneTimePW.getOtpDigit(); 
			refDigit = sendOneTimePW.getRefDigit();
		}
		
		/* GENERATE TRANSACTION ID */
		ArrayList<String> listTransactionID = new ArrayList<String>(appInstance.getTransactionidData().keySet());
		if (cType.equalsIgnoreCase(EventCtype.PLAIN)) {
			transactionID = GssoGenerator.generateTransactionId(listTransactionID);
		}
		else {
			transactionID = GssoGenerator.generateTransactionIdNumber(listTransactionID);
		}

		otpNumber = GssoGenerator.generateNumberByLength(otpDigit, appInstance.getListOTP());

		/* GENERATE REF NUMBER */
		refNumber = GssoGenerator.generateNumberByLength(refDigit, appInstance.getListReferenceNumber());

		long realTransactionTimeout = Long.parseLong(lifeTimeout) + Long.parseLong(transactionTimeoutMin);
		long currentTimeMillis = System.currentTimeMillis();
		long transactionExpire = currentTimeMillis + (TimeUnit.MINUTES.toMillis(realTransactionTimeout));
		long otpExpire = currentTimeMillis + (TimeUnit.MINUTES.toMillis(Long.parseLong(lifeTimeout)));

		TransactionData transactionData = new TransactionData();
		transactionData.setService(service);
		transactionData.setOtp(otpNumber);
		transactionData.setRefNumber(refNumber);
		transactionData.setOtpExpireTime(otpExpire);
		transactionData.setTransactionIdExpireTime(transactionExpire);
		transactionData.setSeedKey(seedKey);
		appInstance.getTransactionidData().put(transactionID, transactionData);
		
		/*
		 * Set transactionID to instance for summary log
		 */
		appInstance.getMapOrigInvokeTransactionID().put(origInvoke, transactionID);

		return transactionID;
	}

	public static void setTimeoutOfTransaction(APPInstance appInstance, final String origInvoke) {
		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(origInvoke);
//		if(!origProfile.getGssoOrigCommand().equals(GssoCommand.WS_GENERATE_OTP)){
			String transactionID = origProfile.getTransactionID();
			long transactionTimeout = appInstance.getTransactionidData().get(transactionID).getTransactionIdExpireTime();
			appInstance.getTransactionidData().get(transactionID).setActive(true);
			TimeoutCalculator timeoutCal = TimeoutCalculator.initialTransactionToExpired(transactionTimeout);
			appInstance.getMapTimeoutOfTransactionID().put(transactionID, timeoutCal);
//		}

	}

	public static void setTimeoutOfWaitDR(APPInstance appInstance, final String origInvoke) {
		String drTimeoutMin = ConfigureTool.getConfigure(ConfigName.DR_TIMEOUT);

		// InvokeSubStates invokeSub = new InvokeSubStates(origInvoke,
		// SubStates.W_DELIVERY_REPORT.name());
		String invokeOutgoing = InvokeSubStates.getInvokeOutgoing(origInvoke, SubStates.W_DELIVERY_REPORT.name());

		TimeoutCalculator timeoutCal = TimeoutCalculator.initialTimeout(Integer.parseInt(drTimeoutMin));
		appInstance.getMapTimeoutOfWaitDR().put(invokeOutgoing, timeoutCal);
	}

	public static void chooseDefaultValues(final GssoOTPRequest otpRequest, final GssoServiceTemplate thisServiceTemplate) {
		Pattern typePatternCardinalNumbers;
		String cardinalNumbersPattern = "([0-9]+)";
		typePatternCardinalNumbers = Pattern.compile(cardinalNumbersPattern);

		SendOneTimePWRequest sendOneTimePW = otpRequest.getSendOneTimePW();
		
		sendOneTimePW.setLifeTimeoutMins(chooseLiftTimeoutMin(sendOneTimePW.getLifeTimeoutMins(),
				thisServiceTemplate.getLifeTimeoutMins()));
		
		sendOneTimePW.setWaitDR(chooseWaitDR(sendOneTimePW.getWaitDR(), thisServiceTemplate.getWaitDR()));

		sendOneTimePW.setRefDigit(chooseRefDigit(sendOneTimePW.getRefDigit(), thisServiceTemplate.getRefDigit(),
				GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.OTP_LENGTH))[0], typePatternCardinalNumbers));

		sendOneTimePW.setOtpDigit(chooseOtpDigit(sendOneTimePW.getOtpDigit(), thisServiceTemplate.getOtpDigit(),
				GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.REF_LENGTH))[0], typePatternCardinalNumbers));

	}
	
	public static void chooseDefaultValuesWSCommand(final SendWSOTPRequest sendWSOTPRequest, final GssoServiceTemplate thisServiceTemplate) {
		Pattern typePatternCardinalNumbers;
		String cardinalNumbersPattern = "([0-9]+)";
		typePatternCardinalNumbers = Pattern.compile(cardinalNumbersPattern);

		sendWSOTPRequest.setAddTimeoutMins(chooseLiftTimeoutMin(sendWSOTPRequest.getAddTimeoutMins(),
					thisServiceTemplate.getLifeTimeoutMins()));

		sendWSOTPRequest.setWaitDR(chooseWaitDR(sendWSOTPRequest.getWaitDR(), thisServiceTemplate.getWaitDR()));

		sendWSOTPRequest.setRefDigit(chooseRefDigit(sendWSOTPRequest.getRefDigit(), thisServiceTemplate.getRefDigit(),
				GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.OTP_LENGTH))[0], typePatternCardinalNumbers));

		sendWSOTPRequest.setOtpDigit(chooseOtpDigit(sendWSOTPRequest.getOtpDigit(), thisServiceTemplate.getOtpDigit(),
				GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.REF_LENGTH))[0], typePatternCardinalNumbers));

	}
	
	

	private static String chooseOtpDigit(final String incomingMessageOtpDigit, final String serviceTemplateOtpDigit,
			final String ec02OtpDigit, final Pattern typePatternCardinalNumbers) {

		return chooseParameterDigit(incomingMessageOtpDigit, serviceTemplateOtpDigit, ec02OtpDigit, typePatternCardinalNumbers);
	}

	private static String chooseRefDigit(final String incomingMessageRefDigit, final String serviceTemplateRefDigit,
			final String ec02RefDigit, final Pattern typePatternCardinalNumbers) {

		return chooseParameterDigit(incomingMessageRefDigit, serviceTemplateRefDigit, ec02RefDigit, typePatternCardinalNumbers);
	}

	private static String chooseParameterDigit(String incomingMessageRefDigit, String serviceTemplateDigit, final String ec02RefDigit,
			final Pattern typePatternCardinalNumbers) {
		Matcher typeMatcher;
		String realRefDigit;

		if (serviceTemplateDigit == null) {
			serviceTemplateDigit = "";
		}

		if (incomingMessageRefDigit == null) {
			incomingMessageRefDigit = "";
		}

		if (!incomingMessageRefDigit.isEmpty()) {
			typeMatcher = typePatternCardinalNumbers.matcher(serviceTemplateDigit);
			String refMin = (typeMatcher.matches()) ? serviceTemplateDigit : ec02RefDigit;

			typeMatcher = typePatternCardinalNumbers.matcher(incomingMessageRefDigit);
			realRefDigit = (typeMatcher.matches()) ? incomingMessageRefDigit : refMin;
		}
		else if (!serviceTemplateDigit.isEmpty()) {
			typeMatcher = typePatternCardinalNumbers.matcher(serviceTemplateDigit);
			realRefDigit = (typeMatcher.matches()) ? serviceTemplateDigit : ec02RefDigit;
		}
		else if (!ec02RefDigit.isEmpty()) {
			realRefDigit = ec02RefDigit;
		}
		else {
			realRefDigit = "4";
		}

		return realRefDigit;
	}

	private static String chooseWaitDR(String incomingMessageWaitDR, String serviceTemplateWaitDR) {
		String ec02WaitDR = ConfigureTool.getConfigureBoolean(ConfigName.WAIT_DR);
		String realWaitDR = "";

		if (serviceTemplateWaitDR == null) {
			serviceTemplateWaitDR = "";
		}
		if (incomingMessageWaitDR == null) {
			incomingMessageWaitDR = "";
		}

		if (!incomingMessageWaitDR.isEmpty()) {
			realWaitDR = (incomingMessageWaitDR.equalsIgnoreCase("True")) ? incomingMessageWaitDR : "False";
		}
		else if (!serviceTemplateWaitDR.isEmpty()) {
			realWaitDR = (serviceTemplateWaitDR.equalsIgnoreCase("True")) ? serviceTemplateWaitDR : "False";
		}
		else if (!ec02WaitDR.isEmpty()) {
			realWaitDR = (ec02WaitDR.equalsIgnoreCase("True")) ? ec02WaitDR : "False";
		}
		else {
			realWaitDR = "True";
		}

		return realWaitDR;
	}

	private static String chooseLiftTimeoutMin(final String incomingMessageLiftTimeoutMin, final String serviceTemplateLiftTimeoutMin) {
		int ec02LifeTimeoutMins = Integer.parseInt(ConfigureTool.getConfigure(ConfigName.LIFE_TIMEOUT_MINS));
		int serviceTemplateLifeTimeoutMinInteger = 0;
		int incomingMessageLifeTimeoutMinInteger = 0;
		int realLifeTimeoutMins = 0;
		/* Plz Check Number b4 */
		try {
			if (serviceTemplateLiftTimeoutMin != null) {
				serviceTemplateLifeTimeoutMinInteger = Integer.parseInt(serviceTemplateLiftTimeoutMin);
			}
		}
		catch (Exception e) {
		}
		try {
			if (incomingMessageLiftTimeoutMin != null) {
				incomingMessageLifeTimeoutMinInteger = Integer.parseInt(incomingMessageLiftTimeoutMin);
			}
		}
		catch (Exception e) {
		}

		/* FOUND 3 */
		if (ec02LifeTimeoutMins != 0 && serviceTemplateLifeTimeoutMinInteger != 0 && incomingMessageLifeTimeoutMinInteger != 0) {
			if (incomingMessageLifeTimeoutMinInteger > serviceTemplateLifeTimeoutMinInteger) {
				realLifeTimeoutMins = serviceTemplateLifeTimeoutMinInteger;
			}
			else {
				realLifeTimeoutMins = incomingMessageLifeTimeoutMinInteger;
			}
		}

		/* FOUND 2 WITHOUT ST */
		else if (ec02LifeTimeoutMins != 0 && serviceTemplateLifeTimeoutMinInteger == 0 && incomingMessageLifeTimeoutMinInteger != 0) {
			if (incomingMessageLifeTimeoutMinInteger > ec02LifeTimeoutMins) {
				realLifeTimeoutMins = ec02LifeTimeoutMins;
			}
			else {
				realLifeTimeoutMins = incomingMessageLifeTimeoutMinInteger;
			}
		}

		/* FOUND 2 WITHOUT INPUT */
		else if (ec02LifeTimeoutMins != 0 && serviceTemplateLifeTimeoutMinInteger != 0 && incomingMessageLifeTimeoutMinInteger == 0) {
			realLifeTimeoutMins = serviceTemplateLifeTimeoutMinInteger;
		}

		/* FOUND EC02 WITHOUT INPUT AND ST */
		else if (ec02LifeTimeoutMins != 0 && serviceTemplateLifeTimeoutMinInteger == 0 && incomingMessageLifeTimeoutMinInteger == 0) {
			realLifeTimeoutMins = ec02LifeTimeoutMins;
		}

		return realLifeTimeoutMins + "";
	}

	public static boolean checkPermissionAccountType(final OrigInvokeProfile origInvokeProfileIncoming, APPInstance appInstance) {

		boolean accountTypeisAllowed = false;

		if (origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())
				|| origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_SOAP.getMessageType())) {
			
			SendOneTimePWRequest sendOneTimePW = origInvokeProfileIncoming.getGssoOTPRequest().getSendOneTimePW();
			String accountType = sendOneTimePW.getAccountType();
			/** PostpaidCosLists **/
			String[] PostpaidCosLists = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.POSTPAID_COS_LISTS));
			
			GssoProfile profile = appInstance.getProfile();
			
			/** CASE INTER NUMBER **/
			if(profile.getOper().equalsIgnoreCase(OperName.INTER)){
				accountTypeisAllowed = true;
			}
			else if (accountType.equalsIgnoreCase(AccountType.PREPAID)) {
				if (profile.getOper().equalsIgnoreCase(OperName.AIS) && (!Arrays.asList(PostpaidCosLists).contains(profile.getCos()))) {
					accountTypeisAllowed = true;
				}
			}
			else if (accountType.equalsIgnoreCase(AccountType.POSTPAID)) {
				if (profile.getOper().equalsIgnoreCase(OperName.AIS) && (Arrays.asList(PostpaidCosLists).contains(profile.getCos()))) {
					accountTypeisAllowed = true;
				}
			}
			else if (accountType.equalsIgnoreCase(AccountType.AIS)) {
				if (profile.getOper().equalsIgnoreCase(OperName.AIS))
					accountTypeisAllowed = true;
			}
			else if (accountType.equalsIgnoreCase(AccountType.NON_AIS)) {
				if (profile.getOper().equalsIgnoreCase(OperName.NonAIS))
					accountTypeisAllowed = true;
			}
			else if (accountType.equalsIgnoreCase(AccountType.ALL)) {
				if (profile.getOper().equalsIgnoreCase(OperName.AIS) || profile.getOper().equalsIgnoreCase(OperName.NonAIS))
					accountTypeisAllowed = true;
			}
			
		}
		else if(origInvokeProfileIncoming.getSendWSOTPRequest()!=null){
			/** Role of msisdn **/
//			if(!origInvokeProfileIncoming.isSendUSMPSecond()){
				SendWSOTPRequest sendWSAuthOTPRequest = origInvokeProfileIncoming.getSendWSOTPRequest();
				String accountType = sendWSAuthOTPRequest.getAccountType();
				/** PostpaidCosLists **/
				String[] PostpaidCosLists = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.POSTPAID_COS_LISTS));
				
				/** PostpaidCosLists **/
				String[] DummyNumberLists = GssoDataManagement.configToArray(ConfigureTool.getConfigure(ConfigName.DUMMYNUMBER_LISTS));
				
				
				GssoProfile profile = appInstance.getProfile();
				accountTypeisAllowed = wsProfileOperCheckMatch(accountType, profile, PostpaidCosLists);
				
				if(origInvokeProfileIncoming.getGssoOrigCommand().equals(GssoCommand.WS_AUTHEN_OTP_ID)){
					/** Convert DummyNumber to UpperCase **/
					for(int i=0; i<DummyNumberLists.length; i++){
						DummyNumberLists[i]=DummyNumberLists[i].toUpperCase();
			        }
					if(!Arrays.asList(DummyNumberLists).contains(profile.getCustomerId().toUpperCase())){
						accountTypeisAllowed = false;
					}
				}
//			}
//			/** Role of OTPMobile**/
//			else{
//				accountTypeisAllowed = true;
//			}
		}
		// old code for generate
		else if (origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.GENERATE_PASSKEY_JSON.getMessageType())
				|| origInvokeProfileIncoming.getIncomingMessageType().equals(
						IncomingMessageType.GENERATE_PASSKEY_SOAP.getMessageType())) {
			accountTypeisAllowed = true;
		}

		return accountTypeisAllowed;
	}
	
	/** WS Check accountType with Oper **/
	public static boolean wsProfileOperCheckMatch(String accountType, GssoProfile profile, String[] PostpaidCosLists) {
		Boolean wsAccountTypeisAllowed = false;
		if (accountType.equalsIgnoreCase(AccountType.PREPAID)) {
			if (profile.getOper().equalsIgnoreCase(OperName.AIS) && (!Arrays.asList(PostpaidCosLists).contains(profile.getCos()))) {
				wsAccountTypeisAllowed = true;
			}
		}
		else if (accountType.equalsIgnoreCase(AccountType.POSTPAID)) {
			if (profile.getOper().equalsIgnoreCase(OperName.AIS) && (Arrays.asList(PostpaidCosLists).contains(profile.getCos()))) {
				wsAccountTypeisAllowed = true;
			}
		}else if (accountType.equalsIgnoreCase(AccountType.ALL)) {
			if (profile.getOper().equalsIgnoreCase(OperName.AIS))
				wsAccountTypeisAllowed = true;
		}

		return wsAccountTypeisAllowed;
	}
	

	public static EquinoxRawData accountTypeIsNotAllow(final String origInvoke, APPInstance appInstance,
			final EquinoxRawData rawDataIncoming, JsonResultCode jsonCode, final String logDescription, final String path,
			GssoComposeDebugLog composeDebugLog, GssoComposeSummaryLog composeSummary, final String currentSubState) {
		EquinoxRawData output = new EquinoxRawData();

		/** CREATE RES MESSAGE **/
		output = GssoConstructMessage.createReturnErrorMessageIdle(appInstance, appInstance.getProfile(), appInstance.getListOrderReference(),
				appInstance.getTimeStampIncoming(), rawDataIncoming, jsonCode, logDescription, path, composeDebugLog, composeSummary,
				currentSubState);

		/* REMOVE PROFILE */
		GssoDataManagement.removeProfile(origInvoke, appInstance);

		return output;
	}

	public static String raiseStatOutErrorForInqSubAndPortChk(final OrigInvokeProfile origInvokeProfileIncoming,
			EC02Instance ec02Instance, GssoComposeDebugLog composeDebugLog) {
		String nodeCommand = "";

		APPInstance appInstance = ec02Instance.getAppInstance();
		EquinoxRawData rawDataOrig = origInvokeProfileIncoming.getOrigEquinoxRawData();

		/** IDLE_SEND_OTP_REQ **/
		if (origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())
				|| origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_SOAP.getMessageType())) {
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
			}

			appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.SEND_OTP.getEventLog());

			nodeCommand = EventLog.SEND_OTP.getEventLog();
		}
		/** IDLE_WS_AUTHEN_OTP_REQ **/
		else if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_SOAP.getMessageType())){
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_ERROR.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_ERROR.getStatistic());
			}

			appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_AUTHEN_OTP.getEventLog());

			nodeCommand = EventLog.WS_AUTHEN_OTP.getEventLog();
		}
		/** IDLE_WS_AUTHEN_OTP_ID_REQ **/
		else if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_ID_SOAP.getMessageType())){
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_ERROR.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_ERROR.getStatistic());
			}

			appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_AUTHEN_OTP_ID.getEventLog());

			nodeCommand = EventLog.WS_AUTHEN_OTP_ID.getEventLog();
		}
		/** IDLE_WS_CREATE_OTP_REQ **/
		else if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_CREATE_OTP_SOAP.getMessageType())){
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());
			}

			appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_CREATE_OTP.getEventLog());

			nodeCommand = EventLog.WS_CREATE_OTP.getEventLog();
		}
		/** IDLE_WS_GENERATE_ONETIMEPW_REQ **/
		else if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_GENERATE_ONETIMEPW_SOAP.getMessageType())){
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_ERROR.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_ERROR.getStatistic());
			}

			appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_GENERATE_OTP.getEventLog());

			nodeCommand = EventLog.WS_GENERATE_OTP.getEventLog();
		}
		/** generatePasskey **/
		else {
			ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_ERROR.getStatistic());

			if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
				composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEPASSKEY_RESPONSE_ERROR.getStatistic());
			}

			appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.GENARATE_PASSKEY.getEventLog());

			nodeCommand = EventLog.GENARATE_PASSKEY.getEventLog();
		}

		return nodeCommand;
	}

	public static void raiseStatoutSuccessForSmsAndEmail(final OrigInvokeProfile origInvokeProfileIncoming, EC02Instance ec02Instance,
			GssoComposeDebugLog composeDebugLog) {

		APPInstance appInstance = ec02Instance.getAppInstance();
		EquinoxRawData rawDataOrig = origInvokeProfileIncoming.getOrigEquinoxRawData();

		if (rawDataOrig.getCType().equals(EventCtype.PLAIN)) {
			/** IDLE_SEND_OTP_REQ **/
			if (origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())) {
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.SEND_OTP.getEventLog());
			}

			/** IDLE_AUTH_OTP **/
			if (origInvokeProfileIncoming.getIncomingMessageType().equals(
					IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.AUTHEN_OTP.getEventLog());
			}
		}
		else {
			if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_SOAP.getMessageType())){
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_AUTHEN_OTP.getEventLog());
			}
			else if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_ID_SOAP.getMessageType())){
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_AUTHEN_OTP_ID.getEventLog());
			}
			else if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_CREATE_OTP_SOAP.getMessageType())){
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_CREATE_OTP.getEventLog());
			}
			else if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_GENERATE_ONETIMEPW_SOAP.getMessageType())){
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_GENERATE_OTP.getEventLog());
			}
			else{
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_SUCCESS.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.SEND_OTP.getEventLog());
			}
		

		}
	}

	public static void raiseStatoutErrorForSmsAndEmail(final OrigInvokeProfile origInvokeProfileIncoming, EC02Instance ec02Instance,
			GssoComposeDebugLog composeDebugLog) {

		APPInstance appInstance = ec02Instance.getAppInstance();
		EquinoxRawData rawDataOrig = origInvokeProfileIncoming.getOrigEquinoxRawData();

		if (rawDataOrig.getCType().equals(EventCtype.PLAIN)) {
			/** IDLE_SEND_OTP_REQ **/
			if (origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())) {

				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.SEND_OTP.getEventLog());
			}

			/** IDLE_AUTH_OTP **/
			if (origInvokeProfileIncoming.getIncomingMessageType().equals(
					IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.AUTHEN_OTP.getEventLog());
			}
		}
		else {
			if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_SOAP.getMessageType())){
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPW_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_AUTHEN_OTP.getEventLog());
			}
			else if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_AUTHEN_OTP_ID_SOAP.getMessageType())){
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_AUTHENONETIMEPW_W_ID_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_AUTHEN_OTP_ID.getEventLog());
			}
			else if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_CREATE_OTP_SOAP.getMessageType())){
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_CREATEONETIMEPW_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_CREATE_OTP.getEventLog());
			}
			else if(origInvokeProfileIncoming.getIncomingMessageType().equals(IncomingMessageType.WS_GENERATE_ONETIMEPW_SOAP.getMessageType())){
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_GENERATEONETIMEPW_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.WS_GENERATE_OTP.getEventLog());
			}
			else{
				ec02Instance.incrementsStat(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());

				if (ConfigureTool.isWriteLog(ConfigName.DEBUG_LOG_ENABLED)) {
					composeDebugLog.addStatisticOut(Statistic.GSSO_RETURN_SENDONETIMEPASSWORD_RESPONSE_ERROR.getStatistic());
				}

				appInstance.getMapOrigInvokeEventDetailOutput().put(rawDataOrig.getInvoke(), EventLog.SEND_OTP.getEventLog());
			}
			
		}
	}

	public static String findXmlValue(final String findWord, final String message) {

		String soapIncoming;
		try {
			soapIncoming = message.substring(indexOfEndWord(Pattern.compile("<" + findWord), message),
					indexOfStartWord(Pattern.compile("</" + findWord + ">"), message));
			soapIncoming = soapIncoming.substring(soapIncoming.indexOf(">") + 1);

			soapIncoming = repleceLifeTimeoutMins(findWord, soapIncoming);

			return soapIncoming;
		}
		catch (Exception e) {}
		
		try {
			soapIncoming = message.substring(indexOfEndWord(Pattern.compile("<.*:" + findWord + " "), message),
					indexOfStartWord(Pattern.compile("</.*:" + findWord + ">"), message));
			soapIncoming = soapIncoming.substring(soapIncoming.indexOf(">") + 1);

			soapIncoming = repleceLifeTimeoutMins(findWord, soapIncoming);
			
			return soapIncoming;
		}
		catch (Exception e) {}
		
		try {
			soapIncoming = message.substring(indexOfEndWord(Pattern.compile("(<([a-z]|[A-Z])+:"+findWord+">)"), message),
					indexOfStartWord(Pattern.compile("</.*:" + findWord + ">"), message));

			soapIncoming = repleceLifeTimeoutMins(findWord, soapIncoming);

			return soapIncoming;
		}
		catch (Exception e) {
			return "";
		}
	}
	
	private static String repleceLifeTimeoutMins(final String findWord, final String message) {
		/* if 'addTimeoutMins' Convert To 'lifeTimeoutMins' */
		if (findWord.equals("addTimeoutMins")) {
			return "<lifeTimeoutMins>" + message + "</lifeTimeoutMins>";
		}
		else {
			return "<" + findWord + ">" + message + "</" + findWord + ">";
		}
	}

	public static String findXmlPrefix(final String firstWord, final String secondaryWord, final String message) {
		try {
			try {
				return message.substring(0, message.indexOf(firstWord)) + firstWord;
			}
			catch (Exception e) {
				return message.substring(0, message.indexOf(secondaryWord) + secondaryWord.length());
			}
		}
		catch (Exception e) {
			return "";
		}
	}

	public static String findXmlSubfix(final String firstWord, final String secondaryWord, final String message) {
		try {
			try {
				return message.substring(message.indexOf(firstWord), message.length());
			}
			catch (Exception e) {
				return message.substring(message.indexOf(secondaryWord), message.length());
			}
		}
		catch (Exception e) {
			return "";
		}
	}

	/** EXTRACT GSSO WS AUTH OTP REQUEST **/
	public static SendWSOTPRequest extractGssoWSAuthOTPRequest(final EquinoxRawData equinoxRawData) {

		SendWSOTPRequest sendWSOTPRequest = new SendWSOTPRequest();

		/** CHECK JSON OR SOAP **/
		/** IF SOAP **/
		if (equinoxRawData.getCType().contains(EventCtype.XML)) {
			try {
				String incomingMessage = equinoxRawData.getRawDataMessage();

				String prefix = "<SendWSOTPRequest>";
				String subfix = "</SendWSOTPRequest>";
				String msisdn = GssoDataManagement.findXmlValue("msisdn", incomingMessage);
				String OTPmobile = GssoDataManagement.findXmlValue( "OTPmobile", incomingMessage);
				String email = GssoDataManagement.findXmlValue("email", incomingMessage);
				String link = GssoDataManagement.findXmlValue("link", incomingMessage);
				String service = GssoDataManagement.findXmlValue("service", incomingMessage);
				String accountType = GssoDataManagement.findXmlValue("accountType", incomingMessage);
				
				String soapIncoming = prefix + msisdn + OTPmobile + email + link + service + accountType + subfix;

				sendWSOTPRequest = (SendWSOTPRequest) JAXBHandler.createInstance(
						InstanceContext.getSendWSOTPRequestContext(), soapIncoming, SendWSOTPRequest.class);

//				wsAuthOtpRequest.setMessageType(GssoMessageType.SOAP);
			}
			catch (Exception e) {
				sendWSOTPRequest = new SendWSOTPRequest();
			}
		}
		
		return sendWSOTPRequest;
	}
	
	/** EXTRACT GSSO WS CREATE OTP REQUEST **/
	public static SendWSOTPRequest extractGssoWSCreateOTPRequest(final EquinoxRawData equinoxRawData) {

		SendWSOTPRequest sendWSOTPRequest = new SendWSOTPRequest();

		/** CHECK JSON OR SOAP **/
		/** IF SOAP **/
		if (equinoxRawData.getCType().contains(EventCtype.XML)) {
			try {
				String incomingMessage = equinoxRawData.getRawDataMessage();

				String prefix = "<SendWSOTPRequest>";
				String subfix = "</SendWSOTPRequest>";
				String msisdn = GssoDataManagement.findXmlValue("msisdn", incomingMessage);
				String service = GssoDataManagement.findXmlValue("service", incomingMessage);
				String accountType = GssoDataManagement.findXmlValue("accountType", incomingMessage);
				String addTimeoutMins = GssoDataManagement.findXmlValue("addTimeoutMins", incomingMessage);
				String soapIncoming = prefix + msisdn + service + accountType + addTimeoutMins + subfix;

				sendWSOTPRequest = (SendWSOTPRequest) JAXBHandler.createInstance(
						InstanceContext.getSendWSOTPRequestContext(), soapIncoming, SendWSOTPRequest.class);

			}
			catch (Exception e) {
				sendWSOTPRequest = new SendWSOTPRequest();
				
			}
		}
		
		return sendWSOTPRequest;
	}
	
	/** EXTRACT GSSO WS GENERATE OTP REQUEST **/
	public static SendWSOTPRequest extractGssoWSGenerateOTPRequest(final EquinoxRawData equinoxRawData) {

		SendWSOTPRequest sendWSOTPRequest = new SendWSOTPRequest();

		/** CHECK JSON OR SOAP **/
		/** IF SOAP **/
		if (equinoxRawData.getCType().contains(EventCtype.XML)) {
			try {
				String incomingMessage = equinoxRawData.getRawDataMessage();

				String prefix = "<SendWSOTPRequest>";
				String subfix = "</SendWSOTPRequest>";
				String msisdn = GssoDataManagement.findXmlValue("msisdn", incomingMessage);
				String service = GssoDataManagement.findXmlValue("service", incomingMessage);
				String accountType = GssoDataManagement.findXmlValue("accountType", incomingMessage);
				String soapIncoming = prefix + msisdn + service + accountType + subfix;

				sendWSOTPRequest = (SendWSOTPRequest) JAXBHandler.createInstance(
						InstanceContext.getSendWSOTPRequestContext(), soapIncoming, SendWSOTPRequest.class);

			}
			catch (Exception e) {
				sendWSOTPRequest = new SendWSOTPRequest();
				
			}
		}
		
		return sendWSOTPRequest;
	}
	
	/** EXTRACT GSSO WS CONFIRM OTP REQUEST **/
	public static GssoWSConfirmOTPRequest extractGssoWSConfirmOTPRequest(final EquinoxRawData equinoxRawData) {

		GssoWSConfirmOTPRequest wsConfirmOtpRequest = new GssoWSConfirmOTPRequest();

		/** CHECK JSON OR SOAP **/
		/** IF SOAP **/
		if (equinoxRawData.getCType().contains(EventCtype.XML)) {
			try {
				String incomingMessage = equinoxRawData.getRawDataMessage();

				String prefix = "<SendWSConfirmOTPRequest>";
				String subfix = "</SendWSConfirmOTPRequest>";
				String msisdn = GssoDataManagement.findXmlValue("msisdn", incomingMessage);
				String pwd = GssoDataManagement.findXmlValue("password", incomingMessage);
				String transactionID = GssoDataManagement.findXmlValue("sessionId", incomingMessage);
//				String service = GssoDataManagement.findXmlValue("service", incomingMessage);
				String soapIncoming = prefix + msisdn + pwd + transactionID + subfix;

				SendWSConfirmOTPRequest sendWsConfirmOTPRequest = (SendWSConfirmOTPRequest) JAXBHandler.createInstance(
						InstanceContext.getSendWSConfirmOTPRequestContext(), soapIncoming, SendWSConfirmOTPRequest.class);

				wsConfirmOtpRequest.setSendWSConfirmOTPReq( sendWsConfirmOTPRequest );
				wsConfirmOtpRequest.setMessageType(GssoMessageType.SOAP);
			}
			catch (Exception e) {
				wsConfirmOtpRequest = new GssoWSConfirmOTPRequest();
				wsConfirmOtpRequest.setMessageType(GssoMessageType.SOAP);
				wsConfirmOtpRequest.setSendWSConfirmOTPReq( new SendWSConfirmOTPRequest() );
			}
		}
		/** IF JSON **/
		else if (equinoxRawData.getCType().contains(EventCtype.PLAIN)) {
			/** JSON TO JAVA **/
			String jsonVal = null;
			try {
				jsonVal = equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL);
			}
			catch (Exception e) {
				jsonVal = "";
			}

			try {
				wsConfirmOtpRequest = InstanceContext.getGson().fromJson(jsonVal, GssoWSConfirmOTPRequest.class);
				wsConfirmOtpRequest.setMessageType(GssoMessageType.JSON);
			}
			catch (Exception e) {
				wsConfirmOtpRequest = new GssoWSConfirmOTPRequest();
				wsConfirmOtpRequest.setMessageType(GssoMessageType.JSON);
				wsConfirmOtpRequest.setSendWSConfirmOTPReq( new SendWSConfirmOTPRequest() );
			}
		}
		return wsConfirmOtpRequest;
	}
	
	/** EXTRACT GSSO WS AUTH OTP WITH ID REQUEST **/
	public static SendWSOTPRequest extractGssoWSAuthOTPWithIDRequest(final EquinoxRawData equinoxRawData) {

		SendWSOTPRequest sendWSOTPRequest = new SendWSOTPRequest();

		/** CHECK JSON OR SOAP **/
		/** IF SOAP **/
		if (equinoxRawData.getCType().contains(EventCtype.XML)) {
			try {
				String incomingMessage = equinoxRawData.getRawDataMessage();

				String prefix = "<SendWSOTPRequest>";
				String subfix = "</SendWSOTPRequest>";
				String msisdn = GssoDataManagement.findXmlValue("msisdn", incomingMessage);
				String OTPmobile = GssoDataManagement.findXmlValue( "OTPmobile", incomingMessage);
				String email = GssoDataManagement.findXmlValue("email", incomingMessage);
				String link = GssoDataManagement.findXmlValue("link", incomingMessage);
				String service = GssoDataManagement.findXmlValue("service", incomingMessage);
				String accountType = GssoDataManagement.findXmlValue("accountType", incomingMessage);
				String companyID = GssoDataManagement.findXmlValue("CompanyID", incomingMessage);
				String companyName = GssoDataManagement.findXmlValue("CompanyName", incomingMessage);
				
				String soapIncoming = prefix + msisdn + OTPmobile + email + link + service 
						+ accountType + companyID + companyName + subfix;

				sendWSOTPRequest = (SendWSOTPRequest) JAXBHandler.createInstance(
						InstanceContext.getSendWSOTPRequestContext(), soapIncoming, SendWSOTPRequest.class);

			}
			catch (Exception e) {
				sendWSOTPRequest = new SendWSOTPRequest();
				
			}
		}
		
		return sendWSOTPRequest;
	}

	/** EXTRACT GSSO WS CONFIRM OTP WITH ID REQUEST **/
	public static GssoWSConfirmOTPWithIDRequest extractGssoWSConfirmOTPWithIDRequest(final EquinoxRawData equinoxRawData) {

		GssoWSConfirmOTPWithIDRequest wsConfirmOtpWithIDRequest = new GssoWSConfirmOTPWithIDRequest();

		/** CHECK JSON OR SOAP **/
		/** IF SOAP **/
		if (equinoxRawData.getCType().contains(EventCtype.XML)) {
			try {
				String incomingMessage = equinoxRawData.getRawDataMessage();

				String prefix = "<SendWSConfirmOTPWithIDRequest>";
				String subfix = "</SendWSConfirmOTPWithIDRequest>";
				String msisdn = GssoDataManagement.findXmlValue("msisdn", incomingMessage);
				String password = GssoDataManagement.findXmlValue("password", incomingMessage);
				String sessionId = GssoDataManagement.findXmlValue("sessionId", incomingMessage);
				String companyID = GssoDataManagement.findXmlValue("CompanyID", incomingMessage);
				String companyName = GssoDataManagement.findXmlValue("CompanyName", incomingMessage);
				String soapIncoming = prefix + msisdn + password + sessionId + companyID + companyName + subfix;

				SendWSConfirmOTPWithIDRequest sendWsConfirmOTPWithIDRequest = (SendWSConfirmOTPWithIDRequest) JAXBHandler.createInstance(
						InstanceContext.getSendWSConfirmOTPWithIDRequestContext(), soapIncoming, SendWSConfirmOTPWithIDRequest.class);

				wsConfirmOtpWithIDRequest.setSendWSConfirmOTPWithIDReq( sendWsConfirmOTPWithIDRequest );
				wsConfirmOtpWithIDRequest.setMessageType(GssoMessageType.SOAP);
			}
			catch (Exception e) {
				wsConfirmOtpWithIDRequest = new GssoWSConfirmOTPWithIDRequest();
				wsConfirmOtpWithIDRequest.setMessageType(GssoMessageType.SOAP);
				wsConfirmOtpWithIDRequest.setSendWSConfirmOTPWithIDReq( new SendWSConfirmOTPWithIDRequest() );
			}
		}
		/** IF JSON **/
		else if (equinoxRawData.getCType().contains(EventCtype.PLAIN)) {
			/** JSON TO JAVA **/
			String jsonVal = null;
			try {
				jsonVal = equinoxRawData.getRawDataAttribute(EquinoxAttribute.VAL);
			}
			catch (Exception e) {
				jsonVal = "";
			}

			try {
				wsConfirmOtpWithIDRequest = InstanceContext.getGson().fromJson(jsonVal, GssoWSConfirmOTPWithIDRequest.class);
				wsConfirmOtpWithIDRequest.setMessageType(GssoMessageType.JSON);
			}
			catch (Exception e) {
				wsConfirmOtpWithIDRequest = new GssoWSConfirmOTPWithIDRequest();
				wsConfirmOtpWithIDRequest.setMessageType(GssoMessageType.JSON);
				wsConfirmOtpWithIDRequest.setSendWSConfirmOTPWithIDReq( new SendWSConfirmOTPWithIDRequest() );
			}
		}
		return wsConfirmOtpWithIDRequest;
	}

	public static ArrayList<String> enableCommandsToRefund(String value)
	{
		if(StringUtils.isEmpty(value))
		{
			return new ArrayList<String>();
		}
		else{
			return new ArrayList<String>(Arrays.asList(value.split("\\|")));
		}
	}

	public static Refund extractGssoRefund(String val){
		Refund refund = new Refund();
		
		try {
			return (Refund) MessageParser.fromJson(val, Refund.class);
		}
		catch (Exception e) {
			return refund;
		}
	}
}