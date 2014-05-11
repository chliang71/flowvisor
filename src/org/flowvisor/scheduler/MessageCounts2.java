package org.flowvisor.scheduler;

public class MessageCounts2 {
	int BARRIER_REPLY; 
	int BARRIER_REQUEST; 
	int ECHO_REPLY; 
	int ECHO_REQUEST; 
	int ERROR;
	int FEATURES_REPLY; 
	int FEATURES_REQUEST; 
	int FLOW_MOD;
	int FLOW_REMOVED;
	int GET_CONFIG_REPLY; 
	int GET_CONFIG_REQUEST; 
	int HELLO;
	int PACKET_IN; 
	int PORT_MOD;
	int SET_CONFIG;
	int STATS_REPLY_DESC; 
	int STATS_REQUEST_DESC;
	int PORT_STATUS;
	int PACKET_OUT;
	int QUEUE_CONFIG_REQUEST;
	int QUEUE_CONFIG_REPLY;
	
	public void aggreate(MessageCounts2 mc) {
		this.BARRIER_REPLY += mc.BARRIER_REPLY;
		this.BARRIER_REQUEST += mc.BARRIER_REQUEST;
		this.ECHO_REPLY += mc.ECHO_REPLY;
		this.ECHO_REQUEST += mc.ECHO_REQUEST;
		this.ERROR += mc.ERROR;
		this.FEATURES_REPLY += mc.FEATURES_REPLY;
		this.FEATURES_REQUEST += mc.FEATURES_REQUEST;
		this.FLOW_MOD += mc.FLOW_MOD;
		this.FLOW_REMOVED += mc.FLOW_REMOVED;
		this.GET_CONFIG_REPLY += mc.GET_CONFIG_REPLY;
		this.GET_CONFIG_REQUEST += mc.GET_CONFIG_REQUEST;
		this.HELLO += mc.HELLO;
		this.PACKET_IN += mc.PACKET_IN;
		this.PACKET_OUT += mc.PACKET_OUT;
		this.PORT_MOD += mc.PORT_MOD;
		this.PORT_STATUS += mc.PORT_STATUS;
		this.SET_CONFIG += mc.SET_CONFIG;
		this.STATS_REPLY_DESC += mc.STATS_REPLY_DESC;
		this.STATS_REQUEST_DESC += mc.STATS_REQUEST_DESC;
		this.QUEUE_CONFIG_REPLY += mc.QUEUE_CONFIG_REPLY;
		this.QUEUE_CONFIG_REQUEST += mc.QUEUE_CONFIG_REQUEST;
	}
	
	public MessageCounts2 substract(MessageCounts2 mc) {
		MessageCounts2 mcinc = new MessageCounts2();
		mcinc.BARRIER_REPLY = this.BARRIER_REPLY - mc.BARRIER_REPLY;
		mcinc.BARRIER_REQUEST = this.BARRIER_REQUEST - mc.BARRIER_REQUEST;
		mcinc.ECHO_REPLY = this.ECHO_REPLY - mc.ECHO_REPLY;
		mcinc.ECHO_REQUEST = this.ECHO_REQUEST - mc.ECHO_REQUEST;
		mcinc.ERROR = this.ERROR - mc.ERROR;
		mcinc.FEATURES_REPLY = this.FEATURES_REPLY - mc.FEATURES_REPLY;
		mcinc.FEATURES_REQUEST = this.FEATURES_REQUEST - mc.FEATURES_REQUEST;
		mcinc.FLOW_MOD = this.FLOW_MOD - mc.FLOW_MOD;
		mcinc.FLOW_REMOVED = this.FLOW_REMOVED - mc.FLOW_REMOVED;
		mcinc.GET_CONFIG_REPLY = this.GET_CONFIG_REPLY - mc.GET_CONFIG_REPLY;
		mcinc.GET_CONFIG_REQUEST = this.GET_CONFIG_REQUEST - mc.GET_CONFIG_REQUEST;
		mcinc.HELLO = this.HELLO - mc.HELLO;
		mcinc.PACKET_IN = this.PACKET_IN - mc.PACKET_IN;
		mcinc.PACKET_OUT = this.PACKET_OUT - mc.PACKET_OUT;
		mcinc.PORT_MOD = this.PORT_MOD - mc.PORT_MOD;
		mcinc.PORT_STATUS = this.PORT_STATUS - mc.PORT_STATUS;
		mcinc.QUEUE_CONFIG_REPLY = this.QUEUE_CONFIG_REPLY - mc.QUEUE_CONFIG_REPLY;
		mcinc.QUEUE_CONFIG_REQUEST = this.QUEUE_CONFIG_REQUEST - mc.QUEUE_CONFIG_REQUEST;
		mcinc.SET_CONFIG = this.SET_CONFIG - mc.SET_CONFIG;
		mcinc.STATS_REPLY_DESC = this.STATS_REPLY_DESC - mc.STATS_REPLY_DESC;
		mcinc.STATS_REQUEST_DESC = this.STATS_REQUEST_DESC - mc.STATS_REQUEST_DESC;
		return mcinc;
	}
	
	@Override
	public String toString() {
		return this.BARRIER_REPLY +
				"::" + this.BARRIER_REQUEST +
				"::" + this.ECHO_REPLY +
				"::" + this.ECHO_REQUEST +
				"::" + this.ERROR + 
				"::" + this.FEATURES_REPLY +
				"::" + this.FEATURES_REQUEST +
				"::" + this.FLOW_MOD +
				"::" + this.GET_CONFIG_REPLY +
				"::" + this.GET_CONFIG_REQUEST +
				"::" + this.HELLO +
				"::" + this.PACKET_IN +
				"::" + this.PACKET_OUT + 
				"::" + this.SET_CONFIG +
				"::" + this.STATS_REPLY_DESC +
				"::" + this.STATS_REQUEST_DESC +
				"::" + this.PORT_STATUS;
	}
}
