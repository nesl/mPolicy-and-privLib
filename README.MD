## What is mPolicy and privLib?

mPolicy is a language designed for enabling privacy policies in the context of mHealth, and privLib is an initial implementation of the functionalities required by mPolicy,
such as determining the relevant privacy policies for a data stream, enforcing access during querying, and fusing policies when data byproducts are produced from data of
different policies.

### Requirements and Installation

For Java Requirements you will need:
java 11.0.5 2019-10-15 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.5+10-LTS)
Eclipse IDE
You will also require java-json.jar, which you can acquire from https://mvnrepository.com/artifact/org.json/json/20140107

For Python requirements, you will need to install all requirements under "Streamers and Evaluators"/requirements.txt
'''
pip install -r requirements.txt
'''

### Setting up the Eclipse project
The main requirement here is include the java-json.jar in the project.  To do this, click on the "Project Explorer" tab, right click on the 'privLib' project, and select "Properties".  Under the Java build path, add/edit the modulepath for java-json.jar to the where it was downloaded.


### Running the Code

#### Generating Policy and Sensor Data
Note that there has already been some sample random data generated, so these next few steps are not necessary unless you want to create your own data.

Running policy_generator.py will repeatedly generate policy data under the policies/ folder using policies/policy_example.txt.  By setting the num_policies parameter in policy_generator.py, you can determine how many repeated policies to generate from the example.  The current implementation does not generate new policies, but only repeats the same policy.  This was meant for generating dummy policies to evaluate the running time of the system.

Running datamaker.py will generate random sensor streams of data.  One of these streams, the context stream, is used by the Java queryStream (in privLib/src/privLib/QueryStream.java) function to evaluate whether or not a query satisfies certain policies.  Basically, the context stream has additional data (such as date information) which is used by the queryStream function to check which policies to attach to certain timestamps.

#### Evaluating Policies and Creating New Policies

Once you open the privLib project in Eclipse, the main example file is called "MainInterface.java".  Before running this code, be sure to change the "policy_filepath" and "data_filepath" variables to the correct directories.
Running this code will first load the policy data, query over a window of sensor data (and evaluate the query against the policies), and finally, combine two policies together to create a downstream policy.

One thing to be careful of when querying the data - make sure that the start_timestamp proposed in "Streamers and Evaluators"/datamaker.py is related to the timestamps used in any queryStream operation.  In order for a query
to be satisfiable, the 'window' of time for the query has to occur within the timestamps for the sensor data.

#### File Descriptions
The Streamer and Evaluators folder contains a set of python scripts.
You can use the "requirements.txt" to pip install all the necessary packages.
policy_generator.py simple takes as input a single policy file (example in the policies_folder) , and then copies it over and over depending on the parameters set in the code
datamaker.py and contextmaker.py both create fake data streams.  The former creates a bunch of fake sensor data, and the latter uses the sampling frequency of this fake
sensor data to create similarly fake context stream values.  datamaker.py depends on the stream objects defined in streams.py to create the sensor data streams.
both line_counter.py and serve_results_reader.py are simply for parsing result files used in the evaluation, but might not be useful to others.

A few things to note about the privLib Java library:
MainInterface.java contains an example of how to perform the high level operations, but it is still fairly unreadable because it was heavily altered for running experiments.
Policy.java parses string versions of policies, and PolicyManager.java just manages all the policies that have been read.
StreamReader.java is designed for reading in a context stream and determining which policies retrieved from the PolicyManager are relevant to the stream at every time instance.
It uses Propositions.java and PropositionalEvaluate.java, which are both designed to evaluate propositional functions (such as those found in the data-windows of mPolicy policies).
These contain specific functions for evaluating whether policies are relevant as well as most restrictive.  Currently, it supports comparisons between TimeRanges, TimeGreaterThan
, LocationAt, and ButtonPressed.  The datatypes supported are of 12hr format, a very specific format of date strings (i.e. "December 30 2019"), and also latitude/longitude pairs.
QueryStream.java is designed to provide access decisions on a query depending on the relevant policies learned from StreamReader.java.
DownstreamPolicyBuilder.java combines policies to create output policies on data byproducts.
Lastly, Constants.java is just a file of major constants that we use in mPolicy.

### Understanding privLib


![Image of general language structure](lang_general.png?raw=true "General structure of mPolicy")

Here we show the general language structure of mPolicy that can be used to create more specific policies.  For example,

![Image of specific policy](lang.png?raw=true "Specific mPolicy example")

This example policy expresses that for all GPS data going to MayoClinic, "If the data window is outside of 9pm and 9am,
and I'm not at my home location, and as long as the data does not extend beyond December 31st, 2019, MayoClinic is allowed to
cluster the data for recommending points of interest or using statistical analysis for finding commonly frequented locations".


You might also be wondering what fusing policies looks like.  Here is an example, which we use in our evaluation as well.


![Image of specific policy](byproduct.png?raw=true "Example byproduct policy of combining IMU and HRV data")
