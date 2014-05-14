package pt.inescid.gsd.cloudsim.partialutility;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;

/*
 * @author Jose M. Simao
 */
public class VmAllocationPartialUtility_SI_MAX_VIRT_POWER extends VmAllocationPolicySimple {

	public VmAllocationPartialUtility_SI_MAX_VIRT_POWER(List<? extends Host> list) {
		super(list);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		List<Host> copyList = new LinkedList<Host>();
		for (Host h : getHostList())
			copyList.add(h);
		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			while (copyList.size()>0) {
				Host max = copyList.get(0);
				for (Host h : copyList) {
					PUHost puHost = (PUHost) h;
					if (puHost.getAvailableMips() > max.getAvailableMips() && 
						puHost.getNumberOfFreePes() >= max.getNumberOfFreePes())
						max = puHost;
				}
				if (allocateHostForVm(vm, max))
					return true;
				else
					copyList.remove(max);
			}
		}
		return false;
	}
	
}
