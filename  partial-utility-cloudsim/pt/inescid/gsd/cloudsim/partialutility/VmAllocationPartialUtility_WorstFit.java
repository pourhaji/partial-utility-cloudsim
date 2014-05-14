package pt.inescid.gsd.cloudsim.partialutility;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

/*
 * @author Jose M. Simao
 */
public class VmAllocationPartialUtility_WorstFit extends VmAllocationPolicySimple {

	public VmAllocationPartialUtility_WorstFit(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		List<Host> copyList = new LinkedList<Host>();
		for (Host h : getHostList())
			copyList.add(h);
	
		
		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			
			// sort in increasing order of used PEs
			Collections.sort(copyList, new Comparator<Host>() {

				@Override
				public int compare(Host h1, Host h2) {
					return -(h1.getNumberOfFreePes() - h2.getNumberOfFreePes());
				}
				
			});
			
			for (Host h: copyList) {
				//if (h.getNumberOfFreePes() >= vm.getNumberOfPes())
				if (allocateHostForVm(vm, h))
					return true;
			}
		}
		return false;
	}
	
}
