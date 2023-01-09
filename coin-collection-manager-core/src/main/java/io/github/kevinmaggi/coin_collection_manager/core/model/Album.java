package io.github.kevinmaggi.coin_collection_manager.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Represents an album that can contain coin.
 */
@Entity
@Table (name = "albums", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"name", "volume"})})
public class Album extends BaseEntity {
	
	/**
	 * Name of the album.
	 */
	@Column(name ="name")
	private String name;
	
	/**
	 * Volume of the album.
	 */
	@Column(name = "volume")
	private int volume;
	
	/**
	 * Where the album is located.
	 */
	@Column(name = "location")
	private String location;
	
	/**
	 * Number of slots of the album.
	 */
	@Column(name = "number_of_slots")
	private int numberOfSlots;
	
	/**
	 * number of contained coins.
	 */
	@Column(name = "number_of_occupied_slots")
	private int occupiedSlots;

	/**
	 * Constructs a new {@code Album} specifying all its characteristics.
	 * 
	 * @param name			Name of the album
	 * @param volume		Volume of the album
	 * @param location		Where the album is located
	 * @param numberOfSlots	Number of total slots of the album
	 * @param occupiedSlots	Number of coins that the album already stores
	 */
	public Album(String name, int volume, String location, int numberOfSlots, int occupiedSlots) {
		super();
		this.name = name;
		this.volume = volume;
		this.location = location;
		this.numberOfSlots = numberOfSlots;
		this.occupiedSlots = occupiedSlots;
	}
	
	protected Album() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getNumberOfSlots() {
		return numberOfSlots;
	}

	public void setNumberOfSlots(int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
	}

	public int getOccupiedSlots() {
		return occupiedSlots;
	}

	public void setOccupiedSlots(int occupiedSlots) {
		this.occupiedSlots = occupiedSlots;
	}
}
