# SDC
## Introduction
The code is composed by IntelliJ IDEA 2022.2.2 (Edu). 

This project realized an implementation of SDC scheduling algorithm.
The detailed implementation is in source file [SDC](src/scheduler/SDC.java).

All the constraints are transferred into inequality equations.
All these constraints combine the system of difference constraints (SDC).
Then it will be solved by the [SCP Solver](http://scpsolver.org/).
## File
1. [SDC](src/scheduler/SDC.java): detailed implementation of SDC scheduler.
2. [Equation](src/scheduler/Equation.java): class used to construct the constraints equations
## How to run the Solver
### One method
You can just **clone** this project and **open** it with IntelliJ IDEA, **configure** the parameter 
you want to give to Main() and then **run**.
### Alternative method
If you have already packaged the project, just run the command 
```
java -jar test1.jar ~/path/to/dotfile ~/path/to/resource
```
## Note
There are three implementations of solver in SDC.
* high level interface SCP solver.
* low level interface SCP solver.
* [LP solver](https://lpsolve.sourceforge.net/5.5/). This is the default one. 


Currently, High and low level interface SCP solver can only run when the objective function is not 
set or does not contain the variable in the system. Otherwise, *WARNING: model is unbounded* will be given.

LP Solver can run, if the lpwizard solver testing program in the [Main](src/scheduler/Main.java) is kept. 
Otherwise, the exception *UnsatisfiedLinkError* can be thrown. I do not know why it is so, if you have 
the same problem as I mentioned above, I strongly suggest you to keep this part of code