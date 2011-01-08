package com.raphfrk.craftproxy;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;


public class PassthroughConnection implements Runnable {

	String hostname = null;
	int port = -1;
	String password = null;

	Socket socketToClient = null;
	Socket socketToServer = null;

	SocketBridge upstreamBridge;
	SocketBridge downstreamBridge;

	MyBoolean threadsStarted = new MyBoolean(false);

	void kill() {

		synchronized(threadsStarted) {
			while( !threadsStarted.get() ) {
				try {
					threadsStarted.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		if( upstreamBridge != null ) {
			upstreamBridge.kill();
		}

		if( downstreamBridge != null ) {
			downstreamBridge.kill();
		}

	}


	PassthroughConnection( Socket socket, String hostname, int port, String password ) {
		this.socketToClient = socket;
		this.hostname = hostname;
		this.port = port;
		this.password = password;
	}

	public void run() {
		
		DataInputStream inputFromClient = null;
		DataOutputStream outputToClient = null;

		try {
			inputFromClient = new DataInputStream( socketToClient.getInputStream() );
		} catch (IOException e) {
			System.out.println("Unable to open data stream to client");
			if( inputFromClient != null ) {
				try {
					inputFromClient.close();
				} catch (IOException e1) {
					System.out.println("Unable to close data stream to client");
				}
			}
			informEnd();
			return;
		}

		try {
			outputToClient = new DataOutputStream( socketToClient.getOutputStream() );
		} catch (IOException e) {
			System.out.println("Unable to open data stream from client");
			if( outputToClient != null ) {
				try {
					outputToClient.close();
				} catch (IOException e1) {
					System.out.println("Unable to close data stream from client");
				}
			}
			informEnd();
			return;
		}
		
		//System.out.println( "Carrying out input handshake");
		//Protocol.processLogin(inputFromClient, outputToClient);


		try {
			System.out.println("Attempting to connect to: " + hostname + ":" + port );
			socketToServer = new Socket();
			socketToServer.connect(new InetSocketAddress(hostname, port), 60000);
		} catch (ConnectException ce) {
			System.out.println( "Unable to connect to server at " + hostname + ":" + port);
			try {
				System.out.println( "Closing client connection");
				DataOutputStream outData = new DataOutputStream( socketToClient.getOutputStream() );
				
				ArrayList<Byte> kick = Protocol.genKickPacket(
						"Unable to open connection to target server");
				
				outData.write(Protocol.tobytes(kick));
				outData.flush();
				socketToClient.close();
			} catch (IOException e) {
				System.out.println( "Unable to close client connection");
			}
			informEnd();
			return;
		} catch (IOException e) {
			informEnd();
			return;
		}

		DataInputStream inputFromServer = null;
		DataOutputStream outputToServer = null;
		
		System.out.println( "Attempting to establish data streams");
		try {
			inputFromServer = new DataInputStream( socketToServer.getInputStream() );
		} catch (IOException e) {
			System.out.println("Unable to open data stream to server");
			if( inputFromServer != null ) {
				try {
					inputFromServer.close();
				} catch (IOException e1) {
					System.out.println("Unable to close data stream to server");
				}
			}
			informEnd();
			return;
		}

		try {
			outputToServer = new DataOutputStream( socketToServer.getOutputStream() );
		} catch (IOException e) {
			System.out.println("Unable to open data stream from server");
			if( outputToServer != null ) {
				try {
					outputToServer.close();
				} catch (IOException e1) {
					System.out.println("Unable to close data stream from server");
				}
			}
			informEnd();
			return;
		}
		
		System.out.println( "Streams established");
		
		upstreamBridge = new SocketBridge( inputFromClient, outputToServer, new UpstreamMonitor(), false );
		downstreamBridge = new SocketBridge( inputFromServer, outputToClient, new DownstreamMonitor(), true );

		Thread t1 = new Thread( upstreamBridge );
		Thread t2 = new Thread( downstreamBridge );
		t1.start();
		t2.start();

		
		informEnd();
	}

	void informEnd() {

		synchronized(threadsStarted) {
			threadsStarted.set(true);
			threadsStarted.notify();
		}
	}

}
