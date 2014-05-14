package pt.inescid.gsd.cloudsim.examples.partialutility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelPlanetLabInMemory;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;

import pt.inescid.gsd.cloudsim.partialutility.PUClass;
import pt.inescid.gsd.cloudsim.partialutility.PUDatacenterBroker;
import pt.inescid.gsd.cloudsim.partialutility.PUHost;
import pt.inescid.gsd.cloudsim.partialutility.PUVm;
import pt.inescid.gsd.cloudsim.partialutility.UnknownPUClass;
import pt.inescid.gsd.cloudsim.partialutility.UnknownVMType;
import pt.inescid.gsd.cloudsim.partialutility.VMType;

public class PlanetLabWorkload {

	/**
	 * 0. creates VM list
	 * 1. records VM allocation
	 * 2. runs planet lab cloudlets
	 * @param datacenter 
	 * @param nVMs
	 * @param nVMs 
	 * @param experience
	 * @param dcConfig
	 * @param cloudletPlanetLabData 
	 * @param vmData 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 * @throws UnknownPUClass 
	 * @throws UnknownVMType 
	 */
	static void runPlanetLab(
			Datacenter datacenter, 
			PUDatacenterBroker broker,
			int nVMs, 
			PU_Experience_Configuation experience, 
			PU_DC_Configuration dcConfig, 
			BufferedWriter vmData, 
			BufferedWriter cloudletPlanetLabData) 
					throws NumberFormatException, IOException, UnknownVMType, UnknownPUClass 
	{
		List<Vm> vmList = PlanetLabWorkload.createVmListForPlanetLab(
				broker, 
				dcConfig, 
				nVMs, 
				experience.vmClassPercentage
		);
		
		// Create Cloudlets for PlanetLab
		List<Cloudlet> cloudletList = PlanetLabWorkload.createCloudlets_PlanetLab(
				broker.getId(), 
				nVMs
		);
		
		broker.submitVmList(vmList);
		broker.submitCloudletList(cloudletList);
	
		CloudSim.startSimulation();
		CloudSim.stopSimulation();
		
		// 3. Print VM results
		PUGenericRunnerStatisticsHelper.printVMInfo(datacenter, broker, vmList, nVMs, experience, dcConfig, vmData);	
		
		// 4. Print cloudlet results
		PUGenericRunnerStatisticsHelper.printCloudletInfo(cloudletList, nVMs, cloudletPlanetLabData);
	
	}

	public static List<Cloudlet> createCloudlets_PlanetLab(
			int brokerId, 
			int nVMs) throws NumberFormatException, IOException 
	{
		long fileSize = 300;
		long outputSize = 300;
		UtilizationModel otherUtilizationModel = new UtilizationModelFull();
	
		File inputFolder = new File(PURunnerGeneric.planetLabDir1);
		File[] files = inputFolder.listFiles();
	
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>(); 
		for (int i = 0; i < files.length && i < nVMs; i++) {
			Cloudlet cloudlet =
				new Cloudlet(
					i,
					Constants.CLOUDLET_LENGTH,
					Constants.CLOUDLET_PES,
					fileSize,
					outputSize,
					new UtilizationModelPlanetLabInMemory(
							files[i].getAbsolutePath(),
							Constants.SCHEDULING_INTERVAL), 
					otherUtilizationModel, 
					otherUtilizationModel);
			cloudlet.setUserId(brokerId);
			//cloudlet.setVmId(i);
			cloudletList.add(cloudlet);
		}
		//	id += nCloudlets;
		//}
		return cloudletList;
	}

	static List<Vm> createVmListForPlanetLab(
			PUDatacenterBroker broker, 
			PU_DC_Configuration dcConfig, 
			int nVMs,
			HashMap<PUClass, Double> vmClassPercentage) 
	{
		double highVms = nVMs*vmClassPercentage.get(PUClass.High);
		double medVms = nVMs*vmClassPercentage.get(PUClass.Medium);
		double lowVms = nVMs*vmClassPercentage.get(PUClass.Low);
		List<PUClass> vmsToCreate = new ArrayList<PUClass>();
		for (int i=0; i<highVms; ++i) {
			vmsToCreate.add(PUClass.High);
		}
		for (int i=0; i<medVms; ++i) {
			vmsToCreate.add(PUClass.Medium);
		}
		for (int i=0; i<lowVms; ++i) {
			vmsToCreate.add(PUClass.Low);
		}
		Random r = new Random(1);
		List<Vm> vms = new ArrayList<Vm>();
		for (int i = 0; i < nVMs; i++) {
			int vmType = i % PURunnerGeneric.VM_TYPES;
			PUVm vm = new PUVm(
						i,
						broker.getId(),
						PURunnerGeneric.VM_MIPS[vmType],
						PURunnerGeneric.VM_PES[vmType],
						PURunnerGeneric.VM_RAM[vmType],
						PURunnerGeneric.VM_BW,
						PURunnerGeneric.VM_SIZE,
						"Xen",
						new CloudletSchedulerDynamicWorkload(PURunnerGeneric.VM_MIPS[vmType], PURunnerGeneric.VM_PES[vmType]),
						//new CloudletSchedulerSpaceShared(),
						PlanetLabWorkload.types[vmType]
					);
			int idx = r.nextInt(vmsToCreate.size());
			PUClass cls = vmsToCreate.remove(idx);
			vm.setVmClass(cls);
			vms.add(vm);
		}
		return vms;
	}

	// Compromised with Constants array order
	static VMType[] types = { VMType.extra, VMType.regular, VMType.small, VMType.micro };
	
}
