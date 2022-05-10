package com.ais.eqx.gsso.utils;

import com.ais.eqx.gsso.instances.InquirySubscriber;
import com.ais.eqx.gsso.instances.InquirySubscriberResult;
import com.ais.eqx.gsso.instances.InquiryVASSubscriber;
import com.ais.eqx.gsso.instances.InquiryVASSubscriberResult;
import com.ais.eqx.gsso.instances.PortCheckResponse;
import com.ais.eqx.gsso.jaxb.InstanceContext;
import com.ais.eqx.gsso.jaxb.JAXBHandler;

import ec02.af.data.EquinoxRawData;
import ec02.af.utils.Log;


public class SoapToObject {

	public static InquirySubscriber parserInquirySub(String rawDataMessage) {

		InquirySubscriber inquirySubscriber = new InquirySubscriber();

		try {
			String soapIncoming = rawDataMessage.substring(rawDataMessage.indexOf("<InquirySubscriberResponse"),
					rawDataMessage.indexOf("</InquirySubscriberResponse>"));
			soapIncoming = soapIncoming.substring(soapIncoming.indexOf(">") + 1);
			
			
//			String soapIncoming = rawDataMessage.substring(rawDataMessage.indexOf("<InquiryVASSubscriberResponse"),
//					rawDataMessage.indexOf("</InquiryVASSubscriberResponse>"));
//			soapIncoming = soapIncoming.substring(soapIncoming.indexOf(">") + 1);

			try {
				String prefix = "<InquirySubscriberResult><OperationStatus>";
				String middle = "</OperationStatus><Subscriber>";
				String subfix = "</Subscriber></InquirySubscriberResult>";
				
				String isSuccess = GssoDataManagement.findXmlValue("IsSuccess", soapIncoming);
				String code = GssoDataManagement.findXmlValue("Code", soapIncoming);
				String description = GssoDataManagement.findXmlValue("Description", soapIncoming);
				String transactionID = GssoDataManagement.findXmlValue("TransactionID", soapIncoming);
				String orderRef = GssoDataManagement.findXmlValue("OrderRef", soapIncoming);
				
				String msisdn = GssoDataManagement.findXmlValue("Msisdn", soapIncoming);
				String customerID = GssoDataManagement.findXmlValue("CustomerID", soapIncoming);
				String state = GssoDataManagement.findXmlValue("State", soapIncoming);
				String lang = GssoDataManagement.findXmlValue("Lang", soapIncoming);
				String smsLanguage = GssoDataManagement.findXmlValue("SMSLanguage", soapIncoming);
				String cos = GssoDataManagement.findXmlValue("Cos", soapIncoming);
				String spName = GssoDataManagement.findXmlValue("spName", soapIncoming);
				String soapIn = prefix + isSuccess + code + description + transactionID + orderRef + middle + msisdn + customerID + state + lang + smsLanguage + cos + spName + subfix;

				InquirySubscriberResult InquirySubResponse = (InquirySubscriberResult) JAXBHandler.createInstance(
						InstanceContext.getInquirySubscriberResponseContext(), soapIn, InquirySubscriberResult.class);
				
				inquirySubscriber.setInquirySubscriberResult(InquirySubResponse);
			}
			catch (Exception e) {
				InquirySubscriberResult InquirySubResponse = (InquirySubscriberResult) JAXBHandler.createInstance(
						InstanceContext.getInquirySubscriberResponseContext(), soapIncoming, InquirySubscriberResult.class);

				inquirySubscriber.setInquirySubscriberResult(InquirySubResponse);
			}

		}
		catch (Exception e) {
			Log.e("Cannot parse root element \"InquirySubscriberResponse\" from InquirySub response message" + "Error Message : "
					+ rawDataMessage);
		}

		return inquirySubscriber;

	}

	public static InquiryVASSubscriber parserInquiryVasSub(String rawDataMessage) {

		InquiryVASSubscriber inquirySubscriber = new InquiryVASSubscriber();

		try {
//			String soapIncoming = rawDataMessage.substring(rawDataMessage.indexOf("<InquirySubscriberResponse"),
//					rawDataMessage.indexOf("</InquirySubscriberResponse>"));
//			soapIncoming = soapIncoming.substring(soapIncoming.indexOf(">") + 1);
			
			
			String soapIncoming = rawDataMessage.substring(rawDataMessage.indexOf("<InquiryVASSubscriberResponse"),
					rawDataMessage.indexOf("</InquiryVASSubscriberResponse>"));
			soapIncoming = soapIncoming.substring(soapIncoming.indexOf(">") + 1);

			try {
				String prefix = "<InquiryVASSubscriberResult><OperationStatus>";
				String middle = "</OperationStatus><Subscriber>";
				String subfix = "</Subscriber></InquiryVASSubscriberResult>";
				
				String isSuccess = GssoDataManagement.findXmlValue("IsSuccess", soapIncoming);
				String code = GssoDataManagement.findXmlValue("Code", soapIncoming);
				String description = GssoDataManagement.findXmlValue("Description", soapIncoming);
				String transactionID = GssoDataManagement.findXmlValue("TransactionID", soapIncoming);
				String orderRef = GssoDataManagement.findXmlValue("OrderRef", soapIncoming);
				
				String msisdn = GssoDataManagement.findXmlValue("msisdn", soapIncoming);
				String customerID = GssoDataManagement.findXmlValue("customerId", soapIncoming);
				String state = GssoDataManagement.findXmlValue("state", soapIncoming);
				String lang = GssoDataManagement.findXmlValue("language", soapIncoming);
				String cos = GssoDataManagement.findXmlValue("cos", soapIncoming);
				String spName = GssoDataManagement.findXmlValue("spName", soapIncoming);
				String soapIn = prefix + isSuccess + code + description + transactionID + orderRef + middle + msisdn + customerID + state + lang + cos + spName + subfix;

				InquiryVASSubscriberResult InquiryVASSubResponse = (InquiryVASSubscriberResult) JAXBHandler.createInstance(
						InstanceContext.getInquiryVASSubscriberResponseContext(), soapIn, InquiryVASSubscriberResult.class);
				
				inquirySubscriber.setInquiryVASSubscriberResult(InquiryVASSubResponse);
			}
			catch (Exception e) {
				InquiryVASSubscriberResult InquirySubResponse = (InquiryVASSubscriberResult) JAXBHandler.createInstance(
						InstanceContext.getInquiryVASSubscriberResponseContext(), soapIncoming, InquiryVASSubscriberResult.class);

				inquirySubscriber.setInquiryVASSubscriberResult(InquirySubResponse);
			}

		}
		catch (Exception e) {
			Log.e("Cannot parse root element \"InquiryVASSubscriberResponse\" from InquirySub response message" + "Error Message : "
					+ rawDataMessage);
		}

		return inquirySubscriber;

	}

	public static PortCheckResponse parserPortCheck(String rawDataMessage, EquinoxRawData rawDatas) {

		PortCheckResponse portCheckResponse = new PortCheckResponse();

		try {
			String soapIncoming = rawDataMessage.substring(rawDataMessage.indexOf("<PortcheckResponse"),
					rawDataMessage.indexOf("</PortcheckResponse>"));
			soapIncoming = soapIncoming.substring(soapIncoming.indexOf(">") + 1);

			try {
				String prefix = "<PortcheckResult><OperationStatus>";
				String middle = "</OperationStatus><Journal>";
				String subfix = "</Journal></PortcheckResult>";
				
				String isSuccess = GssoDataManagement.findXmlValue("IsSuccess", soapIncoming);
				String code = GssoDataManagement.findXmlValue("Code", soapIncoming);
				String description = GssoDataManagement.findXmlValue("Description", soapIncoming);
				String transactionID = GssoDataManagement.findXmlValue("TransactionId", soapIncoming);
				String orderRef = GssoDataManagement.findXmlValue("OrderRef", soapIncoming);
				
				String routingId = GssoDataManagement.findXmlValue("RoutingId", soapIncoming);
				String spName = GssoDataManagement.findXmlValue("SpName", soapIncoming);
				String originalSP = GssoDataManagement.findXmlValue("OriginalSp", soapIncoming);
				String listOfHops = GssoDataManagement.findXmlValue("ListOfHops", soapIncoming);
				String soapIn = prefix + isSuccess + code + description + transactionID + orderRef + middle + routingId + spName + originalSP + listOfHops + subfix;

				portCheckResponse = (PortCheckResponse) JAXBHandler.createInstance(InstanceContext.getPortCheckResponseContext(),
						soapIn, PortCheckResponse.class);
			}
			catch (Exception e) {
				portCheckResponse = (PortCheckResponse) JAXBHandler.createInstance(InstanceContext.getPortCheckResponseContext(),
						soapIncoming, PortCheckResponse.class);
			}

		}
		catch (Exception e) {
			Log.e("Cannot parse root element \"PortcheckResponse\" from PortCheck response message\n" + "Error Message : "
					+ rawDataMessage);
		}

		return portCheckResponse;
	}

}
