package com.ais.eqx.gsso.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.ais.eqx.gsso.instances.DeliveryReportRequest;
import com.ais.eqx.gsso.instances.DeliveryReportRes;
import com.ais.eqx.gsso.instances.GenPasskey;
import com.ais.eqx.gsso.instances.InquirySubscriberResult;
import com.ais.eqx.gsso.instances.InquiryVASSubscriberResult;
import com.ais.eqx.gsso.instances.PortCheckResponse;
import com.ais.eqx.gsso.instances.SendConfirmOTPRequest;
import com.ais.eqx.gsso.instances.SendOneTimePWRequest;
import com.ais.eqx.gsso.instances.SendWSConfirmOTPRequest;
import com.ais.eqx.gsso.instances.SendWSConfirmOTPWithIDRequest;
import com.ais.eqx.gsso.instances.SendWSOTPRequest;
import com.ais.eqx.gsso.instances.SubmitSMResponse;
import com.google.gson.Gson;

public class InstanceContext {

	private static JAXBContext	deliveryReportResContext;
	private static JAXBContext	submitSMResponseContext;
	private static JAXBContext	deliveryReportRequestContext;

	private static JAXBContext	sendOneTimePWRequest;
	private static JAXBContext	confirmOneTimePWRequest;
	private static JAXBContext	generatePasskeyRequest;
	private static JAXBContext	portCheckResponse;
	private static JAXBContext	inquiryVASSubscriberResponse;
	private static JAXBContext	inquirySubscriberResponse;
	
	// New Interface WS
//	private static JAXBContext sendWSAuthOTPRequest;
//	private static JAXBContext sendWSCreateOTPRequest;
//	private static JAXBContext sendWSGenerateOTPRequest;
	private static JAXBContext sendWSConfirmOTPRequest;
//	private static JAXBContext sendWSAuthOTPWithIDRequest;
	private static JAXBContext sendWSConfirmOTPWithIDRequest;
	private static JAXBContext sendWSOTPRequest;

	private static Gson			gson;

	// initGson
	public static synchronized void initGson() throws JAXBException {
		if (gson == null)
			gson = new Gson();
	}

	public static Gson getGson() {
		return gson;
	}

	// initDeliveryReportResContext
	public static synchronized void initDeliveryReportResContext() throws JAXBException {
		if (deliveryReportResContext == null)
			deliveryReportResContext = JAXBContext.newInstance(DeliveryReportRes.class);
	}

	public static JAXBContext getDeliveryReportResContext() {
		return deliveryReportResContext;
	}

	// initSubmitSMResponseContext
	public static synchronized void initSubmitSMResponseContext() throws JAXBException {
		if (submitSMResponseContext == null)
			submitSMResponseContext = JAXBContext.newInstance(SubmitSMResponse.class);
	}

	public static JAXBContext getSubmitSMResponseContext() {
		return submitSMResponseContext;
	}

	// initDeliveryReportRequestContext
	public static synchronized void initDeliveryReportRequestContext() throws JAXBException {
		if (deliveryReportRequestContext == null)
			deliveryReportRequestContext = JAXBContext.newInstance(DeliveryReportRequest.class);
	}

	public static JAXBContext getDeliveryReportRequestContext() {
		return deliveryReportRequestContext;
	}

	// initSendOneTimePWRequestContext
	public static synchronized void initSendOneTimePWRequestContext() throws JAXBException {
		if (sendOneTimePWRequest == null)
			sendOneTimePWRequest = JAXBContext.newInstance(SendOneTimePWRequest.class);
	}

	public static JAXBContext getSendOneTimePWRequestContext() {
		return sendOneTimePWRequest;
	}

	// initConfirmOneTimePWRequestContext
	public static synchronized void initConfirmOneTimePWRequestContext() throws JAXBException {
		if (confirmOneTimePWRequest == null)
			confirmOneTimePWRequest = JAXBContext.newInstance(SendConfirmOTPRequest.class);
	}

	public static JAXBContext getConfirmOneTimePWRequestContext() {
		return confirmOneTimePWRequest;
	}

	// initGeneratePasskeyRequestContexts
	public static synchronized void initGeneratePasskeyRequestContext() throws JAXBException {
		if (generatePasskeyRequest == null)
			generatePasskeyRequest = JAXBContext.newInstance(GenPasskey.class);
	}

	public static JAXBContext getGeneratePasskeyRequestContext() {
		return generatePasskeyRequest;
	}

	// initPortCheckResponseContext
	public static synchronized void initPortCheckResponseContext() throws JAXBException {
		if (portCheckResponse == null)
			portCheckResponse = JAXBContext.newInstance(PortCheckResponse.class);
	}

	public static JAXBContext getPortCheckResponseContext() {
		return portCheckResponse;
	}

	// initInquiryVASSubscriberResponseContext
	public static synchronized void initInquiryVASSubscriberResponseContext() throws JAXBException {
		if (inquiryVASSubscriberResponse == null)
			inquiryVASSubscriberResponse = JAXBContext.newInstance(InquiryVASSubscriberResult.class);
	}

	public static JAXBContext getInquiryVASSubscriberResponseContext() {
		return inquiryVASSubscriberResponse;
	}

	// initInquirySubscriberResponseContext
	public static synchronized void initInquirySubscriberResponseContext() throws JAXBException {
		if (inquirySubscriberResponse == null)
			inquirySubscriberResponse = JAXBContext.newInstance(InquirySubscriberResult.class);
	}

	public static JAXBContext getInquirySubscriberResponseContext() {
		return inquirySubscriberResponse;
	}
	
	// initSendWSConfirmOTPRequestContext
	public static synchronized void initSendWSConfirmOTPRequestContext() throws JAXBException {
		if (sendWSConfirmOTPRequest == null)
			sendWSConfirmOTPRequest = JAXBContext.newInstance(SendWSConfirmOTPRequest.class);
	}

	public static JAXBContext getSendWSConfirmOTPRequestContext() {
		return sendWSConfirmOTPRequest;
	}
	
	// initSendWSConfirmOTPWithIDRequestContext
	public static synchronized void initSendWSConfirmOTPWithIDRequestContext() throws JAXBException {
		if (sendWSConfirmOTPWithIDRequest == null)
			sendWSConfirmOTPWithIDRequest = JAXBContext.newInstance(SendWSConfirmOTPWithIDRequest.class);
	}

	public static JAXBContext getSendWSConfirmOTPWithIDRequestContext() {
		return sendWSConfirmOTPWithIDRequest;
	}
	
	// initWSSendOTPContext
	public static synchronized void initSendWSOTPRequestContext() throws JAXBException {
		if (sendWSOTPRequest == null)
			sendWSOTPRequest = JAXBContext.newInstance(SendWSOTPRequest.class);
	}

	public static JAXBContext getSendWSOTPRequestContext() {
		return sendWSOTPRequest;
	}

}
