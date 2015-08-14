import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;

/**
 * @author Jerome Mutterer
 * @date 2014 12 15 first release
 * @date 2015 01 14 added macro support and custom resolutions thanks to Jarom Jackson
 * @date 2015 02 03 added proper macro support using GenericDialog for options thanks to Wayne Rasband
 */

public class IJ_webcam_plugin implements PlugIn {

	Webcam camera;
	BufferedImage image;
	ImagePlus imp, imp2;
	ImageProcessor ip;
	int camID=0, width=0, height=0;
    int sample_interval = 1000 ;
    int iMax = 10;
	boolean grab=false, customSize=false;

	public void run(String s) {
		
		if (!showDialog())
			return;

		camera = Webcam.getWebcams().get(camID);

		if (null != camera) {
			Dimension[] sizes = camera.getViewSizes();
			Dimension s1 = sizes[sizes.length - 1]; 

			if (customSize && (width > 0) && (height > 0)) {
				Dimension[] customSizes = new Dimension[1];
				customSizes[0] = new Dimension(width, height);
				camera.setCustomViewSizes(customSizes);
				s1 = customSizes[0];
			} 
			
			camera.setViewSize(s1);
			camera.open();
			ip = new ColorProcessor(s1.width, s1.height);
			imp = new ImagePlus("Live (press Escape to grab)", ip);

			WindowManager.addWindow(imp.getWindow());

			imp.show();
            
            
            int i=0;
			while (null != imp.getWindow() && i < iMax) {

				if (camera.isImageNew()) {
					image = camera.getImage();
					imp2 = new ImagePlus("tmp", image);
					ip = imp2.getProcessor();
					imp.setProcessor(ip);
					imp.updateAndDraw();
                    if (IJ.escapePressed()) {
                        IJ.saveAs ("PNG",  "/Users/cje2/Data/IJTest_webcam/Snapper" + i + ".png");
                        i ++ ;
                        IJ.wait (sample_interval);
                    }

				}
			}
			imp.setTitle("Done");
			camera.close();
		}
	}

	boolean showDialog() {
		int n = 0;
		String[] cameraNames = new String[Webcam.getWebcams().size()];

		for (Webcam c : Webcam.getWebcams()) {
			cameraNames[n] = c.getName(); 
			n++;
		}

		GenericDialog gd = new GenericDialog("IJ webcam plugin...");
		gd.addChoice("Camera name", cameraNames, cameraNames[0]);
		gd.addCheckbox("Grab and return", false);
		gd.addCheckbox("Custom size", false);
        
        gd.addNumericField("Sample interval:", sample_interval / 1000, 0, 5, "s");
        gd.addNumericField("No of frames:", iMax, 0, 5, "frames");
        
        gd.addNumericField("Width:", width, 0, 5, "pixels");
		gd.addNumericField("Height:", height, 0, 5, "pixels");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		camID = (int) gd.getNextChoiceIndex();
		grab = (boolean) gd.getNextBoolean();
		customSize = (boolean) gd.getNextBoolean();
        sample_interval = (int) (1000.0 * gd.getNextNumber());
        iMax = (int) gd.getNextNumber();
		width = (int) gd.getNextNumber();
		height = (int) gd.getNextNumber();
        
		return true;
	}
}
