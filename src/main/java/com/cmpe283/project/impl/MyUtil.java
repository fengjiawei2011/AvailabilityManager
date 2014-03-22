package com.cmpe283.project.impl;

public class MyUtil {

	public static boolean pingHost(String host) {
		try {
			String cmd = "";
			if (System.getProperty("os.name").startsWith("Windows")) {
				// For Windows
				cmd = "ping -n 1 " + host;
			} else {
				// For Linux and OSX
				cmd = "ping -c 1 " + host;
			}

			Process myProcess = Runtime.getRuntime().exec(cmd);
			myProcess.waitFor();

			if (myProcess.exitValue() == 0) {
				return true;
			} else {

				return false;
			}

		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}
	}

	
}
