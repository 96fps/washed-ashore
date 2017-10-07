
build:
	mkdir -p build
	javac *.java -d build

run:
	cd build; java GameViewer3d; cd ..