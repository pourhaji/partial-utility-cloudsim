package pt.inescid.gsd.cloudsim.partialutility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;

/*
 * @author Jose M. Simao
 */
public class VmAllocationPolicySimple_PU_DoublePhase extends VmAllocationPolicySimple {

	public VmAllocationPolicySimple_PU_DoublePhase(List<? extends Host> list) {
		super(list);
		for (Host h : list) {
			VmSchedulerTimeShared sched = (VmSchedulerTimeShared) h.getVmScheduler();
			if (sched instanceof VmSchedulerTimeSharedOverSubscription_SI_MinVirtPwr_MinClass)
				((VmSchedulerTimeSharedOverSubscription_SI_MinVirtPwr_MinClass)sched).phase1Start();
			if (sched instanceof VmSchedulerTimeSharedOverSubscription_SI_MaxVirtPwr_MinClass)
				((VmSchedulerTimeSharedOverSubscription_SI_MaxVirtPwr_MinClass)sched).phase1Start();
		}
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		List<Host> copyList = new LinkedList<Host>();
		for (Host h : getHostList())
			copyList.add(h);
		
		int requiredPes = vm.getNumberOfPes();
		boolean result = false;
		int tries = 0;
		List<Integer> freePesTmp = new ArrayList<Integer>();
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}
		
		// distribution of available MIPS
		int availableHosts = 0;
		for (Host h : getHostList())
			if (h.getTotalMips() == h.getAvailableMips())
				availableHosts++;
		stats.addValue(availableHosts);
		// -----
		
		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				int moreFree = Integer.MIN_VALUE;
				int idx = -1;

				// we want the host with less pes in use
				for (int i = 0; i < freePesTmp.size(); i++) {
					if (freePesTmp.get(i) > moreFree) {
						moreFree = freePesTmp.get(i);
						idx = i;
					}
				}

				Host host = getHostList().get(idx);
				result = host.vmCreate(vm);

				if (result) { // if vm were succesfully created in the host
					getVmTable().put(vm.getUid(), host);
					getUsedPes().put(vm.getUid(), requiredPes);
					getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
					result = true;
					break;
				} else {
					freePesTmp.set(idx, Integer.MIN_VALUE);
				}
				tries++;
			} while (!result && tries < getFreePes().size());

			if (result == false) {
				// hack
				for (Host h : copyList) {
					VmSchedulerTimeShared sched = (VmSchedulerTimeShared) h.getVmScheduler();
					if (sched instanceof VmSchedulerTimeSharedOverSubscription_SI_MinVirtPwr_MinClass)
						((VmSchedulerTimeSharedOverSubscription_SI_MinVirtPwr_MinClass)sched).phase1Ended();
					if (sched instanceof VmSchedulerTimeSharedOverSubscription_SI_MaxVirtPwr_MinClass)
						((VmSchedulerTimeSharedOverSubscription_SI_MaxVirtPwr_MinClass)sched).phase1Ended();
				}
				
				while (copyList.size()>0) {
					/*Host max = copyList.get(0);
					for (Host h : copyList) {
						PUHost puHost = (PUHost) h;
						if (puHost.getAvailableMips() > max.getAvailableMips() && 
							puHost.getNumberOfFreePes() >= max.getNumberOfFreePes())
							max = puHost;
					}
					if (allocateHostForVm(vm, max))
						return true;
					else
						copyList.remove(0);
					*/
					int maxPEs=0;
					double maxResources = 0;
					Host maxResourcedHost = null;
					for (Host h : copyList) {
						if (h.getAvailableMips() >= maxResources && h.getNumberOfFreePes() >= maxPEs) {
							maxResources = h.getAvailableMips();
							maxPEs = h.getNumberOfFreePes();
							maxResourcedHost = h;
						}
					}
					if (maxResourcedHost==null) {
						for (Host h : copyList) {
							if (h.getTotalMips() > maxResources) {
								maxResources = h.getAvailableMips();
								maxResourcedHost = h;
							}
						}
					}
					if (allocateHostForVm(vm, maxResourcedHost))
						return true;
					else
						copyList.remove(0);
				}
			}
		}

		return result;
	}
	
}
