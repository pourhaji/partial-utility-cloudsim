package pt.inescid.gsd.cloudsim.partialutility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

public class VmAllocationPartialUtility_SI_BestFit_MIPS extends VmAllocationPolicySimple {

	public VmAllocationPartialUtility_SI_BestFit_MIPS(List<? extends Host> list) {
		super(list);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		List<Host> copyList = new ArrayList<Host>();
		for (Host h : getHostList())
			copyList.add(h);
		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created

			Collections.sort(copyList, new Comparator<Host>() {

				@Override
				public int compare(Host h1, Host h2) {
					return h1.getNumberOfFreePes() - h2.getNumberOfFreePes();
				}
				
			});
			
			for (Host h: copyList) {
				if (h.getNumberOfFreePes() >= vm.getNumberOfPes())
					if (allocateHostForVm(vm, h))
						return true;
			}
			
			// hack
			for (Host h : copyList)
				((VmSchedulerTimeSharedOverSubscription_SI_MinVirtPwr_MinClass)h.getVmScheduler()).phase1Ended();
			
			while (copyList.size()>0) {
				Host max = copyList.get(0);
				for (Host h : getHostList()) {
					PUHost puHost = (PUHost) h;
					if (puHost.getAvailableMips() > max.getAvailableMips() && 
						puHost.getNumberOfFreePes() >= max.getNumberOfFreePes())
						max = puHost;
				}
				if (allocateHostForVm(vm, max))
					return true;
				else
					copyList.remove(0);
			}
		}
		return false;
	}
	
}
