// calcula o fatorial de um número

class fatorial{

	int start()
	{
		int fat, n;
		print "Insira um valor para o qual deseja calcular seu fatorial: ";
		read n;

		for(fat = 1; n > 1; n = n - 1){
			fat = fat * n;
		}

		print "\nFatorial calculado: " + fat;

		return 0;

	}

}