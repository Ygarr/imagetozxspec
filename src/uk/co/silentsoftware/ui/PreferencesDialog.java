/* Image to ZX Spec
 * Copyright (C) 2014 Silent Software (Benjamin Brown)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package uk.co.silentsoftware.ui;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import static uk.co.silentsoftware.config.PersistenceService.PREFS_FILE;
import static uk.co.silentsoftware.config.SpectrumDefaults.SPECTRUM_COLORS;
import static uk.co.silentsoftware.config.SpectrumDefaults.SPECTRUM_COLOURS_BRIGHT;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import uk.co.silentsoftware.config.BasicLoader;
import uk.co.silentsoftware.config.GigaScreenHSBOption;
import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.config.PersistenceService;
import uk.co.silentsoftware.config.ScalingObject;
import uk.co.silentsoftware.core.attributestrategy.AttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenAttributeStrategy;
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.ErrorDiffusionDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.OrderedDitherStrategy;
import uk.co.silentsoftware.core.converters.video.VLCVideoImportEngine;
import uk.co.silentsoftware.core.converters.video.VideoImportEngine;
import uk.co.silentsoftware.dispatcher.WorkDispatcher;
import uk.co.silentsoftware.ui.listener.DitherChangedListener;

/**
 * The options selection dialog
 *
 * Note that this class is tied quite tightly to
 * business logic (e.g. slider ranges/magic numbers) 
 * really just because there isn't need to do
 * the separation for a program this small or at this
 * point (although admittedly its bad practice).
 * Feel free to change this however....
 */
public class PreferencesDialog extends JFrame  {

	private static final long serialVersionUID = 1L;
	
	private final List<DitherChangedListener> ditherChangedListeners = new ArrayList<>();
	
	private final JFrame currentInstance;
	
	/**
	 * Default constructor that initialises all the tabs
	 */
	public PreferencesDialog() {
		
		// Initialise options
		OptionsObject.getInstance();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			// Pah just ignore this error, we'll just have a naff looking UI
		}
		
		// Set up the menu bar
		setIconImage(ImageToZxSpec.IMAGE_ICON.getImage());
		setTitle(getCaption("tab_item_control_panel"));
		setSize(600,480);
	    setLocationRelativeTo(null); 
	    setResizable(false);
		JTabbedPane pane = new JTabbedPane();
		pane.addTab(getCaption("tab_item_misc_options"), createGeneralOptions());
		pane.addTab(getCaption("tab_item_pre_process_options"), createPreProcessOptions());
		pane.addTab(getCaption("tab_item_dither_options"), createDitherOptions());
		pane.addTab(getCaption("tab_item_advanced_options"), createAdvancedOptions());
		getContentPane().add(pane);
		currentInstance = this;
	}
	
	/**
	 * Method that adds the pre process options tab and
	 * its action listeners.
	 * 
	 * TODO: Note this is method is too tightly bound to 
	 * the options and sets hard coded ranges. :(
	 * 
	 * @return
	 */
	private JPanel createPreProcessOptions() {
		final OptionsObject oo = OptionsObject.getInstance();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(5,2));
		JLabel label = new JLabel(getCaption("pp_scaling"), JLabel.CENTER);
		final JPanel scalingPadding = new JPanel(new GridLayout(3,1));
		
		final JComboBox<ScalingObject> scaling = new JComboBox<ScalingObject>(oo.getScalings());
		scaling.setSelectedItem(oo.getScaling());
		scaling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setScaling((ScalingObject)scaling.getSelectedItem());
				PersistenceService.save(oo, PREFS_FILE);
			}
			
		});
		panel.add(label);
		// TODO: this is a bit lame and Java 1.1 style using padding like this
		scalingPadding.add(new JPanel());
		scalingPadding.add(scaling);
		scalingPadding.add(new JPanel());
		panel.add(scalingPadding);
		label = new JLabel(getCaption("pp_video_rate"), JLabel.CENTER);
		final JTextField sampleRate = new JTextField();
		final JPanel samplePadding = new JPanel(new GridLayout(3,1));
		sampleRate.setHorizontalAlignment(JTextField.RIGHT);
		sampleRate.setText(""+oo.getVideoFramesPerSecond());
		sampleRate.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e)
			{
				String value = sampleRate.getText();
				if (value != null && value.trim().length() > 0) {
					try {
						double d = Double.parseDouble(value);
						if (d > 0) {
							oo.setVideoFramesPerSecond(d);
							return;
						}
					} catch (NumberFormatException nfe) {}
				}
				sampleRate.setText(""+oo.getVideoFramesPerSecond());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		samplePadding.add(new JPanel());
		samplePadding.add(sampleRate);
		samplePadding.add(new JPanel());
		panel.add(samplePadding);
		label = new JLabel(getCaption("pp_saturation"), JLabel.CENTER);
		final JSlider satSlider = new JSlider(-100, 100);
		satSlider.setMajorTickSpacing(25);
		satSlider.setPaintTicks(true);
		satSlider.setPaintLabels(true);
		satSlider.setLabelTable(satSlider.createStandardLabels(25));
		satSlider.setValue(Math.round(oo.getSaturation()*100f));
		satSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				oo.setSaturation(satSlider.getValue()/100f);
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(satSlider);
		label = new JLabel(getCaption("pp_contrast"), JLabel.CENTER);
		final JSlider contrastSlider = new JSlider(-100, 100);
		contrastSlider.setMajorTickSpacing(25);
		contrastSlider.setPaintTicks(true);
		contrastSlider.setPaintLabels(true);
		contrastSlider.setLabelTable(contrastSlider.createStandardLabels(25));
		contrastSlider.setValue(Math.round(((float)oo.getContrast()-1)*100f));
		contrastSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				oo.setContrast((float)((contrastSlider.getValue()/100f)+1));
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(contrastSlider);
		label = new JLabel(getCaption("pp_brightness"), JLabel.CENTER);
		final JSlider brightnessSlider = new JSlider(-100, 100);
		brightnessSlider.setMajorTickSpacing(25);
		brightnessSlider.setPaintTicks(true);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.setLabelTable(brightnessSlider.createStandardLabels(25));
		brightnessSlider.setValue(Math.round(((float)oo.getBrightness()/2.56f)));
		brightnessSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				oo.setBrightness((float)(brightnessSlider.getValue()*2.56f));
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(brightnessSlider);
		return panel;
	}
	
	private JPanel createAdvancedOptions() {
		final OptionsObject oo = OptionsObject.getInstance();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(6,2));
		JLabel label = new JLabel(getCaption("adv_serpentine"), JLabel.CENTER);
		final JCheckBox serpentine = new JCheckBox();
		serpentine.setSelected(oo.getSerpentine());
		serpentine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setSerpentine(serpentine.isSelected());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(serpentine);
		label = new JLabel(getCaption("adv_constrain_diffusion"), JLabel.CENTER);
		final JCheckBox constrain = new JCheckBox();
		constrain.setSelected(oo.getConstrainedErrorDiffusion());
		constrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setConstrainedErrorDiffusion(constrain.isSelected());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(constrain);
		
		label = new JLabel(getCaption("adv_prefer_detail"), JLabel.CENTER);
		final JCheckBox preferDetail= new JCheckBox();
		preferDetail.setSelected(oo.getPreferDetail());
		preferDetail.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setPreferDetail(preferDetail.isSelected());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(preferDetail);
		label = new JLabel(getCaption("adv_scr_hsb_order"), JLabel.CENTER);
		final JComboBox<GigaScreenHSBOption> hsbOptions = new JComboBox<GigaScreenHSBOption>(oo.getGigaScreenHsbOptions());
		hsbOptions.setSelectedItem(oo.getGigaScreenHsbOption());
		hsbOptions.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				oo.setGigaScreenHsbOption((GigaScreenHSBOption)hsbOptions.getSelectedItem());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(hsbOptions);
		label = new JLabel(getCaption("adv_dither_intensity"), JLabel.CENTER);
		final JSlider ditherSlider = new JSlider(1, 10);
		ditherSlider.setMajorTickSpacing(1);
		ditherSlider.setPaintTicks(true);
		ditherSlider.setPaintLabels(true);
		ditherSlider.setSnapToTicks(true);
		ditherSlider.setLabelTable(ditherSlider.createStandardLabels(1));
		ditherSlider.setValue(11-oo.getOrderedDitherIntensity());
		ditherSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				oo.setOrderedDitherIntensity(11-ditherSlider.getValue());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(ditherSlider);
		
		label = new JLabel(getCaption("adv_video_import_engine"), JLabel.CENTER);
		final JComboBox<VideoImportEngine> importEngine = new JComboBox<VideoImportEngine>(oo.getVideoImportEngines());
		importEngine.setSelectedItem(oo.getVideoImportEngine());
		importEngine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (((VideoImportEngine)importEngine.getSelectedItem()) instanceof VLCVideoImportEngine) {
					try {
						JFileChooser jfc = new JFileChooser();
						jfc.setDialogTitle(getCaption("dialog_choose_vlc_folder"));
						jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(null)) {
							oo.setPathToVlc(jfc.getSelectedFile().getAbsolutePath());
							oo.initVideoImportEngine();
							JOptionPane.showMessageDialog(null, getCaption("adv_video_vlc_success"), getCaption("adv_video_vlc_success_title"), JOptionPane.INFORMATION_MESSAGE);
						} else {
							throw new IllegalArgumentException();
						}
					} catch (Throwable t) {
						JOptionPane.showMessageDialog(null, getCaption("adv_video_vlc_fail"), getCaption("adv_video_vlc_fail_title"), JOptionPane.WARNING_MESSAGE);
						
						// TODO: Fix this dirty hack - I just need to set a safe default
						importEngine.setSelectedItem(oo.getVideoImportEngines()[0]);
						return;
					}
				}
				oo.setVideoImportEngine((VideoImportEngine)importEngine.getSelectedItem());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		final JPanel importPadding = new JPanel(new GridLayout(3,1));
		importPadding.add(new JPanel());
		importPadding.add(importEngine);
		importPadding.add(new JPanel());
		panel.add(label);
		panel.add(importPadding);

		return panel;
	}
	
	/**
	 * Method that adds the dither options tab and
	 * its action listeners.
	 * 
	 * TODO: Note this is method is too tightly bound to 
	 * the options and sets hard coded ranges.
	 */
	private JPanel createDitherOptions() {
		final OptionsObject oo = OptionsObject.getInstance();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(7,2));
		final JComboBox<DitherStrategy> dithers = new JComboBox<DitherStrategy>();
		createDitherComboBox(dithers, new DitherChangedListener() {

			@Override
			public void ditherSelected(DitherStrategy dither) {
				dithers.setSelectedItem(dither);		
			}
		});
		JLabel label = new JLabel(getCaption("dit_dithering_mode"), JLabel.CENTER);
		panel.add(label);
		panel.add(dithers);
		label = new JLabel(getCaption("dit_colour_mode"), JLabel.CENTER);
		final JComboBox<ColourChoiceStrategy> colourModes = new JComboBox<>(oo.getColourModes());
		colourModes.setSelectedItem(oo.getColourMode());
		colourModes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ColourChoiceStrategy ccs = (ColourChoiceStrategy)colourModes.getSelectedItem();
				oo.setColourMode(ccs);
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(colourModes);	
		
		label = new JLabel(getCaption("dit_attribute_fav"), JLabel.CENTER);
		final JComboBox<AttributeStrategy> attributeModes = new JComboBox<>(oo.getAttributeModes());
		attributeModes.setSelectedItem(oo.getAttributeMode());
		attributeModes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setAttributeMode((AttributeStrategy)attributeModes.getSelectedItem());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(attributeModes);
		
		label = new JLabel(getCaption("dit_giga_attribute_fav"), JLabel.CENTER);
		final JComboBox<GigaScreenAttributeStrategy> gigaScreenAttributeModes = new JComboBox<>(oo.getGigaScreenAttributeStrategies());
		gigaScreenAttributeModes.setSelectedItem(oo.getGigaScreenAttributeStrategy());
		gigaScreenAttributeModes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setGigaScreenAttributeStrategy((GigaScreenAttributeStrategy)gigaScreenAttributeModes.getSelectedItem());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(gigaScreenAttributeModes);
			
		label = new JLabel(getCaption("dit_mono_ink_colour"), JLabel.CENTER);
		panel.add(label);
		final JButton ink = new JButton(getCaption("dit_click_ink"));
		ink.setOpaque(true);
		ink.setForeground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[oo.getMonochromeInkIndex()]));
		ink.setBackground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[oo.getMonochromeInkIndex()]));
		ink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int newIndex = oo.getMonochromeInkIndex()+1;
				if (newIndex >= SPECTRUM_COLOURS_BRIGHT.length) {
					newIndex = 0;
				}
				ink.setForeground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[newIndex]));
				ink.setBackground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[newIndex]));
				oo.setMonochromeInkIndex(newIndex);
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		
		panel.add(ink);
		label = new JLabel(getCaption("dit_mono_paper_colour"), JLabel.CENTER);
		panel.add(label);
		final JButton paper = new JButton(getCaption("dit_click_paper"));
		paper.setOpaque(true);
		paper.setForeground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[oo.getMonochromePaperIndex()]));
		paper.setBackground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[oo.getMonochromePaperIndex()]));
		paper.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int newIndex = oo.getMonochromePaperIndex()+1;
				if (newIndex >= SPECTRUM_COLOURS_BRIGHT.length) {
					newIndex = 0;
				}
				paper.setBackground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[newIndex]));
				paper.setForeground(SPECTRUM_COLORS.get(SPECTRUM_COLOURS_BRIGHT[newIndex]));
				oo.setMonochromePaperIndex(newIndex);
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(paper);
		label = new JLabel(getCaption("dit_threshold"), JLabel.CENTER);
		final JSlider thresholdSlider = new JSlider(0, 768);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setPaintLabels(true);
		thresholdSlider.setLabelTable(thresholdSlider.createStandardLabels(128));
		thresholdSlider.setMajorTickSpacing(128);
		thresholdSlider.setValue(oo.getBlackThreshold());
		thresholdSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				oo.setBlackThreshold(thresholdSlider.getValue());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(label);
		panel.add(thresholdSlider);				
		
		return panel;
	}
	
	void createDitherComboBox(final JComboBox<DitherStrategy> dithers, final DitherChangedListener ditherChangedListener) {
		ditherChangedListeners.add(ditherChangedListener);
		final OptionsObject oo = OptionsObject.getInstance();
		Vector<DitherStrategy> v = new Vector<>();
		v.addAll(Arrays.asList(oo.getErrorDithers()));
		v.addAll(Arrays.asList(oo.getOrderedDithers()));
		v.add(oo.getCharacterDitherStrategy());
		
		dithers.setModel(new DefaultComboBoxModel<>(v));
		dithers.setSelectedItem(oo.getSelectedDitherStrategy());
		dithers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				DitherStrategy obj = (DitherStrategy)dithers.getSelectedItem();
				if (obj instanceof ErrorDiffusionDitherStrategy) {
					oo.setErrorDitherStrategy((ErrorDiffusionDitherStrategy)obj);
				} else if (obj instanceof OrderedDitherStrategy){
					oo.setOrderedDitherStrategy((OrderedDitherStrategy)dithers.getSelectedItem());
				}
				oo.setSelectedDitherStrategy(obj);
				PersistenceService.save(oo, PREFS_FILE);
				for (DitherChangedListener dcl : ditherChangedListeners) {
					if (dcl != ditherChangedListener) {
						dcl.ditherSelected(obj);
					}
				}
			}
		});
	}
	
	/**
	 * Method that adds the misc/output options tab and
	 * its action listeners. A file dialog also populates
	 * the custom tape loader.
	 * 
	 * TODO: Note this is method is too tightly bound to 
	 * the options and sets hard coded ranges. :(
	 */
	private JPanel createGeneralOptions() {
		final OptionsObject oo = OptionsObject.getInstance();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(11,2));
		JLabel label = new JLabel(getCaption("misc_image_output"), JLabel.CENTER);
		panel.add(label);
		final JComboBox<String> formatsBox = new JComboBox<String>(oo.getImageFormats());
		formatsBox.setSelectedItem(oo.getImageFormat());
		formatsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setImageFormat((String)formatsBox.getSelectedItem());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(formatsBox);
		label = new JLabel(getCaption("misc_tap_loader"), JLabel.CENTER);
		panel.add(label);
		final JComboBox<BasicLoader> loadersBox = new JComboBox<BasicLoader>(oo.getBasicLoaders());
		loadersBox.setSelectedItem(oo.getBasicLoader());
		loadersBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				BasicLoader loader = (BasicLoader)loadersBox.getSelectedItem();
				oo.setBasicLoader(loader);
				if (oo.getBasicLoader().toString().startsWith(OptionsObject.CUSTOM_LOADER_PREFIX)) {
					JFileChooser jfc = new JFileChooser(){
						static final long serialVersionUID = 1L;
						public void approveSelection() {
							for (File f:this.getSelectedFiles()) {
								if (f.isDirectory()) {
									return;
								}
							}
							super.approveSelection();
						}
					};
					jfc.setDialogTitle(getCaption("misc_choose_loader"));
					jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					jfc.setAcceptAllFileFilterUsed(false);
					jfc.setFileFilter(new FileFilter() {
						
						public String getDescription() {
							return getCaption("misc_tap_desc");
						}
						public boolean accept(File f) {
							String name = f.getAbsolutePath().toLowerCase();
							return (f.isDirectory() || name.endsWith(".tap")); 
						}
					});
					jfc.setMultiSelectionEnabled(false);
					if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(currentInstance)) {
						File file = jfc.getSelectedFile();
						try{
							loader.setName(OptionsObject.CUSTOM_LOADER_PREFIX+"("+file.getName()+")");
							loader.setPath(file.getCanonicalPath());
						} catch(IOException io){
							return;
						}					
					}
				}
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(loadersBox);
		label = new JLabel(getCaption("misc_threads_per_cpu"), JLabel.CENTER);
		panel.add(label);
		final JComboBox<Integer> cpuThreads = new JComboBox<Integer>();
		for (int i=1; i<6; i++) {
			cpuThreads.addItem(i);
		}
		cpuThreads.setSelectedItem(oo.getThreadsPerCPU());
		cpuThreads.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setThreadsPerCPU((Integer)cpuThreads.getSelectedItem());
				WorkDispatcher.setThreadsPerCPU(oo.getThreadsPerCPU());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(cpuThreads);
		label = new JLabel(getCaption("misc_show_fps"), JLabel.CENTER);
		panel.add(label);
		final JCheckBox fpsCheckBox = new JCheckBox("", oo.getFpsCounter());
		fpsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setFpsCounter(fpsCheckBox.isSelected());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(fpsCheckBox);
		label = new JLabel(getCaption("misc_show_wip"), JLabel.CENTER);
		panel.add(label);
		final JCheckBox wipPreviewCheckBox = new JCheckBox("", oo.getShowWipPreview());
		wipPreviewCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setShowWipPreview(wipPreviewCheckBox.isSelected());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(wipPreviewCheckBox);
		panel.add(new JLabel(getCaption("misc_output_options"), JLabel.CENTER));
		final JCheckBox scrCheckBox = new JCheckBox(getCaption("misc_scr_output"), oo.getExportScreen());
		scrCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setExportScreen(scrCheckBox.isSelected());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(scrCheckBox);
		panel.add(new JPanel());
		final JCheckBox tapeCheckBox = new JCheckBox(getCaption("misc_tap_output"), oo.getExportTape());
		tapeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setExportTape(tapeCheckBox.isSelected());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(tapeCheckBox);
		panel.add(new JPanel());
		final JCheckBox textCheckBox = new JCheckBox(getCaption("misc_text_output"), oo.getExportText());
		textCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setExportText(textCheckBox.isSelected());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(textCheckBox);		
		panel.add(new JPanel());
		final JCheckBox imageCheckBox = new JCheckBox(getCaption("misc_image_output"), oo.getExportImage());
		imageCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setExportImage(imageCheckBox.isSelected());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(imageCheckBox);
		panel.add(new JPanel());
		final JCheckBox animGifCheckBox = new JCheckBox(getCaption("misc_gif_output"), oo.getExportAnimGif());
		animGifCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				oo.setExportAnimGif(animGifCheckBox.isSelected());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(animGifCheckBox);
		panel.add(new JLabel(getCaption("misc_gif_delay"), JLabel.CENTER));
		final JTextField gifDelay = new JTextField();
		gifDelay.setHorizontalAlignment(JTextField.RIGHT);
		gifDelay.setText(""+oo.getGifDisplayTimeMillis());
		gifDelay.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String value = gifDelay.getText();
				if (value != null && value.trim().length() > 0) {
					try {
						int d = Integer.parseInt(value);
						if (d > 0) {
							oo.setGifDisplayTimeMillis(d);
							return;
						}
					} catch (NumberFormatException nfe) {}
				}
				gifDelay.setText(""+oo.getGifDisplayTimeMillis());
				PersistenceService.save(oo, PREFS_FILE);
			}
		});
		panel.add(gifDelay);
		return panel;
	}
}
