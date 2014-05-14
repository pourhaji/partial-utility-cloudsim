package pt.inescid.gsd.cloudsim.partialutility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

import pt.inescid.gsd.cloudsim.examples.partialutility.PURunnerGeneric;

/*
 * @author Jose M. Simao
 */
public class VmAllocationPartialUtility_BestFit extends VmAllocationPolicySimple {

	public VmAllocationPartialUtility_BestFit(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
//		List<Host> copyList = new LinkedList<Host>();
//		for (Host h : getHostList())
//			copyList.add(h);
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
		
		Log.printLine("{{ ");
		for (Integer pe : freePesTmp)
			Log.print(pe + " ");
		Log.printLine("}}");
		
		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
//
//			// print list of PEs
//			System.out.println("{{ ");
//			for (Host h : getHostList())
//				System.out.print(h.getNumberOfFreePes() + " ");
//			System.out.println("}}");
//			
//			// sort in increasing amount of free PEs
//			Collections.sort(copyList, new Comparator<Host>() {
//
//				@Override
//				public int compare(Host h1, Host h2) {
//					return h1.getNumberOfFreePes() - h2.getNumberOfFreePes();
//				}
//				
//			});
//			
//			for (Host h: copyList) {
//				//if (h.getNumberOfFreePes() >= vm.getNumberOfPes())
//				if (allocateHostForVm(vm, h))
//					return true;
//			}
			do {// we still trying until we find a host or until we try all of them
				int lessFree = Integer.MAX_VALUE;
				int idx = -1;

				// we want the host with less pes in use
				for (int i = 0; i < freePesTmp.size(); i++) {
					if (freePesTmp.get(i) < lessFree) {
						lessFree = freePesTmp.get(i);
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
					freePesTmp.set(idx, Integer.MAX_VALUE);
				}
				tries++;
			} while (!result && tries < getFreePes().size());
		}
		
		// average of available MIPS
		double avgAvailableMIPS = stats.getMean();
		// median of available MIPS
		double medianAvailableMIPS = stats.getPercentile(50);
		System.out.println("{{ " + avgAvailableMIPS + " / " + medianAvailableMIPS + " }}");
		
		//PURunnerGeneric.allocationsDataAverage.add(avgAvailableMIPS);
		PURunnerGeneric.allocationsDataAverage.add((double) availableHosts);
		PURunnerGeneric.allocationsDataMedian.add(medianAvailableMIPS);
		
		return result;
	}
	
}
