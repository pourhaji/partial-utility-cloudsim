package pt.inescid.gsd.cloudsim.partialutility;

public class UnknownVMType extends Exception {
	private VMType type;
	
	public UnknownVMType(VMType type) {
		this.type = type;
	}
	
	public VMType getVMType() {
		return type;
	}

}
