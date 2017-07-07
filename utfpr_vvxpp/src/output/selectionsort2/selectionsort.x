class selectionsort{

	int condicional(int v1, int v2){
		if(v1 > v2) return 1;
		return 0;
	}
	
	int start(){
		
		selectionsort select;
		select = new selectionsort();
		
		
		int n,i;
		
		print "Numero de valores do vetor: ";
		read n;
		
		//n = 5;
		
		int vetor[][];
		
		vetor = new int[n][1];
		
		//vetor[0][0] = 5;
		//vetor[1][0] = 7;
		//vetor[2][0] = 3;
		//vetor[3][0] = 8;
		//vetor[4][0] = 1;
		
		print "Digite os valores do vetor: ";
		
		for (i = 0; i < n; i = i + 1){
			read vetor[i][0];
		}
		
		
		int j, min, aux;
		
		for (i = 0; i < (n-1); i = i +1) 
		{
		 	min = i;
		 	for (j = (i+1); j < n; j = j + 1) {
		   		if(vetor[j][0] < vetor[min][0]) 
			 	min = j;
		 	}
		 	if (vetor[i][0] != vetor[min][0]) {
		   		aux = vetor[i][0];
		   		vetor[i][0] = vetor[min][0];
		   		vetor[min][0] = aux;
		 	}
		}
		
		for (i = 0; i < n; i = i + 1){
			print "vetor["+i+"][0] = "+vetor[i][0]+ " \n" ;
		}
		
		return 0;
		
	}

}