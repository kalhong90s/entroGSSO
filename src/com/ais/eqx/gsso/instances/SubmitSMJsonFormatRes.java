package com.ais.eqx.gsso.instances;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.IncomingMessageType;
import com.ais.eqx.gsso.interfaces.EquinoxAttribute;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.Mode;
import com.ais.eqx.gsso.jaxb.InstanceContext;
import com.ais.eqx.gsso.utils.ConfigureTool;

import ec02.af.data.EquinoxRawData;

@XmlRootElement(name = "sendOneTimePWResponse")
public class SubmitSMJsonFormatRes {

	@XmlElement(name = "code")
	private String			code;

	@XmlElement(name = "description")
	private String			description;

	@XmlElement(name = "isSuccess")
	private String			isSuccess;

	@XmlElement(name = "orderRef")
	private String			orderRef;

	@XmlElement(name = "transactionID")
	private String			transactionID;

	@XmlElement(name = "referenceNumber")
	private String			referenceNumber;

	@XmlElement(name = "operName")
	private String			operName;

	@XmlElement(name = "lifeTimeoutMins")
	private String			lifeTimeoutMins;

	@XmlElement(name = "expirePassword")
	private String			expirePassword;

	@XmlElement(name = "oneTimePassword")
	private String			oneTimePassword;

	private EquinoxRawData	eRawData;

	public SubmitSMJsonFormatRes() {
	}

	public SubmitSMJsonFormatRes(final EquinoxRawData rawData, final String code, final String description, final String isSuccess) {
		this.seteRawData(rawData);
		this.setCode(code);
		this.setDescription(description);
		this.setSuccess(isSuccess);
	}

	public EquinoxRawData geteRawData() {
		return eRawData;
	}

	public void seteRawData(final EquinoxRawData eRawData) {
		this.eRawData = eRawData;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String isSuccess() {
		return isSuccess;
	}

	public void setSuccess(final String isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public void setOrderRef(final String orderRef) {
		this.orderRef = orderRef;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(final String transactionID) {
		this.transactionID = transactionID;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(final String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getOperName() {
		return operName;
	}

	public void setOperName(final String operName) {
		this.operName = operName;
	}

	public String getLifeTimeoutMins() {
		return lifeTimeoutMins;
	}

	public void setLifeTimeoutMins(final String lifeTimeoutMins) {
		this.lifeTimeoutMins = lifeTimeoutMins;
	}

	public String getExpirePassword() {
		return expirePassword;
	}

	public void setExpirePassword(final String expirePassword) {
		this.expirePassword = expirePassword;
	}

	public String getOneTimePassword() {
		return oneTimePassword;
	}

	public void setOneTimePassword(final String oneTimePassword) {
		this.oneTimePassword = oneTimePassword;
	}

	public EquinoxRawData toRawDatas(final APPInstance appInstance) {

		OrigInvokeProfile origInvokeProfile = appInstance.getMapOrigProfile().get(geteRawData().getInvoke());
		EquinoxRawData origRawData = origInvokeProfile.getOrigEquinoxRawData();
		String tr = origInvokeProfile.getTransactionID();

		SubmitSMJsonFormatRes submitSMJsonFormatRes = new SubmitSMJsonFormatRes();
		submitSMJsonFormatRes.setCode(getCode());
		submitSMJsonFormatRes.setDescription(getDescription());
		submitSMJsonFormatRes.setSuccess(isSuccess());
		submitSMJsonFormatRes.setOrderRef(origInvokeProfile.getOrderRefLog());		

		if (isSuccess().equalsIgnoreCase("true")) {
			
			/* Check the value of "mode" in EC02 Configuration */
			String mode = ConfigureTool.getConfigure(ConfigName.MODE);
			if(mode.equalsIgnoreCase(Mode.TEST)){
				TransactionData confirmOTP = appInstance.getTransactionidData().get(tr);
				submitSMJsonFormatRes.setOneTimePassword(confirmOTP.getOtp());
			}

			submitSMJsonFormatRes.setTransactionID(tr);
			submitSMJsonFormatRes.setReferenceNumber(appInstance.getTransactionidData().get(origInvokeProfile.getTransactionID())
					.getRefNumber());

			if (!origRawData.getCType().equals(EventCtype.XML)) {

				/** IDLE_SEND_OTP_REQ **/
				if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())) {
					submitSMJsonFormatRes.setOperName(appInstance.getProfile().getOper());
				}

				/** IDLE_AUTH_OTP **/
				if (origInvokeProfile.getIncomingMessageType()
						.equals(IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
					String oper = "";
					if (!(origInvokeProfile.getGssoServiceTemplate().getOper() == null)) {

						oper = origInvokeProfile.getGssoServiceTemplate().getOper();
					}
					else {

						if (appInstance.getProfile().getOper() != null) {
							submitSMJsonFormatRes.setOperName(appInstance.getProfile().getOper());
						}
						else {
							submitSMJsonFormatRes.setOperName(oper);
						}
					}
				}
			}

			submitSMJsonFormatRes.setLifeTimeoutMins(origInvokeProfile.getGssoOTPRequest().getSendOneTimePW().getLifeTimeoutMins());

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date resultdate = new Date(appInstance.getTransactionidData().get(tr).getOtpExpireTime());
			String date = sdf.format(resultdate);

			submitSMJsonFormatRes.setExpirePassword(date);

		}

		String messageOut = InstanceContext.getGson().toJson(submitSMJsonFormatRes);

		String rootElement = "";
		if (!origRawData.getCType().equals(EventCtype.XML)) {

			/** IDLE_SEND_OTP_REQ **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.SEND_OTP_JSON.getMessageType())) {
				rootElement = IncomingMessageType.SEND_OTP_JSON.getResponseFormat();
			}
			/** IDLE_AUTH_OTP **/
			if (origInvokeProfile.getIncomingMessageType().equals(IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getMessageType())) {
				rootElement = IncomingMessageType.AUTHEN_ONETIMEPASSWORD_JSON.getResponseFormat();
			}

		}

		messageOut = "{\"" + rootElement + "\":" + messageOut + "}";

		HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put("name", EventName.HTTP);
		attrs.put("ctype", origRawData.getCType());
		attrs.put("type", EventAction.RESPONSE);
		attrs.put("to", this.geteRawData().getOrig());
		attrs.put(EquinoxAttribute.VAL, messageOut);
		EquinoxRawData eRawdataOut = new EquinoxRawData();
		eRawdataOut.setRawDataAttributes(attrs);
		eRawdataOut.setInvoke(geteRawData().getInvoke());
		return eRawdataOut;

	}

}
