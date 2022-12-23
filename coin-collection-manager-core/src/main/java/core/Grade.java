package core;

/**
 * Represents the grade relative to the coin's conditions/quality (according to the Sheldon coin grading scale).
 */
public enum Grade {
	PO ("Poor"),
	FR ("Fair"),
	AG ("About Good"),
	G ("Good"),
	VG ("Very Good"),
	F ("Fine"),
	VF ("Very Fine"),
	EF ("Extremely Fine"),
	AU ("About Uncirculated"),
	MS ("Mint State"),
	PROOF ("Proof");
	
	/**
	 * The explicit description of the grade corresponding to the code.
	 */
	private String meaning;
	
	Grade(String meaning) {
		this.meaning = meaning;
	}
	
	/**
	 * Get the explicit grade description.
	 * @return		description
	 */
	public String getMeaning() {
		return this.meaning;
	}
}
