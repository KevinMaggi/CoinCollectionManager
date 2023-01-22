/**
 * 
 */
package io.github.kevinmaggi.coin_collection_manager.business.transaction.manager;

import io.github.kevinmaggi.coin_collection_manager.core.repository.AlbumRepository;
import io.github.kevinmaggi.coin_collection_manager.core.repository.CoinRepository;

/**
 * This interface is needed to instantiate {@code TransactionManager}, {@code AlbumRepository} and {@code CoinRepository} objects of 
 * the same type (e.g. Postgres) because the <b>have to</b> belong to the same family. Instantiating them through a factory guarantees 
 * coherence and allows to choose at runtime (by instantiating the wanted type of {@code TransactionManagerFactory} in only one place of 
 * the code. Implements an Abstract Factory pattern.
 */
public interface TransactionManagerFactory {
	/**
	 * Get an instance of {@code TransactionManager}.
	 * 
	 * @return	the {@code TransactionManager}
	 */
	public TransactionManager getTransactionManager();
	
	/**
	 * Get an instance of {@code CoinRepository}.
	 * 
	 * @return	the {@code CoinRepository}
	 */
	public CoinRepository getCoinRepository();
	
	/**
	 * get an instance of {@code AlbumRepository}.
	 * 
	 * @return	the {@code AlbumRepository}
	 */
	public AlbumRepository getAlbumRepository();
}
