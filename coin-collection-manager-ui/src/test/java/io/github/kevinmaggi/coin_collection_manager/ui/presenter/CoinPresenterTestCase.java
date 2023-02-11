package io.github.kevinmaggi.coin_collection_manager.ui.presenter;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.kevinmaggi.coin_collection_manager.business.service.AlbumManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.AlbumNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.CoinNotFoundException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DatabaseException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.DuplicateCoinException;
import io.github.kevinmaggi.coin_collection_manager.business.service.exception.FullAlbumException;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;

public class CoinPresenterTestCase {
	// Test variables
	private String DB_RETRIEVE_ERR_MSG = "Impossible to retrieve the coins from the database due to an error";
	private String COIN_ADDED_PREFIX = "Coin successfully added: ";
	private String COIN_REMOVED_PREFIX = "Coin successfully deleted: ";
	private String COIN_MOVED_PREFIX = "Coin successfully moved: ";
	private String FULL_ALBUM_MSG = "Impossible to add the coin to this album because it is full";
	private String FULL_NEW_ALBUM_MSG = "Impossible to move the coin to this album because it is full";
	private String DUPLICATED_COIN_MSG = "This coin already exists";
	private String COIN_NOT_FOUND_MSG = "This coin doesn't exist";
	private String ALBUM_NOT_FOUND_MSG = "Impossible to complete the operation because this album doesn't exist";
	
	private UUID UUID_COIN_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	private UUID UUID_ALBUM_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
	private UUID UUID_ALBUM_2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
	
	private Album ALBUM_1 = new Album("2€ commemorative", 1, "Armadio", 50, 25);
	private Album ALBUM_2 = new Album("Pre-euro", 1, "Armadio", 50, 25);
	private Coin COIN_1 = new Coin(Grade.G, "Greece", Year.of(2004), "2€ comm. Olympics Game of Athen 2004", "", UUID_ALBUM_1);
	private Coin COIN_2 = new Coin(Grade.AG, "Italy", Year.of(1995), "500 Lire", "", UUID_ALBUM_2);

	// Tests
	private AutoCloseable closeable;
	@Mock
	View view;
	@Mock
	CoinManager coinManager;
	@Mock
	AlbumManager albumManager;
		
	CoinPresenter presenter;
	
	@BeforeEach
	void setupTestCase() {
		closeable = MockitoAnnotations.openMocks(this);
		
		presenter = new CoinPresenter(view, coinManager, albumManager);
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::getAllCoins method")
	class GetAllCoins {
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testGetAllCoinsCallViewIfManagerDoesNotThrowException() {
			List<Coin> list = Arrays.asList(COIN_1, COIN_2);
			when(coinManager.findAllCoins()).thenReturn(list);
			
			InOrder inOrder = inOrder(view, coinManager);
			
			presenter.getAllCoins();
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(view).showAllCoins(list);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws exception")
		void testGetAllCoinsCallViewErrorIfManagerThrowsException() {
			when(coinManager.findAllCoins()).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, coinManager);
			
			presenter.getAllCoins();
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::getCoinsByAlbum method")
	class GetCoinsByAlbum {
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testGetCoinsByAlbumCallViewIfManagerDoesNotThrowException() {
			List<Coin> list = Arrays.asList(COIN_1);
			Album spiedAlbum = spy(ALBUM_1);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_1);
			when(albumManager.findAlbumById(UUID_ALBUM_1)).thenReturn(spiedAlbum);
			when(coinManager.findCoinsByAlbum(UUID_ALBUM_1)).thenReturn(list);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager, spiedAlbum);
			
			presenter.getCoinsByAlbum(spiedAlbum);
			
			inOrder.verify(albumManager).findAllAlbums();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(coinManager).findCoinsByAlbum(UUID_ALBUM_1);
			inOrder.verify(view).showCoinsInAlbum(list, spiedAlbum);
			verifyNoMoreInteractions(spiedAlbum);
			verifyNoMoreInteractions(albumManager);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws DB exception")
		void testGetCoinsByAlbumCallViewErrorIfManagerThrowsDbException() {
			Album spiedAlbum = spy(ALBUM_1);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_1);
			when(albumManager.findAlbumById(UUID_ALBUM_1)).thenReturn(spiedAlbum);
			when(coinManager.findCoinsByAlbum(UUID_ALBUM_1)).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager, spiedAlbum);
			
			presenter.getCoinsByAlbum(spiedAlbum);
			
			inOrder.verify(albumManager).findAllAlbums();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(coinManager).findCoinsByAlbum(UUID_ALBUM_1);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(spiedAlbum);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws album not found exception")
		void testGetCoinsByAlbumCallViewErrorIfManagerThrowsAlbumNotFoundException() {
			List<Album> list = Arrays.asList(ALBUM_2);
			Album spiedAlbum = spy(ALBUM_1);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_1);
			when(albumManager.findAlbumById(UUID_ALBUM_1)).thenThrow(AlbumNotFoundException.class);
			when(albumManager.findAllAlbums()).thenReturn(list);
			
			InOrder inOrder = inOrder(view, albumManager, spiedAlbum);
			
			presenter.getCoinsByAlbum(spiedAlbum);
			
			inOrder.verify(albumManager).findAllAlbums();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(view).showError(ALBUM_NOT_FOUND_MSG);
			inOrder.verify(view).showAllAlbums(list);
			verifyNoMoreInteractions(spiedAlbum);
			verifyNoMoreInteractions(albumManager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::getCoin method")
	class GetCoin {
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testGetCoinCallViewIfManagerDoesNotThrowException() {
			when(coinManager.findCoinById(UUID_COIN_1)).thenReturn(COIN_1);
			when(albumManager.findAlbumById(UUID_ALBUM_1)).thenReturn(ALBUM_1);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager);
			
			presenter.getCoin(UUID_COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(coinManager).findCoinById(UUID_COIN_1);
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(view).showCoin(COIN_1, ALBUM_1);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(albumManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws DB exception")
		void testGetCoinCallViewErrorIfManagerThrowsDbException() {
			when(coinManager.findCoinById(UUID_COIN_1)).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager);
			
			presenter.getCoin(UUID_COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(coinManager).findCoinById(UUID_COIN_1);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(albumManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws coin not found exception")
		void testGetCoinCallViewErrorIfManagerThrowsCoinNotFoundException() {
			List<Coin> list = Arrays.asList(COIN_2);
			when(coinManager.findAllCoins()).thenReturn(list);
			when(coinManager.findCoinById(UUID_COIN_1)).thenThrow(CoinNotFoundException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager);
			
			presenter.getCoin(UUID_COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(coinManager).findCoinById(UUID_COIN_1);
			inOrder.verify(view).showError(COIN_NOT_FOUND_MSG);
			inOrder.verify(view).showAllCoins(list);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(albumManager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::searchCoins method")
	class SearchCoins {
		String key = "2€";
		
		@Test
		@DisplayName("Test when manager doesn't throw exception")
		void testSearchCoinsCallViewIfManagerDoesNotThrowException() {
			List<Coin> list = Arrays.asList(COIN_2);
			when(coinManager.findCoinsByDescription(key)).thenReturn(list);
			
			InOrder inOrder = inOrder(view, coinManager);
			
			presenter.searchCoins(key);
			
			inOrder.verify(coinManager).findCoinsByDescription(key);
			inOrder.verify(view).showSearchedCoins(list, key);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws exception")
		void testSearchCoinsCallViewErrorIfManagerThrowsException() {
			when(coinManager.findCoinsByDescription(key)).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, coinManager);
			
			presenter.searchCoins(key);
			
			inOrder.verify(coinManager).findCoinsByDescription(key);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::addCoin method")
	class AddCoin {
		@Test
		@DisplayName("Test when manager doesn't throw exceptions")
		void testAddCoinCallViewIfManagerDoesNotThrowExceptions() {
			when(coinManager.addCoin(COIN_1)).thenReturn(COIN_1);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager);
			
			presenter.addCoin(COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(albumManager).findAllAlbums();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(coinManager).addCoin(COIN_1);
			inOrder.verify(view).coinAdded(COIN_1);
			inOrder.verify(view).showSuccess(COIN_ADDED_PREFIX + COIN_1.toString());
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws DB exception")
		void testAddCoinCallViewErrorIfManagerThrowsDBException() {
			when(coinManager.addCoin(COIN_1)).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager);
			
			presenter.addCoin(COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(albumManager).findAllAlbums();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(coinManager).addCoin(COIN_1);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws full album exception")
		void testAddCoinCallViewErrorIfManagerThrowsFullAlbumException() {
			when(coinManager.addCoin(COIN_1)).thenThrow(FullAlbumException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager);
			
			presenter.addCoin(COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(albumManager).findAllAlbums();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(coinManager).addCoin(COIN_1);
			inOrder.verify(view).showError(FULL_ALBUM_MSG);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws duplicated coin exception")
		void testAddCoinCallViewErrorIfManagerThrowsDuplicatedCoinException() {
			List<Coin> list = Arrays.asList(COIN_1, COIN_2);
			when(coinManager.findAllCoins()).thenReturn(list);
			when(coinManager.addCoin(COIN_1)).thenThrow(DuplicateCoinException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager);
			
			presenter.addCoin(COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(albumManager).findAllAlbums();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(coinManager).addCoin(COIN_1);
			inOrder.verify(view).showError(DUPLICATED_COIN_MSG);
			inOrder.verify(view).showAllCoins(list);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws album not found exception")
		void testAddCoinCallViewErrorIfManagerThrowsAlbumNotFoundException() {
			List<Album> list = Arrays.asList(ALBUM_2);
			when(albumManager.findAllAlbums()).thenReturn(list);
			when(albumManager.findAlbumById(COIN_1.getAlbum())).thenThrow(AlbumNotFoundException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager);
			
			presenter.addCoin(COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(albumManager).findAllAlbums();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(view).showError(ALBUM_NOT_FOUND_MSG);
			inOrder.verify(view).showAllAlbums(list);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::deleteCoin method")
	class DeleteCoin {
		@Test
		@DisplayName("Test when manager doesn't throw exceptions")
		void testDeleteCoinCallViewIfManagerDoesNotThrowExceptions() {			
			InOrder inOrder = inOrder(view, coinManager);
			
			presenter.deleteCoin(COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(coinManager).deleteCoin(COIN_1);
			inOrder.verify(view).coinDeleted(COIN_1);
			inOrder.verify(view).showSuccess(COIN_REMOVED_PREFIX + COIN_1.toString());
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws DB exception")
		void testDeleteCoinCallViewErrorIfManagerThrowsDBException() {
			doThrow(DatabaseException.class).when(coinManager).deleteCoin(COIN_1);
			
			InOrder inOrder = inOrder(view, coinManager);
			
			presenter.deleteCoin(COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(coinManager).deleteCoin(COIN_1);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws coin not found exception")
		void testDeleteCoinCallViewErrorIfManagerThrowsCoinNotFoundException() {
			List<Coin> list = Arrays.asList(COIN_2);
			when(coinManager.findAllCoins()).thenReturn(list);
			doThrow(CoinNotFoundException.class).when(coinManager).deleteCoin(COIN_1);
			
			InOrder inOrder = inOrder(view, coinManager);
			
			presenter.deleteCoin(COIN_1);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(coinManager).deleteCoin(COIN_1);
			inOrder.verify(view).showError(COIN_NOT_FOUND_MSG);
			inOrder.verify(view).showAllCoins(list);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@Nested
	@DisplayName("Tests for CoinPresenter::moveCoin method")
	class MoveCoin {
		@Test
		@DisplayName("Test when manager doesn't throw exceptions")
		void testMoveCoinCallViewIfManagerDoesNotThrowExceptions() {
			Album spiedAlbum = spy(ALBUM_2);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_2);
			when(albumManager.findAlbumById(UUID_ALBUM_1)).thenReturn(ALBUM_1);
			when(albumManager.findAlbumById(UUID_ALBUM_2)).thenReturn(spiedAlbum);
			when(coinManager.moveCoin(COIN_1, UUID_ALBUM_2)).thenReturn(COIN_1);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager, spiedAlbum);
			
			presenter.moveCoin(COIN_1, spiedAlbum);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_2);
			inOrder.verify(coinManager).moveCoin(COIN_1, UUID_ALBUM_2);
			inOrder.verify(view).coinMoved(COIN_1, ALBUM_1, spiedAlbum);
			inOrder.verify(view).showSuccess(COIN_MOVED_PREFIX + COIN_1.toString());
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws DB exception")
		void testMoveCoinCallViewErrorIfManagerThrowsDBException() {
			Album spiedAlbum = spy(ALBUM_2);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_2);
			when(albumManager.findAlbumById(UUID_ALBUM_1)).thenReturn(ALBUM_1);
			when(albumManager.findAlbumById(UUID_ALBUM_2)).thenReturn(spiedAlbum);
			when(coinManager.moveCoin(COIN_1, UUID_ALBUM_2)).thenThrow(DatabaseException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager, spiedAlbum);
			
			presenter.moveCoin(COIN_1, spiedAlbum);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_2);
			inOrder.verify(coinManager).moveCoin(COIN_1, UUID_ALBUM_2);
			inOrder.verify(view).showError(DB_RETRIEVE_ERR_MSG);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws full album exception")
		void testMoveCoinCallViewErrorIfManagerThrowsFullAlbumException() {
			Album spiedAlbum = spy(ALBUM_2);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_2);
			when(albumManager.findAlbumById(UUID_ALBUM_1)).thenReturn(ALBUM_1);
			when(albumManager.findAlbumById(UUID_ALBUM_2)).thenReturn(spiedAlbum);
			when(coinManager.moveCoin(COIN_1, UUID_ALBUM_2)).thenThrow(FullAlbumException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager, spiedAlbum);
			
			presenter.moveCoin(COIN_1, spiedAlbum);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_2);
			inOrder.verify(coinManager).moveCoin(COIN_1, UUID_ALBUM_2);
			inOrder.verify(view).showError(FULL_NEW_ALBUM_MSG);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws coin not found exception")
		void testMoveCoinCallViewErrorIfManagerThrowsCoinNotFoundException() {
			List<Coin> list = Arrays.asList(COIN_2);
			when(coinManager.findAllCoins()).thenReturn(list);
			Album spiedAlbum = spy(ALBUM_2);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_2);
			when(albumManager.findAlbumById(UUID_ALBUM_1)).thenReturn(ALBUM_1);
			when(albumManager.findAlbumById(UUID_ALBUM_2)).thenReturn(spiedAlbum);
			when(coinManager.moveCoin(COIN_1, UUID_ALBUM_2)).thenThrow(CoinNotFoundException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager, spiedAlbum);
			
			presenter.moveCoin(COIN_1, spiedAlbum);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_2);
			inOrder.verify(coinManager).moveCoin(COIN_1, UUID_ALBUM_2);
			inOrder.verify(view).showError(COIN_NOT_FOUND_MSG);
			inOrder.verify(view).showAllCoins(list);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(view);
		}
		
		@Test
		@DisplayName("Test when manager throws album not found exception")
		void testMoveCoinCallViewErrorIfManagerThrowsAlbumNotFoundException() {
			List<Album> list = Arrays.asList(ALBUM_1);
			when(albumManager.findAllAlbums()).thenReturn(list);
			Album spiedAlbum = spy(ALBUM_2);
			when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_2);
			when(albumManager.findAlbumById(UUID_ALBUM_1)).thenReturn(ALBUM_1);
			when(albumManager.findAlbumById(UUID_ALBUM_2)).thenThrow(AlbumNotFoundException.class);
			
			InOrder inOrder = inOrder(view, coinManager, albumManager, spiedAlbum);
			
			presenter.moveCoin(COIN_1, spiedAlbum);
			
			inOrder.verify(coinManager).findAllCoins();
			inOrder.verify(albumManager).findAllAlbums();
			inOrder.verify(spiedAlbum).getId();
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_1);
			inOrder.verify(albumManager).findAlbumById(UUID_ALBUM_2);
			inOrder.verify(view).showError(ALBUM_NOT_FOUND_MSG);
			inOrder.verify(view).showAllAlbums(list);
			verifyNoMoreInteractions(coinManager);
			verifyNoMoreInteractions(albumManager);
			verifyNoMoreInteractions(view);
		}
	}
	
	@AfterEach
	void cleanTestCase() throws Exception {
		closeable.close();
	}
}
