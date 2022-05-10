package com.ais.eqx.gsso.instances;

import java.util.HashMap;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.enums.GssoCommand;
import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.interfaces.EventCtype;
import com.ais.eqx.gsso.interfaces.EventName;
import com.ais.eqx.gsso.interfaces.Mode;
import com.ais.eqx.gsso.utils.ConfigureTool;
import com.ais.eqx.gsso.utils.SubmitSMXmlFormatRes;

import ec02.af.data.EquinoxRawData;

public class SubmitSMXMLFormatRes {

	private String			code;
	private String			description;
	private String			isSuccess;
	private String			operName;
	private String			orderRef;
	private String			pwd;
	private String			transactionID;
	private String			oneTimePassword;
	private EquinoxRawData	equinoxRawData;
	private GssoCommand			commandName;

	public SubmitSMXMLFormatRes() {
	}

	public SubmitSMXMLFormatRes(final EquinoxRawData rawData, final String code, final String description, final String isSuccess) {
		this.setEquinoxRawData(rawData);
		this.setCode(code);
		this.setDescription(description);
		this.setIsSuccess(isSuccess);
	}

	public EquinoxRawData getEquinoxRawData() {
		return equinoxRawData;
	}

	public void setEquinoxRawData(final EquinoxRawData equinoxRawData) {
		this.equinoxRawData = equinoxRawData;
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

	public void setDescription(final String descriptionl) {
		this.description = descriptionl;
	}

	public String getOperName() {
		return operName;
	}

	public void setOperName(final String operName) {
		this.operName = operName;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public void setOrderRef(final String orderRef) {
		this.orderRef = orderRef;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(final String pwd) {
		this.pwd = pwd;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(final String transactionID) {
		this.transactionID = transactionID;
	}

	public String getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(final String isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getOneTimePassword() {
		return oneTimePassword;
	}

	public void setOneTimePassword(final String oneTimePassword) {
		this.oneTimePassword = oneTimePassword;
	}

	public GssoCommand getCommandName() {
		return commandName;
	}

	public void setCommandName(GssoCommand commandName) {
		this.commandName = commandName;
	}

	public EquinoxRawData toRawDatas(final APPInstance appInstance) {
		
		OrigInvokeProfile origProfile = appInstance.getMapOrigProfile().get(getEquinoxRawData().getInvoke());
		
		EquinoxRawData eRawdataOut = new EquinoxRawData();
		SubmitSMXMLFormatRes submitSMXMLFormatRes = new SubmitSMXMLFormatRes();
		submitSMXMLFormatRes.setCode(getCode());
		submitSMXMLFormatRes.setDescription(getDescription());
		submitSMXMLFormatRes.setIsSuccess(getIsSuccess());
		submitSMXMLFormatRes.setOrderRef(origProfile.getOrderRefLog());
		submitSMXMLFormatRes.setCommandName(origProfile.getGssoOrigCommand());
		
		if (getIsSuccess().equalsIgnoreCase("true")) {
			
			/* Check the value of "mode" in EC02 Configuration */
			String mode = ConfigureTool.getConfigure(ConfigName.MODE);
			if(mode.equalsIgnoreCase(Mode.TEST)){
				submitSMXMLFormatRes.setOneTimePassword(appInstance.getTransactionidData().get(origProfile.getTransactionID()).getOtp());
			}
			
			if(origProfile.getGssoOTPRequest()!=null){
				submitSMXMLFormatRes.setOperName(appInstance.getProfile().getOper());
			}
			submitSMXMLFormatRes.setPwd("");
			
			if(origProfile.getGssoOrigCommand().equals(GssoCommand.WS_GENERATE_OTP)){
				submitSMXMLFormatRes.setPwd(appInstance.getTransactionidData().get(origProfile.getTransactionID()).getOtp());
			}
			submitSMXMLFormatRes.setTransactionID(origProfile.getTransactionID());
		}
		else {
			submitSMXMLFormatRes.setOperName("");
			submitSMXMLFormatRes.setPwd("");
			submitSMXMLFormatRes.setTransactionID("");
			submitSMXMLFormatRes.setOneTimePassword("");

		}

		String soapOut = SubmitSMXmlFormatRes.SOAPFormat(submitSMXMLFormatRes, getIsSuccess());

		HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put("name", EventName.HTTP);
		attrs.put("ctype", EventCtype.XML);
		attrs.put("type", EventAction.RESPONSE);
		attrs.put("to", getEquinoxRawData().getOrig());

		eRawdataOut.setRawDataAttributes(attrs);
		eRawdataOut.setInvoke(getEquinoxRawData().getInvoke());
		eRawdataOut.setRawMessage(soapOut);

		return eRawdataOut;

	}

}
