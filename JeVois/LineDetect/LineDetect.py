import numpy as np
import cv2 as cv
import sys
try:
    import libjevois as jevois
except Exception as e:
    print("Could not find JeVois!")

PY3 = sys.version_info[0] == 3

# params
doOtsu = False
doGuassianBeforeThreshold = True
doGuassianAfterThreshold = True
guassianAmount = 21
# see https://docs.opencv.org/2.4/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html?highlight=findcontours#findcontours
mode = cv.RETR_LIST
method = cv.CHAIN_APPROX_SIMPLE

if PY3:
    xrange = range

# Line detection
#
# @author Max H
#
# @videomapping YUYV 1280 1024 7.5 YUYV 1280 1024 7.5 MaxH LineDetect
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


class LineDetect:
    # ###################################################################################################
    # Constructor
    def __init__(self):
        # Instantiate a JeVois Timer to measure our processing framerate:
        self.timer = jevois.Timer("processing timer", 100, jevois.LOG_INFO)

        # a simple frame counter used to demonstrate sendSerial():
        self.frame = 0

    # ###################################################################################################
    # Process function with no USB output
    def processNoUSB(self, inframe):
        # Get the next camera image (may block until it is captured)
        # and here convert it to OpenCV BGR. If you need a
        # grayscale image, just use getCvGRAY() instead of getCvBGR().
        # Also supported are getCvRGB() and getCvRGBA():
        # inimg = inframe.getCvBGR()

        # Start measuring image processing time (NOTE: does not account for input conversion time):
        self.timer.start()

        jevois.LINFO("Processing video frame {} now...".format(self.frame))

        # TODO: you should implement some processing.
        # Once you have some results, send serial output messages:

        # Get frames/s info from our timer:
        fps = self.timer.stop()

        # Send a serial output message:
        jevois.sendSerial("DONE frame {} - {}".format(self.frame, fps))
        self.frame += 1

    # ###################################################################################################
    # Process function with USB output
    def process(self, inframe, outframe):
        # Get the next camera image (may block until it is captured)
        # and here convert it to OpenCV BGR. If you need a
        # grayscale image, just use getCvGRAY() instead of getCvBGR().
        # Also supported are getCvRGB() and getCvRGBA():
        inimg = inframe.getCvBGR()

        # Start measuring image processing time
        # (NOTE: does not account for input conversion time):
        self.timer.start()

        # Detect edges using the Laplacian algorithm from OpenCV:
        #
        # Replace the line below by your own code! See for example
        # - http://docs.opencv.org/trunk/d4/d13/tutorial_py_filtering.html
        # - http://docs.opencv.org/trunk/d9/d61/tutorial_py_morphological_ops.html
        # - http://docs.opencv.org/trunk/d5/d0f/tutorial_py_gradients.html
        # - http://docs.opencv.org/trunk/d7/d4d/tutorial_py_thresholding.html
        #
        # and so on. When they do "img = cv.imread('name.jpg', 0)"
        # in these tutorials, the last 0 means they want a
        # gray image, so you should use getCvGRAY() above in these cases.
        # When they do not specify a final 0 in imread()
        # then usually they assume color and you should use getCvBGR() above.
        #
        # The simplest you could try is:
        #    outimg = inimg
        # which will make a simple copy of the input image to output.
        outimg = processImg(inimg)

        # Write a title:
        cv.putText(outimg, "JeVois LineDetect", (3, 20), cv.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255))

        # Write frames/s info from our timer into the edge map (NOTE: does not account for output conversion time):
        fps = self.timer.stop()
        height = outimg.shape[0]
        # width = outimg.shape[1]
        cv.putText(outimg, fps, (3, height - 6), cv.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255))

        # Convert our output image to video output format and send to host over USB:
        outframe.sendCv(outimg)

        # Example of sending some serial output message:
        jevois.sendSerial("DONE frame {}".format(self.frame))
        self.frame += 1

    # ###################################################################################################
    # Parse a serial command forwarded to us by the JeVois Engine, return a string
    def parseSerial(self, str):
        jevois.LINFO("parseserial received command [{}]".format(str))
        if str == "hello":
            return self.hello()
        return "ERR Unsupported command"

    # ###################################################################################################
    # Return a string that describes the custom commands we support
    # for the JeVois help message
    def supportedCommands(self):
        # use \n seperator if your module supports several commands
        return "hello - print hello using python"

    # ###################################################################################################
    # Internal method that gets invoked as a custom command
    def hello(self):
        return "Hello from python!"


def angle_cos(p0, p1, p2):
    d1, d2 = (p0-p1).astype('float'), (p2-p1).astype('float')
    return abs(np.dot(d1, d2) / np.sqrt(np.dot(d1, d1)*np.dot(d2, d2)))


def find_squares(img):
    squares = []
    for gray in cv.split(img):
        for thrs in xrange(0, 255, 26):
            if thrs == 0:
                bin = cv.Canny(gray, 0, 50, apertureSize=5)
                bin = cv.dilate(bin, None)
            else:
                _retval, bin = cv.threshold(gray, thrs, 255, cv.THRESH_BINARY)
            bin, contours, _hierarchy = cv.findContours(bin, mode, method)
            for cnt in contours:
                cnt_len = cv.arcLength(cnt, True)
                cnt = cv.approxPolyDP(cnt, 0.02*cnt_len, True)
                if len(cnt) == 4 and cv.contourArea(cnt) > 1000 and cv.isContourConvex(cnt):
                    cnt = cnt.reshape(-1, 2)
                    max_cos = np.max([angle_cos(cnt[i], cnt[(i+1) % 4], cnt[(i+2) % 4]) for i in xrange(4)])
                    if max_cos < 0.1 or True:
                        squares.append(cnt)
    return squares


def processImg(img):
    # convert to B&W
    img = cv.cvtColor(img, cv.COLOR_BGR2GRAY)
    img = cv.cvtColor(img, cv.COLOR_GRAY2RGB)
    if doGuassianBeforeThreshold:
        # Guassian blur
        img = cv.GaussianBlur(img, (guassianAmount, guassianAmount), 0)

    # threshold color
    # strict threshold
    img = cv.threshold(img, 175, 255, cv.THRESH_TOZERO)[1]
    img = cv.cvtColor(img, cv.COLOR_BGR2GRAY)

    add = 0
    if doOtsu:
        add = cv.THRESH_OTSU
    # Otsu's thresholding
    # see https://docs.opencv.org/3.4/d7/d4d/tutorial_py_thresholding.html
    img = cv.threshold(img, 127, 255, cv.THRESH_BINARY + add)[1]

    if doGuassianAfterThreshold:
        # Guassian blur
        img = cv.GaussianBlur(img, (guassianAmount, guassianAmount), 0)

    img = cv.cvtColor(img, cv.COLOR_GRAY2RGB)

    squares = find_squares(img)
    cv.drawContours(img, squares, -1, (0, 255, 0), 3)
    return img


if __name__ == '__main__':
    img = cv.imread("test.png")
    afterImg = processImg(img)
    cv.imwrite("out.png", afterImg)
    try:
        cv.imshow('squares', afterImg)
        cv.waitKey()
    except Exception as e:
        pass
