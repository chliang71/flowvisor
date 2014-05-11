package org.flowvisor.allocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.flowvisor.log.FVLog;
import org.flowvisor.log.LogLevel;
import org.flowvisor.scheduler.ConsumptionModel;
import org.openflow.protocol.OFType;

public class SlicerMessageStats {
	HashMap<OFType, Integer> msgCount;

	public SlicerMessageStats() {
		msgCount = new HashMap<OFType, Integer>();
	}

	public void incMessageCount(OFType type) {
		if(!msgCount.containsKey(type)) {
			msgCount.put(type, new Integer(1));
		} else {
			int curr = msgCount.get(type).intValue();
			msgCount.put(type, new Integer(curr + 1));
		}
	}

	@Override
	public String toString() {
		ArrayList<OFType> keylist = new ArrayList<OFType>(msgCount.keySet());
		String ret = "";
		for(OFType key : keylist) {
			ret += key + ":" + msgCount.get(key) + '\n';
		}
		return ret;
	}

	public int getMessageCount(OFType type) {
		if(!msgCount.containsKey(type))
			return 0;
		else 
			return msgCount.get(type);
	}
	
	public void absorb(SlicerMessageStats sms) {
		HashMap<OFType, Integer> toAbsorb = sms.msgCount;
		Set<OFType> keys = toAbsorb.keySet();
		for(OFType key : keys) {
			if(this.msgCount.containsKey(key)) {
				int curr = this.msgCount.get(key).intValue();
				int add = toAbsorb.get(key).intValue();
				this.msgCount.put(key, new Integer(curr + add));
				FVLog.log(LogLevel.DEBUG, null, "#########adding:" + curr + ":" + add);
			} else {
				this.msgCount.put(key, toAbsorb.get(key));
				FVLog.log(LogLevel.DEBUG, null, "#########putting:" + toAbsorb.get(key));
			}
		}
	}
	
	public void substract(SlicerMessageStats sms) {
		HashMap<OFType, Integer> toSubstract = sms.msgCount;
		Set<OFType> keys = toSubstract.keySet();
		for(OFType key : keys) {
			if(this.msgCount.containsKey(key)) {
				int curr = this.msgCount.get(key).intValue();
				int sub = toSubstract.get(key).intValue();
				this.msgCount.put(key, new Integer(curr - sub));
				FVLog.log(LogLevel.DEBUG, null, "#########sub:" + curr + ":" + sub);
			} else {
				FVLog.log(LogLevel.DEBUG, null, "#########PANIC! substract from 0:" + toSubstract.get(key));
			}
		}
	}
	
	public int generateConsumption(ConsumptionModel cm) {
		//generate consumption based on current aggregate stats
		return cm.generateConsumption(msgCount);
	}
}
