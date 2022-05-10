package com.ais.eqx.gsso.instances;

import java.util.ArrayList;

public class MapDestinationBean {

	private ArrayList<DestinationBean>	destinationBeanList;

	public MapDestinationBean() {
		super();
		this.destinationBeanList = new ArrayList<DestinationBean>();
	}

	public ArrayList<DestinationBean> getDestinationBeanList() {
		return destinationBeanList;
	}

	public void setDestinationBeanList(ArrayList<DestinationBean> destinationBeanList) {
		this.destinationBeanList = destinationBeanList;
	}

}
