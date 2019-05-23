package org.cloudbus.cloudsim.mycloudsim;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import java.util.ArrayList;
import java.util.List;

public class t_demo {

    private static final int HOSTS = 5;
    private static final int VMS = 5;
    private static final int CLOUDLETS_PER_VM = 5;

    private List<Cloudlet> cloudletList_network;
    private List<Cloudlet> cloudletList_game;
    private List<Cloudlet> cloudletList_database;

    private List<Vm> vmList_network;
    private List<Vm> vmList_game;
    private List<Vm> vmList_database;

    private CloudSim simulation;


    public static void main(String[] args) {
        new t_demo();
    }

    public t_demo() {

        System.out.println("Starting: " + getClass().getSimpleName());


        simulation = new CloudSim();

        @SuppressWarnings("unused")
        Datacenter datacenter_network = createDatacenter();
        @SuppressWarnings("unused")
        Datacenter datacenter_game = createDatacenter();
        @SuppressWarnings("unused")
        Datacenter datacenter_database = createDatacenter();

        DatacenterBroker broker_network = new DatacenterBrokerSimple(simulation);
        DatacenterBroker broker_game = new DatacenterBrokerSimple(simulation);
        DatacenterBroker broker_database = new DatacenterBrokerSimple(simulation);

        vmList_network = new ArrayList<>(VMS);
        vmList_game = new ArrayList<>(VMS);
        vmList_database = new ArrayList<>(VMS);

        cloudletList_network = new ArrayList<>(CLOUDLETS_PER_VM);
        cloudletList_game = new ArrayList<>(CLOUDLETS_PER_VM);
        cloudletList_database =new ArrayList<>(CLOUDLETS_PER_VM);

        for (int i = 0; i < VMS; i++) {
            Vm vm = createVm_network(broker_network);
            vmList_network.add(vm);
            Cloudlet cloudlet1 = createCloudlet_network(broker_network, vm);
            cloudletList_network.add(cloudlet1);
        }

        for (int j = 0; j < VMS; j++) {
            Vm vm = createVm_game(broker_game);
            vmList_game.add(vm);
            Cloudlet cloudlet2 = createCloudlet_game(broker_game, vm, j);
            cloudletList_game.add(cloudlet2);
        }

        for (int i = 0; i < VMS; i++) {
            Vm vm = createVm_database(broker_database);
            vmList_database.add(vm);
            Cloudlet cloudlet3 = createCloudlet_database(broker_database, vm);
            cloudletList_database.add(cloudlet3);
        }

        broker_network.submitVmList(vmList_network);
        broker_game.submitVmList(vmList_game);
        broker_database.submitVmList(vmList_database);

        broker_network.submitCloudletList(cloudletList_network);
        broker_game.submitCloudletList(cloudletList_game);
        broker_database.submitCloudletList(cloudletList_database);

        simulation.start();

        List<Cloudlet> newcloudletList_network = broker_network.getCloudletFinishedList();
        List<Cloudlet> newcloudletList_game = broker_game.getCloudletFinishedList();
        List<Cloudlet> newcloudletList_database = broker_database.getCloudletFinishedList();

        new CloudletsTableBuilder(newcloudletList_network).setTitle(broker_network.getName()).build();
        new CloudletsTableBuilder(newcloudletList_game).setTitle(broker_game.getName()).build();
        new CloudletsTableBuilder(newcloudletList_database).setTitle(broker_database.getName()).build();


        System.out.println(getClass().getSimpleName() + " finished!");




    }

    private Datacenter createDatacenter() {
        List<Host> hostList = new ArrayList<>(HOSTS);
        for(int i = 0; i < HOSTS; i++) {
            Host host = createHost();
            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
    }

    private Host createHost() {
        final long mips = 100000; // capacity of each CPU core (in Million Instructions per Second)
        final long ram = 2048; // host memory (Megabyte)
        final long storage = 1000000; // host storage (Megabyte)
        final long bw = 10000; //in Megabits/s

        List<Pe> pesList = new ArrayList<>(); //List of CPU cores

        /*Creates the Host's CPU cores and defines the provisioner
        used to allocate each core for requesting VMs.*/
        for (int i = 0; i < 10; i++) {
            pesList.add(new PeSimple(mips, new PeProvisionerSimple()));
        }

        return new HostSimple(ram, bw, storage, pesList)
            .setRamProvisioner(new ResourceProvisionerSimple())
            .setBwProvisioner(new ResourceProvisionerSimple())
            .setVmScheduler(new VmSchedulerTimeShared());
    }

    private Vm createVm_network(DatacenterBroker broker) {
        final long   mips = 1000;
        final long   storage = 10000; // vm image size (Megabyte)
        final int    ram = 512; // vm memory (Megabyte)
        final long   bw = 1000; // vm bandwidth (Megabits/s)
        final long   pesNumber = 1; // number of CPU cores

        return new VmSimple(vmList_network.size(), mips, pesNumber)
            .setRam(ram)
            .setBw(bw)
            .setSize(storage)
            .setCloudletScheduler(new CloudletSchedulerTimeShared());
    }


    private Vm createVm_game(DatacenterBroker broker) {
        final long   mips = 1000;
        final long   storage = 10000; // vm image size (Megabyte)
        final int    ram = 512; // vm memory (Megabyte)
        final long   bw = 1000; // vm bandwidth (Megabits/s)
        final long   pesNumber = 1; // number of CPU cores

        return new VmSimple(vmList_game.size(), mips, pesNumber)
            .setRam(ram)
            .setBw(bw)
            .setSize(storage)
            .setCloudletScheduler(new CloudletSchedulerTimeShared());
    }

    private Vm createVm_database(DatacenterBroker broker) {
        final long   mips = 1000;
        final long   storage = 10000; // vm image size (Megabyte)
        final int    ram = 512; // vm memory (Megabyte)
        final long   bw = 1000; // vm bandwidth (Megabits/s)
        final long   pesNumber = 1; // number of CPU cores

        return new VmSimple(vmList_database.size(), mips, pesNumber)
            .setRam(ram)
            .setBw(bw)
            .setSize(storage)
            .setCloudletScheduler(new CloudletSchedulerTimeShared());
    }

    private Cloudlet createCloudlet_network(DatacenterBroker broker, Vm vm) {
        final long length = 10000; //in Million Instruction (MI)
        final long fileSize = 300; //Size (in bytes) before execution
        final long outputSize = 300; //Size (in bytes) after execution
        final int  numberOfCpuCores = 1; //cloudlet will use all the VM's CPU cores

        //Defines how CPU, RAM and Bandwidth resources are used
        //Sets the same utilization model for all these resources.
        UtilizationModel utilization = new UtilizationModelFull();

        Cloudlet cloudlet
            = new CloudletSimple(
            cloudletList_network.size(), length, numberOfCpuCores)
            .setFileSize(fileSize)
            .setOutputSize(outputSize)
            .setUtilizationModel(utilization)
            .setVm(vm);

        return cloudlet;
    }


    private Cloudlet createCloudlet_game(DatacenterBroker broker, Vm vm, int i) {
        long[] length = new long[5]; //in Million Instruction (MI)
        length[0]=10000;length[1]=10100;
        length[2]=20000;length[3]=30000;
        length[4]=50000;
        final long fileSize = 300; //Size (in bytes) before execution
        final long outputSize = 300; //Size (in bytes) after execution
        final int  numberOfCpuCores = 1; //cloudlet will use all the VM's CPU cores

        //Defines how CPU, RAM and Bandwidth resources are used
        //Sets the same utilization model for all these resources.
        UtilizationModel utilization = new UtilizationModelFull();
        Cloudlet cloudlet
            = new CloudletSimple(
            cloudletList_game.size(), length[i++], numberOfCpuCores)
            .setFileSize(fileSize)
            .setOutputSize(outputSize)
            .setUtilizationModel(utilization)
            .setVm(vm);

        return cloudlet;
    }

    private Cloudlet createCloudlet_database(DatacenterBroker broker, Vm vm) {
        final long length = 10000; //in Million Instruction (MI)
        final long fileSize = 300; //Size (in bytes) before execution
        final long outputSize = 300; //Size (in bytes) after execution
        final int  numberOfCpuCores = 1; //cloudlet will use all the VM's CPU cores

        //Defines how CPU, RAM and Bandwidth resources are used
        //Sets the same utilization model for all these resources.
        UtilizationModel utilization = new UtilizationModelFull();

        Cloudlet cloudlet
            = new CloudletSimple(
            cloudletList_database.size(), length, numberOfCpuCores)
            .setFileSize(fileSize)
            .setOutputSize(outputSize)
            .setUtilizationModel(utilization)
            .setVm(vm);

        return cloudlet;
    }


}
