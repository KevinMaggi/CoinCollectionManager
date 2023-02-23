package io.github.kevinmaggi.coin_collection_manager.core.model;

import java.time.Year;
import java.util.Objects;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.core.utility.YearAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Represents a coin.
 */
@Entity
@Table(name = "coins", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"grade", "country", "minting_year", "description", "note"})})
public class Coin extends BaseEntity {

	/**
	 * Coin's conditions/quality.
	 */
	@Column(name = "grade", nullable = false)
	private Grade grade;

	/**
	 * Coin's country.
	 */
	@Column(name = "country", nullable = false)
	private String country;

	/**
	 * Coin's year of minting.
	 */
	@Column(name = "minting_year", columnDefinition = "int", nullable = false)
	@Convert(converter = YearAttributeConverter.class)
	private Year mintingYear;

	/**
	 * Coin's description (e.g. "1$", "0.01£", "2€ commemorative: 30th anniversary of the Flag of Europe").
	 */
	@Column(name = "description", nullable = false)
	private String description;

	/**
	 * Possible notes (e.g. "minting error", "for exchange", ...).
	 */
	@Column(name = "note", nullable = false)
	private String note;

	/**
	 * Album where the coin is located.
	 */
	@Column(name = "album")
	private UUID album;

	/**
	 * Constructs a new {@code Coin} specifying all its characteristics.
	 *
	 * @param grade			{@code Grade} of the coin
	 * @param country		Country of the coin
	 * @param mintingYear	Minting year of the coin
	 * @param description	Description of the coin
	 * @param note			Possible notes of the coin
	 * @param album			{@code Album} in which the coin is stored
	 */
	public Coin(Grade grade, String country, Year mintingYear, String description, String note, UUID album) {
		super();
		this.grade = grade;
		this.country = country;
		this.mintingYear = mintingYear;
		this.description = description;
		this.note = note;
		this.album = album;
	}

	protected Coin() {
		super();
	}

	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Year getMintingYear() {
		return mintingYear;
	}

	public void setMintingYear(Year mintingYear) {
		this.mintingYear = mintingYear;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public UUID getAlbum() {
		return album;
	}

	public void setAlbum(UUID album) {
		this.album = album;
	}

	@Override
	public String toString() {
		return "[" + country + " " + mintingYear + "] {" + grade.toString() + "} " + description + " (" + note + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.grade, this.country, this.mintingYear, this.description, this.note);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coin other = (Coin) obj;
		return Objects.equals(this.grade, other.grade) && Objects.equals(this.country, other.country) &&
				Objects.equals(this.mintingYear, other.mintingYear) && Objects.equals(this.description, other.description) &&
				Objects.equals(this.note, other.note);
	}
}
