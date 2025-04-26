// This is a test header comment

module com.chaoticsomeone.ModInfGen {
	// Dependencies are below	
	requires com.google.gson;
	
	// These modules are exported	
	exports com.chaoticsomeone.ModInfGen;
	exports com.chaoticsomeone.ModInfGen.model;
	
	// These modules are opened to reflection	
	opens com.chaoticsomeone.ModInfGen.model to com.google.gson;
	
	// These are the legacy lines	
	opens com.chaoticsomeone.ModInfGen to com.google.gson;
}

// This is the closing comment
