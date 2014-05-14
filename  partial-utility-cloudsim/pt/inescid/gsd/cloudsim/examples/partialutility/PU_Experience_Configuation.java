package pt.inescid.gsd.cloudsim.examples.partialutility;

import java.util.HashMap;

import org.cloudbus.cloudsim.VmAllocationPolicy;

import pt.inescid.gsd.cloudsim.partialutility.PUClass;
import pt.inescid.gsd.cloudsim.partialutility.VMType;



public class PU_Experience_Configuation {
	public String name;
	// Host allocation 
	public Class<?> allocationToHost;
	// VMM scheduler - MIPS and RAM
	public Class<?> vmScheduler;
	public Class<?> ramProvisioner;
//	// VM scheduler
//	public Class<?> cloudletScheduler;
	// percentage of each user class
	public HashMap<PUClass, Double> vmClassPercentage;
	// percentage of each VM type
	public HashMap<VMType, Double> vmTypePercentage;
	public boolean runOptimal;
	
	public PU_Experience_Configuation(
			String name, 
			Class<?> allocationToHost, 
			Class<?> vmScheduler, 
			Class<?> ramProvisioner,
//			Class<?> cloudletScheduler,
			double[] vmClassPercentage,
			double[] vmTypePercentage)
	{
		this.name				= name;
		this.allocationToHost 	= allocationToHost;
		this.vmScheduler		= vmScheduler;
		this.ramProvisioner     = ramProvisioner;
//		this.cloudletScheduler	= cloudletScheduler;
		this.vmClassPercentage	= new HashMap<>();
		this.runOptimal			= false;
		int i=0;
		for (PUClass cls : PUClass.values())
			this.vmClassPercentage.put(cls, vmClassPercentage[i++]);
		
		this.vmTypePercentage	= new HashMap<>();
		i=0;
		for (VMType type : VMType.values())
			this.vmTypePercentage.put(type, vmTypePercentage[i++]);
	}
	
	public PU_Experience_Configuation(
			String name, 
			Class<?> allocationToHost, 
			Class<?> vmScheduler, 
			Class<?> ramProvisioner,
//			Class<?> cloudletScheduler,
			double[] vmClassPercentage,
			double[] vmTypePercentage,
			boolean runOptimal)
	{
		this(name, allocationToHost, vmScheduler, ramProvisioner, vmClassPercentage, vmTypePercentage);
		this.runOptimal = runOptimal;
	}
}
