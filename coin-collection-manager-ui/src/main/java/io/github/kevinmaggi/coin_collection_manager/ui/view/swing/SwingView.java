package io.github.kevinmaggi.coin_collection_manager.ui.view.swing;

import static io.github.kevinmaggi.coin_collection_manager.ui.view.swing.SwingViewUtilities.comboBoxToArray;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Year;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionListener;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.AlbumPresenter;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.CoinPresenter;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;
import java.util.Locale;

/**
 * This class offer an implementation of View component of MVP pattern using Swing.
 */
public class SwingView extends JFrame implements View {
	private static final long serialVersionUID = 1L;
	
	/////////////// Utilities
	private static final String ALBUM_STRING = "%s volume %d located in %s with %d/%d slots occupied";
	private static final String COIN_STRING = "%s of %d from %s located in %s vol.%d (Grade: %s) [note: %s]";
	private static final String RESULTS = "Results for \"%s\":";
	private static final String IN_ALBUM = "Coins in %s vol.%d:";
	
	/////////////// Presenters
	private transient AlbumPresenter albumPresenter;
	private transient CoinPresenter coinPresenter;
	
	/////////////// Interactive components
	private JTextField albumSearchName;
	private JTextField albumSearchVolume;
	private JButton albumSearchButton;
	
	private DefaultListModel<Album> albumListModel;
	private JList<Album> albumList;
	private JLabel albumActualLabel;
	private JLabel albumSelectionLabel;
	
	private JButton albumDeleteButton;
	private JButton albumMoveButton;
	
	private JTextField albumFormName;
	private JTextField albumFormVolume;
	private JTextField albumFormLocation;
	private JTextField albumFormSlots;
	private JButton albumSaveButton;
	
	private JTextField coinFilterDescription;
	private JButton coinFilterButton;

	private DefaultListModel<Coin> coinListModel;
	private JList<Coin> coinList;
	private JLabel coinActualLabel;
	private JLabel coinSelectionLabel;
	
	private JButton coinDeleteButton;
	private JButton coinMoveButton;
	
	private JTextField coinFormDescription;
	private JTextField coinFormCountry;
	private JTextField coinFormYear;
	private JTextField coinFormNote;
	private JComboBox<Grade> coinFormGrade;
	private DefaultComboBoxModel<Album> coinFormAlbumModel;
	private JComboBox<Album> coinFormAlbum;
	private JButton coinSaveButton;
	
	private JLabel statusLabel;
	
	/////////////// Listeners and adapters
	private transient KeyAdapter albumSearchButtonEnabler = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			albumSearchButton.setEnabled(
				!albumSearchName.getText().trim().isEmpty() &&
				albumSearchVolume.getText().trim().matches("\\d+")
			);
		}
	};
	
	private transient KeyAdapter albumFormButtonEnabler = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			albumSaveButton.setEnabled(
				!albumFormName.getText().trim().isEmpty() &&
				albumFormVolume.getText().trim().matches("\\d+") &&
				!albumFormLocation.getText().trim().isEmpty() &&
				albumFormSlots.getText().trim().matches("\\d+")
			);
		}
	};
	
	private transient KeyAdapter coinFilterButtonEnabler = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			coinFilterButton.setEnabled(!coinFilterDescription.getText().trim().isEmpty());
		}
	};
	
	private transient KeyAdapter coinFormButtonEnablerTextBox = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			coinSaveButton.setEnabled(
				!coinFormDescription.getText().trim().isEmpty() &&
				coinFormGrade.getSelectedIndex() != -1 &&
				!coinFormCountry.getText().trim().isEmpty() &&
				coinFormYear.getText().trim().matches("\\d+") &&
				coinFormAlbum.getSelectedIndex() != -1
			);
		}
	};
	
	private transient ItemListener coinFormButtonEnablerComboBox = 
			e -> {
				if (e.getStateChange() == ItemEvent.SELECTED)
					coinSaveButton.setEnabled(
						!coinFormDescription.getText().trim().isEmpty() &&
						coinFormGrade.getSelectedIndex() != -1 &&
						!coinFormCountry.getText().trim().isEmpty() &&
						coinFormYear.getText().trim().matches("\\d+") &&
						coinFormAlbum.getSelectedIndex() != -1
					);
			};
			
	private transient ActionListener albumSearchAction = 
			e -> new Thread(() -> 
				albumPresenter.searchAlbum(albumSearchName.getText(), Integer.valueOf(albumSearchVolume.getText()))
			).start();
			
	private transient ActionListener albumClearAction = 
			e -> new Thread(() ->
				albumPresenter.getAllAlbums()
			).start();
			
	private transient ListSelectionListener albumListSelection =
			e -> {
				if(!e.getValueIsAdjusting()) {
					if(albumList.getSelectedIndex() != -1) {
						new Thread(() -> {
							albumPresenter.getAlbum(albumList.getSelectedValue().getId());
							coinPresenter.getCoinsByAlbum(albumList.getSelectedValue());
						}).start();
					} else {
						albumSelectionLabel.setText(" ");
						albumDeleteButton.setEnabled(false);
						albumMoveButton.setEnabled(false);
					}
				}
			};
		
	private transient ActionListener albumDeleteAction =
			e -> new Thread(() -> {
				albumPresenter.deleteAlbum(albumList.getSelectedValue());
				coinPresenter.getAllCoins();
			}).start();
			
	private transient ActionListener albumMoveAction =
			e -> {
				String input = JOptionPane.showInputDialog(this, 
						"New location:", "Move " + albumList.getSelectedValue().toString(), JOptionPane.PLAIN_MESSAGE);
				new Thread(() -> {
					if(input != null && !input.isBlank())
						albumPresenter.moveAlbum(albumList.getSelectedValue(), input);
				}).start();
			};
			
	private transient ActionListener albumSaveAction =
			e -> new Thread(() -> 
				albumPresenter.addAlbum(
						new Album(
								albumFormName.getText(),
								Integer.parseInt(albumFormVolume.getText()), 
								albumFormLocation.getText(), 
								Integer.parseInt(albumFormSlots.getText()), 
								0)
				)
			).start();
			
	private transient ActionListener coinFilterAction =
			e -> new Thread(() -> 
				coinPresenter.searchCoins(coinFilterDescription.getText())
			).start();
			
	private transient ActionListener coinClearAction = 
			e -> new Thread(() -> 
				coinPresenter.getAllCoins()
			).start();
			
	private transient ListSelectionListener coinListSelection = 
			e -> {
				if(!e.getValueIsAdjusting()) {
					if(coinList.getSelectedIndex() != -1) {
						new Thread(() ->
							coinPresenter.getCoin(coinList.getSelectedValue().getId())
						).start();
					} else {
						coinSelectionLabel.setText(" ");
						coinDeleteButton.setEnabled(false);
						coinMoveButton.setEnabled(false);
					}
				}
			};
			
	private transient ActionListener coinDeleteAction =
			e -> new Thread(() -> 
				coinPresenter.deleteCoin(coinList.getSelectedValue())
			).start();
			
	private transient ActionListener coinMoveAction =
			e -> {
				Object input = JOptionPane.showInputDialog(this, 
						"New album:", "Move " + coinList.getSelectedValue().toString(), JOptionPane.PLAIN_MESSAGE, 
						null, comboBoxToArray(coinFormAlbum), null);
				new Thread(() -> {
					if(input != null)
						coinPresenter.moveCoin(coinList.getSelectedValue(), (Album)input);
				}).start();
			};
			
	private transient ActionListener coinSaveAction =
			e -> new Thread(() -> 
				coinPresenter.addCoin(
						new Coin(
								(Grade)coinFormGrade.getSelectedItem(),
								coinFormCountry.getText(),
								Year.parse(coinFormYear.getText()),
								coinFormDescription.getText(),
								coinFormNote.getText(),
								((Album)coinFormAlbum.getSelectedItem()).getId())
				)
			).start();
	
	/////////////// Methods
	DefaultListModel<Album> getAlbumListModel() {
		return albumListModel;
	}

	DefaultListModel<Coin> getCoinListModel() {
		return coinListModel;
	}
	
	DefaultComboBoxModel<Album> getCoinFormAlbumModel() {
		return coinFormAlbumModel;
	}
	
	JButton getAlbumDeleteButton() {
		return albumDeleteButton;
	}

	JButton getAlbumMoveButton() {
		return albumMoveButton;
	}

	JButton getCoinDeleteButton() {
		return coinDeleteButton;
	}

	JButton getCoinMoveButton() {
		return coinMoveButton;
	}

	/////////////// View Interface methods
	/**
	 * Sets the presenters.
	 * 
	 * @param coinPresenter		presenter for coin entities
	 * @param albumPresenter	presenter for album entities
	 */
	public void setPresenters(CoinPresenter coinPresenter, AlbumPresenter albumPresenter) {
		this.coinPresenter = coinPresenter;
		this.albumPresenter = albumPresenter;
		
		coinPresenter.getAllCoins();
		albumPresenter.getAllAlbums();
	}
	
	/**
	 * Shows all albums in the dedicated element.
	 * 
	 * @param albums	albums to show
	 */
	@Override
	public void showAllAlbums(List<Album> albums) {
		SwingUtilities.invokeLater(() -> {
			albumList.clearSelection();
			albumListModel.removeAllElements();
			albums.stream().forEach(albumListModel::addElement);
			albumActualLabel.setText("All albums:");
			
			albumSearchName.setText("");
			albumSearchVolume.setText("");
			albumSearchButton.setEnabled(false);
			
			coinFormAlbumModel.removeAllElements();
			albums.stream().forEach(coinFormAlbumModel::addElement);
		});
	}

	/**
	 * Shows the albums result of search in the dedicated element.
	 * 
	 * @param album		album to show
	 * @param search	searching key as string
	 */
	@Override
	public void showSearchedAlbum(Album album, String search) {
		SwingUtilities.invokeLater(() -> {
			albumListModel.removeAllElements();
			albumListModel.addElement(album);
			albumActualLabel.setText(String.format(RESULTS, search));
		});
	}

	/**
	 * Shows a selected album in the dedicated element.
	 * 
	 * @param album		album to show
	 */
	@Override
	public void showAlbum(Album album) {
		SwingUtilities.invokeLater(() -> {
			albumSelectionLabel.setText(
					String.format(ALBUM_STRING, 
							album.getName(), album.getVolume(), album.getLocation(), album.getOccupiedSlots(), album.getNumberOfSlots()));
			albumDeleteButton.setEnabled(true);
			albumMoveButton.setEnabled(true);
		});
	}

	/**
	 * Feedbacks the user to an added album: updates lists and selection label.
	 * 
	 * @param album		added album
	 */
	@Override
	public void albumAdded(Album album) {
		SwingUtilities.invokeLater(() -> {
			int index = coinFormAlbum.getSelectedIndex();
			
			albumListModel.addElement(album);
			coinFormAlbumModel.addElement(album);
			
			if (index == -1)
				coinFormAlbum.setSelectedIndex(-1);
			
			albumFormName.setText("");
			albumFormVolume.setText("");
			albumFormLocation.setText("");
			albumFormSlots.setText("");
			
			albumSaveButton.setEnabled(false);
			
			repaintLists();
		});
	}

	/**
	 * Feedbacks the user to a deleted album: update lists.
	 * 
	 * @param album		deleted album
	 */
	@Override
	public void albumDeleted(Album album) {
		SwingUtilities.invokeLater(() -> {
			albumListModel.removeElement(album);
			coinFormAlbumModel.removeElement(album);
			
			repaintLists();
		});
	}

	/**
	 * Feedbacks the user to a moved album: updates lists and selection label.
	 * 
	 * @param album		moved album
	 */
	@Override
	public void albumMoved(Album album) {
		SwingUtilities.invokeLater(() -> {
			int index = albumListModel.indexOf(album);
			albumListModel.removeElement(album);
			albumListModel.add(index, album);
			albumList.setSelectedValue(album, true);
			
			albumSelectionLabel.setText(
					String.format(ALBUM_STRING, 
							album.getName(), album.getVolume(), album.getLocation(), album.getOccupiedSlots(), album.getNumberOfSlots()));
			
			repaintLists();
		});
	}

	/**
	 * Shows all coins in the dedicated element.
	 * 
	 * @param coins		coins to show
	 */
	@Override
	public void showAllCoins(List<Coin> coins) {
		SwingUtilities.invokeLater(() -> {
			coinList.clearSelection();
			coinListModel.removeAllElements();
			coins.stream().forEach(coinListModel::addElement);
			coinActualLabel.setText("All coins:");
			
			coinFilterDescription.setText("");
			albumSearchButton.setEnabled(false);
			
			albumList.clearSelection();
		});
	}

	/**
	 * Shows the coins result of search in the dedicated element.
	 * 
	 * @param coins		coins to show
	 * @param search	searching key as string
	 */
	@Override
	public void showSearchedCoins(List<Coin> coins, String search) {
		SwingUtilities.invokeLater(() -> {
			coinListModel.removeAllElements();
			coins.stream().forEach(coinListModel::addElement);
			coinActualLabel.setText(String.format(RESULTS, search));
			
			albumList.clearSelection();
		});
	}

	/**
	 * Shows the coins contained in an album in the dedicated element.
	 * 
	 * @param coins		coins to show
	 * @param album		album subject of the filter
	 */
	@Override
	public void showCoinsInAlbum(List<Coin> coins, Album album) {
		SwingUtilities.invokeLater(() -> {
			coinListModel.removeAllElements();
			coins.stream().forEach(coinListModel::addElement);
			coinActualLabel.setText(String.format(IN_ALBUM, album.getName(), album.getVolume()));
		});
	}

	/**
	 * Shows a selected coin in the dedicated element.
	 * 
	 * @param coin		coin to show
	 * @param album		album to which belongs
	 */
	@Override
	public void showCoin(Coin coin, Album album) {
		SwingUtilities.invokeLater(() -> {
			coinSelectionLabel.setText(
					String.format(COIN_STRING, 
							coin.getDescription(), coin.getMintingYear().getValue(), coin.getCountry(), album.getName(), 
							album.getVolume(), coin.getGrade().getMeaning(), coin.getNote()));
			coinDeleteButton.setEnabled(true);
			coinMoveButton.setEnabled(true);
		});
	}

	/**
	 * Feedbacks the user to an added coin: updates list and clears form.
	 * 
	 * @param coin		added coin
	 */
	@Override
	public void coinAdded(Coin coin) {
		SwingUtilities.invokeLater(() -> {
			coinListModel.addElement(coin);
			
			coinFormDescription.setText("");
			coinFormGrade.setSelectedIndex(-1);
			coinFormCountry.setText("");
			coinFormYear.setText("");
			coinFormAlbum.setSelectedIndex(-1);
			coinFormNote.setText("");
			
			coinSaveButton.setEnabled(false);
			
			repaintLists();
		});
	}

	/**
	 * Feedbacks the user to a deleted coin: updates list.
	 * 
	 * @param coin		deleted coin
	 */
	@Override
	public void coinDeleted(Coin coin) {
		SwingUtilities.invokeLater(() -> {
			coinListModel.removeElement(coin);
			
			repaintLists();
		});
	}

	/**
	 * Feedbacks the user to a moved coin: updates lists and selection label.
	 * 
	 * @param coin		moved coin
	 * @param oldAlbum	old album of the coin
	 * @param newAlbum	new album of the coin
	 */
	@Override
	public void coinMoved(Coin coin, Album oldAlbum, Album newAlbum) {
		SwingUtilities.invokeLater(() -> {
			int index = coinListModel.indexOf(coin);
			coinListModel.removeElement(coin);
			coinListModel.add(index, coin);
			coinList.setSelectedValue(coin, true);
			
			coinSelectionLabel.setText(
					String.format(COIN_STRING, 
							coin.getDescription(), coin.getMintingYear().getValue(), coin.getCountry(), newAlbum.getName(), 
							newAlbum.getVolume(), coin.getGrade().getMeaning(), coin.getNote()));
			
			repaintLists();
		});
	}

	/**
	 * Shows to the user an error message
	 * 
	 * @param msg		message to show
	 */
	@Override
	public void showError(String msg) {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText(msg);
		});
	}

	/**
	 * Shows to the user a success message
	 * 
	 * @param msg		message to show
	 */
	@Override
	public void showSuccess(String msg) {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setForeground(Color.GREEN);
			statusLabel.setText(msg);
		});
	}

	/////////////// Private utility methods
	/**
	 * Force the repainting of the list without delay.
	 */
	private void repaintLists() {
		coinList.repaint();
		albumList.repaint();
		coinFormAlbum.repaint();
	}
	
	/////////////// GUI construction
	/**
	 * Constructor of the GUI
	 */
	public SwingView() {
		setPreferredSize(new Dimension(1024, 640));
		setLocale(Locale.ENGLISH);
		setMinimumSize(new Dimension(1024, 640));
		setFont(new Font("Tahoma", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(SwingView.class.getResource("/icon/icon.png")));
		setTitle("Coin Collection Manager");
		getContentPane().setLayout(new GridLayout(1, 0, 0, 0));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JPanel mainPanel = new JPanel();
		GridBagConstraints gbc_mainPanel = new GridBagConstraints();
		gbc_mainPanel.ipady = 10;
		gbc_mainPanel.ipadx = 10;
		gbc_mainPanel.fill = GridBagConstraints.BOTH;
		gbc_mainPanel.gridx = 0;
		gbc_mainPanel.gridy = 0;
		panel.add(mainPanel, gbc_mainPanel);
		mainPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel albumPanel = new JPanel();
		albumPanel.setBorder(new CompoundBorder(new LineBorder(new Color(240, 240, 240), 4), new LineBorder(new Color(0, 0, 255), 2)));
		mainPanel.add(albumPanel);
		GridBagLayout gbl_albumPanel = new GridBagLayout();
		gbl_albumPanel.columnWidths = new int[]{484, 0};
		gbl_albumPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_albumPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_albumPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		albumPanel.setLayout(gbl_albumPanel);
		
		JPanel albumTitlePanel = new JPanel();
		GridBagConstraints gbc_albumTitlePanel = new GridBagConstraints();
		gbc_albumTitlePanel.insets = new Insets(0, 0, 5, 0);
		gbc_albumTitlePanel.fill = GridBagConstraints.BOTH;
		gbc_albumTitlePanel.gridx = 0;
		gbc_albumTitlePanel.gridy = 0;
		albumPanel.add(albumTitlePanel, gbc_albumTitlePanel);
		
		JLabel albumTitleLabel = new JLabel("ALBUMS");
		albumTitleLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		albumTitlePanel.add(albumTitleLabel);
		
		JPanel albumSearchPanel = new JPanel();
		GridBagConstraints gbc_albumSearchPanel = new GridBagConstraints();
		gbc_albumSearchPanel.insets = new Insets(0, 0, 5, 0);
		gbc_albumSearchPanel.fill = GridBagConstraints.BOTH;
		gbc_albumSearchPanel.gridx = 0;
		gbc_albumSearchPanel.gridy = 1;
		albumPanel.add(albumSearchPanel, gbc_albumSearchPanel);
		GridBagLayout gbl_albumSearchPanel = new GridBagLayout();
		gbl_albumSearchPanel.columnWidths = new int[]{0, 136, 94, 25, 90, 65, 0};
		gbl_albumSearchPanel.rowHeights = new int[]{0, 0};
		gbl_albumSearchPanel.columnWeights = new double[]{0.0, 3.0, 0.0, 0.0, 2.0, 0.0, Double.MIN_VALUE};
		gbl_albumSearchPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		albumSearchPanel.setLayout(gbl_albumSearchPanel);
		
		JLabel albumSearchNameLabel = new JLabel("Key name: ");
		albumSearchNameLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		albumSearchNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumSearchNameLabel = new GridBagConstraints();
		gbc_albumSearchNameLabel.ipadx = 5;
		gbc_albumSearchNameLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_albumSearchNameLabel.ipady = 10;
		gbc_albumSearchNameLabel.insets = new Insets(0, 0, 0, 5);
		gbc_albumSearchNameLabel.gridx = 0;
		gbc_albumSearchNameLabel.gridy = 0;
		albumSearchPanel.add(albumSearchNameLabel, gbc_albumSearchNameLabel);
		
		albumSearchName = new JTextField();
		albumSearchName.setName("albumSearchName");
		albumSearchName.addKeyListener(albumSearchButtonEnabler);
		albumSearchName.setToolTipText("Album's name to search");
		albumSearchName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumSearchNameLabel.setLabelFor(albumSearchName);
		GridBagConstraints gbc_albumSearchName = new GridBagConstraints();
		gbc_albumSearchName.insets = new Insets(0, 0, 0, 5);
		gbc_albumSearchName.fill = GridBagConstraints.HORIZONTAL;
		gbc_albumSearchName.gridx = 1;
		gbc_albumSearchName.gridy = 0;
		albumSearchPanel.add(albumSearchName, gbc_albumSearchName);
		albumSearchName.setColumns(10);
		
		JLabel albumSearchVolumeLabel = new JLabel("Key volume: ");
		albumSearchVolumeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		albumSearchVolumeLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumSearchVolumeLabel = new GridBagConstraints();
		gbc_albumSearchVolumeLabel.ipady = 10;
		gbc_albumSearchVolumeLabel.ipadx = 5;
		gbc_albumSearchVolumeLabel.insets = new Insets(0, 0, 0, 5);
		gbc_albumSearchVolumeLabel.gridx = 2;
		gbc_albumSearchVolumeLabel.gridy = 0;
		albumSearchPanel.add(albumSearchVolumeLabel, gbc_albumSearchVolumeLabel);
		
		albumSearchVolume = new JTextField();
		albumSearchVolume.setName("albumSearchVolume");
		albumSearchVolume.addKeyListener(albumSearchButtonEnabler);
		albumSearchVolume.setToolTipText("Album's volume to search");
		albumSearchVolume.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumSearchVolumeLabel.setLabelFor(albumSearchVolume);
		GridBagConstraints gbc_albumSearchVolume = new GridBagConstraints();
		gbc_albumSearchVolume.insets = new Insets(0, 0, 0, 5);
		gbc_albumSearchVolume.fill = GridBagConstraints.HORIZONTAL;
		gbc_albumSearchVolume.gridx = 3;
		gbc_albumSearchVolume.gridy = 0;
		albumSearchPanel.add(albumSearchVolume, gbc_albumSearchVolume);
		albumSearchVolume.setColumns(10);
		
		albumSearchButton = new JButton("Search");
		albumSearchButton.addActionListener(albumSearchAction);
		albumSearchButton.setEnabled(false);
		albumSearchButton.setToolTipText("Search the album");
		albumSearchButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumSearchButton = new GridBagConstraints();
		gbc_albumSearchButton.insets = new Insets(0, 0, 0, 5);
		gbc_albumSearchButton.gridx = 4;
		gbc_albumSearchButton.gridy = 0;
		albumSearchPanel.add(albumSearchButton, gbc_albumSearchButton);
		
		JButton albumClearButton = new JButton("All albums");
		albumClearButton.setToolTipText("Reload all albums");
		albumClearButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumClearButton.setEnabled(true);
		albumClearButton.addActionListener(albumClearAction);
		GridBagConstraints gbc_albumClearButton = new GridBagConstraints();
		gbc_albumClearButton.gridx = 5;
		gbc_albumClearButton.gridy = 0;
		albumSearchPanel.add(albumClearButton, gbc_albumClearButton);
		
		JPanel albumMainPanel = new JPanel();
		GridBagConstraints gbc_albumMainPanel = new GridBagConstraints();
		gbc_albumMainPanel.insets = new Insets(0, 0, 5, 0);
		gbc_albumMainPanel.fill = GridBagConstraints.BOTH;
		gbc_albumMainPanel.gridx = 0;
		gbc_albumMainPanel.gridy = 2;
		albumPanel.add(albumMainPanel, gbc_albumMainPanel);
		GridBagLayout gbl_albumMainPanel = new GridBagLayout();
		gbl_albumMainPanel.columnWidths = new int[]{226, 0};
		gbl_albumMainPanel.rowHeights = new int[]{15, 0, 0, 0};
		gbl_albumMainPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_albumMainPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		albumMainPanel.setLayout(gbl_albumMainPanel);
		
		albumActualLabel = new JLabel(" ");
		albumActualLabel.setName("albumActual");
		albumActualLabel.setHorizontalAlignment(SwingConstants.CENTER);
		albumActualLabel.setFont(new Font("Tahoma", Font.ITALIC, 12));
		GridBagConstraints gbc_albumActualLabel = new GridBagConstraints();
		gbc_albumActualLabel.ipady = 10;
		gbc_albumActualLabel.insets = new Insets(0, 0, 5, 0);
		gbc_albumActualLabel.gridx = 0;
		gbc_albumActualLabel.gridy = 0;
		albumMainPanel.add(albumActualLabel, gbc_albumActualLabel);
		
		JScrollPane albumScroll = new JScrollPane();
		GridBagConstraints gbc_albumScroll = new GridBagConstraints();
		gbc_albumScroll.insets = new Insets(0, 0, 5, 0);
		gbc_albumScroll.fill = GridBagConstraints.BOTH;
		gbc_albumScroll.gridx = 0;
		gbc_albumScroll.gridy = 1;
		albumMainPanel.add(albumScroll, gbc_albumScroll);
		
		albumListModel = new DefaultListModel<>();
		albumList = new JList<>(albumListModel);
		albumList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		albumList.addListSelectionListener(albumListSelection);
		albumList.setName("albumList");
		albumList.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumScroll.setViewportView(albumList);
		
		albumSelectionLabel = new JLabel(" ");
		albumSelectionLabel.setName("albumSelection");
		albumSelectionLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
		GridBagConstraints gbc_albumSelectionLabel = new GridBagConstraints();
		gbc_albumSelectionLabel.ipady = 10;
		gbc_albumSelectionLabel.ipadx = 10;
		gbc_albumSelectionLabel.gridx = 0;
		gbc_albumSelectionLabel.gridy = 2;
		albumMainPanel.add(albumSelectionLabel, gbc_albumSelectionLabel);
		
		JPanel albumControlPanel = new JPanel();
		GridBagConstraints gbc_albumControlPanel = new GridBagConstraints();
		gbc_albumControlPanel.fill = GridBagConstraints.BOTH;
		gbc_albumControlPanel.gridx = 0;
		gbc_albumControlPanel.gridy = 3;
		albumPanel.add(albumControlPanel, gbc_albumControlPanel);
		GridBagLayout gbl_albumControlPanel = new GridBagLayout();
		gbl_albumControlPanel.columnWidths = new int[]{65, 51, 0};
		gbl_albumControlPanel.rowHeights = new int[]{23, 37, 0, 0};
		gbl_albumControlPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_albumControlPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		albumControlPanel.setLayout(gbl_albumControlPanel);
		
		albumDeleteButton = new JButton("Delete album");
		albumDeleteButton.addActionListener(albumDeleteAction);
		albumDeleteButton.setToolTipText("Delete this album");
		albumDeleteButton.setEnabled(false);
		albumDeleteButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumDeleteButton = new GridBagConstraints();
		gbc_albumDeleteButton.anchor = GridBagConstraints.EAST;
		gbc_albumDeleteButton.insets = new Insets(0, 0, 5, 5);
		gbc_albumDeleteButton.gridx = 0;
		gbc_albumDeleteButton.gridy = 0;
		albumControlPanel.add(albumDeleteButton, gbc_albumDeleteButton);
		
		albumMoveButton = new JButton("Move album");
		albumMoveButton.setToolTipText("Change the location of this album");
		albumMoveButton.setEnabled(false);
		albumMoveButton.addActionListener(albumMoveAction);
		albumMoveButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumMoveButton = new GridBagConstraints();
		gbc_albumMoveButton.anchor = GridBagConstraints.WEST;
		gbc_albumMoveButton.insets = new Insets(0, 0, 5, 0);
		gbc_albumMoveButton.gridx = 1;
		gbc_albumMoveButton.gridy = 0;
		albumControlPanel.add(albumMoveButton, gbc_albumMoveButton);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 2;
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 1;
		albumControlPanel.add(separator, gbc_separator);
		
		JPanel albumFormPanel = new JPanel();
		GridBagConstraints gbc_albumFormPanel = new GridBagConstraints();
		gbc_albumFormPanel.ipadx = 10;
		gbc_albumFormPanel.ipady = 10;
		gbc_albumFormPanel.gridwidth = 2;
		gbc_albumFormPanel.fill = GridBagConstraints.BOTH;
		gbc_albumFormPanel.gridx = 0;
		gbc_albumFormPanel.gridy = 2;
		albumControlPanel.add(albumFormPanel, gbc_albumFormPanel);
		GridBagLayout gbl_albumFormPanel = new GridBagLayout();
		gbl_albumFormPanel.columnWidths = new int[]{0, 215, 45, 120, 0};
		gbl_albumFormPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_albumFormPanel.columnWeights = new double[]{0.0, 4.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_albumFormPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		albumFormPanel.setLayout(gbl_albumFormPanel);
		
		JLabel albumFormNameLabel = new JLabel("Name: ");
		albumFormNameLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		albumFormNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumFormNameLabel = new GridBagConstraints();
		gbc_albumFormNameLabel.anchor = GridBagConstraints.EAST;
		gbc_albumFormNameLabel.ipady = 10;
		gbc_albumFormNameLabel.ipadx = 10;
		gbc_albumFormNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_albumFormNameLabel.gridx = 0;
		gbc_albumFormNameLabel.gridy = 0;
		albumFormPanel.add(albumFormNameLabel, gbc_albumFormNameLabel);
		
		albumFormName = new JTextField();
		albumFormName.addKeyListener(albumFormButtonEnabler);
		albumFormName.setName("albumFormName");
		albumFormName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumFormNameLabel.setLabelFor(albumFormName);
		GridBagConstraints gbc_albumFormName = new GridBagConstraints();
		gbc_albumFormName.fill = GridBagConstraints.HORIZONTAL;
		gbc_albumFormName.insets = new Insets(0, 0, 5, 5);
		gbc_albumFormName.gridx = 1;
		gbc_albumFormName.gridy = 0;
		albumFormPanel.add(albumFormName, gbc_albumFormName);
		albumFormName.setColumns(10);
		
		JLabel albumFormVolumeLabel = new JLabel("Volume: ");
		albumFormVolumeLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumFormVolumeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_albumFormVolumeLabel = new GridBagConstraints();
		gbc_albumFormVolumeLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_albumFormVolumeLabel.ipady = 10;
		gbc_albumFormVolumeLabel.ipadx = 10;
		gbc_albumFormVolumeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_albumFormVolumeLabel.gridx = 2;
		gbc_albumFormVolumeLabel.gridy = 0;
		albumFormPanel.add(albumFormVolumeLabel, gbc_albumFormVolumeLabel);
		
		albumFormVolume = new JTextField();
		albumFormVolume.setPreferredSize(new Dimension(105, 21));
		albumFormVolume.setMinimumSize(new Dimension(105, 21));
		albumFormVolume.addKeyListener(albumFormButtonEnabler);
		albumFormVolume.setName("albumFormVolume");
		albumFormVolume.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumFormVolume = new GridBagConstraints();
		gbc_albumFormVolume.anchor = GridBagConstraints.WEST;
		gbc_albumFormVolume.insets = new Insets(0, 0, 5, 0);
		gbc_albumFormVolume.gridx = 3;
		gbc_albumFormVolume.gridy = 0;
		albumFormPanel.add(albumFormVolume, gbc_albumFormVolume);
		albumFormVolume.setColumns(10);
		
		JLabel albumFormLocationLabel = new JLabel("Location: ");
		albumFormLocationLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		albumFormLocationLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumFormLocationLabel = new GridBagConstraints();
		gbc_albumFormLocationLabel.anchor = GridBagConstraints.EAST;
		gbc_albumFormLocationLabel.ipady = 10;
		gbc_albumFormLocationLabel.ipadx = 10;
		gbc_albumFormLocationLabel.insets = new Insets(0, 0, 5, 5);
		gbc_albumFormLocationLabel.gridx = 0;
		gbc_albumFormLocationLabel.gridy = 1;
		albumFormPanel.add(albumFormLocationLabel, gbc_albumFormLocationLabel);
		
		albumFormLocation = new JTextField();
		albumFormLocation.addKeyListener(albumFormButtonEnabler);
		albumFormLocation.setName("albumFormLocation");
		albumFormLocation.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumFormLocationLabel.setLabelFor(albumFormLocation);
		GridBagConstraints gbc_albumFormLocation = new GridBagConstraints();
		gbc_albumFormLocation.insets = new Insets(0, 0, 5, 5);
		gbc_albumFormLocation.fill = GridBagConstraints.HORIZONTAL;
		gbc_albumFormLocation.gridx = 1;
		gbc_albumFormLocation.gridy = 1;
		albumFormPanel.add(albumFormLocation, gbc_albumFormLocation);
		albumFormLocation.setColumns(10);
		
		JLabel albumFormSlotsLabel = new JLabel("Slots: ");
		albumFormSlotsLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		albumFormSlotsLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumFormSlotsLabel = new GridBagConstraints();
		gbc_albumFormSlotsLabel.ipady = 10;
		gbc_albumFormSlotsLabel.ipadx = 10;
		gbc_albumFormSlotsLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_albumFormSlotsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_albumFormSlotsLabel.gridx = 2;
		gbc_albumFormSlotsLabel.gridy = 1;
		albumFormPanel.add(albumFormSlotsLabel, gbc_albumFormSlotsLabel);
		
		albumFormSlots = new JTextField();
		albumFormSlots.setPreferredSize(new Dimension(105, 21));
		albumFormSlots.setMinimumSize(new Dimension(105, 21));
		albumFormSlots.addKeyListener(albumFormButtonEnabler);
		albumFormSlots.setName("albumFormSlots");
		albumFormSlots.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumFormSlotsLabel.setLabelFor(albumFormSlots);
		GridBagConstraints gbc_albumFormSlots = new GridBagConstraints();
		gbc_albumFormSlots.anchor = GridBagConstraints.WEST;
		gbc_albumFormSlots.insets = new Insets(0, 0, 5, 0);
		gbc_albumFormSlots.gridx = 3;
		gbc_albumFormSlots.gridy = 1;
		albumFormPanel.add(albumFormSlots, gbc_albumFormSlots);
		albumFormSlots.setColumns(10);
		
		albumSaveButton = new JButton("Save album");
		albumSaveButton.setToolTipText("Save album");
		albumSaveButton.setEnabled(false);
		albumSaveButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumSaveButton.addActionListener(albumSaveAction);
		GridBagConstraints gbc_albumSaveButton = new GridBagConstraints();
		gbc_albumSaveButton.gridwidth = 4;
		gbc_albumSaveButton.gridx = 0;
		gbc_albumSaveButton.gridy = 2;
		albumFormPanel.add(albumSaveButton, gbc_albumSaveButton);
		
		JPanel coinPanel = new JPanel();
		coinPanel.setBorder(new CompoundBorder(new LineBorder(new Color(240, 240, 240), 4), new LineBorder(new Color(255, 255, 0), 2)));
		mainPanel.add(coinPanel);
		GridBagLayout gbl_coinPanel = new GridBagLayout();
		gbl_coinPanel.columnWidths = new int[]{0, 0};
		gbl_coinPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_coinPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_coinPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		coinPanel.setLayout(gbl_coinPanel);
		
		JPanel coinTitlePanel = new JPanel();
		GridBagConstraints gbc_coinTitlePanel = new GridBagConstraints();
		gbc_coinTitlePanel.insets = new Insets(0, 0, 5, 0);
		gbc_coinTitlePanel.fill = GridBagConstraints.BOTH;
		gbc_coinTitlePanel.gridx = 0;
		gbc_coinTitlePanel.gridy = 0;
		coinPanel.add(coinTitlePanel, gbc_coinTitlePanel);
		
		JLabel coinTitleLabel = new JLabel("COINS");
		coinTitleLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		coinTitlePanel.add(coinTitleLabel);
		
		JPanel coinFilterPanel = new JPanel();
		GridBagConstraints gbc_coinFilterPanel = new GridBagConstraints();
		gbc_coinFilterPanel.insets = new Insets(0, 0, 5, 0);
		gbc_coinFilterPanel.fill = GridBagConstraints.BOTH;
		gbc_coinFilterPanel.gridx = 0;
		gbc_coinFilterPanel.gridy = 1;
		coinPanel.add(coinFilterPanel, gbc_coinFilterPanel);
		GridBagLayout gbl_coinFilterPanel = new GridBagLayout();
		gbl_coinFilterPanel.columnWidths = new int[]{56, 96, 85, 65, 0};
		gbl_coinFilterPanel.rowHeights = new int[]{21, 0};
		gbl_coinFilterPanel.columnWeights = new double[]{0.0, 3.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_coinFilterPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		coinFilterPanel.setLayout(gbl_coinFilterPanel);
		
		JLabel coinFilterDescriptionLabel = new JLabel("Key description: ");
		coinFilterDescriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		coinFilterDescriptionLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_coinFilterDescriptionLabel = new GridBagConstraints();
		gbc_coinFilterDescriptionLabel.ipady = 10;
		gbc_coinFilterDescriptionLabel.ipadx = 10;
		gbc_coinFilterDescriptionLabel.anchor = GridBagConstraints.WEST;
		gbc_coinFilterDescriptionLabel.insets = new Insets(0, 0, 0, 5);
		gbc_coinFilterDescriptionLabel.gridx = 0;
		gbc_coinFilterDescriptionLabel.gridy = 0;
		coinFilterPanel.add(coinFilterDescriptionLabel, gbc_coinFilterDescriptionLabel);
		
		coinFilterDescription = new JTextField();
		coinFilterDescription.setName("coinFilterDescription");
		coinFilterDescription.addKeyListener(coinFilterButtonEnabler);
		coinFilterDescription.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFilterDescriptionLabel.setLabelFor(coinFilterDescription);
		coinFilterDescription.setToolTipText("Coins' description to search");
		GridBagConstraints gbc_coinFilterDescription = new GridBagConstraints();
		gbc_coinFilterDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_coinFilterDescription.insets = new Insets(0, 0, 0, 5);
		gbc_coinFilterDescription.gridx = 1;
		gbc_coinFilterDescription.gridy = 0;
		coinFilterPanel.add(coinFilterDescription, gbc_coinFilterDescription);
		coinFilterDescription.setColumns(10);
		
		coinFilterButton = new JButton("Filter");
		coinFilterButton.setEnabled(false);
		coinFilterButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFilterButton.setToolTipText("Filter coins");
		coinFilterButton.addActionListener(coinFilterAction);
		GridBagConstraints gbc_coinFilterButton = new GridBagConstraints();
		gbc_coinFilterButton.insets = new Insets(0, 0, 0, 5);
		gbc_coinFilterButton.anchor = GridBagConstraints.NORTH;
		gbc_coinFilterButton.gridx = 2;
		gbc_coinFilterButton.gridy = 0;
		coinFilterPanel.add(coinFilterButton, gbc_coinFilterButton);
		
		JButton coinClearButton = new JButton("All coins");
		coinClearButton.setToolTipText("Reload all coins");
		coinClearButton.addActionListener(coinClearAction);
		coinClearButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinClearButton.setEnabled(true);
		GridBagConstraints gbc_coinClearButton = new GridBagConstraints();
		gbc_coinClearButton.gridx = 3;
		gbc_coinClearButton.gridy = 0;
		coinFilterPanel.add(coinClearButton, gbc_coinClearButton);
		
		JPanel coinMainPanel = new JPanel();
		GridBagConstraints gbc_coinMainPanel = new GridBagConstraints();
		gbc_coinMainPanel.insets = new Insets(0, 0, 5, 0);
		gbc_coinMainPanel.fill = GridBagConstraints.BOTH;
		gbc_coinMainPanel.gridx = 0;
		gbc_coinMainPanel.gridy = 2;
		coinPanel.add(coinMainPanel, gbc_coinMainPanel);
		GridBagLayout gbl_coinMainPanel = new GridBagLayout();
		gbl_coinMainPanel.columnWidths = new int[]{45, 0};
		gbl_coinMainPanel.rowHeights = new int[]{13, 0, 0, 0};
		gbl_coinMainPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_coinMainPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		coinMainPanel.setLayout(gbl_coinMainPanel);
		
		coinActualLabel = new JLabel(" ");
		coinActualLabel.setName("coinActual");
		coinActualLabel.setFont(new Font("Tahoma", Font.ITALIC, 12));
		GridBagConstraints gbc_coinActualLabel = new GridBagConstraints();
		gbc_coinActualLabel.insets = new Insets(0, 0, 5, 0);
		gbc_coinActualLabel.ipady = 10;
		gbc_coinActualLabel.anchor = GridBagConstraints.NORTH;
		gbc_coinActualLabel.gridx = 0;
		gbc_coinActualLabel.gridy = 0;
		coinMainPanel.add(coinActualLabel, gbc_coinActualLabel);
		
		JScrollPane coinScroll = new JScrollPane();
		GridBagConstraints gbc_coinScroll = new GridBagConstraints();
		gbc_coinScroll.insets = new Insets(0, 0, 5, 0);
		gbc_coinScroll.fill = GridBagConstraints.BOTH;
		gbc_coinScroll.gridx = 0;
		gbc_coinScroll.gridy = 1;
		coinMainPanel.add(coinScroll, gbc_coinScroll);
		
		coinListModel = new DefaultListModel<>();
		coinList = new JList<>(coinListModel);
		coinList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		coinList.setName("coinList");
		coinScroll.setViewportView(coinList);
		coinList.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinList.addListSelectionListener(coinListSelection);
		
		coinSelectionLabel = new JLabel(" ");
		coinSelectionLabel.setName("coinSelection");
		coinSelectionLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
		GridBagConstraints gbc_coinSelectionLabel = new GridBagConstraints();
		gbc_coinSelectionLabel.ipady = 10;
		gbc_coinSelectionLabel.ipadx = 10;
		gbc_coinSelectionLabel.gridx = 0;
		gbc_coinSelectionLabel.gridy = 2;
		coinMainPanel.add(coinSelectionLabel, gbc_coinSelectionLabel);
		
		JPanel coinControlPanel = new JPanel();
		GridBagConstraints gbc_coinControlPanel = new GridBagConstraints();
		gbc_coinControlPanel.fill = GridBagConstraints.BOTH;
		gbc_coinControlPanel.gridx = 0;
		gbc_coinControlPanel.gridy = 3;
		coinPanel.add(coinControlPanel, gbc_coinControlPanel);
		GridBagLayout gbl_coinControlPanel = new GridBagLayout();
		gbl_coinControlPanel.columnWidths = new int[]{85, 85, 0};
		gbl_coinControlPanel.rowHeights = new int[]{21, 0, 0, 0};
		gbl_coinControlPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_coinControlPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		coinControlPanel.setLayout(gbl_coinControlPanel);
		
		coinDeleteButton = new JButton("Delete coin");
		coinDeleteButton.setToolTipText("Delete this coin");
		coinDeleteButton.addActionListener(coinDeleteAction);
		coinDeleteButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinDeleteButton.setEnabled(false);
		GridBagConstraints gbc_coinDeleteButton = new GridBagConstraints();
		gbc_coinDeleteButton.anchor = GridBagConstraints.EAST;
		gbc_coinDeleteButton.insets = new Insets(0, 0, 5, 5);
		gbc_coinDeleteButton.gridx = 0;
		gbc_coinDeleteButton.gridy = 0;
		coinControlPanel.add(coinDeleteButton, gbc_coinDeleteButton);
		
		coinMoveButton = new JButton("Move coin");
		coinMoveButton.setToolTipText("Change the album of this coin");
		coinMoveButton.addActionListener(coinMoveAction);
		coinMoveButton.setEnabled(false);
		coinMoveButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_coinMoveButton = new GridBagConstraints();
		gbc_coinMoveButton.anchor = GridBagConstraints.WEST;
		gbc_coinMoveButton.insets = new Insets(0, 0, 5, 0);
		gbc_coinMoveButton.gridx = 1;
		gbc_coinMoveButton.gridy = 0;
		coinControlPanel.add(coinMoveButton, gbc_coinMoveButton);
		
		JSeparator separator_1 = new JSeparator();
		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.gridwidth = 2;
		gbc_separator_1.insets = new Insets(0, 0, 5, 0);
		gbc_separator_1.gridx = 0;
		gbc_separator_1.gridy = 1;
		coinControlPanel.add(separator_1, gbc_separator_1);
		
		JPanel coinFormPanel = new JPanel();
		GridBagConstraints gbc_coinFormPanel = new GridBagConstraints();
		gbc_coinFormPanel.ipady = 10;
		gbc_coinFormPanel.ipadx = 10;
		gbc_coinFormPanel.gridwidth = 2;
		gbc_coinFormPanel.fill = GridBagConstraints.BOTH;
		gbc_coinFormPanel.gridx = 0;
		gbc_coinFormPanel.gridy = 2;
		coinControlPanel.add(coinFormPanel, gbc_coinFormPanel);
		GridBagLayout gbl_coinFormPanel = new GridBagLayout();
		gbl_coinFormPanel.columnWidths = new int[]{50, 45, 0, 120, 0};
		gbl_coinFormPanel.rowHeights = new int[]{13, 0, 0, 0, 0};
		gbl_coinFormPanel.columnWeights = new double[]{0.0, 4.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_coinFormPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		coinFormPanel.setLayout(gbl_coinFormPanel);
		
		JLabel coinFormDescriptionLabel = new JLabel("Description: ");
		coinFormDescriptionLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		coinFormDescriptionLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_coinFormDescriptionLabel = new GridBagConstraints();
		gbc_coinFormDescriptionLabel.ipady = 10;
		gbc_coinFormDescriptionLabel.ipadx = 10;
		gbc_coinFormDescriptionLabel.insets = new Insets(0, 0, 5, 5);
		gbc_coinFormDescriptionLabel.anchor = GridBagConstraints.NORTHEAST;
		gbc_coinFormDescriptionLabel.gridx = 0;
		gbc_coinFormDescriptionLabel.gridy = 0;
		coinFormPanel.add(coinFormDescriptionLabel, gbc_coinFormDescriptionLabel);
		
		coinFormDescription = new JTextField();
		coinFormDescription.setName("coinFormDescription");
		coinFormDescription.addKeyListener(coinFormButtonEnablerTextBox);
		coinFormDescription.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFormDescriptionLabel.setLabelFor(coinFormDescription);
		GridBagConstraints gbc_coinFormDescription = new GridBagConstraints();
		gbc_coinFormDescription.insets = new Insets(0, 0, 5, 5);
		gbc_coinFormDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_coinFormDescription.gridx = 1;
		gbc_coinFormDescription.gridy = 0;
		coinFormPanel.add(coinFormDescription, gbc_coinFormDescription);
		coinFormDescription.setColumns(10);
		
		JLabel coinFormGradeLabel = new JLabel("Grade: ");
		coinFormGradeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		coinFormGradeLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_coinFormGradeLabel = new GridBagConstraints();
		gbc_coinFormGradeLabel.ipadx = 10;
		gbc_coinFormGradeLabel.ipady = 10;
		gbc_coinFormGradeLabel.anchor = GridBagConstraints.EAST;
		gbc_coinFormGradeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_coinFormGradeLabel.gridx = 2;
		gbc_coinFormGradeLabel.gridy = 0;
		coinFormPanel.add(coinFormGradeLabel, gbc_coinFormGradeLabel);
		
		coinFormGrade = new JComboBox<>();
		coinFormGrade.setMinimumSize(new Dimension(105, 21));
		coinFormGrade.setPreferredSize(new Dimension(105, 21));
		coinFormGrade.setName("coinFormGrade");
		coinFormGrade.setModel(new DefaultComboBoxModel<>(Grade.values()));
		coinFormGrade.setSelectedIndex(-1);
		coinFormGrade.addItemListener(coinFormButtonEnablerComboBox);
		coinFormGrade.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFormGradeLabel.setLabelFor(coinFormGrade);
		GridBagConstraints gbc_coinFormGrade = new GridBagConstraints();
		gbc_coinFormGrade.anchor = GridBagConstraints.WEST;
		gbc_coinFormGrade.insets = new Insets(0, 0, 5, 0);
		gbc_coinFormGrade.gridx = 3;
		gbc_coinFormGrade.gridy = 0;
		coinFormPanel.add(coinFormGrade, gbc_coinFormGrade);
		
		JLabel coinFormCountryLabel = new JLabel("Country: ");
		coinFormCountryLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		coinFormCountryLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_coinFormCountryLabel = new GridBagConstraints();
		gbc_coinFormCountryLabel.ipadx = 10;
		gbc_coinFormCountryLabel.ipady = 10;
		gbc_coinFormCountryLabel.anchor = GridBagConstraints.EAST;
		gbc_coinFormCountryLabel.insets = new Insets(0, 0, 5, 5);
		gbc_coinFormCountryLabel.gridx = 0;
		gbc_coinFormCountryLabel.gridy = 1;
		coinFormPanel.add(coinFormCountryLabel, gbc_coinFormCountryLabel);
		
		coinFormCountry = new JTextField();
		coinFormCountry.setName("coinFormCountry");
		coinFormCountry.addKeyListener(coinFormButtonEnablerTextBox);
		coinFormCountry.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFormCountryLabel.setLabelFor(coinFormCountry);
		GridBagConstraints gbc_coinFormCountry = new GridBagConstraints();
		gbc_coinFormCountry.insets = new Insets(0, 0, 5, 5);
		gbc_coinFormCountry.fill = GridBagConstraints.HORIZONTAL;
		gbc_coinFormCountry.gridx = 1;
		gbc_coinFormCountry.gridy = 1;
		coinFormPanel.add(coinFormCountry, gbc_coinFormCountry);
		coinFormCountry.setColumns(10);
		
		JLabel coinFormYearLabel = new JLabel("Year: ");
		coinFormYearLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		coinFormYearLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_coinFormYearLabel = new GridBagConstraints();
		gbc_coinFormYearLabel.ipady = 10;
		gbc_coinFormYearLabel.ipadx = 10;
		gbc_coinFormYearLabel.anchor = GridBagConstraints.EAST;
		gbc_coinFormYearLabel.insets = new Insets(0, 0, 5, 5);
		gbc_coinFormYearLabel.gridx = 2;
		gbc_coinFormYearLabel.gridy = 1;
		coinFormPanel.add(coinFormYearLabel, gbc_coinFormYearLabel);
		
		coinFormYear = new JTextField();
		coinFormYear.setMinimumSize(new Dimension(105, 21));
		coinFormYear.setPreferredSize(new Dimension(105, 21));
		coinFormYear.setName("coinFormYear");
		coinFormYear.addKeyListener(coinFormButtonEnablerTextBox);
		coinFormYear.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFormYearLabel.setLabelFor(coinFormYear);
		GridBagConstraints gbc_coinFormYear = new GridBagConstraints();
		gbc_coinFormYear.anchor = GridBagConstraints.WEST;
		gbc_coinFormYear.insets = new Insets(0, 0, 5, 0);
		gbc_coinFormYear.gridx = 3;
		gbc_coinFormYear.gridy = 1;
		coinFormPanel.add(coinFormYear, gbc_coinFormYear);
		coinFormYear.setColumns(10);
		
		JLabel coinFormAlbumLabel = new JLabel("Album: ");
		coinFormAlbumLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFormAlbumLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_coinFormAlbumLabel = new GridBagConstraints();
		gbc_coinFormAlbumLabel.ipady = 10;
		gbc_coinFormAlbumLabel.ipadx = 10;
		gbc_coinFormAlbumLabel.anchor = GridBagConstraints.EAST;
		gbc_coinFormAlbumLabel.insets = new Insets(0, 0, 5, 5);
		gbc_coinFormAlbumLabel.gridx = 0;
		gbc_coinFormAlbumLabel.gridy = 2;
		coinFormPanel.add(coinFormAlbumLabel, gbc_coinFormAlbumLabel);
		
		coinFormAlbumModel = new DefaultComboBoxModel<>();
		coinFormAlbum = new JComboBox<>(coinFormAlbumModel);
		coinFormAlbum.setName("coinFormAlbum");
		coinFormAlbum.addItemListener(coinFormButtonEnablerComboBox);
		coinFormAlbum.setSelectedIndex(-1);
		coinFormAlbum.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFormAlbumLabel.setLabelFor(coinFormAlbum);
		GridBagConstraints gbc_coinFormAlbum = new GridBagConstraints();
		gbc_coinFormAlbum.insets = new Insets(0, 0, 5, 5);
		gbc_coinFormAlbum.fill = GridBagConstraints.HORIZONTAL;
		gbc_coinFormAlbum.gridx = 1;
		gbc_coinFormAlbum.gridy = 2;
		coinFormPanel.add(coinFormAlbum, gbc_coinFormAlbum);
		
		JLabel coinFormNoteLabel = new JLabel("Note: ");
		coinFormNoteLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		coinFormNoteLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_coinFormNoteLabel = new GridBagConstraints();
		gbc_coinFormNoteLabel.ipadx = 10;
		gbc_coinFormNoteLabel.ipady = 10;
		gbc_coinFormNoteLabel.anchor = GridBagConstraints.EAST;
		gbc_coinFormNoteLabel.insets = new Insets(0, 0, 5, 5);
		gbc_coinFormNoteLabel.gridx = 2;
		gbc_coinFormNoteLabel.gridy = 2;
		coinFormPanel.add(coinFormNoteLabel, gbc_coinFormNoteLabel);
		
		coinFormNote = new JTextField();
		coinFormNote.setPreferredSize(new Dimension(105, 21));
		coinFormNote.setMinimumSize(new Dimension(105, 21));
		coinFormNote.setName("coinFormNote");
		coinFormNote.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFormNoteLabel.setLabelFor(coinFormNote);
		GridBagConstraints gbc_coinFormNote = new GridBagConstraints();
		gbc_coinFormNote.anchor = GridBagConstraints.WEST;
		gbc_coinFormNote.insets = new Insets(0, 0, 5, 0);
		gbc_coinFormNote.gridx = 3;
		gbc_coinFormNote.gridy = 2;
		coinFormPanel.add(coinFormNote, gbc_coinFormNote);
		coinFormNote.setColumns(10);
		
		coinSaveButton = new JButton("Save coin");
		coinSaveButton.setEnabled(false);
		coinSaveButton.addActionListener(coinSaveAction);
		coinSaveButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinSaveButton.setToolTipText("Save coin");
		GridBagConstraints gbc_coinSaveButton = new GridBagConstraints();
		gbc_coinSaveButton.gridwidth = 4;
		gbc_coinSaveButton.gridx = 0;
		gbc_coinSaveButton.gridy = 3;
		coinFormPanel.add(coinSaveButton, gbc_coinSaveButton);
		
		JPanel statusPanel = new JPanel();
		GridBagConstraints gbc_statusPanel = new GridBagConstraints();
		gbc_statusPanel.fill = GridBagConstraints.BOTH;
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 1;
		panel.add(statusPanel, gbc_statusPanel);
		
		statusLabel = new JLabel(" ");
		statusLabel.setName("status");
		statusLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		statusPanel.add(statusLabel);
	}
}
