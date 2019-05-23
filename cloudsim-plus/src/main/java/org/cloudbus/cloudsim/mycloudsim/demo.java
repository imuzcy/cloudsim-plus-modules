package org.cloudbus.cloudsim.mycloudsim;

import org.apache.commons.lang3.ObjectUtils;
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
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class demo {
    /** The cloudlet list. 云服务器列表 */
    private List<Cloudlet> cloudletList;

    /** The vm_network_list. 网关服务器虚拟机列表 */
    private static List<Vm> vm_network_list;

    /** The vm_game_list. 游戏服务器虚拟机列表 */
    private static List<Vm> vm_game_list;

    /** The vm_database_list. 数据库服务器虚拟机列表 */
    private static List<Vm> vm_database_list;


    /** 结束条件*/
    private static boolean flag = false;

    /** 系统平均的响应时间 */
    private static double R_e = 1.0;

    /** 系统平均的吞吐量 */
    private static double Lambada_e = 1.0;

    /** 每层中虚拟服务器的平均利用率U=(U0,U1,U2) */
    private static double[] U = new double[4];

    /** 虚拟机vm_network_list的运行状态 */
    private static List<Boolean> vm_network;

    /** 虚拟机vm_game_list的运行状态 */
    private static List<Boolean> vm_game;

    /** 虚拟机vm_database_list的运行状态 */
    private static List<Boolean> vm_database;

    /** L[i]是第i层中每个虚拟服务器的平均队列长度 */
    private static double[] L = new double[3];

    /** D[i]是第i层中的总服务需求 */
    private static double[] D= new double[3];

    /** R[i]是第i层的平均的响应时间 */
    private static double[] R = new double[3];

    /**
     * T代表系统配置，T[i]代表第i层中活动的虚拟机的数量。
     */
    private static int[] T= new int[3];

    /** T_star代表新的系统配置 */
    private static int[] T_star = new int[3];

    /** Lambada[i]代表第i层中吞吐量 */
    private static double[] Lambada = new double[3];

    /** 玩家r游戏会话的结束时间 */
    private static int[] E = new int[3];

    /** NV[i]表示第i层中由再配置器模块新添加的虚拟服务器的集合 */
    private static int[] NV;

    /** AV[i]表示第i层中活动虚拟机的集合 */
    private static int[] AV;

    /** DA[i]表示查找AV[i]中关闭时间大于玩家r会话结束时间E(r)的虚拟机的集合 */
    private static int[] DA;

    /** N表示为系统中玩家数量 */
    private static int N;

    /** FinishTime */
    private static double finishTime;

    private CloudSim simulation;

    public static void main(String[] args) {
       new demo();
    }

    /** 运行仿真 */
    public demo() {
        System.out.println("Starting " + getClass().getSimpleName());


        /** 第一步: Initialize the CloudSim package. */
        simulation = new CloudSim();

        /** 第二步:  创建数据中心*/
        @SuppressWarnings("unused")
        Datacenter datacenter0 = createDatacenter();

        /** 第三步: 创建Broker */
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);

        /** 第四步: 创建VMs和Cloudlet,将它们发送到broker */

        /** 创建Network_VMs */
        vm_network_list = create_network_Vms(broker, 5);
        /** 创建Game_VMs */
        vm_game_list = create_game_Vms(broker, 5);
        /** 创建Database_VMs */
        vm_database_list = create_database_Vms(broker, 5);

        /** 创建cloudletList */
        cloudletList = createCloudlets(broker, 20);




        /** 提交给broker */
        broker.submitVmList(vm_network_list);
        broker.submitVmList(vm_game_list);
        broker.submitVmList(vm_database_list);

        broker.submitCloudletList(cloudletList);

        U[0] = 1.0;
        U[1] = 1.0;
        U[2] = 1.0;

        E[0] = 17;
        E[1] = 22;
        E[2] = 55;

        /** 第五步: 开始仿真 */
        finishTime = simulation.start();
        Heuristic_Algorithm(cloudletList);

        /** 最后一步: 打印结束并输出结果*/
        List<Cloudlet> newList = broker.getCloudletFinishedList();

        new CloudletsTableBuilder(newList).build();
        System.out.println(getClass().getSimpleName() + " finished!");


    }

    /** 创建Network_VMs */
    private List<Vm> create_network_Vms(DatacenterBroker broker, int vms) {
        //Creates a container to store VMs. This list is passed to the broker later
        List<Vm> list = new ArrayList<>(vms);

        /** VM参数 */
        long size = 1000; //image size (Megabyte)
        int ram = 512; //vm memory (Megabyte)
        int mips = 1000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus

        /** 创建VMs */
        for (int i = 0; i < vms; i++) {
            Vm vm = new VmSimple(i, mips, pesNumber)
                .setRam(ram).setBw(bw).setSize(size)
                .setCloudletScheduler(new CloudletSchedulerTimeShared());

            //for creating a VM with a space shared scheduling policy for cloudlets:
            //vm[i] = VmSimple(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());

            list.add(vm);
        }

        return list;
    }


    /** 创建Game_VMs */
    private List<Vm> create_game_Vms(DatacenterBroker broker, int vms) {
        //Creates a container to store VMs. This list is passed to the broker later
        List<Vm> list = new ArrayList<>(vms);

        /** VM参数 */
        long[] size = new long[5]; //image size (Megabyte)
        size[0]=1000;
        size[1]=900;
        size[2]=800;
        size[3]=1400;
        size[4]=2000;
        int ram = 512; //vm memory (Megabyte)
        int mips = 1000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus

        /** 创建VMs */
        for (int i = 0; i < vms; i++) {
            Vm vm = new VmSimple(i, mips, pesNumber)
                .setRam(ram).setBw(bw).setSize(size[i])
                .setCloudletScheduler(new CloudletSchedulerTimeShared());

            //for creating a VM with a space shared scheduling policy for cloudlets:
            //vm[i] = VmSimple(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());

            list.add(vm);
        }

        return list;
    }

    /** 创建Databese_VMs */
    private List<Vm> create_database_Vms(DatacenterBroker broker, int vms) {
        //Creates a container to store VMs. This list is passed to the broker later
        List<Vm> list = new ArrayList<>(vms);

        /** VM参数 */
        long size = 1000; //image size (Megabyte)
        int ram = 512; //vm memory (Megabyte)
        int mips = 1000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus

        /** 创建VMs */
        for (int i = 0; i < vms; i++) {
            Vm vm = new VmSimple(i, mips, pesNumber)
                .setRam(ram).setBw(bw).setSize(size)
                .setCloudletScheduler(new CloudletSchedulerTimeShared());

            //for creating a VM with a space shared scheduling policy for cloudlets:
            //vm[i] = VmSimple(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());

            list.add(vm);
        }

        return list;
    }



    /** 创建Cloudlet*/
    private List<Cloudlet> createCloudlets(DatacenterBroker broker, int cloudlets) {
        List<Cloudlet> list = new ArrayList<>(cloudlets);

        //cloudlet parameters
        long length = 10000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < cloudlets; i++) {
            Cloudlet cloudlet = new CloudletSimple(i, length, pesNumber)
                .setFileSize(fileSize)
                .setOutputSize(outputSize)
                .setUtilizationModel(utilizationModel);
            list.add(cloudlet);
        }

        return list;
    }

    /** 创建数据中心 */
    private Datacenter createDatacenter() {
        // Here are the steps needed to create a DatacenterSimple:
        // 1. We need to create a list to store one or more
        //    Machines
        List<Host> hostList = new ArrayList<>(10);

        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.
        List<Pe> peList1 = new ArrayList<>(10);

        long mips = 1000;

        // 3. Create PEs and add these into the list.
        //for a quad-core machine, a list of 4 PEs is required:
        for(int i = 0; i < 4; i++)
            peList1.add(new PeSimple(mips, new PeProvisionerSimple()));

        //Another list, for a dual-core machine
        List<Pe> peList2 = new ArrayList<>();
        for(int i = 0; i < 2; i++)
            peList2.add(new PeSimple(mips, new PeProvisionerSimple()));

        //4. Create Hosts with its id and list of PEs and add them to the list of machines
        int hostId = -1;
        long ram = 2048; //host memory (Megabyte)
        long storage = 1000000; //host storage (Megabyte)
        long bw = 10000; //Megabits/s

        Host host1 = new HostSimple(ram, bw, storage, peList1)
            .setRamProvisioner(new ResourceProvisionerSimple())
            .setBwProvisioner(new ResourceProvisionerSimple())
            .setVmScheduler(new VmSchedulerTimeShared());
        hostList.add(host1);

        Host host2 = new HostSimple(ram, bw, storage, peList2)
            .setRamProvisioner(new ResourceProvisionerSimple())
            .setBwProvisioner(new ResourceProvisionerSimple())
            .setVmScheduler(new VmSchedulerTimeShared());
        hostList.add(host2);



        // 6. Finally, we need to create a DatacenterSimple object.
        DatacenterSimple dc = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
        dc.getCharacteristics()
            .setCostPerSecond(3.0)
            .setCostPerMem(0.05)
            .setCostPerStorage(0.1)
            .setCostPerBw(0.1);
        return dc;
    }

    private static double show_network_CpuUtilizationForAllVms(final double simulationFinishTime) {
        //System.out.println("\nHosts CPU utilization history for the entire simulation period\n");
        double vm_average_CpuUsage = 0.0;
        double vm_all_CpuUsage = 0.0;
        int numberOfUsageHistoryEntries = 0;
        for (Vm vm : vm_network_list) {
            //System.out.printf("VM %d\n", vm.getId());
            if (vm.getUtilizationHistory().getHistory().isEmpty()) {
                //System.out.println("\tThere isn't any usage history");
                continue;
            }

            for (Map.Entry<Double, Double> entry : vm.getUtilizationHistory().getHistory().entrySet()) {
                final double time = entry.getKey();
                final double vmCpuUsage = entry.getValue() * 100;
                if (vmCpuUsage > 0) {
                    numberOfUsageHistoryEntries++;
                    //System.out.printf("\tTime: %2.0f CPU Utilization: %6.2f%%\n", time, vmCpuUsage);
                }
                vm_all_CpuUsage += vmCpuUsage;
            }
        }
        if (vm_network_list.size() == 0) {
            return 0.0;
        }
        else {
            vm_average_CpuUsage = vm_all_CpuUsage / vm_network_list.size();
            System.out.printf("vm_average_CpuUsage %f\n", vm_average_CpuUsage);
            return vm_average_CpuUsage;
        }
    }

    /** game CPU Usage   */
    private static double show_game_CpuUtilizationForAllVms(final double simulationFinishTime) {
        //System.out.println("\nHosts CPU utilization history for the entire simulation period\n");
        double vm_average_CpuUsage = 0.0;
        double vm_all_CpuUsage = 0.0;
        int numberOfUsageHistoryEntries = 0;
        for (Vm vm : vm_game_list) {
            //System.out.printf("VM %d\n", vm.getId());
            if (vm.getUtilizationHistory().getHistory().isEmpty()) {
                //System.out.println("\tThere isn't any usage history");
                continue;
            }

            for (Map.Entry<Double, Double> entry : vm.getUtilizationHistory().getHistory().entrySet()) {
                final double time = entry.getKey();
                final double vmCpuUsage = entry.getValue() * 100;
                if (vmCpuUsage > 0) {
                    numberOfUsageHistoryEntries++;
                   // System.out.printf("\tTime: %2.0f CPU Utilization: %6.2f%%\n", time, vmCpuUsage);
                }
                vm_all_CpuUsage += vmCpuUsage;
            }
        }
        if (vm_game_list.size() == 0) {
            return 0.0;
        }
        else {
            vm_average_CpuUsage = vm_all_CpuUsage / vm_game_list.size();
            System.out.printf("vm_average_CpuUsage %f\n", vm_average_CpuUsage);
            return vm_average_CpuUsage;
        }
    }

    /** database CPU Usage   */
    private static double show_database_CpuUtilizationForAllVms(final double simulationFinishTime) {
        //System.out.println("\nHosts CPU utilization history for the entire simulation period\n");
        double vm_average_CpuUsage = 0.0;
        double vm_all_CpuUsage = 0.0;
        int numberOfUsageHistoryEntries = 0;
        for (Vm vm : vm_database_list) {
           // System.out.printf("VM %d\n", vm.getId());
            if (vm.getUtilizationHistory().getHistory().isEmpty()) {
               // System.out.println("\tThere isn't any usage history");
                continue;
            }

            for (Map.Entry<Double, Double> entry : vm.getUtilizationHistory().getHistory().entrySet()) {
                final double time = entry.getKey();
                final double vmCpuUsage = entry.getValue() * 100;
                if (vmCpuUsage > 0) {
                    numberOfUsageHistoryEntries++;
                  //  System.out.printf("\tTime: %2.0f CPU Utilization: %6.2f%%\n", time, vmCpuUsage);
                }
                vm_all_CpuUsage += vmCpuUsage;
            }
        }
        if (vm_database_list.size() == 0) {
            return 0.0;
        }
        else {
            vm_average_CpuUsage = vm_all_CpuUsage / vm_database_list.size();
            System.out.printf("vm_average_CpuUsage %f\n", vm_average_CpuUsage);
            return vm_average_CpuUsage;
        }
    }


    private static void work(List<Cloudlet> cloudlet_list) {

        for (int i = 0; i < 3; i++) {
            if (NV[i] != 0) {

            }
            else {

            }
        }

    }



    /** Monitor监控器 */
    private static void Monitor() {

//        /** Repose Time 响应时间*/
//        R_e =
//
//        /** Throughout 吞吐量 */
//        Lambada_e =

        /** VMs CPU 实时使用率*/
        show_network_CpuUtilizationForAllVms(finishTime);
        show_game_CpuUtilizationForAllVms(finishTime);
        show_database_CpuUtilizationForAllVms(finishTime);

        /** VMs运行状态 */
        int[] num = new int[3];

//        for (Vm vm : vm_network_list) {
//            vm_network.add(vm.isWorking());
//        }
//
//        for (Vm vm : vm_game_list) {
//            vm_game.add(vm.isWorking());
//        }
//
//        for (Vm vm : vm_database_list) {
//            vm_database.add(vm.isWorking());
//        }

        for (Vm vm : vm_network_list) {
            if (vm.getCpuPercentUsage()==0) {
                num[0]++;
            }
        }


        for (Vm vm : vm_game_list) {
            if (vm.getCpuPercentUsage()==0) {
                num[1]++;
            }
        }

        for (Vm vm : vm_database_list) {
            if (vm.getCpuPercentUsage()==0) {
                num[2]++;
            }
        }

        if (0 == vm_network_list.size() &&
            0 == vm_game_list.size() &&
            0 == vm_database_list.size()
           ) {
            flag = true;
        }

    }


    /** 再配置器模块: Heuristic_Algorithm 启发式算法 */
    private static void Heuristic_Algorithm(List<Cloudlet> cloudletlist) {
        do {
            /** 从监控器获取虚拟机的运行状态以及R,Lambada,U的值 */
            Monitor();
            for (int i = 0; i < 3; i++) {
                T_star[ i ] = T[i];
            }

            if (R_e > 0.9 * Arrays.stream(R).max().getAsDouble()) {
                T_star = AddToResource(T, U, Lambada_e, R_e);
            }

            for (int i = 0; i < 3; i++) {
                if (T[i] != T_star[i]) {
                    T[i] = T_star[i];
                }
            }
            if (flag) {
                break;
            }
        }while(true);
    }


    /** MVA_Algorithm MVA算法 */
    private static double[] MVA_Algorithm(int[] T, int N, double[] D) {

        for (int i = 0; i < 3; i++) {
            L[i] = 0;
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < 3; j++) {
                R[j] = D[j] * (1 + L[j]);
            }
            for (int k = 0; k < 3; k++) {
                R_e = R[k] * T[k];
            }
            Lambada_e = N * 1.0 / R_e;
            for (int j = 0; j < 3; j++) {
                L[j] = Lambada_e * R[j];
            }
        }
        for (int i = 0; i < 3; i++) {
            U[i] = Lambada_e * D[i];
        }

        U[3] = R_e;

        return U;
    }


    /** AddToResource Algorithm */
    private static int[] AddToResource(int[] T, double[] U, double Lambada_e, double R_e) {

        /** 计算并发的玩家数量N */
        N = (int)Math.ceil(Lambada_e * R_e);

        /** 计算每层的服务需求D=(D1, D2, D3) */
        for(int i = 0; i < 3; i++) {
            D[ i ] = T[ i ] * U[ i ] / Lambada_e;
        }

        long size = 10000;
        /** vm内存(MB) */
        int ram = 512;
        int mips = 250;
        long bw = 1000;
        /** cpu内核数 */
        int pesNumber = 1;

        do {

            int h = 0;
            double U_max = -9999.0;
            for (int i = 0; i < 3; i++) {
                if (U_max < U[ i ]) {
                    U_max = U[ i ];
                    h = i;
                }
            }

            switch (h) {
                case 0:
                    Vm vm0 = new VmSimple(vm_network_list.size()+1, mips, pesNumber)
                        .setRam(ram).setBw(bw).setSize(size)
                        .setCloudletScheduler(new CloudletSchedulerTimeShared());
                    vm_network_list.add(vm0);
                    break;
                case 1:
                    Vm vm1 = new VmSimple(vm_game_list.size()+1, mips, pesNumber)
                        .setRam(ram).setBw(bw).setSize(size)
                        .setCloudletScheduler(new CloudletSchedulerTimeShared());
                    vm_game_list.add(vm1);
                    break;
                case 2:
                    Vm vm2= new VmSimple(vm_database_list.size()+1, mips, pesNumber)
                        .setRam(ram).setBw(bw).setSize(size)
                        .setCloudletScheduler(new CloudletSchedulerTimeShared());
                    vm_database_list.add(vm2);
                    break;
            }

            T[h] = T[h] + 1;

            U = MVA_Algorithm(T, N, D);
            R_e = U[3];

        }while(R_e > 0.9 * Arrays.stream(R).max().getAsDouble());
        return T;
    }

}
