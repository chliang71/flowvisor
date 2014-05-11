package org.flowvisor.scheduler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;

public class InformationCollector {
	
	Selector selector;
	ServerSocketChannel ssc;
	AtomicInteger channelId;
	Gson gson;
	HashMap<Integer, int[][]> matrixMap;
	HashMap<Integer, String> updateInfo;
	int[] switchNum;
	int currentNumofSwitch;
	int currentNumofController;
	Thread collector;
	boolean stopCollecting;
	
	public InformationCollector() {
		gson = new Gson();
		channelId = new AtomicInteger(0);
		matrixMap = new HashMap<Integer, int[][]>();
		updateInfo = new HashMap<Integer, String>();
		stopCollecting = false;
	}
	
	public void startCollecting() {
		collector = new Thread(new CollectorThread());
		collector.start();
	}
	
	public void stopCollecting() {
		stopCollecting = true;
	}
	
	public int[][] retriveMatrix() {
		if(matrixMap.keySet().size() == 0) {
			return null;
		}
		//return the merge matrix from all channels
		//first of all, we know that all matrixes have the 
		//same number of columns(each fv is connected to all controllers)
		//but the number of rows needs to be added
		int ncolumns = 0;
		int nrows = 0;
		int index = 0;
		for(int[][] m : matrixMap.values()) {
			nrows += m.length;
			ncolumns = m[0].length;
			switchNum[index] = m.length;
			System.out.println("i:" + index + " "+ m.length);
			index ++;
		}
		int[][] total = new int[nrows][ncolumns];
		int offset = 0;
		for(int i = 0;i<nrows;i++)
			for(int j = 0;j<ncolumns;j++)
				total[i][j] = 0;
		for(int[][] m : matrixMap.values()) {
			for(int i = 0;i<m.length;i++)
				for(int j = 0;j<m[0].length;j++)
					total[i+offset][j] = m[i][j];
			offset += m.length;
		}
		currentNumofSwitch = nrows;
		currentNumofController = ncolumns;
		return total;
	}
	
	public int getNumSwitch() {
		return currentNumofSwitch;
	}
	
	public int getNumControllre() {
		return currentNumofController;
	}
	
	public void buildUpdate(int[] assignment) {
		System.out.println("Update received!:" + assignment.length);
		if(assignment.length != currentNumofSwitch) {
			System.out.println("PANIC! Inconsistent mapping");
			return;
		}
		int remaining = assignment.length;
		int index = 0;
		while(remaining != 0) {
			int[] cur = new int[switchNum[index]];
			for(int i=0;i<cur.length;i++) {
				cur[i] = assignment[assignment.length - remaining];
				remaining--;
			}
			String s = gson.toJson(cur);
			System.out.println("update:" + (index+1) + "-->" + s.trim());
			synchronized (updateInfo) {
				updateInfo.put((index+1), s.trim());
			}
			index ++;
		}
	}
	
	class CollectorThread implements Runnable {
		@Override
		public void run() {

			try {
				selector = Selector.open();
				ssc = ServerSocketChannel.open(); 
				ssc.configureBlocking(false);  
				ssc.socket().setReuseAddress(true);
				ssc.socket().bind(new InetSocketAddress(12345)); 
				ssc.register(selector, SelectionKey.OP_ACCEPT, "Server"); 

				while(!stopCollecting) {
					selector.select();  
					for(SelectionKey key : selector.selectedKeys()) {
						if (!key.isValid())
							continue;
						if((key.readyOps() & SelectionKey.OP_ACCEPT)
							     == SelectionKey.OP_ACCEPT) {
							ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
							SocketChannel sc = ssc.accept();
							sc.configureBlocking( false );
							sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new AtomicInteger(channelId.addAndGet(1)));
							
							synchronized(selector) {
								if(switchNum == null) 
									switchNum = new int[1];
								else 
									switchNum = new int[switchNum.length + 1];
							}
						} else if ((key.readyOps() & SelectionKey.OP_READ)
							     == SelectionKey.OP_READ) {
						     // Read the data
						     SocketChannel sc = (SocketChannel)key.channel();
						     ByteBuffer buffer = ByteBuffer.allocate(1024);
						     int id = ((AtomicInteger)key.attachment()).get();
						     int n = sc.read(buffer);
						     if(n == -1) {
						    	 System.out.println("-1 got from id:" + id + " peer quited?");
						    	 key.cancel();
						     } else {
						    	 buffer.flip();
						    	 String s = new String(buffer.array());
						    	 System.out.println("collector:" + s.trim() + "|" + n + " bytes from " + id);

						    	 buffer.clear();
						    	 int[][] matrix = gson.fromJson(s.trim(), int[][].class);
						    	 matrixMap.put(id, matrix);
						     }						     
						} else if ((key.readyOps() & SelectionKey.OP_WRITE)
							     == SelectionKey.OP_WRITE) {
							//SocketChannel sc = (SocketChannel)key.channel();
							//ByteBuffer buffer = ByteBuffer.wrap("Looks good".getBytes());
						    //sc.write(buffer);	
							int id = ((AtomicInteger)key.attachment()).get();
							String info = null;
							synchronized (updateInfo) {
								if(updateInfo.containsKey(id)) {
									info = updateInfo.get(id);
									updateInfo.remove(id);
								}
							}
							if(info != null) {
								System.out.println("somthing to write back to id:" + id + ":" + info + "|");
								info += " ";
								SocketChannel sc = (SocketChannel)key.channel();
								ByteBuffer buffer = ByteBuffer.wrap(info.getBytes());
							    sc.write(buffer);								    
							}
						}
					} 
					selector.selectedKeys().clear();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
