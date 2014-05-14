package pt.inescid.gsd.cloudsim.partialutility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
//import org.cloudbus.cloudsim.examples.power.Constants;

import pt.inescid.gsd.cloudsim.combinations.CombinationsTest;
import pt.inescid.gsd.cloudsim.examples.partialutility.PURunnerGeneric;


public class PUDatacenterBroker extends DatacenterBroker {

	public PUDatacenterBroker(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public double ADI;
	public double revenue,infrastructureCost;
	public long totalAllocatedMIPS;
	public long totalAllocatedRAM;
	public double optimalRevenue;
	public boolean RUN_OPTIMAL = false;
	public static double margin = 0.60;
	
	/*
	protected void submitCloudlets() {
		List<Vm> createdVMs = getVmsCreatedList();
		int vmIdx = 0; 
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm = null;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIdx);
			} else { 
				Log.printLine("PUDatacenterBroker does not support cloudlets assigned to VMs");
				System.exit(-1);;
			}
			Log.printLine(
					CloudSim.clock() + ": " + getName() + ": Sending cloudlet " + 
					cloudlet.getCloudletId() + " to VM #" + vm.getId() + " in DataCenter #" + getVmsToDatacentersMap().get(vm.getId()));
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIdx = (vmIdx + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}
	}
	*/
	
	enum State {Success, Fail, Visited}; 
	
	class VmInfo {
		public Vm vm;
		public State state;
		public VmInfo(Vm vm, State state) {
			this.vm = vm;
			this.state = state;
		}
	}
	
	protected double getRentingCost(Vm vm) throws UnknownVMType, UnknownPUClass, UnknownPUInterval {
		PUVm puVM = (PUVm) vm;
		Host h = puVM.getHost();
		double allocatedMips = h.getTotalAllocatedMipsForVm(vm);
		double requestedMips = 
				PURunnerGeneric.VM_PES[VMType.values().length-1-puVM.getType().ordinal()] 
				*
				PURunnerGeneric.VM_MIPS[VMType.values().length-1-puVM.getType().ordinal()];
		double basePrice = PricesMatrix.P.getPrice(puVM.getType(), puVM.getVmClass());
		double mipsDepreciation = 1-allocatedMips/requestedMips;

		PUInterval interval = PUInterval.getInterval(mipsDepreciation);
		PUClass puCls = puVM.getVmClass();
		double pu = PUMatrix.M.getUtility(interval, puCls);

		double price = basePrice * (1-mipsDepreciation) * pu;

		return price;
	}
	
	protected void clearDatacenters() {
		
	/* BEGIN OF Flexible SLAs */
		
		/**
		 * ADI
		 */
		ADI = 0;
		double requested = 0;
		for (Vm vm : vmsCreatedList) {
			PUVm puVM = (PUVm) vm;
			Host h = puVM.getHost();
			if (h == null) 
				continue; // happen if the VM is not allocated (base strategy)
			ADI += h.getTotalAllocatedMipsForVm(puVM);
			requested += PURunnerGeneric.VM_MIPS[VMType.values().length-1-puVM.getType().ordinal()];
		}
		ADI = 1-(ADI/requested);
		
		/**
		 * total allocated mips
		 */
		totalAllocatedMIPS = 0;
		for (Vm vm : vmsCreatedList/*vmList*/) {
			Host h = vm.getHost();
			if (h == null) continue;
			double allocatedMips = h.getTotalAllocatedMipsForVm(vm);
			totalAllocatedMIPS += allocatedMips;
			totalAllocatedRAM += h.getRamProvisioner().getAllocatedRamForVm(vm);
		}
		
		/**
		 * cost of the infrastructure
		 */
		try {
			infrastructureCost=0;
			for (Vm vm : vmsCreatedList/*vmList*/) {
				PUVm puVM = (PUVm) vm;
				Host h = vm.getHost();
				if (h == null) continue;
				double basePrice = PricesMatrix.P.getPrice(puVM.getType(), PUClass.Low);
				infrastructureCost += basePrice*margin;
			}
		} catch(Exception ex) {
			ex.printStackTrace(System.out);
			System.exit(-1);
		}
		
		/**
		 * Measure renting cost -> move to PUDatacenterBroker
		 */
		VmInfo[] info = new VmInfo[vmList.size()];
		try {
			double rentingCost = 0;
			int nVMs = 0;
			int idx = 0;
			for (Vm vm : vmList) {
				PUVm puVM = (PUVm) vm;
				Host h = vm.getHost();
				if (h == null) {
					info[idx++] = new VmInfo(vm, State.Fail);
					continue;
				}
				info[idx++] = new VmInfo(vm, State.Success);
				double allocatedMips = h.getTotalAllocatedMipsForVm(vm);
				double requestedMips = 
						PURunnerGeneric.VM_PES[VMType.values().length-1-puVM.getType().ordinal()] 
						*
						PURunnerGeneric.VM_MIPS[VMType.values().length-1-puVM.getType().ordinal()];
				double basePrice = PricesMatrix.P.getPrice(puVM.getType(), puVM.getVmClass());
				double mipsDepreciation = 1-allocatedMips/requestedMips;
				//new
				//double ramDepreciation = 1-h.getRamProvisioner().getAllocatedRamForVm(vm)/vm.getRam();
				//
				PUInterval interval = PUInterval.getInterval(mipsDepreciation);
				PUClass puCls = puVM.getVmClass();
				double pu = PUMatrix.M.getUtility(interval, puCls);
				// new
				//double price = basePrice * (1-(mipsDepreciation+ramDepreciation)/2) * pu;
				double price = basePrice * (1-mipsDepreciation) * pu;
				//
				rentingCost += price;
				nVMs++;
			}
			Log.printLine("Number of VM for renting cost " + nVMs);
			this.revenue = rentingCost;
		} catch(Exception ex) {
			ex.printStackTrace(System.out);
			System.exit(-1);
		}

		/**
		 * Measure renting cost (V2)
		 * Considers that a successful allocation after a fail is a replacement of the failing request
		 */
		double newRevenue = 0;
		try {
			for (int s=0; s<info.length;) {
				if (info[s].state == State.Visited)
					++s;
				else if (info[s].state == State.Success) {
					newRevenue += getRentingCost(info[s].vm);
					++s; 
				} else {
					int f = s;
					while (f < info.length && (info[f].state == State.Fail || info[f].state == State.Visited))
						++f;
					if (f==info.length)
						break;
					info[f].state = State.Visited;
					newRevenue += getRentingCost(info[s].vm, info[f].vm.getMips());
					++s;
				}
			} 
			System.out.println("** NEW REVENUE ** " + newRevenue);
		} catch (UnknownVMType | UnknownPUClass | UnknownPUInterval e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//// !!!!!!!!!!!!!!!!
		this.revenue = newRevenue;
		//// !!!!!!!!!!!!!!!!
			
		if (PUVm.RECORD_HISTORY) {
			System.out.println("*** PRINTING HOST UTILIZATION for " + PURunnerGeneric.currentExperience.name + "***");
			// 5. Print hosts stats
			for (Vm vm : vmList) {
				PUHost puHost = (PUHost) (vm.getHost());
				if (puHost == null)
					continue;
				System.out.print("stats for host #" + puHost.getId() + " [");
				for (double d : puHost.getUtilizationHistory()) {
					System.out.print(d + "; ");
				}
				System.out.println(" ]");
			}
		}
		
		/* 
		 * Optimal Revenue
		 */
		optimalRevenue = 0;
		if (!PURunnerGeneric.currentExperience.name.equals("FFD-OverSub")) {
			HashMap<Host, List<Vm>> mapHostToVMs = new HashMap<Host, List<Vm>>();
			for (Vm vm : getVmsCreatedList()) {
				Host h = vm.getHost();
				List<Vm> vmsInHost = mapHostToVMs.get(h);
				if (vmsInHost == null) {
					vmsInHost = new ArrayList<Vm>();
					mapHostToVMs.put(h, vmsInHost);
				}
				vmsInHost.add(vm);
			}
			for (Host h : mapHostToVMs.keySet()) {
				optimalRevenue += CombinationsTest.getOptimalRevenue(
						mapHostToVMs.get(h).toArray(new Vm[] {}), 
						h.getTotalMips());
			}	
		}
		
		/* END OF Flexible SLAs */
		
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

	private double getRentingCost(Vm vm, double mips) throws UnknownVMType, UnknownPUClass, UnknownPUInterval {
		PUVm puVM = (PUVm) vm;
		Host h = puVM.getHost();
		double allocatedMips = mips;//h.getTotalAllocatedMipsForVm(vm);
		double requestedMips = 
				PURunnerGeneric.VM_PES[VMType.values().length-1-puVM.getType().ordinal()] 
				*
				PURunnerGeneric.VM_MIPS[VMType.values().length-1-puVM.getType().ordinal()];
		double basePrice = PricesMatrix.P.getPrice(puVM.getType(), puVM.getVmClass());
		double mipsDepreciation = 1-allocatedMips/requestedMips;

		PUInterval interval = PUInterval.getInterval(mipsDepreciation);
		PUClass puCls = puVM.getVmClass();
		double pu = PUMatrix.M.getUtility(interval, puCls);

		double price = basePrice * (1-mipsDepreciation) * pu;

		return price;
	}
	
}
