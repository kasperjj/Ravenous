#!/bin/sh

###
 # Copyright (c) 2008, Solido Systems
 # All rights reserved.
 #
 # Redistribution and use in source and binary forms, with or without
 # modification, are permitted provided that the following conditions are met:
 #
 # Redistributions of source code must retain the above copyright notice, this
 # list of conditions and the following disclaimer.
 #
 # Redistributions in binary form must reproduce the above copyright notice,
 # this list of conditions and the following disclaimer in the documentation
 # and/or other materials provided with the distribution.
 #
 # Neither the name of Solido Systems nor the names of its contributors may be
 # used to endorse or promote products derived from this software without
 # specific prior written permission.
 #
 # THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 # AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 # IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 # ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 # LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 # CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 # SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 # INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 # CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 # ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 # POSSIBILITY OF SUCH DAMAGE.
###

# Add a line for each package that needs to be compiled

DIRS="./com/solidosystems/ravenous/http/\
      ./com/solidosystems/ravenous/client/\
      ./com/solidosystems/ravenous/host/\
      ./com/solidosystems/ravenous/web/\
      ./com/solidosystems/ravenous/template/\
      ./com/solidosystems/ravenous/db/\
      ./com/solidosystems/ravenous/db/persistence/\
      ./com/solidosystems/ravenous/server/"

# The directory into which compiled classes are placed
BUILD="build"
JFLAGS="-g -Xlint:all"
LIBS="simple-4.1.11.jar:SnakeYAML-1.2.jar"
ENCODING="UTF8"

function clean {
#	echo rm -rf ${BUILD}
	rm -rf ${BUILD}
}

function build {
	if [ ! -e ${BUILD} ]; then mkdir ${BUILD}; fi
	for dir in $DIRS; do
#		echo javac ${JFLAGS} -d ${BUILD} $dir/*.java
		javac ${JFLAGS} -cp "${LIBS}:${BUILD}:." -encoding ${ENCODING} -d ${BUILD} $dir/*.java
#		javac ${JFLAGS} -encoding ${ENCODING} -d ${BUILD} $dir/*.java
	done
}

function build_fast {
	if [ ! -e ${BUILD} ]; then build; return; fi
	for dir in $DIRS; do
		build_files=""
		for java_file in $dir/*.java; do
			class_file="./build/${java_file%\.java}.class"
			if [ ! "$class_file" -nt "$java_file" ]; then
				build_files="${build_files} ${java_file}"
			fi
		done
		if [ -n "$build_files" ]; then
#			echo javac ${JFLAGS} -d ${BUILD} ${build_files}
			javac ${JFLAGS} -cp "${LIBS}:${BUILD}:." -encoding ${ENCODING} -d ${BUILD} ${build_files}
#			javac ${JFLAGS} -encoding ${ENCODING} -d ${BUILD} ${build_files}

		fi
	done
}

function make_jar {
    mkdir tmpjar
    cp -f SnakeYAML-1.2.jar ./tmpjar/
    cd tmpjar
    unzip -q SnakeYAML-1.2.jar
    cp -Rf org ../build/
    rm -Rf *
    cp -f ../simple-4.1.11.jar ./
	unzip -q simple-4.1.11.jar
	cd org
	cp -Rf simpleframework ../../build/org/
	cd ..
	rm -Rf *
	cp -f ../h2-1.1.115.jar ./
	unzip -q h2-1.1.115.jar
	cd org
	cp -Rf h2 ../../build/org/
	cd ..
	cd ..
	rm -Rf tmpjar
	cp -Rf icons ./build/
	jar cfm ravenous2dev.jar DevManifest.txt -C build .
}

shopt -s nocasematch

if [ -n "$1" ]; then
	if [[ "$1" == "all" ]]; then
		clean
		build
	elif [[ "$1" == "clean" ]]; then
		clean
	elif [[ "$1" == "fast" ]]; then
		build_fast
	elif [[ "$1" == "jar" ]]; then
		clean
		build
		make_jar
	else
		echo "Valid targets: all clean fast jar"
	fi
else
	build_fast
fi
