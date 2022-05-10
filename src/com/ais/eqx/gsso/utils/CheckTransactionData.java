package com.ais.eqx.gsso.utils;

import com.ais.eqx.gsso.instances.TransactionData;

import ec02.af.utils.Log;

public class CheckTransactionData {
	
	public static boolean chkMatchTransaction(TransactionData transactionData) {
		boolean isFondTransactionID = false;
		Log.d("   Strat  chkMatchTransaction ");
		try {
			
			if (transactionData != null) {
					isFondTransactionID = true;
			}
			else {
				isFondTransactionID = false;
			}
		}
		catch (Exception e) {
			isFondTransactionID = false;
		}
		Log.d("   End  chkMatchTransaction ");
		return isFondTransactionID;
	}
	
}
