package com.example.ml_pdr;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
// This class define the particle filtering algorithm using a systematic resampling.
class ParticleFiltering {
    static double[][] generateNewParticles(double[][] particles, float[] acc, double[] gyr, double[] observation, double[] qObservation, float t, BuildingMap map, ExecutorService executorService,boolean recording,String path) {
        double acc_sigma = 0.2;
        double gyr_sigma = 0.1;
        NormalDistribution gyr_norm = new NormalDistribution(0,gyr_sigma);
        NormalDistribution acc_norm = new NormalDistribution(0,acc_sigma);
        Vector3D obs = new Vector3D(observation[0], observation[1], observation[2]);
        path = path + File.separator + "particles.txt";

        double sigma = 2;
        int n = particles.length;
        double[] weights = new double[n];

        for (int i = 0; i < n; i++) {

            Vector3D acceleration = new Vector3D(acc[0] + acc_norm.sample(), acc[1]+ acc_norm.sample(), acc[2]+ acc_norm.sample());
            Quaternion s = new Quaternion(0, gyr[0]+ gyr_norm.sample(), gyr[1]+ gyr_norm.sample(), gyr[2]+ gyr_norm.sample());

            Vector3D velocity = new Vector3D(particles[i][3], particles[i][4], particles[i][5]);
            Quaternion q = new Quaternion(particles[i][6], particles[i][7], particles[i][8], particles[i][9]);

            // Compute new position
            Vector3D delta_p = velocity.scalarMultiply(t).add(acceleration.scalarMultiply(t * t / 2));
            Vector3D position = new Vector3D(particles[i][0], particles[i][1], particles[i][2]);
            Rotation rot = new Rotation(particles[i][6], particles[i][7], particles[i][8], particles[i][9], false);
            Vector3D newPosition = position.add(rot.applyTo(delta_p));
            particles[i][0] = newPosition.getX();
            particles[i][1] = newPosition.getY();
            particles[i][2] = newPosition.getZ();
            if (recording) {
                String data = String.valueOf(position.getX()) + ',' + position.getY() + ',' + position.getZ() + ',' + particles[i][0] + ',' + particles[i][1] + ',' + particles[i][2];
                CsvWriter writer = new CsvWriter(path, data);
                executorService.execute(writer);
            }
            // Compute new quaternion rotation
            Quaternion dq = q.multiply(s).multiply(0.5);
            Quaternion newQ = q.add(dq.multiply(t)).normalize();
            particles[i][6] = newQ.getQ0();
            particles[i][7] = newQ.getQ1();
            particles[i][8] = newQ.getQ2();
            particles[i][9] = newQ.getQ3();

            // Compute new velocity
            Vector3D newVelocity = velocity.add(acceleration.scalarMultiply(t));
            particles[i][3] = newVelocity.getX();
            particles[i][4] = newVelocity.getY();
            particles[i][5] = newVelocity.getZ();

            // Re-weighting according to the observation
            // If the particle crossed a wall during its evolution, we set the weight at 0
            if (MapMatching.check(position.toArray(), Arrays.copyOfRange(particles[i],0,3),map)){
                weights[i] = 0;
            }
            // Else we compute the particle likelihood according the distance and the rotation difference with the observation
            else {
                Vector3D diff = obs.subtract(new Vector3D(particles[i][0],particles[i][1],particles[i][2]));
                Quaternion qObs = new Quaternion(qObservation[0],qObservation[1],qObservation[2],qObservation[3]);
                double p = Math.exp(-Math.pow(diff.getNorm()+quaternionNorm(qObs,newQ),2) / (2 * sigma * sigma)) / (Math.sqrt(2 * Math.PI) * sigma);
                weights[i] = p;
            }
        }

        // Normalize the weights
        double sum = Utils.sum(weights);
        for (int l = 0; l < n; l++) {
            weights[l] = weights[l]/sum;
        }

        // Particles resampling according to their weights
        double[][] newParticles = new double[n][10];
        int j = 0;
        double sum_w = weights[0];
        double u = Math.random() / (double)n;
        for (int i = 0; i < n; i++) {
            while (sum_w < u) {
                j+=1;
                sum_w += weights[j];
            }
            newParticles[i] = particles[j];
            u += 1d / (double)n;
        }
        return newParticles;
    }

    static private double quaternionNorm(Quaternion q, Quaternion qHat){
        Quaternion qProd = q.multiply(qHat.getConjugate());
        double[] vec = qProd.getVectorPart();
        return 2*(Math.abs(vec[0])+Math.abs(vec[1])+Math.abs(vec[2]));
    }
}
