// lê uma matriz de 3 x 3 elementos usando um comando for,
// multiplica cada elemento por 5 e imprime o resultado.

class matriz3x3{

	int start(){
	
		int i, j;
		int m[][];
		m = new int[3][3];
	
		 //captura os elementos
		 for(i = 0; i < 3; i = i + 1){

			for(j = 0; j < 3; j = j + 1){

				print "Elemento [" +i+ "]" + "[" +j+ "]" + "=";

				read m[i][j];
			}
		}

		 //EXIBIR VALORES ORIGINAIS
		 print "\n Valores Originais \n";

		 for(i = 0;i < 3; i = i + 1){

			for(j = 0; j < 3; j = j + 1){
				print ""+m[i][j];
				print ("\n");

			}

		}

		 //EXIBIR VALORES MULTIPICADOS
		 print "\n Valores Multiplicados \n";
		 for(i = 0;i < 3; i = i + 1){

			for(j = 0; j < 3; j = j + 1){

				m[i][j] = m[i][j] * 5;

				print ""+m[i][j];
				print ("\n");
			}
		 }
		 
		return 0;
	}
}