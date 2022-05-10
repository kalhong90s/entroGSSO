package com.ais.eqx.gsso.utils;

import com.ais.eqx.gsso.enums.ConfigName;
import com.ais.eqx.gsso.instances.APPInstance;
import com.ais.eqx.gsso.instances.GssoInqSubRequest;
import com.ais.eqx.gsso.instances.PortCheckReq;

public class GssoConstructSoapMessage {

	public static String composeInqSubReqToUSMP(final GssoInqSubRequest inqSubReq, APPInstance appInstance, boolean isCheckDoVas) {

		String uriOverride =  "";
		String headerOverrideName = "";
		String headerOverrideValue = "";
		String BodySub  = "";

		if(isCheckDoVas){
			if(appInstance.isInquiryVasSubscriber()){
				uriOverride = ConfigureTool.getConfigure(ConfigName.INQUIRY_VAS_SUBSCRIBER_URI_OVERRIDE);
				headerOverrideName = ConfigureTool.getConfigure(ConfigName.INQUIRY_VAS_SUBSCRIBER_HEADER_OVERRIDE_NAME);
				headerOverrideValue = ConfigureTool.getConfigure(ConfigName.INQUIRY_VAS_SUBSCRIBER_HEADER_OVERRIDE);
				BodySub = ConfigureTool.getConfigure(ConfigName.INQUIRYVASSUBSCRIBER_BODY_SUB);
			}
			else {
				uriOverride = ConfigureTool.getConfigure(ConfigName.INQUIRY_SUBSCRIBER_URI_OVERRIDE);
				headerOverrideName = ConfigureTool.getConfigure(ConfigName.INQUIRY_SUBSCRIBER_HEADER_OVERRIDE_NAME);
				headerOverrideValue = ConfigureTool.getConfigure(ConfigName.INQUIRY_SUBSCRIBER_HEADER_OVERRIDE);
				BodySub = ConfigureTool.getConfigure(ConfigName.INQUIRYSUBSCRIBER_BODY_SUB);
			}
		}
		else{
			uriOverride = ConfigureTool.getConfigure(ConfigName.INQUIRY_VAS_SUBSCRIBER_URI_OVERRIDE);
			headerOverrideName = ConfigureTool.getConfigure(ConfigName.INQUIRY_VAS_SUBSCRIBER_HEADER_OVERRIDE_NAME);
			headerOverrideValue = ConfigureTool.getConfigure(ConfigName.INQUIRY_VAS_SUBSCRIBER_HEADER_OVERRIDE);
			BodySub = ConfigureTool.getConfigure(ConfigName.INQUIRYVASSUBSCRIBER_BODY_SUB);
		}
		
		String soapOut = "<UriOverride value=\"" + uriOverride + "\"/>"
		 + "<HeaderOverride name=\"" + headerOverrideName + "\" value=\"" + headerOverrideValue + "\"/>"
		 + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sub=\"http://vsmp.ais.co.th/webservices/subscriber/\">"
			 + "<soapenv:Header/>"
			 + "<soapenv:Body>"
				 + "<sub:"+ BodySub + ">"
					 + "<sub:Username>" + inqSubReq.getUserName() + "</sub:Username>"
					 + "<sub:OrderRef>" + inqSubReq.getOrderRef() + "</sub:OrderRef>"
					 + "<sub:OrderDesc>" + inqSubReq.getOrderDesc() + "</sub:OrderDesc>"
					 + "<sub:Msisdn>" + inqSubReq.getMsisdn() + "</sub:Msisdn>"
				 + "</sub:"+ BodySub + ">"
			 + "</soapenv:Body>"
		 + "</soapenv:Envelope>";
		
		return soapOut;
	}

	public static String composePortCheckReqToUSMP(final PortCheckReq portCheckReq) {

		String soapOut = "<UriOverride value=\"" + ConfigureTool.getConfigure(ConfigName.PORTCHECK_SUBSCRIBER_URI_OVERRIDE) + "\"/>"
		 +  "<HeaderOverride name=\"" + ConfigureTool.getConfigure(ConfigName.PORTCHECK_SUBSCRIBER_HEADER_OVERRIDE_NAME) + "\" value=\"" + ConfigureTool.getConfigure(ConfigName.PORTCHECK_SUBSCRIBER_HEADER_OVERRIDE) + "\"/>"
		 + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://Ais.co.th/Web/Services/\">"
			 + "<soapenv:Header/>"
			 + "<soapenv:Body>"
				 + "<ser:Portcheck>"
					 + "<ser:UserName>" + portCheckReq.getUserName() + "</ser:UserName>"
					 + "<ser:OrderRef>" + portCheckReq.getOrderRef() + "</ser:OrderRef>"
					 + "<ser:OrderDesc>" + portCheckReq.getOrderDesc() + "</ser:OrderDesc>"
					 + "<ser:Msisdn>" + portCheckReq.getMsisdn() + "</ser:Msisdn>"
				 + "</ser:Portcheck>"
			 + "</soapenv:Body>"
		 + "</soapenv:Envelope>";

		return soapOut;
	}

}
