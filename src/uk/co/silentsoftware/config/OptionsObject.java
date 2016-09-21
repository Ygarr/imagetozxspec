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
package uk.co.silentsoftware.config;

import static uk.co.silentsoftware.config.LanguageSupport.getCaption;
import static uk.co.silentsoftware.config.PersistenceService.PREFS_FILE;

import java.io.File;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.silentsoftware.core.attributestrategy.AttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.FavourBrightAttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.FavourHalfBrightAttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.FavourMostPopularAttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.ForceBrightAttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.ForceHalfBrightAttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.ForceReducedHalfBrightAttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenAttributeStrategy;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenBrightPaletteStrategy;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenHalfBrightPaletteStrategy;
import uk.co.silentsoftware.core.attributestrategy.GigaScreenMixedPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.ColourChoiceStrategy;
import uk.co.silentsoftware.core.colourstrategy.FullPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.GigaScreenPaletteStrategy;
import uk.co.silentsoftware.core.colourstrategy.MonochromePaletteStrategy;
import uk.co.silentsoftware.core.converters.image.CharacterDitherStrategy;
import uk.co.silentsoftware.core.converters.image.DitherStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.AtkinsonDitherStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.BurkesDitherStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.ErrorDiffusionDitherStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.FloydSteinbergDitherStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.JarvisJudiceNinkeDitherStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.LowErrorAtkinsonDitherStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.NoDitherStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.SierraFilterLightStrategy;
import uk.co.silentsoftware.core.converters.image.errordiffusionstrategy.StuckiDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.BayerEightByEightDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.BayerFourByFourDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.BayerTwoByOneOrderedDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.BayerTwoByTwoOrderedDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.LightnessOrderedDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.MagicSquareDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.NasikMagicSquareDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.OmegaOrderedDitherStrategy;
import uk.co.silentsoftware.core.converters.image.orderedditherstrategy.OrderedDitherStrategy;
import uk.co.silentsoftware.core.converters.video.JMFVideoImportEngine;
import uk.co.silentsoftware.core.converters.video.VLCVideoImportEngine;
import uk.co.silentsoftware.core.converters.video.VideoImportEngine;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

/**
 * The backing object behind the OptionDialog,
 * holding the particular user configuration.
 */
public class OptionsObject {

	private String pathToVlc = null;
	
	/**
	 * The number of frames to sample from the video per second
	 */
	private double videoFramesPerSecond = 10;
	
	/**
	 * The number of milliseconds delay for each gif frame
	 */
	private int gifDisplayTimeMillis = 100;

	/**
	 * Prefix identifier for custom basic loaders
	 */
	public final static String CUSTOM_LOADER_PREFIX = getCaption("loader_custom")+" ";
	
	/**
	 * Basic loader for slideshows/video (tap output)
	 */
	private final BasicLoader[] basicLoaders;
	{
		basicLoaders = new BasicLoader[]{
				new BasicLoader(getCaption("loader_simple"), "simple.tap"),
				new BasicLoader(getCaption("loader_buffered"), "buffered.tap"),
				new BasicLoader(getCaption("loader_gigascreen"), "gigascreen.tap"),
				new BasicLoader(CUSTOM_LOADER_PREFIX, null)		
		};
	}
	
	/**
	 * The chosen basic loader
	 */
	private BasicLoader basicLoader = basicLoaders[0]; 

	/**
	 * Dither strategies available
	 */
	private final ErrorDiffusionDitherStrategy[] errorDithers;
	{
		errorDithers = new ErrorDiffusionDitherStrategy[]{
			new AtkinsonDitherStrategy(),
			new BurkesDitherStrategy(),
			new FloydSteinbergDitherStrategy(),
			new JarvisJudiceNinkeDitherStrategy(),
			new LowErrorAtkinsonDitherStrategy(),
			new NoDitherStrategy(),
			new SierraFilterLightStrategy(),
			new StuckiDitherStrategy()
		};
	}
	
	/**
	 * Currently selected error diffusion dither strategy
	 */
	private ErrorDiffusionDitherStrategy errorDiffusionDitherStrategy = errorDithers[0];

	private final OrderedDitherStrategy[] orderedDithers;
	{
		orderedDithers = new OrderedDitherStrategy[]{
			new BayerTwoByOneOrderedDitherStrategy(),	
			new BayerTwoByTwoOrderedDitherStrategy(),
			new OmegaOrderedDitherStrategy(),
			new BayerFourByFourDitherStrategy(),
			new BayerEightByEightDitherStrategy(),
			new LightnessOrderedDitherStrategy(),
			new MagicSquareDitherStrategy(),
			new NasikMagicSquareDitherStrategy()
		};
	}
	
	/**
	 * Currently selected ordered dither strategy
	 */
	private OrderedDitherStrategy orderedDitherStrategy = orderedDithers[7];
	
	/**
	 * The odd one out - character dither strategy is neither error or ordered
	 */
	private CharacterDitherStrategy characterDitherStrategy = new CharacterDitherStrategy();

	/**
	 * Whether the mode is error diffusion (true) or ordered dither
	 */
	private volatile DitherStrategy selectedDitherStrategy = errorDiffusionDitherStrategy;
	
	/**
	 * Scaling modes available
	 */
	private final ScalingObject[] scalings;
	{
		scalings = new ScalingObject[]{
			new ScalingObject(getCaption("scaling_none"), -1, -1), 
			new ScalingObject("256x192", SpectrumDefaults.SCREEN_WIDTH, SpectrumDefaults.SCREEN_HEIGHT), 
			new ScalingObject(getCaption("scaling_width_prop"), SpectrumDefaults.SCREEN_WIDTH, -1),
			new ScalingObject(getCaption("scaling_height_prop"), -1, SpectrumDefaults.SCREEN_HEIGHT) 
		};
	}
	
	/**
	 * Currently selected scaling mode
	 */
	public volatile ScalingObject scaling = scalings[1];
	
	/**
	 * ZX Spectrum scaling mode
	 */
	public final ScalingObject zxScaling = scalings[1];
	
	/**
	 * Pixel colouring strategy (there used to be a half
	 * colour mode too but I've removed it until I come
	 * up with a better/working replacement)
	 */
	private final ColourChoiceStrategy[] colourModes;
	{
		colourModes = new ColourChoiceStrategy[]{
			new FullPaletteStrategy(),
			new GigaScreenPaletteStrategy(),
			new MonochromePaletteStrategy()
		};
	}
	
	/**
	 * Currently selected pixel colouring strategy
	 */
	private volatile ColourChoiceStrategy colourMode = colourModes[0];
	
	/**
	 * Attribute favouritism choice - when colours need to be changed 
	 * to a two colour attribute what is favoured?
	 */
	private final AttributeStrategy[] attributeModes;
	{
		attributeModes = new AttributeStrategy[] {
				new FavourHalfBrightAttributeStrategy(),
				new FavourBrightAttributeStrategy(),
				new FavourMostPopularAttributeStrategy(),
				new ForceHalfBrightAttributeStrategy(),
				new ForceBrightAttributeStrategy(),
				new ForceReducedHalfBrightAttributeStrategy(),
		};
	};
	
	/**
	 * The method of choosing the correct attribute colour
	 */
	private volatile AttributeStrategy attributeMode = attributeModes[0];	
	
	private final GigaScreenAttributeStrategy[] gigaScreenAttributeStrategies;
	{
		gigaScreenAttributeStrategies = new GigaScreenAttributeStrategy[] {
				new GigaScreenHalfBrightPaletteStrategy(),
				new GigaScreenBrightPaletteStrategy(),
				new GigaScreenMixedPaletteStrategy(),
		};
	};
	
	/**
	 * The method of choosing the correct attribute colour
	 */
	private volatile GigaScreenAttributeStrategy gigaScreenAttributeStrategy = gigaScreenAttributeStrategies[0];

	/**
	 * 
	 */
	private GigaScreenHSBOption[] gigaScreenHsbOptions;
	{		
		gigaScreenHsbOptions = GigaScreenHSBOption.values();
	}
	
	private volatile GigaScreenHSBOption gigaScreenHsbOption = GigaScreenHSBOption.None;
	
	/**
	 * The video converting libraries supported
	 */
	private final VideoImportEngine[] VideoImportEngines;
	{
		VideoImportEngines = new VideoImportEngine[] {
				new JMFVideoImportEngine(),
				new VLCVideoImportEngine()
		};
	};
	
	/**
	 * Currently selected video converter
	 */
	private volatile VideoImportEngine VideoImportEngine = VideoImportEngines[0];

	/**
	 * Display frames per second
	 */
	private volatile boolean fpsCounter = false;	
	
	/**
	 * Display frames per second
	 */
	private volatile boolean showWipPreview = true;	
	
	/**
	 * Export image formats available
	 */
	private final String[] imageFormats = new String[]{"png", "jpg"};
	
	/**
	 * Currently selected image export format
	 */
	private volatile String imageFormat = imageFormats[0];

	/**
	 * Image pre-process contrast setting
	 */
	private volatile float contrast = 1;
	
	/**
	 * Image pre-process brightness setting
	 */
	private volatile float brightness = 0;
	
	/**
	 * Image pre-process saturation setting
	 */
	private volatile float saturation = 0;
	
	/**
	 * Monochrome b/w threshold - determines what
	 * value a colour must be below for it to be
	 * considered black
	 */
	private volatile int blackThreshold = 384;
	
	/**
	 * Flag for allowing image export
	 */
	private volatile boolean exportImage = true;
	
	/**
	 * Flag for allowing screen export
	 */
	private volatile boolean exportScreen = false;
	
	/**
	 * Flag for allowing tape (slideshow) export
	 */
	private volatile boolean exportTape = false;
	
	/**
	 * Flag to export an anim gif
	 */
	private volatile boolean exportAnimGif = false;
	
	/**
	 * Flag to export a text dither as text
	 */
	private volatile boolean exportText = false;

	/**
	 * The monochrome mode ink colour (spectrum palette index)
	 */
	private volatile int monochromeInkIndex = 0;
	
	/**
	 * The monochrome mode paper colour (spectrum palette index)
	 */
	private volatile int monochromePaperIndex = 7;
	
	/**
	 * Intensity for ordered dithering
	 */
	private volatile int orderedDitherIntensity = 1;
	
	/**
	 * Serpentine dithering can improve error diffusion dithers
	 */
	private volatile boolean serpentine = false;
	
	/**
	 * Constrains the error diffusion to 8x8 pixels (a single attribute block)
	 */
	private volatile boolean constrainedErrorDiffusion = false;

	/**
	 * Constrains the error diffusion to 8x8 pixels (a single attribute block)
	 */
	private volatile boolean preferDetail = false;
	
	/**
	 * Threads per CPU (technically this may not be "per" CPU
	 * it's whatever CPU has free resources however it's a good
	 * human metric).
	 */
	private int threadsPerCPU = 2;
	
	private final static OptionsObject instance;
	
	static {
		File f = new File(PREFS_FILE);
		OptionsObject tempRef = null;
		if (f.exists()) {
			try {
				tempRef = (OptionsObject)PersistenceService.load(PREFS_FILE);
			} catch(Throwable t) {
				// Ignore - it's usually an older version file so a new save will fix it.
			}
		}
		if (tempRef == null) {
			tempRef = new OptionsObject();
		}
		if (tempRef.getVideoImportEngine() instanceof VLCVideoImportEngine) {
			tempRef.initVideoImportEngine();
		}
		instance = tempRef;	
	}
	
	public void initVideoImportEngine() {
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), pathToVlc);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
	}
	
	/**
	 * Retrieves the option object instance
	 * @return
	 */
	public static OptionsObject getInstance() {
		return instance;
	}
	
	public  int getThreadsPerCPU(){
		return threadsPerCPU;
	}
	public void setThreadsPerCPU(int threadsPerCPU){
		this.threadsPerCPU = threadsPerCPU;
	}
	public  ErrorDiffusionDitherStrategy getErrorDiffusionDitherStrategy() {
		return errorDiffusionDitherStrategy;
	}
	public void setErrorDitherStrategy(ErrorDiffusionDitherStrategy ditherStrategy) {
		this.errorDiffusionDitherStrategy = ditherStrategy;
	}
	public ErrorDiffusionDitherStrategy[] getErrorDithers() {
		return errorDithers;
	}
	public ScalingObject[] getScalings() {
		return scalings;
	}
	public ScalingObject getScaling() {
		return scaling;
	}
	public ScalingObject getZXDefaultScaling() {
		return zxScaling;
	}
	public void setScaling(ScalingObject scaling) {
		this.scaling = scaling;
	}
	public float getContrast() {
		return contrast;
	}
	public void setContrast(float contrast) {
		this.contrast = contrast;
	}
	public float getBrightness() {
		return brightness;
	}
	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}
	public float getSaturation() {
		return saturation;
	}
	public void setSaturation(float saturation) {
		this.saturation = saturation;
	}
	public  void setFpsCounter(boolean fpsCounter) {
		this.fpsCounter = fpsCounter;
	}
	public  void setShowWipPreview(boolean showWipPreview) {
		this.showWipPreview = showWipPreview;
	}
	public String getImageFormat() {
		return imageFormat;
	}
	public void setImageFormat(String imageFormat) {
		this.imageFormat = imageFormat;
	}
	public String[] getImageFormats() {
		return imageFormats;
	}
	public boolean getFpsCounter() {
		return fpsCounter;
	}
	public boolean getShowWipPreview() {
		return showWipPreview;
	}
	public boolean getExportImage() {
		return exportImage;
	}
	public void setExportImage(boolean exportImage) {
		this.exportImage = exportImage;
	}
	public boolean getExportScreen() {
		return exportScreen;
	}
	public void setExportScreen(boolean exportScreen) {
		this.exportScreen = exportScreen;
	}
	public boolean getExportAnimGif() {
		return exportAnimGif;
	}
	public void setExportAnimGif(boolean exportAnimGif) {
		this.exportAnimGif = exportAnimGif;
	}
	public boolean getExportText() {
		return exportText;
	}
	public void setExportText(boolean exportText) {
		this.exportText = exportText;
	}
	public boolean getExportTape() {
		return exportTape;
	}
	public  void setExportTape(boolean exportTape) {
		this.exportTape = exportTape;
	}
	public ColourChoiceStrategy getColourMode() {
		return colourMode;
	}
	public void setColourMode(ColourChoiceStrategy colourMode) {
		this.colourMode = colourMode;
	}
	public ColourChoiceStrategy[] getColourModes() {
		return colourModes;
	}
	public int getMonochromeInkIndex() {
		return monochromeInkIndex;
	}
	public void setMonochromeInkIndex(int monochromeInkIndex) {
		this.monochromeInkIndex = monochromeInkIndex;
	}
	public int getMonochromePaperIndex() {
		return monochromePaperIndex;
	}
	public void setMonochromePaperIndex(int monochromePaperIndex) {
		this.monochromePaperIndex = monochromePaperIndex;
	}
	public int getBlackThreshold() {
		return blackThreshold;
	}
	public void setBlackThreshold(int blackThreshold) {
		this.blackThreshold = blackThreshold;
	}
	public OrderedDitherStrategy getOrderedDitherStrategy() {
		return orderedDitherStrategy;
	}
	public void setOrderedDitherStrategy(OrderedDitherStrategy orderedDitherStrategy) {
		this.orderedDitherStrategy = orderedDitherStrategy;
	}
	public OrderedDitherStrategy[] getOrderedDithers() {
		return orderedDithers;
	}
	public CharacterDitherStrategy getCharacterDitherStrategy() {
		return characterDitherStrategy;
	}
	public DitherStrategy getSelectedDitherStrategy() {
		return selectedDitherStrategy;
	}
	public void setSelectedDitherStrategy(DitherStrategy selectedDitherStrategy) {
		this.selectedDitherStrategy = selectedDitherStrategy;
	}
	public int getOrderedDitherIntensity() {
		return orderedDitherIntensity;
	}
	public void setOrderedDitherIntensity(int orderedDitherIntensity) {
		this.orderedDitherIntensity = orderedDitherIntensity;
	}
	public AttributeStrategy getAttributeMode() {
		return attributeMode;
	}
	public void setAttributeMode(AttributeStrategy attributeMode) {
		this.attributeMode = attributeMode;
	}
	public AttributeStrategy[] getAttributeModes() {
		return attributeModes;
	}
	public BasicLoader[] getBasicLoaders() {
		return basicLoaders;
	}
	public BasicLoader getBasicLoader() {
		return basicLoader;
	}
	public void setBasicLoader(BasicLoader basicLoader) {
		this.basicLoader = basicLoader;
	}
	public double getVideoFramesPerSecond() {
		return videoFramesPerSecond;
	}
	public void setVideoFramesPerSecond(double videoFramesPerSecond) {
		this.videoFramesPerSecond = videoFramesPerSecond;
	}
	public int getGifDisplayTimeMillis() {
		return gifDisplayTimeMillis;
	}
	public void setGifDisplayTimeMillis(int gifDisplayTimeMillis) {
		this.gifDisplayTimeMillis = gifDisplayTimeMillis;
	}
	public boolean getSerpentine() {
		return serpentine;
	}
	public void setSerpentine(boolean serpentine) {
		this.serpentine = serpentine;
	}
	public boolean getConstrainedErrorDiffusion() {
		return constrainedErrorDiffusion;
	}
	public void setConstrainedErrorDiffusion(boolean constrainedErrorDiffusion) {
		this.constrainedErrorDiffusion = constrainedErrorDiffusion;
	}
	public VideoImportEngine getVideoImportEngine() {
		return VideoImportEngine;
	}
	public void setVideoImportEngine(VideoImportEngine VideoImportEngine) {
		this.VideoImportEngine = VideoImportEngine;
	}
	public VideoImportEngine[] getVideoImportEngines() {
		return VideoImportEngines;
	}
	public boolean getPreferDetail() {
		return preferDetail;
	}
	public void setPreferDetail(boolean preferDetail) {
		this.preferDetail = preferDetail;
	}

	public String getPathToVlc() {
		return pathToVlc;
	}

	public void setPathToVlc(String pathToVlc) {
		this.pathToVlc = pathToVlc;
	}

	public GigaScreenAttributeStrategy[] getGigaScreenAttributeStrategies() {
		return gigaScreenAttributeStrategies;
	}

	public GigaScreenAttributeStrategy getGigaScreenAttributeStrategy() {
		return gigaScreenAttributeStrategy;
	}

	public void setGigaScreenAttributeStrategy(GigaScreenAttributeStrategy gigaScreenAttributeStrategy) {
		this.gigaScreenAttributeStrategy = gigaScreenAttributeStrategy;
	}

	public GigaScreenHSBOption[] getGigaScreenHsbOptions() {
		return gigaScreenHsbOptions;
	}

	public void setGigaScreenHsbOptions(GigaScreenHSBOption[] gigaScreenHsbOptions) {
		this.gigaScreenHsbOptions = gigaScreenHsbOptions;
	}

	public GigaScreenHSBOption getGigaScreenHsbOption() {
		return gigaScreenHsbOption;
	}

	public void setGigaScreenHsbOption(GigaScreenHSBOption gigaScreenHsbOption) {
		this.gigaScreenHsbOption = gigaScreenHsbOption;
	}	
	
	
}
