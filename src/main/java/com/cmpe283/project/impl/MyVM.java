package com.cmpe283.project.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * Write a description of class MyVM here.
 * 
 * @author (your name)
 * @version (a version number or a date)
 */
public class MyVM {
	private VirtualMachine vm;

	private static final Logger logger = Logger.getLogger(MyVM.class);

	/**
	 * Constructor for objects of class MyVM
	 */
	public MyVM(VirtualMachine vm ) {
		// initialise instance variables
		try {
			this.vm = vm;
			if(this.vm == null){
				throw new Exception("VirtualMachine  is null");
			}

		} catch (Exception e) {
			logger.warn(e.toString());
		}

	}

	public VirtualMachine getVMInstance(){
		return this.vm;
	}
	public boolean powerOn() throws VmConfigFault,
			TaskInProgress, FileFault, InvalidState,
			InsufficientResourcesFault, RuntimeFault, RemoteException,
			InterruptedException {

		Task task = vm.powerOnVM_Task(null);
		System.out.println("VirtualMachine " + vm.getName() + " is powerring on! Wait...");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("VirtualMachine " + vm.getName() + " is powered on successfully!");
			return true;
		} else
			return false;
	}

	public boolean isVmPoweredOn() {
		VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
		if (vmps == VirtualMachinePowerState.poweredOn) {
			System.out.println("VirtualMachine " + vm.getName() + " has been powered on!");
			return true;
		}
		System.out.println("VirtualMachine " + vm.getName() + " now is NOT powered on! Please Power on it!");
		return false;
	}

	
	public boolean powerOff() throws VmConfigFault,
			TaskInProgress, FileFault, InvalidState,
			InsufficientResourcesFault, RuntimeFault, RemoteException,
			InterruptedException {

		Task task = vm.powerOffVM_Task();
		System.out.println("VirtualMachine " + vm.getName() + " is powerring off! Wait...");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("VirtualMachine " + vm.getName() + " is powered off successfully!");
			return true;
		} else
			return false;
	}

	public boolean isPoweredOff() {
		VirtualMachinePowerState vmps = this.vm.getRuntime().getPowerState();
		if (vmps == VirtualMachinePowerState.poweredOff) {
			System.out.println("VirtualMachine " + this.vm.getName() + " has been powered off!");
			return true;
		}
		return false;
	}

	public boolean coldMigrateTo(HostSystem hostDes)
			throws Exception {
		
		ComputeResource cr = (ComputeResource) hostDes.getParent();

		if (vm.getRuntime().powerState == VirtualMachinePowerState.poweredOn) {
			if(!powerOff()){
				throw new Exception("VM cannot be powered off!");
			}
		}
		
		System.out.println("Migrating......");
		Task migrateTask = vm.migrateVM_Task(cr.getResourcePool(), hostDes, VirtualMachineMovePriority.lowPriority, VirtualMachinePowerState.poweredOff);
		if (migrateTask.waitForTask() == Task.SUCCESS) {
			System.out.println("migrate successfully");
			return true;
		}
		return false;
	}

	
	public void reset() {
		try {
			// your code here
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public void suspend() {
		try {
			// your code here
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	@Override
	public String toString() {
		String separator = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("---------------------------------------------------------------------");
		sb.append(separator);
		sb.append(String.format("VM Name: %s", vm.getName()));
		sb.append(separator);
		sb.append(String.format("Guest OS: %s", vm.getSummary().getConfig().guestFullName));
		sb.append(separator);
		sb.append(String.format("VM Version: %s", vm.getConfig().version));
		sb.append(separator);
		sb.append(String.format("CPU: %d vCPU(s)", vm.getConfig().getHardware().numCPU));
		sb.append(separator);
		sb.append(String.format("Memory: %d MB", vm.getConfig().getHardware().memoryMB));
		sb.append(separator);
		sb.append(String.format("Memory Overhead: %2f MB", (long) vm.getConfig().memoryAllocation.reservation / 1000000f));
		sb.append(separator);
		sb.append(String.format("VMware Tools: %s", vm.getGuest().toolsRunningStatus));
		sb.append(separator);
		sb.append(String.format("IP Addresses: %s", vm.getSummary().getGuest().getIpAddress()));
		sb.append(separator);
		sb.append(String.format("State: %s", vm.getGuest().guestState));
		sb.append(separator);
		sb.append("---------------------------------------------------------------------");

		return sb.toString();
	}
}
