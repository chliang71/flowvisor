package org.flowvisor.allocator;

import java.util.ArrayList;
import java.util.HashMap;

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
}
