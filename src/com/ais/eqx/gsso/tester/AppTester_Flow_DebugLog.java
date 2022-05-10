package com.ais.eqx.gsso.tester;

import ec02.server.EC02Handler;
import ec02.server.EC02Server;
import org.xml.sax.InputSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;

public class AppTester_Flow_DebugLog {

	final static String             incomingMessage =               "./example.msg/debug.log";
	final static String             configEC02 =                    "./conf/GSSO.EC02.GSSO.0";
	final static String[]           app =                           {"GSSO", "GSSO", "0"};
	final static boolean            replaceInvokeAndInstance =      false;
	final static boolean            replaceOTP =                    true;
	final static boolean            replaceLog =                    true;


	public static void main(String[] args) throws IOException, InterruptedException {

		//## 01 Delete Old Log
		if(replaceLog) {
			String logName = app[0] + ".EC02LIB." + app[1] + "." + app[2] + ".log";  //aaa2.EC02LIB.aaa.0.log";
			File file = new File("./log/" + logName);
			System.err.println("Delete file " + logName + " : " + file.delete());
		}
		//---------------------------------------------------------------------------


		//## 02 Read incomingMessage
		BufferedReader in = new BufferedReader(new FileReader(incomingMessage));
		ArrayList<String> listOfMessages = splitIncomingMessage(in);
		in.close();
		//---------------------------------------------------------------------------


		//## 03 Read config
		String conf = "";
		String temp;
		in = new BufferedReader(new FileReader(configEC02));
		while ((temp = in.readLine()) != null) { conf += temp; }
		in.close();
		//---------------------------------------------------------------------------


		//## 04 Run
		String[] arg = {app[0], app[1], app[2], conf};
		EC02Server.main(arg);
		EC02Handler handler = new EC02Handler();
		handler.verifyAFConfig(conf);
		int index = 1;
		String outputMessage = "";

		for (String inputMessage : listOfMessages) {
			System.err.println("Round [" + index + "] Beginning");
			if (index !=1 && !inputMessage.contains("sendOneTimePW")) {
				if (replaceInvokeAndInstance) {
					inputMessage = replaceInvoke(outputMessage, inputMessage);
					inputMessage = replaceInstance(outputMessage, inputMessage);
					inputMessage = replaceE01id(outputMessage, inputMessage);
				}
				if (replaceOTP) {
					inputMessage = replaceValueOTPJsnon(outputMessage, inputMessage, "transactionID&quot;:&quot;");
					inputMessage = replaceValueOTPJsnon(outputMessage, inputMessage, "oneTimePassword&quot;:&quot;");
					inputMessage = replaceValueOTPSoap(outputMessage, inputMessage, "<transactionID>","</transactionID>");
					inputMessage = replaceValueOTPSoap(outputMessage, inputMessage, "<oneTimePassword>","</oneTimePassword>");
				}

			}
			String outGoing = handler.handle(inputMessage.getBytes(), 90000);
			if(outGoing.contains("invoke=\"")){
				outputMessage =outGoing;
			}
			System.err.println("#====> Incoming Message :" + inputMessage);
			System.err.println("#====> Outgoing Message :");
			System.out.println(formatXml(outGoing));
			Thread.sleep(200);
//            System.err.println("Round :: " + index + " Done!!!");
			index++;
		}
		System.exit(0);

	}


	private static String replaceInvoke(String fromMessage, String toMessage) {


		String[] baseData = fromMessage.split("<EquinoxRawData");
		for (String base : baseData) {

			String[] data = base.split(" type=\"");
			if (data.length == 1) {
				continue;
			}
			String type = data[1].split("\"")[0];
			if (!"request".equalsIgnoreCase(type)) {
				continue;
			}
			fromMessage = base;
			break;
		}


		int beginIndexInvoke = fromMessage.indexOf(" invoke=\"") + " invoke=\"".length();
		String[] data = fromMessage.split(" invoke=\"");
		char[] buff = data[1].toCharArray();
		int endIndexInvoke = 0;
		int i = 0;
		for (char c : buff) {
			if (c == '\"') {
				if (buff[i - 1] != '\\') {
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
		i = 0;
		for (char c : buff) {
			if (c == '\"') {
				if (buff[i - 1] != '\\') {
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

	private static String replaceE01id(String fromMessage, String toMessage) {

		if (fromMessage.contains(" ctype=\"db\"")) {

			String[] baseData = fromMessage.split("<EquinoxRawData");
			for (String base : baseData) {

				String[] data = base.split(" type=\"");
				if (data.length == 1) {
					continue;
				}
				String type = data[1].split("\"")[0];
				if (!"request".equalsIgnoreCase(type)) {
					continue;
				}
				fromMessage = base;
				break;
			}


			int beginIndexInvoke = fromMessage.indexOf(" id=\"") + " id=\"".length();
			String[] data = fromMessage.split(" id=\"");
			char[] buff = data[1].toCharArray();
			int endIndexInvoke = 0;
			int i = 0;
			for (char c : buff) {
				if (c == '\"') {
					if (buff[i - 1] != '\\') {
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
			i = 0;
			for (char c : buff) {
				if (c == '\"') {
					if (buff[i - 1] != '\\') {
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
		} else {
			return toMessage;
		}
	}

	private static String replaceInstance(String fromMessage, String toMessage) {

		int beginInstance = fromMessage.indexOf("<EquinoxInstance>");
		int endInstance = fromMessage.indexOf("</EquinoxInstance>");
		String instance = fromMessage.substring(beginInstance, endInstance);


		beginInstance = toMessage.indexOf("<EquinoxInstance>");
		endInstance = toMessage.indexOf("</EquinoxInstance>");

		toMessage = toMessage.substring(0, beginInstance) + instance + toMessage.substring(endInstance);


		return toMessage;

	}

	private static String replaceValueOTPJsnon(String fromMessage, String toMessage,String value){
		String[] baseData = fromMessage.split("<EquinoxRawData");
		for (String base : baseData) {
			String[] data = base.split(" type=\"");
			if (data.length == 1){
				continue;
			}
			if (!base.contains("val")){
				continue;
			}
			fromMessage = base;
			break;
		}
		if(fromMessage.contains(value)) {
			String[] data = fromMessage.split(value);
			int endIndex = data[1].indexOf("&quot;");
			String temp = data[1].substring(0, endIndex);
			if (value.contains("transactionID")) {
				return   toMessage.replace("$transactionID", temp);
			} else {
				return toMessage.replace("$pwd", temp);
			}
		}
		return toMessage;
	}
	private static String replaceValueOTPSoap(String fromMessage, String toMessage,String font,String end){
		String[] baseData = fromMessage.split("<EquinoxRawData");
		for (String base : baseData) {
			String[] data = base.split(" type=\"");
			if (data.length == 1){
				continue;
			}
			if (!base.contains("<return>")){
				continue;
			}
			fromMessage = base;
			break;
		}
		if(fromMessage.contains(font)) {
			String[] data = fromMessage.split(font);
			int endIndex = data[1].indexOf(end);
			String temp = data[1].substring(0, endIndex);
			if (font.contains("transactionID")) {
				return   toMessage.replace("$transactionID", temp);
			} else {
				return toMessage.replace("$pwd", temp);
			}
		}
		return toMessage;
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

			return new String(((ByteArrayOutputStream) res.getOutputStream()).toByteArray());

		} catch (Exception e) {
			return xml;
		}
	}


	public static ArrayList<String> splitIncomingMessage(BufferedReader incomingMsg) throws IOException {
		ArrayList<String> listOfMessages = new ArrayList<String>();
		String str;
		StringBuilder reqMessage = new StringBuilder();

		FileWriter myWriter = new FileWriter("./example.msg/IncomingMessage.xml");
		boolean writer = false;
		while ((str = incomingMsg.readLine()) != null) {
			if (!str.startsWith("DEBUG") && !str.startsWith("ADEBUG") && !str.startsWith("-->") && !str.startsWith("    -->") && !str.startsWith("} ") && !str.startsWith("#====> Outgoing Message is:") && !str.equals("") && !str.startsWith("====") && !str.startsWith("****")) {

//				reqMessage += str;

				// filter incomingMassage form Log
				if (str.contains("#====> Incoming Message :")) {
					writer = true;
				} else if (str.contains("<?xml version=\"1.0\" encoding=\"tis-620\" ?>")) {
					str = "#====> Incoming Message :" + str;
					writer = true;
				}
				if(writer){
					reqMessage.append(str);
					myWriter.write(str+"\n");
				}
				if(str.equals("</EquinoxMessage>")){myWriter.write("\n\n\n\n"); writer = false; }
			}
		}
		myWriter.close();


		String[] splitIncomingMsg = reqMessage.toString().split("#====> Incoming Message :");
		for (String in : splitIncomingMsg) {
			if (in.startsWith("<?xml")) {
				String msg = in.substring(0, in.indexOf("</EquinoxMessage>") + 17);
				listOfMessages.add(msg);
			}
		}

		return listOfMessages;
	}

}
