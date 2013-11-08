package org.flowvisor.allocator;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.flowvisor.classifier.FVClassifier;
import org.flowvisor.events.FVEventLoop;
import org.flowvisor.log.FVLog;
import org.flowvisor.log.LogLevel;
import org.flowvisor.slicer.FVSlicer;
import org.openflow.protocol.OFMessage;

public class Allocator {
	//this records all slicer and classifier
	//each slicer represents a controller
	//while each classifier represents a switch
	ConcurrentHashMap<String, FVSlicer> slicerMap;
	ConcurrentHashMap<String, FVClassifier> classifierMap;
	ConcurrentHashMap<String, String> sliceToClassifierMap;
	ConcurrentHashMap<String, SlicerMessageStats> sliceMsgStats;
	static Allocator runningInstance = null;
	static FVEventLoop loop;
	
	static TimerScheduler timer;
	
	public void startTimer() {
		timer = new TimerScheduler();
		new Thread(timer).start();
	}
	
	public static void createAllocator(FVEventLoop eloop) {
		loop = eloop;
		if(runningInstance == null) {
			runningInstance = new Allocator();
			runningInstance.startTimer();
		}

		FVLog.log(LogLevel.DEBUG, null, "#####Create new Allocator#####");
		
	}
	
	private Allocator(){
		this.slicerMap = new ConcurrentHashMap<String, FVSlicer>();
		this.classifierMap = new ConcurrentHashMap<String, FVClassifier>();
		this.sliceToClassifierMap = new ConcurrentHashMap<String, String>();
		this.sliceMsgStats = new ConcurrentHashMap<String, SlicerMessageStats>();
		FVLog.log(LogLevel.DEBUG, null, "creating new Allocators");
	}
	
	public static Allocator getRunningAllocator() {
		return runningInstance;
	}
	
	public void incMsgCount(FVSlicer slicer, OFMessage msg) {
		if(!sliceMsgStats.containsKey(slicer.getName())) {
			SlicerMessageStats sms = new SlicerMessageStats();
			sms.incMessageCount(msg.getType());
			sliceMsgStats.put(slicer.getName(), sms);
		} else {
			SlicerMessageStats sms = sliceMsgStats.get(slicer.getName());
			sms.incMessageCount(msg.getType());
		}
	}
	
	public void addNewSlicer(String name, FVSlicer slicer){
		if(!slicerMap.containsKey(name)) {
			FVLog.log(LogLevel.DEBUG, null, "#####Adding a new slicer " + name + "#####");
			slicerMap.put(name, slicer);
		}
	}
	
	public void addNewClassifier(String name, FVClassifier classifier){
		if(!classifierMap.containsKey(name)) {
			FVLog.log(LogLevel.DEBUG, null, "#####Adding a new classifier " + name + "#####");
			classifierMap.put(name, classifier);
		}
	}
	
	public void assignSlicerToClassifier(String slicerName, String classifierId) {
		if(!sliceToClassifierMap.containsKey(slicerName)) {
			FVLog.log(LogLevel.DEBUG, null, "#####Adding a new slicer to classifer entry, s:" +
		slicerName + " c:" + classifierId + "#####");
			sliceToClassifierMap.put(slicerName, classifierId);
		}
	}
	
	public void modifySlicer(Set<String> newSlices) {
		//####################################
	}
	
	
	private String getSliceMessageStats() {
		String ret = "";
		ArrayList<String> keylist = new ArrayList<String>(sliceMsgStats.keySet());
		for (String key : keylist) {
			ret += "------------------>\n" + key + ":\n" + sliceMsgStats.get(key).toString();
		}
		return ret;
	}
	
	class TimerScheduler implements Runnable {
		
		@Override
		public void run() {
			try {
				while(true) {
					Thread.sleep(5000);
					System.out.println("#####Timer Scheduler Waked Up#####");
					System.out.println(getSliceMessageStats());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
