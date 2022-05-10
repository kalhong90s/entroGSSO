package com.ais.eqx.gsso.interfaces;

import java.util.ArrayList;

import com.ais.eqx.gsso.instances.EC02Instance;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.common.data.E01Data;

public interface IAFSubState {

	public ArrayList<EquinoxRawData> doActionSubState(AbstractAF abstractAF, EC02Instance ec02Instance, EquinoxRawData equinoxRawData);

	public ArrayList<EquinoxRawData> doActionSubStateE01(AbstractAF abstractAF, EC02Instance ec02Instance,
			EquinoxRawData equinoxRawData, E01Data e01Data);

}
