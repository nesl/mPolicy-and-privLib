package privLib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import privLib.Policy;
import privLib.Constants;

//Class for splitting up policies and reading them individually - requires splitting the downstream into multiple policies
public class PolicyManager {
	
	//A buffer for the current policy we are concerned with.  I don't seem to empty this at any point???
	ArrayList<String> policy_str_list = new ArrayList<String>();
	
	// Maps a policy ID to a set of policies (i.e. includes downstreams.
	public Map<String, ArrayList<Policy>> prop_statements = new HashMap<String, ArrayList<Policy>>();
	
	// Maps an Entity to a set of policies.
	public Map<String, ArrayList<Policy>> entity_policies = new HashMap<String, ArrayList<Policy>>();
	
	public int num_policies = 0; //Just keep track of how many policies we've added.
	
	
	//Split an ID by a colon, and add it to the mapping for both id-downstream policies and entity-policies
	public void appendPolicy(Policy p, String id) {
		//System.out.println(id);
		String key = id;
		//This is a downstream policy, add it to the same mapping as the original
		if (id.contains("-")) {
			key = id.split("-")[0];
		}
		
		// If a mapping doesn't already exists, we initialize the arraylist
		if (!prop_statements.containsKey(key)) {
			ArrayList<Policy> policies = new ArrayList<Policy>();
			policies.add(p);
			prop_statements.put(key, policies);
		}
		else { //Otherwise, we just get the arraylist and append to it.
			ArrayList<Policy> policies = prop_statements.get(key);
			policies.add(p);
			prop_statements.put(key, policies);
		}
		
		String entity = p.data_entity;
		// If a mapping doesn't already exist for the entities, we initialize the policy arraylist
		if (!entity_policies.containsKey(entity)) {
			ArrayList<Policy> policies = new ArrayList<Policy>();
			policies.add(p);
			entity_policies.put(entity, policies);
		}
		else { //Otherwise, we just get the arraylist and append to it.
			ArrayList<Policy> policies = entity_policies.get(entity);
			policies.add(p);
			entity_policies.put(entity, policies);
		}

		//System.out.println("B: " + entity_policies.get("MayoClinic").get(0).hell);
//		for(String x : entity_policies.get("MayoClinic").get(0).data_window_prop_sentences) {
//			System.out.println("B: " + x);
//		}
		
		num_policies++;
		
	}
	
	
	// Read policy file
	public void readFile(String filepath) {

		Policy p = new Policy();
		
		boolean is_downstream = false;
		String data_stream_type = "";
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(
					filepath));
			String line = reader.readLine();
			while (line != null) {
				
				//Line contains "DOWNSTREAM" meaning we create a new policy off of this
				// Then reset the policy object to nothing
				if (line.contains(Constants.DOWNSTREAM)) {
					String id = p.readPolicy(policy_str_list);
					//System.out.println("D " + policy_str_list.size());
					
					//If this previous policy was a downstream policy, we take a copy of the original data stream type
					data_stream_type = p.data_stream_type;
					
					appendPolicy(p, id);
					p = new Policy();
					
					policy_str_list = new ArrayList<String>();
					
					//We begin a downstream policy
					is_downstream = true;
				}
				// OLD: line.contains(Constants.POLICY) && num_policies > 0 && policy_str_list.size() > 0
				else if (line.contains(Constants.POLICY) && policy_str_list.size() > 0) { 
					//Basically makes sure that a policy following another policy or downstream policy should still work.
					//We create a new policy, unless this is the first policy. and it should not be a downstream one.
					String id = p.readPolicy(policy_str_list);
					//System.out.println("D:" + id);
					p.data_stream_type = data_stream_type; //Set the original data stream type
					//System.out.println(policy_str_list.size());
					appendPolicy(p, id);
					p = new Policy();
					
					policy_str_list = new ArrayList<String>();
					policy_str_list.add(line);
					
					is_downstream = false;
				}
				else {
					//Ignore blank lines
					if(!line.isBlank()) {
						policy_str_list.add(line);
					}
					
				}
				
				
				// read next line
				line = reader.readLine();
			}
			
			//When the file is closed, do the same thing - create a new policy.
			String id = p.readPolicy(policy_str_list);
			if(p.data_stream_type.isBlank()) {
				p.data_stream_type = data_stream_type;
			}
			appendPolicy(p, id);
			//System.out.println(id);
			
			is_downstream = false;
			
//			for(String x : p.data_window_prop_sentences) {
//				System.out.println("A: " + x);
//			}
			//p = null;
			
			//System.out.println("Created " + num_policies + " policies.");
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
