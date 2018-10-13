#!/usr/bin/env python

# Python 2/3 compatibility
import sys
PY3 = sys.version_info[0] == 3

if PY3:
    xrange = range

import numpy as np
import cv2 as cv


# params
doOtsu = False
doGuassianBeforeThreshold = True
doGuassianAfterThreshold = True
guassianAmount = 21

# see https://docs.opencv.org/2.4/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html?highlight=findcontours#findcontours
mode = cv.RETR_LIST
method = cv.CHAIN_APPROX_SIMPLE

def angle_cos(p0, p1, p2):
    d1, d2 = (p0-p1).astype('float'), (p2-p1).astype('float')
    return abs( np.dot(d1, d2) / np.sqrt( np.dot(d1, d1)*np.dot(d2, d2) ) )

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
                    max_cos = np.max([angle_cos(cnt[i], cnt[(i+1) % 4], cnt[(i+2) % 4] ) for i in xrange(4)])
                    if max_cos < 0.1 or True:
                        squares.append(cnt)
    for square in squares:
        print(square, cv.contourArea(cnt))
    return squares

def process(img):
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
    cv.drawContours( img, squares, -1, (0, 255, 0), 3 )
    return img

if __name__ == '__main__':
    img = cv.imread("test.png")
    img = process(img)
    cv.imshow('squares', img)
    cv.imwrite("out.png", img)
    cv.waitKey();
