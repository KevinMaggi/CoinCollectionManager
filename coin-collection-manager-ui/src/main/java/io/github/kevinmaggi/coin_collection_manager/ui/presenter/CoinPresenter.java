package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import java.util.Collections;
import java.util.List;

import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.CoinNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateCoinException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.FullAlbumException;
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
		List<Coin> actualCoins = Collections.emptyList();
		try {
			actualCoins = manager.findAllCoins();
			Coin added = manager.addCoin(coin);
			view.coinAdded(added);
			view.showSuccess("Coin successfully added: " + added.toString());
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch (FullAlbumException e) {
			view.showError("Impossible to add the coin to this album because it is full");
		} catch (DuplicateCoinException e) {
			view.showError("This coin already exists");
			view.showAllCoins(actualCoins);
		}
	}
	
	public synchronized void deleteCoin(Coin coin) {
		List<Coin> actualCoins = Collections.emptyList();
		try {
			actualCoins = manager.findAllCoins();
			manager.deleteCoin(coin);
			view.coinDeleted(coin);
			view.showSuccess("Coin successfully deleted: " + coin.toString());
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch (CoinNotFoundException e) {
			view.showError("This coin doesn't exist");
			view.showAllCoins(actualCoins);
		}
	}
	
	public synchronized void moveCoin(Coin coin, Album newAlbum) {
		List<Coin> actualCoins = Collections.emptyList();
		try {
			actualCoins = manager.findAllCoins();
			Coin moved = manager.moveCoin(coin, newAlbum.getId());
			view.coinMoved(moved);
			view.showSuccess("Coin successfully moved: " + moved.toString());
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch (FullAlbumException e) {
			view.showError("Impossible to move the coin to this album because it is full");
		} catch (CoinNotFoundException e) {
			view.showError("This coin doesn't exist");
			view.showAllCoins(actualCoins);
		}
	}
}
