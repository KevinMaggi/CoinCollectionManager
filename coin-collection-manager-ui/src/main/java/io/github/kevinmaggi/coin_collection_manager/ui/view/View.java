package io.github.kevinmaggi.coin_collection_manager.ui.view;

import java.util.List;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;

/**
 * This interface defines the methods for the view component of a MVP pattern.
 */
public interface View {
	/**
	 * Shows all albums in the dedicated element and perform additional operation (e.g. inform the user).
	 * 
	 * @param albums	albums to show
	 */
	void showAllAlbums(List<Album> albums);
	
	/**
	 * Shows the albums result of search in the dedicated element and perform additional operation (e.g. inform the user).
	 * 
	 * @param albums	albums to show
	 * @param search	searching key as string
	 */
	void showSearchedAlbums(List<Album> albums, String search);
	
	/**
	 * Shows a selected album in the dedicated element and perform additional operation (e.g. inform the user).
	 * 
	 * @param album		album to show
	 */
	void showAlbum(Album album);
	
	/**
	 * Feedbacks the user to an added album (e.g. shows a dialog, updates the list of albums).
	 * 
	 * @param album		added album
	 */
	void albumAdded(Album album);
	
	/**
	 * Feedbacks the user to a deleted album (e.g. shows a dialog, updates the list of albums).
	 * 
	 * @param album		deleted album
	 */
	void albumDeleted(Album album);
	
	/**
	 * Feedbacks the user to a moved album (e.g. shows a dialog, updates the location of the album).
	 * 
	 * @param album		moved album
	 */
	void albumMoved(Album album);
	
	/**
	 * Shows all coins in the dedicated element and perform additional operation (e.g. inform the user).
	 * 
	 * @param coins		coins to show
	 */
	void showAllCoins(List<Coin> coins);
	
	/**
	 * Shows the coins result of search in the dedicated element and perform additional operation (e.g. inform the user).
	 * 
	 * @param coins		coins to show
	 * @param search	searching key as string
	 */
	void showSearchedCoins(List<Coin> coins, String search);
	
	/**
	 * Shows the coins contained in an album in the dedicated element and perform additional operation (e.g. inform the user).
	 * 
	 * @param coins		coins to show
	 * @param album		album subject of the filter
	 */
	void showCoinsInAlbum(List<Coin> coins, Album album);
	
	/**
	 * Shows a selected coin in the dedicated element and perform additional operation (e.g. inform the user).
	 * 
	 * @param coin		coin to show
	 * @param album		album to which belongs
	 */
	void showCoin(Coin coin, Album album);
	
	/**
	 * Feedbacks the user to an added coin (e.g. shows a dialog, updates the list of albums).
	 * 
	 * @param coin		added coin
	 */
	void coinAdded(Coin coin);
	
	/**
	 * Feedbacks the user to a deleted coin (e.g. shows a dialog, updates the list of albums).
	 * 
	 * @param coin		deleted coin
	 */
	void coinDeleted(Coin coin);
	
	/**
	 * Feedbacks the user to a moved coin (e.g. shows a dialog, updates the location of the album).
	 * 
	 * @param coin		moved coin
	 * @param oldAlbum	old album of the coin
	 * @param newAlbum	new album of the coin
	 */
	void coinMoved(Coin coin, Album oldAlbum, Album newAlbum);
	
	/**
	 * Shows to the user an error message
	 * 
	 * @param msg		message to show
	 */
	void showError(String msg);
	
	/**
	 * Shows to the user a success message
	 * 
	 * @param msg		message to show
	 */
	void showSuccess(String msg);
}
