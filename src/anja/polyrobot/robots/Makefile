# Makefile für das anja-Projekt

PROJ_ROOT=../../..

include $(PROJ_ROOT)/Make.$(OSNAME)

SRC=	Robot.java		\
	ScenePosition.java	\
	PointRobot.java		\
	ConvexRobot.java

OBJ=$(SRC:.java=.class)

.SUFFIXES:	.java .class .ps .gif .html

.java.class:
	$(JAVAC) $(JAVACOPT) $<

all:	$(OBJ)

clean:
	$(RM) *.class
