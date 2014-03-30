package com.cmpe283.project.impl;

import org.apache.log4j.Logger;

import com.vmware.vim25.mo.HostSystem;

public class MyMonitor implements Runnable {

	private MyVM vm;
	private MyStatistics statistic;
	private static final Logger logger = Logger.getLogger(MyVCenter.class);

	public MyMonitor() {
		statistic = new MyStatistics(MyVCenter.getServiceInstance().getPerformanceManager());
	}

	public MyVM getVm() {
		return vm;
	}

	public void setVm(MyVM vm) {
		this.vm = vm;
	}

	public boolean pingHost(MyVM mvm) {
		
		try {
			String cmd = "";
			if (System.getProperty("os.name").startsWith("Windows")) {
				// For Windows
				cmd = "ping -n 1 " + mvm.getVMInstance().getGuest().getIpAddress();
			} else {
				// For Linux and OSX
				cmd = "ping -c 1 " + mvm.getVMInstance().getGuest().getIpAddress();
			}

			Process myProcess = Runtime.getRuntime().exec(cmd);
			myProcess.waitFor();

			if (myProcess.exitValue() == 0) {
				System.out.println("ping " + vm.getVMInstance().getGuest().getIpAddress() + " successfully");
				return true;
			} else {
				System.out.println("ping " + vm.getVMInstance().getGuest().getIpAddress() + " Error");
				return false;
			}

		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}
	}
	
	public boolean pingHost(HostSystem host){
		
		try {
			String cmd = "";
			if (System.getProperty("os.name").startsWith("Windows")) {
				// For Windows
				cmd = "ping -n 1 " + MyVHost.getVHostIP(host);
			} else {
				// For Linux and OSX
				cmd = "ping -c 1 " + MyVHost.getVHostIP(host);
			}

			Process myProcess = Runtime.getRuntime().exec(cmd);
			myProcess.waitFor();

			if (myProcess.exitValue() == 0) {
				System.out.println("ping VHost" + MyVHost.getVHostIP(host) + " successfully");
				return true;
			} else {
				System.out.println("ping VHost" + MyVHost.getVHostIP(host) + " Error");
				return false;
			}

		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void run() {
		statistic.printStatisticsForVm(vm.getVMInstance());
		HostSystem myHost = MyVCenter.getVhostByVM(vm.getVMInstance());
		int fail_counter = 0;
		while (true) {
			MyVCenter.isHostAvailable(myHost);
			boolean isPingSucc = this.pingHost(vm);
			if (isPingSucc) {
				if (fail_counter > 0)
					fail_counter = 0;
				// statistic.printStatisticsForVm(vm.getVMInstance());
				// System.out.println(vm.getVMInstance().toString());
			} else {
				try {
					if (vm.isVmPoweredOn())
						fail_counter++;

					if (fail_counter == 10) {
						
						HostSystem hostDes = null;
						if (this.pingHost(myHost)) {
							vm.revert();
							vm.powerOn();
							isPingSucc = this.pingHost(vm);
							int counter = 0;
							while(!isPingSucc){
								counter++;
								if(counter == 50) break;
								System.out.println(String.format("Waiting for VM %s powering on", vm.getVMInstance().getName()));
								Thread.sleep(1000*3);
							}
							fail_counter = 0;
							
						} else {
							
							if(fail_counter == 5){
								
								fail_counter = 0;
								if (MyVCenter.getHosts().length == 1) {
									hostDes = MyVCenter.addMigratedHost();
									vm.coldMigrateTo(hostDes);
									fail_counter++;
								} else {
									hostDes = MyVCenter.getMigratedVHost();
									vm.coldMigrateTo(hostDes);
									fail_counter++;
								}
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(1000 * 3);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
