package org.flowvisor.scheduler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.flowvisor.allocator.Allocator;
import org.flowvisor.allocator.SlicerMessageStats;
import org.flowvisor.log.FVLog;
import org.flowvisor.log.LogLevel;

import com.google.gson.Gson;

import randy.Programming;
import randy.model.ModelInput;
import randy.model.ModelOutput;


public class Scheduler implements Runnable {

	boolean singleNode;
	Allocator allocator;
	//HashMap<String, SliceStats> globalSliceStatsMap;	
	HashMap<String, SliceStats> sliceStatsMap;
	HashMap<Long, SlicerMessageStats> switchStats;
	HashMap<Long, SlicerMessageStats> lastSwitchStats;//just a record

	//the index for LP part
	LinkedList<String> sliceIndex;//slice name
	LinkedList<Long> fsIndex;//dpid

	HashMap<String, String> switchControllerMap;  //maintains the mapping from switch to controller 

	String currentMarshalledConsumption;

	ArrayList<Long> switches; //list of all dpids
	boolean toStop;

	ConsumptionModel cm;

	SocketChannel sc;
//
//	private final String SLICE_STAT_QUERY = "fvctl -f /dev/null list-slice-stats ";
//	private final String SLICE_INFO_QUERY = "fvctl -f /dev/null list-slice-info ";
//	private final String SWITCH_QUERY = "fvctl -f /dev/null list-datapaths";
//	private final String CREATE_SLICE = "fvctl -f /dev/null add-slice -p='\\n' ";
//	private final String REMOVE_SLICE = "fvctl -f /dev/null remove-slice ";
//	private final String FLOWSPACE_QUERY = "fvctl -f /dev/null list-flowspace";
//	private final String CREATE_FLOWSPACE = "fvctl -f /dev/null add-flowspace ";
//	private final String REMOVE_FLOWSPACE = "fvctl -f /dev/null remove-flowspace ";
//	private final String UPDATE_FLOWSPACE = "fvctl -f /dev/null update-flowspace ";
//	private final String SWITCH_STATS_QUERY = "fvctl -f /dev/null list-datapath-stats ";
//	private final String ALL_SLICE_QUERY = "fvctl -f /dev/null list-slices ";

	public Scheduler(boolean singleNodeMode, Allocator alloc) {
		sliceStatsMap = new HashMap<String, SliceStats>();
		lastSwitchStats = new HashMap<Long, SlicerMessageStats>();
		switchStats = new HashMap<Long, SlicerMessageStats>();
		switchControllerMap = new HashMap<String, String>();
		sliceIndex = new LinkedList<String>();
		switches = null;
		toStop = false;
		currentMarshalledConsumption = "";
		singleNode = singleNodeMode;
		allocator = alloc;
	}


	protected void init(){
		boolean stop = false;
		try {
			Thread.sleep(5000);
			while(!stop) {
				querySwitches();
				Thread.sleep(1000);
				if(switches.size() > 0)
					stop = true;
			}
			fsIndex = new LinkedList<>(switches);
			stop = false;
			while(!stop) {
				queryAllSliceInfo();
				Thread.sleep(1000);
				if(sliceIndex.size() > 0)
					stop = true;
			}
		}catch(InterruptedException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	@Override
	public void run() {
		//1.establish the info about the network, switch id, controller id,url,etc.
		//2.periodically checks the stats of each controller, all info we need is slice stats, pass it the LP
		//3.wait for LP to response, apply the update using flowspace update
		//we should have known the url of all controllers at this point, thus we
		//can create slice accordingly. And switches can be obtained by query

		//We assume that before the scheduler starts, there already exist a bunch of flowspace
		//and controllers, we only change them, not create them(this makes debug a little easier)
		init();
		FVLog.log(LogLevel.DEBUG, null, "init finished!");
		cm = new StaticConsumptionModel();
		FVLog.log(LogLevel.DEBUG, null,"# of controller:" + getSliceNumber());
		FVLog.log(LogLevel.DEBUG, null,"their slice are:");
		for(String s : sliceIndex) {
			FVLog.log(LogLevel.DEBUG, null, s + " ");
		}
		FVLog.log(LogLevel.DEBUG, null,"# of switch:" + getSwitchNumber());
		FVLog.log(LogLevel.DEBUG, null,"their flowspace are:");
		for(Long s : switches) {
			FVLog.log(LogLevel.DEBUG, null, s + " ");
		}
		//createFlowspaceForAll(null);//this will create flowspaces for all switches, all flowspace is under slice "fvadmin"

		//TODO:create slices for all controllers, should be easy, given the url of all controllers 

		//for now, let's assume the total number of controllers and switches are constant
		ModelInput input = staticConfig();
		//////////////////
		if(!singleNode) {
			try{
				sc = SocketChannel.open();
				sc.connect(new InetSocketAddress(InetAddress.getByName("localhost"), 12345));
				sc.configureBlocking(false);
				while(!sc.finishConnect());
			} catch(Exception e) {
				e.printStackTrace();
				System.err.println("ERROR connecting central scheduler, quiting");
				System.exit(1);
			}
		}
		
		//the main schedule loop, do the 2nd,3rd things mentioned above
		while(!toStop) {
			for(Long dpid : switches) {
				querySwitchStats(dpid);
			}
			//displaySwStats();
			int[][] consumption = generateConsumption();			
			if(!singleNode) {
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				String marshalledConsupmtion = marshalConsumption(consumption); 
				System.out.println(marshalledConsupmtion);
				try {
					ByteBuffer sendbuffer = ByteBuffer.wrap(marshalledConsupmtion.getBytes());
					while(sendbuffer.hasRemaining()) {
						sc.write(sendbuffer);
					}
					sc.read(buffer);
					buffer.flip();
					String ret = new String(buffer.array());
					ret = ret.trim();
					String last = ret;
					if(ret.contains(" ")) {
						String[] sub = ret.split(" ");
						last = sub[sub.length - 1];
					}
					System.out.println("got this: " + ret + "| last:" + last + "|");
					int[] assignment = new Gson().fromJson(last, int[].class);
					for(int i = 0;i<assignment.length;i++) {
						System.out.print(assignment[i] + " ");
					}
					System.out.println();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				
				FVLog.log(LogLevel.DEBUG, null, "consumption:");
				for (int i = 0;i<fsIndex.size();i++) {
					for(int j=0;j<sliceIndex.size();j++) {
						FVLog.log(LogLevel.DEBUG, null, consumption[i][j] + " ");
					}				
					FVLog.log(LogLevel.DEBUG, null, "\n");
				}
				/*int[][] invertedConsumption = new int[consumption[0].length][consumption.length];
				for(int i = 0;i<consumption.length;i++)
					for(int j = 0;j<consumption[0].length;j++)
						invertedConsumption[j][i] = consumption[i][j];*/
				input.setConsumpiton(consumption);
//				ModelOutput output = Programming.runProgramming(input, 1);
//				if(output == null) {
//					System.out.println("LP returned:No solution found!");
//				} else {
//					System.out.println("LP returned:Attempt to update");
//					int[] assignment = output.getControllerSwitchAssignment();
//					updateMapping(assignment);
//				}
				int[] assignment = new int[getSwitchNumber()];
				Random ran = new Random();
				String a = "";
				for(int i = 0;i<assignment.length;i++) {
					assignment[i] = ran.nextInt(getSliceNumber());
					a += assignment[i] + " ";
				}
				FVLog.log(LogLevel.DEBUG, null, "===>new assignment:" + a + "\n");
				updateMapping(assignment);
				/////////////////////////////
				FVLog.log(LogLevel.DEBUG, null, "Checking slices!!!");
				allocator.checkAllSlice();
				////////////////////////////
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private int[][] getLatency() {
		int rows = getSliceNumber();
		int cols = getSwitchNumber();
		int[][] latency = new int[getSliceNumber()][getSwitchNumber()];
		//let's make it simple for now
		for(int i = 0;i<rows;i++) 
			for(int j=0;j<cols;j++)
				latency[i][j] = 1;
		return latency;
	}
	
	private int getAverageLatency() {
		int[][] latency = getLatency();
		int sum = 0;
		int rows = latency.length;
		int cols = latency[0].length;
		for(int i = 0;i<rows;i++)
			for(int j = 0;j<cols;j++)
				sum += latency[i][j];		
		return sum/(rows*cols); 
	}
	
	private int getWorstLatency() {
		//let's make it simple for now
		return getAverageLatency() + 1;
	}
	
	private int[][] getCapacities() {
		int rows = getSliceNumber();
		int cols = getCapacitiesNumber();
		int[][] caps = new int[getSliceNumber()][getCapacitiesNumber()];
		//let's make it simple for now
		for(int i = 0;i<rows;i++) {
			for(int j = 0;j<cols;j++) {
				caps[i][j] = 25;
			}
		}
		return caps;
	}
	
	private int getCapacitiesNumber() {
		return 2; //CPU?RAM?
	}
	
	private int[][] getMigrationCost() {
		int rows = getControllerNumber();
		int cols = getLocationNumber();
		int[][] cost = new int[rows][cols];
		for(int i = 0;i<rows;i++)
			for(int j = 0;j<cols;j++)
				cost[i][j] = 1;
		return cost;
	}
	
	private int getControllerNumber() {
		return getSliceNumber();
	}
	
	private int getLocationNumber() {
		return getSliceNumber();
	}
	
	public ModelInput staticConfig() {
		//set the parameters that are constant over time, including:
		//# of controller
		//# of switch
		//latency(avg and worst)
		//capacity of controller
		//location number
		//migration cost
		ModelInput input = new ModelInput();
		//for now, let's say we are free to user all controllers
		input.setControllerNumber(getControllerNumber());
		input.setSwitchNumber(getSwitchNumber());		
		input.setCapacitiesNumber(getCapacitiesNumber());//CPU?RAM?
		//these is the number of all controllers, but some of them may be idle
		input.setLocationNumber(getLocationNumber()); 
		input.setLatency(getLatency());		
		input.setCapacities(getCapacities());
		input.setMigrationCost(getMigrationCost());
		input.setAverageLatency(getAverageLatency());
		input.setWorstLatency(getWorstLatency());
		return input;
	}

	public void stop() {
		toStop = true;
	}

	public int getSliceNumber() {
		return this.sliceIndex.size();
	}

	public int getSwitchNumber() {
		return this.switches.size();
	}

	public void queryAllSliceInfo() {
		sliceIndex = new LinkedList<String>(allocator.getSliceName());
	}

	public void querySwitches() {
		switches = allocator.getSwitches();
	}
	
	public void querySwitchStats(Long dpid) {
		SlicerMessageStats stat = allocator.getSwitchStatsByDPID(dpid);
		//switchStats.put(dpid, stat);				
		if(lastSwitchStats.containsKey(dpid)) {
			SlicerMessageStats last = lastSwitchStats.get(dpid);
			//inc = stat - last
			stat.substract(last);
		}
		switchStats.put(dpid, stat);
		lastSwitchStats.put(dpid, stat);		
	}

	public int[][] generateConsumption() {
		//generate resource consumption based on current switchStats
		int slicenum = sliceIndex.size();
		int fsnum = fsIndex.size();
		int[][] consumption = new int[fsnum][slicenum];
		for(int i = 0;i<fsnum;i++) {
			for(int j = 0;j<slicenum;j++) {
				consumption[i][j] = 0;
			}
		}		
		ArrayList<Long> allDpid = new ArrayList<Long>(switchStats.keySet());
		for(Long dpid : allDpid) {
			SlicerMessageStats ss = switchStats.get(dpid);
			int currConsumption = ss.generateConsumption(cm);

			System.out.println("dpid:" + dpid);
			int currSWIndex = fsIndex.indexOf(dpid);
			String sliceName = allocator.getControllerBySwitch(dpid);
			int currSliceIndex = sliceIndex.indexOf(sliceName);
			System.out.println("slice:" + sliceName + "-->switch:" + dpid);
			consumption[currSWIndex][currSliceIndex] = currConsumption;
		}		
		return consumption;		
	}

	public String marshalConsumption(int[][] consumption) {
		Gson gson = new Gson();
		String jstring = gson.toJson(consumption);
		return jstring;
	}

	public void updateMapping(int[] mapping) {
		//update switch -> controller mapping
		//mapping[i] = j means switch i now should be assigned to controller j
		if(mapping == null) 
			return;

		HashMap<Long, String> newMapping = new HashMap<Long, String>();
		for(int i = 0;i<mapping.length;i++) {
			Long dpid = fsIndex.get(i);
			String sliceName = sliceIndex.get(mapping[i]);
			newMapping.put(dpid, sliceName);
			System.out.println("The new mapping:" + dpid + "-->" + sliceName);
			//migrateFlowSpace(sliceName, flowspaceName);
		}
		allocator.setNewMapping(newMapping);		
	}
}
