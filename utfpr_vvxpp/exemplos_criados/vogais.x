// Receber do teclado uma mensagem e imprimir quantas letras A, E, I, O, U tem esta mensagem.
// Considerar minúscula e maiúscula.

class vogais{
	
	int start(){
		
		//vogais vogais;
		//vogais = new vogais();
		
		int n,i,na,ne,ni,no,nu;
		na = 0;
		ne = 0;
		ni = 0;
		no = 0;
		nu = 0;
		
		print "Digite a quantidade de letras da string: ";
		read n;
		
		string vetor[][];
		vetor = new string[n][1];
		
		for (i = 0; i < n; i = i + 1){
			print "Digite uma letra: ";
			read vetor[i][0];
		}
		
		
		for (i = 0; i < n; i = i + 1){
			
			string key;
			key = vetor[i][0];
			
			//Maiusculas
			if (key == "A"){
			
				na = na + 1;
			
			}else{
				if (key == "E"){
				
					ne = ne + 1;
					
				}else{
				
					if (key == "I"){
					
						ni = ni + 1;
						
					}else{
					
						if (key == "O"){
						
							no = no + 1;
							
						}else{
						
							if (key == "U"){

								nu = nu + 1;

							}
						}
					}
				}
			}
			
			// Minusculas
			if (key == "a"){
			
				na = na + 1;
			
			}else{
				if (key == "e"){
				
					ne = ne + 1;
					
				}else{
				
					if (key == "i"){
					
						ni = ni + 1;
						
					}else{
					
						if (key == "o"){
						
							no = no + 1;
							
						}else{
						
							if (key == "u"){

								nu = nu + 1;

							}
						}
					}
				}
			}
			
		}

		print "Letras A:" + na + "\n";
		print "Letras E:" + ne + "\n";
		print "Letras I:" + ni + "\n";
		print "Letras O:" + no + "\n";
		print "Letras U:" + nu + "\n";

		return 0;

	}

}