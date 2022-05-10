package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//Old code
//@XmlRootElement(name = "InquirySubscriberResult")

//Newcode
@XmlRootElement(name = "InquiryVASSubscriberResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class InquiryVASSubscriberResult {

	@XmlElement(name = "OperationStatus", type = OperationStatusOfVas.class)
	private OperationStatusOfVas	operationStatus;

	@XmlElement(name = "Subscriber", type = SubscriberOfVas.class)
	private SubscriberOfVas		subscriber;

	public OperationStatusOfVas getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(final OperationStatusOfVas operationStatus) {
		this.operationStatus = operationStatus;
	}

	public SubscriberOfVas getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(final SubscriberOfVas subscriber) {
		this.subscriber = subscriber;
	}

}
