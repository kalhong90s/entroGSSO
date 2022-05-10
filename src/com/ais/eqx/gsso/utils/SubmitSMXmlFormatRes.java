package com.ais.eqx.gsso.utils;

import org.apache.commons.lang3.StringUtils;

import com.ais.eqx.gsso.enums.GssoCommand;
import com.ais.eqx.gsso.instances.SubmitSMXMLFormatRes;

public class SubmitSMXmlFormatRes {

	public static String SOAPFormat(SubmitSMXMLFormatRes submitSMXMLFormatRes, String isSuccess) {

		StringBuilder soapOutBuilder = new StringBuilder();
		String commandName = "";
		String link = "";
		soapOutBuilder.append("<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		soapOutBuilder.append("<S:Body>");
		
		if(submitSMXMLFormatRes.getCommandName().equals(GssoCommand.WS_AUTHEN_OTP)){
			commandName = "authenOneTimePWResponse";
			link = "ws.gsso";
		}
		else if(submitSMXMLFormatRes.getCommandName().equals(GssoCommand.WS_AUTHEN_OTP_ID)){
			commandName = "authenOneTimePWwithIDResponse";
			link = "ws.gsso";
		}
		else if(submitSMXMLFormatRes.getCommandName().equals(GssoCommand.WS_CREAT_OTP)){
			commandName = "createOneTimePWResponse";
			link = "ws.gsso";
		}
		else if(submitSMXMLFormatRes.getCommandName().equals(GssoCommand.WS_GENERATE_OTP)){
			commandName = "generateOneTimePWResponse";
			link = "ws.gsso";
		}
		else{
//			soapOutBuilder.append("<ns2:sendOneTimePWResponse xmlns:ns2=\"http://ws.sso.gsso/\">");
			commandName = "sendOneTimePWResponse";
			link = "ws.sso.gsso";
		}
		soapOutBuilder.append("<ns2:"+commandName+" xmlns:ns2=\"http://"+link+"/\">");
		soapOutBuilder.append("<return>");
		soapOutBuilder.append("<code>" + submitSMXMLFormatRes.getCode() + "</code>");
		soapOutBuilder.append("<description>" + submitSMXMLFormatRes.getDescription() + "</description>");
		soapOutBuilder.append("<isSuccess>" + submitSMXMLFormatRes.getIsSuccess() + "</isSuccess>");

		/* response for only sso command */
		if(link.equals("ws.sso.gsso")){
			if (StringUtils.isEmpty(submitSMXMLFormatRes.getOperName())) {
				soapOutBuilder.append("<operName/>");
			}
			else {
				soapOutBuilder.append("<operName>" + submitSMXMLFormatRes.getOperName() + "</operName>");
			}
		}

		soapOutBuilder.append("<orderRef>" + submitSMXMLFormatRes.getOrderRef() + "</orderRef>");

		if (StringUtils.isEmpty(submitSMXMLFormatRes.getPwd())) {
			soapOutBuilder.append("<pwd/>");
		}
		else {
			soapOutBuilder.append("<pwd>" + submitSMXMLFormatRes.getPwd() + "</pwd>");
		}
		
		if (StringUtils.isEmpty(submitSMXMLFormatRes.getTransactionID())) {
			soapOutBuilder.append("<transactionID/>");
		}
		else {
			soapOutBuilder.append("<transactionID>" + submitSMXMLFormatRes.getTransactionID() + "</transactionID>");
		}
		
		if (StringUtils.isEmpty(submitSMXMLFormatRes.getOneTimePassword())) {
			soapOutBuilder.append("<oneTimePassword/>");
		}
		else {
			soapOutBuilder.append("<oneTimePassword>" + submitSMXMLFormatRes.getOneTimePassword() + "</oneTimePassword>");
		}
		soapOutBuilder.append("</return>");
//		soapOutBuilder.append("</ns2:sendOneTimePWResponse>");
		soapOutBuilder.append("</ns2:"+commandName+">");
		soapOutBuilder.append("</S:Body>");
		soapOutBuilder.append("</S:Envelope>");

		return soapOutBuilder.toString();
	}

}
