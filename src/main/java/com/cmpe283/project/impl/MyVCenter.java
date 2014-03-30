package com.cmpe283.project.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cmpe283.project.AvailabilityManager;
import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectFault;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.InvalidLogin;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VimFault;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class MyVCenter {

	private static ServiceInstance si;
	public static final String VCENTER_CONNECTION_URL = "https://130.65.132.150/sdk";
	public static final String VCENTER_CONNECTION_USERNAME = "administrator";
	public static final String VCENTER_CONNECTION_PASSWORD = "12!@qwQW";
	public static String VHOST_ONE = "130.65.132.155";
	public static String VHOST_TWO = "130.65.132.159";
	public static String VHOST_THREE = "130.65.132.151";
	
	/* Migrate Vhost parameters*/
	public  static final String VHOST_IP = "130.65.132.159";
	public  static final String VHOST_CONNECTION_USERNAME = "root";
	public  static final String VHOST_CONNECTION_PASSWORD = "12!@qwQW";
	public  static final String VHOST_SSL_THUMBPRINT = "43:51:66:B8:3C:76:F5:8F:9A:63:90:0D:13:2C:25:B8:48:64:2D:6F";

	private static final Logger logger = Logger.getLogger(MyVCenter.class);

	public MyVCenter() {
		try {
			si = si == null?new ServiceInstance(new URL(MyVCenter.VCENTER_CONNECTION_URL), MyVCenter.VCENTER_CONNECTION_USERNAME, MyVCenter.VCENTER_CONNECTION_PASSWORD, true) : si;
		} catch (MalformedURLException  | RemoteException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static HostSystem getOriginVHost() throws InvalidProperty, RuntimeFault, RemoteException{
		return MyVCenter.getHostByIp(MyVCenter.VHOST_ONE);
	}

	public static HostSystem getMigratedVHost() throws InvalidProperty, RuntimeFault, RemoteException{
		return MyVCenter.getHostByIp(MyVCenter.VHOST_TWO);
	}
	
	public static ServiceInstance getServiceInstance() {
		return si;
	}
	
	public static HostSystem addMigratedHost() throws Exception{
		return addHost(VHOST_IP, VHOST_CONNECTION_USERNAME, VHOST_CONNECTION_PASSWORD, VHOST_SSL_THUMBPRINT);
	}

	public static HostSystem addHost(String ip, String username, String password, String sslThumbPrint) throws Exception {
		HostConnectSpec newHostConnectSpec = new HostConnectSpec();
		newHostConnectSpec.setHostName(ip);
		newHostConnectSpec.setUserName(username);
		newHostConnectSpec.setPassword(password);
		newHostConnectSpec.setSslThumbprint(sslThumbPrint);
		Datacenter dc = getDatacenter();

		System.out.println("adding host ........");
		Task addHostTask = dc.getHostFolder().addStandaloneHost_Task(newHostConnectSpec, new ComputeResourceConfigSpec(), true);
		if (addHostTask.waitForTask() == Task.SUCCESS) {
			System.out.println("add host succefully");
			return getHostByIp(VHOST_IP);
		}else{
			throw new Exception(" fail to add host !");
		}
	}

	public static void migrateAll(HostSystem hostFrom, HostSystem hostTo) throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException {

		VirtualMachine[] vms = hostFrom.getVms();
		for (VirtualMachine vm : vms) {
			AvailabilityManager.coldMigrate(si, vm, hostTo);
		}
	}
	
	
	public static  HostSystem[] getHosts()
			throws InvalidProperty, RuntimeFault, RemoteException {
		// HostSystem host = null;
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");// "130.65.132.159"

		if (hosts == null) {
			System.out.println("Cannot find the host");
			// si.getServerConnection().logout();
			return null;
		} else {
			System.out.println(" find out the host !!");
			HostSystem[] hostSystems = new HostSystem[hosts.length];
			for (int i = 0; i < hosts.length; i++) {
				// System.out.println(me.getName());
				hostSystems[i] = (HostSystem) hosts[i];
			}

			return hostSystems;
		}
	}

	public static boolean disconnectHost(String hostIp) throws RuntimeFault, RemoteException, InterruptedException {
		HostSystem host = getHostByIp(hostIp);

		Task disconTask = host.disconnectHost();

		System.out.println("disconnecting ........");
		if (disconTask.waitForTask() == Task.SUCCESS) {
			System.out.println("disconnect host successfully");
			return true;
		}
		logger.warn("disconnect host error");
		return false;
	}

	public static HostSystem getHostByIp(String hostIp) throws InvalidProperty, RuntimeFault, RemoteException {
		return (HostSystem) (new InventoryNavigator(si.getRootFolder()).searchManagedEntity("HostSystem", hostIp));
	}

	public static VirtualMachine getVMByName(String Name) throws InvalidProperty, RuntimeFault, RemoteException {

		Folder f = si.getRootFolder();
		return (VirtualMachine) new InventoryNavigator(f).searchManagedEntity("VirtualMachine", Name);
	}

	public static List<MyVM> getVMs() throws InvalidProperty, RuntimeFault, RemoteException {
		Folder vmFolder = si.getRootFolder();
		List<MyVM> vmsList = new ArrayList<MyVM>();
		ManagedEntity[] vms = new InventoryNavigator(vmFolder).searchManagedEntities("VirtualMachine");
		if (vms == null) {
			logger.warn("Cannot find VirtualMachines!");
			return null;
		}
		for (int i = 0; i < vms.length; i++) {
			MyVM myvm = new MyVM((VirtualMachine)vms[i]);
			vmsList.add(myvm);
		}
		return vmsList;
	}
	
	public static List<MyVM> getVMsByHost(HostSystem host) throws InvalidProperty, RuntimeFault, RemoteException {
		
		List<MyVM> vmsList = new ArrayList<MyVM>();
		VirtualMachine[] vms = host.getVms();
		if (vms == null) {
			logger.warn("Cannot find VirtualMachines!");
			return null;
		}
		for (int i = 0; i < vms.length; i++) {
			MyVM myvm = new MyVM((VirtualMachine)vms[i]);
			vmsList.add(myvm);
		}
		return vmsList;
	}
	
	

	public static boolean removeHost(HostSystem host) throws VimFault, RuntimeFault, RemoteException, InterruptedException {
		ComputeResource cr = (ComputeResource) host.getParent();
		Task removehost = cr.destroy_Task(); // remove error
												// Host.Inventory.RemoveHostFromCluster
		System.out.println("removing....");
		if (removehost.waitForTask() == Task.SUCCESS) {
			System.out.println("remove host success");
			return true;
		}
		logger.warn("remove host error");
		return false;
	}
	
	public static Datacenter getDatacenter() throws InvalidProperty, RuntimeFault, RemoteException {
		Folder rootFolder = si.getRootFolder();
		Datacenter dc = null;
		ManagedEntity[] machines = rootFolder.getChildEntity();
		if (machines != null || machines.length != 0) {
			for (int i = 0; i < machines.length; i++) {
				if (machines[i] instanceof Datacenter) {
					dc = (Datacenter) machines[i];
				}
			}
		}
		return dc;
	}
	
	public static HostSystem getVhostByName(String hostName) throws Exception {
		Folder vmFolder = si.getRootFolder();
		HostSystem host =
				(HostSystem) new InventoryNavigator(vmFolder).searchManagedEntity("HostSystem", hostName);
		if (host == null)
			throw new Exception("host is null");

		return host;
	}

	public static List<HostSystem> getVhosts() throws InvalidProperty, RuntimeFault, RemoteException {
		List<HostSystem> hosts = new ArrayList<HostSystem>();

		Folder vmFolder = si.getRootFolder();
		ManagedEntity[] entities =
				new InventoryNavigator(vmFolder).searchManagedEntities("HostSystem");
		if (entities != null) {
			for (ManagedEntity entity : entities) {
				hosts.add((HostSystem) entity);
			}
		} else {
			logger.warn("Found No Vhost!");
		}

		return hosts;
	}
	
	public static HostSystem getVhostByVM(VirtualMachine vm) {

		try {
			List<HostSystem> hosts = getVhosts();

			for (HostSystem host : hosts) {
				if (host.getMOR().val.equals(vm.getSummary().runtime.host.val)) {
					return host;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
		}

		return null;
	}

	public static boolean isHostAvailable(HostSystem host){
		ManagedEntityStatus hostStatus = host.getOverallStatus();

		System.out.println(String.format("VHost %s: Overall Status: %s", host.getName(), hostStatus));

		return hostStatus == ManagedEntityStatus.green;
	}

}
