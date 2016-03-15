# Drawmetry 
![alt text](https://github.com/ecolban/Drawmetry/blob/master/src/images/logo64.png "Drawmetry logo")

## Overview
Drawmetry is a constraint propagation-based drawing tool used to create precise drawings, such as graphs, charts and diagrams, geometric figures, curves, and floor plans.

The user may express dependencies between the various points of a drawing, such as the distance from one endpoint of a line segment to the other, the angle at vertex of a triangle, horizontal alignment of a point relatively to another point. (Mathematically speaking, these points are point variables since they may take different values (or positions) at different times. Drawmetry automatically repositions any point variables so that the constraints are satisfied at all times. This is Drawmetry's most important feature.

The point variables are shown as green, orange, and red dots. Green dots are free, and can be dragged to any place in the drawing. Orange dots are constrained but have one degree of freedom. They can be dragged along a circle or a line. Red dots are fully constrained. They can only hold one or two discreet positions.  

## Runnable Jar File
A runnable jar file is in the jar folder of this project. Copy the jar file to own computer and launch by double-clicking on the file, or by typing 'java -jar drawmetry.jar' in a terminal window. 

## Example Drawings
In the examples folder of this project. You can view these files by opening them with the drawmetry tool. You can also just drag and drop them into the tool, which will add a copy of the drawing into another drawing. Since the save format of the files is SVG, the drawings can also be viewed in a web browser. However, point variables and constraints are extensions to standard SVG and are ignored by the web browser.


