package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;

import io.github.kevinmaggi.coin_collection_manager.business.service.AlbumManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.AlbumNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateAlbumException;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;

/**
 * Presenter implementation for Albums.
 */
public class AlbumPresenter extends Presenter {
	private static final String DB_RETRIEVE_ERR_MSG = "Impossible to retrieve the albums from the database due to an error";
	private static final String DB_ERROR_LOG_FORMAT = "Error during DB operations: %s";

	private static final Logger LOGGER = LogManager.getLogger(AlbumPresenter.class);

	private AlbumManager manager;

	/**
	 * Simple constructor.
	 *
	 * @param view		view to update at each action
	 * @param manager	service layer to use for Album entities
	 */
	public AlbumPresenter(View view, AlbumManager manager) {
		super(view);
		this.manager = manager;
	}

	/**
	 * Retrieves all albums from DB and updates the view either with the list of albums or an error.
	 */
	public void getAllAlbums() {
		try {
			view.showAllAlbums(manager.findAllAlbums());
			LOGGER.info("Successfully retrieved all albums from DB.");
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error("An error occurred while retrieving all albums from DB.");
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		}
	}

	/**
	 * Retrieves a specific album from DB and updates the view either with the album or an error.
	 *
	 * @param id	id of the album to retrieve
	 */
	public synchronized void getAlbum(UUID id) {
		List<Album> actualAlbums = Collections.emptyList();
		try {
			actualAlbums = manager.findAllAlbums();
			view.showAlbum(manager.findAlbumById(id));
			LOGGER.info(() -> String.format("Successfully retrieved album %s from DB.", id.toString()));
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while retrieving album %s from DB.", id.toString()));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		} catch(AlbumNotFoundException e) {
			updateViewAlbumsListAfterAlbumNotFound(actualAlbums);
			LOGGER.warn(() -> String.format("Album %s is not present in DB.", id.toString()));
		}
	}

	/**
	 * Retrieves the album from DB that has specific name and volume and updates the view either with the list of albums or an error.
	 *
	 * @param name		name to match
	 * @param volume	volume to match
	 */
	public synchronized void searchAlbum(String name, int volume) {
		List<Album> actualAlbums = Collections.emptyList();
		try {
			actualAlbums = manager.findAllAlbums();
			view.showSearchedAlbum(manager.findAlbumByNameAndVolume(name, volume), name + " vol." + volume);
			LOGGER.info(() -> String.format("Successfully retrieved album \"%s vol.%d\" from DB.", name, volume));
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while retrieving album \"%s vol.%d\" from DB.", name, volume));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		} catch(AlbumNotFoundException e) {
			updateViewAlbumsListAfterAlbumNotFound(actualAlbums);
			LOGGER.warn(() -> String.format("Album \"%s vol.%d\" is not present in DB.", name, volume));
		}
	}

	/**
	 * Adds an album to the DB and updates the view calling appropriate feedback and invoking a success or error alert.
	 *
	 * @param album		album to add
	 */
	public synchronized void addAlbum(Album album) {
		List<Album> actualAlbums = Collections.emptyList();
		try {
			actualAlbums = manager.findAllAlbums();
			Album added = manager.addAlbum(album);
			view.albumAdded(added);
			view.showSuccess("Album successfully added: " + added.toString());
			LOGGER.info(() -> String.format("Successfully added album %s to DB.", album.toString()));
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while adding album %s from DB.", album.toString()));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		} catch(DuplicateAlbumException e) {
			view.showError("This album already exists");
			view.showAllAlbums(actualAlbums);
			LOGGER.warn(() -> String.format("Album \"%s vol.%d\" is already present in DB.", album.getName(), album.getVolume()));
		}
	}

	/**
	 * Delete an album from the DB and updates the view calling appropriate feedback and invoking a success or error alert.
	 *
	 * @param album		album to delete
	 */
	public synchronized void deleteAlbum(Album album) {
		List<Album> actualAlbums = Collections.emptyList();
		try {
			actualAlbums = manager.findAllAlbums();
			manager.deleteAlbum(album);
			view.albumDeleted(album);
			view.showSuccess("Album successfully deleted: " + album.toString());
			LOGGER.info(() -> String.format("Successfully deleted album %s to DB.", album.toString()));
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while deleting album %s from DB.", album.toString()));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		} catch(AlbumNotFoundException e) {
			updateViewAlbumsListAfterAlbumNotFound(actualAlbums);
			LOGGER.warn(() -> String.format("Album %s to delete is not present in DB.", album.toString()));
		}
	}

	/**
	 * Move an album from the DB and updates the view calling appropriate feedback and invoking a success or error alert.
	 *
	 * @param album			album to move
	 * @param newLocation	new location of the album
	 */
	public synchronized void moveAlbum(Album album, String newLocation) {
		List<Album> actualAlbums = Collections.emptyList();
		try {
			actualAlbums = manager.findAllAlbums();
			Album moved = manager.moveAlbum(album, newLocation);
			view.albumMoved(moved);
			view.showSuccess("Album successfully moved: " + moved.toString());
			LOGGER.info(() -> String.format("Successfully moved album %s.", album.toString()));
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while moving album %s.", album.toString()));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		} catch(AlbumNotFoundException e) {
			updateViewAlbumsListAfterAlbumNotFound(actualAlbums);
			LOGGER.warn(() -> String.format("Album %s to move is not present in DB.", album.toString()));
		}
	}

	private void updateViewAlbumsListAfterAlbumNotFound(List<Album> list) {
		view.showError("This album doesn't exist");
		view.showAllAlbums(list);
	}
}
