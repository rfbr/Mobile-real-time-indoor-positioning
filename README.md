# Mobile real-time indoor positioning system using deep-learning and particle filtering

Introduction
= 
This is the first implementation of a mobile real-time indoor positioning system using a smart-phone held in hand. In order to achieve this, we will only use the Inertial Measurement Unit (IMU) present in the phone, gathering acceleration and angular velocity. The proposed method is based on 6-DOF odometry, computing relative sensor pose changes between two sequential moments using a neural network (presented in this [project](https://github.com/rfbr/PDR_with_Deep_Learning)). The estimate will then be refined by a Bayesian
filter, a particle filter, associated with a map-matching algorithm.    

Note on the particle filtering algorithm
=
Despite the good results obtained using the neural network, it is obvious that the relative estimates suffer from an error that accumulates over time. To reduce this error, we improved our estimate using a Bayesian filter, the particle filtering.
Indeed, the evolution of our system, the pedestrian, can be identified as a Hidden Markov Chain. We can not know the exact state of the pedestrian, i.e. its position and its orientation, we only have access to one observation of this state through the neural network. However, we have a priori on the evolution of our system, this one following the laws of physics: knowing its initial state as well as its acceleration and its speed over time, we can deternine its evolution at each time *t*. Nevertheless the sensors being subjected to noise and bias, we only have a probabilistic evolution. 

Consider the pedestrian in a given state at time *t*, *E_t*, we have an evolution distribution who gives us a hypothetical state at time $t+1$ that we will denote *E_t+1*. As said before, we cannot have access to the real state of the pedestrian, however we have an observation *O_t+1* given by the neural network estimation. Thus, Bayes' theorem gives us:

Hence, we can define the posterior probability to be in the state *E_t+1* given the observation *O_t+1* according to what's called likelihood *Pr(O_t+1|E_t+1)*, this probability reflects the confidence we place in our observation, the prior distribution *Pr(E_t+1)* and the marginal probability of the observation, this one being unknown but common to all hythetical states.

The particle filter resumes this system presented above with the principle that that each hypothetical state will be represented by the evolution of a particle. We will compute the likelihood of each particle at a given time, which will define the weight of the partcile, to redefine by resampling the particles according to their weight the posterior distribution of the pedestrian real state. To estimate this state, we will take the empirical mean of the particle state, which estimates the posterior expectation.

In addition, with this statistical estimation of the ground truth, we can incorporate some information about where the pedestrian is moving. 

Results
=
![mm1](https://user-images.githubusercontent.com/45492759/69190649-23c28b80-0b21-11ea-92ac-1765d1079c17.png)
![mm2](https://user-images.githubusercontent.com/45492759/69190653-258c4f00-0b21-11ea-8d80-82303c195343.png)
![mm5](https://user-images.githubusercontent.com/45492759/69190689-376df200-0b21-11ea-8948-44261e893448.png)
![mm4](https://user-images.githubusercontent.com/45492759/69190693-3937b580-0b21-11ea-8328-ab6e5d9aad65.png)
