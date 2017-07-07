class insertionsort{
	
	int condicional(int v1, int v2){
		if(v1 > v2) return 1;
		return 0;
	}
	
	int start(){
		
		insertionsort insert;
		insert = new insertionsort();
		
		
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
		
		
		for (i = 1; i < n; i = i + 1){
			
			int key;
			int j,x,z;
			key = vetor[i][0];
			j = i;
			
			int b; 
			
			for(z=0;1==1;z=z+1){
				b = insert.condicional(j,0); 
				x = j - 1;
				if(b == 1) 
					b = insert.condicional(vetor[x][0] , key);
				
				if(b == 1){
					vetor[j][0] = vetor[x][0];
					j = j - 1;
				}else{
					break;
				}
			}
			vetor[j][0] = key;
			
		}
		
		for (i = 0; i < n; i = i + 1){
			print "vetor["+i+"][0] = "+vetor[i][0]+ " \n" ;
		}
		
		return 0;
		
	}

}

