package codegen;

import parser.*;

import semanalysis.*;

import symtable.*;

import syntacticTree.*;

import java.io.*;


public class CodeGen extends TypeCheck {
    static final String Jasextension = ".jas"; // extensao do arquivo a ser gerado
    String SourceFile; // nome do arquivo fonte
    String SourcePath; // diretorio do arquivo fonte
    String SourceAbs; // nome completo do fonte
    PrintWriter fp = null; // arquivo onde o codigo e escrito
    String breaklabel = null; // armazena rotulo de desvio de um break
    int currlabel = 0; // numero do proximo rotulo a ser gerado
    boolean hasStart = false; // indica se classe tem um metodo start
    boolean store = false; // indica se variavel esta sendo assinalada
    int stackSize; // variaveis que controlam o uso da pilha
    int stackHigh; // variaveis que controlam o uso da pilha

    public CodeGen() {
        super(); // chama construtor de TypeCheck
    }

    public void CodeGenRoot(ListNode x, String filename)
        throws SemanticException, GenCodeException {
        TypeCheckRoot(x); // faz analise semantica
        System.out.println("0 Semantic error found");

        int i;
        i = filename.lastIndexOf(File.separator); // pega so o nome do fonte
        SourceFile = filename.substring(i + 1);

        if (i < 0) {
            SourcePath = ""; // pega o caminho
        } else {
            SourcePath = filename.substring(0, i);
        }

        SourceAbs = filename; // guarda tbem o caminho completo
        CodeGenClassDeclListNode(x); // chama geracao para a raiz
    }

    // ------------- lista de classes --------------------------
    public void CodeGenClassDeclListNode(ListNode x) throws GenCodeException {
        if (x == null) {
            return;
        }

        CodeGenClassDeclNode((ClassDeclNode) x.node);
        CodeGenClassDeclListNode(x.next);
    }

    // ------------- declaracao de classe -------------------------
    public void CodeGenClassDeclNode(ClassDeclNode x) throws GenCodeException {
        Symtable temphold = Curtable; // salva tabela corrente
        boolean tempstart = hasStart; // salva variavel hasStart
        EntryClass c = null;
        EntryClass nc;
        EntryClass ns = null;
        String Filename = null;
        String sname;
        FileOutputStream os;
        PrintWriter fpOld = fp; // salva arquivo sendo gerado

        if (x == null) {
            return;
        }

        // acha a classe na tabela
        nc = (EntryClass) Curtable.classFindUp(x.name.image);
        ns = nc.parent; // pega superclasse

        // pega nome da superclasse
        if (ns == null) {
            sname = "java/lang/Object"; // nehuma superclasse
        } else {
            sname = ns.completeName();
        }

        Filename = SourceAbs + "." + nc.completeName() + Jasextension;

        try {
            // cria arquivo para gerar codigo da classe
            os = new FileOutputStream(Filename);
        } catch (FileNotFoundException e) {
            throw new GenCodeException("Cannot create output file: " +
                Filename);
        }

        fp = new PrintWriter(os);
        System.out.println("Generating " + Filename);

        // escreve o cabecalho do arquivo: .source .class e .super
        putCode(";------------------------------------------------");
        putCode("; Code generated by X++ compiler");
        putCode("; Version 0.1 - 2002");
        putCode(";------------------------------------------------");
        putCode(".source " + SourceFile); // escreve .source
        putCode(".class public " + nc.completeName()); // .classe
        putCode(".super " + sname); // e .super

        Curtable = nc.nested; // tabela corrente = tabela da classe
        hasStart = false;

        CodeGenClassBodyNode(x.body);

        if (hasStart) { // verifica se classe tem metodo start
            createMain();
        }

        // fechar arquivo para classe
        fp.close();
        fp = fpOld; // recupera arquivo anterior
        Curtable = temphold; // recupera tabela corrente
        hasStart = tempstart; // recupera flag
    }

    // ------------- corpo da classe -------------------------
    public void CodeGenClassBodyNode(ClassBodyNode x) throws GenCodeException {
        EntryMethod l;

        if (x == null) {
            return;
        }

        CodeGenClassDeclListNode(x.clist);
        CodeGenVarDeclListNode(x.vlist);
        l = Curtable.methodFindInclass("constructor", null);

        if (l.fake) {
            GeraConstructorDefault(); // se construtor e falso, gera default
        }

        CodeGenConstructDeclListNode(x.ctlist);
        CodeGenMethodDeclListNode(x.mlist);
    }

    // gera um construtor () quando nao e declarado
    private void GeraConstructorDefault() {
        String sup;
        EntryClass cc;

        cc = (EntryClass) Curtable.levelup; // pega classe corrente

        if (cc.parent != null) { // acha nome da superclasse
            sup = cc.parent.completeName();
        } else {
            sup = "java/lang/Object";
        }

        // gera o codigo para o construtor default
        putCode();
        putCode(";Default constructor. Calls super()");
        putCode(".method public <init>()V");
        putCode(".limit locals 1");
        putCode(".limit stack 1");
        putCode("aload_0");
        putCode("invokespecial " + sup + "/<init>()V");
        putCode("return");
        putCode(".end method");
    }

    // ---------------- Lista de declaracoes de variaveis ----------------
    public void CodeGenVarDeclListNode(ListNode x) {
        if (x == null) {
            return;
        }

        CodeGenVarDeclNode((VarDeclNode) x.node);
        CodeGenVarDeclListNode(x.next);
    }

    // -------------------- Declaracao de variavel --------------------
    public void CodeGenVarDeclNode(VarDeclNode x) {
        ListNode p;
        EntryVar l;
        EntryTable c = null;

        if (x == null) {
            return;
        }

        for (p = x.vars; p != null; p = p.next) {
            VarNode q = (VarNode) p.node;

            // pega  variavel na tabela
            l = Curtable.varFind(q.position.image);

            // gera diretiva .field
            putCode(".field public " + l.name + " " + l.dscJava());
        }
    }

    // -------------- Lista de construtores ---------------------
    public void CodeGenConstructDeclListNode(ListNode x) {
        if (x == null) {
            return;
        }

        CodeGenConstructDeclNode((ConstructDeclNode) x.node);

        CodeGenConstructDeclListNode(x.next);
    }

    // ------------------ Declaracao de construtor -----------------
    public void CodeGenConstructDeclNode(ConstructDeclNode x) {
        EntryMethod t;
        EntryRec r = null;
        EntryTable e;
        EntryClass thisclass;
        EntryVar thisvar;
        ListNode p;
        VarDeclNode q;
        VarNode u;
        int n;
        String sup = "";

        if (x == null) {
            return;
        }

        p = x.body.param;
        n = 0;

        // monta a lista com os tipos dos parametros
        while (p != null) {
            q = (VarDeclNode) p.node; // q = no com a declaracao do parametro
            u = (VarNode) q.vars.node; // u = no com o nome e dimensao
            n++;

            // acha a entrada do tipo na tabela
            e = Curtable.classFindUp(q.position.image);

            // constroi a lista com os tipos dos parametros
            r = new EntryRec(e, u.dim, n, r);
            p = p.next;
        }

        String parlist = "";

        if (r != null) {
            r = r.inverte(); // inverte a lista

            // monta descritor para cada um dos parametros
            parlist = r.dscJava();
        }

        // acha a entrada do construtor na tabela
        t = Curtable.methodFind("constructor", r);
        CurMethod = t; // guarda metodo corrente

        // gera codigo para o cabecalho do construtor
        putCode();
        putCode(".method public <init>(" + parlist + ")V");

        // numero maximo de locais utilizadas
        putCode(".limit locals " + t.totallocals);

        // pega a entrada da classe corrente na tabela
        thisclass = (EntryClass) Curtable.levelup;

        // pega o nome da superclasse
        if (thisclass.parent != null) {
            sup = ((EntryClass) thisclass.parent).completeName();
        } else {
            sup = "java/lang/Object";
        }

        if (!CurMethod.hassuper) // verifica se tem chamada ao super construtor
         { // se nao, chama super())
            putCode();
            putCode("aload_0", 1);
            putCode("invokespecial " + sup + "/<init>()V", -1);
        }

        // inicia um novo escopo na tabela corrente
        Curtable.beginScope();

        thisvar = new EntryVar("this", thisclass, 0, 0);
        Curtable.add(thisvar); // inclui variavel local "this" com numero 0
        Nlocals = 1; // initializa numero de variaveis locais
        stackSize = stackHigh = 0; // inicializa numero de espacos ocupados na pilha
        CodeGenMethodBodyNode(x.body);
        Curtable.endScope(); // retira variaveis locais da tabela
        putCode("return");
        putCode(".limit stack " + (t.totalstack = stackSize));
        putCode(".end method"); // final do metodo
    }

    // -------------------------- Lista de metodos -----------------
    public void CodeGenMethodDeclListNode(ListNode x) {
        if (x == null) {
            return;
        }

        CodeGenMethodDeclNode((MethodDeclNode) x.node);

        CodeGenMethodDeclListNode(x.next);
    }

    // --------------------- Declaracao de metodo ---------------
    public void CodeGenMethodDeclNode(MethodDeclNode x) {
        EntryMethod t;
        EntryRec r = null;
        EntryTable e;
        EntryVar thisvar;
        ListNode p;
        VarDeclNode q;
        VarNode u;
        int n;

        if (x == null) {
            return;
        }

        p = x.body.param;
        n = 0;

        // monta a lista com os tipos dos parametros
        while (p != null) {
            q = (VarDeclNode) p.node; // q = no com a declaracao do parametro
            u = (VarNode) q.vars.node; // u = no com o nome e dimensao
            n++;

            // acha a entrada do tipo na tabela
            e = Curtable.classFindUp(q.position.image);

            // constroi a lista com os tipos dos parametros
            r = new EntryRec(e, u.dim, n, r);
            p = p.next;
        }

        String parlist = "";

        if (r != null) {
            r = r.inverte(); // inverte a lista

            // monta descritor para cada um dos parametros
            parlist = r.dscJava();
        }

        // acha a entrada do metodo na tabela
        t = Curtable.methodFind(x.name.image, r);
        CurMethod = t; // guarda metodo corrente

        // gera codigo para o cabecalho do metodo
        String statc = "";
        putCode();

        if (t.name.equals("start") && (r == null)) // metodo start 
         { // se for metodo inicial, deve ser static
            statc = "static ";
            hasStart = true;
        }

        putCode(".method public " + statc + t.name + "(" + parlist + ")" +
            t.dscJava());
        putCode(".limit locals " + t.totallocals);

        // inicia um novo escopo na tabela corrente
        Curtable.beginScope();

        thisvar = new EntryVar("this", Curtable.levelup, 0, 0);
        Curtable.add(thisvar); // inclui variavel local "this" na tabela

        Nlocals = 1; // numero de variaveis locais (this)
        stackSize = stackHigh = 0; // inicializa numero de espacos ocupados na pilha
        CodeGenMethodBodyNode(x.body);
        Curtable.endScope(); // retira variaveis locais da tabela corrente

        if ((CurMethod.type == INT_TYPE) && (CurMethod.dim == 0)) {
            putCode("bipush 0", 1);
            putCode("ireturn", -1);
        } else {
            putCode("aconst_null", 1);
            putCode("areturn", -1);
        }

        putCode(".limit stack " + (t.totalstack = stackSize));
        putCode(".end method"); // final do metodo
    }

    //-------------------------- Corpo de metodo ----------------------
    public void CodeGenMethodBodyNode(MethodBodyNode x) {
        if (x == null) {
            return;
        }

        CodeGenLocalVarDeclListNode(x.param); // trata parametro como var. local

        CodeGenStatementNode(x.stat);
    }

    //------------------------ lista de variaveis locais ----------------------
    public void CodeGenLocalVarDeclListNode(ListNode x) {
        if (x == null) {
            return;
        }

        CodeGenLocalVarDeclNode((VarDeclNode) x.node);

        CodeGenLocalVarDeclListNode(x.next);
    }

    //---------------------- Declaracao de variaveis locais ----------------------
    public void CodeGenLocalVarDeclNode(VarDeclNode x) {
        ListNode p;
        VarNode q;
        EntryTable c;

        if (x == null) {
            return;
        }

        // procura tipo da declaracao na tabela de simbolos
        c = Curtable.classFindUp(x.position.image);

        for (p = x.vars; p != null; p = p.next) {
            q = (VarNode) p.node;

            // insere a variavel local na tabela corrente
            Curtable.add(new EntryVar(q.position.image, c, q.dim, Nlocals++));
        }
    }

    // --------------------------- Comando composto ----------------------
    public void CodeGenBlockNode(BlockNode x) {
        Curtable.beginScope(); // inicio de um escopo
        CodeGenStatementListNode(x.stats);
        Curtable.endScope(); // final do escopo, libera vars locais
    }

    // --------------------- Lista de comandos --------------------
    public void CodeGenStatementListNode(ListNode x) {
        if (x == null) {
            return;
        }

        CodeGenStatementNode((StatementNode) x.node);

        CodeGenStatementListNode(x.next);
    }

    // --------------------------- Comando print ---------------------
    public void CodeGenPrintNode(PrintNode x) {
        type t;

        if (x == null) {
            return;
        }

        putCode();
        putCode(";begins print ");

        // coloca System.out na pilha
        putCode("getstatic java/lang/System/out Ljava/io/PrintStream;", 1);

        // coloca resultado da expressao na pilha
        store = false;
        t = CodeGenExpreNode(x.expr);

        // chama PrintStream.print(String)
        putCode("invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V",
            -2);
    }

    // ---------------------- Comando read --------------------------
    public void CodeGenReadNode(ReadNode x) {
        type t = null;

        if (x == null) {
            return;
        }

        try {
            t = TypeCheckExpreNode(x.expr); // tem que verificar o tipo da expressao
        } catch (Exception e) {
        }

        if (t.ty == INT_TYPE) { // chama readInt, resultado na pilha
            putCode("invokestatic langXrt/Runtime/readInt()I", 1);
        } else { // le string chama readString, resultado na pilha
            putCode("invokestatic langXrt/Runtime/readString()Ljava/lang/String;",
                1);
        }

        store = true; // indica que e operacao de armazenagem
        CodeGenExpreNode(x.expr);
    }

    // --------------------- Comando return -------------------------
    public void CodeGenReturnNode(ReturnNode x) {
        type t;

        if (x == null) {
            return;
        }

        putCode();
        putCode(";begins return ");

        // t = tipo e dimensao do resultado da expressao
        store = false;
        t = CodeGenExpreNode(x.expr);

        // dependendo do tipo de retorno gera a instrucao
        if ((t.ty == INT_TYPE) && (t.dim == 0)) {
            putCode("ireturn", -1);
        } else {
            putCode("areturn", -1);
        }
    }

    // ------------------------ Comando super --------------------------
    public void CodeGenSuperNode(SuperNode x) {
        type t;
        String sup;

        if (x == null) {
            return;
        }

        // p aponta para a entrada da superclasse da classe corrente
        EntryClass p = Curtable.levelup.parent;

        sup = p.completeName();

        putCode();
        putCode(";begins super");
        putCode("aload_0", 1);

        // coloca os parametros na pilha
        store = false;
        t = CodeGenExpreListNode(x.args);

        String arglist = (t.ty == null) ? "" : t.dscJava();
        int removeStack = (t.ty == null) ? 0 : ((EntryRec) t.ty).cont;

        putCode("invokespecial " + sup + "/<init>(" + arglist + ")V",
            -(removeStack + 1));
    }

    // ------------------------- Comando de atribuicao -------------------
    public void CodeGenAtribNode(AtribNode x) {
        type t1 = null;
        type t2;

        if (x == null) {
            return;
        }

        // pega tipo da expressao a esquerda da atribuicao
        try {
            t1 = TypeCheckExpreNode(x.expr1);
        } catch (Exception e) {
        }

        // calcula expressao a direita
        store = false;
        t2 = CodeGenExpreNode(x.expr2);
        

        if ((t1.ty != t2.ty) && isSubClass(t1.ty, t2.ty)) { // faz coercao de tipos
	        if ( t1.dim > 0 )
	        {
            	putCode("checkcast " + t1.dscJava());
            }
            else
            {
            	EntryClass ec = (EntryClass) t1.ty;
            	putCode("checkcast " + ec.completeName());
            }
        }

        // armazena resultado
        store = true;
        CodeGenExpreNode(x.expr1);
    }

    // ---------------------------------- comando if --------------------
    public void CodeGenIfNode(IfNode x) {
        type t;
        String lab = newLabel();

        if (x == null) {
            return;
        }

        putCode();
        putCode(";begins if " + lab);

        // gera codigo para a condicao, deixando resultado na pilha
        store = false;
        t = CodeGenExpreNode(x.expr);

        // se falso desvia
        putCode("ifeq else" + lab, -1);

        // gera codigo para ramo verdadeiro
        CodeGenStatementNode(x.stat1);

        if (x.stat2 != null) { // existe else

            // gera desvio no final do ramo verdadeiro
            putCode("goto fi" + lab);

            // gera label para inicio do ramo falso
            putLabel("else" + lab + ":");

            // gera codigo para ramo falso
            CodeGenStatementNode(x.stat2);

            // gera label para fim do comando
            putLabel("fi" + lab + ":");
        } else { // nao existe else
                 // gera label para fim do comando
            putLabel("else" + lab + ":");
        }
    }

    // ------------------------- comando for -----------------------
    public void CodeGenForNode(ForNode x) {
        String lab = newLabel();
        String oldbreaklabel = breaklabel; // salva label do break

        if (x == null) {
            return;
        }

        putCode();
        putCode(";begins for " + lab);

        // gera codigo para inicializacao
        CodeGenStatementNode(x.init);

        // gera label para laco do comando
        putLabel("forloop" + lab + ":");

        // gera codigo para condicao e deixa resultado na pilha
        store = false;
        CodeGenExpreNode(x.expr);

        // testa o resultado
        putCode("ifeq rof" + lab, -1);
        breaklabel = "rof" + lab; // novo label p/ break: fim do for

        // gera codigo para o corpo do for
        CodeGenStatementNode(x.stat);

        // gera codigo para incremento
        CodeGenStatementNode(x.incr);

        // gera retorno ao laco
        putCode("goto forloop" + lab);

        // gera label do fim do comando
        putLabel(breaklabel + ":");

        breaklabel = oldbreaklabel; // restaura label do break
    }

    // --------------------------- Comando break --------------------
    public void CodeGenBreakNode(BreakNode x) {
        if (x == null) {
            return;
        }

        putCode();
        putCode(";begins break ");

        // gera desvio para o label do break
        putCode("goto " + breaklabel);
    }

    // --------------------------- Comando vazio -------------------
    public void CodeGenNopNode(NopNode x) {
        putCode();
        putCode(";begins empty statement ");
        putCode("nop");
    }

    // -------------------------- Alocacao de objeto ------------------------
    public type CodeGenNewObjectNode(NewObjectNode x) {
        type t;
        EntryMethod p;
        EntryClass c;

        if (x == null) {
            return null;
        }

        // procura a classe da qual se deseja criar um objeto
        c = (EntryClass) Curtable.classFindUp(x.name.image);

        // cria o objeto
        putCode("new " + ((EntryClass) c).completeName(), 1);

        // duplica para chamar construtor
        putCode("dup", 1);

        // gera codigo para os argumentos do construtor
        t = CodeGenExpreListNode(x.args);

        String arglist = (t.ty == null) ? "" : t.dscJava();
        int removeStack = (t.ty == null) ? 0 : ((EntryRec) t.ty).cont;

        // gera chamada do construtor
        putCode("invokespecial " + c.completeName() + "/<init>(" + arglist +
            ")V", -(removeStack + 1));

        // objeto criado esta na pilha
        t = new type(c, 0);

        return t;
    }

    // -------------------------- Alocacao de array ------------------------
    public type CodeGenNewArrayNode(NewArrayNode x) {
        type t;
        EntryTable c;
        EntryRec r;

        if (x == null) {
            return null;
        }

        // procura o tipo do qual se deseja criar um array
        c = Curtable.classFindUp(x.name.image);

        // gera codigo para cada uma das dimensoes
        t = CodeGenExpreListNode(x.dims);
        r = (EntryRec) t.ty;

        if (r.cont == 1) // dimensao = 1
         {
            if (c == INT_TYPE) {
                putCode("newarray I", 1);
            } else {
                putCode("anewarray " + c.dscJava(), 1);
            }
        } else {
            putCode("multianewarray " + EntryTable.strDim(r.cont) +
                c.dscJava() + " " + r.cont, 1);
        }

        return new type(c, r.cont);
    }

    // --------------------------- Lista de expressoes ---------------
    public type CodeGenExpreListNode(ListNode x) {
        type t;
        type t1;
        EntryRec r;
        int n;

        if (x == null) {
            return new type(null, 0);
        }

        // gera codigo para o primeiro no da lista
        t = CodeGenExpreNode((ExpreNode) x.node);

        // gera codigo para o restante da lista. t1.ty contem um EntryRec
        t1 = CodeGenExpreListNode(x.next);

        // n = tamanho da lista em t1
        n = (t1.ty == null) ? 0 : ((EntryRec) t1.ty).cont;

        // cria novo EntryRec com t.ty como 1.o elemento
        r = new EntryRec(t.ty, t.dim, n + 1, (EntryRec) t1.ty);

        // cria type com r como variavel ty
        t = new type(r, 0);

        return t;
    }

    // --------------------- Expressao relacional -------------------
    public type CodeGenRelationalNode(RelationalNode x) {
        type t1;
        type t2;
        int op; // operacao
        String lab = newLabel();

        if (x == null) {
            return null;
        }

        op = x.position.kind;

        // gera codigo para as duas subexpressoes
        t1 = CodeGenExpreNode(x.expr1);
        t2 = CodeGenExpreNode(x.expr2);

        if ((t1.dim == 0) && (t1.ty == INT_TYPE)) { // se for inteiro...

            String s = selectBinInstruct(op); // pega instrucao correspondente
            putCode(s + " relexpr" + lab, -2);
        } else { // se for array, objeto ou string usa a mesma instrucao

            if (op == langXConstants.EQ) { // so == ou != sao permitidos
                putCode("if_acmpeq relexpr" + lab, -2);
            } else {
                putCode("if_acmpne relexpr" + lab, -2);
            }
        }

        putCode("bipush 0", 1);
        putCode("goto pxeler" + lab);
        putLabel("relexpr" + lab + ":");
        putCode("bipush 1"); // nao incrementa stack pois seria contar 2 vezes
                             // ja que o bipush 0 incrementou  

        putLabel("pxeler" + lab + ":");

        return new type(INT_TYPE, 0);
    }

    // ------------------------ Soma ou subtracao  -------------------
    public type CodeGenAddNode(AddNode x) {
        type t1;
        type t2;
        int op; // operacao
        int i;
        int j;

        if (x == null) {
            return null;
        }

        // gera codigo para as subexpressoes
        op = x.position.kind;
        t1 = CodeGenExpreNode(x.expr1);
        t2 = CodeGenExpreNode(x.expr2);

        i = j = 0;

        if (t1.ty == INT_TYPE) {
            i++;
        } else if (t1.ty == STRING_TYPE) {
            j++;
        }

        if (t2.ty == INT_TYPE) {
            i++;
        } else if (t2.ty == STRING_TYPE) {
            j++;
        }

        // 2 operandos inteiros
        if (i == 2) {
            String s = selectBinInstruct(op); // pega instrucao correspondente
            putCode(s, -1);

            return new type(INT_TYPE, 0);
        }

        // 2 operandos string
        if (j == 2) {
            // chama "concat" para concatenar strings
            putCode("invokevirtual java/lang/String/concat" +
                "(Ljava/lang/String;)Ljava/lang/String;", -1);

            return new type(STRING_TYPE, 0);
        }

        // um inteiro e um string
        if (t1.ty == INT_TYPE) { // primeiro operando e um int (2o. na pilha)
            putCode("swap"); // passa para cima e converte para string
            putCode("invokestatic java/lang/Integer/toString(I)" +
                "Ljava/lang/String;");
            putCode("swap"); // volta para baixo
        } else { // somente converte pois ja esta no topo
            putCode("invokestatic java/lang/Integer/toString(I)" +
                "Ljava/lang/String;");
        }

        // chama "concat" para concatenar strings
        putCode("invokevirtual java/lang/String/concat" +
            "(Ljava/lang/String;)Ljava/lang/String;", -1);

        return new type(STRING_TYPE, 0);
    }

    // ---------------------- Multiplicacao ou divisao --------------------
    public type CodeGenMultNode(MultNode x) {
        type t1;
        type t2;
        int op; // operacao

        if (x == null) {
            return null;
        }

        op = x.position.kind;
        t1 = CodeGenExpreNode(x.expr1);
        t2 = CodeGenExpreNode(x.expr2);

        String s = selectBinInstruct(op); // pega instrucao correspondente
        putCode(s, -1);

        return new type(INT_TYPE, 0);
    }

    // ------------------------- Expressao unaria ------------------------
    public type CodeGenUnaryNode(UnaryNode x) {
        type t;
        int op; // operacao

        if (x == null) {
            return null;
        }

        op = x.position.kind;
        t = CodeGenExpreNode(x.expr);

        if (op == langXConstants.MINUS) {
            putCode("ineg");
        }

        return new type(INT_TYPE, 0);
    }

    // -------------------------- Constante inteira ----------------------
    public type CodeGenIntConstNode(IntConstNode x) {
        if (x == null) {
            return null;
        }

        // carrega a constante na pilha
        putCode("ldc " + x.position.image, 1);

        return new type(INT_TYPE, 0);
    }

    // ------------------------ Constante string ----------------------------
    public type CodeGenStringConstNode(StringConstNode x) {
        if (x == null) {
            return null;
        }

        // carrega a constante na pilha
        putCode("ldc " + x.position.image, 1);

        return new type(STRING_TYPE, 0);
    }

    // ------------------------------ Constante null --------------------------
    public type CodeGenNullConstNode(NullConstNode x) {
        if (x == null) {
            return null;
        }

        // carrega a constante na pilha
        putCode("aconst_null", 1);

        return new type(NULL_TYPE, 0);
    }

    // -------------------------------- Nome de variavel ------------------
    public type CodeGenVarNode(VarNode x) {
        EntryVar p;
        String s = store ? "store" : "load";
        String s2 = store ? "putfield" : "getfield";

        if (x == null) {
            return null;
        }

        // procura variavel na tabela
        p = Curtable.varFind(x.position.image);

        if (p.localcount >= 0) { // e variavel local

            if ((p.type == INT_TYPE) && (p.dim == 0)) { // escolhe iload ou aload
                putCode("i" + s + " " + p.localcount, store ? (-1) : 1);
            } else {
                putCode("a" + s + " " + p.localcount, store ? (-1) : 1);
            }
        } else { // variavel da classe
                 // pega a classe a que ela pertence

            EntryClass v = p.mytable.levelup;

            // empilha "this"
            putCode("aload_0", 1);

            // se for para armazenar, troca topo da pilha
            if (store) {
                putCode("swap");
            }

            putCode(s2 + " " + v.completeName() + "/" + p.name + " " +
                p.dscJava(), store ? (-2) : 0);
        }

        return new type(p.type, p.dim);
    }

    // ---------------------------- Chamada de funcao ------------------------
    public type CodeGenCallNode(CallNode x) {
        EntryClass c;
        EntryMethod m;
        type t1;
        type t2;

        if (x == null) {
            return null;
        }

        // pega objeto correspondente ao primeiro filho
        t1 = CodeGenExpreNode(x.expr);

        // gera codigo para os argumentos
        t2 = CodeGenExpreListNode(x.args);

        // procura o metodo desejado na classe t1.ty
        c = (EntryClass) t1.ty;
        m = c.nested.methodFind(x.meth.image, (EntryRec) t2.ty);

        String arglist = (t2.ty == null) ? "" : t2.dscJava();
        int removeStack = (t2.ty == null) ? 0 : ((EntryRec) t2.ty).cont;

        // gera chamada
        putCode("invokevirtual " + c.completeName() + "/" + m.name + "(" +
            arglist + ")" + m.dscJava(), -removeStack);

        return new type(m.type, m.dim);
    }

    // --------------------------- Indexacao de variavel ---------------
    public type CodeGenIndexNode(IndexNode x) {
        EntryClass c;
        type t1;
        type t2;
        boolean b = store;

        if (x == null) {
            return null;
        }

        // calcula primeiro filho, o array
        store = false;
        t1 = CodeGenExpreNode(x.expr1);

        // se for para armazenar, troca topo
        if (b) {
            putCode("swap");
        }

        // pega indice
        t2 = CodeGenExpreNode(x.expr2);

        if (b) // realiza a operacao, armazenar ou carregar na pilha
         {
            putCode("swap"); // troca o topo

            if ((t1.ty == INT_TYPE) && (t1.dim == 1)) { // array de inteiros
                putCode("iastore", -3);
            } else {
                putCode("aastore", -3);
            }
        } else {
            if ((t1.ty == INT_TYPE) && (t1.dim == 1)) { // array de inteiros
                putCode("iaload", -1);
            } else {
                putCode("aaload", -1);
            }
        }

        store = b;

        return new type(t1.ty, t1.dim - 1);
    }

    // -------------------------- Acesso a campo de variavel ---------------
    public type CodeGenDotNode(DotNode x) {
        EntryClass c;
        EntryVar v;
        type t;
        boolean b = store;

        if (x == null) {
            return null;
        }

        // carrega na pilha o primeiro filho; o objeto que contem o campo
        store = false;
        t = CodeGenExpreNode(x.expr);

        // procura a variavel desejada na classe t1.ty
        c = (EntryClass) t.ty;
        v = c.nested.varFind(x.field.image);

        if (b) {
            putCode("swap"); // troca o topo
            putCode("putfield " + c.completeName() + "/" + v.name + " " +
                v.dscJava(), -2);
        } else {
            putCode("getfield " + c.completeName() + "/" + v.name + " " +
                v.dscJava());
        }

        store = b;

        return new type(v.type, v.dim);
    }

    // --------------------------- Expressao em geral --------------------------
    public type CodeGenExpreNode(ExpreNode x) {
        if (x instanceof NewObjectNode) {
            return CodeGenNewObjectNode((NewObjectNode) x);
        } else if (x instanceof NewArrayNode) {
            return CodeGenNewArrayNode((NewArrayNode) x);
        } else if (x instanceof RelationalNode) {
            return CodeGenRelationalNode((RelationalNode) x);
        } else if (x instanceof AddNode) {
            return CodeGenAddNode((AddNode) x);
        } else if (x instanceof MultNode) {
            return CodeGenMultNode((MultNode) x);
        } else if (x instanceof UnaryNode) {
            return CodeGenUnaryNode((UnaryNode) x);
        } else if (x instanceof CallNode) {
            return CodeGenCallNode((CallNode) x);
        } else if (x instanceof IntConstNode) {
            return CodeGenIntConstNode((IntConstNode) x);
        } else if (x instanceof StringConstNode) {
            return CodeGenStringConstNode((StringConstNode) x);
        } else if (x instanceof NullConstNode) {
            return CodeGenNullConstNode((NullConstNode) x);
        } else if (x instanceof IndexNode) {
            return CodeGenIndexNode((IndexNode) x);
        } else if (x instanceof DotNode) {
            return CodeGenDotNode((DotNode) x);
        } else if (x instanceof VarNode) {
            return CodeGenVarNode((VarNode) x);
        } else {
            return null;
        }
    }

    // --------------------------- Comando em geral -------------------
    public void CodeGenStatementNode(StatementNode x) {
        if (x instanceof BlockNode) {
            CodeGenBlockNode((BlockNode) x);
        } else if (x instanceof VarDeclNode) {
            CodeGenLocalVarDeclNode((VarDeclNode) x);
        } else if (x instanceof AtribNode) {
            CodeGenAtribNode((AtribNode) x);
        } else if (x instanceof IfNode) {
            CodeGenIfNode((IfNode) x);
        } else if (x instanceof ForNode) {
            CodeGenForNode((ForNode) x);
        } else if (x instanceof PrintNode) {
            CodeGenPrintNode((PrintNode) x);
        } else if (x instanceof NopNode) {
            CodeGenNopNode((NopNode) x);
        } else if (x instanceof ReadNode) {
            CodeGenReadNode((ReadNode) x);
        } else if (x instanceof ReturnNode) {
            CodeGenReturnNode((ReturnNode) x);
        } else if (x instanceof SuperNode) {
            CodeGenSuperNode((SuperNode) x);
        } else if (x instanceof BreakNode) {
            CodeGenBreakNode((BreakNode) x);
        }
    }

    private String selectBinInstruct(int op) {
        switch (op) {
        case langXConstants.PLUS:
            return "iadd";

        case langXConstants.MINUS:
            return "isub";

        case langXConstants.STAR:
            return "imul";

        case langXConstants.SLASH:
            return "idiv";

        case langXConstants.REM:
            return "irem";

        case langXConstants.EQ:
            return "if_icmpeq";

        case langXConstants.NEQ:
            return "if_icmpne";

        case langXConstants.LT:
            return "if_icmplt";

        case langXConstants.GT:
            return "if_icmpgt";

        case langXConstants.LE:
            return "if_icmple";

        case langXConstants.GE:
            return "if_icmpge";
        }

        return null;
    }

    private String newLabel() {
        return Integer.toString(currlabel++);
    }

    private void createMain() {
        EntryClass v = (EntryClass) Curtable.levelup;

        putCode();
        putCode(";Entry point for the JVM");
        putCode(".method static public main([Ljava/lang/String;)V");
        putCode(".limit  locals 1");
        putCode(".limit  stack 1");
        putCode("invokestatic langXrt/Runtime/initialize()I");
        putCode("ifne         end");
        putCode("invokestatic " + v.completeName() + "/start()I");
        putCode("pop");
        putLabel("end:");
        putCode("invokestatic langXrt/Runtime/finilizy()V");
        putCode("return");
        putCode(".end method");
    }

    private void putCode(String s, int inc) {
        stackHigh += inc;

        if (stackHigh > stackSize) {
            stackSize = stackHigh;
        }

        fp.println("\t\t" + s);
    }

    private void putCode(String s) {
        putCode(s, 0);
    }

    private void putCode() {
        fp.println();
    }

    private void putLabel(String s) {
        fp.println(s);
    }
}
