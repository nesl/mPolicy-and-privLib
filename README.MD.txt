## What is mPolicy and privLib?

mPolicy is a language designed for enabling privacy policies in the context of mHealth, and privLib is an initial implementation of the functionalities required by mPolicy, 
such as determining the relevant privacy policies for a data stream, enforcing access during querying, and fusing policies when data byproducts are produced from data of 
different policies.

![Image of general language structure](lang_general.png?raw=true "General structure of mPolicy")

Here we show the general language structure of mPolicy that can be used to create more specific policies.  For example,

![Image of specific policy](lang.png?raw=true "Specific mPolicy example")

This example policy expresses that for all GPS data going to MayoClinic, "If the data window is outside of 9pm and 9am, 
and I'm not at my home location, and as long as the data does not extend beyond December 31st, 2019, MayoClinic is allowed to 
cluster the data for recommending points of interest or using statistical analysis for finding commonly frequented locations".


You might also be wondering what fusing policies looks like.  Here is an example, which we use in our evaluation as well.


![Image of specific policy](byproduct.png?raw=true "Example byproduct policy of combining IMU and HRV data")

The Streamer and Evaluators folder contains a set of python scripts.
policy_generator.py simple takes as input a single policy file (example in the policies_folder) , and then copies it over and over depending on the parameters set in the code
datamaker.py and contextmaker.py both create fake data streams.  The former creates a bunch of fake sensor data, and the latter uses the sampling frequency of this fake
sensor data to create similarly fake context stream values.  datamaker.py depends on the stream objects defined in streams.py to create the sensor data streams.
both line_counter.py and serve_results_reader.py are simply for parsing result files used in the evaluation, but might not be useful to others.

As for the actual privLib, we have implmented a Java library consisting of ~1500 lines of code for performining the aforementioned operations on mPolicy policies.
This library is contained in the privLib as an Eclipse project.


