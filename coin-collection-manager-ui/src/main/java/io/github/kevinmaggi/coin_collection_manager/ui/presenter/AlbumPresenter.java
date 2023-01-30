package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import io.github.kevinmaggi.coin_collection_manager.business.service.AlbumManager;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;

/**
 * Presenter implementation for Albums.
 */
public class AlbumPresenter extends Presenter {
	
	private AlbumManager manager;

	public AlbumPresenter(View view, AlbumManager manager) {
		super(view);
		this.manager = manager;
	}

	public void getAllAlbums() {
		
	}
	
	public void searchAlbum(String name, int volume) {
		
	}
	
	public synchronized void addAlbum(Album album) {
		
	}
	
	public synchronized void deleteAlbum(Album album) {
		
	}
	
	public synchronized void moveAlbum(Album album, String newLocation) {
		
	}
}
