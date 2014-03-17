package com.cmpe283.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyPing {

	public static void ping1(String hostn) throws IOException {
		final InetAddress host = InetAddress.getByName(hostn);
		System.out.println("host.isReachable(1000) = " + host.isReachable(1000));

	}

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

	public static boolean isReachablebyPing(String ip) {

		try {
			String command;

			if (System.getProperty("os.name").toLowerCase()
					.startsWith("windows")) {
				// For Windows
				command = "ping -n 2 " + ip;
			} else {
				// For Linux and OSX
				command = "ping -c 2 " + ip;
			}

			Process proc = Runtime.getRuntime().exec(command);
			StreamGobbler outputGobbler = new StreamGobbler(
					proc.getInputStream(), "OUTPUT");
			outputGobbler.start();

			proc.waitFor();
			return checkAvailability(outputGobbler.getOutputLines());

		} catch (IOException | InterruptedException ex) {
			Logger.getLogger(StreamGobbler.class.getName()).log(Level.SEVERE,
					null, ex);
		}

		return false;
	}

	private static boolean checkAvailability(List<String> outputLines) {

		for (String line : outputLines) {
			if (line.contains("unreachable")) {
				return false;
			}
			if (line.contains("TTL=")) {
				return true;
			}
		}
		return false;

	}
}


class StreamGobbler extends Thread {

	protected InputStream is;

	protected String type;

	protected List<String> outputLines;

	StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
		outputLines = new ArrayList<String>();
	}

	public List<String> getOutputLines() {
		return outputLines;
	}

	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				outputLines.add(line);
			}
		} catch (IOException ex) {
			Logger.getLogger(StreamGobbler.class.getName()).log(Level.SEVERE,
					null, ex);
		}

	}
}
