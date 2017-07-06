mkdir output
javac parser/langX.java
javac langXrt/Runtime.java
java parser.langX "$1"
java -jar ~/jasmin-2.4/jasmin.jar "$1.$2.jas"
clear
java -classpath .:. $2

mkdir "output/$3"

cp "$1.arvore.txt" "output/$3"
cp "$2.class" "output/$3"
cp "saida.txt" "output/$3"
cp "$1" "output/$3"
