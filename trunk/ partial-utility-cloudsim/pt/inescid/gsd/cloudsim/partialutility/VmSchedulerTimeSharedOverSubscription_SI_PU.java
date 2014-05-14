/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package pt.inescid.gsd.cloudsim.partialutility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.lists.PeList;

/*
 * @author Jose M. Simao
 */
public class VmSchedulerTimeSharedOverSubscription_SI_PU extends VmSchedulerTimeShared {

	/**
	 * Instantiates a new vm scheduler time shared over subscription.
	 * 
	 * @param pelist the pelist
	 */
	public VmSchedulerTimeSharedOverSubscription_SI_PU(List<? extends Pe> pelist) {
		super(pelist);
	}

	@Override
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShareRequested) {
		/**
		 * TODO: add the same to RAM and BW provisioners
		 */
		if (vm.isInMigration()) {
			if (!getVmsMigratingIn().contains(vm.getUid()) && !getVmsMigratingOut().contains(vm.getUid())) {
				getVmsMigratingOut().add(vm.getUid());
			}
		} else {
			if (getVmsMigratingOut().contains(vm.getUid())) {
				getVmsMigratingOut().remove(vm.getUid());
			}
		}
		boolean result = allocatePesForVm(vm.getUid(), mipsShareRequested);
		updatePeProvisioning();
		return result;
	}	
	/**
	 * Allocate pes for vm. The policy allows over-subscription. In other words, the policy still
	 * allows the allocation of VMs that require more CPU capacity that is available.
	 * Oversubscription results in performance degradation. 
	 * Each virtual PE cannot be allocated more CPU capacity than MIPS of a single PE.
	 * 
	 * @param vmUid the vm uid
	 * @param mipsShareRequested the mips share requested
	 * @return true, if successful
	 */
	@Override
	protected boolean allocatePesForVm(String vmUid, List<Double> mipsShareRequested) {
		double totalRequestedMips = 0;

		// if the requested mips is bigger than the capacity of a single PE, we cap
		// the request to the PE's capacity
		List<Double> mipsShareRequestedCapped = new ArrayList<Double>();
		double peMips = getPeCapacity();
		for (Double mips : mipsShareRequested) {
			if (mips > peMips) {
				mipsShareRequestedCapped.add(peMips);
				totalRequestedMips += peMips;
			} else {
				mipsShareRequestedCapped.add(mips);
				totalRequestedMips += mips;
			}
		}

//		for (Double mips : mipsShareRequested) {
//			totalRequestedMips += mips;
//		}
			
		getMipsMapRequested().put(vmUid, mipsShareRequested);
		setPesInUse(getPesInUse() + mipsShareRequested.size());

		if (getVmsMigratingIn().contains(vmUid)) {
			// the destination host only experience 10% of the migrating VM's MIPS
			totalRequestedMips *= 0.1;
		}
		
		boolean result = true;
		if (getAvailableMips() >= totalRequestedMips) {
			List<Double> mipsShareAllocated = new ArrayList<Double>();
			for (Double mipsRequested : mipsShareRequestedCapped) {
				if (getVmsMigratingOut().contains(vmUid)) {
					// performance degradation due to migration = 10% MIPS
					mipsRequested *= 0.9;
				} else if (getVmsMigratingIn().contains(vmUid)) {
					// the destination host only experience 10% of the migrating VM's MIPS
					mipsRequested *= 0.1;
				}
				mipsShareAllocated.add(mipsRequested);
			}

			getMipsMap().put(vmUid, mipsShareAllocated);
			setAvailableMips(getAvailableMips() - totalRequestedMips);
		} else {
			result = redistributeMipsDueToOverSubscription();
		}

		return result;
	}

	/**
	 * This method recalculates distribution of MIPs among VMs considering eventual shortage of MIPS
	 * compared to the amount requested by VMs.
	 * @param mipsShareRequested 
	 * @param vmUid 
	 */
	protected boolean redistributeMipsDueToOverSubscription() {
		// First, we calculate the scaling factor - the MIPS allocation for all VMs will be scaled
		// proportionally
		double totalRequiredMipsByAllVms = 0;

		Map<String, List<Double>> mipsMapCapped = new HashMap<String, List<Double>>();
		for (Entry<String, List<Double>> entry : getMipsMapRequested().entrySet()) {

			double requiredMipsByThisVm = 0.0;
			String vmId = entry.getKey();
			List<Double> mipsShareRequested = entry.getValue();
			List<Double> mipsShareRequestedCapped = new ArrayList<Double>();
			double peMips = getPeCapacity();
			for (Double mips : mipsShareRequested) {
				if (mips > peMips) {
					mipsShareRequestedCapped.add(peMips);
					requiredMipsByThisVm += peMips;
				} else {
					mipsShareRequestedCapped.add(mips);
					requiredMipsByThisVm += mips;
				}
			}

			mipsMapCapped.put(vmId, mipsShareRequestedCapped);

			if (getVmsMigratingIn().contains(entry.getKey())) {
				// the destination host only experience 10% of the migrating VM's MIPS
				requiredMipsByThisVm *= 0.1;
			}
			totalRequiredMipsByAllVms += requiredMipsByThisVm;
		}

		double totalAvailableMips = PeList.getTotalMips(getPeList());
		//double scalingFactor = totalAvailableMips / totalRequiredMipsByAllVms;
		
		// needed MIPS
		double neededMips = totalRequiredMipsByAllVms - totalAvailableMips;
		
		// compute capacity of all VMs by classes
		HashMap<String, PUVm> allVMsMapInHost = listOfAvailableVMs(mipsMapCapped);
		Collection<PUVm> allVMsInHost = allVMsMapInHost.values();
		double classesMips[] = new double[PUClass.values().length];
		double classesCounters[] = new double[PUClass.values().length];
		for (PUVm vm : allVMsInHost) {
			// Find total MIPS of vm
			double totalMIPSOfVm = 0;
			for (Double requestedMips : mipsMapCapped.get(vm.getUid()))
				totalMIPSOfVm += requestedMips;
			classesMips[vm.getVmClass().ordinal()] += totalMIPSOfVm;
			classesCounters[vm.getVmClass().ordinal()] += 1;
		}
		boolean fullByClasses = true;
		double smallest = Integer.MAX_VALUE;
		int smallestIdx = -1;
		for (int i=0; i<PUClass.values().length; ++i) {
			if (classesMips[i]-neededMips > 0 /* and all VMs are above their minimum utility*/) {
				fullByClasses = false;
				if (smallest > classesMips[i]-neededMips) {
					smallest = classesMips[i]-neededMips;
					smallestIdx = i;
				}
			}
		}
		
		if (fullByClasses) {
			Log.printLine("** no class in this host can be depreciated enough **");
			Log.printLine("** neededMips = " + neededMips + " **");
			Log.printLine("** totalAvailableMips = " + totalAvailableMips + " **");
			Log.printLine("** totalRequiredMipsByAllVms = " + totalRequiredMipsByAllVms + " **");
			//System.exit(-1);
			return false;
		}

		double scalingFactor = smallest / classesMips[smallestIdx];
		PUClass classOfVictims = PUClass.values()[smallestIdx];

		// Clear the old MIPS allocation
		getMipsMap().clear();

		// Update the actual MIPS allocated to the VMs
		for (Entry<String, List<Double>> entry : mipsMapCapped.entrySet()) {
			String vmUid = entry.getKey();
			List<Double> requestedMips = entry.getValue();

			List<Double> updatedMipsAllocation = new ArrayList<Double>();
			for (Double mips : requestedMips) {
				// check if VM is of selected class 
				PUVm vm = allVMsMapInHost.get(vmUid);
				if (vm.getVmClass().equals(classOfVictims)) {
					updatedMipsAllocation.add(mips*scalingFactor);					
				} else {
					updatedMipsAllocation.add(mips);
				}
			}
			// add in the new map
			getMipsMap().put(vmUid, updatedMipsAllocation);
		}

		// As the host is oversubscribed, there no more available MIPS
		setAvailableMips(0);
		
		checkSoundnessOfAllocation();
		
		return true;
	}
	
	// Build list of available VMs
	private HashMap<String, PUVm> listOfAvailableVMs(Map<String, List<Double>> mipsMapCapped) {
		HashMap<String, PUVm> allVMsInHost = new  HashMap<String, PUVm>();
		for (String vmUid : mipsMapCapped.keySet()) {
			DatacenterBroker broker = (DatacenterBroker) CloudSim.getEntity("BestFit-PU-Broker");
			PUVm puVm = null;
			for (Vm vm : broker.getVmList()) {
				if (vm.getUid().equals(vmUid)) {
					puVm = (PUVm) vm;
					break;
				}
			}
			if (puVm == null) {
				Log.printLine("** no vm found for id " + vmUid + "!! **");
				System.exit(-1);
			}
			allVMsInHost.put(vmUid, puVm);
		}
		return allVMsInHost;
	}
	
	private void checkSoundnessOfAllocation() {
		for (Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
			List<Double> mipsShareAllocated = entry.getValue();
			double peMips = getPeCapacity();
			for (Double mips : mipsShareAllocated) {
				if (mips > peMips) {
					Log.printLine("!!!!!!!!! More MIPS than a single PE can allocate !!!!!!!!!!");
					System.exit(-1);
				}
			}
		}
	}

}
