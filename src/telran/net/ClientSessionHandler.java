package telran.net;
import java.net.*;
import java.io.*;
public class ClientSessionHandler implements Runnable {
 Socket socket;
 ObjectInputStream reader;
 ObjectOutputStream writer;
 ApplProtocol protocol;
 TcpServer tcpServer;
 public static  int TOTAL_IDLE_TIMEOUT = 0;
 private final int LIMIT_IDLE_TIME = 60000;
 public ClientSessionHandler(Socket socket, ApplProtocol protocol, TcpServer tcpServer) throws Exception {
	 this.socket = socket;
	 this.protocol = protocol;
	 this.tcpServer = tcpServer;
	 reader = new ObjectInputStream(socket.getInputStream());
	 writer = new ObjectOutputStream(socket.getOutputStream());
 }
	@Override
	public void run() {
			boolean isSleeping = false;
			while(!tcpServer.executor.isShutdown() && !isSleeping) {
				
				try {
					Request request = (Request) reader.readObject();
					if(request != null) {
						TOTAL_IDLE_TIMEOUT = 0;
					}
					Response response = protocol.getResponse(request);
					writer.writeObject(response);
					writer.reset();
				} catch(SocketTimeoutException e) {
					//for exit from readObject to another iteration of cycle
					TOTAL_IDLE_TIMEOUT += TcpServer.IDLE_TIMEOUT;
					if(TOTAL_IDLE_TIMEOUT > LIMIT_IDLE_TIME ) {
						if(TcpServer.counter.get() > tcpServer.getnThreads()) {
							isSleeping = true;
						}
					}
				}
				catch (EOFException e) {
					TcpServer.counter.decrementAndGet();
					System.out.println("Client closed connection");
				} 
				catch (Exception e) {
					System.out.println("Abnormal closing connection");
				}
			}
			
		} 


}
