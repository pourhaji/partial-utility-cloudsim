package pt.inescid.gsd.cloudsim.partialutility;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class RamProvisioner_PU_Overcommit extends RamProvisionerSimple {

	public RamProvisioner_PU_Overcommit(int availableRam) {
		super(availableRam);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean allocateRamForVm(Vm vm, int ram) {
		// TODO Auto-generated method stub
		return super.allocateRamForVm(vm, ram);
	}
	
	
}
