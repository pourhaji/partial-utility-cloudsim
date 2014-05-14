package pt.inescid.gsd.cloudsim.examples.partialutility;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.util.MathUtil;

import pt.inescid.gsd.cloudsim.combinations.CombinationsTest;
import pt.inescid.gsd.cloudsim.partialutility.PUDatacenterBroker;
import pt.inescid.gsd.cloudsim.partialutility.PUHost;
import pt.inescid.gsd.cloudsim.partialutility.PUVm;
import pt.inescid.gsd.cloudsim.partialutility.PricesMatrix;
import pt.inescid.gsd.cloudsim.partialutility.UnknownPUClass;
import pt.inescid.gsd.cloudsim.partialutility.UnknownVMType;
import pt.inescid.gsd.cloudsim.partialutility.VMType;
import pt.inescid.gsd.cloudsim.partialutility.PUHost.FailureCause;

public class PUGenericRunnerStatisticsHelper {

	
	public static void printVMInfo(Datacenter datacenter,
			PUDatacenterBroker broker, List<Vm> vmList, int nVMs,
			PU_Experience_Configuation experience,
			PU_DC_Configuration dcConfig, BufferedWriter vmData)
			throws IOException, UnknownVMType, UnknownPUClass 
	{
		// 1. Collect statistics
		long totalRequestedMIPS = PURunnerGeneric.getTotalRequestedMIPS(vmList);
		long availableMIPS = PURunnerGeneric.getDatacenterAvailableMIPS(datacenter);
		List<Vm> notAllocated = new LinkedList<Vm>();
		List<Vm> allocated = new LinkedList<Vm>();
		for (Vm vm : vmList) {
			if (vm.isBeingInstantiated())
				notAllocated.add(vm);
			else
				allocated.add(vm);
		}
		
		int failedVMsMIPS = 0;
		double lostRevenue = 0;
		for (Vm vm : notAllocated) {
			failedVMsMIPS += vm.getMips();
			PUVm puVm = (PUVm) vm;
			lostRevenue += PricesMatrix.P.getPrice(puVm.getType(), puVm.getVmClass());
		}
	
		//long totalAllocatedMIPS = totalRequestedMIPS-failedVMsMIPS;
		// MIPS
		long totalAllocatedMIPS = broker.totalAllocatedMIPS;
		double requestedRatio, allocatedRatio;
		// RAM
		//long totalRequestRAM = PURunnerGeneric.getTotalRAM(vmList);
		long totalAllocatedRAM =  broker.totalAllocatedRAM;//PURunnerGeneric.getTotalRAM(allocated);
		long availableRAM = PURunnerGeneric.getDatacenterAvailableRAM(datacenter);
		double allocatedRatioDCRAM = totalAllocatedRAM/(double)availableRAM;
		
		int failedByMIPS=0, failedByRAM=0;
		HashSet<String> history = new HashSet<String>();
		for (Host h : datacenter.getHostList()) {
			PUHost ph = (PUHost) h;
			HashMap<Vm,PUHost.FailureCause> vms = ph.getAllocationFailedVM();
			for (Entry<Vm,PUHost.FailureCause> e : vms.entrySet()) {
				if (!notAllocated.contains(e.getKey())) 
					continue;
				//if (history.contains(e.getKey().getId()+e.getValue().toString())) continue;
				if (history.contains(e.getKey().getUid())) continue;
				if (e.getValue().equals(PUHost.FailureCause.RAM)) {
					failedByRAM++;				
					history.add(e.getKey().getUid());
					continue;
				}
				if (e.getValue().equals(PUHost.FailureCause.MIPS)) {
					failedByMIPS++;				
					//history.add(e.getKey()+e.getValue().toString());
					history.add(e.getKey().getUid());
					continue;
				}
			}
		}
		
		//public enum VMType {micro, small, regular, extra};
		int extra=0, regular=0, small=0, micro=0;
		for (Vm vm : notAllocated) {
			if (vm.getMips() == PURunnerGeneric.VM_MIPS[0])
				extra++;
			else if (vm.getMips() == PURunnerGeneric.VM_MIPS[1])
				regular++;
			else if (vm.getMips() == PURunnerGeneric.VM_MIPS[2])
				small++;
			else if (vm.getMips() == PURunnerGeneric.VM_MIPS[3])
				micro++;
			else
				throw new RuntimeException("No VM size found!!");
		}

		// Get a DescriptiveStatistics instance
		DescriptiveStatistics stats = new DescriptiveStatistics();
		// Add the data from the array
		List<Double> allMIPS = PURunnerGeneric.getDatacenterListAvailableMIPS(datacenter);
		for(Double d : allMIPS)
			stats.addValue(d);
		
		// median of available MIPS
		double avgAvailableMIPS = stats.getMean();
		// average available MIPS
		double medianAvailableMIPS = stats.getStandardDeviation();
				
		double optimalRevenue=0, optimalRevenueGlobal = 0; 

		for (Host h : datacenter.getHostList()) {
			optimalRevenue += CombinationsTest.getOptimalRevenue(
					h.getVmList().toArray(new Vm[] {}), 
					h.getTotalMips());
		}
			
		/*
		double totalMips = 0;
		for (Host h : mapHostToVMs.keySet()  ) {
			totalMips+= h.getTotalMips();
		}
		*/
			
		Vm allVMsInHost[] = new Vm[allocated.size()];
		for (int i=0; i<allVMsInHost.length; ++i)
			allVMsInHost[i] = allocated.get(i);
		optimalRevenueGlobal = CombinationsTest.getOptimalRevenue(allVMsInHost, availableMIPS);
		
		
		// 2. Print statistics
		Log.printLine("+ Statitics for " + experience + " +");
		Log.printLine("Failed Vms = " + notAllocated.size());
		Log.printLine("Failed VMs required (MIPS) = " + failedVMsMIPS);
		Log.printLine("Total requested MIPS = " + totalRequestedMIPS);
		Log.printLine("Total Available MIPS = " + availableMIPS);
		Log.printLine("Datacenter requested ratio = " + (requestedRatio=totalRequestedMIPS/(double)availableMIPS));
		Log.printLine("Datacenter allocated ratio = " + (allocatedRatio=totalAllocatedMIPS/(double)availableMIPS));
		Log.printLine("Renting cost = " + broker.revenue);
		Log.printLine("ADI = " + broker.ADI);
		
		// 3. Write VM results
		//String data = nHosts+";"+nVms+";"+/*nCloudlets*/1000+";"+broker.infrastructureCost+";"+broker.rentingCost+";"+RoI+";"+profit+";"+requestedRatio+";"+allocatedRatio+";"+availableMIPS+";"+totalRequestedMIPS+";"+(availableMIPS-totalRequestedMIPS)+";"+totalAllocatedMIPS+";"+failedVMsMIPS+";";
		/*		vmData.write(
			"nhosts;nVMs;revenue;averageMIPS;medianMIPS;"+
			"requestedRatio;allocatedMIPSRatio;allocatedRAMRatio;"+
			"totalMIPS;allocatedMIPS;failedMIPS;"+
			"LostRevenue;NetRevenue;"+
			"failedByMIPS;failedByRAM;" + 
			"extra;regular;small;micro;" + 
			"optimalRevenue;optimalRevenueGlobal;"+
			"totalFailed\n");*/
		vmData.write(
				dcConfig.numberOfHosts+";"+nVMs+";"+broker.revenue+";"+
				avgAvailableMIPS+";"+medianAvailableMIPS+";"+
				requestedRatio+";"+allocatedRatio+";"+allocatedRatioDCRAM+";"+
				availableMIPS+";"+totalAllocatedMIPS+";"+failedVMsMIPS+";"+
				lostRevenue+";"+(broker.revenue-lostRevenue)+";"+
				failedByMIPS+";"+failedByRAM+";"+
				extra+";"+regular+";"+small+";"+micro+";"+
				/*broker.optimalRevenue+";"+broker.optimalRevenueGlobal+";"+*/
				broker.optimalRevenue+";"+optimalRevenueGlobal+";"+
				(failedByMIPS+failedByRAM)+"\n");
	}


	// Compromised with Constants array order
	static VMType[] types = { VMType.extra, VMType.regular, VMType.small, VMType.micro };

	
	static void printCloudletInfo(List<Cloudlet> cloudletList, int numOfVMs, BufferedWriter cloudletData) throws IOException {
		long totalExecTime=0, totalWaitTime=0;
		cloudletData.write(cloudletList.size()+";"+numOfVMs+";");
		for (Cloudlet c : cloudletList) {
			totalExecTime += c.getActualCPUTime();
			totalWaitTime += c.getWaitingTime();
			//cloudletData.write(c.getActualCPUTime()+";");
		}
		Log.printLine("Number of cloudlets " + cloudletList.size());
		Log.printLine("Cloudlets average exec time " + totalExecTime/cloudletList.size());
		Log.printLine("Cloudlets average wait time " + totalWaitTime/cloudletList.size());
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (Cloudlet c : cloudletList) {
			totalExecTime += c.getActualCPUTime();
			totalWaitTime += c.getWaitingTime();
			stats.addValue(c.getActualCPUTime()+c.getWaitingTime());
		}
		cloudletData.write(
				totalWaitTime/cloudletList.size()+";"+
				totalExecTime/cloudletList.size()+";"+
				//(totalWaitTime+totalExecTime)/cloudletList.size()+";"+
				stats.getMean()+";"+
				stats.getPercentile(50)+"\n");
		
	}

}
