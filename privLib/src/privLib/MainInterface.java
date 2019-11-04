package privLib;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import privLib.*;

public class MainInterface {

	final static private String policy_filepath = "";//add your own
	final static private String data_filepath = "";


	//Get the median value of an array
	public static long getMedian(long[] num_array) {
		Arrays.sort(num_array);
		long median;
		if (num_array.length % 2 == 0)
		    median = (num_array[num_array.length/2] + num_array[num_array.length/2 - 1])/2;
		else
		    median = num_array[num_array.length/2];

		return median;
	}

	public static void main(String[] args) {

		//Get all the policy files we want to evaluate
		ArrayList<String> policy_files = new ArrayList<String>();
		File policy_dir = new File(policy_filepath);

		File[] listOfFiles = policy_dir.listFiles();


		for (File file : listOfFiles) {
		    if (file.isFile() && !file.getName().equals("policy_example.txt")) {
		    	policy_files.add(file.getName());
		    }
		}

		int num_experiments = 20;
		int num_participants = 20000;
		int stream_freq = 250;
		Map<String, Integer> policy_number_map = new HashMap<String, Integer>();
		Map<String, ArrayList<Long>> parseTimes = new HashMap<String, ArrayList<Long>>();
		Map<String, ArrayList<Long>> policyStreamCreationTimes = new HashMap<String, ArrayList<Long>>();
		Map<String, ArrayList<Long>> policyQueryTime = new HashMap<String, ArrayList<Long>>();
		Map<String, ArrayList<Long>> policyCombineTime = new HashMap<String, ArrayList<Long>>();


		for (int i = 0; i < num_experiments; ++i) {

			//Shuffle the files so they are random
			Collections.shuffle(policy_files);

			//First we iterate through a number of policies
			//Then we iterate through a number of different tries (i.e. we repeat same experiment x times)
			for(String policy_file : policy_files) {

				if(!policy_file.equals("policies_compare.txt")) { //We are only interested in this file for combining
					continue;
					// DONT FORGET ABOUT THE BOTTOM ONE TOO
				}

//				long[] avgParseTime = new long[num_experiments];
//				int num_policies = 0;
//				long[] avgLineCountContext = new long[num_experiments];
//				long[] avgPolicyStreamCreationTime = new long[num_experiments];
//				long[] avgQueryTime = new long[num_experiments];
//				long[] avgCombineTime = new long[num_experiments];

				// If this doesn't contain it, none of them do.
				//  so initialize them.
				if( !parseTimes.containsKey(policy_file) ) {
					parseTimes.put(policy_file, new ArrayList<Long>());
					policyStreamCreationTimes.put(policy_file, new ArrayList<Long>());
					policyQueryTime.put(policy_file, new ArrayList<Long>());
					policyCombineTime.put(policy_file, new ArrayList<Long>());
				}


				//System.out.println("Experiment " + i + " with " + policy_file);

				//Time some of these functions
				long startTime = System.nanoTime();
				long startTime2 = System.currentTimeMillis();

				//This will take in the policies and parse them.
				String entity_name = "MayoClinic";
				PolicyManager pm = new PolicyManager();
				pm.readFile(policy_filepath + policy_file);
				//num_policies += pm.num_policies;
				policy_number_map.put(policy_file, pm.num_policies);

				//Time to parse the policies
				long parseTime = System.nanoTime() - startTime;
				//long parseTime2 = System.currentTimeMillis() - startTime2;
				//avgParseTime[i] = parseTime;
				ArrayList<Long> parseResults = parseTimes.get(policy_file);
				parseResults.add(parseTime);
				parseTimes.put(policy_file, parseResults);

				Map<String, ArrayList<Policy>> ids_to_policies = pm.prop_statements;
				Map<String, ArrayList<Policy>> entities_to_policies = pm.entity_policies;

				//Time some of these functions
				startTime = System.nanoTime();
				//Initialize with an entity
				// This creates the policy stream - test the overhead of having this.
				StreamReader stream_reader = new StreamReader(entity_name);
				int context_line_count = stream_reader.receiveStream(data_filepath, "context.txt", entities_to_policies);
				//avgLineCountContext[i] = context_line_count;  //Add it to the averages
				//Measure time to create the new context stream with policies attached
				//This depends on both the number of policies as well as the policy stream size
				long policy_stream_creation_time = System.nanoTime() - startTime;
				//avgPolicyStreamCreationTime[i] =  policy_stream_creation_time;
				ArrayList<Long> poliyStreamCreationT = policyStreamCreationTimes.get(policy_file);
				poliyStreamCreationT.add(policy_stream_creation_time);
				policyStreamCreationTimes.put(policy_file, poliyStreamCreationT);
				//System.out.println(policy_stream_creation_time);


				startTime = System.nanoTime();
				//Query for something
				QueryStream qs = new QueryStream();
				boolean satisfied = qs.queryStream(1572304074369L, 1572304075449L, data_filepath, "GPS.txt", ids_to_policies, entities_to_policies,
						entity_name, "GPS", "Clustering Data", "Recommending Points of Interest");
				//Measure the time it takes.  KEEP IN MIND THAT TIME THIS DEPENDS ON HOW EARLY WE REACH A FAILED SATISFY BOOLEAN
				long query_time = System.nanoTime() - startTime;
				//avgQueryTime[i] =  query_time;
				ArrayList<Long> policyQueryT = policyQueryTime.get(policy_file);
				policyQueryT.add(query_time);
				policyQueryTime.put(policy_file, policyQueryT);

				//Grab two policies that could be relevant
				String[] keys = new String[ids_to_policies.keySet().size()];
				//System.out.println(ids_to_policies.keySet().size());
				int keyIndex = 0;
				for(String key : ids_to_policies.keySet()) {
					keys[keyIndex] = key;
					keyIndex++;
				}

				Policy p1 = ids_to_policies.get(keys[0]).get(0);
				Policy p2 = ids_to_policies.get(keys[1]).get(0);


				//Combine Two Streams Together
				DownstreamPolicyBuilder dpb = new DownstreamPolicyBuilder();
				//Trust values are entity, class, op, data-stream-type
				ArrayList<Integer> trust_vals = new ArrayList<Integer>( Arrays.asList(1,0,1,1));
				ArrayList<Double> weights = new ArrayList<Double>(Arrays.asList(1.0, 1.0, 1.0, 1.0));

				startTime = System.nanoTime();
				//Evaluate this policy depending on how many participants and the frequency of it
				for(int j = 0; j < num_participants * stream_freq; ++j) {
				Policy outPolicy = dpb.combinePolicies(p1, p2, trust_vals, weights,
						"125", "UCLA Health", "Motion Inference", "University Research");
				}
				long policy_creation_time = System.nanoTime() - startTime;
				//avgCombineTime[i] =  policy_creation_time;
				ArrayList<Long> policyCombineT = policyCombineTime.get(policy_file);
				policyCombineT.add(policy_creation_time);
				policyCombineTime.put(policy_file, policyCombineT);

			} //end inner for

//				//Print out the results
//				String log = "";
//				log += "Number of Policies: " + num_policies/num_experiments + "\n";
//				log += "Parse Time: " + getMedian(avgParseTime) + "\n";
//				log += "Context Line Count: " + getMedian(avgLineCountContext) + "\n";
//				log += "Policy Stream Creation Time: " + getMedian(avgPolicyStreamCreationTime) + "\n";
//				log += "Data Query Time: " + getMedian(avgQueryTime) + "\n";
//				log += "Policy Combining Time: " + getMedian(avgCombineTime) + "\n";
//				System.out.println(log);
		}



		//Iterate through each policy file, and compute the median of their times.
		for(String policy_file : policy_files) {
			if(policy_file.equals("policies_compare.txt")) {
				String log = "";
				log += "Number of Policies: " + policy_number_map.get(policy_file) + "\n";
				log += "Parse Time: " + getMedian(parseTimes.get(policy_file).stream().mapToLong(l -> l).toArray()) + "\n";
				log += "Policy Stream Creation Time: " + getMedian(policyStreamCreationTimes.get(policy_file).stream().mapToLong(l -> l).toArray()) + "\n";
				log += "Data Query Time: " + getMedian(policyQueryTime.get(policy_file).stream().mapToLong(l -> l).toArray()) + "\n";
				log += "Policy Combining Time: " + getMedian(policyCombineTime.get(policy_file).stream().mapToLong(l -> l).toArray()) + "\n";
				System.out.println(log);
			}

		}




	}
}
