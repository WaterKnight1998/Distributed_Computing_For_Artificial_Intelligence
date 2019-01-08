package loadBalancer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import gradientDescent.UtilitiesGradientDescent;

public class AnswerRequest extends Thread {
	
	private Socket socket;
	
	public AnswerRequest(Socket socket) {
		this.socket=socket;
	}
	
	@Override
	public void run() {
		try(ObjectOutputStream o = new ObjectOutputStream(this.socket.getOutputStream())){
			try(ObjectInputStream i = new ObjectInputStream(this.socket.getInputStream())){
				try {
					//Declaration of variables
					//TrainingData
					INDArray data = (INDArray) i.readObject();
					INDArray X = UtilitiesGradientDescent.obtainrX(data);
					INDArray theta = Nd4j.zeros(X.columns(), 1);
					
					//NumberIterations
					int numIteraciones = i.readInt();
					
					//Aplha
					double alpha = i.readDouble();

					//Division in data segments (data/numberNodes)
					for(int iter = 1; iter <= numIteraciones; iter++) {
						INDArray aux = Nd4j.zeros(X.columns(), 1);
						
						this.removeDisconnectedNodesFromIpList();
						List<INDArray> dataSegment = this.splitData(data);
						synchronized (ThreadForWaitingServer.IPList) {
							for(int j = 0; j < ThreadForWaitingServer.IPList.size(); j++) {
								try(Socket sockAux = new Socket(ThreadForWaitingServer.IPList.get(j).getAddress(), ThreadForWaitingServer.IPList.get(j).getPort());
									ObjectOutputStream ob = new ObjectOutputStream(sockAux.getOutputStream());){
										ob.writeObject(theta);
										ob.flush();
										ob.writeObject(dataSegment.get(j));
										ob.flush();
										//reading aux value calculated at the computing node
										try(ObjectInputStream on = new ObjectInputStream(sockAux.getInputStream())){ 
											INDArray res = (INDArray) on.readObject();
											aux = aux.add(res);
										}catch(IOException e) {e.printStackTrace();iter--;}
								}catch(IOException e) {e.printStackTrace();iter--;}
							}
							theta = theta.sub(aux.mul((1.0/data.rows())*alpha));
						}
					}
					
					o.writeObject(theta);
					o.flush();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {e.printStackTrace();}
		} catch (IOException e) {e.printStackTrace();}
		
		if(this.socket!=null) {
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void removeDisconnectedNodesFromIpList() {
		boolean state;
		synchronized (ThreadForWaitingServer.IPList) {
			Iterator<InetSocketAddress> iterator = ThreadForWaitingServer.IPList.iterator();
			while (iterator.hasNext()) {
				InetSocketAddress aux = iterator.next();
				state = true;
				
				try (Socket s = new Socket(aux.getAddress(), aux.getPort())) {
					
				} catch (IOException e) {
					e.printStackTrace();
					state = false;
				}
				
				if (!state) {
					iterator.remove();;
				}
			}
		}
	}
	
	private List<INDArray> splitData(INDArray data){
		List<INDArray> dataSegments = new ArrayList<>();
		synchronized (ThreadForWaitingServer.IPList) {
			if(ThreadForWaitingServer.IPList.size()!=0) {
				int numberRowsSegment = data.rows() / ThreadForWaitingServer.IPList.size(); 
				int rowsAccu = 0;

				//Division in data segments (data/numberNodes)
				for (int j = 0; j < (ThreadForWaitingServer.IPList.size() - 1); j++) {
					dataSegments.add(data.get(NDArrayIndex.interval(rowsAccu, rowsAccu+numberRowsSegment), NDArrayIndex.all()));
					rowsAccu+=numberRowsSegment;
				}
				dataSegments.add(data.get(NDArrayIndex.interval(rowsAccu, data.rows()), NDArrayIndex.all()));
			} else {
				dataSegments.add(data);
			}
			
			return dataSegments;
		}
	}
	
}
