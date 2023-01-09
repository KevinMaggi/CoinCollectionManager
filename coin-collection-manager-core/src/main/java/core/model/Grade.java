package core.model;

/**
 * Represents the grade relative to the {@code Coin}'s conditions/quality
 * (according to the <a href="https://en.wikipedia.org/wiki/Sheldon_coin_grading_scale">Sheldon coin grading scale</a>).
 * Grades are described with examples related to a Walking Liberty half dollar.
 */
public enum Grade {
	/**
	 * Clear enough to identify, date may be worn smooth with one side of the coin blanked.
	 * Coins that are very badly corroded may also fall under this category.
	 */
	PO ("Poor"),
	
	/**
	 * Some detail shows.
	 */
	FR ("Fair"),
	
	/**
	 * Readable lettering although very heavily worn. The date and design may be worn smooth.
	 */
	AG ("About Good"),
	
	/**
	 * Rims of the coin are slightly worn, design is visible, but faint in areas, with many parts of the coin worn flat.
	 * Peripheral lettering nearly full.
	 */
	G ("Good"),
	
	/**
	 * Slight detail shows, with two to three letters of the word LIBERTY showing in coins with this feature.
	 */
	VG ("Very Good"),
	
	/**
	 * Some deeply recessed areas show detail.
	 * All lettering is sharp. The letters in the word LIBERTY show completely in coins with this feature, but may be weak.
	 * Moderate to considerable, but even wear throughout the coin.
	 */
	F ("Fine"),
	
	/**
	 * Moderate wear on the higher surface features.
	 */
	VF ("Very Fine"),
	
	/**
	 * Overall sharpness. Light wear seen at the highest points of the coin. Details of the coin are sharp. Traces of mint luster may show.
	 */
	EF ("Extremely Fine"),
	
	/**
	 * Traces of wear at the highest points of the coin. At least half of the original mint luster remains.
	 */
	AU ("About Uncirculated"),
	
	/**
	 * Refers to a coin minted for regular distribution that was never actually put into circulation (uncirculated).
	 */
	MS ("Mint State"),
	
	/**
	 * Refers to special samples of a coin issue, made with special polished dies.
	 */
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
