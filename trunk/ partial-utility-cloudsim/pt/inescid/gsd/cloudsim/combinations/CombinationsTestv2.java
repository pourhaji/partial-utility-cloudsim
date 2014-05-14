//package pt.inescid.gsd.cloudsim.combinations;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.print.attribute.standard.NumberUpSupported;
//
//import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
//import org.cloudbus.cloudsim.Vm;
//import org.cloudbus.cloudsim.examples.power.Constants;
//import org.paukov.combinatorics.Factory;
//import org.paukov.combinatorics.Generator;
//import org.paukov.combinatorics.ICombinatoricsVector;
//
//import pt.inescid.gsd.cloudsim.partialutility.PUClass;
//import pt.inescid.gsd.cloudsim.partialutility.PUInterval;
//import pt.inescid.gsd.cloudsim.partialutility.PUMatrix;
//import pt.inescid.gsd.cloudsim.partialutility.PUVm;
//import pt.inescid.gsd.cloudsim.partialutility.PricesMatrix;
//import pt.inescid.gsd.cloudsim.partialutility.UnknownPUClass;
//import pt.inescid.gsd.cloudsim.partialutility.UnknownPUInterval;
//import pt.inescid.gsd.cloudsim.partialutility.UnknownVMType;
//import pt.inescid.gsd.cloudsim.partialutility.VMType;
//
//class VMTracking {
//	public PUVm vm;
//	double mipsAllocated;
//	double currDep;
//	public VMTracking(PUVm vm, double mipsAllocated, double currDep) {
//		this.vm = vm;
//		this.mipsAllocated = mipsAllocated;
//		this.currDep = currDep;
//	}
//}
//
//public class CombinationsTestv2 {
//	private static final int NUMBER_OF_VMS = 10;
//	private static final int MIPS_OF_HOST = 10*1200;
//	private static final double MIN_INTERVAL = 0.01;
//	
//	public static void main(String[] args) throws UnknownVMType, UnknownPUClass, UnknownPUInterval {
//		// Create set of VMs
//		VMTracking vms[] = new VMTracking[NUMBER_OF_VMS];
//		for (int i=0; i<NUMBER_OF_VMS; ++i) {
//			vms[i] = new VMTracking(
//						new PUVm(i,1,Constants.VM_MIPS[(i+3)%Constants.VM_TYPES],1,1024,100,100,"Xen",
//								new CloudletSchedulerSpaceShared(), 
//								VMType.values()[(i+3)%VMType.values().length]), 
//						Constants.VM_MIPS[(i+3)%Constants.VM_TYPES], 
//						0);
//			vms[i].vm.setVmClass(PUClass.values()[(i+3)%PUClass.values().length]);
//		}
//		
//		// losses array
//		double currLosses[] = new double[NUMBER_OF_VMS];
//		double minLosses[] = new double[NUMBER_OF_VMS];
//		
//		// permutations of vms with minimum loss
//		ICombinatoricsVector<VMTracking> minPermutation = null;
//		
//		ICombinatoricsVector<VMTracking> initialVector = 
//			Factory.createVector(vms);
//		
//		while (getSumOfAllocatedMips(vms) > MIPS_OF_HOST) {
//			for (int i=0; i<minLosses.length; ++i)
//				minLosses[i] = Double.POSITIVE_INFINITY;
//			//for (int numberOfCombination = 1; numberOfCombination<=MIPS_OF_HOST; ++numberOfCombination) {
//				double depreciation = MIN_INTERVAL; // 1%
//				//Generator<VMTracking> gen = Factory.createSimpleCombinationGenerator(initialVector, numberOfCombination);
//				//for (ICombinatoricsVector<VMTracking> perm : gen) {
//					// print tested permutations
//					// System.out.println("testing permutation " + perm);
//				int minVm = Integer.MAX_VALUE;
//				for (int i=0; i<NUMBER_OF_VMS;++i) {
//					// apply depreciations
//					/*for (VMTracking vmTrack : perm) {
//						double currRevenue = revenue(vmTrack);
//						double newRevenue = revenue(vmTrack, depreciation);
//						currLosses[vmTrack.vm.getId()] = currRevenue - newRevenue;
//					}*/
//					double currRevenue = revenue(vms[i]);
//					double newRevenue = revenue(vms[i], depreciation);
//					currLosses[i] += currRevenue - newRevenue;
//					// compare currLoss with minimum so far
//					if (totalLoss(currLosses) < totalLoss(minLosses)) {
//						for (int j=0; j<NUMBER_OF_VMS; ++j)
//							minLosses[j] = currLosses[j];
//						//minPermutation = perm;
//						minVm = i;
//					}
//				}
//				//}
//			//}
//			//applyDegradation(vms, minPermutation);
//			applyDegradation(vms, minVm);
//			printVmStatus(vms);
//			System.out.println("Current sum of allocated MIPS = " + getSumOfAllocatedMips(vms) + " target " + MIPS_OF_HOST);
//		}
//	}
//
//	private static void printVmStatus(VMTracking[] vms) {
//		for (VMTracking vm : vms) {
//			System.out.print(vm.mipsAllocated+"/"+vm.vm.getMips()+" ");
//		}
//		System.out.println();
//	}
//
//	private static void applyDegradation(VMTracking[] vms, int minVm) {
//		double dep = vms[minVm].currDep + MIN_INTERVAL;
//		vms[minVm].mipsAllocated = vms[minVm].mipsAllocated - (vms[minVm].mipsAllocated * dep);
//		vms[minVm].currDep += MIN_INTERVAL; 	
//	}
//
//	private static void applyDegradation(VMTracking[] vms, ICombinatoricsVector<VMTracking> minPermutation) {
//		for (VMTracking vm : minPermutation) {
//			double dep = vms[vm.vm.getId()].currDep + MIN_INTERVAL;
//			vms[vm.vm.getId()].mipsAllocated = vms[vm.vm.getId()].mipsAllocated - (vms[vm.vm.getId()].mipsAllocated * dep);
//			vms[vm.vm.getId()].currDep += MIN_INTERVAL;
//		}
//	}
//
//	private static double totalLoss(double[] currLosses) {
//		double total = 0.0;
//		for (double d : currLosses) {
//			total += d;
//		}
//		return total;
//	}
//
//	private static double revenue(VMTracking vmTrack) throws UnknownVMType, UnknownPUClass, UnknownPUInterval {
//		return revenue(vmTrack, 0.0);
//	}
//
//	private static double revenue(VMTracking vmTrack, double additionalDepreciation) throws UnknownVMType, UnknownPUClass, UnknownPUInterval {
//		PUVm vm = vmTrack.vm;
//
//		//double allocatedMips = vmTrack.mipsAllocated;
//		//double requestedMips = vm.getMips();//Constants.VM_MIPS[VMType.values().length-1-vm.getType().ordinal()];
//		double basePrice = PricesMatrix.P.getPrice(vm.getType(), vm.getVmClass());
//		double depreciation = vmTrack.currDep + additionalDepreciation;//(1-allocatedMips/requestedMips)+additionalDepreciation;
//		PUInterval interval = PUInterval.getInterval(depreciation);
//		PUClass puCls = vm.getVmClass();
//		double pu = PUMatrix.M.getUtility(interval, puCls);
//		double price = basePrice * (1-depreciation) * pu;
//		
//		return price;
//	}
//
//	private static double getSumOfAllocatedMips(VMTracking[] vms) {
//		double totalMips = 0;
//		for (VMTracking vm : vms) {
//			totalMips += vm.mipsAllocated;
//		}
//		return totalMips;
//	}
//}
