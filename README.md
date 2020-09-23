# ToucanSimple
This "simple" code shows how the batch run of DSSAT can be implemented in Java, on both Windows and Linux.
First, get your own copy of DSSAT software from https://apps.agapps.org/ide/serial/index.php/request?sft=3

## Windows
1. 

## Linux
You'll need to compile the DSSAT executable file (DSCSM047.EXE) first:
1. sudo yum install gcc-gfortran glibc-static git cmake
2. mkdir codebase
3. mkdir toucan
4. git clone https://github.com/dssat/dssat-csm-os
5. cd dscsm-csm-os
6. mkdir build
7. cd build
8. cmake ..
9. make
   * In case you get an error from SC_rm_file.f90, see the issue at https://github.com/DSSAT/dssat-csm-os/issues/33
 

