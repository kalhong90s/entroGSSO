package com.ais.eqx.gsso.substates;

import java.util.ArrayList;

import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.interfaces.IAFSubState;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.common.data.E01Data;

public class END implements IAFSubState {

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData) {
		return new ArrayList<EquinoxRawData>();
	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data) {
		return null;
	}

}
