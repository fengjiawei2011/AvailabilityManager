package com.cmpe283.project.impl;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import com.vmware.vim25.Action;
import com.vmware.vim25.AlarmAction;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.MethodAction;
import com.vmware.vim25.MethodActionArgument;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.VirtualMachine;

public class MyAlarm {
	
	MyVCenter myVCenter = null;
	private static final Logger logger = Logger.getLogger(MyStatistics.class.getName());
	
	public MyAlarm() throws MalformedURLException, RemoteException{
		myVCenter = new MyVCenter();
	}
	public void setPowerOffAlarm() {
		AlarmManager alarmMgr = MyVCenter.getServiceInstance().getAlarmManager();

		try {
			for (MyVM vm : myVCenter.getVMs()) {

				AlarmSpec spec = buildAlarmSpec("VmPowerOffAlarm." + vm.getVMInstance().getName());

				Alarm[] alarms = alarmMgr.getAlarm(vm.getVMInstance());

				for (Alarm alarm : alarms) {
					if (alarm.getAlarmInfo().getName().equals(spec.getName())) {
						alarm.removeAlarm();
					}
				}

				alarmMgr.createAlarm(vm.getVMInstance(), spec);

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		}

	}

	private AlarmSpec buildAlarmSpec(String alarmName) {
		AlarmSpec spec = new AlarmSpec();

		StateAlarmExpression expression = createStateAlarmExpression();

		AlarmAction methodAction = createAlarmTriggerAction(createPowerOffAction());

		// spec.setAction(methodAction);
		spec.setExpression(expression);
		spec.setName(alarmName);
		spec.setDescription("Monitor VM state and trigger some alarm actions");
		spec.setEnabled(true);

		AlarmSetting as = new AlarmSetting();
		as.setReportingFrequency(0); // as often as possible
		as.setToleranceRange(0);

		spec.setSetting(as);

		return spec;
	}

	private AlarmTriggeringAction createAlarmTriggerAction(Action action) {
		AlarmTriggeringAction alarmAction = new AlarmTriggeringAction();
		alarmAction.setYellow2red(true);
		alarmAction.setAction(action);
		return alarmAction;
	}

	private StateAlarmExpression createStateAlarmExpression() {
		StateAlarmExpression expression = new StateAlarmExpression();
		expression.setType("VirtualMachine");
		expression.setStatePath("runtime.powerState");
		expression.setOperator(StateAlarmOperator.isEqual);
		expression.setRed("poweredOff");
		return expression;
	}

	private MethodAction createPowerOffAction() {
		MethodAction action = new MethodAction();
		action.setName("PowerOffVM_Task");
		MethodActionArgument argument = new MethodActionArgument();
		argument.setValue(null);
		action.setArgument(new MethodActionArgument[] { argument });
		return action;
	}
}
