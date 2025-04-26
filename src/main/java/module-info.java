module com.chaoticsomeone.ModInfGen {
	requires com.google.gson;
	exports com.chaoticsomeone.ModInfGen;
	opens com.chaoticsomeone.ModInfGen.model to com.google.gson;
}