package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;

/**
 * Presenter implementation for Coins.
 */
public class CoinPresenter extends Presenter {
	
	private CoinManager manager;

	public CoinPresenter(View view, CoinManager manager) {
		super(view);
		this.manager = manager;
	}

	public void getAllCoins() {
		
	}
	
	public void getCoinsByAlbum(Album album) {
		
	}
	
	public void searchCoins(String description) {
		
	}
	
	public synchronized void addCoin(Coin coin) {
		
	}
	
	public synchronized void deleteCoin(Coin coin) {
		
	}
	
	public synchronized void moveCoin(Coin coin, Album newAlbum) {
		
	}
}
