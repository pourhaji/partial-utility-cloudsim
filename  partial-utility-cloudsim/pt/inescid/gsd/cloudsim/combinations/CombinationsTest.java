package pt.inescid.gsd.cloudsim.combinations;

import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.NumberUpSupported;

import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import pt.inescid.gsd.cloudsim.partialutility.PUClass;
import pt.inescid.gsd.cloudsim.partialutility.PUInterval;
import pt.inescid.gsd.cloudsim.partialutility.PUMatrix;
import pt.inescid.gsd.cloudsim.partialutility.PUVm;
import pt.inescid.gsd.cloudsim.partialutility.PricesMatrix;
import pt.inescid.gsd.cloudsim.partialutility.UnknownPUClass;
import pt.inescid.gsd.cloudsim.partialutility.UnknownPUInterval;
import pt.inescid.gsd.cloudsim.partialutility.UnknownVMType;
import pt.inescid.gsd.cloudsim.partialutility.VMType;

class VMTracking {
	public PUVm vm;
	double mipsAllocated;
	double currDep;
	public VMTracking(PUVm vm, double mipsAllocated, double currDep) {
		this.vm = vm;
		this.mipsAllocated = mipsAllocated;
		this.currDep = currDep;
	}
}

public class CombinationsTest {
	private static final int NUMBER_OF_VMS = 16;
	private static final double MIN_INTERVAL = 0.02;
	
	public static double getOptimalRevenue(Vm[] allVMs, double mipsOfHost) {
		double revenue = 0.0;
		// Create set of VMs
		VMTracking vms[] = new VMTracking[allVMs.length];
		for (int i=0; i<allVMs.length; ++i) {
			vms[i] = new VMTracking(
						(PUVm)allVMs[i], 
						allVMs[i].getMips(), 
						0);
		}
		
		try {
			// losses array
			double currLosses[] = new double[allVMs.length];
			double minLosses[] = new double[allVMs.length];
				
			Log.printLine("Initial revenue is = " + (revenue = totalRevenue(vms)));
			
			while (getSumOfAllocatedMips(vms) > mipsOfHost) {
				for (int i=0; i<minLosses.length; ++i)
					minLosses[i] = Double.POSITIVE_INFINITY;
				double depreciation = MIN_INTERVAL; // 1%
				
				double tmpLosses[] = new double[allVMs.length];
				
				int minVm = Integer.MAX_VALUE;
				for (int i=0; i<allVMs.length;++i) {
					// if dep >= 90% skip
					if (vms[i].currDep >= 0.90)
						continue;
					
					for (int j=0; j<allVMs.length; ++j)
						tmpLosses[j] = currLosses[j];
	
					double currRevenue = revenue(vms[i]);
					double newRevenue  = revenue(vms[i], depreciation);
					
					tmpLosses[i] += currRevenue - newRevenue;
	
					// compare currLoss with minimum so far
					if (totalLoss(tmpLosses) < totalLoss(minLosses)) {
						for (int j=0; j<allVMs.length; ++j)
							minLosses[j] = tmpLosses[j];
						minVm = i;
					}
				}
				for (int j=0; j<allVMs.length; ++j)
					currLosses[j] = minLosses[j];
				
				applyDegradation(vms, minVm);
				//printVmStatus(vms, currLosses);
				//System.out.println("Current sum of allocated MIPS = " + getSumOfAllocatedMips(vms) + " target " + mipsOfHost);
				revenue = totalRevenue(vms);
				Log.printLine("Current revenue is = " + revenue);
			}
		} catch (UnknownVMType e) {
			e.printStackTrace();
		} catch (UnknownPUClass e) {
			e.printStackTrace();
		} catch (UnknownPUInterval e) {
			e.printStackTrace();
		}		
		return revenue;
	}
	
	
	public static void main(String[] args) throws UnknownVMType, UnknownPUClass, UnknownPUInterval {
		PUVm allVMs[] = new PUVm[NUMBER_OF_VMS];
		
		for (int i=0; i<NUMBER_OF_VMS; ++i) {
			allVMs[i] = 
				new PUVm(i,1,Constants.VM_MIPS[(i)%Constants.VM_TYPES],1,1024,100,100,"Xen",
					new CloudletSchedulerSpaceShared(), 
					VMType.values()[(i)%VMType.values().length]);
			allVMs[i].setVmClass(PUClass.High);
		}
		
		Log.printLine("** optimal revenue is " + getOptimalRevenue(allVMs, NUMBER_OF_VMS*1200) + " **");
	}

	private static double totalRevenue(VMTracking[] vms) throws UnknownVMType, UnknownPUClass, UnknownPUInterval {
		double total=0;
		for (VMTracking vm : vms)
			total += revenue(vm);
		return total;
	}

	private static void printVmStatus(VMTracking[] vms, double[] currLosses) {
		for (int i=0; i < vms.length; ++i)
			System.out.print(
					(vms[i].mipsAllocated-vms[i].currDep*vms[i].mipsAllocated)
					+"/"+vms[i].vm.getMips()+"("+vms[i].currDep+"/"+currLosses[i]+")");
		System.out.println();
	}

	private static void applyDegradation(VMTracking[] vms, int minVm) {
		double dep = vms[minVm].currDep + MIN_INTERVAL;
		//vms[minVm].mipsAllocated = vms[minVm].mipsAllocated - (vms[minVm].mipsAllocated * dep);
		vms[minVm].currDep += MIN_INTERVAL; 	
		if (vms[minVm].currDep > 1.0)
			vms[minVm].currDep = 1.0;
	}

	private static double totalLoss(double[] currLosses) {
		double total = 0.0;
		for (double d : currLosses) {
			total += d;
		}
		return total;
	}

	private static double revenue(VMTracking vmTrack) throws UnknownVMType, UnknownPUClass, UnknownPUInterval {
		return revenue(vmTrack, 0.0);
	}

	private static double revenue(VMTracking vmTrack, double additionalDepreciation) throws UnknownVMType, UnknownPUClass, UnknownPUInterval {
		PUVm vm = vmTrack.vm;

		//double allocatedMips = vmTrack.mipsAllocated;
		//double requestedMips = vm.getMips();//Constants.VM_MIPS[VMType.values().length-1-vm.getType().ordinal()];
		double basePrice = PricesMatrix.P.getPrice(vm.getType(), vm.getVmClass());
		double depreciation = vmTrack.currDep + additionalDepreciation;//(1-allocatedMips/requestedMips)+additionalDepreciation;
		PUInterval interval = PUInterval.getInterval(depreciation);
		PUClass puCls = vm.getVmClass();
		double pu = PUMatrix.M.getUtility(interval, puCls);
		double price = basePrice * (1-depreciation) * pu;
		
		return price;
	}

	private static double getSumOfAllocatedMips(VMTracking[] vms) {
		double totalMips = 0;
		for (VMTracking vm : vms) {
			totalMips += vm.mipsAllocated - (vm.mipsAllocated * vm.currDep);
		}
		return totalMips;
	}
}
