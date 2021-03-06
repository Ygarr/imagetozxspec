Image To ZX Spectrum 1.4.0
==========================


Legal / Disclaimer
------------------
This software is copyright Silent Software (Benjamin Brown) 2014 and 
is licensed under the GNU GPLv3. Please see the included licenses.txt 
for more details.

Use at your own risk, no warranty is given that the software is fit 
for any purpose.


** WARNING **
-------------
"GigaScreen" output created by this program and displayed on a real 
ZX Spectrum contains flashing images and will flicker heavily.
It is recommended you do not use the GigaScreen features if you are 
affected by flashing images.


Distribution
------------
Distribution of the source and compiled code is granted to World of 
Spectrum (www.worldofspectrum.org) and Silent Software (Development) 
only.
Should you wish to distribute this software and are not either of
the above parties please provide a link to the software home page
<http://www.silentdevelopment.co.uk> - do not directly link to the
software zip distribution - leeching is not very polite. 
Alternatively, host the zip file yourself, but a link to the 
aforementioned website must be clearly visible near to the download
link. 
If you wish to distribute this via physical medium, e.g. CD please 
contact me instead - I want a copy of your mag/cd/disk/tape(?!) :))


Known Issues
------------
* A number of video formats cannot be converted - this is down to 
Java's poor cross platform codec support in JMF. Use the VLC 
Video Import Engine option in Advanced Options option.
* Images that are not resized and whose height or width cannot be 
divided by 8 will not have the last 8 pixels in the column or row 
(respectively) converted correctly since a colour attribute block
needs to be an entire 8x8 pixel block. This only affects non
resized images.


What is Image to ZX Spectrum?
-----------------------------
Image to ZX Spec(trum) is a utility to convert image and video 
files to a REAL ZX Spectrum as a slideshow/video, to a Spectrum 
compatible file format for games development (SCREEN$ "scr" format) 
or create retro art posters (images of any size - memory limited - can 
have a Spectrum effect applied to them).


How to run
----------
If you have Java installed just double click the Img2ZXSpec.jar
file (like you would a regular .exe file). If you don't have Java
you need to get it - <http://www.java.com/getjava>
The easiest way to learn how to use it is just to play with it, you
can even change most image options during image conversion.
For information on advanced features read on...


General Options Tab
-------------------
Open the control panel "Options - Control" panel or click the gear icon.

### Image Export (.png/.jpg) 

This is the file format to be used for regular (PC) image export.
PNG and JPEG are currently the only true image formats available. PNG has 
one benefit over JPEG format in that it does not suffer image degradation
so it has been chosen as the default image format. Enabling the 256x192 
resize option will force all images to be reduced to this size, 
otherwise images can be as large as memory allows.

### Tape/Slideshow/Video Loader

This is the ZX Spectrum software to load your converted images that are
saved in the .tap (tape) output file. Typically the "Simple" option will 
suffice for Spectrum emulators and individual images. Buffered USR0 provides 
a better solution  for a real Spectrum and requires at least a 128K model 
to use, but removes attribute flicker commonly seen during video playback. 
GigaScreen USR 0 is also 128K only but is intended specifically to view the
GigaScreen 102 colour images and is not compatible with video or regular
Spectrum images. 
WARNING: GigaScreen images display with this loader flicker *a lot* and the
image display quality is poor owing to flashing two images quickly using BASIC. 
A hardware or machine code viewer will display GigaScreen SCR images more 
accurately and with a lot less flicker (e.g. the built in viewer in Fatware).

All built-in loaders allow you to specify the frame delay between images 
showing before the video/slideshow starts on the Spectrum. Alternatively you 
can add  your own custom loader by choosing the "Custom" droplist option. 
There are no built-in loaders suitable for GigaScreen palette images so a 
custom loader must be provided if you want to use GigaScreen files on tape.
See the FAQ note for more information on video support.

### Threads per CPU

Warning - do not change this setting during processing unless you want to
abort current work! This setting specifies how many threads convert images
at once PER CPU/CORE on your computer, so a droplist setting of 3 on a dual 
core machine is 6 threads. Usually the best settings for this are 1 or 2
for single cores and 2 or 3 for dual cores.
IMPORTANT - the more threads you have the more memory you will need 
simultaneously, this is especially important when you consider that the 
original sized image, the work in progress images and all the working is
held in memory at the same time until a result is produced! Use this setting
judiciously!

### Showing the Frames Per Second (FPS)

Enabling this will show on the work in progress preview the current rate at 
which images are being converted in real time. The calculation is worked out 
by taking an average every 2 seconds as to how much new completed work has been 
added to the work result queue. This option adds virtually no CPU overhead and
is useful for determining the best Threads per CPU value to use.

### Showing the Work In Progress (WIP) Preview

This enables the preview of images being converted AND the FPS counter if it too
is enabled. Disabling this option may provide a small improvement to the 
conversion performance.

### Output Options

* SCR (.scr/screen)
This is the file format representing a snapshot of a real Spectrum's
graphics memory area. This is not much use without a special file 
viewer. Images using this format will automatically be resized to
256x192 pixels.

* TAP (.tap/tape file) 
This is for the real enthusiasts. Using this option will convert your
images to a slideshow, prepared as a ".tap" file suitable for emulators
or with conversion, real Spectrum hardware. On loading you will be
presented with a prompt informing you that it is the Image to ZX Spec
program and that you can choose the delay between images (or frames in
the case of video) for the slideshow. Images using this format will 
automatically be resized to 256x192 pixels.

* Text Export (.txt file)
This is used to store the raw UTF-8 text data of any Character Converter 
dithered images. In the case of a converted video this file contains many 
frames appended after each other. Note that the Character Converter can
also have output exported as regular images.

* Anim Gif Export (.gif file)
This is the most convenient way of viewing converted videos on a PC
and produces a looping slideshow/animation of the chosen images or
video file. Images using this format will automatically be resized to
256x192 pixels.

* Anim Gif Frame Delay (millisecs)
This is the delay between changing images/frames of the Anim Gif Export.


Pre-Process Options
-------------------
These are basic options to allow you to improve the source image or video
before conversion to a Spectrum format. These options affect images in 
memory only and do not change your source files.

### Scaling
Scales the source image to the Spectrum's native resolution, with options to
keep the image proportional. Selecting "None" will create a "Spectrumified" 
image but it is not compatible with a real Spectrum - use png/jpeg export
output to view images converted with this option.

### Video Sampling Rate
Converting video to a Spectrum requires some skill balancing the frame rate
so that playback speed is correct. Changing this value increases or reduces
the number of frames per second played back by the spectrum. Specify too
large an amount and a humble (real) Spectrum cannot keep up, too low an
amount causes the video to look (even more) jerky. This value closely ties
in with the frame delay value you must use with the video loader software
on the Spectrum. In an ideal world for 10 FPS video you would specify
10 as the "video sampling rate" and 5 for the "frame delay" in the Spectrum
video loader. The value 5 is determined because a PAL Spectrum has 50
screen refreshes per second and 50/10 = 5. Unfortunately real hardware often
can't keep up - see the FAQ for recommended values instead.

### Saturation Change
Alters the image saturation

### Contrast Change
Alters the image contrast

### Brightness Change
Alters the image brightness


Dither Options
--------------

### Dithering Mode
These are the error diffusion dithering "patterns" of dots you can 
see when an image is converted. There are a number of different 
patterns (algorithms) to choose from and all have different benefits. 
In general, the "Low Error Atkinson" (or "Atkinson") and "Magic Square 4x4 
(Nasik)" tend to be the best monochrome and colour modes respectively, 
although you may need to enable some pre-processing options such as 
contrast change to get the best results especially for the non 
Ordered/Magic dithers. To see a preview of all dithering modes choose "View 
Dither Options" from the Options tool bar drop list. Note that if you are 
converting video then the preview will show a random frame from the first
few seconds.
The character converter dither which attempts to map the graphics to the ZX 
spectrum character set, this tends to work best with pictures that are not 
"busy" and have high constrast.

### Colour Modes
Choose whether to have two colour dithered images "Monochrome", "Full Palette" 
for the full Spectrum palette or "GigaScreen" which uses persistence of vision 
to generate 102 colours. GigaScreen works on a real Spectrum by flashing two 
specially created images in sequence, the "blur" (persistence of vision) 
produces the extra colours. The file representation a Spectrum "scr" file is
is two regular scr images saved as one file. As a jpg or png file these are 
represented as the actual colours. FatWare has the the capability to display 
these images on a real Spectrum, however there is a GigaScreen loader included
(see General Options, Tape/Slideshow/Video Loader above) but this is not 
recommended as it is a very poor and flickery solution.
Monochrome mode tends to make detail much easier to see but with the obvious 
colour sacrifice. Monochrome conversion tends to be at least 3 times faster 
than colour mode conversion.

### Attribute Choice
A technical limitation of the ZX Spectrum was that it only had 15 colours 
(16 if you count black twice). These colours were divided into two sets 
which where generated by taking 8 initial colours with full
brightness and halving the voltage, producing another 7 (8) colours. 
Converted image colours can only be in one of these two sets. When the 
converter detects that pixels being analysed are in both sets it decides 
based on the favouristism mode which set it should use for the final result. 
The "favouristism" modes are Half Brightness, Full Brightness or Most Popular 
Colour, the latter mode determines the set for the whole 8x8 pixel attribute 
block by finding which set the most popular (frequently occurring) colour is 
in. The half and full modes determine that if an analysed attribute block 
has both half or bright set colours then favour the user selected colour set. 
Finally, the "force" modes force all colours to either be in the half 
or full brightness set regardless, depending on which force option is chosen.
A special "Reduced Colour Set" is also available which omits the magenta and
turquoise colours of the Spectrum palette but is otherwise identical to the
"Force Half Brightness" option.

### GigaScreen Attribute Choice
GigaScreen images use two regular ZX Spectrum images (screens). This option
allows you to determine which colour brightness set (as regular attribute 
choice) is used on each screen. Choosing "One screen bright, One screen half 
bright" is not recommended as the conversion calculations involved are slower 
and the "GigaScreen SCR Export Attribute Order" option (see Advanced Options) 
cannot be used due to technical limitations.

### Monochrome INK Colour
Option to choose one of the two colours when the Monochrome option in Colour 
Modes (see above) is used. This is the Spectrum "INK" colour

### Monochrome PAPER Colour
Option to choose the second colour when the Monochrome option in Colour Modes 
(see above) is used. This is the Spectrum "PAPER" colour.

### Threshold (Monochrome only)
This determines the degree to which colours in the picture should be
considered as black for the purposes of the Monochrome colour mode.
The lower the value, the less colours are black (or whatever ink 
colour is chosen) and vice versa. This setting can also be used by
the Character Converter dither mode.


Advanced Options
------------------------

### Serpentine Error Diffusion
This mode causes any error diffusion dither algorithms to switch directions
on each consequtive pixel line, as opposed to "book" style reading of the
pixels which reads only left to right. The result is that some dithers
may produce better results with less perceivable dithering patterns in the
final image.

### Constrain Error Diffusion To Attribute
The nature of error diffusion is to spread "error" trying to get find an 
exact Spectrum colour match for a single pixel to surrounding pixels. The
Spectrum palette is problematic in that colours can only be changed on each
8x8 pixel attribute block, spreading an error outside of this block may cause
a cascading effect and create even worse results in the next block. This
option prevents the error exceeding 8x8 pixels at the expense of a grid 
pattern appearing across the image, but often this may be preferable to
an image where a number of attribute blocks have visibly wrong colours.

### Prefer Detail To Colour Accuracy
This options hints to the converter to favour darker colours in the source image 
as the Spectrum's black, as opposed to blue or another colour. The same happens
for light colours but with white.
The result is that converted images have slightly more contrasting detail (i.e.
black and white) which significantly improves most images, at the expense of
colour accuracy. Typically cartoons, bright images, and images with a lot of 
similars colour benefit the most from this. It also helps reduce Spectrum 
attribute  artifacts

### GigaScreen SCR Export Attribute Order
This option only affects the SCR GigaScreen export (see General Options, SCR 
Export) output but allows you to determine which screen order certain colours 
are in. E.g. A pink may be made up of white in screen 1, red in screen 2.
You can choose to order by brightness (may reduce flicker), contrast (may reduce 
attribute artifact edges) or hue (may helps reduce artifact edges, like contrast
but similar colours rather than same colours will be grouped on one screen).
By default "None" is the default which is faster and in general is "good enough".

### Ordered Dither Intensity
This determines the extent to which an image will be dithered. The lower the 
value, the less the dither. Increasing the intensity will also increase the 
brightness of the image (a side effect).

### Video Import Engine
By default Image to ZX Spec's built in video decoder (JMF) doesn't support a large 
variety of formats (such as mpeg or mp4). By choosing the option "VLCJ" you can 
plug in your local install of VLC video player (assuming you have a compatible 
version). Selecting this option will prompt you to locate the VLC folder on your 
computer. Note that if you are using 64 bit Java you need to use the 64 bit version 
of VLC - this is not the default download version of VLC. VLC is resource heavy
and writes its output files to temporary disk space, it is recommended you use this
option with caution.


FAQ
---
### My video won't convert!
The built in Java Media Framework (JMF) has limited codec compatibility 
(see "Known Issues" higher up). You can do either of two things to fix this:

1. Under the Advanced Options tab choose VLC from the drop down instead of JMF
and select the directory location of your machine's VLC install. Note you need
to be using a version of VLC 2.1.3 or after and matches your machine 
architecture (i.e. 32 or 64 bit).

2. Open VirtualDub and convert it to an uncompressed or Radius Cinepak codec AVI.
Whilst you're doing this reducing the image size to 256x192 will be useful 
as it will reduce the video buffering time of Image to ZX Spec.
A full list of JMF video (CROSS PLATFORM ONLY) codec formats is provided
here: 
<http://java.sun.com/javase/technologies/desktop/media/jmf/2.1.1/formats.html>
Note Motion JPEG doesn't seem to actually work at all :(

### Not enough PROCESS memory to load ALL of the UNCOMPRESSED video into RAM. 
Note: 32 bit machines are limited to about 700Mb-1.2GB. 
To increase the memory settings open a command prompt at the location of the 
jar file and type java -Xmx1500m Img2ZXSpec.jar where "1500" is the memory in 
megabytes. This should work but may be only useful for 64 bit machines that 
allow more process memory.

### What frame rate should the video be converted at?
This rate may vary depending on how you are using the video - using the video 
player in an emulator, the Spectrum Video Loader's "Frame Delay" should 
typically be between 5 (exact) and 10 for 100% speed emulation using a "Video 
Sampling Rate" of 10 FPS.
On real hardware it has been determined for a 100% correct playback 
speed a frame rate of *7.14* FPS for the "Video Sampling Rate" and a value of 
*1* for the Spectrum Video Loader's "Frame Delay" is *usually* needed. This 
was tested using a Spectrum +3, a DivIDE Plus to play the video back from, and 
the Buffered USR 0 (128K) player.

### How do I write a custom loader?
The loaders can be written in Spectrum BASIC and typically converted by one
of the many .bas to .tap converters available on World of Spectrum. See the
Image to ZX Spec source code "simple.bas" text file for an example. More
advanced users may wish to write a loader in assembly/machine code for display
of GigaScreen images or to provide enhanced features.

### Why do you spell Colour "colour", even in the source?
I am British, the Sinclair Spectrum is also British - I see no need for
American spellings :)

Any comments? Please drop them on my blog at
<http://www.silentdevelopment.co.uk>