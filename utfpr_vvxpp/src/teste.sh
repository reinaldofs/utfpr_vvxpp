echo "Parametro 1 $1"
echo "Parametro 2 $2"
echo "Parametro 3 $2"
echo "Arquivo ${file_uri}"

fname=`echo $2 | cut -d\. -f1`
fname=${fname}

echo "classe $fname"