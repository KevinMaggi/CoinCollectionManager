package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.github.kevinmaggi.coin_collection_manager.business.service.AlbumManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.AlbumNotFoundException;
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
	private static final String DB_RETRIEVE_ERR_MSG = "Impossible to retrieve the coins from the database due to an error";
	
	private CoinManager coinManager;
	private AlbumManager albumManager;

	/**
	 * Simple constructor.
	 * 
	 * @param view		view to update at each action
	 * @param manager	service layer to use for Coin entities
	 */
	public CoinPresenter(View view, CoinManager coinManager, AlbumManager albumManager) {
		super(view);
		this.coinManager = coinManager;
		this.albumManager = albumManager;
	}

	/**
	 * Retrieves all coins from DB and updates the view either with the list of coins or an error.
	 */
	public void getAllCoins() {
		try {
			view.showAllCoins(coinManager.findAllCoins());
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		}
	}
	
	/**
	 * Retrieves all coins from DB that have place in an album and updates the view either with the list of coins or an error.
	 * 
	 * @param album		album to show its content
	 */
	public synchronized void getCoinsByAlbum(Album album) {
		try {
			UUID id = album.getId();
			Album retrieved = albumManager.findAlbumById(id);
			view.showCoinsInAlbum(coinManager.findCoinsByAlbum(id), retrieved);
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch (AlbumNotFoundException e) {
			view.showError("Impossible to complete the operation because this album doesn't exist");
			view.showAllAlbums(albumManager.findAllAlbums());
		}
	}
	
	/**
	 * Retrieves a specific coin from DB and updates the view either with the coin or an error.
	 * 
	 * @param id		id of the coin to retrieve
	 */
	public synchronized void getCoin(UUID id) {
		List<Coin> actualCoins = Collections.emptyList();
		try {
			actualCoins = coinManager.findAllCoins();
			Coin coin = coinManager.findCoinById(id);
			Album album = albumManager.findAlbumById(coin.getAlbum());
			view.showCoin(coin, album);
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch (CoinNotFoundException e) {
			updateViewCoinsListAfterCoinNotFound(actualCoins);
		}
	}
	
	/**
	 * Retrieves all coins from DB that have a specific description updates the view either with the list of coins or an error.
	 * 
	 * @param description	description to match
	 */
	public void searchCoins(String description) {
		try {
			view.showSearchedCoins(coinManager.findCoinsByDescription(description), description);
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		}
	}
	
	/**
	 * Adds a coin to the DB and updates the view calling appropriate feedback and invoking a success or error alert.
	 * 
	 * @param coin		coin to add
	 */
	public synchronized void addCoin(Coin coin) {
		List<Coin> actualCoins = Collections.emptyList();
		try {
			actualCoins = coinManager.findAllCoins();
			Coin added = coinManager.addCoin(coin);
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
	
	/**
	 * Delete a coin from the DB and updates the view calling appropriate feedback and invoking a success or error alert.
	 * 
	 * @param coin		coin to delete
	 */
	public synchronized void deleteCoin(Coin coin) {
		List<Coin> actualCoins = Collections.emptyList();
		try {
			actualCoins = coinManager.findAllCoins();
			coinManager.deleteCoin(coin);
			view.coinDeleted(coin);
			view.showSuccess("Coin successfully deleted: " + coin.toString());
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch (CoinNotFoundException e) {
			updateViewCoinsListAfterCoinNotFound(actualCoins);
		}
	}
	
	/**
	 * Move a coin from the DB and updates the view calling appropriate feedback and invoking a success or error alert.
	 * 
	 * @param coin			coin to move
	 * @param newAlbum		new album into which move the coin
	 */
	public synchronized void moveCoin(Coin coin, Album newAlbum) {
		List<Coin> actualCoins = Collections.emptyList();
		try {
			actualCoins = coinManager.findAllCoins();
			UUID newId = newAlbum.getId();
			Album oldRetrieved = albumManager.findAlbumById(coin.getAlbum());
			Album newRetrieved = albumManager.findAlbumById(newId);
			Coin moved = coinManager.moveCoin(coin, newAlbum.getId());
			view.coinMoved(moved, oldRetrieved, newRetrieved);
			view.showSuccess("Coin successfully moved: " + moved.toString());
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
		} catch (FullAlbumException e) {
			view.showError("Impossible to move the coin to this album because it is full");
		} catch (CoinNotFoundException e) {
			updateViewCoinsListAfterCoinNotFound(actualCoins);
		} catch (AlbumNotFoundException e) {
			view.showError("Impossible to complete the operation because this album doesn't exist");
			view.showAllAlbums(albumManager.findAllAlbums());
		}
	}
	
	private void updateViewCoinsListAfterCoinNotFound(List<Coin> list) {
		view.showError("This coin doesn't exist");
		view.showAllCoins(list);
	}
}
