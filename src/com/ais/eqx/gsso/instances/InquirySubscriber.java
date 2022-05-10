package com.ais.eqx.gsso.instances;

public class InquirySubscriber {

	InquirySubscriberResult inquirySubscriberResult;

	public InquirySubscriberResult getInquirySubscriberResult() {
		return inquirySubscriberResult;
	}

	public void setInquirySubscriberResult(InquirySubscriberResult inquirySubscriberResult) {
		this.inquirySubscriberResult = inquirySubscriberResult;
	}
	
	public InquirySubscriber() {
		inquirySubscriberResult = new InquirySubscriberResult();
	}
}
