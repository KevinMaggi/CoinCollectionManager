package io.github.kevinmaggi.coin_collection_manager.ui.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import java.time.Year;
import java.util.regex.Pattern;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.kevinmaggi.coin_collection_manager.business.service.AlbumManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.CoinManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.transactional.AlbumTransactionalManager;
import io.github.kevinmaggi.coin_collection_manager.business.service.transactional.CoinTransactionalManager;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.TransactionManagerFactory;
import io.github.kevinmaggi.coin_collection_manager.business.transaction.manager.postgresql.PostgresTransactionManagerFactory;
import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.AlbumPresenter;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.CoinPresenter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@RunWith(GUITestRunner.class)
public class SwingViewWithPresentersIT extends AssertJSwingJUnitTestCase {
	// Test variables
	private Album ALBUM_PRE, ALBUM_COMM_1, ALBUM_COMM_2;
	private Coin COIN_COMM_1, COIN_COMM_2, COIN_PRE;

	// Tests
	private static final int TIMEOUT = 5000;

	private static EntityManagerFactory emf;
	private EntityManager em;
	private TransactionManagerFactory factory;

	private FrameFixture window;

	private CoinPresenter coinPresenter;
	private AlbumPresenter albumPresenter;

	private SwingView view;

	@BeforeClass
	public static void setUpTestCase() {
		System.setProperty("db.port", System.getProperty("postgres.port", "5432"));
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}

	@Override
	protected void onSetUp() {
		em = emf.createEntityManager();
		factory = new PostgresTransactionManagerFactory(em);

		CoinManager coinManager = new CoinTransactionalManager(factory.getTransactionManager());
		AlbumManager albumManager = new AlbumTransactionalManager(factory.getTransactionManager());

		// Ensure to start every test with an empty database
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE albums").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE coins").executeUpdate();
		em.getTransaction().commit();

		// Run view
		GuiActionRunner.execute(() -> {
			view = new SwingView();
			coinPresenter = new CoinPresenter(view, coinManager, albumManager);
			albumPresenter = new AlbumPresenter(view, albumManager);

			view.setPresenters(coinPresenter, albumPresenter);
			return view;
		});
		window = new FrameFixture(robot(), view);
		window.show();
	}

	@Test @GUITest
	public void testAllAlbumsButtonUpdatesTheListAndTheDropdownThroughPresenter() {
		initAlbums();
		persistAlbums();

		window.button(JButtonMatcher.withText("All albums")).click();

		pause(new Condition("List contain something") {
			@Override
			public boolean test() {
				return window.list("albumList").contents().length != 0;
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();

		assertThat(listContents).containsExactlyInAnyOrder(ALBUM_COMM_1.toString(), ALBUM_COMM_2.toString(), ALBUM_PRE.toString());
		assertThat(comboboxContents).containsExactlyInAnyOrder(ALBUM_COMM_1.toString(), ALBUM_COMM_2.toString(), ALBUM_PRE.toString());
	}

	@Test @GUITest
	public void testSearchAlbumButtonUpdatesTheListThroughPresenterWhenSuccess() {
		initAlbums();
		persistAlbums();

		window.textBox("albumSearchName").setText("").enterText(ALBUM_PRE.getName());
		window.textBox("albumSearchVolume").setText("").enterText(String.valueOf(ALBUM_PRE.getVolume()));
		window.button(JButtonMatcher.withText("Search")).click();

		pause(new Condition("List contain something") {
			@Override
			public boolean test() {
				return window.list("albumList").contents().length != 0;
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("albumList").contents();

		assertThat(listContents).containsOnly(ALBUM_PRE.toString());
	}

	@Test @GUITest
	public void testSearchAlbumButtonUpdatesTheListThroughPresenterWhenFail() {
		initAlbums();
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
		});

		window.textBox("albumSearchName").setText("").enterText(ALBUM_PRE.getName());
		window.textBox("albumSearchVolume").setText("").enterText(String.valueOf(ALBUM_PRE.getVolume()));
		window.button(JButtonMatcher.withText("Search")).click();

		pause(new Condition("List is empty") {
			@Override
			public boolean test() {
				return window.list("albumList").contents().length == 0;
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("albumList").contents();

		window.label(JLabelMatcher.withName("status").andText("This album doesn't exist"));
		assertThat(listContents).isEmpty();
	}

	@Test @GUITest
	public void testSelectAlbumUpdatesTheLabelAndTheCoinListThroughPresenterWhenSuccess() {
		populateDB();
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
		});
		window.list("albumList").selectItems(ALBUM_PRE.toString());

		pause(new Condition("Label is not empty") {
			@Override
			public boolean test() {
				return !window.label("albumSelection").text().trim().isEmpty();
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(window.label(JLabelMatcher.withName("albumSelection")).text())
			.contains(ALBUM_PRE.getName())
			.contains(String.valueOf(ALBUM_PRE.getVolume()));
		assertThat(listContents).containsOnly(COIN_PRE.toString());
	}

	@Test @GUITest
	public void testSelectAlbumUpdatesTheListThroughPresenterWhenFail() {
		initAlbums();
		persistAlbums();
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
		});
		em.getTransaction().begin();
		em.remove(ALBUM_PRE);
		em.getTransaction().commit();

		window.list("albumList").selectItems(ALBUM_PRE.toString());

		pause(new Condition("Label is not empty") {
			@Override
			public boolean test() {
				return !window.label("status").text().trim().isEmpty();
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("albumList").contents();

		window.label(JLabelMatcher.withName("status").andText(Pattern.compile(".*his album doesn't exist")));
		assertThat(listContents).containsOnly(ALBUM_COMM_1.toString(), ALBUM_COMM_2.toString());
	}

	@Test @GUITest
	public void testAddAlbumButtonAddsToTheListAndDropdownThroughPresenterWhenSuccess() {
		initAlbums();

		window.textBox("albumFormName").setText("").enterText(ALBUM_PRE.getName());
		window.textBox("albumFormVolume").setText("").enterText(String.valueOf(ALBUM_PRE.getVolume()));
		window.textBox("albumFormLocation").setText("").enterText(ALBUM_PRE.getLocation());
		window.textBox("albumFormSlots").setText("").enterText(String.valueOf(ALBUM_PRE.getNumberOfSlots()));

		window.button(JButtonMatcher.withText("Save album")).click();

		pause(new Condition("List contain something") {
			@Override
			public boolean test() {
				return window.list("albumList").contents().length != 0;
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();

		assertThat(listContents).contains(ALBUM_PRE.toString());
		assertThat(comboboxContents).contains(ALBUM_PRE.toString());
	}

	@Test @GUITest
	public void testAddAlbumButtonUpdatesTheListAndDropdownThroughPresenterWhenFail() {
		initAlbums();
		persistAlbums();

		window.textBox("albumFormName").setText("").enterText(ALBUM_PRE.getName());
		window.textBox("albumFormVolume").setText("").enterText(String.valueOf(ALBUM_PRE.getVolume()));
		window.textBox("albumFormLocation").setText("").enterText(ALBUM_PRE.getLocation());
		window.textBox("albumFormSlots").setText("").enterText(String.valueOf(ALBUM_PRE.getNumberOfSlots()));

		window.button(JButtonMatcher.withText("Save album")).click();

		pause(new Condition("Label is not empty") {
			@Override
			public boolean test() {
				return !window.label("status").text().trim().isEmpty();
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();

		window.label(JLabelMatcher.withName("status").andText("This album already exists"));
		assertThat(listContents).contains(ALBUM_PRE.toString());
		assertThat(comboboxContents).contains(ALBUM_PRE.toString());
	}

	@Test @GUITest
	public void testDeleteAlbumButtonRemovesFromTheListAndTheDropdownThroughPresenterWhenSuccess() {
		initAlbums();
		persistAlbums();
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});

		window.list("albumList").selectItem(ALBUM_PRE.toString());
		window.button(JButtonMatcher.withText("Delete album")).click();

		pause(new Condition("List is empty") {
			@Override
			public boolean test() {
				return window.list("albumList").contents().length == 0;
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();

		assertThat(listContents).doesNotContain(ALBUM_PRE.toString());
		assertThat(comboboxContents).doesNotContain(ALBUM_PRE.toString());
	}

	@Test @GUITest
	public void testDeleteAlbumButtonRemovesFromTheListAndTheDropdownThroughPresenterWhenFail() {
		initAlbums();
		persistAlbums();
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});
		window.list("albumList").selectItem(ALBUM_PRE.toString());

		pause(new Condition("Label react to selection") {
			@Override
			public boolean test() {
				// Is necessary to wait the perform of getCoinsByAlbum in order to allow it to release transaction
				return !window.label("coinActual").text().contains("All coins");
			}
		}, timeout(TIMEOUT));

		em.getTransaction().begin();
		em.remove(ALBUM_PRE);
		em.getTransaction().commit();

		window.button(JButtonMatcher.withText("Delete album")).click();

		pause(new Condition("Label shows error message relative to deletion") {
			@Override
			public boolean test() {
				return window.label("status").text().trim() == "This album doesn't exist";
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();

		window.label(JLabelMatcher.withName("status").andText("This album doesn't exist"));
		assertThat(listContents).doesNotContain(ALBUM_PRE.toString());
		assertThat(comboboxContents).doesNotContain(ALBUM_PRE.toString());
	}

	@Test @GUITest
	public void testMoveAlbumButtonUpdatesTheListAndTheDropdownThroughPresenterWhenSuccess() {
		initAlbums();
		persistAlbums();
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});

		window.list("albumList").selectItem(ALBUM_PRE.toString());
		window.button(JButtonMatcher.withText("Move album")).click();
		window.dialog().textBox().enterText("New location");
		window.dialog().button(JButtonMatcher.withText("OK")).click();

		pause(new Condition("List contain updated element") {
			@Override
			public boolean test() {
				return window.label("albumSelection").text().contains("New location");
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();

		assertThat(listContents).contains(ALBUM_PRE.toString());
		assertThat(comboboxContents).contains(ALBUM_PRE.toString());
		assertThat(ALBUM_PRE.getLocation()).isEqualTo("New location");
	}

	@Test @GUITest
	public void testMoveAlbumButtonUpdatesTheListAndTheDropdownThroughPresenterWhenFail() {
		initAlbums();
		persistAlbums();
		GuiActionRunner.execute(() -> {
			view.getAlbumListModel().addElement(ALBUM_PRE);
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});

		window.list("albumList").selectItem(ALBUM_PRE.toString());

		pause(new Condition("Label react to selection") {
			@Override
			public boolean test() {
				return !window.label("albumSelection").text().trim().isEmpty();
			}
		}, timeout(TIMEOUT));

		em.getTransaction().begin();
		em.remove(ALBUM_PRE);
		em.getTransaction().commit();

		window.button(JButtonMatcher.withText("Move album")).click();
		window.dialog().textBox().enterText("New location");
		window.dialog().button(JButtonMatcher.withText("OK")).click();

		pause(new Condition("Label shows error message relative to movement") {
			@Override
			public boolean test() {
				return window.label("status").text().trim() == "This album doesn't exist";
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();

		window.label(JLabelMatcher.withName("status").andText("This album doesn't exist"));
		assertThat(listContents).doesNotContain(ALBUM_PRE.toString());
		assertThat(comboboxContents).doesNotContain(ALBUM_PRE.toString());
		assertThat(ALBUM_PRE.getLocation()).isEqualTo("Armadio");
	}

	@Test @GUITest
	public void testAllCoinsButtonUpdatesTheListThroughPresenter() {
		populateDB();

		window.button(JButtonMatcher.withText("All coins")).click();

		pause(new Condition("List contain something") {
			@Override
			public boolean test() {
				return window.list("coinList").contents().length != 0;
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).containsExactlyInAnyOrder(COIN_COMM_1.toString(), COIN_COMM_2.toString(), COIN_PRE.toString());
	}

	@Test @GUITest
	public void testFilterCoinsButtonUpdateTheListThroughPresenter() {
		populateDB();

		window.textBox("coinFilterDescription").setText("").enterText(COIN_PRE.getDescription());
		window.button(JButtonMatcher.withText("Filter")).click();

		pause(new Condition("List contain something") {
			@Override
			public boolean test() {
				return window.list("coinList").contents().length != 0;
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).containsOnly(COIN_PRE.toString());
	}

	@Test @GUITest
	public void testSelectCoinUpdatesTheLabelThroughPresenterWhenSuccess() {
		populateDB();
		GuiActionRunner.execute(() -> {
			view.getCoinListModel().addElement(COIN_PRE);
		});

		window.list("coinList").selectItems(COIN_PRE.toString());

		pause(new Condition("Label is not empty") {
			@Override
			public boolean test() {
				return !window.label("coinSelection").text().trim().isEmpty();
			}
		}, timeout(TIMEOUT));

		assertThat(window.label(JLabelMatcher.withName("coinSelection")).text())
			.contains(COIN_PRE.getDescription())
			.contains(COIN_PRE.getCountry())
			.contains(COIN_PRE.getGrade().getMeaning())
			.contains(COIN_PRE.getMintingYear().toString())
			.contains(COIN_PRE.getNote());
	}

	@Test @GUITest
	public void testSelectCoinUpdatesTheListThroughPresenterWhenFail() {
		populateDB();
		GuiActionRunner.execute(() -> {
			view.getCoinListModel().addElement(COIN_PRE);
		});
		em.getTransaction().begin();
		em.remove(COIN_PRE);
		em.getTransaction().commit();

		window.list("coinList").selectItems(COIN_PRE.toString());

		pause(new Condition("Label is not empty") {
			@Override
			public boolean test() {
				return !window.label("status").text().trim().isEmpty();
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		window.label(JLabelMatcher.withName("status").andText("This coin doesn't exist"));
		assertThat(listContents).contains(COIN_COMM_1.toString(), COIN_COMM_2.toString());
	}

	@Test @GUITest
	public void testAddCoinButtonAddsToTheListThroughPresenterWhenSuccess() {
		initAlbums();
		persistAlbums();
		initCoins();
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});

		window.textBox("coinFormDescription").setText("").enterText(COIN_PRE.getDescription());
		window.textBox("coinFormCountry").setText("").enterText(COIN_PRE.getCountry());
		window.comboBox("coinFormAlbum").clearSelection().selectItem(ALBUM_PRE.toString());
		window.comboBox("coinFormGrade").clearSelection().selectItem(COIN_PRE.getGrade().toString());
		window.textBox("coinFormYear").setText("").enterText(COIN_PRE.getMintingYear().toString());
		window.textBox("coinFormNote").setText("").enterText(COIN_PRE.getNote());

		window.button(JButtonMatcher.withText("Save coin")).click();

		pause(new Condition("List contain something") {
			@Override
			public boolean test() {
				return window.list("coinList").contents().length != 0;
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).contains(COIN_PRE.toString());
	}

	@Test @GUITest
	public void testAddCoinButtonUpdatesTheListThroughPresenterWhenFailCoinDuplicate() {
		populateDB();
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});

		window.textBox("coinFormDescription").setText("").enterText(COIN_PRE.getDescription());
		window.textBox("coinFormCountry").setText("").enterText(COIN_PRE.getCountry());
		window.comboBox("coinFormAlbum").clearSelection().selectItem(ALBUM_PRE.toString());
		window.comboBox("coinFormGrade").clearSelection().selectItem(COIN_PRE.getGrade().toString());
		window.textBox("coinFormYear").setText("").enterText(COIN_PRE.getMintingYear().toString());
		window.textBox("coinFormNote").setText("").enterText(COIN_PRE.getNote());

		window.button(JButtonMatcher.withText("Save coin")).click();

		pause(new Condition("Label is not empty") {
			@Override
			public boolean test() {
				return !window.label("status").text().trim().isEmpty();
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).contains(COIN_PRE.toString());
		window.label(JLabelMatcher.withName("status").andText("This coin already exists"));
	}

	@Test @GUITest
	public void testAddCoinButtonDoNothingThroughPresenterWhenFailAlbumFull() {
		initAlbums();
		persistAlbums();
		initCoins();
		ALBUM_PRE.setOccupiedSlots(ALBUM_PRE.getNumberOfSlots());
		em.getTransaction().begin();
		em.merge(ALBUM_PRE);
		em.getTransaction().commit();
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});

		window.textBox("coinFormDescription").setText("").enterText(COIN_PRE.getDescription());
		window.textBox("coinFormCountry").setText("").enterText(COIN_PRE.getCountry());
		window.comboBox("coinFormAlbum").clearSelection().selectItem(ALBUM_PRE.toString());
		window.comboBox("coinFormGrade").clearSelection().selectItem(COIN_PRE.getGrade().toString());
		window.textBox("coinFormYear").setText("").enterText(COIN_PRE.getMintingYear().toString());
		window.textBox("coinFormNote").setText("").enterText(COIN_PRE.getNote());

		window.button(JButtonMatcher.withText("Save coin")).click();

		pause(new Condition("Label is not empty") {
			@Override
			public boolean test() {
				return !window.label("status").text().trim().isEmpty();
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).doesNotContain(COIN_PRE.toString());
		window.label(JLabelMatcher.withName("status").andText("Impossible to add the coin to this album because it is full"));
	}

	@Test @GUITest
	public void testAddCoinButtonDoNothingThroughPresenterWhenFailAlbumNotFound() {
		initAlbums();
		persistAlbums();
		initCoins();
		em.getTransaction().begin();
		em.remove(ALBUM_PRE);
		em.getTransaction().commit();
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_PRE);
		});

		window.textBox("coinFormDescription").setText("").enterText(COIN_PRE.getDescription());
		window.textBox("coinFormCountry").setText("").enterText(COIN_PRE.getCountry());
		window.comboBox("coinFormAlbum").clearSelection().selectItem(ALBUM_PRE.toString());
		window.comboBox("coinFormGrade").clearSelection().selectItem(COIN_PRE.getGrade().toString());
		window.textBox("coinFormYear").setText("").enterText(COIN_PRE.getMintingYear().toString());
		window.textBox("coinFormNote").setText("").enterText(COIN_PRE.getNote());

		window.button(JButtonMatcher.withText("Save coin")).click();

		pause(new Condition("Label is not empty") {
			@Override
			public boolean test() {
				return !window.label("status").text().trim().isEmpty();
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).doesNotContain(COIN_PRE.toString());
		window.label(JLabelMatcher.withName("status").andText("Impossible to complete the operation because this album doesn't exist"));
	}

	@Test @GUITest
	public void testDeleteCoinButtonRemovesFromTheListThroughPresenterWhenSuccess() {
		populateDB();
		GuiActionRunner.execute(() -> {
			view.getCoinListModel().addElement(COIN_PRE);
		});

		window.list("coinList").item(COIN_PRE.toString()).select();
		window.button(JButtonMatcher.withText("Delete coin")).click();

		pause(new Condition("List is empty") {
			@Override
			public boolean test() {
				return window.list("coinList").contents().length == 0;
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).doesNotContain(COIN_PRE.toString());
	}

	@Test @GUITest
	public void testDeleteCoinButtonRemovesFromTheListThroughPresenterWhenFail() {
		populateDB();
		GuiActionRunner.execute(() -> {
			view.getCoinListModel().addElement(COIN_PRE);
		});
		em.getTransaction().begin();
		em.remove(COIN_PRE);
		em.getTransaction().commit();

		window.list("coinList").item(COIN_PRE.toString()).select();
		window.button(JButtonMatcher.withText("Delete coin")).click();

		pause(new Condition("Label shows error relative to deletion") {
			@Override
			public boolean test() {
				return window.label("status").text().trim() == "This coin doesn't exist";
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).doesNotContain(COIN_PRE.toString());
		window.label(JLabelMatcher.withName("status").andText("This coin doesn't exist"));
	}

	@Test @GUITest
	public void testMoveCoinButtonUpdatesTheListThroughPresenterWhenSuccess() {
		populateDB();
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_COMM_2);
			view.getCoinListModel().addElement(COIN_COMM_1);
		});

		window.list("coinList").item(COIN_COMM_1.toString()).select();
		window.button(JButtonMatcher.withText("Move coin")).click();
		window.dialog().comboBox().selectItem(ALBUM_COMM_2.toString());
		window.dialog().button(JButtonMatcher.withText("OK")).click();

		pause(new Condition("List contain updated element") {
			@Override
			public boolean test() {
				return window.label("coinSelection").text().contains(ALBUM_COMM_2.getName());
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).contains(COIN_COMM_1.toString());
		assertThat(COIN_COMM_1.getAlbum()).isEqualTo(ALBUM_COMM_2.getId());
	}

	@Test @GUITest
	public void testMoveCoinButtonUpdatesTheListThroughPresenterWhenFailCoinNotFound() {
		populateDB();
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_COMM_2);
			view.getCoinListModel().addElement(COIN_COMM_1);
		});

		window.list("coinList").item(COIN_COMM_1.toString()).select();

		pause(new Condition("Label react to selection") {
			@Override
			public boolean test() {
				return !window.label("coinSelection").text().trim().isEmpty();
			}
		}, timeout(TIMEOUT));

		em.getTransaction().begin();
		em.remove(COIN_COMM_1);
		em.getTransaction().commit();

		window.button(JButtonMatcher.withText("Move coin")).click();
		window.dialog().comboBox().selectItem(ALBUM_COMM_2.toString());
		window.dialog().button(JButtonMatcher.withText("OK")).click();

		pause(new Condition("Label shows error relative to movement") {
			@Override
			public boolean test() {
				return window.label("status").text().trim() == "This coin doesn't exist";
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).doesNotContain(COIN_COMM_1.toString());
		window.label(JLabelMatcher.withName("status").andText("This coin doesn't exist"));
	}

	@Test @GUITest
	public void testMoveCoinButtonDoNothingThroughPresenterWhenFailFullAlbum() {
		populateDB();
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_COMM_2);
			view.getCoinListModel().addElement(COIN_COMM_1);
		});
		ALBUM_COMM_2.setOccupiedSlots(ALBUM_COMM_2.getNumberOfSlots());
		em.getTransaction().begin();
		em.merge(ALBUM_COMM_2);
		em.getTransaction().commit();

		window.list("coinList").item(COIN_COMM_1.toString()).select();
		window.button(JButtonMatcher.withText("Move coin")).click();
		window.dialog().comboBox().selectItem(ALBUM_COMM_2.toString());
		window.dialog().button(JButtonMatcher.withText("OK")).click();

		pause(new Condition("Label shows error relative to movement") {
			@Override
			public boolean test() {
				return window.label("status").text().trim() == "Impossible to move the coin to this album because it is full";
			}
		}, timeout(TIMEOUT));

		String[] listContents = window.list("coinList").contents();

		assertThat(listContents).contains(COIN_COMM_1.toString());
		assertThat(COIN_COMM_1.getAlbum()).isEqualTo(ALBUM_COMM_1.getId());
		window.label(JLabelMatcher.withName("status").andText("Impossible to move the coin to this album because it is full"));
	}

	@Test @GUITest
	public void testMoveCoinButtonUpdatesAlbumListsThroughPresenterWhenFailAlbumNotFound() {
		populateDB();
		GuiActionRunner.execute(() -> {
			view.getCoinFormAlbumModel().addElement(ALBUM_COMM_2);
			view.getCoinListModel().addElement(COIN_COMM_1);
		});

		window.list("coinList").item(COIN_COMM_1.toString()).select();
		window.button(JButtonMatcher.withText("Move coin")).click();

		em.getTransaction().begin();
		em.remove(ALBUM_COMM_2);
		em.getTransaction().commit();

		window.dialog().comboBox().selectItem(ALBUM_COMM_2.toString());
		window.dialog().button(JButtonMatcher.withText("OK")).click();

		pause(new Condition("Label shows error relative to movement") {
			@Override
			public boolean test() {
				return window.label("status").text().trim() == "Impossible to complete the operation because this album doesn't exist";
			}
		}, timeout(TIMEOUT));

		String[] coinListContents = window.list("coinList").contents();
		String[] albumListContents = window.list("albumList").contents();
		String[] comboboxContents = window.comboBox("coinFormAlbum").contents();

		assertThat(coinListContents).contains(COIN_COMM_1.toString());
		assertThat(albumListContents).doesNotContain(ALBUM_COMM_2.toString());
		assertThat(comboboxContents).doesNotContain(ALBUM_COMM_2.toString());
		assertThat(COIN_COMM_1.getAlbum()).isEqualTo(ALBUM_COMM_1.getId());
		window.label(JLabelMatcher.withName("status").andText("Impossible to complete the operation because this album doesn't exist"));
	}

	@Override
	protected void onTearDown() throws Exception {
		em.clear();
		em.close();
	}

	@AfterClass
	public static void cleanTestCase() {
		emf.close();
	}

	// private
	private void populateDB() {
		initAlbums();
		persistAlbums();
		initCoins();
		persistCoins();
	}

	private void persistCoins() {
		em.getTransaction().begin();
		em.persist(COIN_COMM_1);
		em.persist(COIN_COMM_2);
		em.persist(COIN_PRE);
		em.getTransaction().commit();
	}

	private void initCoins() {
		COIN_COMM_1 = new Coin(Grade.AG, "Italy", Year.of(2004), "2€ comm. World Food Programme", "", ALBUM_COMM_1.getId());
		COIN_COMM_2 = new Coin(Grade.AG, "Greece", Year.of(2004), "2€ comm. Olympics Game of Athen 2004", "", ALBUM_COMM_1.getId());
		COIN_PRE = new Coin(Grade.AG, "Italy", Year.of(1995), "500 Lire", "", ALBUM_PRE.getId());
	}

	private void persistAlbums() {
		em.getTransaction().begin();
		em.persist(ALBUM_COMM_1);
		em.persist(ALBUM_COMM_2);
		em.persist(ALBUM_PRE);
		em.getTransaction().commit();
	}

	private void initAlbums() {
		ALBUM_PRE = new Album("Europa pre-euro", 1, "Armadio", 50, 0);
		ALBUM_COMM_1 = new Album("Euro commemorativi", 1, "Armadio", 50, 0);
		ALBUM_COMM_2 = new Album("Euro commemorativi", 2, "Armadio", 50, 0);
	}
}
