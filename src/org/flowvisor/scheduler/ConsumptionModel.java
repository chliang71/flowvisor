package org.flowvisor.scheduler;

import java.util.HashMap;

import org.openflow.protocol.OFType;

public interface ConsumptionModel {
	public int generateConsumption(HashMap<OFType, Integer> mc);
}
