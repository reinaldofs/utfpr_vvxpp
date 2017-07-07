// Ler nome, sexo e idade. Se sexo for feminino e idade menor que 25. Imprimir o nome da pessoa
// e a palavra ACEITA. Caso contrario imprimir NAO ACEITA. 

class idadesexo{

	int start(){
		
		int idade;
		string sexo;
		string nome;
		
		print "digite o nome: \n";
		read nome;

		print "digite a idade: \n";
		read idade;
		
		print "digite o sexo (F - feminino ou M - masculino): \n";
		read sexo;
		
		if(sexo == "F" ){
			
			if(idade < 25){

				print "NAO ACEITA";
			
			}else{
				print nome;
				print " ACEITA";
			}

		}{
			print "ACEITO(A)";
		}
		
		return 0;
		
	}
}