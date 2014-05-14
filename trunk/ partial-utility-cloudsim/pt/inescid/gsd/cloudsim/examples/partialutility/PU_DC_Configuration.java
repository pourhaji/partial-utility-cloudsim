package pt.inescid.gsd.cloudsim.examples.partialutility;

public class PU_DC_Configuration {
	public int numberOfHosts;
	public int hostTypes;
	public int[] hostPES;
	public int[] hostMIPS;
	public int[] hostRAM;
	
	public PU_DC_Configuration(int numberOfHosts, int hostTypes, int[] hostPES, int[] hostMIPS, int[] hostRAM) {
		this.numberOfHosts = numberOfHosts;
		this.hostTypes 	= hostTypes;
		this.hostPES	= hostPES;
		this.hostMIPS	= hostMIPS;
		this.hostRAM	= hostRAM;
	}


}
