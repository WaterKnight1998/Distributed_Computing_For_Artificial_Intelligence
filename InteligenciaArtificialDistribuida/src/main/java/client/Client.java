package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.nd4j.linalg.api.ndarray.INDArray;

import gradientDescent.UtilitiesGradientDescent;

public class Client {
	public static void main(String[] args) {
		Scanner reader = new Scanner(System.in).useLocale(Locale.US);
		File traningData = null;
		String lineRead = null;

		do {
			System.out.print("Name of the file for training: ");
			if (reader.hasNextLine()) {
				lineRead = reader.nextLine();
				traningData = new File(lineRead);
			}

		} while (!traningData.isFile());

		int numberOfIterations = 0;
		do {
			System.out.print("Number of iterations: ");
			if (reader.hasNextInt()) numberOfIterations = reader.nextInt();
		} while (numberOfIterations <= 0);

		Double alpha = null;
		do {
			System.out.print("Alpha value(decimal separator '.'): ");
			if (reader.hasNextDouble()) alpha = reader.nextDouble();
		} while (alpha <= 0);
		
		System.out.println();
		
		ObjectOutputStream o = null;
		ObjectInputStream i = null;
		
		try (Socket s = new Socket(Client.getValidIp(), 6666)) {
			o = new ObjectOutputStream(s.getOutputStream());
			i = new ObjectInputStream(s.getInputStream());
			
			o.writeObject(UtilitiesGradientDescent.loadMatrixFromFile(lineRead));
			o.writeInt(numberOfIterations);
			o.writeDouble(alpha);
			o.flush();
			
			INDArray theta = (INDArray) i.readObject();
			System.out.println(theta);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (i != null) i.close();
				if (o != null) o.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		reader.close();
	}
	
	
	private static final String getValidIp() {
		String ipLoadBalancer = "";
		try(Scanner readerFromKeyboard = new Scanner(System.in)){
			Pattern ipPattern = Pattern.compile(
			        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
			do {
				System.out.print("Introduce IP from Load Balancer: ");
				if(readerFromKeyboard.hasNextLine()) {
					ipLoadBalancer = readerFromKeyboard.nextLine();
				}
			}while(!ipPattern.matcher(ipLoadBalancer).matches());
			
		}
		return ipLoadBalancer;
	}
}
