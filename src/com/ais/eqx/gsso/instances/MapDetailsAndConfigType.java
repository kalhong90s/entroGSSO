package com.ais.eqx.gsso.instances;

import ais.mmt.sand.comlog.DetailsLogPrototype;

public class MapDetailsAndConfigType {

	private DetailsLogPrototype	detail;
	private boolean				isError	= false;

	public MapDetailsAndConfigType() {

	}

	public MapDetailsAndConfigType(DetailsLogPrototype detail, boolean isError) {
		super();
		this.detail = detail;
		this.isError = isError;
	}

	public DetailsLogPrototype getDetail() {
		return detail;
	}

	public void setDetail(DetailsLogPrototype detail) {
		this.detail = detail;
	}

	public void setNoFlow() {
		this.isError = true;
	}

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

}
