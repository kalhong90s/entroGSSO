package com.ais.eqx.gsso.tester;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ec02.exception.MessageParserException;
import ec02.server.EC02Handler;
import ec02.server.EC02Server;

public class APPTester {

	public static String formatXml(String xml) {
		try {
			Transformer serializer = SAXTransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			Source xmlSource = new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
			StreamResult res = new StreamResult(new ByteArrayOutputStream());
			serializer.transform(xmlSource, res);
			return new String(((ByteArrayOutputStream) res.getOutputStream()).toByteArray());
		}
		catch (Exception e) {
			return xml;
		}
	}

	/**
	 * @param args
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, TransformerException, XPathExpressionException,
			ParserConfigurationException, SAXException, MessageParserException, InterruptedException, ExecutionException {

		String filePath = "./example.msg/v2.0/" +

//		"example.xml"
//		"_idle_soap_request.xml"
//		"_idle_json_request.xml"				
//		"testAuthOTP.xml" 
//		"AuthenOTPWithID.xml"
//		"idle_ws_generate_otp.xml"
//		"idle_ws_createOTP_request.xml"
//		"idle_wsdl_send_req.xml"

//		"InquirySubscriber.xml"
//		"InquiryVASSubscriber.xml"
//		"InquiryVASSubWSAuth.xml"
//		"InquiryVASSubAuthWithID.xml"
//		"InquiryVassubAuthenIDSecond.xml"
//		"InquiryVASSubGenerateOTP.xml"
//		"InquiryVASSubWSCreateOTP.xml"


//		"_w_service_template_e01_response.xml"
//		"wsAuth_service_template_e01.xml"
//		"wsAuthWithID_service_template_e01.xml"
//		"new_w_service_template.xml"
//		"wsGenerateOTP_service_template_e01.xml"
//		"wsCreateOTP_service_template_e01.xml"

//		"SubmitSMReq.xml"
//		"SubmitSMSReq_wsAuth.xml"
//		"SubmitSMSReq_wsAuth_ID.xml"
//		"SubmitSMSReq_wsGenerateOTP.xml"
//		"SubmitSMSReq_wsCreateOTP.xml"
		
//		 "SendEmailReq.xml"
//		 "SendEmailReq_wsAuth.xml"
//		 "SendEmailReq_wsAuth_ID.xml"

//		"DeliveryReportReq.xml"
//		"DeliveryReportReq_wsAuth.xml"
//		"DeliveryReportReq_wsAuth_ID.xml"
//		"DeliveryReportReq_wsGenerate_OTP.xml"
//		"DeliveryReportReq_wsCreate_OTP.xml"

		
//		 "_idle_soap_confirmOTP_request.xml"
//		 "idle_ws_confirmOTP_request.xml"
//		 "idle_ws_confirmOTP_ID_request.xml"
//		 "idle_ws_confirmOTP_CreateOTP.xml"
		// "example.xml"
		
//		"idle_wsdl_send_req.xml"
//		"w_wsdl_e01_response.xml"
//		"WSDLformat.xml"
		
		// "PortCheckResponse.xml"
		// "temp.xml"
		// "PortCheckResponse.xml"
		
//		 "_authen_otp_request.xml"
//		 "_generate_passkey_request_json.xml"
//		 "_confirm_otp_json_request.xml"

// 		"def_idel_generate_pk_json.xml"
//		"def_idel_generate_pk_soap.xml"

				"debug.log"
//				"temp.xml"
//		 "testtemp.xml"
		;

		BufferedReader in = new BufferedReader(new FileReader(filePath));

		String str, reqMessage = "";
		String conf = "";
		String temp = "";

		while ((str = in.readLine()) != null) {
			reqMessage += (str + "\n");
		}
		in.close();

		in = new BufferedReader(new FileReader("./conf/GSSO.EC02.GSSO.0"));
		while ((temp = in.readLine()) != null) {
			conf += temp;
		}
		in.close();
		// System.out.println( "Request is:  \n" + reqMessage);
		String[] a = { "GSSO", "GSSO", "0", conf };
		EC02Server.main(a);
		EC02Handler handler = new EC02Handler();
		System.out.println(handler.verifyAFConfig(conf));
		String responseMessage = handler.handle(reqMessage.getBytes(), 100000);
		System.out.println("Response is: \n" + formatXml(responseMessage));

		System.exit(0);
	}

}
