package com.cmpe283.project;

import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.VirtualMachine;

public class Printer {
	
	public static void printVmName(VirtualMachine vm){
		System.out.println(vm.getName());
	}
	
	public static void printDatacenterName(Datacenter dc){
		System.out.println(dc.getName());
	}
}
