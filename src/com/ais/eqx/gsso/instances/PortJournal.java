package com.ais.eqx.gsso.instances;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Journal")
public class PortJournal {

	@XmlElement(name = "RoutingId")
	private String		routingId;

	@XmlElement(name = "SpName")
	private String		spName;

	@XmlElement(name = "OriginalSp")
	private String		originalSP;

	@XmlElement(name = "ListOfHops", type = ListOfHops.class)
	private ListOfHops	listOfHops;

	public String getRoutingId() {
		return routingId;
	}

	public void setRoutingId(String routingId) {
		this.routingId = routingId;
	}

	public String getSpName() {
		return spName;
	}

	public void setSpName(String spName) {
		this.spName = spName;
	}

	public String getOriginalSP() {
		return originalSP;
	}

	public void setOriginalSP(String originalSP) {
		this.originalSP = originalSP;
	}

	public ListOfHops getListOfHops() {
		return listOfHops;
	}

	public void setListOfHops(ListOfHops listOfHops) {
		this.listOfHops = listOfHops;
	}

}
