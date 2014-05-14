package pt.inescid.gsd.cloudsim.partialutility;

public class UnknownPUInterval extends Exception {
	private PUInterval intv;
	
	public UnknownPUInterval(PUInterval intv) {
		this.intv = intv;
	}

	public PUInterval getUnknownInterval() {
		return intv;
	}
}
