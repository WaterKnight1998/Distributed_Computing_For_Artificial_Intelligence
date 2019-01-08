package node;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.nd4j.linalg.api.ndarray.INDArray;

import gradientDescent.UtilitiesGradientDescent;

public class MakeCalculus extends Thread {
	
	private Socket socket;
	
	public MakeCalculus(Socket socket) {
		this.socket=socket;
	}
	
	@Override
	public void run() {
		 //It throws an excepcion when a node is pinged, as you can see in the next line we are waiting for an INDArray to arrive at the node
		 try (ObjectInputStream i = new ObjectInputStream(this.socket.getInputStream());
			  ObjectOutputStream o = new ObjectOutputStream(this.socket.getOutputStream())){
				System.out.println("Node");
				System.out.println("Blocked theta");
				INDArray theta = (INDArray) i.readObject();
				
				System.out.println("Blocked data");
				INDArray data = (INDArray) i.readObject();
				
				
				INDArray X= UtilitiesGradientDescent.obtainrX(data);
				INDArray y= UtilitiesGradientDescent.obtainY(data);
				INDArray res = UtilitiesGradientDescent.auxSumInNode(theta, X, y);
				
				o.writeObject(res);
				o.flush();
		} catch (EOFException e) {
			System.out.println("The load balancer made me a ping");
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		 
		if(this.socket != null) {
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
