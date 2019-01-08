package loadBalancer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LoadBalancer {
	public static void main(String[] args) {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		ThreadForWaitingServer es = new ThreadForWaitingServer();
		es.start();
		
		try (ServerSocket ss = new ServerSocket(6666)) {
			while (true) {
				try {
					final Socket s = ss.accept();
					if (!ThreadForWaitingServer.IPList.isEmpty()) {
						pool.execute(new AnswerRequest(s));	
					}
				} catch(IOException e) {e.printStackTrace();}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pool.shutdown();
	}
}
