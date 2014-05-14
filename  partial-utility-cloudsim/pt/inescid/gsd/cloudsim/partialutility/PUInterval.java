package pt.inescid.gsd.cloudsim.partialutility;

public class PUInterval {
	
	public static PUInterval None = new PUInterval(0, 0);
	public static  PUInterval I1 = new PUInterval(0.001, 0.2);
	public static  PUInterval I2 = new PUInterval(0.2, 0.4);
	public static  PUInterval I3 = new PUInterval(0.4, 0.6);
	public static  PUInterval I4 = new PUInterval(0.6, 0.8);
	public static  PUInterval I5 = new PUInterval(0.8, 1);
	public static  PUInterval All = new PUInterval(1.0, 1.0);
	
	private static PUInterval[] allIntervals = {None, I1, I2, I3, I4, I5, All};
	
	private double begin, end;
	
	public PUInterval(double begin, double end) {
		this.begin = begin;
		this.end = end;
	}
	public boolean contains(double value) {
		return value >= begin && value < end;
	}
	public static PUInterval getInterval(double depreciation) {
		if (depreciation == 0.0)
			 return None;
		if (depreciation >= 1.0)
			return All;
		for (PUInterval intrv : allIntervals) {
			if (intrv.contains(depreciation)) return intrv;
		}
		return null;
	}
	
	public int hashCode() {
		return 0;
	}
}
