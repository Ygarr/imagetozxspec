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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.media.NoPlayerException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import uk.co.silentsoftware.config.OptionsObject;
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.ResultImage;
import uk.co.silentsoftware.core.converters.spectrum.ScrConverter;
import uk.co.silentsoftware.core.converters.spectrum.TapeConverter;
import uk.co.silentsoftware.core.converters.spectrum.TextConverter;
import uk.co.silentsoftware.core.converters.video.GifConverter;
import uk.co.silentsoftware.core.helpers.ImageHelper;
import uk.co.silentsoftware.core.helpers.SaveHelper;
import uk.co.silentsoftware.dispatcher.WorkContainer;
import uk.co.silentsoftware.dispatcher.WorkDispatcher;
import uk.co.silentsoftware.ui.listener.AboutListener;
import uk.co.silentsoftware.ui.listener.CustomDropTargetListener;
import uk.co.silentsoftware.ui.listener.DitherChangedListener;
import uk.co.silentsoftware.ui.listener.FileInputListener;
import uk.co.silentsoftware.ui.listener.FileOutputListener;
import uk.co.silentsoftware.ui.listener.FileReadyListener;
import uk.co.silentsoftware.ui.listener.OperationFinishedListener;

/**
 * Very simple and noddy image to zx spectrum compatible
 * image converter.
 */
public class ImageToZxSpec {

	public static final ImageIcon IMAGE_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/logo.png"));
	private static final ImageIcon OPEN_FILE_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/New Document.png"));
	private static final ImageIcon CONVERT_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/Play.png"));
	private static final ImageIcon CONVERT_CANCEL_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/Stop.png"));
	private static final ImageIcon SETTINGS_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/Gear.png"));
	private static final ImageIcon PREVIEW_ICON = new ImageIcon(ImageToZxSpec.class.getResource("/icons/Coherence.png"));	
	private static final String SINCLAIR_IMAGE_PATH = "/icons/sinclair.png";
	
	/**
	 * The message to show when idle
	 */
	public static final String DEFAULT_STATUS_MESSAGE = getCaption("main_waiting");
	
	/**
	 * Max poll time wait before starting (in seconds)
	 */
	private final static int VIDEO_POLL_TIMEOUT = 8;
	
	
	/**
	 * Max poll time wait for the conversion thread if processing images (in seconds)
	 */
	private static final int IMAGE_POLL_TIMEOUT = 5;
	
	/**
	 * The total number of frames/images converted in this session
	 */
	private static int frameCounter = 0;
	
	/**
	 * Actual frames per second for impressing your mates ;)
	 */
	private static volatile float fps;
	
	/**
	 * Input folders to process
	 */
	private static File[] inFiles = null;
	
	/**
	 * Destination folder to output files to
	 */
	private static File outFolder = null;
		
	/**
	 * The spectrum logo when no images are loaded
	 */
	private static volatile BufferedImage specLogo = null;
	
	/**
	 * Text file output for text dithers
	 */
	private static final TextConverter textConverter = new TextConverter();
	
	/**
	 * The ".tap" file converter
	 */
	private static final TapeConverter tapeConverter = new TapeConverter();
	
	/**
	 * The ".gif" file converter
	 */
	private static final GifConverter gifConverter = new GifConverter();
	
	/**
	 * The ZX icon for the window
	 */
	private static final String DEFAULT_FILE_NAME = "Img2ZXSpec";
	
	/**
	 * The main UI frame
	 */
	private static final JFrame frame = new JFrame("Image to ZX Spec 1.4 © Silent Software 2014");
	
	/** 
	 * The panel for rendering the images
	 */
	public static JPanel p = null;	
	
	/**
	 * The status box
	 */
	private static JTextField statusBox = null;
	
	/**
	 * The input folder menu item
	 */
	private static JMenuItem folder = null; 
	
	/**
	 * The output folder menu item
	 */
	private static JMenuItem outputFolder = null;
	
	/**
	 * The menu bar
	 */
	private static JMenuBar menubar = null;
	
	/**
	 * The options dialog
	 */
	private static PreferencesDialog preferences = new PreferencesDialog();
	
	/**
	 * The preview window
	 */
	private static final PreviewFrame preview = new PreviewFrame();
	
	/**
	 * The debug frames per second counter
	 */
	private static Thread fpsThread;
	
	/**
	 * Main dialog preview panel showing progress
	 */
	private volatile static BufferedImage mainPreviewImage = null; 
	
	/**
	 * Conversion cancellation flag
	 */
	private volatile static boolean isCancelled = false;

	private static JToolBar toolBar;

	private static JButton convertButton;
	
	/**
	 * Create a single thread for the rendering and inits the UI
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {		
		createUserInterface();
	}
	
	/**
	 * Create the natty UI/preview window.
	 * @throws MalformedURLException 
	 */
	public static void createUserInterface() throws MalformedURLException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			// Pah just ignore this error, we'll just have a naff looking UI
		}
		
		WindowListener windowListener = new WindowAdapter() {
			public void windowClosing(WindowEvent w) {
				WorkDispatcher.shutdownNow();
				frame.setVisible(false);
				frame.dispose();
			}
		};
		frame.addWindowListener(windowListener);
		
		// Drag and drop support
		frame.setDropTarget(new DropTarget(frame, new CustomDropTargetListener()));
		
		frame.setIconImage(IMAGE_ICON.getImage());
		frame.getContentPane().setLayout(new BorderLayout());
		toolBar = createToolbar();
		frame.getContentPane().add(toolBar, BorderLayout.PAGE_START);
		
		// Add the panel for rendering the original + result
		p = new JPanel(){
			
			@Override
			public void update(Graphics g) {
				paint(g);
			}

			static final long serialVersionUID = 0;
			public void paint(Graphics g) {
				if (mainPreviewImage != null) {
					// Draw rendered preview image
					g.drawImage(mainPreviewImage, 0, 0, null);
				} else if (specLogo != null) {
					g.drawImage(specLogo, 0, 0, null);
				}
			}
			
		};
	    frame.getContentPane().add(p, BorderLayout.CENTER);
	    statusBox = new JTextField(DEFAULT_STATUS_MESSAGE);
	    statusBox.setEditable(false);
		frame.getContentPane().add(statusBox, BorderLayout.PAGE_END);
		
		// Add the menu bar options
		JMenu fileMenu = new JMenu(getCaption("tab_file"));
		
		// Input folder
		folder = new JMenuItem(getCaption("tab_item_choose_input"));
		folder.addActionListener(new FileInputListener(frame));
		
		// Output folder
		outputFolder = new JMenuItem(getCaption("tab_item_choose_output"));
		outputFolder.addActionListener(new FileOutputListener(frame));		
		
		// Exit button
		JMenuItem exit = new JMenuItem(getCaption("tab_item_exit"));
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				frame.dispose();
				System.exit(0);
			}
		});
		
		fileMenu.add(folder);
		fileMenu.add(outputFolder);
		fileMenu.add(new Separator());
		fileMenu.add(exit);
		menubar = new JMenuBar();
		menubar.add(fileMenu);
		
		final JMenu optionsMenu = new JMenu(getCaption("tab_option"));
		JMenuItem preferencesItem = new JMenuItem(getCaption("tab_item_control_panel"));
		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (!preferences.isShowing()) {
					preferences.setVisible(true);
				}
				preferences.toFront();
			}
		});
		final JMenuItem previewItem = new JMenuItem(getCaption("tab_item_view_preview"));
		previewItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (!preview.isShowing()) {
					preview.setVisible(true);
					processPreview();
				}
				preview.toFront();
			}
		});
		optionsMenu.add(preferencesItem);
		optionsMenu.addSeparator();
		optionsMenu.add(previewItem);
		menubar.add(optionsMenu);
		
		JMenu about = new JMenu(getCaption("tab_about"));
		JMenuItem aboutZx = new JMenuItem(getCaption("tab_item_about"));
		about.add(aboutZx);
		aboutZx.addActionListener(new AboutListener());
		menubar.add(about);
		
		// Default settings for window (and sets it relative to screen centre)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar(menubar);
	    frame.setVisible(true);
	    frame.setResizable(false);
	    
	    // Show a nice logo while nothing loaded
	    try {
			specLogo = ImageIO.read(ImageToZxSpec.class.getResource(SINCLAIR_IMAGE_PATH).openStream());
			Dimension dim = new Dimension(specLogo.getWidth(),specLogo.getHeight()+toolBar.getHeight()+statusBox.getHeight()+frame.getInsets().top+frame.getInsets().bottom+menubar.getHeight());
			frame.setSize(dim);
		    frame.setPreferredSize(dim);
			frame.repaint();
			frame.setLocationRelativeTo(null);
		} catch (IOException e1) {
		}
		frame.pack();
	}
	
	public static boolean isSupported(File f) {
		String name = f.getAbsolutePath().toLowerCase();
		return (f.isDirectory()
				|| name.endsWith(".gif") 
				|| name.endsWith(".png")
				|| name.endsWith(".jpg")
				|| name.endsWith(".jpeg") 
				|| name.endsWith(".avi") 
				|| name.endsWith(".mov")
				|| name.endsWith(".mp4")
				|| name.endsWith(".mpg") 
				&& !name.contains(SaveHelper.FILE_SUFFIX));
	}
	
	/**
	 * Method to begin the (multi dither, not WIP) preview
	 * which submits work to the work engine and controls the
	 * UI settings and validates the content being loaded.
	 */
	public static void processPreview() {
		if (!preview.isShowing()) {
			return;
		}
		PreviewFrame.reset();
		OptionsObject oo = OptionsObject.getInstance();
		final BlockingQueue<Future<WorkContainer>> futures = new LinkedBlockingQueue<Future<WorkContainer>>(oo.getOrderedDithers().length+oo.getErrorDithers().length+1);
		
		try {
			if (inFiles == null) {
				return;
			}
			if (inFiles.length > 0) {
				File f = inFiles[0];
				Image image;
				disableInput();
				if (f.getPath().endsWith(".avi") || f.getPath().endsWith(".mov") || f.getPath().endsWith(".mp4") || f.getPath().endsWith(".mpg")) {
					BlockingQueue<Image> queue = new LinkedBlockingQueue<Image>();
					oo.getVideoImportEngine().convertVideoToImages(f, true, queue);
					image = queue.poll(VIDEO_POLL_TIMEOUT, TimeUnit.SECONDS); 
				} else {
					image = ImageIO.read(f);
				}
				if (image == null) {
					// Something went wrong, re-enable the UI
					enableInput();
					return;
				}
				Future<WorkContainer> future;
				for(DitherStrategy dither : oo.getOrderedDithers()) {
					future = WorkDispatcher.submitPreviewWork(image, dither);
					futures.add(future);
				}			
				for(DitherStrategy dither : oo.getErrorDithers()) {
					future = WorkDispatcher.submitPreviewWork(image, dither);
					futures.add(future);
				}
				
				future = WorkDispatcher.submitPreviewWork(image, null);
				futures.add(future);
				
			}
			startPreviewWaiter(futures);
			
		} catch (Exception e) {
			setStatusMessage(e.getMessage());
		}
	}
	
	public static JToolBar createToolbar() throws MalformedURLException {
		JToolBar toolBar = new JToolBar();
		JButton openButton = new JButton(OPEN_FILE_ICON);
		openButton.addActionListener(new FileInputListener(frame));
		toolBar.add(openButton);
		convertButton = new JButton(CONVERT_ICON);
		convertButton.addActionListener(new FileReadyListener(frame, new OperationFinishedListener() {	
			@Override
			public void operationFinished(boolean success) {
				if (!success) {
					return;
				}
				// On convert click change the main button text to allow cancelling
				boolean isConverting = !convertButton.getIcon().equals(CONVERT_CANCEL_ICON);
				isCancelled = !isConverting;
				if (isConverting){	
					try {
						startFpsCalculator();
						processFiles();
					} catch(Exception e) {
						JOptionPane.showMessageDialog(null, getCaption("dialog_error")+e.getMessage(), getCaption("dialog_error_title"), JOptionPane.OK_OPTION);  			
					}
				} else {
					convertButton.setEnabled(false);
				}
			}
		}));
		toolBar.add(convertButton);
		JButton settingsButton = new JButton(SETTINGS_ICON);
		settingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (!preferences.isShowing()) {
					preferences.setVisible(true);
				}
				preferences.toFront();
			}
		});
		toolBar.add(settingsButton);
		JButton previewButton = new JButton(PREVIEW_ICON);
		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (!preview.isShowing()) {
					preview.setVisible(true);
					processPreview();
				}
				preview.toFront();
			}
		});
		toolBar.add(previewButton);
		JPanel panel = new JPanel();
		final JComboBox<DitherStrategy> dithers = new JComboBox<DitherStrategy>();
		preferences.createDitherComboBox(dithers, new DitherChangedListener() {

			@Override
			public void ditherSelected(DitherStrategy dither) {
				dithers.setSelectedItem(dither);
			}
		});
		panel.setLayout(new GridLayout(1,1));	
		panel.add(dithers);
		toolBar.add(panel);
		toolBar.setFloatable(false);
		return toolBar;
	}
	
	/**
	 * A "waiter" which waits for the work engine (the WorkDispatcher) to return
	 * a result so it can be displayed in the dither preview dialog when it is
	 * ready. The list of futures contains the work dispatcher's work in progress
	 * Future tasks.
	 * 
	 * @param futures
	 */
	private static void startPreviewWaiter(final BlockingQueue<Future<WorkContainer>> futures) {
		Thread t = new Thread() {
			public void run() {
				try {
					Future<WorkContainer> future;
					while ((future = futures.poll(IMAGE_POLL_TIMEOUT, TimeUnit.SECONDS)) != null && !isCancelled) {
						WorkContainer wc = null;
						try{wc = future.get();}catch (Exception e){}
						if (wc != null) {
							BufferedImage img = ResultImage.getFinalImage(wc.getResultImage()).getImage();
							Point p = PreviewFrame.getPoint();
							BufferedImage preview = PreviewFrame.getPreviewImage();
							ImageHelper.copyImage(img, preview, p);
							PreviewFrame.repaintImage();
						}
					}
				} catch (InterruptedException e) {
				}
				finally {
					enableInput();
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}
	
	/**
	 * Method to begin the WIP preview which submits work to the work engine 
	 * and controls the UI settings and validates the content being loaded.
	 * In particular if a video is found in the input only this file is processed.
	 */
	public static void processFiles(){
		disableInput();
		frame.repaint();
		Thread t = new Thread(){
		
			@Override
			public void run() {
				try {
					for(File f : inFiles) {
						// We have a video so only deal with this file
						String path = f.getPath().toLowerCase();
						if (path.endsWith(".avi") || path.endsWith(".mov") || f.getPath().endsWith(".mp4") || f.getPath().endsWith(".mpg")) {
							processVideo(f);
							return;
						}
					}
					processSingleFiles();
				} catch (Exception e) {
					e.printStackTrace();
					setStatusMessage(e.getMessage());
					enableInput();
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}
	
	/**
	 * Inner core method for the process files method that specifically deals 
	 * with a video file. The file is converted to a number of images and this
	 * work is put into the work engine as per the usual procedure for images.
	 * A waiter thread is initialised to pick the completed work up and as the
	 * images are loaded in order the future tasks' results remain correctly 
	 * ordered when they are collected.
	 * 
	 * @param f
	 * @throws InterruptedException
	 * @throws NoPlayerException
	 * @throws IOException
	 * @throws ExecutionException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private static void processVideo(final File f) throws InterruptedException, NoPlayerException, IOException, ExecutionException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		final BlockingQueue<Image> sharedQueue = new LinkedBlockingQueue<Image>();
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					OptionsObject.getInstance().getVideoImportEngine().convertVideoToImages(f, false, sharedQueue);
				} catch(Throwable t) {
					ImageToZxSpec.setStatusMessage(t.getMessage());
				}
			}
		});
		t.start();
		final BlockingQueue<Future<WorkContainer>> futures = new LinkedBlockingQueue<Future<WorkContainer>>();
		startWaiter(futures, f.getName());
		
		// Make sure we get through these as fast as possible as they are using memory while 
		// being held in the work engine awaiting collection and outputting before a GC can occur.
		// Due to numerous other processing threads and this being the feeder/producer it must be 
		// high priority
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Image buf = null;
		while ((buf = sharedQueue.poll(VIDEO_POLL_TIMEOUT, TimeUnit.SECONDS)) != null && !isCancelled) {
			try {
				Future<WorkContainer> future = WorkDispatcher.submitWork(buf, OptionsObject.getInstance().getExportScreen() || OptionsObject.getInstance().getExportTape());
				futures.add(future);
			} catch (OutOfMemoryError oome) {
				setStatusMessage(oome.getMessage());
				break;
			}
		}	
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
	}
	
	
	/**
	 * Inner core method for the process files method that specifically deals 
	 * with a single files. The files are loaded as images and these are put
	 * into the work engine for processing.
	 * A waiter thread is initialised to pick the completed work up and as the
	 * images are loaded in order the future tasks' results remain correctly 
	 * ordered when they are collected.
	 * 
	 * @throws InterruptedException
	 * @throws NoPlayerException
	 * @throws IOException
	 * @throws ExecutionException
	 */
	private static void processSingleFiles() throws InterruptedException, NoPlayerException, IOException, ExecutionException {
		if (inFiles.length == 0) {
			return;
		}
		BlockingQueue<Future<WorkContainer>> futures = new LinkedBlockingQueue<Future<WorkContainer>>(inFiles.length);
		startWaiter(futures, null);
		OptionsObject oo = OptionsObject.getInstance();
		for(File f : inFiles) {
			try {
				BufferedImage image = ImageIO.read(f);
				Future<WorkContainer> future = WorkDispatcher.submitWork(image, oo.getExportScreen() || oo.getExportTape());
				futures.add(future);
				if (isCancelled) {
					break;
				}
			} catch(OutOfMemoryError oome) {
				setStatusMessage(oome.getMessage());
				break;
			}
		}
	}
	
	
	
	/**
	 * A "waiter" which waits for the work engine (the WorkDispatcher) to return
	 * a result so it can be displayed in the WIP preview when it is
	 * ready. The list of futures contains the work dispatcher's work in progress
	 * Future tasks. When the results are returned as buffered images or scrs the 
	 * relevant scr, tap, png, jpg etc is created. In the case of video the videoName
	 * is used for the file name base.
	 * 
	 * @param futures
	 */
	private static void startWaiter(final BlockingQueue<Future<WorkContainer>> futures, final String videoName) {
		// Old school threading for the work results handler
		Thread t = new Thread() {
			public void run() {
				try {
					OptionsObject oo = OptionsObject.getInstance();
					File textOutput = new File(outFolder+"/"+DEFAULT_FILE_NAME+".txt");
					if (oo.getExportText()) {
						SaveHelper.deleteFile(textOutput);
					}
					if (oo.getExportAnimGif()) {
						gifConverter.createSequence(oo.getGifDisplayTimeMillis());
					}
					List<byte[]> convertedTap = new ArrayList<byte[]>();
					specLogo = null;
					final String processingText=getCaption("main_processed")+" ";
					setStatusMessage(getCaption("main_working"));
					Future<WorkContainer> future = null;
					int frameIndex = 0;
					int pollTimeout = VIDEO_POLL_TIMEOUT; 
					if (videoName == null) {
						pollTimeout = IMAGE_POLL_TIMEOUT;
					}
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
					while ((future = futures.poll(pollTimeout, TimeUnit.SECONDS)) != null && !isCancelled) {
						WorkContainer result = future.get();
						setStatusMessage(processingText+(frameIndex+1));
						BufferedImage imageResult = ResultImage.getFinalImage(result.getResultImage()).getImage();
						if (oo.getShowWipPreview()) {
							BufferedImage preProcessedResult = result.getPreprocessedImageResult();
							createDisplayPreview(preProcessedResult, imageResult);
							
							Dimension dim = new Dimension(imageResult.getWidth()*2+frame.getInsets().left+frame.getInsets().right,
									imageResult.getHeight()+statusBox.getHeight()+toolBar.getHeight()+frame.getInsets().top+frame.getInsets().bottom+menubar.getHeight());
							if (!frame.getSize().equals(dim)) {
								frame.setSize(dim);
							}
							frame.repaint();
						}
						
						if (oo.getExportTape()) {
							// Gigascreens are 2 screens in 1 and thus we need to split the scr
							if (oo.getColourMode() instanceof  GigaScreenPaletteStrategy) {
								byte[] scr1 = Arrays.copyOf(result.getScrData(), ScrConverter.SCR_SIZE);
								byte[] scr2 = Arrays.copyOfRange(result.getScrData(), ScrConverter.SCR_SIZE, 13824);
								convertedTap.add(tapeConverter.createTapPart(scr1));
								convertedTap.add(tapeConverter.createTapPart(scr2));
							} else {
								convertedTap.add(tapeConverter.createTapPart(result.getScrData()));
							}
						}
						if (oo.getExportAnimGif()) {
							gifConverter.addFrame(ResultImage.getFinalImage(result.getResultImage()).getImage());
						}
						String name = videoName;
						if (name == null) {
							name = inFiles[frameIndex].getName();
						}
						if (oo.getExportImage()) {
							SaveHelper.saveImage(imageResult, outFolder, name);
						}
						if (oo.getExportScreen()) {
							SaveHelper.saveBytes(result.getScrData(), new File(outFolder+"/"+name+".scr"));
						}	
						if (oo.getExportText()) {
							String text = textConverter.convert(ResultImage.getFinalImage(result.getResultImage()).getImage());
							SaveHelper.saveBytes(text.getBytes(),textOutput, true);
						}
						// FPS counter - periodically reset for averages
						++frameCounter;
						
						// File and frame index counter
						++frameIndex;
					}
					if (!isCancelled) {
						if (oo.getExportTape() && convertedTap.size() > 0) {
							byte[] result = tapeConverter.createTap(convertedTap);
							if (result != null && result.length > 0) {
								SaveHelper.saveBytes(result, new File(outFolder+"/"+DEFAULT_FILE_NAME+".tap"));
							}
						}
						if (oo.getExportAnimGif()) {
							setStatusMessage(getCaption("main_saving_gif"));
							byte[] result = gifConverter.createGif();	
							if (result != null && result.length > 0) {
								SaveHelper.saveBytes(result, new File(outFolder+"/"+DEFAULT_FILE_NAME+".gif"));
							}
						}
					} else {
						futures.clear();
					}
					setStatusMessage(DEFAULT_STATUS_MESSAGE);
					if (frameIndex == 0 && videoName != null) {
						JOptionPane.showMessageDialog(null, getCaption("dialog_no_video_processed"), getCaption("dialog_no_video_processed_title"), JOptionPane.ERROR_MESSAGE);
					}
				} catch(Exception e){
					setStatusMessage(e.getMessage());
				} finally {
					enableInput();
				}
			} 
		};
		t.setDaemon(true);
		t.start();
	}
	
	/**
	 * A simple implementation of an FPS calculator that uses actual
	 * time difference rather than expected sleep time to determine the
	 * real FPS. finalised variables in the vague hope these will be
	 * optimised and GC cleaned up in the young generation.
	 */
	private static void startFpsCalculator() {
		// Old school threading for the fps calculator
		if (OptionsObject.getInstance().getFpsCounter() && (fpsThread == null || !fpsThread.isAlive())) {
			fpsThread = new Thread() {
				public void run() {
					frameCounter = 0;
					while (OptionsObject.getInstance().getFpsCounter()) {
						final float start = frameCounter;
						final long time = System.currentTimeMillis();
						try {Thread.sleep(1000);} catch(Exception e){}
						final long diff = System.currentTimeMillis()- time;
						final float end = frameCounter;
						fps = (end-start)/(diff/1000);
					}
				}
			};
			fpsThread.setDaemon(true);
			fpsThread.setPriority(Thread.MIN_PRIORITY);
			fpsThread.start();
		}
	}
	
	/**
	 * Sets a status or error message above the convert button
	 * @param message
	 */
	public static void setStatusMessage(final String message) {
		statusBox.setText(message);
		statusBox.repaint(1000);
	}
	
	/**
	 * Internal method to build a static preview image to display in the main panel
	 * during video processing
	 * 
	 * @param preprocessed
	 * @param result
	 * @return
	 */
	private static BufferedImage createDisplayPreview(BufferedImage preprocessed, BufferedImage result) {
		mainPreviewImage = new BufferedImage(result.getWidth()*2, result.getHeight(), result.getType());
		Graphics preBuffer = mainPreviewImage.createGraphics();
		preBuffer.drawImage(preprocessed, 0, 0, null);
		preBuffer.drawImage(result, result.getWidth(), 0, null);
		if (OptionsObject.getInstance().getFpsCounter()) {
			preBuffer.setColor(Color.WHITE);
			preBuffer.drawString(getCaption("main_fps_overlay")+" "+fps, 10, 20);
		}
		return mainPreviewImage;
	}
	
	/**
	 * Reset's the UI state to enable input
	 */
	public static void enableInput() {
		convertButton.setEnabled(true);
		convertButton.setIcon(CONVERT_ICON);
		folder.setEnabled(true);
		outputFolder.setEnabled(true);
		isCancelled = false;
	}
	
	
	/**
	 * Disable the UI state to prevent input
	 */
	static void disableInput() {
		convertButton.setIcon(CONVERT_CANCEL_ICON);
		folder.setEnabled(false);
		outputFolder.setEnabled(false);
	}
	
	public static File[] getInFiles() {
		return ImageToZxSpec.inFiles;
	}
	
	public static void setInFiles(File[] inFiles) {
		ImageToZxSpec.inFiles = inFiles;
	}
	
	public static void setOutFolder(File outFolder) {
		ImageToZxSpec.outFolder = outFolder;
	}
	
	public static File getOutFolder() {
		return outFolder;
	}

	public static void showPreview() {
		preview.setVisible(true);
	}
	
	public static boolean isCancelled() {
		return isCancelled;
	}
}
