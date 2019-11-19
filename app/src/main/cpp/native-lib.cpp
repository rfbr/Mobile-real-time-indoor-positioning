#include <jni.h>
#include <string>
#include <vector>
#include <random>
using namespace std;
extern "C" JNIEXPORT jintArray
JNICALL
Java_com_example_ml_1pdr_ParticleFiltering_resample(
        JNIEnv *env,
        jobject /* this */,
        jdoubleArray arr) {
    jsize size = env->GetArrayLength( arr );
    vector<double> weights( 10 );
    env->GetDoubleArrayRegion( arr, 0, size, &weights[0] );

    random_device rd;
    mt19937 gen(rd());
    uniform_real_distribution<double> unif(0.,1.);

    double sum = 0;
    for (int i = 0; i < size; i++)
    {
        sum+= weights[i];
    }

    for (int i = 0; i < size; i++)
    {
        weights[i] /= sum;
    }
    double sum_w = weights[0];
    vector<int> results(size);
    int j =0;
    double u = unif(gen)/size;
    for (int i = 0; i < size; i++)
    {
        while(sum_w<u){
            j+=1;
            sum_w += weights[j];
        }
        results[i] = j;
        u += 1./size;
    }

    jintArray output = env->NewIntArray( results.size() );
    env->SetIntArrayRegion( output, 0, results.size(), &results[0] );
    return output;
}

extern "C" JNIEXPORT jdoubleArray
JNICALL
Java_com_example_ml_1pdr_ParticleFiltering_test(
        JNIEnv *env,
        jobject /* this */,
        jdoubleArray arr) {
    jsize size = env->GetArrayLength( arr );
    vector<double> particle( size );
    env->GetDoubleArrayRegion( arr, 0, size, &particle[0] );
    double sigma = .5;
    random_device rd;
    mt19937 gen(rd());
    normal_distribution<> d{0,1};

    particle[0] += d(gen);
    particle[1] += d(gen);
    particle[2] += d(gen);
    double p = exp(-(pow(particle[0],2)+pow(particle[1],2)+pow(particle[2],2))/(2*sigma*sigma))/(sqrt(2*M_PI)*sigma);

    vector<double> results(4);
    results[0] = particle[0];
    results[1] = particle[1];
    results[2] = particle[2];
    results[3] = p;
    jdoubleArray output = env->NewDoubleArray( results.size() );
    env->SetDoubleArrayRegion( output, 0, results.size(), &results[0] );
    return output;
}