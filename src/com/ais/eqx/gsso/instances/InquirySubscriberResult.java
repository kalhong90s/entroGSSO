package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//Old code
//@XmlRootElement(name = "InquirySubscriberResult")

//Newcode
@XmlRootElement(name = "InquirySubscriberResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class InquirySubscriberResult {

	@XmlElement(name = "OperationStatus", type = OperationStatusOfSub.class)
	private OperationStatusOfSub	operationStatus;

	@XmlElement(name = "Subscriber", type = SubscriberOfSub.class)
	private SubscriberOfSub		subscriber;

	public OperationStatusOfSub getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(final OperationStatusOfSub operationStatus) {
		this.operationStatus = operationStatus;
	}

	public SubscriberOfSub getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(final SubscriberOfSub subscriber) {
		this.subscriber = subscriber;
	}

}
