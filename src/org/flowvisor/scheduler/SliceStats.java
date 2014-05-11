package org.flowvisor.scheduler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SliceStats {

	String sliceName;
	MessageCounts2 rx;
	MessageCounts2 tx;
	MessageCounts2 drop;
	
	public void aggreate(SliceStats ss) {
		if (!this.sliceName.equals(ss.sliceName)) {
			System.err.println("ERROR: aggreating different slice!");
			return;
		}
		this.rx.aggreate(ss.rx);
		this.tx.aggreate(ss.tx);
		this.drop.aggreate(ss.drop);
	}
	
	public void parseFromJson(String jsonString) {
		JsonElement jelement = new JsonParser().parse(jsonString);
		JsonObject jobject = jelement.getAsJsonObject();
		
		JsonObject subjobject = jobject.getAsJsonObject("rx");
		rx = parseHelper(subjobject);
		
		subjobject = jobject.getAsJsonObject("tx");
		tx = parseHelper(subjobject);
		
		subjobject = jobject.getAsJsonObject("drop");
		drop = parseHelper(subjobject);
		
	}
	
	private MessageCounts2 parseHelper(JsonObject jobject){
		jobject = jobject.getAsJsonObject("Total");
		if (jobject.has("STATS_REQUEST.DESC")) {
			jobject.addProperty("STATS_REQUEST_DESC", jobject.
					get("STATS_REQUEST.DESC").getAsString());
		}
		if (jobject.has("STATS_REPLY.DESC")) {
			jobject.addProperty("STATS_REPLY_DESC", 
					jobject.get("STATS_REPLY.DESC").getAsString());
		}
		Gson gson = new Gson();
		return gson.fromJson(jobject, MessageCounts2.class);
	}
}

