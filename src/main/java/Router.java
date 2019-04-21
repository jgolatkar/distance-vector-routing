import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Router implements Runnable {

	private RoutingTable routingTable;
	private MulticastSocket socket = null;
	private long WAIT_TIME = 15000;
	private int port;
	private String hostName;

	// constructor
	public Router(String hostName) {
		this.hostName = hostName;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length < 2) {
			System.err.println("Router arguments missing");
			System.exit(0);
		}

		// create multicast socket connection
		// pass port number from command line arguments
		Router router = new Router(args[1]);
		MulticastSocket socket = router.setupConnection(Integer.parseInt(args[0]));

		// read neighbor data from file
		router.initRoutingTable(args[2]);

		Thread thread = new Thread(router);
		thread.start();

		while (true) {
			byte[] buf = new byte[2048];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			// blocks until a packet is received
			socket.receive(packet);
			buf = packet.getData();
			ByteArrayInputStream byteInStream = new ByteArrayInputStream(buf);
			ObjectInputStream obInStream = new ObjectInputStream(new BufferedInputStream(byteInStream));
			try {
				RoutingTable receivedTable = (RoutingTable) obInStream.readObject();
				obInStream.close();
				RoutingTable ownTable = router.getRoutingTable();
				// check if received packet is from immediate neighbor

				List<String> immNeighbors = ownTable.getNeighbors();
				immNeighbors.remove(router.getHostName());
				if (immNeighbors.contains(receivedTable.getHostName())) {
					ownTable.updateRoutingTable(ownTable, receivedTable);
				}

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	// read data from the file to populate routing table
	private void initRoutingTable(String file) throws IOException {
		this.routingTable = new RoutingTable(file);
		routingTable.populateTable(hostName);
	}

	private MulticastSocket setupConnection(int port) {
		// write start log
		try {
			socket = new MulticastSocket(port);
			this.port = port;
			InetAddress grpAddress = InetAddress.getByName("227.5.6.7");
			socket.joinGroup(grpAddress); // join multicast group
			System.out.println("Router is up...");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return socket;
	}

	// sender thread
	@Override
	public void run() {
		// send data every five seconds
		int count = 0;
		while (true) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
			routingTable.linkCostChange(); // check for link cost change
			InetAddress group = null;
			try {
				group = InetAddress.getByName("227.5.6.7");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream obOutStream = null;

			try {
				obOutStream = new ObjectOutputStream(outputStream);
				obOutStream.writeObject(routingTable);
				obOutStream.close();
				byte[] buff = outputStream.toByteArray();
				DatagramPacket packet = new DatagramPacket(buff, buff.length, group, port);
				socket.send(packet);
				System.out.println("Output Number " + ++count + ":");
				routingTable.printTable((ArrayList<Entry>) routingTable.getTable());
				System.out.println();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public RoutingTable getRoutingTable() {
		return routingTable;
	}

	public void setRoutingTable(RoutingTable routingTable) {
		this.routingTable = routingTable;
	}

}
