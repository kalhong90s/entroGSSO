package com.ais.eqx.gsso.utils;

import java.util.ArrayList;

public class GssoServiceManagement {

	public static boolean containService(ArrayList<String> listConfigService, String orig) {
		boolean isFound = false;
		String splitConfigService = null;
		String serviceIncoming = null;

		try {
			String[] service = orig.trim().split("\\.");
			serviceIncoming = service[0] + "." + service[1] + "." + service[2];
		}
		catch (Exception e) {
			serviceIncoming = orig;
		}

		for (String configService : listConfigService) {

			try {
				String[] serviceFromConfig = configService.trim().split("\\.");
				splitConfigService = serviceFromConfig[0] + "." + serviceFromConfig[1] + "." + serviceFromConfig[2];
			}
			catch (Exception e) {
				splitConfigService = configService;
			}

			if (splitConfigService.equals(serviceIncoming)) {
				isFound = true;
				break;
			}
		}
		return isFound;
	}

}
