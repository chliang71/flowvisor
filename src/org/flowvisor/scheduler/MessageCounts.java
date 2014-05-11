package org.flowvisor.scheduler;

import java.util.HashMap;


public class MessageCounts {
	
	HashMap<MessageType, Integer> counts;
	public MessageCounts() {
		counts = new HashMap<MessageType, Integer>();
	}
	
	public int getCount(MessageType type) {
		if(counts == null)
			counts = new HashMap<MessageType, Integer>();
		if(!counts.containsKey(type))
			return 0;
		return counts.get(type);
	}
	
	public void aggreate(MessageCounts mc) {
		for (MessageType type : MessageType.values()) {
			int curr;
			if(counts.containsKey(type))
				curr = this.counts.get(type);
			else
				curr = 0;
			curr += mc.getCount(type);
			this.counts.put(type, new Integer(curr));
		}
	}
	
	@Override
	public String toString() {
		String ret = "";
		for (MessageType type : MessageType.values()) {
			ret += counts.get(type) + "--";
		}
		return ret;
	}
}
