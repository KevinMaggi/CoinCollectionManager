package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static final String DB_ERROR_LOG_FORMAT = "Error during DB operations: %s";
	
	private static final Logger LOGGER = LogManager.getLogger(CoinPresenter.class);
	
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
			LOGGER.info("Successfully retrieved all coins from DB.");
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error("An error occurred while retrieving all coins from DB.");
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
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
			LOGGER.info(() -> String.format("Successfully retrieved coins from album %s from DB.", album.toString()));
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while retrieving coins from album %s from DB.", album.toString()));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		} catch (AlbumNotFoundException e) {
			view.showError("Impossible to complete the operation because this album doesn't exist");
			view.showAllAlbums(albumManager.findAllAlbums());
			LOGGER.warn(() -> String.format("Album %s is not present in DB.", album.toString()));
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
			LOGGER.info(() -> String.format("Successfully retrieved coin %s from DB.", id.toString()));
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while retrieving coin %s from DB.", id.toString()));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		} catch (CoinNotFoundException e) {
			updateViewCoinsListAfterCoinNotFound(actualCoins);
			LOGGER.warn(() -> String.format("Coin %s is not present in DB.", id.toString()));
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
			LOGGER.info(() -> String.format("Successfully retrieved coins for \"%s\" from DB.", description));
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while retrieving coins for \"%s\" from DB.", description));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
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
			LOGGER.info(() -> String.format("Successfully added coin %s to DB.", coin.toString()));
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while adding coin %s from DB.", coin.toString()));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		} catch (FullAlbumException e) {
			view.showError("Impossible to add the coin to this album because it is full");
			LOGGER.warn(() -> String.format("Album where to insert %s is full.", coin.toString()));
		} catch (DuplicateCoinException e) {
			view.showError("This coin already exists");
			view.showAllCoins(actualCoins);
			LOGGER.warn(() -> String.format("Coin %s is already present in DB.", coin.toString()));
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
			LOGGER.info(() -> String.format("Successfully deleted coin %s to DB.", coin.toString()));
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while deleting coin %s from DB.", coin.toString()));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		} catch (CoinNotFoundException e) {
			updateViewCoinsListAfterCoinNotFound(actualCoins);
			LOGGER.warn(() -> String.format("Coin %s to delete is not present in DB.", coin.toString()));
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
			LOGGER.info(() -> String.format("Successfully moved coin %s.", coin.toString()));
		} catch (DatabaseException e) {
			view.showError(DB_RETRIEVE_ERR_MSG);
			LOGGER.error(() -> String.format("An error occurred while moving coin %s.", coin.toString()));
			LOGGER.debug(() -> String.format(DB_ERROR_LOG_FORMAT, ExceptionUtils.getRootCauseMessage(e)));
		} catch (FullAlbumException e) {
			view.showError("Impossible to move the coin to this album because it is full");
			LOGGER.warn(() -> String.format("Album where to move %s is full.", coin.toString()));
		} catch (CoinNotFoundException e) {
			updateViewCoinsListAfterCoinNotFound(actualCoins);
			LOGGER.warn(() -> String.format("Coin %s to move is not present in DB.", coin.toString()));
		} catch (AlbumNotFoundException e) {
			view.showError("Impossible to complete the operation because this album doesn't exist");
			view.showAllAlbums(albumManager.findAllAlbums());
			LOGGER.warn(() -> String.format("Album where to move %s is not present in DB.", coin.toString()));
		}
	}
	
	private void updateViewCoinsListAfterCoinNotFound(List<Coin> list) {
		view.showError("This coin doesn't exist");
		view.showAllCoins(list);
	}
}
