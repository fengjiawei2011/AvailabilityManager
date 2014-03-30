package com.cmpe283.project.impl;

import java.util.HashMap;

public class AMConfig {
	public final static String UserName = "administrator";
	public final static String Password = "12!@qwQW";
	public final static String VcenterUrl = "https://130.65.132.150/sdk";
	public final static String SuperVcenterUrl = "https://130.65.132.14/sdk";
	
	public final static int NumConnectHost = 10;
	public final static int BackupInterval = 600;  //seconds
	public final static int HeartbeatInterval = 100;  //seconds
	public final static int PrintInterval = 300;  //seconds
	public final static int NumPing = 5; //seconds
	public final static int PingInterval = 3; //seconds
	
	public final static HashMap<String, String> Vhosts = new HashMap<String, String>() {

		{
			put("130.65.132.151", "68:68:84:51:62:6E:DB:D1:2E:8B:F7:35:B0:B0:0E:60:43:7F:17:43");
			put("130.65.132.155", "EE:2B:25:8F:48:6E:38:5C:B3:DD:B0:87:FD:66:AA:1B:25:DF:B9:7C");
			put("130.65.132.159", "43:51:66:B8:3C:76:F5:8F:9A:63:90:0D:13:2C:25:B8:48:64:2D:6F");
		}
	};
	
	public final static HashMap<String, String> VHosts = new HashMap<String, String>() {

		{
			put("130.65.132.151", "t03-vHost01-cum1-lab1 _.132.151");
			put("130.65.132.155", "t03-vHost01-cum1-proj1_132.155");
			put("130.65.132.159", "t03-vHost01-cum1-lab2_132.159");
		}
	};
	
	public final static String[] PerfCounters = { "cpu.usage.average",
			"cpu.usagemhz.average", "cpu.used.summation", "cpu.wait.summation",
			"mem.usage.average", "mem.overhead.average",
			"mem.consumed.average", "net.usage.average",
			"net.received.average", "net.transmitted.average",
			"disk.commands.summation", "disk.usage.average",
			"datastore.datastoreReadBytes.latest",
			"virtualDisk.readOIO.latest", "virtualDisk.writeOIO.latest" };
}
