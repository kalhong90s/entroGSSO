package com.ais.eqx.gsso.states;

import java.util.ArrayList;

import com.ais.eqx.gsso.model.GSSOHandler;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.af.interfaces.IAFState;

public class W_ACTIVE implements IAFState {

	@Override
	public String doAction(AbstractAF abstractAF, Object ec02Instance, ArrayList<EquinoxRawData> rawDatas) {
		return GSSOHandler.handle(abstractAF, ec02Instance, rawDatas);
	}

}
