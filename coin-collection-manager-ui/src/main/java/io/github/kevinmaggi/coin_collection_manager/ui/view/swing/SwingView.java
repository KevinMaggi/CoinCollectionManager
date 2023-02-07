package io.github.kevinmaggi.coin_collection_manager.ui.view.swing;

import java.util.List;

import javax.swing.JFrame;

import io.github.kevinmaggi.coin_collection_manager.core.model.Album;
import io.github.kevinmaggi.coin_collection_manager.core.model.Coin;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.AlbumPresenter;
import io.github.kevinmaggi.coin_collection_manager.ui.presenter.CoinPresenter;
import io.github.kevinmaggi.coin_collection_manager.ui.view.View;
import java.awt.GridLayout;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import io.github.kevinmaggi.coin_collection_manager.core.model.Grade;
import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.ListSelectionModel;

public class SwingView extends JFrame implements View {
	public SwingView() {
		setMinimumSize(new Dimension(960, 640));
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
		gbl_albumPanel.columnWidths = new int[]{0, 0};
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
		gbl_albumSearchPanel.columnWidths = new int[]{0, 136, 94, 110, 90, 0};
		gbl_albumSearchPanel.rowHeights = new int[]{0, 0};
		gbl_albumSearchPanel.columnWeights = new double[]{0.0, 3.0, 0.0, 0.0, 2.0, Double.MIN_VALUE};
		gbl_albumSearchPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		albumSearchPanel.setLayout(gbl_albumSearchPanel);
		
		JLabel albumSearchNameLabel = new JLabel("Key name: ");
		albumSearchNameLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		albumSearchNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumSearchNameLabel = new GridBagConstraints();
		gbc_albumSearchNameLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_albumSearchNameLabel.ipady = 10;
		gbc_albumSearchNameLabel.ipadx = 10;
		gbc_albumSearchNameLabel.insets = new Insets(0, 0, 0, 5);
		gbc_albumSearchNameLabel.gridx = 0;
		gbc_albumSearchNameLabel.gridy = 0;
		albumSearchPanel.add(albumSearchNameLabel, gbc_albumSearchNameLabel);
		
		albumSearchName = new JTextField();
		albumSearchName.setName("albumSearchNameValue");
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
		gbc_albumSearchVolumeLabel.ipadx = 10;
		gbc_albumSearchVolumeLabel.insets = new Insets(0, 0, 0, 5);
		gbc_albumSearchVolumeLabel.gridx = 2;
		gbc_albumSearchVolumeLabel.gridy = 0;
		albumSearchPanel.add(albumSearchVolumeLabel, gbc_albumSearchVolumeLabel);
		
		albumSearchVolume = new JTextField();
		albumSearchVolume.setName("albumSearchVolumeValue");
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
		
		JButton albumSearchButton = new JButton("Search");
		albumSearchButton.setToolTipText("Search the album");
		albumSearchButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumSearchButton = new GridBagConstraints();
		gbc_albumSearchButton.gridx = 4;
		gbc_albumSearchButton.gridy = 0;
		albumSearchPanel.add(albumSearchButton, gbc_albumSearchButton);
		
		JPanel albumMainPanel = new JPanel();
		GridBagConstraints gbc_albumMainPanel = new GridBagConstraints();
		gbc_albumMainPanel.insets = new Insets(0, 0, 5, 0);
		gbc_albumMainPanel.fill = GridBagConstraints.BOTH;
		gbc_albumMainPanel.gridx = 0;
		gbc_albumMainPanel.gridy = 2;
		albumPanel.add(albumMainPanel, gbc_albumMainPanel);
		GridBagLayout gbl_albumMainPanel = new GridBagLayout();
		gbl_albumMainPanel.columnWidths = new int[]{226, 0};
		gbl_albumMainPanel.rowHeights = new int[]{15, 0, 0};
		gbl_albumMainPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_albumMainPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		albumMainPanel.setLayout(gbl_albumMainPanel);
		
		albumMainActualLabel = new JLabel(" ");
		albumMainActualLabel.setName("albumMainActualValue");
		albumMainActualLabel.setHorizontalAlignment(SwingConstants.CENTER);
		albumMainActualLabel.setFont(new Font("Tahoma", Font.ITALIC, 12));
		GridBagConstraints gbc_albumMainActualLabel = new GridBagConstraints();
		gbc_albumMainActualLabel.ipady = 10;
		gbc_albumMainActualLabel.insets = new Insets(0, 0, 5, 0);
		gbc_albumMainActualLabel.gridx = 0;
		gbc_albumMainActualLabel.gridy = 0;
		albumMainPanel.add(albumMainActualLabel, gbc_albumMainActualLabel);
		
		JScrollPane albumMainScroll = new JScrollPane();
		GridBagConstraints gbc_albumMainScroll = new GridBagConstraints();
		gbc_albumMainScroll.fill = GridBagConstraints.BOTH;
		gbc_albumMainScroll.gridx = 0;
		gbc_albumMainScroll.gridy = 1;
		albumMainPanel.add(albumMainScroll, gbc_albumMainScroll);
		
		albumMainList = new JList<>();
		albumMainList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		albumMainList.setName("albumMainList");
		albumMainList.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumMainScroll.setViewportView(albumMainList);
		
		JPanel albumControlPanel = new JPanel();
		GridBagConstraints gbc_albumControlPanel = new GridBagConstraints();
		gbc_albumControlPanel.fill = GridBagConstraints.BOTH;
		gbc_albumControlPanel.gridx = 0;
		gbc_albumControlPanel.gridy = 3;
		albumPanel.add(albumControlPanel, gbc_albumControlPanel);
		GridBagLayout gbl_albumControlPanel = new GridBagLayout();
		gbl_albumControlPanel.columnWidths = new int[]{65, 51, 0};
		gbl_albumControlPanel.rowHeights = new int[]{23, 0, 0, 0};
		gbl_albumControlPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_albumControlPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		albumControlPanel.setLayout(gbl_albumControlPanel);
		
		JButton albumControlDeleteButton = new JButton("Delete album");
		albumControlDeleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		albumControlDeleteButton.setToolTipText("Delete this album");
		albumControlDeleteButton.setEnabled(false);
		albumControlDeleteButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumControlDeleteButton = new GridBagConstraints();
		gbc_albumControlDeleteButton.anchor = GridBagConstraints.EAST;
		gbc_albumControlDeleteButton.insets = new Insets(0, 0, 5, 5);
		gbc_albumControlDeleteButton.gridx = 0;
		gbc_albumControlDeleteButton.gridy = 0;
		albumControlPanel.add(albumControlDeleteButton, gbc_albumControlDeleteButton);
		
		JButton albumControlMoveButton = new JButton("Move album");
		albumControlMoveButton.setToolTipText("Change the location of this album");
		albumControlMoveButton.setEnabled(false);
		albumControlMoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		albumControlMoveButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumControlMoveButton = new GridBagConstraints();
		gbc_albumControlMoveButton.anchor = GridBagConstraints.WEST;
		gbc_albumControlMoveButton.insets = new Insets(0, 0, 5, 0);
		gbc_albumControlMoveButton.gridx = 1;
		gbc_albumControlMoveButton.gridy = 0;
		albumControlPanel.add(albumControlMoveButton, gbc_albumControlMoveButton);
		
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
		albumFormName.setName("albumFormNameValue");
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
		albumFormVolume.setName("albumFormVolumeValue");
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
		albumFormLocation.setName("albumFormLocationValue");
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
		albumFormSlots.setName("albumFormSlotsValue");
		albumFormSlots.setFont(new Font("Tahoma", Font.PLAIN, 12));
		albumFormSlotsLabel.setLabelFor(albumFormSlots);
		GridBagConstraints gbc_albumFormSlots = new GridBagConstraints();
		gbc_albumFormSlots.anchor = GridBagConstraints.WEST;
		gbc_albumFormSlots.insets = new Insets(0, 0, 5, 0);
		gbc_albumFormSlots.gridx = 3;
		gbc_albumFormSlots.gridy = 1;
		albumFormPanel.add(albumFormSlots, gbc_albumFormSlots);
		albumFormSlots.setColumns(10);
		
		JButton albumFormSaveButton = new JButton("Save album");
		albumFormSaveButton.setToolTipText("Save album");
		albumFormSaveButton.setEnabled(false);
		albumFormSaveButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_albumFormSaveButton = new GridBagConstraints();
		gbc_albumFormSaveButton.gridwidth = 4;
		gbc_albumFormSaveButton.gridx = 0;
		gbc_albumFormSaveButton.gridy = 2;
		albumFormPanel.add(albumFormSaveButton, gbc_albumFormSaveButton);
		
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
		gbl_coinFilterPanel.columnWidths = new int[]{56, 96, 85, 0};
		gbl_coinFilterPanel.rowHeights = new int[]{21, 0};
		gbl_coinFilterPanel.columnWeights = new double[]{0.0, 3.0, 2.0, Double.MIN_VALUE};
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
		coinFilterDescription.setName("coinFilterDescriptionValue");
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
		
		JButton btnNewButton = new JButton("Filter");
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnNewButton.setToolTipText("Filter coins");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.NORTH;
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 0;
		coinFilterPanel.add(btnNewButton, gbc_btnNewButton);
		
		JPanel coinMainPanel = new JPanel();
		GridBagConstraints gbc_coinMainPanel = new GridBagConstraints();
		gbc_coinMainPanel.insets = new Insets(0, 0, 5, 0);
		gbc_coinMainPanel.fill = GridBagConstraints.BOTH;
		gbc_coinMainPanel.gridx = 0;
		gbc_coinMainPanel.gridy = 2;
		coinPanel.add(coinMainPanel, gbc_coinMainPanel);
		GridBagLayout gbl_coinMainPanel = new GridBagLayout();
		gbl_coinMainPanel.columnWidths = new int[]{45, 0};
		gbl_coinMainPanel.rowHeights = new int[]{13, 0, 0};
		gbl_coinMainPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_coinMainPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		coinMainPanel.setLayout(gbl_coinMainPanel);
		
		coinMainActualLabel = new JLabel(" ");
		coinMainActualLabel.setName("coinMainActualValue");
		coinMainActualLabel.setFont(new Font("Tahoma", Font.ITALIC, 12));
		GridBagConstraints gbc_coinMainActualLabel = new GridBagConstraints();
		gbc_coinMainActualLabel.insets = new Insets(0, 0, 5, 0);
		gbc_coinMainActualLabel.ipady = 10;
		gbc_coinMainActualLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_coinMainActualLabel.gridx = 0;
		gbc_coinMainActualLabel.gridy = 0;
		coinMainPanel.add(coinMainActualLabel, gbc_coinMainActualLabel);
		
		JScrollPane coinMainScroll = new JScrollPane();
		GridBagConstraints gbc_coinMainScroll = new GridBagConstraints();
		gbc_coinMainScroll.fill = GridBagConstraints.BOTH;
		gbc_coinMainScroll.gridx = 0;
		gbc_coinMainScroll.gridy = 1;
		coinMainPanel.add(coinMainScroll, gbc_coinMainScroll);
		
		coinMainList = new JList<>();
		coinMainList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		coinMainList.setName("coinMainList");
		coinMainScroll.setViewportView(coinMainList);
		coinMainList.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
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
		
		JButton coinControlDeleteButton = new JButton("Delete coin");
		coinControlDeleteButton.setToolTipText("Delete this coin");
		coinControlDeleteButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinControlDeleteButton.setEnabled(false);
		GridBagConstraints gbc_coinControlDeleteButton = new GridBagConstraints();
		gbc_coinControlDeleteButton.anchor = GridBagConstraints.EAST;
		gbc_coinControlDeleteButton.insets = new Insets(0, 0, 5, 5);
		gbc_coinControlDeleteButton.gridx = 0;
		gbc_coinControlDeleteButton.gridy = 0;
		coinControlPanel.add(coinControlDeleteButton, gbc_coinControlDeleteButton);
		
		JButton coinControlMoveButton = new JButton("Move coin");
		coinControlMoveButton.setToolTipText("Change the album of this coin");
		coinControlMoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		coinControlMoveButton.setEnabled(false);
		coinControlMoveButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_coinControlMoveButton = new GridBagConstraints();
		gbc_coinControlMoveButton.anchor = GridBagConstraints.WEST;
		gbc_coinControlMoveButton.insets = new Insets(0, 0, 5, 0);
		gbc_coinControlMoveButton.gridx = 1;
		gbc_coinControlMoveButton.gridy = 0;
		coinControlPanel.add(coinControlMoveButton, gbc_coinControlMoveButton);
		
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
		coinFormDescription.setName("coinFormDescriptionValue");
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
		
		JComboBox<Grade> coinFormGrade = new JComboBox<>();
		coinFormGrade.setPreferredSize(new Dimension(105, 21));
		coinFormGrade.setName("coinFormGradeValue");
		coinFormGrade.setModel(new DefaultComboBoxModel<>(Grade.values()));
		coinFormGrade.setSelectedIndex(-1);
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
		coinFormCountry.setName("coinFormCountryValue");
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
		coinFormYear.setName("coinFormYearValue");
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
		
		JComboBox<Album> coinFormAlbum = new JComboBox<>();
		coinFormAlbum.setName("coinFormAlbumValue");
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
		coinFormNote.setName("coinFormNoteValue");
		coinFormNote.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFormNoteLabel.setLabelFor(coinFormNote);
		GridBagConstraints gbc_coinFormNote = new GridBagConstraints();
		gbc_coinFormNote.anchor = GridBagConstraints.WEST;
		gbc_coinFormNote.insets = new Insets(0, 0, 5, 0);
		gbc_coinFormNote.gridx = 3;
		gbc_coinFormNote.gridy = 2;
		coinFormPanel.add(coinFormNote, gbc_coinFormNote);
		coinFormNote.setColumns(10);
		
		JButton coinFormSaveButton = new JButton("Save coin");
		coinFormSaveButton.setEnabled(false);
		coinFormSaveButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		coinFormSaveButton.setToolTipText("Save coin");
		GridBagConstraints gbc_coinFormSaveButton = new GridBagConstraints();
		gbc_coinFormSaveButton.gridwidth = 4;
		gbc_coinFormSaveButton.gridx = 0;
		gbc_coinFormSaveButton.gridy = 3;
		coinFormPanel.add(coinFormSaveButton, gbc_coinFormSaveButton);
		
		JPanel statusPanel = new JPanel();
		GridBagConstraints gbc_statusPanel = new GridBagConstraints();
		gbc_statusPanel.fill = GridBagConstraints.BOTH;
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 1;
		panel.add(statusPanel, gbc_statusPanel);
		
		statusLabel = new JLabel(" ");
		statusLabel.setName("statusValue");
		statusLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		statusPanel.add(statusLabel);
	}
	private static final long serialVersionUID = 1L;
	
	private AlbumPresenter albumPresenter;
	private CoinPresenter coinPresenter;
	
	private JTextField albumSearchName;
	private JTextField albumSearchVolume;
	private JTextField coinFilterDescription;
	private JList<Album> albumMainList;
	private JLabel albumMainActualLabel;
	private JLabel coinMainActualLabel;
	private JList<Coin> coinMainList;
	private JLabel statusLabel;
	private JTextField albumFormName;
	private JTextField albumFormVolume;
	private JTextField albumFormLocation;
	private JTextField albumFormSlots;
	private JTextField coinFormDescription;
	private JTextField coinFormCountry;
	private JTextField coinFormYear;
	private JTextField coinFormNote;
	
	// TODO remove
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					SwingView frame = new SwingView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void setPresenters(CoinPresenter coinPresenter, AlbumPresenter albumPresenter) {
		this.coinPresenter = coinPresenter;
		this.albumPresenter = albumPresenter;
		
		coinPresenter.getAllCoins();
		albumPresenter.getAllAlbums();
	}

	@Override
	public void showAllAlbums(List<Album> albums) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showSearchedAlbums(List<Album> albums, String search) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showAlbum(Album album) {
		// TODO Auto-generated method stub

	}

	@Override
	public void albumAdded(Album album) {
		// TODO Auto-generated method stub

	}

	@Override
	public void albumDeleted(Album album) {
		// TODO Auto-generated method stub

	}

	@Override
	public void albumMoved(Album album) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showAllCoins(List<Coin> coins) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showSearchedCoins(List<Coin> coins, String search) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showCoinsInAlbum(List<Coin> coins, Album album) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showCoin(Coin coin, Album album) {
		// TODO Auto-generated method stub

	}

	@Override
	public void coinAdded(Coin coin) {
		// TODO Auto-generated method stub

	}

	@Override
	public void coinDeleted(Coin coin) {
		// TODO Auto-generated method stub

	}

	@Override
	public void coinMoved(Coin coin, Album oldAlbum, Album newAlbum) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showError(String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showSuccess(String msg) {
		// TODO Auto-generated method stub

	}

}
