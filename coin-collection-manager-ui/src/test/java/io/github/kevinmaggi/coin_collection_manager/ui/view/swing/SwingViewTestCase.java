package io.github.kevinmaggi.coin_collection_manager.ui.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.AlbumPresenter;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.CoinPresenter;

@RunWith(GUITestRunner.class)
public class SwingViewTestCase extends AssertJSwingJUnitTestCase {
	// Test variables	
	private UUID UUID_COIN_PRE = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
	private UUID UUID_ALBUM_COMM_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174003");
	private UUID UUID_ALBUM_PRE = UUID.fromString("123e4567-e89b-12d3-a456-426614174005");
	
	private Album ALBUM_COMM_1 = new Album("2€ commemorative", 1, "Armadio", 50, 2);
	private Album ALBUM_COMM_2 = new Album("2€ commemorative", 2, "Armadio", 50, 0);
	private Album ALBUM_PRE= new Album("Pre-euro", 1, "Armadio", 50, 1);
	private Coin COIN_COMM_1 = new Coin(Grade.G, "GR", Year.of(2004), "2€ Olympics Game of Athen 2004", "", UUID_ALBUM_COMM_1);
	private Coin COIN_COMM_2 = new Coin(Grade.G, "IT", Year.of(2004), "2€ World Food Programme 2004", "", UUID_ALBUM_COMM_1);
	private Coin COIN_PRE = new Coin(Grade.AG, "IT", Year.of(1995), "500 Lire", "", UUID_ALBUM_PRE);
	
	// Tests
	private static final int TIMEOUT = 5000;
	
	private FrameFixture window;
	
	private AutoCloseable closeable;

	@Mock
	private CoinPresenter coinPresenter;
	@Mock
	private AlbumPresenter albumPresenter;
	
	private SwingView view;
	
	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			view = new SwingView();
			view.setPresenters(coinPresenter, albumPresenter);
			return view;
		});
		window = new FrameFixture(robot(), view);
		window.show();
	}
	
	@Test
	public void testSetPresenters() {
		// setPresenter is already called in setup method.
		// It is important to remember this also in next tests because the following calls are by default.
		
		verify(coinPresenter).getAllCoins();
		verify(albumPresenter).getAllAlbums();
	}
	
	/////////////// Test initial state
	@Test @GUITest
	public void testInitialState() {
		// Album panel
		window.label(JLabelMatcher.withText("Key name: "));
		window.textBox("albumSearchName").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Key volume: "));
		window.textBox("albumSearchVolume").requireEnabled().requireEmpty();
		window.button(JButtonMatcher.withText("Search")).requireDisabled();
		window.button(JButtonMatcher.withText("All albums")).requireEnabled();
		
		/*The label is initialized empty, only in the setPresenter method it will be written by the showAll methods 
		 *called by presenters (here mocked, so the label remains empty; anyway later will be tested the showAll methods)*/
		window.label(JLabelMatcher.withName("albumActual").andText(" "));
		window.list("albumList").requireNoSelection();
		
		window.button(JButtonMatcher.withText("Delete album")).requireDisabled();
		window.button(JButtonMatcher.withText("Move album")).requireDisabled();
		
		window.label(JLabelMatcher.withText("Name: "));
		window.textBox("albumFormName").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Volume: "));
		window.textBox("albumFormVolume").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Location: "));
		window.textBox("albumFormLocation").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Slots: "));
		window.textBox("albumFormSlots").requireEnabled().requireEmpty();
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
		
		// Coin panel
		window.label(JLabelMatcher.withText("Key description: "));
		window.textBox("coinFilterDescription").requireEnabled().requireEmpty();
		window.button(JButtonMatcher.withText("Filter")).requireDisabled();
		window.button(JButtonMatcher.withText("All coins")).requireEnabled();
		
		/*The label is initialized empty, only in the setPresenter method it will be written by the showAll methods 
		 *called by presenters (here mocked, so the label remains empty; anyway later will be tested the showAll methods)*/
		window.label(JLabelMatcher.withName("coinActual").andText(" "));
		window.list("coinList").requireNoSelection();
		
		window.button(JButtonMatcher.withText("Delete coin")).requireDisabled();
		window.button(JButtonMatcher.withText("Move coin")).requireDisabled();
		
		window.label(JLabelMatcher.withText("Description: "));
		window.textBox("coinFormDescription").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Grade: "));
		window.comboBox("coinFormGrade").requireEnabled().requireNoSelection();
		window.label(JLabelMatcher.withText("Country: "));
		window.textBox("coinFormCountry").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Year: "));
		window.textBox("coinFormYear").requireEnabled().requireEmpty();
		window.label(JLabelMatcher.withText("Album: "));
		window.comboBox("coinFormAlbum").requireEnabled().requireNoSelection();
		window.label(JLabelMatcher.withText("Note: "));
		window.textBox("coinFormNote").requireEnabled().requireEmpty();
		window.button(JButtonMatcher.withText("Save coin")).requireDisabled();
		
		// Status bar
		window.label(JLabelMatcher.withName("status").andText(" "));
	}
	
	/////////////// Test view methods implementation related to status message
	@Test @GUITest
	public void testShowErrorShouldShowMessage() {
		String MSG = "Some error message";
		
		view.showError(MSG);
		
		window.label(JLabelMatcher.withName("status").andText(MSG));
	}
	
	@Test @GUITest
	public void testShowSuccessShouldShowMessage() {
		String MSG = "Some success message";
		
		view.showSuccess(MSG);
		
		window.label(JLabelMatcher.withName("status").andText(MSG));
	}
	
	/////////////// Test view methods implementation related to albums
	@Test @GUITest
	public void testShowAllAlbumShouldUpdateListLabelSearchingAndDropdown() {
		List<Album> list = Arrays.asList(ALBUM_COMM_1, ALBUM_COMM_2, ALBUM_PRE);
		
		view.showAllAlbums(list);
		
		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();
		
		assertThat(listContents).containsExactlyInAnyOrder(ALBUM_COMM_1.toString(), ALBUM_COMM_2.toString(), ALBUM_PRE.toString());
		window.list("albumList").requireNoSelection();
		assertThat(window.label(JLabelMatcher.withName("albumActual")).text()).isEqualTo("All albums:");
		window.textBox("albumSearchName").requireEmpty();
		window.textBox("albumSearchVolume").requireEmpty();
		window.button(JButtonMatcher.withText("Search")).requireDisabled();
		assertThat(comboboxContents).containsExactlyInAnyOrder(ALBUM_COMM_1.toString(), ALBUM_COMM_2.toString(), ALBUM_PRE.toString());
	}
	
	@Test @GUITest
	public void testShowSearchedAlbumShouldShowItInListAndModifyTheLabel() {
		String SEARCHING_KEY = "Pre-euro vol.1";
		
		view.showSearchedAlbum(ALBUM_PRE, SEARCHING_KEY);
		
		String[] listContents = window.list("albumList").contents();
		
		assertThat(listContents).containsOnly(ALBUM_PRE.toString());
		assertThat(window.label(JLabelMatcher.withName("albumActual")).text())
			.isEqualTo("Results for \"" + SEARCHING_KEY + "\":");
	}
	
	@Test @GUITest
	public void testShowAlbumShouldShowTheAlbumDetails() {
		view.showAlbum(ALBUM_PRE);
		
		window.label(JLabelMatcher.withName("albumSelection").andText(ALBUM_PRE.getName() + " volume " + ALBUM_PRE.getVolume() + 
				" located in " + ALBUM_PRE.getLocation() + " with " + ALBUM_PRE.getOccupiedSlots() + "/" + ALBUM_PRE.getNumberOfSlots() + 
				" slots occupied"));
		window.button(JButtonMatcher.withText("Delete album")).requireEnabled();
		window.button(JButtonMatcher.withText("Move album")).requireEnabled();
	}
	
	@Test @GUITest
	public void testAlbumAddedShouldAddItToTheListClearTheFormAndDoNotSelectItemInComboBoxIfUnselected() {
		view.albumAdded(ALBUM_PRE);
		
		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();
		
		assertThat(listContents).contains(ALBUM_PRE.toString());
		assertThat(comboboxContents).contains(ALBUM_PRE.toString());
		assertThat(window.comboBox("coinFormAlbum").selectedItem()).isNull();
		window.textBox("albumFormName").requireEmpty();
		window.textBox("albumFormVolume").requireEmpty();
		window.textBox("albumFormLocation").requireEmpty();
		window.textBox("albumFormSlots").requireEmpty();
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
	}
	
	@Test @GUITest
	public void testAlbumAddedShouldNotChangeSelectedItemInComboBoxIfSelected() {
		GuiActionRunner.execute(() ->  view.getCoinFormAlbumModel().addElement(ALBUM_COMM_1));
		window.comboBox("coinFormAlbum").selectItem(ALBUM_COMM_1.toString());
		
		view.albumAdded(ALBUM_PRE);
		
		assertThat(window.comboBox("coinFormAlbum").selectedItem()).isEqualTo(ALBUM_COMM_1.toString());
	}
	
	@Test @GUITest
	public void testAlbumDeletedShouldRemoveFromLists() {
		GuiActionRunner.execute(() -> view.getAlbumListModel().addElement(ALBUM_PRE));
		
		view.albumDeleted(ALBUM_PRE);
		
		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();
		
		assertThat(listContents).doesNotContain(ALBUM_PRE.toString());
		assertThat(comboboxContents).doesNotContain(ALBUM_PRE.toString());
	}
	
	@Test @GUITest
	public void testMovedAlbumShouldUpdateListAndLabel() {
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});
		
		view.albumMoved(ALBUM_PRE);
		
		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();
		
		assertThat(listContents).contains(ALBUM_PRE.toString());
		assertThat(comboboxContents).contains(ALBUM_PRE.toString());
		window.label(JLabelMatcher.withText(ALBUM_PRE.getName() + " volume " + ALBUM_PRE.getVolume() + " located in " + 
				ALBUM_PRE.getLocation() + " with " + ALBUM_PRE.getOccupiedSlots() + "/" + ALBUM_PRE.getNumberOfSlots() + " slots occupied"));
	}
	
	/////////////// Test view methods implementation related to coins
	@Test @GUITest
	public void testShowAllCoinsShouldUpdateListLabelSearchingAndAlbumSelection() {
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
		});
		window.list("albumList").selectItem(ALBUM_PRE.toString());
		
		List<Coin> list = Arrays.asList(COIN_COMM_1, COIN_COMM_2, COIN_PRE);
		
		view.showAllCoins(list);
		
		String[] listContents = window.list("coinList").contents();
		
		assertThat(listContents).containsExactlyInAnyOrder(COIN_COMM_1.toString(), COIN_COMM_2.toString(), COIN_PRE.toString());
		window.list("coinList").requireNoSelection();
		assertThat(window.label(JLabelMatcher.withName("coinActual")).text()).isEqualTo("All coins:");
		window.textBox("coinFilterDescription").requireEmpty();
		window.button(JButtonMatcher.withText("Filter")).requireDisabled();
		window.list("albumList").requireNoSelection();
	}
	
	@Test @GUITest
	public void testShowSearchedCoinsShouldShowThemInListAndModifyTheLabel() {
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
		});
		window.list("albumList").selectItem(ALBUM_PRE.toString());
		
		String SEARCHING_KEY = "2€";
		
		List<Coin> coins = Arrays.asList(COIN_COMM_1, COIN_COMM_2);
		
		view.showSearchedCoins(coins, SEARCHING_KEY);
		
		String[] listContents = window.list("coinList").contents();
		
		assertThat(listContents).containsExactlyInAnyOrder(COIN_COMM_1.toString(), COIN_COMM_2.toString());
		assertThat(window.label(JLabelMatcher.withName("coinActual")).text())
			.isEqualTo("Results for \"" + SEARCHING_KEY + "\":");
		window.list("albumList").requireNoSelection();
	}
	
	@Test @GUITest
	public void testShowCoinInAlbumShouldShowThemInListAndModifyTheLabel() {
		List<Coin> coins = Arrays.asList(COIN_COMM_1, COIN_COMM_2);
		
		view.showCoinsInAlbum(coins, ALBUM_COMM_1);
		
		String[] listContents = window.list("coinList").contents();
		
		assertThat(listContents).containsExactlyInAnyOrder(COIN_COMM_1.toString(), COIN_COMM_2.toString());
		assertThat(window.label(JLabelMatcher.withName("coinActual")).text())
			.isEqualTo("Coins in " + ALBUM_COMM_1.getName() + " vol." + ALBUM_COMM_1.getVolume() + ":");
	}
	
	@Test @GUITest
	public void testShowCoinShouldShowTheAlbumDetailsAndEnableButtons() {
		view.showCoin(COIN_PRE, ALBUM_PRE);
		
		window.label(JLabelMatcher.withName("coinSelection").andText(COIN_PRE.getDescription() + " of " + COIN_PRE.getMintingYear() + 
				" from " + COIN_PRE.getCountry() + " located in " + ALBUM_PRE.getName() + " vol." + ALBUM_PRE.getVolume() + 
				" (Grade: " + COIN_PRE.getGrade().getMeaning() + ") [note: " + COIN_PRE.getNote() + "]"));
		window.button(JButtonMatcher.withText("Delete coin")).requireEnabled();
		window.button(JButtonMatcher.withText("Move coin")).requireEnabled();
	}
	
	@Test @GUITest
	public void testCoinAddedShouldAddItToTheListAndClearTheForm() {
		view.coinAdded(COIN_PRE);
		
		String[] listContents = window.list("coinList").contents();
		
		assertThat(listContents).contains(COIN_PRE.toString());
		window.textBox("coinFormDescription").requireEmpty();
		window.comboBox("coinFormGrade").requireNoSelection();
		window.textBox("coinFormCountry").requireEmpty();
		window.textBox("coinFormYear").requireEmpty();
		window.comboBox("coinFormAlbum").requireNoSelection();
		window.textBox("coinFormNote").requireEmpty();
		window.button(JButtonMatcher.withText("Save coin")).requireDisabled();
	}
	
	@Test @GUITest
	public void testCoinDeletedShouldRemoveFromList() {
		GuiActionRunner.execute(() -> view.getCoinListModel().addElement(COIN_PRE));
		
		view.coinDeleted(COIN_PRE);
		
		String[] listContents = window.list("coinList").contents();	
		
		assertThat(listContents).doesNotContain(COIN_PRE.toString());
	}
	
	@Test @GUITest
	public void testMovedCoinShouldUpdateListLabel() {
		GuiActionRunner.execute(() -> view.getCoinListModel().addElement(COIN_COMM_1));
		
		view.coinMoved(COIN_COMM_1, ALBUM_COMM_1, ALBUM_COMM_2);

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).contains(COIN_COMM_1.toString());
		window.label(JLabelMatcher.withName("coinSelection").andText(COIN_COMM_1.getDescription() + " of " + 
				COIN_COMM_1.getMintingYear() + " from " + COIN_COMM_1.getCountry() + " located in " + ALBUM_COMM_2.getName() + " vol." + 
				ALBUM_COMM_2.getVolume() + " (Grade: " + COIN_COMM_1.getGrade().getMeaning() + ") [note: " + COIN_COMM_1.getNote() + "]"));
	}
	
	/////////////// Test GUI logic related to albums
	@Test @GUITest
	public void testAlbumFormButtonEnablerShouldEnableWhenFieldsAreCompiledWithCorrectFormat() {
		JTextComponentFixture name = window.textBox("albumFormName");
		JTextComponentFixture volume = window.textBox("albumFormVolume");
		JTextComponentFixture location = window.textBox("albumFormLocation");
		JTextComponentFixture slots = window.textBox("albumFormSlots");
		
		name.setText("").enterText("Pre-euro");
		volume.setText("").enterText("1");
		location.setText("").enterText("armadio");
		slots.setText("").enterText("50");
		window.button(JButtonMatcher.withText("Save album")).requireEnabled();
	}
	
	@Test @GUITest
	public void testAlbumFormButtonEnablerShouldDisableWhenFieldsAreNotCompiled() {
		JTextComponentFixture name = window.textBox("albumFormName");
		JTextComponentFixture volume = window.textBox("albumFormVolume");
		JTextComponentFixture location = window.textBox("albumFormLocation");
		JTextComponentFixture slots = window.textBox("albumFormSlots");

		name.setText("").enterText("");
		volume.setText("").enterText("");
		location.setText("").enterText("");
		slots.setText("").enterText("");
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
		
		name.setText("").enterText("Pre-euro");
		volume.setText("").enterText("");
		location.setText("").enterText("");
		slots.setText("").enterText("");
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
		
		name.setText("").enterText("");
		volume.setText("").enterText("1");
		location.setText("").enterText("");
		slots.setText("").enterText("");
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
		
		name.setText("").enterText("");
		volume.setText("").enterText("");
		location.setText("").enterText("armadio");
		slots.setText("").enterText("");
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
		
		name.setText("").enterText("");
		volume.setText("").enterText("");
		location.setText("").enterText("");
		slots.setText("").enterText("50");
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
	}
	
	@Test @GUITest
	public void testAlbumFormButtonEnablerShouldDisableWhenFieldsAreCompiledWithIncorrectFormat() {
		JTextComponentFixture name = window.textBox("albumFormName");
		JTextComponentFixture volume = window.textBox("albumFormVolume");
		JTextComponentFixture location = window.textBox("albumFormLocation");
		JTextComponentFixture slots = window.textBox("albumFormSlots");

		name.setText("").enterText(" ");
		volume.setText("").enterText("1");
		location.setText("").enterText("armadio");
		slots.setText("").enterText("50");
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
		
		name.setText("").enterText("Pre-euro");
		volume.setText("").enterText("NaN");
		location.setText("").enterText("armadio");
		slots.setText("").enterText("50");
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
		
		name.setText("").enterText("Pre-euro");
		volume.setText("").enterText("1");
		location.setText("").enterText(" ");
		slots.setText("").enterText("50");
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
		
		name.setText("").enterText("Pre-euro");
		volume.setText("").enterText("1");
		location.setText("").enterText("armadio");
		slots.setText("").enterText("NaN");
		window.button(JButtonMatcher.withText("Save album")).requireDisabled();
	}

	@Test @GUITest
	public void testAddAlbumButtonShouldCallPresenter() {
		window.textBox("albumFormName").setText("").enterText("Pre-euro");
		window.textBox("albumFormVolume").setText("").enterText("1");
		window.textBox("albumFormLocation").setText("").enterText("armadio");
		window.textBox("albumFormSlots").setText("").enterText("50");

		window.button(JButtonMatcher.withText("Save album")).click();
		
		verify(albumPresenter, Mockito.timeout(TIMEOUT)).addAlbum(new Album("Pre-euro", 1, "armadio", 50, 0));
	}
	
	@Test @GUITest
	public void testAlbumSearchButtonEnablerShouldEnableWhenFieldsAreCompiledWithCorrectFormat() {
		JTextComponentFixture name = window.textBox("albumSearchName");
		JTextComponentFixture volume = window.textBox("albumSearchVolume");
		
		name.setText("").enterText("Pre-euro");
		volume.setText("").enterText("1");
		window.button(JButtonMatcher.withText("Search")).requireEnabled();
	}
	
	@Test @GUITest
	public void testAlbumSearchButtonEnablerShouldDisableWhenFieldsAreNotCompiled() {
		JTextComponentFixture name = window.textBox("albumSearchName");
		JTextComponentFixture volume = window.textBox("albumSearchVolume");
		
		name.setText("").enterText("Pre-euro");
		volume.setText("").enterText("");
		window.button(JButtonMatcher.withText("Search")).requireDisabled();
		
		name.setText("").enterText("");
		volume.setText("").enterText("1");
		window.button(JButtonMatcher.withText("Search")).requireDisabled();
	}
	
	@Test @GUITest
	public void testAlbumSearchButtonEnablerShouldDisableWhenFieldsAreCompiledWithIncorrectFormat() {
		JTextComponentFixture name = window.textBox("albumSearchName");
		JTextComponentFixture volume = window.textBox("albumSearchVolume");
		
		name.setText("").enterText(" ");
		volume.setText("").enterText("1");
		window.button(JButtonMatcher.withText("Search")).requireDisabled();
		
		name.setText("").enterText("Pre-euro");
		volume.setText("").enterText("NaN");
		window.button(JButtonMatcher.withText("Search")).requireDisabled();
	}

	@Test @GUITest
	public void testSearchButtonShouldCallPresenter() {
		String NAME = "KEY NAME";
		String VOLUME = "1";
		
		window.textBox("albumSearchName").setText("").enterText(NAME);
		window.textBox("albumSearchVolume").setText("").enterText(VOLUME);
		
		window.button(JButtonMatcher.withText("Search")).click();
		
		verify(albumPresenter, Mockito.timeout(TIMEOUT)).searchAlbum(NAME, Integer.valueOf(VOLUME));
	}

	@Test @GUITest
	public void testAllAlbumsButtonShouldCallPresenter() {
		window.button(JButtonMatcher.withText("All albums")).click();
		
		// first call in phase of configuration
		verify(albumPresenter, Mockito.timeout(TIMEOUT).times(2)).getAllAlbums();
	}
	
	@Test @GUITest
	public void testSelectAlbumShouldCallPresenters() {
		Album spiedAlbum = spy(ALBUM_PRE);
		when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_PRE);
		
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(spiedAlbum);
		});
		
		window.list("albumList").selectItem(0);
		
		verify(albumPresenter, Mockito.timeout(TIMEOUT)).getAlbum(UUID_ALBUM_PRE);
		verify(coinPresenter, Mockito.timeout(TIMEOUT)).getCoinsByAlbum(spiedAlbum);
	}
	
	@Test @GUITest
	public void testDeselectAlbumShouldClearLabelAndDisableButtons() {
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
		});
		
		window.list("albumList").selectItem(0);
		window.list("albumList").clearSelection();
		
		window.label(JLabelMatcher.withName("albumSelection").andText(" "));
		window.button(JButtonMatcher.withText("Delete album")).requireDisabled();
		window.button(JButtonMatcher.withText("Move album")).requireDisabled();
	}
	
	@Test @GUITest
	public void testDeleteAlbumButtonShouldCallPresenters() {
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
			view.getAlbumDeleteButton().setEnabled(true);
		});
		
		window.list("albumList").item(ALBUM_PRE.toString()).select();
		window.button(JButtonMatcher.withText("Delete album")).click();
		
		verify(albumPresenter, Mockito.timeout(TIMEOUT)).deleteAlbum(ALBUM_PRE);
		// first call in phase of configuration
		verify(coinPresenter, Mockito.timeout(TIMEOUT).times(2)).getAllCoins();
	}
	
	@Test @GUITest
	public void testMoveAlbumButtonShouldShowDialogAndCallPresenterIfNotBlank() {
		String NEW_LOCATION = "Some location";
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
			view.getAlbumMoveButton().setEnabled(true);
		});
		
		window.list("albumList").item(ALBUM_PRE.toString()).select();
		window.button(JButtonMatcher.withText("Move album")).click();
		
		window.dialog().label(JLabelMatcher.withText("New location:"));
		window.dialog().textBox().setText(NEW_LOCATION);
		window.dialog().button(JButtonMatcher.withText("OK")).click();
		
		verify(albumPresenter, Mockito.timeout(TIMEOUT)).moveAlbum(ALBUM_PRE, NEW_LOCATION);
	}
	
	@Test @GUITest
	public void testMoveAlbumButtonShouldShowDialogAndNotCallPresenterIfBlank() {
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
			view.getAlbumMoveButton().setEnabled(true);
		});
		
		window.list("albumList").item(ALBUM_PRE.toString()).select();
		window.button(JButtonMatcher.withText("Move album")).click();
		
		window.dialog().label(JLabelMatcher.withText("New location:"));
		window.dialog().button(JButtonMatcher.withText("OK")).click();

		verify(albumPresenter, never()).moveAlbum(any(), any());
	}
	
	@Test @GUITest
	public void testMoveAlbumButtonShouldShowDialogAndNotCallPresenterIfCancel() {
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
			view.getAlbumMoveButton().setEnabled(true);
		});
		
		window.list("albumList").item(ALBUM_PRE.toString()).select();
		window.button(JButtonMatcher.withText("Move album")).click();
		
		window.dialog().label(JLabelMatcher.withText("New location:"));
		window.dialog().button(JButtonMatcher.withText("Cancel")).click();

		verify(albumPresenter, never()).moveAlbum(any(), any());
	}

	/////////////// Test GUI logic related to coins
	@Test @GUITest
	public void testCoinFormButtonEnablerShouldEnableWhenFieldsAreCompiledWithCorrectFormat() {
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});
		
		JTextComponentFixture description = window.textBox("coinFormDescription");
		JTextComponentFixture country = window.textBox("coinFormCountry");
		JComboBoxFixture album = window.comboBox("coinFormAlbum");
		JComboBoxFixture grade = window.comboBox("coinFormGrade");
		JTextComponentFixture year = window.textBox("coinFormYear");
		
		description.setText("").enterText("2€");
		country.setText("").enterText("IT");
		album.clearSelection().selectItem(ALBUM_PRE.toString());
		grade.clearSelection().selectItem("AG");
		year.setText("").enterText("2004");
		window.button(JButtonMatcher.withText("Save coin")).requireEnabled();
	}
	
	@Test @GUITest
	public void testCoinFormButtonEnablerShouldDisableWhenFieldsAreNotCompiled() {
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});
		
		JTextComponentFixture description = window.textBox("coinFormDescription");
		JTextComponentFixture country = window.textBox("coinFormCountry");
		JComboBoxFixture album = window.comboBox("coinFormAlbum");
		JComboBoxFixture grade = window.comboBox("coinFormGrade");
		JTextComponentFixture year = window.textBox("coinFormYear");
		
		description.setText("").enterText(" ");
		country.setText("").enterText("IT");
		album.clearSelection().selectItem(ALBUM_PRE.toString());
		grade.clearSelection().selectItem("AG");
		year.setText("").enterText("2004");
		window.button(JButtonMatcher.withText("Save coin")).requireDisabled();
		
		description.setText("").enterText("2€");
		country.setText("").enterText("");
		album.clearSelection().selectItem(ALBUM_PRE.toString());
		grade.clearSelection().selectItem("AG");
		year.setText("").enterText("2004");
		window.button(JButtonMatcher.withText("Save coin")).requireDisabled();
		
		description.setText("").enterText("2€");
		country.setText("").enterText("IT");
		album.clearSelection();
		grade.clearSelection().selectItem("AG");
		year.setText("").enterText("2004");
		window.button(JButtonMatcher.withText("Save coin")).requireDisabled();
		
		description.setText("").enterText("2€");
		country.setText("").enterText("IT");
		album.clearSelection().selectItem(ALBUM_PRE.toString());
		grade.clearSelection();
		year.setText("").enterText("2004");
		window.button(JButtonMatcher.withText("Save coin")).requireDisabled();
		
		description.setText("").enterText("2€");
		country.setText("").enterText("IT");
		album.clearSelection().selectItem(ALBUM_PRE.toString());
		grade.clearSelection().selectItem("AG");
		year.setText("").enterText(" ");
		window.button(JButtonMatcher.withText("Save coin")).requireDisabled();
	}
	
	@Test @GUITest
	public void testCoinFormButtonEnablerShouldDisableWhenFieldsAreCompiledWithIncorrectFormat() {
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});
		
		JTextComponentFixture description = window.textBox("coinFormDescription");
		JTextComponentFixture country = window.textBox("coinFormCountry");
		JComboBoxFixture album = window.comboBox("coinFormAlbum");
		JComboBoxFixture grade = window.comboBox("coinFormGrade");
		JTextComponentFixture year = window.textBox("coinFormYear");
		
		description.setText("").enterText("2€");
		country.setText("").enterText("IT");
		album.clearSelection().selectItem(ALBUM_PRE.toString());
		grade.clearSelection().selectItem("AG");
		year.setText("").enterText("NaN");
		window.button(JButtonMatcher.withText("Save coin")).requireDisabled();
	}
	
	@Test @GUITest
	public void testAddCoinButtonShouldCallPresenter() {
		Album spiedAlbum = spy(ALBUM_COMM_1);
		when(spiedAlbum.getId()).thenReturn(UUID_ALBUM_COMM_1);
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(spiedAlbum);
		});
		
		window.textBox("coinFormDescription").setText("").enterText("2€");
		window.textBox("coinFormCountry").setText("").enterText("IT");
		window.comboBox("coinFormAlbum").clearSelection().selectItem(spiedAlbum.toString());
		window.comboBox("coinFormGrade").clearSelection().selectItem("AG");
		window.textBox("coinFormYear").setText("").enterText("2004");
		window.textBox("coinFormNote").setText("").enterText("Note");

		window.button(JButtonMatcher.withText("Save coin")).click();
		
		verify(coinPresenter, Mockito.timeout(TIMEOUT)).addCoin(new Coin(Grade.AG, "IT", Year.of(2004), "2€", "Note", UUID_ALBUM_COMM_1));
	}
	
	@Test @GUITest
	public void testCoinFilterButtonEnablerShouldEnableWhenFieldAreCompiledWithCorrectFormat() {
		JTextComponentFixture description = window.textBox("coinFilterDescription");
		
		description.setText("").enterText("FILTER KEY");
		window.button(JButtonMatcher.withText("Filter")).requireEnabled();
	}
	
	@Test @GUITest
	public void testCoinFilterButtonEnablerShouldDisableWhenFieldAreNotCompiled() {
		JTextComponentFixture description = window.textBox("coinFilterDescription");
		
		description.setText("").enterText("");
		window.button(JButtonMatcher.withText("Filter")).requireDisabled();
	}
	
	@Test @GUITest
	public void testCoinFilterButtonEnablerShouldEnableWhenFieldAreCompiledWithInorrectFormat() {
		JTextComponentFixture description = window.textBox("coinFilterDescription");
		
		description.setText("").enterText(" ");
		window.button(JButtonMatcher.withText("Filter")).requireDisabled();
	}
	
	@Test @GUITest
	public void testFilterButtonShouldCallPresenter() {
		String DESCRIPTION = "KEY DESCRIPTION";
		
		window.textBox("coinFilterDescription").setText("").enterText(DESCRIPTION);
		
		window.button(JButtonMatcher.withText("Filter")).click();
		
		verify(coinPresenter, Mockito.timeout(TIMEOUT)).searchCoins(DESCRIPTION);
	}
	
	@Test @GUITest
	public void testAllCoinsButtonShouldCallPresenter() {
		window.button(JButtonMatcher.withText("All coins")).click();
		
		// first call in phase of configuration
		verify(coinPresenter, Mockito.timeout(TIMEOUT).times(2)).getAllCoins();
	}
	
	@Test @GUITest
	public void testSelectCoinShouldCallPresenter() {
		Coin spiedCoin = spy(COIN_PRE);
		when(spiedCoin.getId()).thenReturn(UUID_COIN_PRE);
		
		GuiActionRunner.execute(() -> {
			view.getCoinListModel().addElement(spiedCoin);
		});
		
		window.list("coinList").selectItem(0);
		
		verify(coinPresenter, Mockito.timeout(TIMEOUT)).getCoin(UUID_COIN_PRE);
	}
	
	@Test @GUITest
	public void testDeselectCoinShouldClearLabelAndDisableButtons() {
		GuiActionRunner.execute(() -> {
			view.getCoinListModel().addElement(COIN_PRE);
		});
		
		window.list("coinList").selectItem(0);
		window.list("coinList").clearSelection();
		
		window.label(JLabelMatcher.withName("coinSelection").andText(" "));
		window.button(JButtonMatcher.withText("Delete coin")).requireDisabled();
		window.button(JButtonMatcher.withText("Move coin")).requireDisabled();
	}
	
	@Test @GUITest
	public void testDeleteCoinButtonShouldCallPresenter() {
		GuiActionRunner.execute(() -> {
			view.getCoinListModel().addElement(COIN_PRE);
			view.getCoinDeleteButton().setEnabled(true);
		});
		
		window.list("coinList").item(COIN_PRE.toString()).select();
		window.button(JButtonMatcher.withText("Delete coin")).click();
		
		verify(coinPresenter, Mockito.timeout(TIMEOUT)).deleteCoin(COIN_PRE);
	}
	
	@Test @GUITest
	public void testMoveCoinButtonShouldShowDialogAndCallPresenterIfNotBlank() {
		GuiActionRunner.execute(() -> {
			view.getCoinListModel().addElement(COIN_COMM_1);
			view.getCoinFormAlbumModel().addElement(ALBUM_COMM_2);
			view.getCoinFormAlbumModel().addElement(ALBUM_COMM_1);
			view.getCoinMoveButton().setEnabled(true);
		});
		
		window.list("coinList").item(COIN_COMM_1.toString()).select();
		window.button(JButtonMatcher.withText("Move coin")).click();
		
		window.dialog().label(JLabelMatcher.withText("New album:"));
		assertThat(window.dialog().comboBox().selectedItem()).isNull();
		window.dialog().comboBox().selectItem(ALBUM_COMM_2.toString());
		window.dialog().button(JButtonMatcher.withText("OK")).click();
		
		verify(coinPresenter, Mockito.timeout(TIMEOUT)).moveCoin(COIN_COMM_1, ALBUM_COMM_2);
	}
	
	@Test @GUITest
	public void testMoveCoinButtonShouldShowDialogAndNotCallPresenterIfBlank() {
		GuiActionRunner.execute(() -> {
			view.getCoinListModel().addElement(COIN_COMM_1);
			view.getCoinFormAlbumModel().addElement(ALBUM_COMM_2);
			view.getCoinFormAlbumModel().addElement(ALBUM_COMM_1);
			view.getCoinMoveButton().setEnabled(true);
		});
		
		window.list("coinList").item(COIN_COMM_1.toString()).select();
		window.button(JButtonMatcher.withText("Move coin")).click();
		
		window.dialog().label(JLabelMatcher.withText("New album:"));
		window.dialog().comboBox().selectItem(0);
		window.dialog().button(JButtonMatcher.withText("OK")).click();
		
		verify(coinPresenter, never()).moveCoin(any(), any());
	}
	
	@Test @GUITest
	public void testMoveCoinButtonShouldShowDialogAndNotCallPresenterIfCancel() {
		GuiActionRunner.execute(() -> {
			view.getCoinListModel().addElement(COIN_COMM_1);
			view.getCoinFormAlbumModel().addElement(ALBUM_COMM_2);
			view.getCoinFormAlbumModel().addElement(ALBUM_COMM_1);
			view.getCoinMoveButton().setEnabled(true);
		});
		
		window.list("coinList").item(COIN_COMM_1.toString()).select();
		window.button(JButtonMatcher.withText("Move coin")).click();
		
		window.dialog().label(JLabelMatcher.withText("New album:"));
		window.dialog().button(JButtonMatcher.withText("Cancel")).click();

		verify(coinPresenter, never()).moveCoin(any(), any());
	}
	
	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}
}
