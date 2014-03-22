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

	private static final Logger logger = Logger.getLogger(MyVCenter.class);

	public MyVCenter() throws MalformedURLException, RemoteException {
		URL url = new URL(VCENTER_CONNECTION_URL);
		si = new ServiceInstance(url, MyVCenter.VCENTER_CONNECTION_USERNAME, MyVCenter.VCENTER_CONNECTION_PASSWORD);
	}

	public static ServiceInstance getServiceInstance() {
		return si;
	}

	public boolean addHost(String id, String username, String password, String sslThumbPrint) throws InvalidLogin, HostConnectFault, RuntimeFault, InvalidProperty, RemoteException, InterruptedException {
		HostConnectSpec newHostConnectSpec = new HostConnectSpec();
		newHostConnectSpec.setHostName(id);
		newHostConnectSpec.setUserName(username);
		newHostConnectSpec.setPassword(password);
		newHostConnectSpec.setSslThumbprint(sslThumbPrint);
		Datacenter dc = getDatacenter();

		System.out.println("adding host ........");
		Task addHostTask = dc.getHostFolder().addStandaloneHost_Task(newHostConnectSpec, new ComputeResourceConfigSpec(), true);
		if (addHostTask.waitForTask() == Task.SUCCESS) {
			System.out.println("add host succefully");
			return true;
		}
		logger.warn("add host error");
		return false;
	}

	public  void migrateAll() throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException {
		HostSystem[] hosts = getHosts();
		VirtualMachine[] vms = hosts[0].getVms();
		for (VirtualMachine vm : vms) {
			AvailabilityManager.coldMigrate(si, vm, hosts[1]);
		}
	}
	
	
	public  HostSystem[] getHosts()
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

	public boolean disconnectHost(String hostIp) throws RuntimeFault, RemoteException, InterruptedException {
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

	public HostSystem getHostByIp(String hostIp) throws InvalidProperty, RuntimeFault, RemoteException {
		return (HostSystem) (new InventoryNavigator(si.getRootFolder()).searchManagedEntity("HostSystem", hostIp));
	}

	public VirtualMachine getVMByName(String Name) throws InvalidProperty, RuntimeFault, RemoteException {

		Folder f = si.getRootFolder();
		return (VirtualMachine) new InventoryNavigator(f).searchManagedEntity("VirtualMachine", Name);
	}

	public List<MyVM> getVMs() throws InvalidProperty, RuntimeFault, RemoteException {
		Folder vmFolder = si.getRootFolder();
		List<MyVM> vmsList = new ArrayList<MyVM>();
		ManagedEntity[] vms = new InventoryNavigator(vmFolder).searchManagedEntities("VirtualMachine");
		if (vms == null) {
			logger.warn("Cannot find VirtualMachines!");
			return null;
		}
		for (int i = 0; i < vms.length; i++) {
			MyVM myvm = new MyVM((VirtualMachine)vms[0]);
			vmsList.add(myvm);
		}
		return vmsList;
	}
	
	public List<MyVM> getVMsByHost(HostSystem host) throws InvalidProperty, RuntimeFault, RemoteException {
		
		List<MyVM> vmsList = new ArrayList<MyVM>();
		VirtualMachine[] vms = host.getVms();
		if (vms == null) {
			logger.warn("Cannot find VirtualMachines!");
			return null;
		}
		for (int i = 0; i < vms.length; i++) {
			MyVM myvm = new MyVM((VirtualMachine)vms[0]);
			vmsList.add(myvm);
		}
		return vmsList;
	}
	
	

	public boolean removeHost(HostSystem host) throws VimFault, RuntimeFault, RemoteException, InterruptedException {
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

	public Datacenter getDatacenter() throws InvalidProperty, RuntimeFault, RemoteException {
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

}
