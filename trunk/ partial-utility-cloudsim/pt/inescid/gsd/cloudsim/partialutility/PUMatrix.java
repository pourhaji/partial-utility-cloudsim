package pt.inescid.gsd.cloudsim.partialutility;

import java.util.HashMap;

public class PUMatrix {
	
	public static PUMatrix M = new PUMatrix();
	
	private HashMap<PUInterval,HashMap<PUClass,Double>> matrix;

	public PUMatrix() {
		matrix = new HashMap<>();
		
		/* test matrix */
		HashMap<PUClass,Double> line0 = new HashMap<>();
		line0.put(PUClass.Low,1.0);
		line0.put(PUClass.Medium,1.0);
		line0.put(PUClass.High,1.0);
		matrix.put(PUInterval.None, line0);
		
		/* 6-Maio-2014 */
		HashMap<PUClass,Double> line1 = new HashMap<>();
		line1.put(PUClass.Low,1.0);
		line1.put(PUClass.Medium,1.0);
		line1.put(PUClass.High,0.8);
		matrix.put(PUInterval.I1, line1);
		
		HashMap<PUClass,Double> line2 = new HashMap<>();
		line2.put(PUClass.Low,0.9);
		line2.put(PUClass.Medium,0.8);
		line2.put(PUClass.High,0.6);
		matrix.put(PUInterval.I2, line2);
		
		HashMap<PUClass,Double> line3 = new HashMap<>();
		line3.put(PUClass.Low,0.8);
		line3.put(PUClass.Medium,0.6);
		line3.put(PUClass.High,0.2);
		matrix.put(PUInterval.I3, line3);

		HashMap<PUClass,Double> line4 = new HashMap<>();
		line4.put(PUClass.Low,0.6);
		line4.put(PUClass.Medium,0.4);
		line4.put(PUClass.High,0.0);
		matrix.put(PUInterval.I4, line4);

		HashMap<PUClass,Double> line5 = new HashMap<>();
		line5.put(PUClass.Low,0.2);
		line5.put(PUClass.Medium,0.0);
		line5.put(PUClass.High,0.0);
		matrix.put(PUInterval.I5, line5);
		
		HashMap<PUClass,Double> line6 = new HashMap<>();
		line6.put(PUClass.Low,0.0);
		line6.put(PUClass.Medium,0.0);
		line6.put(PUClass.High,0.0);
		matrix.put(PUInterval.All, line6);
		/* ----------- */
		
		/* 5-Maio-2014*/
		/*
		HashMap<PUClass,Double> line1 = new HashMap<>();
		line1.put(PUClass.Low,1.0);
		line1.put(PUClass.Medium,1.0);
		line1.put(PUClass.High,0.8);
		matrix.put(PUInterval.I1, line1);
		
		HashMap<PUClass,Double> line2 = new HashMap<>();
		//line2.put(PUClass.Low,0.9);
		line2.put(PUClass.Low,1.0);
		//line2.put(PUClass.Medium,0.8);
		line2.put(PUClass.Medium,1.0);
		line2.put(PUClass.High,0.6);
		matrix.put(PUInterval.I2, line2);
		
		HashMap<PUClass,Double> line3 = new HashMap<>();
		//line3.put(PUClass.Low,0.8);
		line3.put(PUClass.Low,0.9);
		//line3.put(PUClass.Medium,0.6);
		line3.put(PUClass.Medium,0.8);
		line3.put(PUClass.High,0.2);
		matrix.put(PUInterval.I3, line3);

		HashMap<PUClass,Double> line4 = new HashMap<>();
		//line4.put(PUClass.Low,0.6);
		line4.put(PUClass.Low,0.8);
		//line4.put(PUClass.Medium,0.4);
		line4.put(PUClass.Medium,0.6);
		line4.put(PUClass.High,0.0);
		matrix.put(PUInterval.I4, line4);

		HashMap<PUClass,Double> line5 = new HashMap<>();
		//line5.put(PUClass.Low,0.2);
		line5.put(PUClass.Low,0.6);
		//line5.put(PUClass.Medium,0.0);
		line5.put(PUClass.Medium,0.4);
		line5.put(PUClass.High,0.0);
		matrix.put(PUInterval.I5, line5);
		
		HashMap<PUClass,Double> line6 = new HashMap<>();
		line6.put(PUClass.Low,0.0);
		line6.put(PUClass.Medium,0.0);
		line6.put(PUClass.High,0.0);
		matrix.put(PUInterval.All, line6);
		/* ----------- */
	}
	
	public double getUtility(PUInterval intv, PUClass puClass) throws UnknownPUClass, UnknownPUInterval {
		HashMap<PUClass,Double> line = matrix.get(intv);
		if (line == null) 
			throw new UnknownPUInterval(intv);
		Double pu = line.get(puClass);
		if (pu == null) 
			throw new UnknownPUClass(puClass);
		return pu;
	}
}
