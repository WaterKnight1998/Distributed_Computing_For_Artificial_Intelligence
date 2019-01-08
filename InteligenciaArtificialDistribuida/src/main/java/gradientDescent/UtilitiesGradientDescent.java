package gradientDescent;

import java.io.IOException;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

public class UtilitiesGradientDescent {
	
	//return the matrix that is in the file
	public static INDArray loadMatrixFromFile(String ruta) throws IOException {
		return Nd4j.readNumpy(ruta,",");
	}
	
	public static INDArray gradientDescent(int numberOfIterations, double alpha,INDArray theta,INDArray X, INDArray y,INDArray data) {
		INDArray aux = Nd4j.zeros(X.columns(), 1);
		for(int iter=1;iter<=numberOfIterations;iter++) {
			aux = theta.sub(X.mmul(theta).sub(y).transpose().mmul(X).transpose().mul((1.0/data.rows())*alpha));
			theta = aux;
		}
		
		return theta;
	}
	
	public static INDArray auxSumInNode(INDArray theta,INDArray X, INDArray y) {
		return X.mmul(theta).sub(y).transpose().mmul(X).transpose();
	}
	
	public static INDArray obtainrX(INDArray data) {
		 return Nd4j.concat(1, Nd4j.ones(data.rows(),1),data.get(NDArrayIndex.all(),NDArrayIndex.interval(0, data.columns()-1)));       
	}
	
	public static INDArray obtainY(INDArray data) {
		return data.get(NDArrayIndex.all(),NDArrayIndex.interval(data.columns()-1, data.columns()));
	}
}
