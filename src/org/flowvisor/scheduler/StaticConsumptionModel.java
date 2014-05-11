package org.flowvisor.scheduler;

import java.util.HashMap;

import org.flowvisor.log.FVLog;
import org.flowvisor.log.LogLevel;
import org.openflow.protocol.OFType;

public class StaticConsumptionModel implements ConsumptionModel {
	
	HashMap<OFType, Integer> consumption;
	
	public StaticConsumptionModel() {
		consumption = new HashMap<OFType, Integer>();
		consumption.put(OFType.BARRIER_REPLY, 1);
		consumption.put(OFType.BARRIER_REQUEST, 1);
		consumption.put(OFType.ECHO_REPLY, 1);
		consumption.put(OFType.ECHO_REQUEST, 1);
		consumption.put(OFType.ERROR, 1);
		consumption.put(OFType.FEATURES_REPLY, 1);
		consumption.put(OFType.FEATURES_REQUEST, 1);
		consumption.put(OFType.FLOW_MOD, 5);
		consumption.put(OFType.GET_CONFIG_REPLY, 1);
		consumption.put(OFType.GET_CONFIG_REQUEST, 1);
		consumption.put(OFType.HELLO, 1);
		consumption.put(OFType.PACKET_IN, 5);
		consumption.put(OFType.PORT_STATUS, 1);
		consumption.put(OFType.SET_CONFIG, 1);
		consumption.put(OFType.STATS_REPLY, 1);
		consumption.put(OFType.STATS_REQUEST, 1);
		consumption.put(OFType.PORT_MOD, 1);
		consumption.put(OFType.FLOW_REMOVED, 1);
		consumption.put(OFType.QUEUE_CONFIG_REPLY, 1);
		consumption.put(OFType.QUEUE_CONFIG_REQUEST, 1);
		consumption.put(OFType.PACKET_OUT, 5);
	}
	
	@Override
	public int generateConsumption(HashMap<OFType, Integer> mc) {
		/*int cur = 0;
		for(MessageType type : MessageType.values()) {
			cur += mc.getCount(type)*consumption.get(type);
		}
		return cur;*/
		int cur = 0;
		for(OFType key : mc.keySet()) {
			if(!consumption.containsKey(key)) {
				FVLog.log(LogLevel.DEBUG, null, "model not complete! dont have info for:" + key);
				continue;
			}
			cur += mc.get(key)*consumption.get(key);
		}
		return cur;
	}
}
