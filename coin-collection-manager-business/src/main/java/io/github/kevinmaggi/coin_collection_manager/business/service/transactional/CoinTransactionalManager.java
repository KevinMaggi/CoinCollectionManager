package io.github.kevinmaggi.coin_collection_manager.business.service.transactional;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.exception.DatabaseOperationException;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.TransactionManager;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.repository.*;

public class CoinTransactionalManager extends TransactionalManager implements CoinManager {
	
	private static final String DB_EXCEPTION_MSG = "Something went wrong during the DB querying";
	
	/**
	 * Simple constructor.
	 * 
	 * @param tm	{@code TransactionManager} to use for executing code
	 */
	public CoinTransactionalManager(TransactionManager tm) {
		super(tm);
	}

	/**
	 * Finds all {@code Coin}s in DB.
	 * 
	 * @return	A list of all the {@code Coin}s
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	@Override
	public List<Coin> findAllCoins() throws DatabaseException {
		try {
			return tm.doInTransaction(CoinRepository::findAll);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

	/**
	 * Finds a specific {@code Coin}.
	 * 
	 * @param id	Id of the coin
	 * @return		The coin
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	@Override
	public Coin findCoinById(UUID id) throws DatabaseException {
		try {
			return tm.doInTransaction(
					(CoinRepository repo) -> repo.findById(id)
					);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

	/**
	 * Finds {@code Coin}s contained in an {@code Album}.
	 * 
	 * @param album	The album
	 * @return		A list of all the coins
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	@Override
	public List<Coin> findCoinsByAlbum(UUID album) throws DatabaseException {
		try {
			return tm.doInTransaction(
					(CoinRepository repo) -> repo.findByAlbum(album)
					);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

	/**
	 * Finds a {@code Coin} with a specific description.
	 * 
	 * @param description	The description
	 * @return				A list with {@code Coin}s with that description
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	@Override
	public List<Coin> findCoinsByDescription(String description) throws DatabaseException {
		try {
			return tm.doInTransaction(
					(CoinRepository repo) -> repo.findByDescription(description)
					);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

	@Override
	public Coin addCoin(Coin coin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Coin updateCoin(Coin coin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteCoin(Coin coin) {
		// TODO Auto-generated method stub

	}

}
