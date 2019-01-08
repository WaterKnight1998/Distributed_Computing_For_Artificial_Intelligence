package loadBalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThreadForWaitingServer extends Thread {
	protected static List<InetSocketAddress> IPList;
	
	public ThreadForWaitingServer() {
		IPList=Collections.synchronizedList(new ArrayList<>());
	}
	
	@Override
	public void run() {
		try(ServerSocket ss = new ServerSocket(7777)) {
			while(true) {
				try(Socket s = ss.accept()){
					InetSocketAddress ip = new InetSocketAddress(s.getInetAddress(), s.getPort() + 1); //WTF +1????
					ThreadForWaitingServer.IPList.add(ip);
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
