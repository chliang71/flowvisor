package org.flowvisor.scheduler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SliceInfo {
	private String admin_contact;
	private String admin_statue;
	private String controller_url;
	private String current_flowmod_usage;
	private String current_rate;
	private String drop_policy;
	private String recv_lldp;
	private String slice_name;

	public void parseFromJson(String jsonString) {
		try {
		JsonElement jelement = new JsonParser().parse(jsonString);
		JsonObject jobject = jelement.getAsJsonObject();

		//System.out.println(jobject);
		admin_contact = jobject.get("admin-contact").getAsString();
		admin_statue = jobject.get("admin-status").getAsString();
		controller_url = jobject.get("controller-url").getAsString();
		current_flowmod_usage = jobject.get("current-flowmod-usage").getAsString();
		current_rate = jobject.get("current-rate").getAsString();
		drop_policy = jobject.get("drop-policy").getAsString();
		recv_lldp = jobject.get("recv-lldp").getAsString();
		slice_name = jobject.get("slice-name").getAsString();
		} catch (Exception e) {
			System.out.println("Parsing SliceInfo failed on:" + jsonString);
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return this.admin_contact +
				":::" + this.admin_statue +
				":::" + this.controller_url +
				":::" + this.current_flowmod_usage +
				":::" + this.current_rate + 
				":::" + this.drop_policy +
				":::" + this.recv_lldp +
				":::" + this.slice_name;
	}
}
