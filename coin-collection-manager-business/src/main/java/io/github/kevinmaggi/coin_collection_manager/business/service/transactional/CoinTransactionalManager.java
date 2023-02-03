package io.github.kevinmaggi.coin_collection_manager.business.service.transactional;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.CoinNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateCoinException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.FullAlbumException;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.exception.DatabaseOperationException;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.TransactionManager;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.repository.*;

/**
 * This class is an implementation of {@code CoinManager} using {@code TransactionalManager}.
 */
public class CoinTransactionalManager extends TransactionalManager implements CoinManager {
	
	private static final String DB_EXCEPTION_MSG = "Something went wrong during the DB querying";
	private static final String DUPLICATE_COIN_MSG = "Such coin is already present in the DB";
	private static final String FULL_ALBUM_MSG = "Can't add such coin to the album because it's already full";
	private static final String COIN_NOT_FOUND_MSG = "Doesn't exist such coin in the DB";
	
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
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws CoinNotFoundException	if no coin corresponds to the id
	 */
	@Override
	public Coin findCoinById(UUID id) throws DatabaseException, CoinNotFoundException {
		try {
			Coin returned = tm.doInTransaction(
								(CoinRepository repo) -> repo.findById(id)
								);
			if (returned == null)
				throw new CoinNotFoundException(COIN_NOT_FOUND_MSG);
			else
				return returned;
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

	/**
	 * Adds a {@code Coin} to the DB.
	 * 
	 * @param coin	The coin to add
	 * @return		The coin added
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws FullAlbumException		if try to add a coin to a full album
	 * @throws DuplicateCoinException	if try to add a coin already present in DB
	 */
	@Override
	public Coin addCoin(Coin coin) throws DatabaseException, FullAlbumException, DuplicateCoinException {
		try {
			return tm.doInTransaction(
					(CoinRepository coinRepo, AlbumRepository albumRepo) -> {
						if (coinRepo.findByGradeCountryYearDescriptionAndNote(
								coin.getGrade(), coin.getCountry(), coin.getMintingYear(), coin.getDescription(), coin.getNote()
								) == null
						) {
							Album album = albumRepo.findById(coin.getAlbum());
							if (album.getOccupiedSlots() < album.getNumberOfSlots()) {
								album.setOccupiedSlots(album.getOccupiedSlots() + 1);
								albumRepo.save(album);
								return coinRepo.save(coin);
							}
							else
								throw new FullAlbumException(FULL_ALBUM_MSG);
						}
						else
							throw new DuplicateCoinException(DUPLICATE_COIN_MSG);
					}
					);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

	/**
	 * Removes a {@code Coin} from the DB.
	 * 
	 * @param coin	The coin to remove
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws CoinNotFoundException	if try to update a coin not yet/anymore in DB
	 */
	@Override
	public void deleteCoin(Coin coin) throws DatabaseException, CoinNotFoundException {
		try {
			tm.doInTransaction(
					(CoinRepository coinRepo, AlbumRepository albumRepo) -> {
						if (coin.getId() == null)
							throw new CoinNotFoundException(COIN_NOT_FOUND_MSG);
						Coin dbCoin = coinRepo.findById(coin.getId());
						if (dbCoin != null) {
							Album album = albumRepo.findById(coin.getAlbum());
							album.setOccupiedSlots(album.getOccupiedSlots() - 1);
							albumRepo.save(album);
							coinRepo.delete(coin);
							return null;
						}
						else
							throw new CoinNotFoundException(COIN_NOT_FOUND_MSG);
					}
					);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}
	
	/**
	 * Updates a {@code Coin} in the DB changing the album where it is located.
	 * 
	 * @param coin			The coin to update
	 * @param newAlbumId	The album where to move it
	 * @return				The updated coin
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws FullAlbumException		if try to add a coin to a full album
	 * @throws CoinNotFoundException	if try to update a coin not yet/anymore in DB
	 */
	public Coin moveCoin(Coin coin, UUID newAlbumId) throws DatabaseException, FullAlbumException, CoinNotFoundException {
		try {
			return tm.doInTransaction(
					(CoinRepository coinRepo, AlbumRepository albumRepo) -> {
						if (coin.getId() == null)
							throw new CoinNotFoundException(COIN_NOT_FOUND_MSG);
						Coin dbCoin = coinRepo.findById(coin.getId());
						if (dbCoin != null) {
							// retrieve coin's old album from coin's record on the db
							Album oldAlbum = albumRepo.findById(coin.getAlbum());
							// retrieve coin's new album directly from coin
							Album newAlbum = albumRepo.findById(newAlbumId);
							if (newAlbum.equals(oldAlbum)) {
								return coinRepo.save(coin);
							}
							else {
								if (newAlbum.getOccupiedSlots() < newAlbum.getNumberOfSlots()) {
									oldAlbum.setOccupiedSlots(oldAlbum.getOccupiedSlots() - 1);
									albumRepo.save(oldAlbum);
									newAlbum.setOccupiedSlots(newAlbum.getOccupiedSlots() + 1);
									albumRepo.save(newAlbum);
									coin.setAlbum(newAlbumId);
									return coinRepo.save(coin);
								}
								else
									throw new FullAlbumException(FULL_ALBUM_MSG);
							}
						}
						else
							throw new CoinNotFoundException(COIN_NOT_FOUND_MSG);
					}
					);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

}
