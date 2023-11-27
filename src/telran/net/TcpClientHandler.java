package telran.net;

import java.io.*;
import java.net.*;
public class TcpClientHandler implements Closeable, NetworkHandler {
	 Socket socket;
	 ObjectOutputStream writer;
	 ObjectInputStream reader;
	 String host;
	 int port;
	 public TcpClientHandler(String host, int port) throws Exception {
		 socket = new Socket(host, port);
		 writer = new ObjectOutputStream(socket.getOutputStream());
		 reader = new ObjectInputStream(socket.getInputStream());
		 this.host = host;
		 this.port = port;
	 }
	@Override
	public void close() throws IOException {
		socket.close();

	}
	public void reconnect() throws UnknownHostException, IOException {
		
		Socket newSocket = new Socket(host, port);
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T send(String requestType, Serializable requestData) {
		Request request = new Request(requestType, requestData);
		try {
			try {
				writer.writeObject(request);
			} catch (SocketException e) {
				reconnect();
			}
			Response response = (Response) reader.readObject();
			if(response.code() != ResponseCode.OK) {
				throw new Exception(response.code() + ": " + response.responseData().toString());
			}
			return (T) response.responseData();
			
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		
	}
	

}
