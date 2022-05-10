package com.ais.eqx.gsso.abstracts;

import java.util.ArrayList;

import com.ais.eqx.gsso.instances.EC02Instance;
import com.ais.eqx.gsso.interfaces.IAFSubState;

import ec02.af.abstracts.AbstractAF;
import ec02.af.data.EquinoxRawData;
import ec02.common.data.E01Data;

public class AbstractAFSubStateManager implements IAFSubState {

	public IAFSubState	subState;

	@Override
	public ArrayList<EquinoxRawData> doActionSubState(final AbstractAF abstractAF, final EC02Instance ec02Instance,
			final EquinoxRawData equinoxRawData) {

		return subState.doActionSubState(abstractAF, ec02Instance, equinoxRawData);
	}

	@Override
	public ArrayList<EquinoxRawData> doActionSubStateE01(final AbstractAF abstractAF, final EC02Instance ec02Instance,
			final EquinoxRawData equinoxRawData, final E01Data e01Data) {

		return subState.doActionSubStateE01(abstractAF, ec02Instance, equinoxRawData, e01Data);
	}
}
