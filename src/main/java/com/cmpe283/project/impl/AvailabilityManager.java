package com.cmpe283.project.impl;

import java.rmi.RemoteException;
import java.util.List;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.HostSystem;

public class AvailabilityManager {
	
	public static void main(String[] args) throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException {
		// TODO Auto-generated method stub
		
		MyVCenter vcenter = new MyVCenter();
		List<MyVM> vms = MyVCenter.getVMs();
		MySnapshot snap = new MySnapshot(vms);
		new Thread(snap).start();
	
		
		for(MyVM vm : vms){
			if(vm.isVmPoweredOn()){
				MyMonitor monitor = new MyMonitor();
				monitor.setVm(vm);
				new Thread(monitor).start();
			}
		}

	}

}
