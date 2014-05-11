package org.flowvisor.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FlowSpaceInfo {
	
	//ConcurrentHashMap<String, LinkedList<FlowSpace>> flowspaceMap;//TODO, how about change the linkedlist to a hashmap?
	HashMap<String, String> flowspaceSliceMap;//key fs name, value slice name
	HashMap<String, String> flowspaceNameMap;//key fs dpid, value fs name,assume dpid is in form of "00:00....00:01"
	HashMap<String, FlowSpace> flowspaceMap;//key fs name, value fs
	HashMap<String, String> invertedFlowspaceNameMap;
	
	public FlowSpaceInfo() {
		flowspaceSliceMap = new HashMap<>();
		flowspaceNameMap = new HashMap<>();
		flowspaceMap = new HashMap<>();
		invertedFlowspaceNameMap = new HashMap<>();
	}
	
	public Set<String> getAllFSDPID() {
		return flowspaceNameMap.keySet();
	}
	
	public String lookupDpidByFSName(String fsname) {
		return invertedFlowspaceNameMap.get(fsname);
	}
	
	public String lookupFSNameByDpid(Long dpid) {
		return flowspaceNameMap.get(dpid);
	}
	
	public String lookupSliceByDPID(Long dpid) {
		
		String fsname = flowspaceNameMap.get(dpid);
		return flowspaceSliceMap.get(fsname);
	}
	
	public HashMap<String, String> getSwitchControllerMap() {
		return flowspaceSliceMap;
	}
	
	public String lookupNamebyDPID(String dpid) {
		return flowspaceNameMap.get(dpid);
	}
	
	class FlowSpace {
		String flowspaceName;
		String dpid;//TODO:dpid is supposed to be a long, but in a flowspace, it's format is like 00:00:00...:01,fix?
		int id;
		int priority;
		String name;
		int force_enqueue;
		HashMap<String, Integer> match;  
		HashMap<String, Integer> slice_action;
		ArrayList<Integer> queues;
		
		FlowSpace() {
			match = new HashMap<String, Integer>();
			slice_action = new HashMap<String, Integer>();
			queues = new ArrayList<Integer>();
		}
		
		public String listMatches() {
			String ret = "";
			for(String key : match.keySet()) {
				ret += "-key:" + key + "  value:" + match.get(key); 
			}
			return ret;
		}
		
		public String listAction() {
			String ret = "";
			for(String key : slice_action.keySet()) {
				ret += "-key:" + key + "  value:" + slice_action.get(key); 
			}
			return ret;			
		}
		
		@Override
		public String toString() {
			return "FlowSpace:" + this.dpid + ":" +
					this.force_enqueue + ":" + this.id + ":" + this.name
					+ ":" + this.priority + ":" + listMatches() + ":" +
					listAction() + '\n';
		}
	}
	
	private void addFlowspaceToSlice(String sliceName, String fsName) {
		
		/*LinkedList<FlowSpace> list = flowspaceMap.get(sliceName);
		if (list != null) {
			list.add(fs);		
		} else {
			list = new LinkedList<FlowSpace>();
			list.add(fs);
			flowspaceMap.put(sliceName, list);
		}*/
		flowspaceSliceMap.put(fsName, sliceName);
	}

	public void display(){
		for(String key:flowspaceSliceMap.keySet()) {		
			/*System.out.println(key + "------------------???");
			ret = "slice:" + key + '\n';
			for(FlowSpace fs : flowspaceMap.get(key)) {
				ret += fs.toString();
			}			
			System.out.println(ret);*/
			System.out.println("key:" + key + "  value:" + flowspaceSliceMap.get(key));
		}
	}
	
	public void changeFlowspace(String dstSliceName, String fsName) {
		/*LinkedList<FlowSpace> srclist = flowspaceMap.get(srcSliceName);
		if (srclist == null) {
			System.out.println("ERROR! this FS does not belong to " + srcSliceName);
			return;
		}
		int i = 0;
		for(FlowSpace fs : srclist) {
			if (fs.name.equals(fsName))
				break;
			i ++;
		}
		FlowSpace fs = srclist.remove(i);
		addFlowspaceToSlice(dstSliceName, fs);*/
		flowspaceSliceMap.put(fsName, dstSliceName);		
	}
	
	public boolean confimMapping(String dstSliceName, String fsName) {
		//if fsName already belongs to dstSliceName, return true, otherwise false
		if(flowspaceSliceMap.containsKey(fsName) && flowspaceSliceMap.get(fsName).equals(dstSliceName)) 
			return true;
		return false;
	}
	
	public void createFromCmd(String string) {
		//System.out.println(string);
		String[] substring = string.split("\n");
		if (!substring[0].equals("Configured Flow entries:")) {
			System.err.println("unexpected string!" + substring[0]+ "--");
			return;
		} 
		
		if (substring[1].trim().equals("None")) {
			System.err.println("Do not have any flowspace");
			return;
		}
		
		for (int i = 1;i<substring.length;i++) {
			JsonElement jelement = new JsonParser().parse(substring[i]);
			JsonObject jobject = jelement.getAsJsonObject();
			
			FlowSpace fs = new FlowSpace();
			fs.dpid = jobject.get("dpid").getAsString();
			
			
			fs.id = jobject.get("id").getAsInt();
			fs.name = jobject.get("name").getAsString();
			//record the map of name and dpid
			flowspaceNameMap.put(fs.dpid, fs.name);
			invertedFlowspaceNameMap.put(fs.name, fs.dpid);
			
			fs.force_enqueue = jobject.get("force-enqueue").getAsInt();
			fs.priority = jobject.get("priority").getAsInt();
			JsonObject matchObj = jobject.getAsJsonObject("match");
			//System.out.println("----->" + matchObj);			
			for(Map.Entry<String, JsonElement> entry : matchObj.entrySet()) {
				//System.out.println(entry.getKey() + "::::" + entry.getValue());
				fs.match.put(entry.getKey(), entry.getValue().getAsInt());
			}
			
			JsonArray sliceActionArray = jobject.getAsJsonArray("slice-action");
			for(int j = 0;j<sliceActionArray.size();j++) {
				JsonElement action = sliceActionArray.get(j);
				JsonObject actionObj = action.getAsJsonObject();
				String sliceName = actionObj.get("slice-name").getAsString();
				fs.slice_action.put(sliceName,
						actionObj.get("permission").getAsInt());
				//Also this fs to the map
				addFlowspaceToSlice(sliceName, fs.name);
			}
			
			JsonArray queuesArray = jobject.getAsJsonArray("queues");
			//System.out.println("------>" + queues);
			for(int j = 0;j<queuesArray.size();j++) {
				JsonElement queues = queuesArray.get(j);
				fs.queues.add(queues.getAsInt());
			}
			flowspaceMap.put(fs.name, fs);
		}
		
		
	}
}
