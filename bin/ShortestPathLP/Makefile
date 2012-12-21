PROJ_ROOT=../..
PROJ_DIR=appsSwingGui/ShortestPathLP
TARGETDIR=ShortestPathLP
JAVACOPT=-target 1.5 -source 1.5

SRCDIRS=.

LIBS = anja/geom anja/util anja/swinggui

WMLFILES:=$(wildcard *.wml)

WEBFILES:=$(wildcard pics/*.JPG)

include $(PROJ_ROOT)/Make.Start

classes.zip: all
	$(RM) classes.zip
	cd ../.. && \
	$(ZIP) appsSwingGui/ShortestPathLP/classes.zip \
		anja/geom/*.class \
		anja/gui/*.class \
		anja/swinggui/*.class \
		anja/swinggui/polygon/*.class \
		anja/util/*.class \
		appsSwingGui/ShortestPathLP/*.class \
		appsSwingGui/SeidelTriag/*.class \
