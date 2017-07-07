fname=`echo $2 | cut -d\. -f1`
fname=${fname}

mkdir ../output
javac parser/langX.java
javac langXrt/Runtime.java
java parser.langX "$1"
java -jar ~/jasmin-2.4/jasmin.jar "$1.$fname.jas"
clear
java -classpath .:. $fname

mkdir "../output/$3"

cp "$1.arvore.txt" "../output/$3"
cp "$fname.class" "../output/$3"
cp "saida.txt" "../output/$3"
cp "$1" "../output/$3"

java -jar cobertura.jar "../output/$3/$2"

