package com.ais.eqx.gsso.instances;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ListOfHops")
public class ListOfHops {

	@XmlElement(name = "ListOfHop", type = ListOfHop.class)
	private ArrayList<ListOfHop>	listOfHop;

	public ArrayList<ListOfHop> getListOfHop() {
		return listOfHop;
	}

	public void setListOfHop(ArrayList<ListOfHop> listOfHop) {
		this.listOfHop = listOfHop;
	}

}
