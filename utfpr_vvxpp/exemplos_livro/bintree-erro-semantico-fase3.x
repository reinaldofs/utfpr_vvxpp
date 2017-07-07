/***********************************************
Esse programa implementa uma arvore de busca binaria
*************************************************/

class bintree { /* na da arvore binaria */


class data { // define um classe aninhada do tipo data (dia, mes ano)
int dia, mes, ano;

constructor()  // construtor 1, sem parametros
{
   ano = 1900; // inicializa em 1/1/1900
   mes = 1;
   dia = 1;
}

constructor(int d, int m, int a) // construtor 2 - dia mes e ano como
{                // parametros 
   dia = d;
   mes = m;
   ano = a;
}

int compara(data x) // compara duas datas 
{             // < 0 - menor > 0 maior 0 igual
   if ( ano < x.ano) return -1;
   if ( ano > x.ano) return 1;
   if ( mes < x.mes) return -1;
   if ( mes > x.mes) return 1;
   if ( dia < x.dia) return -1;
   if ( dia > x.dia) return;
   return 0;
}


} // final classe data


// variaveis da classe bintree

data key;       // chave de comparacao
bintree left,right; // referencia para os filhos

constructor(data x)
{
   key = x;
   left = null;
   right = null;
}

int insert(data k) // adiciona um elemento na arvore
{
int x;

   x = k.compara(key);
   if (x < 0)
   {
      if (left != 0)
         return left.insert(k);
      left = new bintree(k);
      return 1;
   }
   if (x > 0)
   {
      if (right != null)
         return right.insert(k);
      right = new bintree(k);
      return 1;
   }
   return 0;
}


int treeprint(int x) // imprime a arvore
{
int i;

   if (left != null)
      i = left.treeprint(x+4);
   for (i = 0; i < x; i = i + 1)
      print " ";
   print key + "/" + key.mes + "/" + key.ano + "\n";
   if (right != null)
      i = right.treeprint(x+4);
}


int start()
{
bintree t;
int i, d, m, a;
data w;

   read d; read m; read a;
   w = new data(d, m, a);
   t = new bintree(w, 1);
   for (i = 0; i < 10; i = i + 1)
   {
      read d; read m; read a;
      w = new data(d, m, a);
      if (t.insert(w) == 0)
         print "Elemento ja existe\n";
   }
   i = t.treeprint(0);
   return 0;
}

}
