import libjevois as jevois
import cv2
import numpy as np

def badPosterize(imageNormal):
    """
    Posterize the image through a color list, diving it and making a pallete.
    Finally, applying to the image and returning the image with a the new pallete
    :param imageNormal: CV opened image | imageNormal | The normal image opened with OpenCV
    """
    colorList = np.arange(0, 256)
    colorDivider = np.linspace(0, 255,3)[1]
    colorQuantization = np.int0(np.linspace(0, 255, 2))
    colorLevels = np.clip(np.int0(colorList/colorDivider), 0, 1)
    colorPalette = colorQuantization[colorLevels]
    return colorPalette[imageNormal]


def badPosterize2(imageNormal):
    lab= cv2.cvtColor(imageNormal, cv2.COLOR_BGR2LAB)

    #-----Splitting the LAB image to different channels-------------------------
    l, a, b = cv2.split(imageNormal)

    #-----Applying CLAHE to L-channel-------------------------------------------
    clahe = cv2.createCLAHE(clipLimit=3.0, tileGridSize=(8,8))
    cl = clahe.apply(l)

    #-----Merge the CLAHE enhanced L-channel with the a and b channel-----------
    limg = cv2.merge((cl,a,b))

    #-----Converting image from LAB Color model to RGB model--------------------
    final = cv2.cvtColor(limg, cv2.COLOR_LAB2BGR)
    return final

## Deep fry images
#
# @author Max H
#
# @videomapping YUYV 1280 1024 30 YUYV 1280 1024 15 MaxH DeepFrier
# @email
# @address 123 first street, Los Angeles CA 90012, USA
# @copyright Copyright (C) 2018 by Max H
# @mainurl
# @supporturl
# @otherurl
# @license GPL v3
# @distribution Unrestricted
# @restrictions None
# @ingroup modules
class DeepFrier:
    # ###################################################################################################
    ## Constructor
    def __init__(self):
        # Instantiate a JeVois Timer to measure our processing framerate:
        self.timer = jevois.Timer("processing timer", 100, jevois.LOG_INFO)

        # a simple frame counter used to demonstrate sendSerial():
        self.frame = 0

    # ###################################################################################################
    ## Process function with no USB output
    def processNoUSB(self, inframe):
        # Get the next camera image (may block until it is captured) and here convert it to OpenCV BGR. If you need a
        # grayscale image, just use getCvGRAY() instead of getCvBGR(). Also supported are getCvRGB() and getCvRGBA():
        inimg = inframe.getCvBGR()

        # Start measuring image processing time (NOTE: does not account for input conversion time):
        self.timer.start()

        jevois.LINFO("Processing video frame {} now...".format(self.frame))

        # TODO: you should implement some processing.
        # Once you have some results, send serial output messages:

        # Get frames/s info from our timer:
        fps = self.timer.stop()

        # Send a serial output message:
        jevois.sendSerial("DONE frame {} - {}".format(self.frame, fps));
        self.frame += 1

    # ###################################################################################################
    ## Process function with USB output
    def process(self, inframe, outframe):
        # Get the next camera image (may block until it is captured) and here convert it to OpenCV BGR. If you need a
        # grayscale image, just use getCvGRAY() instead of getCvBGR(). Also supported are getCvRGB() and getCvRGBA():
        inimg = inframe.getCvBGR()

        # Start measuring image processing time (NOTE: does not account for input conversion time):
        self.timer.start()

        # Detect edges using the Laplacian algorithm from OpenCV:
        #
        # Replace the line below by your own code! See for example
        # - http://docs.opencv.org/trunk/d4/d13/tutorial_py_filtering.html
        # - http://docs.opencv.org/trunk/d9/d61/tutorial_py_morphological_ops.html
        # - http://docs.opencv.org/trunk/d5/d0f/tutorial_py_gradients.html
        # - http://docs.opencv.org/trunk/d7/d4d/tutorial_py_thresholding.html
        #
        # and so on. When they do "img = cv2.imread('name.jpg', 0)" in these tutorials, the last 0 means they want a
        # gray image, so you should use getCvGRAY() above in these cases. When they do not specify a final 0 in imread()
        # then usually they assume color and you should use getCvBGR() above.
        #
        # The simplest you could try is:
        #    outimg = inimg
        # which will make a simple copy of the input image to output.
        outimg = badPosterize(inimg)

        # Write a title:
        cv2.putText(outimg, "JeVois DeepFrier", (3, 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255,255,255))

        # Write frames/s info from our timer into the edge map (NOTE: does not account for output conversion time):
        fps = self.timer.stop()
        height = outimg.shape[0]
        width = outimg.shape[1]
        cv2.putText(outimg, fps, (3, height - 6), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255,255,255))

        outimg = cv2.cvtColor(outimg, cv2.COLOR_BGR2RGB)

        # Convert our output image to video output format and send to host over USB:
        outframe.sendCv(outimg)

        # Example of sending some serial output message:
        jevois.sendSerial("DONE frame {}".format(self.frame));
        self.frame += 1

    # ###################################################################################################
    ## Parse a serial command forwarded to us by the JeVois Engine, return a string
    def parseSerial(self, str):
        jevois.LINFO("parseserial received command [{}]".format(str))
        if str == "hello":
            return self.hello()
        return "ERR Unsupported command"

    # ###################################################################################################
    ## Return a string that describes the custom commands we support, for the JeVois help message
    def supportedCommands(self):
        # use \n seperator if your module supports several commands
        return "hello - print hello using python"

    # ###################################################################################################
    ## Internal method that gets invoked as a custom command
    def hello(self):
        return "Hello from python!"
