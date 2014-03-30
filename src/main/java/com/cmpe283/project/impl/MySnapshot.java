package com.cmpe283.project.impl;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import com.vmware.vim25.FileFault;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.SnapshotFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class MySnapshot implements Runnable {
	List<MyVM> vms = null;

	public MySnapshot(List<MyVM> vms) {
		this.vms = vms;
	}

	public static boolean revert2LastSnapshot(HostSystem vhost) throws Exception {
		ServiceInstance superVCenter = new ServiceInstance(new URL(AMConfig.SuperVcenterUrl), AMConfig.UserName, AMConfig.Password, true);
		VirtualMachine vm = (VirtualMachine) new InventoryNavigator(superVCenter.getRootFolder()).searchManagedEntity("VirtualMachine", AMConfig.Vhosts.get(MyVHost.getVHostIP(vhost)));
		MyVM v = new MyVM(vm);
		boolean res = revert2LastSnapshot(v);
		v.powerOn();

		superVCenter.getServerConnection().logout();
		return res;
	}
	
	public static boolean revert2LastSnapshot(MyVM vm) throws Exception {
		Task task = vm.getVMInstance().revertToCurrentSnapshot_Task(null);
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println(vm.getVMInstance().getName()+ " was reverted to last snapshot.");
			return true;
		} else {
			System.out.println(vm.getVMInstance().getName() + " recover failure.");
			return false;
		}
	}
	
	public static void removeAllSnapshot(MyVM vm) throws Exception {
		Task task = vm.getVMInstance().removeAllSnapshots_Task();
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Removed all snapshots for " + vm.getVMInstance().getName());
		}
	}


	@Override
	public void run() {
		while (true) {
			for (MyVM vm : vms) {
				if (vm.pingSelf()) {

					try {
						vm.createSnapShot();
					} catch (InvalidName e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (VmConfigFault e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SnapshotFault e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TaskInProgress e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FileFault e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidState e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RuntimeFault e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			try {
				Thread.sleep(1000 * 60 * 5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
