package com.cmpe283.project.impl;

import com.vmware.vim25.mo.HostSystem;


public class MyVHost {
	
	public  static final String VHOST_IP = "130.65.132.159";
	public  static final String VHOST_CONNECTION_USERNAME = "root";
	public  static final String VHOST_CONNECTION_PASSWORD = "12!@qwQW";
	public  static final String VHOST_SSL_THUMBPRINT = "43:51:66:B8:3C:76:F5:8F:9A:63:90:0D:13:2C:25:B8:48:64:2D:6F";
	
	
	public static String getVHostIP(HostSystem host){
		return host.getConfig().getNetwork().getVnic()[0].getSpec().getIp().getIpAddress();
	}
	
}
