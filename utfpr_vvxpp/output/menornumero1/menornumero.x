class menornumero{

	int start(){
		
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
		
		
		int min;
		
		min = vetor[0][0];
		
		for (i = 0; i < n ; i = i + 1) {
            if(vetor[i][0] < min) 
				min = vetor[i][0];
        }
		
		print "Menor valor = "+min+ " \n" ;
		
		return 0;
		
	}

}