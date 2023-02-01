package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		}
	}
	
	/**
	 * Retrieves a specific album from DB and updates the view either with the album or an error.
	 * 
	 * @param id	id of the album to retrieve
	 */
	public void getAlbum(UUID id) {
		List<Album> actualAlbums = Collections.emptyList();
		try {
			actualAlbums = manager.findAllAlbums();
			view.showAlbum(manager.findAlbumById(id));
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch(AlbumNotFoundException e) {
			updateViewAlbumsListAfterAlbumNotFound(actualAlbums);
		}
	}
	
	/**
	 * Retrieves the album from DB that has specific name and volume and updates the view either with the list of albums or an error.
	 * 
	 * @param name		name to match
	 * @param volume	volume to match
	 */
	public void searchAlbum(String name, int volume) {
		List<Album> actualAlbums = Collections.emptyList();
		try {
			actualAlbums = manager.findAllAlbums();
			view.showAllAlbums(Arrays.asList(manager.findAlbumByNameAndVolume(name, volume)));
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch(AlbumNotFoundException e) {
			updateViewAlbumsListAfterAlbumNotFound(actualAlbums);
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
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch(DuplicateAlbumException e) {
			view.showError("This album already exists");
			view.showAllAlbums(actualAlbums);
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
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch(AlbumNotFoundException e) {
			updateViewAlbumsListAfterAlbumNotFound(actualAlbums);
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
		} catch(DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch(AlbumNotFoundException e) {
			updateViewAlbumsListAfterAlbumNotFound(actualAlbums);
		}
	}
	
	private void updateViewAlbumsListAfterAlbumNotFound(List<Album> list) {
		view.showError("This album doesn't exist");
		view.showAllAlbums(list);
	}
}
