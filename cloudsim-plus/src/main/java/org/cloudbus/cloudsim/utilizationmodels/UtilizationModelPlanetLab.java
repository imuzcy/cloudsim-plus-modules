package org.cloudbus.cloudsim.utilizationmodels;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Defines the resource utilization model based on a
 * <a href="https://www.planet-lab.org">PlanetLab</a>
 * Datacenter trace file.
 */
public class UtilizationModelPlanetLab extends UtilizationModelAbstract {

    /**
     * The scheduling interval.
     */
    private double schedulingInterval;

    /**
     * The resource utilization data for an entire day, in intervals of 5
     * minutes (5 min * 288 = 24 hours).
     */
    private final double[] data;

    /**
     * Instantiates a new PlanetLab resource utilization model from a trace
     * file.
     *
     * @param inputPath The path of a PlanetLab Datacenter trace file.
     * @param schedulingInterval the scheduling interval that defines the time interval in which precise utilization is be got
     * @throws NumberFormatException the number format exception
     * @throws IOException Signals that an I/O exception has occurred
     * @see #getSchedulingInterval()
     */
    public UtilizationModelPlanetLab(String inputPath, double schedulingInterval)
            throws NumberFormatException, IOException
    {
        super();
        data = new double[289];
        setSchedulingInterval(schedulingInterval);
        try (BufferedReader input = new BufferedReader(new FileReader(inputPath))) {
            final int n = data.length;
            for (int i = 0; i < n - 1; i++) {
                data[i] = Integer.valueOf(input.readLine()) / 100.0;
            }
            data[n - 1] = data[n - 2];
        }
    }

    /**
     * Instantiates a new PlanetLab resource utilization model with variable
     * data samples from a trace file.
     *
     * @param inputPath The path of a PlanetLab Datacenter trace file.
     * @param schedulingInterval the scheduling interval that defines the time interval in which precise utilization is be got
     * @param dataSamples number of samples in the file
     * @throws NumberFormatException the number format exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @see #setSchedulingInterval(double)
     */
    public UtilizationModelPlanetLab(String inputPath, double schedulingInterval, int dataSamples)
            throws NumberFormatException, IOException
    {
        super();
        setSchedulingInterval(schedulingInterval);
        data = new double[dataSamples];
        try (BufferedReader input = new BufferedReader(new FileReader(inputPath))) {
            final int n = data.length;
            for (int i = 0; i < n - 1; i++) {
                data[i] = Integer.valueOf(input.readLine()) / 100.0;
            }
            data[n - 1] = data[n - 2];
        }
    }

    @Override
    public double getUtilization(double time) {
        if (time % getSchedulingInterval() == 0) {
            return data[(int) time / (int) getSchedulingInterval()];
        }
        final int time1 = (int) Math.floor(time / getSchedulingInterval());
        final int time2 = (int) Math.ceil(time / getSchedulingInterval());
        final double utilization1 = data[time1];
        final double utilization2 = data[time2];
        final double delta = (utilization2 - utilization1) / ((time2 - time1) * getSchedulingInterval());
        return utilization1 + delta * (time - time1 * getSchedulingInterval());

    }

    /**
     * Gets the scheduling interval that defines the time interval in which precise utilization is be got.
     * <p>That means if the {@link #getUtilization(double)} is called
     * passing any time that is multiple of this scheduling interval,
     * the utilization returned will be the value stored for that
     * specific time. Otherwise, the value will be a mean
     * of the beginning and the ending of the interval in which
     * the given time is.</p>
     *
     *
     * @return the scheduling interval
     */
    public double getSchedulingInterval() {
        return schedulingInterval;
    }

    /**
     * Sets the scheduling interval.
     *
     * @param schedulingInterval the scheduling interval to set
     * @see #getSchedulingInterval()
     */
    public final void setSchedulingInterval(double schedulingInterval) {
        this.schedulingInterval = schedulingInterval;
    }
}
