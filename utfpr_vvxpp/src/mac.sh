javac parser/langX.java
javac langXrt/Runtime.java
java parser.langX ../ssamples/olamundo.x
java -jar ~/jasmin-2.4/jasmin.jar ../ssamples/olamundo.x.olamundo.jas
java -classpath .:. olamundo