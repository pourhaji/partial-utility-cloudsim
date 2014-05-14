package pt.inescid.gsd.cloudsim.partialutility;

public class UnknownPUClass extends Exception {

	private PUClass puClass;

	public UnknownPUClass(PUClass cls) {
		this.puClass = cls;
	}
	
	public PUClass getUnknownClass() {
		return puClass;
	}

}
