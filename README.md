# LPLCompiler


## Tasks
This coursework involves mainly 3 tasks:
- Write java code to build AST's in `handbuilt` package
- Complete compile methods in the AST classes
- Complete implementation of `LPLParser`

The LPL grammar is provided in the `LPL.sbnf` file in the data folder. The grammar for this part of the coursework is LL(1).

## Compilation
For this project I will be using the `CLI` to compile and run the code. There are several steps necessary for the compilation via CLI to be successfull.

1. Set `SBNF.jar` and `SSM.jar` as global libraries in the project. These are external to the CW folder and should be set for compilation
2. Set the `CLASSPATH` enviorment variable. This should include. Path to SBNF: Path to SSM: Path to out/production/CourseworkPart1
3. Set `Junit` libraries from maven as global libraries. Coordinates filed `org.junit.jupiter:junit-jupiter:5.9.3`


### Task 1 compilation
 To have the AST printed simply from the root of the project run:
 - `javac src/handbuilt/Ex_a.java`
 - `java handbuilt.Ex_ap`


