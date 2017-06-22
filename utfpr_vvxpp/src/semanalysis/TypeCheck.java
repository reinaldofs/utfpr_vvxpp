package semanalysis;

import parser.*;

import symtable.*;

import syntacticTree.*;


public class TypeCheck extends VarCheck {
    int nesting; // controla o nivel de aninhamento em comandos repetitivos
    protected int Nlocals; // conta numero de variaveis locais num metodo
    type Returntype; // tipo de retorno de um metodo
    protected final EntrySimple STRING_TYPE;
    protected final EntrySimple INT_TYPE;
    protected final EntrySimple NULL_TYPE;
    protected EntryMethod CurMethod; // metodo sendo analisado
    boolean cansuper; // indica se chamada super e permitida

    public TypeCheck() {
        super();
        nesting = 0;
        Nlocals = 0;
        STRING_TYPE = (EntrySimple) Maintable.classFindUp("string");
        INT_TYPE = (EntrySimple) Maintable.classFindUp("int");
        NULL_TYPE = new EntrySimple("$NULL$");
        Maintable.add(NULL_TYPE);
    }

    public void TypeCheckRoot(ListNode x) throws SemanticException {
        VarCheckRoot(x); // faz analise das variaveis e metodos
        TypeCheckClassDeclListNode(x); // faz analise do corpo dos metodos

        if (foundSemanticError != 0) { // se houve erro, lanca excecao
            throw new SemanticException(foundSemanticError +
                " Semantic Errors found (phase 3)");
        }
    }

    // ------------- lista de classes --------------------------
    public void TypeCheckClassDeclListNode(ListNode x) {
        if (x == null) {
            return;
        }

        try {
            TypeCheckClassDeclNode((ClassDeclNode) x.node);
        } catch (SemanticException e) { // se um erro ocorreu na classe, da a msg mas faz a analise p/ proxima
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        TypeCheckClassDeclListNode(x.next);
    }

    // verifica se existe referencia circular de superclasses
    private boolean circularSuperclass(EntryClass orig, EntryClass e) {
        if (e == null) {
            return false;
        }

        if (orig == e) {
            return true;
        }

        return circularSuperclass(orig, e.parent);
    }

    // ------------- declaracao de classe -------------------------
    public void TypeCheckClassDeclNode(ClassDeclNode x)
        throws SemanticException {
        Symtable temphold = Curtable; // salva tabela corrente
        EntryClass nc;

        if (x == null) {
            return;
        }

        nc = (EntryClass) Curtable.classFindUp(x.name.image);

        if (circularSuperclass(nc, nc.parent)) { // se existe declaracao circular, ERRO
            nc.parent = null;
            throw new SemanticException(x.position, "Circular inheritance");
        }

        Curtable = nc.nested; // tabela corrente = tabela da classe
        TypeCheckClassBodyNode(x.body);
        Curtable = temphold; // recupera tabela corrente
    }

    // ------------- corpo da classe -------------------------
    public void TypeCheckClassBodyNode(ClassBodyNode x) {
        if (x == null) {
            return;
        }

        TypeCheckClassDeclListNode(x.clist);
        TypeCheckVarDeclListNode(x.vlist);
        TypeCheckConstructDeclListNode(x.ctlist);
        TypeCheckMethodDeclListNode(x.mlist);
    }

    // ---------------- Lista de declaracoes de variaveis ----------------
    public void TypeCheckVarDeclListNode(ListNode x) {
        if (x == null) {
            return;
        }

        try {
            TypeCheckVarDeclNode((VarDeclNode) x.node);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        TypeCheckVarDeclListNode(x.next);
    }

    // -------------------- Declaracao de variavel --------------------
    public void TypeCheckVarDeclNode(VarDeclNode x) throws SemanticException {
        ListNode p;
        EntryVar l;

        if (x == null) {
            return;
        }

        for (p = x.vars; p != null; p = p.next) {
            VarNode q = (VarNode) p.node;

            // tenta pegar 2a. ocorrencia da variavel na tabela
            l = Curtable.varFind(q.position.image, 2);

            // se conseguiu a variavel foi definida 2 vezes, ERRO
            if (l != null) {
                throw new SemanticException(q.position,
                    "Variable " + q.position.image + " already declared");
            }
        }
    }

    // -------------- Lista de construtores ---------------------
    public void TypeCheckConstructDeclListNode(ListNode x) {
        if (x == null) {
            return;
        }

        try {
            TypeCheckConstructDeclNode((ConstructDeclNode) x.node);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        TypeCheckConstructDeclListNode(x.next);
    }

    // ------------------ Declaracao de construtor -----------------
    public void TypeCheckConstructDeclNode(ConstructDeclNode x)
        throws SemanticException {
        EntryMethod t;
        EntryRec r = null;
        EntryTable e;
        EntryClass thisclass;
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

        if (r != null) {
            r = r.inverte(); // inverte a lista
        }

        // acha a entrada do construtor na tabela
        t = Curtable.methodFind("constructor", r);
        CurMethod = t; // guarda metodo corrente

        // inicia um novo escopo na tabela corrente
        Curtable.beginScope();

        // pega a entrada da classe corrente na tabela
        thisclass = (EntryClass) Curtable.levelup;

        thisvar = new EntryVar("this", thisclass, 0, 0);
        Curtable.add(thisvar); // inclui variavel local "this" com numero 0
        Returntype = null; // tipo de retorno do metodo = nenhum
        nesting = 0; // nivel de aninhamento de comandos for
        Nlocals = 1; // inicializa numero de variaveis locais
        TypeCheckMethodBodyNode(x.body);
        t.totallocals = Nlocals; // numero de variavamos locais do metodo
        Curtable.endScope(); // retira variaveis locais da tabela
    }

    // -------------------------- Lista de metodos -----------------
    public void TypeCheckMethodDeclListNode(ListNode x) {
        if (x == null) {
            return;
        }

        try {
            TypeCheckMethodDeclNode((MethodDeclNode) x.node);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        TypeCheckMethodDeclListNode(x.next);
    }

    // --------------------- Declaracao de metodo ---------------
    public void TypeCheckMethodDeclNode(MethodDeclNode x)
        throws SemanticException {
        EntryMethod t;
        EntryRec r = null;
        EntryTable e;
        EntryClass thisclass;
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

        if (r != null) {
            r = r.inverte();
        }

        // acha a entrada do metodo na tabela
        t = Curtable.methodFind(x.name.image, r);
        CurMethod = t; // guarda metodo corrente

        // Returntype = tipo de retorno do metodo
        Returntype = new type(t.type, t.dim);

        // inicia um novo escopo na tabela corrente
        Curtable.beginScope();

        // pega a entrada da classe corrente na tabela
        thisclass = (EntryClass) Curtable.levelup;

        thisvar = new EntryVar("this", thisclass, 0, 0);
        Curtable.add(thisvar); // inclui variavel local "this" na tabela

        nesting = 0; // nivel de aninhamento de comandos for
        Nlocals = 1; // inicializa numero de variaveis locais
        TypeCheckMethodBodyNode(x.body);
        t.totallocals = Nlocals; // numero de variaveis locais declaradas
        Curtable.endScope(); // retira variaveis locais da tabela corrente
    }

    //-------------------------- Corpo de metodo ----------------------
    public void TypeCheckMethodBodyNode(MethodBodyNode x) {
        if (x == null) {
            return;
        }

        TypeCheckLocalVarDeclListNode(x.param); // trata parametro como var. local

        cansuper = false;

        if (Curtable.levelup.parent != null) { // existe uma superclasse para a classe corrente ?
                                               // acha primeiro comando do metodo

            StatementNode p = x.stat;

            while (p instanceof BlockNode)
                p = (StatementNode) ((BlockNode) p).stats.node;

            cansuper = p instanceof SuperNode; // verifica se e chamada super
        }

        try {
            TypeCheckStatementNode(x.stat);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }
    }

    //------------------------ lista de variaveis locais ----------------------
    public void TypeCheckLocalVarDeclListNode(ListNode x) {
        if (x == null) {
            return;
        }

        try {
            TypeCheckLocalVarDeclNode((VarDeclNode) x.node);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        TypeCheckLocalVarDeclListNode(x.next);
    }

    //---------------------- Declaracao de variaveis locais ----------------------
    public void TypeCheckLocalVarDeclNode(VarDeclNode x)
        throws SemanticException {
        ListNode p;
        VarNode q;
        EntryVar l;
        EntryVar u;
        EntryTable c;

        if (x == null) {
            return;
        }

        // procura tipo da declaracao na tabela de simbolos
        c = Curtable.classFindUp(x.position.image);

        // se nao achou, ERRO
        if (c == null) {
            throw new SemanticException(x.position,
                "Class " + x.position.image + " not found.");
        }

        for (p = x.vars; p != null; p = p.next) {
            q = (VarNode) p.node;
            l = Curtable.varFind(q.position.image);

            // se variavel ja existe e preciso saber que tipo de variavel e
            if (l != null) {
                // primeiro verifica se e local, definida no escopo corrente
                if (l.scope == Curtable.scptr) { // se for, ERRO
                    throw new SemanticException(q.position,
                        "Variable " + p.position.image + " already declared");
                }

                // c.c. verifica se e uma variavel de classe
                if (l.localcount < 0) { // se for da uma advertencia
                    System.out.println("Line " + q.position.beginLine +
                        " Column " + q.position.beginColumn +
                        " Warning: Variable " + q.position.image +
                        " hides a class variable");
                } else { // senao, e uma variavel local em outro escopo
                    System.out.println("Line " + q.position.beginLine +
                        " Column " + q.position.beginColumn +
                        " Warning: Variable " + q.position.image +
                        " hides a parameter or a local variable");
                }
            }

            // insere a variavel local na tabela corrente
            Curtable.add(new EntryVar(q.position.image, c, q.dim, Nlocals++));
        }
    }

    // --------------------------- Comando composto ----------------------
    public void TypeCheckBlockNode(BlockNode x) {
        Curtable.beginScope(); // inicio de um escopo
        TypeCheckStatementListNode(x.stats);
        Curtable.endScope(); // final do escopo, libera vars locais
    }

    // --------------------- Lista de comandos --------------------
    public void TypeCheckStatementListNode(ListNode x) {
        if (x == null) {
            return;
        }

        try {
            TypeCheckStatementNode((StatementNode) x.node);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        TypeCheckStatementListNode(x.next);
    }

    // --------------------------- Comando print ---------------------
    public void TypeCheckPrintNode(PrintNode x) throws SemanticException {
        type t;

        if (x == null) {
            return;
        }

        // t = tipo e dimensao do resultado da expressao
        t = TypeCheckExpreNode(x.expr);

        // tipo tem que ser string e dimensao tem que ser 0
        if ((t.ty != STRING_TYPE) || (t.dim != 0)) {
            throw new SemanticException(x.position, "string expression required");
        }
    }

    // ---------------------- Comando read --------------------------
    public void TypeCheckReadNode(ReadNode x) throws SemanticException {
        type t;

        if (x == null) {
            return;
        }

        // verifica se o no filho tem um tipo valido
        if (!(x.expr instanceof DotNode || x.expr instanceof IndexNode ||
                x.expr instanceof VarNode)) {
            throw new SemanticException(x.position,
                "Invalid expression in read statement");
        }

        // verifica se e uma atribuicao para "this"
        if (x.expr instanceof VarNode) {
            EntryVar v = Curtable.varFind(x.expr.position.image);

            if ((v != null) && (v.localcount == 0)) // e a variavel local 0?
             {
                throw new SemanticException(x.position,
                    "Reading into variable " + " \"this\" is not legal");
            }
        }

        // verifica se o tipo e string ou int
        t = TypeCheckExpreNode(x.expr);

        if ((t.ty != STRING_TYPE) && (t.ty != INT_TYPE)) {
            throw new SemanticException(x.position,
                "Invalid type. Must be int or string");
        }

        // verifica se nao e array
        if (t.dim != 0) {
            throw new SemanticException(x.position, "Cannot read array");
        }
    }

    // --------------------- Comando return -------------------------
    public void TypeCheckReturnNode(ReturnNode x) throws SemanticException {
        type t;

        if (x == null) {
            return;
        }

        // t = tipo e dimensao do resultado da expressao
        t = TypeCheckExpreNode(x.expr);

        // verifica se e igual ao tipo do metodo corrente
        if (t == null) { // t == null nao tem expressao no return

            if (Returntype == null) {
                return;
            } else { // se Returntype != null e um metodo, entao ERRO
                throw new SemanticException(x.position,
                    "Return expression required");
            }
        } else {
            if (Returntype == null) { // retorno num construtor, ERRO
                throw new SemanticException(x.position,
                    "Constructor cannot return a value");
            }
        }

        // compara tipo e dimensao
        if ((t.ty != Returntype.ty) || (t.dim != Returntype.dim)) {
            throw new SemanticException(x.position, "Invalid return type");
        }
    }

    // ------------------------ Comando super --------------------------
    public void TypeCheckSuperNode(SuperNode x) throws SemanticException {
        type t;

        if (x == null) {
            return;
        }

        if (Returntype != null) {
            throw new SemanticException(x.position,
                "super is only allowed in constructors");
        }

        if (!cansuper) {
            throw new SemanticException(x.position,
                "super must be first statement in the constructor");
        }

        cansuper = false; // nao permite outro super

        // p aponta para a entrada da superclasse da classe corrente
        EntryClass p = Curtable.levelup.parent;

        if (p == null) {
            throw new SemanticException(x.position,
                "No superclass for this class");
        }

        // t.ty possui um EntryRec com os tipos dos parametros
        t = TypeCheckExpreListNode(x.args);

        // procura o construtor na tabela da superclasse
        EntryMethod m = p.nested.methodFindInclass("constructor",
                (EntryRec) t.ty);

        // se nao achou, ERRO
        if (m == null) {
            throw new SemanticException(x.position,
                "Constructor " + p.name + "(" +
                ((t.ty == null) ? "" : ((EntryRec) t.ty).toStr()) +
                ") not found");
        }

        CurMethod.hassuper = true; // indica que existe chamada a super no metodo
    }

    // ------------------------- Comando de atribuicao -------------------
    public void TypeCheckAtribNode(AtribNode x) throws SemanticException {
        type t1;
        type t2;
        EntryVar v;

        if (x == null) {
            return;
        }

        // verifica se o no filho tem um tipo valido
        if (!(x.expr1 instanceof DotNode || x.expr1 instanceof IndexNode ||
                x.expr1 instanceof VarNode)) {
            throw new SemanticException(x.position,
                "Invalid left side of assignment");
        }

        // verifica se e uma atribuicao para "this"
        if (x.expr1 instanceof VarNode) {
            v = Curtable.varFind(x.expr1.position.image);

            if ((v != null) && (v.localcount == 0)) // e a variavel local 0?
             {
                throw new SemanticException(x.position,
                    "Assigning to variable " + " \"this\" is not legal");
            }
        }

        t1 = TypeCheckExpreNode(x.expr1);
        t2 = TypeCheckExpreNode(x.expr2);

        // verifica tipos das expressoes
        // verifica dimensoes
        if (t1.dim != t2.dim) {
            throw new SemanticException(x.position,
                "Invalid dimensions in assignment");
        }

        // verifica se lado esquerdo e uma classe e direito e null, OK
        if (t1.ty instanceof EntryClass && (t2.ty == NULL_TYPE)) {
            return;
        }

        // verifica se t2 e subclasse de t1
        if (!(isSubClass(t2.ty, t1.ty) || isSubClass(t1.ty, t2.ty))) {
            throw new SemanticException(x.position,
                "Incompatible types for assignment ");
        }
    }

    protected boolean isSubClass(EntryTable t1, EntryTable t2) {
        // verifica se sao o mesmo tipo (vale para tipos simples)
        if (t1 == t2) {
            return true;
        }

        // verifica se sao classes
        if (!(t1 instanceof EntryClass && t2 instanceof EntryClass)) {
            return false;
        }

        // procura t2 nas superclasses de t1
        for (EntryClass p = ((EntryClass) t1).parent; p != null;
                p = p.parent)
            if (p == t2) {
                return true;
            }

        return false;
    }

    // ---------------------------------- comando if --------------------
    public void TypeCheckIfNode(IfNode x) {
        type t;

        if (x == null) {
            return;
        }

        try {
            t = TypeCheckExpreNode(x.expr);

            if ((t.ty != INT_TYPE) || (t.dim != 0)) {
                throw new SemanticException(x.expr.position,
                    "Integer expression expected");
            }
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        try {
            TypeCheckStatementNode(x.stat1);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        try {
            TypeCheckStatementNode(x.stat2);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }
    }

    // ------------------------- comando for -----------------------
    public void TypeCheckForNode(ForNode x) {
        type t;

        if (x == null) {
            return;
        }

        // analisa inicializacao
        try {
            TypeCheckStatementNode(x.init);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        // analisa expressao de controle
        try {
            t = TypeCheckExpreNode(x.expr);

            if ((t.ty != INT_TYPE) || (t.dim != 0)) {
                throw new SemanticException(x.expr.position,
                    "Integer expression expected");
            }
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        // analisa expressao de incremento
        try {
            TypeCheckStatementNode(x.incr);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        // analisa comando a ser repetido
        try {
            nesting++; // incrementa o aninhamento
            TypeCheckStatementNode(x.stat);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
        }

        nesting--; // decrementa o aninhamento
    }

    // --------------------------- Comando break --------------------
    public void TypeCheckBreakNode(BreakNode x) throws SemanticException {
        if (x == null) {
            return;
        }

        // verifica se esta dentro de um for. Se nao, ERRO
        if (nesting <= 0) {
            throw new SemanticException(x.position,
                "break not in a for statement");
        }
    }

    // --------------------------- Comando vazio -------------------
    public void TypeCheckNopNode(NopNode x) {
        // nada a ser feito
    }

    // ------------------ Expressoes ----------------------------------------
    // -------------------------- Alocacao de objeto ------------------------
    public type TypeCheckNewObjectNode(NewObjectNode x)
        throws SemanticException {
        type t;
        EntryMethod p;
        EntryTable c;

        if (x == null) {
            return null;
        }

        // procura a classe da qual se deseja criar um objeto
        c = Curtable.classFindUp(x.name.image);

        // se nao achou, ERRO
        if (c == null) {
            throw new SemanticException(x.position,
                "Class " + x.name.image + " not found");
        }

        // t.ty recebe a lista de tipos dos argumentos
        t = TypeCheckExpreListNode(x.args);

        // procura um construtor com essa assinatura
        Symtable s = ((EntryClass) c).nested;
        p = s.methodFindInclass("constructor", (EntryRec) t.ty);

        // se nao achou, ERRO
        if (p == null) {
            throw new SemanticException(x.position,
                "Constructor " + x.name.image + "(" +
                ((t.ty == null) ? "" : ((EntryRec) t.ty).toStr()) +
                ") not found");
        }

        // retorna c como tipo, dimensao = 0, local = -1 (nao local)
        t = new type(c, 0);

        return t;
    }

    // -------------------------- Alocacao de array ------------------------
    public type TypeCheckNewArrayNode(NewArrayNode x) throws SemanticException {
        type t;
        EntryTable c;
        ListNode p;
        ExpreNode q;
        int k;

        if (x == null) {
            return null;
        }

        // procura o tipo da qual se deseja criar um array
        c = Curtable.classFindUp(x.name.image);

        // se nao achou, ERRO
        if (c == null) {
            throw new SemanticException(x.position,
                "Type " + x.name.image + " not found");
        }

        // para cada expressao das dimensoes, verifica se tipo e int
        for (k = 0, p = x.dims; p != null; p = p.next) {
            t = TypeCheckExpreNode((ExpreNode) p.node);

            if ((t.ty != INT_TYPE) || (t.dim != 0)) {
                throw new SemanticException(p.position,
                    "Invalid expression for an array dimension");
            }

            k++;
        }

        return new type(c, k);
    }

    // --------------------------- Lista de expressoes ---------------
    public type TypeCheckExpreListNode(ListNode x) {
        type t;
        type t1;
        EntryRec r;
        int n;

        if (x == null) {
            return new type(null, 0);
        }

        try {
            // pega tipo do primeiro no da lista
            t = TypeCheckExpreNode((ExpreNode) x.node);
        } catch (SemanticException e) {
            System.out.println(e.getMessage());
            foundSemanticError++;
            t = new type(NULL_TYPE, 0);
        }

        // pega tipo do restante da lista. t1.ty contem um EntryRec
        t1 = TypeCheckExpreListNode(x.next);

        // n = tamanho da lista em t1
        n = (t1.ty == null) ? 0 : ((EntryRec) t1.ty).cont;

        // cria novo EntryRec com t.ty como 1.o elemento
        r = new EntryRec(t.ty, t.dim, n + 1, (EntryRec) t1.ty);

        // cria type com r como variavel ty
        t = new type(r, 0);

        return t;
    }

    // --------------------- Expressao relacional -------------------
    public type TypeCheckRelationalNode(RelationalNode x)
        throws SemanticException {
        type t1;
        type t2;
        int op; // operacao

        if (x == null) {
            return null;
        }

        op = x.position.kind;
        t1 = TypeCheckExpreNode(x.expr1);
        t2 = TypeCheckExpreNode(x.expr2);

        // se ambos sao int, retorna OK
        if ((t1.ty == INT_TYPE) && (t2.ty == INT_TYPE)) {
            return new type(INT_TYPE, 0);
        }

        // se a dimensao e diferente, ERRO
        if (t1.dim != t2.dim) {
            throw new SemanticException(x.position,
                "Can not compare objects with different dimensions");
        }

        // se dimensao > 0 so pode comparar igualdade
        if ((op != langXConstants.EQ) && (op != langXConstants.NEQ) &&
                (t1.dim > 0)) {
            throw new SemanticException(x.position,
                "Can not use " + x.position.image + " for arrays");
        }

        // se dois sao objetos do mesmo tipo pode comparar igualdade
        // isso inclui 2 strings
        if ((isSubClass(t2.ty, t1.ty) || isSubClass(t1.ty, t2.ty)) &&
                ((op == langXConstants.NEQ) || (op == langXConstants.EQ))) {
            return new type(INT_TYPE, 0);
        }

        // se um e objeto e outro null, pode comparar igualdade
        if (((t1.ty instanceof EntryClass && (t2.ty == NULL_TYPE)) ||
                (t2.ty instanceof EntryClass && (t1.ty == NULL_TYPE))) &&
                ((op == langXConstants.NEQ) || (op == langXConstants.EQ))) {
            return new type(INT_TYPE, 0);
        }

        throw new SemanticException(x.position,
            "Invalid types for " + x.position.image);
    }

    // ------------------------ Soma ou subtracao  -------------------
    public type TypeCheckAddNode(AddNode x) throws SemanticException {
        type t1;
        type t2;
        int op; // operacao
        int i;
        int j;

        if (x == null) {
            return null;
        }

        op = x.position.kind;
        t1 = TypeCheckExpreNode(x.expr1);
        t2 = TypeCheckExpreNode(x.expr2);

        // se dimensao > 0, ERRO
        if ((t1.dim > 0) || (t2.dim > 0)) {
            throw new SemanticException(x.position,
                "Can not use " + x.position.image + " for arrays");
        }

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

        // 2 operadores inteiros, OK
        if (i == 2) {
            return new type(INT_TYPE, 0);
        }

        // um inteiro e um string so pode somar
        if ((op == langXConstants.PLUS) && ((i + j) == 2)) {
            return new type(STRING_TYPE, 0);
        }

        throw new SemanticException(x.position,
            "Invalid types for " + x.position.image);
    }

    // ---------------------- Multiplicacao ou divisao --------------------
    public type TypeCheckMultNode(MultNode x) throws SemanticException {
        type t1;
        type t2;
        int op; // operacao
        int i;
        int j;

        if (x == null) {
            return null;
        }

        op = x.position.kind;
        t1 = TypeCheckExpreNode(x.expr1);
        t2 = TypeCheckExpreNode(x.expr2);

        // se dimensao > 0, ERRO
        if ((t1.dim > 0) || (t2.dim > 0)) {
            throw new SemanticException(x.position,
                "Can not use " + x.position.image + " for arrays");
        }

        // so dois int's sao aceitos
        if ((t1.ty != INT_TYPE) || (t2.ty != INT_TYPE)) {
            throw new SemanticException(x.position,
                "Invalid types for " + x.position.image);
        }

        return new type(INT_TYPE, 0);
    }

    // ------------------------- Expressao unaria ------------------------
    public type TypeCheckUnaryNode(UnaryNode x) throws SemanticException {
        type t;

        if (x == null) {
            return null;
        }

        t = TypeCheckExpreNode(x.expr);

        // se dimensao > 0, ERRO
        if (t.dim > 0) {
            throw new SemanticException(x.position,
                "Can not use unary " + x.position.image + " for arrays");
        }

        // so int e aceito
        if (t.ty != INT_TYPE) {
            throw new SemanticException(x.position,
                "Incompatible type for unary " + x.position.image);
        }

        return new type(INT_TYPE, 0);
    }

    // -------------------------- Constante inteira ----------------------
    public type TypeCheckIntConstNode(IntConstNode x) throws SemanticException {
        int k;

        if (x == null) {
            return null;
        }

        // tenta transformar imagem em numero inteiro
        try {
            k = Integer.parseInt(x.position.image);
        } catch (NumberFormatException e) { // se deu erro, formato e invalido (possivelmente fora dos limites)
            throw new SemanticException(x.position, "Invalid int constant");
        }

        return new type(INT_TYPE, 0);
    }

    // ------------------------ Constante string ----------------------------
    public type TypeCheckStringConstNode(StringConstNode x) {
        if (x == null) {
            return null;
        }

        return new type(STRING_TYPE, 0);
    }

    // ------------------------------ Constante null --------------------------
    public type TypeCheckNullConstNode(NullConstNode x) {
        if (x == null) {
            return null;
        }

        return new type(NULL_TYPE, 0);
    }

    // -------------------------------- Nome de variavel ------------------
    public type TypeCheckVarNode(VarNode x) throws SemanticException {
        EntryVar p;

        if (x == null) {
            return null;
        }

        // procura variavel na tabela
        p = Curtable.varFind(x.position.image);

        // se nao achou, ERRO
        if (p == null) {
            throw new SemanticException(x.position,
                "Variable " + x.position.image + " not found");
        }

        return new type(p.type, p.dim);
    }

    // ---------------------------- Chamada de metodo ------------------------
    public type TypeCheckCallNode(CallNode x) throws SemanticException {
        EntryClass c;
        EntryMethod m;
        type t1;
        type t2;

        if (x == null) {
            return null;
        }

        // calcula tipo do primeiro filho
        t1 = TypeCheckExpreNode(x.expr);

        // se for array, ERRO
        if (t1.dim > 0) {
            throw new SemanticException(x.position, "Arrays do not have methods");
        }

        // se nao for uma classe, ERRO
        if (!(t1.ty instanceof EntryClass)) {
            throw new SemanticException(x.position,
                "Type " + t1.ty.name + " does not have methods");
        }

        // pega tipos dos argumentos
        t2 = TypeCheckExpreListNode(x.args);

        // procura o metodo desejado na classe t1.ty
        c = (EntryClass) t1.ty;
        m = c.nested.methodFind(x.meth.image, (EntryRec) t2.ty);

        // se nao achou, ERRO
        if (m == null) {
            throw new SemanticException(x.position,
                "Method " + x.meth.image + "(" +
                ((t2.ty == null) ? "" : ((EntryRec) t2.ty).toStr()) +
                ") not found in class " + c.name);
        }

        return new type(m.type, m.dim);
    }

    // --------------------------- Indexacao de variavel ---------------
    public type TypeCheckIndexNode(IndexNode x) throws SemanticException {
        EntryClass c;
        type t1;
        type t2;

        if (x == null) {
            return null;
        }

        // calcula tipo do primeiro filho
        t1 = TypeCheckExpreNode(x.expr1);

        // se nao for array, ERRO
        if (t1.dim <= 0) {
            throw new SemanticException(x.position,
                "Can not index non array variables");
        }

        // pega tipo do indice
        t2 = TypeCheckExpreNode(x.expr2);

        // se nao for int, ERRO
        if ((t2.ty != INT_TYPE) || (t2.dim > 0)) {
            throw new SemanticException(x.position,
                "Invalid type. Index must be int");
        }

        return new type(t1.ty, t1.dim - 1);
    }

    // -------------------------- Acesso a campo de variavel ---------------
    public type TypeCheckDotNode(DotNode x) throws SemanticException {
        EntryClass c;
        EntryVar v;
        type t;

        if (x == null) {
            return null;
        }

        // calcula tipo do primeiro filho
        t = TypeCheckExpreNode(x.expr);

        // se for array, ERRO
        if (t.dim > 0) {
            throw new SemanticException(x.position, "Arrays do not have fields");
        }

        // se nao for uma classe, ERRO
        if (!(t.ty instanceof EntryClass)) {
            throw new SemanticException(x.position,
                "Type " + t.ty.name + " does not have fields");
        }

        // procura a variavel desejada na classe t.ty
        c = (EntryClass) t.ty;
        v = c.nested.varFind(x.field.image);

        // se nao achou, ERRO
        if (v == null) {
            throw new SemanticException(x.position,
                "Variable " + x.field.image + " not found in class " + c.name);
        }

        return new type(v.type, v.dim);
    }

    // --------------------------- Expressao em geral --------------------------
    public type TypeCheckExpreNode(ExpreNode x) throws SemanticException {
        if (x instanceof NewObjectNode) {
            return TypeCheckNewObjectNode((NewObjectNode) x);
        } else if (x instanceof NewArrayNode) {
            return TypeCheckNewArrayNode((NewArrayNode) x);
        } else if (x instanceof RelationalNode) {
            return TypeCheckRelationalNode((RelationalNode) x);
        } else if (x instanceof AddNode) {
            return TypeCheckAddNode((AddNode) x);
        } else if (x instanceof MultNode) {
            return TypeCheckMultNode((MultNode) x);
        } else if (x instanceof UnaryNode) {
            return TypeCheckUnaryNode((UnaryNode) x);
        } else if (x instanceof CallNode) {
            return TypeCheckCallNode((CallNode) x);
        } else if (x instanceof IntConstNode) {
            return TypeCheckIntConstNode((IntConstNode) x);
        } else if (x instanceof StringConstNode) {
            return TypeCheckStringConstNode((StringConstNode) x);
        } else if (x instanceof NullConstNode) {
            return TypeCheckNullConstNode((NullConstNode) x);
        } else if (x instanceof IndexNode) {
            return TypeCheckIndexNode((IndexNode) x);
        } else if (x instanceof DotNode) {
            return TypeCheckDotNode((DotNode) x);
        } else if (x instanceof VarNode) {
            return TypeCheckVarNode((VarNode) x);
        } else {
            return null;
        }
    }

    // --------------------------- Comando em geral -------------------
    public void TypeCheckStatementNode(StatementNode x)
        throws SemanticException {
        if (x instanceof BlockNode) {
            TypeCheckBlockNode((BlockNode) x);
        } else if (x instanceof VarDeclNode) {
            TypeCheckLocalVarDeclNode((VarDeclNode) x);
        } else if (x instanceof AtribNode) {
            TypeCheckAtribNode((AtribNode) x);
        } else if (x instanceof IfNode) {
            TypeCheckIfNode((IfNode) x);
        } else if (x instanceof ForNode) {
            TypeCheckForNode((ForNode) x);
        } else if (x instanceof PrintNode) {
            TypeCheckPrintNode((PrintNode) x);
        } else if (x instanceof NopNode) {
            TypeCheckNopNode((NopNode) x);
        } else if (x instanceof ReadNode) {
            TypeCheckReadNode((ReadNode) x);
        } else if (x instanceof ReturnNode) {
            TypeCheckReturnNode((ReturnNode) x);
        } else if (x instanceof SuperNode) {
            TypeCheckSuperNode((SuperNode) x);
        } else if (x instanceof BreakNode) {
            TypeCheckBreakNode((BreakNode) x);
        }
    }
}
