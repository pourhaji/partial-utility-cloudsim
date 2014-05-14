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
public class VmAllocationPartialUtility_WorstFit_MIPS extends VmAllocationPolicySimple {

	public VmAllocationPartialUtility_WorstFit_MIPS(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		List<Host> copyList = new LinkedList<Host>();
		for (Host h : getHostList())
			copyList.add(h);
		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created

			Collections.sort(copyList, new Comparator<Host>() {
				@Override
				public int compare(Host h1, Host h2) {
					int cmp = (int)(h1.getAvailableMips() - h2.getAvailableMips());
					return cmp;
				}
			});
			System.out.println("After sorting hosts in VmAllocationPartialUtility_WorstFit_MIPS");
			System.out.print("« ");
			for (Host h : copyList) 
				System.out.print(h.getAvailableMips()+" ");
			System.out.println(" »");
			for (Host h: copyList) {
				//if (h.getNumberOfFreePes() >= vm.getNumberOfPes())
					if (allocateHostForVm(vm, h))
						return true;
			}
		}
		return false;
	}
	
}
