package io.github.kevinmaggi.coin_collection_manager.core.model;

import java.time.Year;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import io.github.kevinmaggi.coin_collection_manager.core.utility.YearAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Represents a coin.
 */
@Entity
@Table(name = "coins", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"grade", "country", "minting_year", "description", "note"})})
public class Coin {
	
	/**
	 * Auto generated identifier.
	 */
	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;
	
	/**
	 * Coin's conditions/quality.
	 */
	@Column(name = "grade")
	private Grade grade;
	
	/**
	 * Coin's country.
	 */
	@Column(name = "country")
	private String country;
	
	/**
	 * Coin's year of minting.
	 */
	@Column(name = "minting_year", columnDefinition = "int")
	@Convert(converter = YearAttributeConverter.class)
	private Year mintingYear;
	
	/**
	 * Coin's description (e.g. "1$", "0.01£", "2€ commemorative: 30th anniversary of the Flag of Europe").
	 */
	@Column(name = "description")
	private String description;
	
	/**
	 * Possible notes (e.g. "minting error", "for exchange", ...).
	 */
	@Column(name = "note")
	private String note;
	
	/**
	 * Album where the coin is located
	 */
	@Column(name = "album")
	private UUID album;

	public Coin(Grade grade, String country, Year mintingYear, String description, String note, UUID album) {
		super();
		this.grade = grade;
		this.country = country;
		this.mintingYear = mintingYear;
		this.description = description;
		this.note = note;
		this.album = album;
	}

	protected Coin() {}

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
}
