package io.github.kevinmaggi.coin_collection_manager.business.service.transactional;

import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.business.service.AlbumManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.AlbumNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateAlbumException;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.exception.DatabaseOperationException;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.TransactionManager;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.repository.*;

/**
 * This class is an implementation of {@code AlbumManager} using {@code TransactionalManager}.
 */
public class AlbumTransactionalManager extends TransactionalManager implements AlbumManager {
	
	private static final String DB_EXCEPTION_MSG = "Something went wrong during the DB querying";
	private static final String DUPLICATE_ALBUM_MSG = "Such album is already present in the DB";
	private static final String ALBUM_NOT_FOUND_MSG = "Doesn't exist such album in the DB";
	
	/**
	 * Simple constructor.
	 * 
	 * @param tm	{@code TransactionManager} to use for executing code
	 */
	public AlbumTransactionalManager(TransactionManager tm) {
		super(tm);
	}

	/**
	 * Finds all {@code Album}s in DB.
	 * 
	 * @return	A list of all the {@code Album}s
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	@Override
	public List<Album> findAllAlbums() throws DatabaseException {
		try {
			return tm.doInTransaction(AlbumRepository::findAll);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

	/**
	 * Finds a specific {@code Album}.
	 * 
	 * @param id	Id of the album
	 * @return		The album
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	@Override
	public Album findAlbumById(UUID id) throws DatabaseException {
		try {
			return tm.doInTransaction(
					(AlbumRepository albumRepo) -> albumRepo.findById(id)
					);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

	/**
	 * Finds an {@code Album} with specific name and volume.
	 * 
	 * @param name		The name
	 * @param volume	The volume
	 * @return			The coin
	 * @throws DatabaseException	if an error occurs during database querying
	 */
	@Override
	public Album findAlbumByNameAndVolume(String name, int volume) throws DatabaseException {
		try {
			return tm.doInTransaction(
					(AlbumRepository albumRepo) -> albumRepo.findByNameAndVolume(name, volume)
					);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

	/**
	 * Adds an {@code Album} to the DB.
	 * 
	 * @param album		The album to add
	 * @return			The added album
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws DuplicateAlbumException	if try to add an album already present in DB
	 */
	@Override
	public Album addAlbum(Album album) throws DatabaseException, DuplicateAlbumException {
		try {
			return tm.doInTransaction(
					(AlbumRepository albumRepo) -> {
						if (albumRepo.findByNameAndVolume(album.getName(), album.getVolume()) == null) {
							return albumRepo.save(album);
						}
						else
							throw new DuplicateAlbumException(DUPLICATE_ALBUM_MSG);
					}
					);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

	/**
	 * Updates an {@code Album} in the DB.
	 * 
	 * @param album		The album to update
	 * @return			The updated album
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws AlbumNotFoundException	if try to update an album not yet/anymore in DB
	 */
	@Override
	public Album updateAlbum(Album album) throws DatabaseException, AlbumNotFoundException {
		try {
			return tm.doInTransaction(
					(AlbumRepository albumRepo) -> {
						if (album.getId() == null)
							throw new AlbumNotFoundException(ALBUM_NOT_FOUND_MSG);
						Album dbAlbum = albumRepo.findById(album.getId());
						if (dbAlbum != null)
							return albumRepo.save(album);
						else
							throw new AlbumNotFoundException(ALBUM_NOT_FOUND_MSG);
					}
					);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

	/**
	 * Removes an {@code Album} from the DB.
	 * 
	 * @param album		The album to remove
	 * @throws DatabaseException		if an error occurs during database querying
	 * @throws AlbumNotFoundException	if try to update an album not yet/anymore in DB
	 */
	@Override
	public void deleteAlbum(Album album) throws DatabaseException, AlbumNotFoundException {
		try {
			tm.doInTransaction(
				(CoinRepository coinRepo, AlbumRepository albumRepo) -> {
					if (album.getId() == null)
						throw new AlbumNotFoundException(ALBUM_NOT_FOUND_MSG);
					Album dbAlbum = albumRepo.findById(album.getId());
					if (dbAlbum != null) {
						List<Coin> coins = coinRepo.findByAlbum(album.getId());
						for (Coin coin : coins)
							coinRepo.delete(coin);
						albumRepo.delete(album);
						return null;
					}
					else
						throw new AlbumNotFoundException(ALBUM_NOT_FOUND_MSG);
				}
				);
		} catch (DatabaseOperationException e) {
			throw new DatabaseException(DB_EXCEPTION_MSG, e);
		}
	}

}
