package privLib;
 
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import privLib.Support;

public class AttributeDAG {
	
	
	String DAG_NAME = "";
	JSONObject dag;
	
	public AttributeDAG(String dag_name) {
		DAG_NAME = dag_name;
	}
	
	
	
	//Let's just simplify this into finding matching intents and operations
	//To match, it means that the querying intent has to exactly match
	public boolean queryMatch(String query_attribute, String policy_attribute) {
		boolean matches = false;
		
		if(query_attribute.equals(policy_attribute)) {
			matches = true;
		}
		
		return matches;
	}
	
	
	//Convert a file into a DAG
	public void readDAG(String filepath, String filename) {
		
		//First we read the file apparently
		String json_str = Support.readFile(filepath + filename);
		
		try {
			dag = new JSONObject(json_str);
			
			//System.out.println(dag.toString());
			
			 
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
