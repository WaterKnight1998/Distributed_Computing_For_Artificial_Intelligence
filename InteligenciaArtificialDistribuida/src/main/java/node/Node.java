package node;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Node {
	public static void main(String[] args) {
		Node.connectToLoadBalancer();
		ExecutorService pool = Executors.newFixedThreadPool(10);
		
		try (ServerSocket ss = new ServerSocket(0)){
			System.out.println("Port Number: " + ss.getLocalPort());
			
			while (true) {
				try {
					final Socket s = ss.accept();
					pool.execute(new MakeCalculus(s));	
				} catch(IOException e) {e.printStackTrace();}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		pool.shutdown();
	}
	
	private static void connectToLoadBalancer() {
		String ipLoadBalancer = "";
		
		try (Scanner readerFromKeyboard = new Scanner(System.in)){
			Pattern ipPattern = Pattern.compile(
			        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
			do {
				System.out.print("Introduce IP from Load Balancer: ");
				if(readerFromKeyboard.hasNextLine()) {
					ipLoadBalancer = readerFromKeyboard.nextLine();
				}
			} while(!ipPattern.matcher(ipLoadBalancer).matches());
			
			Node.initialPing(ipLoadBalancer);
		}
	}
	
	private static void initialPing(String ipBalancer) {
		try (Socket s = new Socket(ipBalancer, 7777)) {//Set manually IP of Load Balancer
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
