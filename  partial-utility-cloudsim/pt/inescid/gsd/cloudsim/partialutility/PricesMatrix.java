package pt.inescid.gsd.cloudsim.partialutility;

import java.util.HashMap;

public class PricesMatrix {
	
	public static PricesMatrix P = new PricesMatrix(); 
	
	private HashMap<VMType,HashMap<PUClass,Double>> matrix;
	
	public PricesMatrix() {
		matrix = new HashMap<>();
		
		/* test matrix */
		HashMap<PUClass,Double> line1 = new HashMap<>();
		line1.put(PUClass.Low,0.26);
		line1.put(PUClass.Medium,0.32);
		line1.put(PUClass.High,0.4);
		matrix.put(VMType.micro, line1);
		
		HashMap<PUClass,Double> line2 = new HashMap<>();
		line2.put(PUClass.Low,0.51);
		line2.put(PUClass.Medium,0.64);
		line2.put(PUClass.High,0.8);
		matrix.put(VMType.small, line2);
		
		HashMap<PUClass,Double> line3 = new HashMap<>();
		line3.put(PUClass.Low,1.02);
		line3.put(PUClass.Medium,1.28);
		line3.put(PUClass.High,1.6);
		matrix.put(VMType.regular, line3);

		HashMap<PUClass,Double> line4 = new HashMap<>();
		line4.put(PUClass.Low,1.54);
		line4.put(PUClass.Medium,1.92);
		line4.put(PUClass.High,2.4);
		matrix.put(VMType.extra, line4);
		/* ----------- */ 
	}
	
	public double getPrice(VMType type, PUClass cls) throws UnknownVMType, UnknownPUClass {
		HashMap<PUClass,Double> line = matrix.get(type);
		if (line == null) throw new UnknownVMType(type);
		Double price = line.get(cls);
		if (price == null) throw new UnknownPUClass(cls);
		return price;
	}
	
}
