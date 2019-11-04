package privLib;
import privLib.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Dictionary;
import java.util.HashMap;

// Class designed to read policies from a policy type file
// Assuming that we are reading a single policy (downstream not included, each policy is individual)
public class Policy {
	
	public String policy_id = "";
	public String data_entity = "";
	public String data_stream_type = "";
	public String data_entity_type = "";
	
	//So how this works is by putting all the propositional related stuff
	// into the prop_funcs, while the prop_statement is a string containing logic stuff
	//  with indexes linking to the prop_funcs.
	public ArrayList<String> data_window_prop_funcs = new ArrayList<String>();  // We can turn this into a collection of objects and propositional operations
	public Map<Integer, String> data_window_index_to_funcs = new HashMap<Integer, String>();
	public ArrayList<String> data_window_prop_sentences = new ArrayList<String>();  //Composed of statements
	public Map<String, ArrayList<ArrayList<String>>> data_methods = new HashMap<String, ArrayList<ArrayList<String>>>();
	// I  think I made this into a double arraylist to account for NOTs
//	public List<String> operations = new ArrayList<>();
//	public List<String> intents = new ArrayList<>();
	
	public String data_ops_intents = "";
	
	//These are composed of key,values, where keys are the field (i.e. DATA_WINDOW) and the values are the current statement
	public Map<String, String> prop_statements = new HashMap<String, String>();
	
	
	public static String current_field = "";
	public String current_logic = "";
	public int field_num = 0;
	
	public static void printList(String[] list) {
		for(String item : list) {
            System.out.println(item);
        }
	}
	
	//Takes in a List of Strings as a policy object - where each item is a line of the policy
	public String readPolicy(List<String> policy) {
		
		for(String line : policy) {
			String[] parts = line.split(" +(?![^()]*\\))");
			ArrayList<String> components = 
					new ArrayList<String>(Arrays.asList(parts));
			components.removeAll(Arrays.asList(""));
			filterComponents(components);
		}
		return policy_id;
	}
	
	
	// Given a line of text, we check what the 
	// field is, otherwise we return the current_field
	public void filterComponents(List<String> parts) { 
		
		// Make sure we don't have an empty string
		if (!parts.isEmpty() && parts.get(0).length() > 0) {
			// Get the first element and remove the colon
			String potentialField = parts.get(0).substring(0, parts.get(0).length() - 1);
			
			//This is a regular field
			if(Arrays.asList(Constants.FIELDS).contains(potentialField)) {
				//System.out.println(potentialField);
				current_field = potentialField;
				field_num = 0;
				
				//Get the policy ID
				if(current_field.equals(Constants.POLICY)) {
					policy_id = parts.get(1);
				}
				//Get the Entity
				if(current_field.equals(Constants.ENTITY)) {
					data_entity = parts.get(1);
				}
				//Get the Data Stream Type
				if(current_field.equals(Constants.DATA_STREAM_TYPE)) {
					data_stream_type = parts.get(1);
				}
				
			}
			else {  //This is a constraint field, and we add to the policy data structures
				parse_fields(parts);
			}
			
			
			
		}
		
	}
	
	//Adds logical statements into the corresponding string and arraylist fields.
	public void add_logic_statement(boolean negate, String prop_func) {
		
		String statement_to_add = " ";
		if(negate) {
			statement_to_add = " !";
		}
		int func_count = -1; //This is the index of the current propositional function for whatever field we are using
		
		
		//Check if this is for data_window
		if(current_field.equals(Constants.DATA_WINDOW)) {
			
			//Add the propositional func to the list
			data_window_prop_funcs.add(prop_func);
			func_count = data_window_prop_funcs.size();
			data_window_index_to_funcs.put(func_count, prop_func);
			//data_window_prop_sentences.add(prop_statements.get(current_field));
		}
		else if(current_field.equals(Constants.DATA_METHODS)) {  // We add the current field to the data prop sentences
			//System.out.println("F -- " + prop_statements.get(Constants.DATA_METHODS));
			//data_window_prop_sentences.add(prop_statements.get(Constants.DATA_METHODS));
			data_window_prop_sentences.add(new String(prop_statements.get(Constants.DATA_WINDOW)));
		}
		
		
		//Changes the current statement and puts it back into the mapping
		String current_statement = prop_statements.get(current_field);
		current_statement += statement_to_add + func_count;
		prop_statements.put(current_field, current_statement);
		
		//System.out.println(current_statement);
		
	}
	
	//Assumes we aren't using a regular field, so we get
	// the propositional constraints
	public void parse_fields(List<String> components) {
		
		String field = components.get(0).substring(0, components.get(0).length() - 1);
		
		//If this is a LOGIC field, we need to convert this to AND/OR statements
		if(Arrays.asList(Constants.LOGICS).contains(field) ) {
			//System.out.println(field);
			current_logic = field;
			
			//If mapping already contains the current field, we need to add it to the 
			// sentences arraylist
			//System.out.println("+: " + prop_statements.get(current_field));
			
			if (prop_statements.containsKey(current_field)) {
				
				if(current_field.equals(Constants.DATA_WINDOW)) {
					//System.out.println("-: " + prop_statements.get(Constants.DATA_WINDOW));
					data_window_prop_sentences.add(new String(prop_statements.get(Constants.DATA_WINDOW)));
					//hell += "DD";
				}

			}
			
			//Add logic statement to this data_ops_intents
			if(current_field.equals(Constants.ALLOWED_OPS_INTENTS)) {
				data_ops_intents += current_logic + " ";
			}
			
			//System.out.println(current_field);
			//Reinitialize the statements to be placed
			prop_statements.put(current_field, current_logic);
			//System.out.println(current_field + " " + prop_statements.get(current_field));
		}
		else { //Otherwise, it corresponds to a number of constraints with NOTS
			//System.out.println(components.size());
			
			// Means there's a NOT in this party
			if (components.size() == 2) {
				add_logic_statement(true, components.get(1));
			}
			else {  //Assumes component size to be 1 here.
				add_logic_statement(false, components.get(0));
			}
			
			//Add full statement to the ALLOWED_OPS_INTENTS
			if(current_field.equals(Constants.ALLOWED_OPS_INTENTS)) {
				
				for(String x : components) {
					data_ops_intents += x + " ";
				}
			}
			
		}
		field_num++;
		
	}
	
	// data_window_prop_funcs = new ArrayList<>();  // We can turn this into a collection of objects and propositional operations
	// public String data_window_prop_statement


}
// TODO: This hasn't checked the case of dealing with empty lines when reading
// TODO: Also assumes that a 2 components string is a NOT + constraint, whereas this might not always be true
// TODO: Still have to represent the downstream case
