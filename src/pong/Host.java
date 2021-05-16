package pong;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Host extends Thread {
	private InetAddress[] ip;
	public ServerSocket server;
	private Socket guest;

	public Host() throws Exception {
		ip = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
		server = new ServerSocket(0);
	}

	@Override
	public void run() {
		try {
			guest = server.accept();
		} catch (SocketException e2) {

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getIP() {
		String s = "";
		for (InetAddress i : ip)
			if (!i.toString().contains(":"))
				s += i.toString() + "\n";
		return s;
	}
}