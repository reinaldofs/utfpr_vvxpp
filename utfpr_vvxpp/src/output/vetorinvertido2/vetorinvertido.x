// recebe o tamanho do vetor e o vetor
// imprime o vetor original e o vetor invertido.

class vetorinvertido{

	int start(){

		int n,i;
		
		print "Numero de valores do vetor: ";
		read n;
		
		int vetor[][];
		vetor = new int[n][1];
			
		for (i = 0; i < n; i = i + 1){
			print "Digite os valores do vetor: ";
			read vetor[i][0];
		}

		// vetor original
		print "\n vetor original";
		
		for (i = 0; i < n; i = i + 1){
			print "\n"+vetor[i][0];
		}

		// vetor invertido
		print "\n vetor invertido";
		n = n - 1;
		for (i = n; i >= 0; i = i - 1){
			print "\n"+vetor[i][0];
		}

		return 0;
	}
}