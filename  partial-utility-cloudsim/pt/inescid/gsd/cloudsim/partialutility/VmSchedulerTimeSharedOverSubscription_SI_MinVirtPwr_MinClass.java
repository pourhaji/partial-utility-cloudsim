/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package pt.inescid.gsd.cloudsim.partialutility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.lists.PeList;

/*
 * @author Jose M. Simao
 */
public class VmSchedulerTimeSharedOverSubscription_SI_MinVirtPwr_MinClass extends VmSchedulerTimeShared {

	/**
	 * Instantiates a new vm scheduler time shared over subscription.
	 * 
	 * @param pelist the pelist
	 */
	public VmSchedulerTimeSharedOverSubscription_SI_MinVirtPwr_MinClass(List<? extends Pe> pelist) {
		super(pelist);
	}

	boolean inPhase1 = false;
	
	public void phase1Start() { inPhase1 = true; }
	public void phase1Ended() { inPhase1 = false; }
	
	protected boolean allocatePesForVm(String vmUid, List<Double> mipsShareRequested) {
		if (inPhase1) {
			if (super.allocatePesForVm(vmUid, mipsShareRequested))
				return true;
			return false;
		}
		
		
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
		double needMIPS = totalRequiredMipsByAllVms - totalAvailableMips;
		
		ArrayList<PUVm> allVMsInHost = new ArrayList<PUVm>();
		
		// Build list of available VMs
		for (String vmId : mipsMapCapped.keySet()) {
			DatacenterBroker broker = (DatacenterBroker) CloudSim.getEntity("SI-Phase1-Broker");//("PU-Datacenter-Broker");
			PUVm puVm = null;
			for (Vm vm : broker.getVmList()) {
				if (vm.getUid().equals(vmId)) {
					puVm = (PUVm) vm;
					break;
				}
			}
			if (puVm == null) {
				Log.printLine("** no vm found!! **");
				System.exit(-1);
			}
			allVMsInHost.add(puVm);
		}
		
		PUVm victim = null;
		boolean canFit = false;
		while (allVMsInHost.size() != 0) {
			// Select victim of smaller type (and with min/max available MIPS?)
			//victim = allVMsInHost.get(0);
			for (int i=0; i<VMType.values().length; ++i)
				for (PUVm vm : allVMsInHost) {
					if (((PUVm)vm).getType().equals(VMType.values()[i])) {
//						if (victim == null || (vm.getVmClass().compareTo(victim.getVmClass()) < 0 
//								&& vm.getType().compareTo(victim.getType())<0))
//								victim = vm;
						if (victim == null || vm.getVmClass().compareTo(victim.getVmClass()) < 0)
							victim = vm;
					}
				}
			// Find total MIPS of victim
			double totalMIPSOfVictim = 0;
			for (Double requestedMips : mipsMapCapped.get(victim.getUid()))
				totalMIPSOfVictim += requestedMips;
			// If this vm cannot be depreciated remove it from the list and try again
			if (totalMIPSOfVictim-needMIPS<=0) {
				allVMsInHost.remove(victim);
				victim = null;
				continue;
			} else {
				canFit = true;
				break;
			}
		}

		if (!canFit) {
			Log.printLine(" *+*+* [SI_MinVirtPwr.redistributeMipsDueToOverSubscription] Could not find VM to depreciate and fit " + needMIPS + " *+*+*");
			//System.exit(-1);
			return false;
		}
		
		// Clear the old MIPS allocation
		getMipsMap().clear();

		// Update the actual MIPS allocated to the VM
		double mipsPerPE = needMIPS / victim.getNumberOfPes();
		
		
		// Update the actual MIPS allocated to the VMs
		for (Entry<String, List<Double>> entry : mipsMapCapped.entrySet()) {
			String vmUid = entry.getKey();
			List<Double> requestedMips = entry.getValue();

			List<Double> updatedMipsAllocation = new ArrayList<Double>();
			for (Double mips : requestedMips) {
				// if vmUid is the victim, remove the necessary mipsPerPE 
				if (vmUid == victim.getUid()) {
					updatedMipsAllocation.add(mips-mipsPerPE);					
				} else {
					updatedMipsAllocation.add(mips);
				}
			}
			// add in the new map
			getMipsMap().put(vmUid, updatedMipsAllocation);
		}
		
		// As the host is oversubscribed, there no more available MIPS
		setAvailableMips(0);
		
		scheckSoundnessOfAllocation();
		
		return true;
	}

	private void scheckSoundnessOfAllocation() {
		for (Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
			List<Double> mipsShareAllocated = entry.getValue();
			double peMips = getPeCapacity();
			for (Double mips : mipsShareAllocated) {
				if (mips > peMips) {
					Log.printLine("!!!!!!!!! More MIPS than a single PE can allocate !!!!!!!!!!");
				}
			}
		}
	}

}
