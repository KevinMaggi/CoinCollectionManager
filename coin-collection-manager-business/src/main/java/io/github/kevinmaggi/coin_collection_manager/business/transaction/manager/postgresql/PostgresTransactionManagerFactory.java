/**
 *
 */
package io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.postgresql;

import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.TransactionManagerFactory;
import io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql.PostgresAlbumRepository;
import io.github.kevinmaggi.coin_collection_manager.core.repository.postgresql.PostgresCoinRepository;
import jakarta.persistence.EntityManager;

/**
 * This class is a concrete implementation of {@code TransactionManagerFactory} for Postgres family of product.
 * Instantiating {@code TransactionManager}, {@code CoinRepoitory} and {@code AlbumRepository} with this factory not only we ensure
 * that they will be coherent in the sense that they are for Postgres DB, but also that they use the same {@code EntityManager}.
 */
public class PostgresTransactionManagerFactory implements TransactionManagerFactory {

	private EntityManager em;
	private PostgresTransactionManager tm = null;
	private PostgresCoinRepository cr = null;
	private PostgresAlbumRepository ar = null;

	/**
	 * Simple constructor.
	 *
	 * @param em	the {@code EntityManager} to use
	 */
	public PostgresTransactionManagerFactory(EntityManager em) {
		this.em = em;
	}

	/**
	 * Get an instance of {@code PostgresTransactionManager}.
	 *
	 * @return	the {@code PostgresTransactionManager}
	 */
	@Override
	public PostgresTransactionManager getTransactionManager() {
		if (tm == null)
			tm = new PostgresTransactionManager(em, getCoinRepository(), getAlbumRepository());
		return tm;
	}

	/**
	 * Get an instance of {@code PostgresCoinRepository}.
	 *
	 * @return	the {@code PostgresCoinRepository}
	 */
	@Override
	public PostgresCoinRepository getCoinRepository() {
		if (cr == null)
			cr = new PostgresCoinRepository(em);
		return cr;
	}

	/**
	 * get an instance of {@code PostgresAlbumRepository}.
	 *
	 * @return	the {@code PostgresAlbumRepository}
	 */
	@Override
	public PostgresAlbumRepository getAlbumRepository() {
		if (ar == null)
			ar = new PostgresAlbumRepository(em);
		return ar;
	}

}
