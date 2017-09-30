# washed-ashore
Cleanup of a several-year-old 3D project written in java.

![screenshot of terrain](terrain_game.png)

I wrote the majority of this code my junior year of High School (2012-2013), after taking AP computer science the previous year, but moving OS's several times has messed up date-created metadata, so I don't know exactly when.

The program is interesting, but the code is a mess. It only uses 2D draw calls, performing all the projection math from 3D. All of the geometry generation and rendering code is in one file, which also reads the player's keyboard and moves their ship/camera. It isn't exactly easily readable or easy to modify.

I'm currently just cleaning up the formatting and removing dead/commented-out code, but there's plenty of areas where a section of code is repeated almost exactly, with minor changes in values. This is especially true of the geometry generation sections, where a series of nested loops trace different kinds of pseudo-cylinders.

Once the code is readable and rationally organized, I'm considering porting it to C and OpenGL.

...but I'm going to be late to class, so I'd better run now! :D
