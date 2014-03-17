package com.cmpe283.project;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class IPContainer {
	private final  int MAX_IP_NUM = 8; 
	private String vmsIpAddressesPool[];
	private int vmsIpStatus[];
	private String vmsIpRange = "130.65.133.23";
	
	private static final Logger logger = Logger.getLogger(IPContainer.class);
	
	public IPContainer(ServiceInstance si) throws InvalidProperty, RuntimeFault, RemoteException{
		vmsIpAddressesPool = new String[MAX_IP_NUM];
		vmsIpStatus = new int[MAX_IP_NUM];
		this.initIpAddressPool();
		this.initIpStatus();
		this.setIpStatusbyVMs(si);
		
	}
	
	public String getIP(){
		for(int i = 0; i < MAX_IP_NUM; i++){
			if(vmsIpStatus[i] != 0 ){
				return vmsIpAddressesPool[i];
			}
		}
		logger.warn(" No IP address is able to be used in ip address pool !");
		return "ipAllUsed";
	}
	
	private void setIpStatusbyVMs(ServiceInstance si) throws InvalidProperty, RuntimeFault, RemoteException{
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] vms = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		for(ManagedEntity me : vms){
			VirtualMachine vm = (VirtualMachine)me;
			this.setIpStatus(vm.getSummary().getGuest().getIpAddress());
		}
		
	}
	
	private void initIpAddressPool(){
		System.out.println(" initing ip address pool... ");
		for(int i = 0; i < MAX_IP_NUM; i++){
			vmsIpAddressesPool[i] = vmsIpRange + i;
			System.out.println("ip : " + vmsIpAddressesPool[i]);
		}
	}
	
	private void initIpStatus(){
		System.out.println("initing ip status...");
		for(int i = 0; i < MAX_IP_NUM; i++){
			vmsIpStatus[i] = 1;
			System.out.println("initial ip status : " + vmsIpStatus[i]);
		}
	}
	
	public void setIpStatus(String ip){
		for(int i = 0; i < MAX_IP_NUM; i++){
			if(vmsIpAddressesPool[i].equals(ip)){
				vmsIpStatus[i] = 0;
			}
		}
	}
	

}
