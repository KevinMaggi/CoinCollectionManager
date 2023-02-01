package io.github.kevinmaggi.coin_collection_manager.core.model;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Represents an entity to be persisted.
 */
@MappedSuperclass
public abstract class BaseEntity {

	/**
	 * Auto generated identifier.
	 */
	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	protected BaseEntity() {
		super();
	}

	public UUID getId() {
		return id;
	}
	
	@Override
	public abstract String toString();
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals(Object obj);
}