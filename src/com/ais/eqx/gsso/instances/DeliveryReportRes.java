package com.ais.eqx.gsso.instances;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import com.ais.eqx.gsso.interfaces.EventAction;
import com.ais.eqx.gsso.jaxb.InstanceContext;
import com.ais.eqx.gsso.jaxb.JAXBHandler;

import ec02.af.data.EquinoxRawData;

@XmlRootElement(name = "mandatory")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeliveryReportRes {

	@XmlPath("message_id/@value")
	private String			messageId;

	private EquinoxRawData	equinoxRawData;

	public DeliveryReportRes() {
	}

	public DeliveryReportRes(final EquinoxRawData equinoxRawData) {
		setEquinoxRawData(equinoxRawData);

	}

	public EquinoxRawData getEquinoxRawData() {
		return equinoxRawData;
	}

	public void setEquinoxRawData(final EquinoxRawData equinoxRawData) {
		this.equinoxRawData = equinoxRawData;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(final String messageId) {
		this.messageId = messageId;
	}

	public EquinoxRawData toRawDatas(final String messageId) {
		DeliveryReportRes deliveryReportRes = new DeliveryReportRes();
		deliveryReportRes.setMessageId(messageId);
		String massageOut = JAXBHandler.composeMessage(InstanceContext.getDeliveryReportResContext(), deliveryReportRes);

		HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put("name", "SMPP");
		attrs.put("ctype", "deliver_sm");
		attrs.put("type", EventAction.RESPONSE);
		attrs.put("to", this.getEquinoxRawData().getOrig());
		EquinoxRawData eRawdataOut = new EquinoxRawData();
		eRawdataOut.setRawDataAttributes(attrs);
		eRawdataOut.setInvoke(this.getEquinoxRawData().getInvoke());
		eRawdataOut.setRawMessage(massageOut);
		return eRawdataOut;
	}

}
