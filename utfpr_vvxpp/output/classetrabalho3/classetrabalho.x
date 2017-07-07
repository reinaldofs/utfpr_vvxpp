// Ler o ano de nascimento de uma pessoa e iformar sua idade
// se a indade for menor que 16 anos informar não pode trabalho
// se a indade for entre 16 e 18 anos informar menor aprendiz
// se a idade for maior que 18 anos informar contrato de trabalho

class classetrabalho{

	int start(){
		
		int anonascimento, idade;
		
		print "digite o ano de nascimento: \n";
		read anonascimento;
		
		idade = 2017 - anonascimento;
		
		print "idade: " + idade + "\n";
		
		if(idade < 16){
			print "nao pode trabalhar";
			
			return 0;
		}
		
		if(idade > 18){
			print "contrato de trabalho";
			
			return 0;
		}
		
		if(idade >= 16){
		
			if(idade <= 18){
				print "menor aprendiz";
			}
		}
	
		return 0;
	}
	

}