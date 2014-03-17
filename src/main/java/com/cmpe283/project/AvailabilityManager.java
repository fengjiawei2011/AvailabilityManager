package com.cmpe283.project;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.tempuri.Service;
import org.tempuri.ServiceSoap;

import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class AvailabilityManager {
	private static final String URL = "https://130.65.132.150/sdk";
	private static final String USERNAME = "administrator";
	private static final String PASSWORD = "12!@qwQW";

	private static final Logger logger = Logger
			.getLogger(AvailabilityManager.class.getName());

	public static void main(String[] args) throws MalformedURLException,
			RemoteException, InterruptedException {
		// TODO Auto-generated method stub
		logger.log(Level.FINE, "logger test");
		URL url = new URL(AvailabilityManager.URL);
		ServiceInstance si = new ServiceInstance(url,
				AvailabilityManager.USERNAME, AvailabilityManager.PASSWORD,
				true);
		
		

		//HostSystem[] hosts = AvailabilityManager.getHosts(si);
		//AvailabilityManager.checkruning(si);
		//AvailabilityManager.run(si);
		//ping(lanVM.getGuest().getIpAddress());
		
		si.getServerConnection().logout();

	}
	
	public static void checkruning(ServiceInstance si) throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException{
		
		VirtualMachine[] vms = AvailabilityManager.getVMs(si);
		while(true){
			for(VirtualMachine vm: vms){
				if(vm.getGuest().guestState.equals("notRunning"))
					System.out.println(vm.getName() + " " + vm.getGuest().guestState);
			}
			System.out.println();
			Thread.sleep(2000);
		}
	}
	
	public static VirtualMachine getVM(ServiceInstance si, String hostName) throws InvalidProperty, RuntimeFault, RemoteException{
		
		Folder f = si.getRootFolder();
		return (VirtualMachine) new InventoryNavigator(f).searchManagedEntity("VirtualMachine", hostName);
	}
	
	
	
	public static void run(ServiceInstance si) throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException{
		IPContainer ipc = new IPContainer(si);
		VirtualMachine[] vms = AvailabilityManager.getVMs(si);
		int i = 0;
		while (i != 5) {
			i++;
			for (VirtualMachine vm : vms) {
				// Printer.printVmName(vm);

				if (vm.getName().equals("T03-VM01C-Win-Jiawei")
						|| vm.getName().equals("T03-VM01C-Win-Yang")
						//|| vm.getName().equals("T03-VM02-Lin-Ling")
						//|| vm.getName().equals("T03-VM03-Lin-Ruiyun")
						//|| vm.getName().equals("T03-VM02-Lin-Lan")
						) { 
					    											
					continue;
				}
				setSelectedVM(vm);
				if (!AvailabilityManager.isVmPoweredOn(vm)) {
					AvailabilityManager.powerOnVm(vm);
					// setSelectedVM(vm);
				}

				while (vm.getGuest().guestState.equals("notRunning")) {
					System.out.println(vm.getName() + "  "
							+ vm.getGuest().guestState);
					Thread.sleep(2000);
				}
				System.out.println("---- OS is " + vm.getGuest().guestState
						+ "-----");
				if (!AvailabilityManager.hasIpAddress(vm)) {
					String ip = ipc.getIP();
					vm.getGuest().setIpAddress(ip);
					System.out.println("set ip " + ip + " for " + vm.getName());

				}else{
					System.out.println(vm.getName() + " has ip address already : " + vm.getGuest().getIpAddress());
				}

				while (!MyPing.pingHost(vm.getGuest().getIpAddress())) {
					System.out.println("Ping " + vm.getGuest().getIpAddress()
							+ " Result : "
							+ MyPing.pingHost(vm.getGuest().getIpAddress()));
					Thread.sleep(2000);
				}

				
				System.out.println("Ping " + vm.getGuest().getIpAddress()
						+ " Result : "
						+ MyPing.pingHost(vm.getGuest().getIpAddress()));
				
				System.out.println(vm.getName() + " is powering off!");
				vm.powerOffVM_Task();
				i++;
			}
		}
	}

	public static void setSelectedVM(VirtualMachine vm) {
		System.out.println("Name : " + vm.getName());
		System.out.println("Guest OS:"
				+ vm.getSummary().getConfig().guestFullName);
		System.out.println("VM Version:" + vm.getConfig().version);
		System.out.println("CPU:" + vm.getConfig().getHardware().numCPU
				+ " vCPU");
		System.out.println("Memory:" + vm.getConfig().getHardware().memoryMB
				+ " MB");
		System.out.println("Memory Overhead:"
				+ (long) vm.getConfig().memoryAllocation.reservation / 1000000f
				+ " MB");
		System.out.println("VMware Tools:" + vm.getGuest().toolsRunningStatus);
		System.out.println("IP Addresses:"
				+ vm.getSummary().getGuest().getIpAddress());
		System.out.println("State:" + vm.getGuest().guestState);
		System.out.println();
	}

	public static void powerOnVm(VirtualMachine vm) throws VmConfigFault,
			TaskInProgress, FileFault, InvalidState,
			InsufficientResourcesFault, RuntimeFault, RemoteException,
			InterruptedException {
		Task task = vm.powerOnVM_Task(null);
		System.out.println("VirtualMachine " + vm.getName()
				+ " now is powerring on! Please wait...");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("VirtualMachine " + vm.getName()
					+ " is powered on successfully!");
		}
	}

	public static boolean hasIpAddress(VirtualMachine vm) {
		return vm.getSummary().getGuest().getIpAddress() == null ? false : true;
	}

	public static void powerOffVm(VirtualMachine vm) throws VmConfigFault,
			TaskInProgress, FileFault, InvalidState,
			InsufficientResourcesFault, RuntimeFault, RemoteException,
			InterruptedException {
		Task task = vm.powerOffVM_Task();
		System.out.println("VirtualMachine " + vm.getName()
				+ " now is powerring off! Please wait...");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("VirtualMachine " + vm.getName()
					+ " is powered off successfully!");
		}
	}

	public static boolean isVmPoweredOn(VirtualMachine vm) {
		VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
		if (vmps == VirtualMachinePowerState.poweredOn) {
			System.out.println("VirtualMachine " + vm.getName()
					+ " has been powered on!");
			return true;
		}
		System.out.println("VirtualMachine " + vm.getName()
				+ " now is NOT powered on! Please Power on it!");
		return false;
	}

	public static boolean isVmPoweredOff(VirtualMachine vm) {
		VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
		if (vmps == VirtualMachinePowerState.poweredOff) {
			System.out.println("VirtualMachine " + vm.getName()
					+ " has been powered off!");
			return true;
		}
		// System.out.println("VirtualMachine "+vm.getName()+" is powered on!");
		return false;
	}

	public static Datacenter getDatacenter(ServiceInstance si)
			throws InvalidProperty, RuntimeFault, RemoteException {
		Folder rootFolder = si.getRootFolder();
		Datacenter dc = null;
		ManagedEntity[] machines = rootFolder.getChildEntity();
		if (machines != null || machines.length != 0) {
			for (int i = 0; i < machines.length; i++) {
				// System.out.println("length : "+machines.length);
				if (machines[i] instanceof Datacenter) {
					dc = (Datacenter) machines[i];
					// System.out.println("datacenter : "+ dc.getName());
				}
			}
		}
		return dc;
	}

	public static HostSystem[] getHosts(ServiceInstance si)
			throws InvalidProperty, RuntimeFault, RemoteException {
		// HostSystem host = null;
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] hosts = new InventoryNavigator(rootFolder)
				.searchManagedEntities("HostSystem");// "130.65.132.159"

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

	public static VirtualMachine[] getVMs(ServiceInstance si)
			throws InvalidProperty, RuntimeFault, RemoteException {
		Folder vmFolder = si.getRootFolder();
		ManagedEntity[] vms = new InventoryNavigator(vmFolder).searchManagedEntities("VirtualMachine");
		if (vms == null) {
			logger.warning("Cannot find VirtualMachines!");
			return null;
		}
		VirtualMachine[] virtualMachines = new VirtualMachine[vms.length];
		for (int i = 0; i < vms.length; i++) {
			virtualMachines[i] = (VirtualMachine) vms[i];
		}

		return virtualMachines;

	}

	public static void ping(String host) {
		System.out.println("Ping Host: " + host);
		Service service = new Service();
		ServiceSoap port = service.getServiceSoap();
		String result = port.pingHost(host);
		System.out.println("Ping Result: " + result);
	}

	public void printHosts(ManagedEntity[] hosts) {
		for (ManagedEntity me : hosts) {
			// HostSystem hs = new HostSystem();
		}

	}

}
