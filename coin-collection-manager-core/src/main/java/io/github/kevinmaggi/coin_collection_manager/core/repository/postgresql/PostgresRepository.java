package io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql;

import jakarta.persistence.EntityManager;

/**
 * Base Postgres repository to extend for all type of repository for Postgres.
 */
public abstract class PostgresRepository {
	/**
	 * {@code EntityManager} to use for operation on database.
	 */
	protected EntityManager em;

	/**
	 * Simple constructor
	 *
	 * @param em	{@code EntityManager} to use for operation on database
	 */
	protected PostgresRepository(EntityManager em) {
		this.em = em;
	}
}
