
public class Entry {
	private String source;
	private String dest;
	private int nexthop;
	private float cost;
	
	//default constructor
	public Entry() {

	}

	//constructor with fields
	public Entry(String source, String dest, int nexthop, float cost) {
		super();
		this.source = source;
		this.dest = dest;
		this.nexthop = nexthop;
		this.cost = cost;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public int getNexthop() {
		return nexthop;
	}

	public void setNexthop(int nexthop) {
		this.nexthop = nexthop;
	}

	public float getCost() {
		return cost;
	}

	public void setCost(float cost) {
		this.cost = cost;
	}
	
	
	
}
