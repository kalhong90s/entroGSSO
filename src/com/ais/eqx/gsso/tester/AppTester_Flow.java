package com.ais.eqx.gsso.tester;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import ec02.server.EC02Handler;
import ec02.server.EC02Server;

public class AppTester_Flow {

	public static final String examplePath = "./example.msg/v2.0/";
	
	public static void main(String[] args){

		File file = new File( "./log/GSSO.EC02LIB.GSSO.0.log" );
		System.err.println( "Delete file \"GSSO.EC02LIB.GSSO.0.log\" : " + file.delete() );
		
		String[] flow;
		
//		flow = new String[]{"temp.xml"};

		flow =  new String[]{

				/******************** V2.0 *****************/

			 	"debug.xml"
				
				/* SEND OTP REQ && CONFIRM, CONFIRM WITH KEY */
//			 	"_idle_soap_request.xml"
//				"_idle_json_request.xml"
//				,"InquirySubscriber.xml"
//				,"InquiryVASSubscriber.xml"
//				,"_w_service_template_e01_response.xml"
//				,"SubmitSMResponse.xml"
//				,"RefundRes.xml"
				
				/*  GENERATE PASS  KEY */
//				"_generate_passkey_request_json.xml"
				
				
				/******************** V1.0 *****************/
				
//				"testAuthOTP.xml",
//				"InquiryVASSubWSAuth.xml",
//				"wsAuth_service_template_e01.xml",
//				"SubmitSMSReq_wsAuth.xml",
//				 "SendEmailReq_wsAuth.xml",
//				 "DeliveryReportReq_wsAuth.xml",
				 
//				 "DeliveryReportReq.xml"
//				 "DeliveryReportReq_wsAuth.xml"
//				 "DeliveryReportReq_wsAuth_ID.xml"
				
//				 "SendEmailReq.xml"
//				 "SendEmailReq_wsAuth.xml"
//				 "SendEmailReq_wsAuth_ID.xml"

//				 "SubmitSMReq.xml"
//				"SubmitSMSReq_wsAuth.xml"
//				"SubmitSMSReq_wsAuth_ID.xml";
				
				// "example.xml"
//				 "_idle_soap_request.xml"
//				 "_idle_json_request.xml"
//				 "_idle_soap_confirmOTP_request.xml"

//				 "_w_service_template_e01_response.xml"
//				"wsAuth_service_template_e01.xml"
//				"wsAuthWithID_service_template_e01.xml"
//				"new_w_service_template.xml"
				// "example.xml"
				
//				 "InquirySubscriber.xml"
//				"InquiryVASSubscriber.xml"
//				"InquiryVASSubWSAuth.xml"
//				"InquiryVASSubAuthWithID.xml"
				
				// "PortCheckResponse.xml"
				// "temp.xml"
				// "PortCheckResponse.xml"
				
//				 "_authen_otp_request.xml"
				// "_generate_passkey_request_json.xml"
//				 "_confirm_otp_json_request.xml"

						// "def_idel_generate_pk_json.xml"
//						 "def_idel_generate_pk_soap.xml"
						
//				"testAuthOTP.xml"
//				"AuthenOTPWithID.xml"
//						"temp.xml"
				// "testtemp.xml"
				

//				"temp.xml"
				};
		
			try {
				
				flow(flow);
			} catch (Exception e) {
				e.printStackTrace();
			}
	
		}
	
	private static void flow(String[] flow) throws IOException, InterruptedException, ExecutionException{
		
		
		String[] filenames = flow;
		String instance = "<EquinoxInstance><value val=\"\" />";
		String inputMessage;
		String outputMessage = null;
		int index = 1;
		for (String filename : filenames) {
			System.err.println("Round :: " + index + " Begining");
			Thread.sleep(500);
			inputMessage = getMessageFile(filename);
			if(index != 1){
				if(!filename.equals("RRR.xml")){
					inputMessage = replaceInvoke(outputMessage, inputMessage);
					inputMessage = replaceE01id(outputMessage, inputMessage);
				}
				inputMessage = replaceInstance(outputMessage, inputMessage);
			}
			outputMessage = inputServer(inputMessage, instance);
			System.err.println("Round :: " + index + " Done!!!");
			index++;
		}
		
		System.exit(0);
	}
	

	private static String getMessageFile(String filename){
		String exampleFile = examplePath + filename;
		
		String str;
		String reqMessage = "";

		BufferedReader in = null;
		try {

			/** Input **/
			in = new BufferedReader(new FileReader(exampleFile));
			while ((str = in.readLine()) != null) {
				reqMessage += (str + "\n");
			}
			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return reqMessage;
	}
	
	private static String replaceInvoke(String fromMessage, String toMessage){
		
		
		String[] baseData = fromMessage.split("<EquinoxRawData");
		for (String base : baseData) {

			String[] data = base.split(" type=\"");
			if (data.length == 1){
				continue;
			}
			String type = data[1].split("\"")[0];
			if (!"request".equalsIgnoreCase(type)){
				continue;
			}
			fromMessage = base;
			break;
		}
		
		
		int beginIndexInvoke = fromMessage.indexOf(" invoke=\"")  + " invoke=\"".length() ;
		String[] data = fromMessage.split(" invoke=\"");
		char[] buff = data[1].toCharArray();
		int endIndexInvoke = 0;
		int i =0;
		for (char c : buff) {
			if(c == '\"'){
				if(buff[i-1] != '\\'){
					endIndexInvoke = beginIndexInvoke + i;
					break;
				}
			}
			i++;
		}
		
		String invoke = fromMessage.substring(beginIndexInvoke, endIndexInvoke);
		
		beginIndexInvoke = toMessage.indexOf(" invoke=\"") + " invoke=\"".length();
		data = toMessage.split(" invoke=\"");
		buff = data[1].toCharArray();
		endIndexInvoke = 0;
		i =0;
		for (char c : buff) {
			if(c == '\"'){
				if(buff[i-1] != '\\'){
					endIndexInvoke = beginIndexInvoke + i;
					break;
				}
			}
			i++;
		}
		
		StringBuilder result = new StringBuilder();
		result.append(toMessage.substring(0, beginIndexInvoke))
		.append(invoke)
		.append(toMessage.substring(endIndexInvoke, toMessage.length()));
		
		return result.toString();
	}
	
	private static String replaceE01id(String fromMessage, String toMessage){
		
		if(fromMessage.contains(" ctype=\"db\"")){

			String[] baseData = fromMessage.split("<EquinoxRawData");
			for (String base : baseData) {

				String[] data = base.split(" type=\"");
				if (data.length == 1){
					continue;
				}
				String type = data[1].split("\"")[0];
				if (!"request".equalsIgnoreCase(type)){
					continue;
				}
				fromMessage = base;
				break;
			}
			
			
			int beginIndexInvoke = fromMessage.indexOf(" id=\"")  + " id=\"".length() ;
			String[] data = fromMessage.split(" id=\"");
			char[] buff = data[1].toCharArray();
			int endIndexInvoke = 0;
			int i =0;
			for (char c : buff) {
				if(c == '\"'){
					if(buff[i-1] != '\\'){
						endIndexInvoke = beginIndexInvoke + i;
						break;
					}
				}
				i++;
			}
			
			String invoke = fromMessage.substring(beginIndexInvoke, endIndexInvoke);
			
			beginIndexInvoke = toMessage.indexOf(" id=\"") + " id=\"".length();
			data = toMessage.split(" id=\"");
			buff = data[1].toCharArray();
			endIndexInvoke = 0;
			i =0;
			for (char c : buff) {
				if(c == '\"'){
					if(buff[i-1] != '\\'){
						endIndexInvoke = beginIndexInvoke + i;
						break;
					}
				}
				i++;
			}
			
			StringBuilder result = new StringBuilder();
			result.append(toMessage.substring(0, beginIndexInvoke))
			.append(invoke)
			.append(toMessage.substring(endIndexInvoke, toMessage.length()));
			
			return result.toString();
		}
		else{
			return toMessage;
		}
	}


	private static String replaceInstance(String fromMessage, String toMessage){
		
				int beginInstance = fromMessage.indexOf("<EquinoxInstance>");
				int endInstance = fromMessage.indexOf("</EquinoxInstance>");
				String instance = fromMessage.substring(beginInstance, endInstance);
				
		
				beginInstance = toMessage.indexOf("<EquinoxInstance>");
				endInstance = toMessage.indexOf("</EquinoxInstance>");
				
				StringBuilder incomingMessage = new StringBuilder();
				incomingMessage.append(toMessage.substring(0, beginInstance)).append(instance).append(toMessage.substring(endInstance, toMessage.length()));
				
				toMessage = incomingMessage.toString();
				
				
				return toMessage;
		
			}


	private static String inputServer(String inputMessage, String instance) throws IOException, InterruptedException,
				ExecutionException {
	
			String str;
			String conf = "";
	
			BufferedReader in = null;
			try {
	
				/** EC02 Configuration **/
				in = new BufferedReader(new FileReader(
						"./conf/GSSO.EC02.GSSO.0"));
				while ((str = in.readLine()) != null) {
					conf += str;
				}
				in.close();
	
			} catch (FileNotFoundException e) {
				e.printStackTrace();
	
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println(formatXml(inputMessage));
			
			String[] args = { "GSSO", "GSSO", "0", conf };
			EC02Server.main(args);
	
			EC02Handler handler = new EC02Handler();
			handler.verifyAFConfig(conf);
			String handlerMessage = null;
			// for (int i = 0; i < 100000; i++) {
			handlerMessage = handler.handle(inputMessage.getBytes(), 90000);
			// }
	
			/** RESULT **/
			System.err.println("Outout Message ::");
			System.out.println(formatXml(handlerMessage));
	
			/** **/
			
			return formatXml(handlerMessage);
	
		}

	public static String formatXml(String xml) {
		try {
			Transformer serializer = SAXTransformerFactory.newInstance()
					.newTransformer();
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");
			Source xmlSource = new SAXSource(new InputSource(
					new ByteArrayInputStream(xml.getBytes())));
			StreamResult res = new StreamResult(new ByteArrayOutputStream());
			serializer.transform(xmlSource, res);

			return new String(
					((ByteArrayOutputStream) res.getOutputStream())
							.toByteArray());

		} catch (Exception e) {
			return xml;
		}
	}

}
