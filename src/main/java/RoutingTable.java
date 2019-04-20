import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutingTable implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<String> neighbors; // directly connected neighbors
	private List<String> knownNodes; // nodes known to the router
	private Map<String, Float> immNeighborCost;
	private Float infinity = 16.0F;
	private List<Entry> table; // routing table
	private String filename;
	private String hostName;

	// constructor with file name
	public RoutingTable(String filename) {
		this.filename = filename;
		this.neighbors = new ArrayList<>();
		this.table = new ArrayList<>();
		knownNodes = new ArrayList<>();
		immNeighborCost = new HashMap<>();
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public List<String> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(List<String> neighbors) {
		this.neighbors = neighbors;
	}

	public List<Entry> getTable() {
		return table;
	}

	public void setTable(List<Entry> table) {
		this.table = table;
	}

	public String getFile() {
		return filename;
	}

	public void setFile(String filename) {
		this.filename = filename;
	}

	// populate table
	public void populateTable(String routerName) throws IOException {
		this.hostName = routerName;
		// neighbors.put(routerName, 0.0F);
		File file = new File(this.filename);
		BufferedReader br;
		String line;
		try {
			br = new BufferedReader(new FileReader(file));
			line = br.readLine();
			Entry entry = new Entry(routerName, routerName, "-", 0.0F); // self entry
			table.add(entry);
			knownNodes.add(routerName);
			immNeighborCost.put(routerName, 0.0F);
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\\s+");
				if (parts.length == 2) {
					String neighbor = parts[0];
					Float cost = Float.parseFloat(parts[1]);
					Entry e = new Entry(routerName, neighbor, neighbor, cost);
					table.add(e);
					neighbors.add(neighbor);
					knownNodes.add(neighbor);
					immNeighborCost.put(neighbor, cost);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	// upon receiving new table, update the current table
	public synchronized void updateRoutingTable(RoutingTable ownTable, RoutingTable newTable) {
		// loop through the new routing table entries

		for (Entry newEntry : newTable.getTable()) {
			// create new entry if new route is found
			if (!knownNodes.contains(newEntry.getDest())) {
				Entry entry = new Entry(ownTable.getHostName(), newEntry.getDest(), newTable.getHostName(),
						newEntry.getCost() + immNeighborCost.get(newTable.getHostName()));
				ownTable.getTable().add(entry);
				knownNodes.add(newEntry.getDest());
			} else {
				/*
				 * check route in current own table that goes to the same destination and
				 * compare cost
				 */
				Float costCurr;
				for (Entry ownEntry : ownTable.getTable()) {

					if (ownEntry.getDest().equals(newEntry.getDest())) {
						// add cost to source of new route and new source to destination and compare it
						// with cost of route to dest in the current table
						costCurr = ownEntry.getCost();
						for (Entry entryToSource : ownTable.getTable()) {
							if (entryToSource.getDest().equals(newEntry.getSource())) {
								Float updatedCost = entryToSource.getCost() + newEntry.getCost();
								if (updatedCost < costCurr) {
									ownEntry.setCost(updatedCost);
									ownEntry.setNexthop(entryToSource.getDest());
									break;
								}
							}
						}
						break;
					}
				}

			}
		}

	}

	public String getHostName() {
		return hostName;
	}

	// method to print table
	public synchronized void printTable(ArrayList<Entry> t) {
		for (Entry entry : t) {
			System.out.println("Shortest path " + entry.getSource() + "-" + entry.getDest() + ": the next hop is "
					+ entry.getNexthop() + " and the cost is " + entry.getCost());
		}
	}

	// method to check link cost change in the file
	public synchronized void linkCostChange() {
		FileReader fileReader;
		BufferedReader bufferedReader;
		try {
			fileReader = new FileReader(new File(filename));
			bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			while ((line = bufferedReader.readLine()) != null) {
				String[] parts = line.split("\\s+");
				if (parts.length == 2) {
					String neighbor = parts[0];
					Float cost = Float.parseFloat(parts[1]);
					Entry e = new Entry(hostName, neighbor, neighbor, cost);

					if (immNeighborCost.containsKey(neighbor)) {
						if (immNeighborCost.get(neighbor).compareTo(cost) != 0) {
							// link change detected, update the cost in map and in entry list
							System.out.println("-- Link Cost Change Detected -- \n");
							immNeighborCost.put(neighbor, cost);
							for (Entry currEntry : table) {
								if (currEntry.getDest().equals(e.getDest())) {
									currEntry.setCost(cost);
									currEntry.setNexthop(e.getDest());
									break;
								}
							}
						}
					} else {
						immNeighborCost.put(neighbor, cost);
						table.add(e);
						knownNodes.add(neighbor);
					}

				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
