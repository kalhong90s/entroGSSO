package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PortcheckResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class PortCheckResponse {

	@XmlElement(name = "OperationStatus", type = PortCheckOperationStatus.class)
	private PortCheckOperationStatus	operationStatus;

	@XmlElement(name = "Journal", type = PortJournal.class)
	private PortJournal					portJournal;

	public PortCheckOperationStatus getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(final PortCheckOperationStatus operationStatus) {
		this.operationStatus = operationStatus;
	}

	public PortJournal getPortJournal() {
		return portJournal;
	}

	public void setPortJournal(final PortJournal portJournal) {
		this.portJournal = portJournal;
	}

}
