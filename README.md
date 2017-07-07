# UTFPR_VVXPP
APS – Verificação e validação - Compilador da linguagem X++<br>
Turma de 2014/02

## Pré Requisitos
  - Eclipse e git.
  - Instalar o plugin [JavaCC] no eclipse.
  - Baixar o [Jasmin].
  - NodeJS 8 e npm
  - Maven

## Clonar o Repositório
```sh
git clone https://github.com/reinaldofs/utfpr_vvxpp.git
```

## Gerar o Compilador e o Runtime
### 1º Passo
![N|Solid](https://cldup.com/leoOm2O8Xz.png)

### 2º Passo
```sh
cd utfpr_vvxpp/src
javac parser/langX.java
javac langXrt/Runtime.java
```



## Executar um programa .x
### 1º Passo
```sh
java parser.langX ../ssamples/olamundo.x
```
![N|Solid](https://cldup.com/jGKmB6KSAR.png)

### 2º Passo
```sh
java -jar ~/jasmin-2.4/jasmin.jar olamundo.x.olamundo.jas
```
![N|Solid](https://cldup.com/1LipzYh3GV.png)

### 3º Passo
```sh
java -classpath .:../src olamundo
```
![N|Solid](https://cldup.com/Kxt8eu5EQY.png)

### 4º Passo
```sh
cd ../cfg-server
npm install
npm start
```


## Referências
- [Como Construir um Compilador Utilizando Ferramentas Java]

   [JavaCC]: <http://www.devmedia.com.br/conheca-agora-e-baixe-o-plugin-do-javacc-para-eclipse/2311>
   [Jasmin]: <http://jasmin.sourceforge.net/>
   [Como Construir um Compilador Utilizando Ferramentas Java]:  <http://conteudo.icmc.usp.br/pessoas/delamaro/SlidesCompiladores/CompiladoresFinal.pdf>
   



