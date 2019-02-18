# ParserRzdDesktop
Parser RZD desktop version

For Windows:
1. Set path for Java by command: set path=%path%;C:\Program Files\Java\jdk<set specify version>\bin
2. Set encoding CP1251 by command: chcp 1251
3. Compile program by command: javac -cp ".;libs/json-simple-1.1.jar";".;libs/jsoup-1.11.3.jar" Main.java
4. Launch program by command: java -cp ".;libs/json-simple-1.1.jar";".;libs/jsoup-1.11.3.jar" Main <from> <to> <date> <maxPrice>
