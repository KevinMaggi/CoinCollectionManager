package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;

/**
 * Presenter implementation for Coins.
 */
public class CoinPresenter extends Presenter {
	private static final String DB_RETRIEVE_ERR_MSG = "Impossible to retrieve the albums from the database due to an error";
	
	private CoinManager manager;

	public CoinPresenter(View view, CoinManager manager) {
		super(view);
		this.manager = manager;
	}

	public void getAllCoins() {
		try {
			view.showAllCoins(manager.findAllCoins());
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		}
	}
	
	public void getCoinsByAlbum(Album album) {
		try {
			view.showCoinsInAlbum(manager.findCoinsByAlbum(album.getId()), album);
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		}
	}
	
	public void searchCoins(String description) {
		try {
			view.showSearchedCoins(manager.findCoinsByDescription(description), description);
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		}
	}
	
	public synchronized void addCoin(Coin coin) {
		
	}
	
	public synchronized void deleteCoin(Coin coin) {
		
	}
	
	public synchronized void moveCoin(Coin coin, Album newAlbum) {
		
	}
}
