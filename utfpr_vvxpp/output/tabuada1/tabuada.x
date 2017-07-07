class tabuada{

	int start(){
		
		int n,i;
		
		print "Informe o valor de que deseja a tabuada:";
		read n;
		
		for (i = 0; i <= 10; i = i + 1){
			print n+" x "+i+" = "+(n*i)+"\n";
		}
		
		return 0;
		
	}

}