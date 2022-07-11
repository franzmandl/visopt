.source in.jova  ; P
.class public Example  ; P
.super java/lang/Object  ; P

.field public publicMember I  ; P
.field private privateMember Ljava/lang/String;  ; P

.method public <init>()V  ; P
.limit stack 1  ; P
.limit locals 1  ; P
  aload_0  ; P/B:Example.Example()
  invokespecial java/lang/Object/<init>()V  ; P/B:Example.Example()
  return  ; P/B:Example.Example()
.end method  ; P

.method public allOptimizations()I  ; P
.limit stack 3  ; P
.limit locals 5  ; P
  ldc 0  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:0
  istore 1  ; copy  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:0
  istore 2  ; input  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:0
  istore 3  ; result  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:0
  istore 4  ; two  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:0
  ldc 2  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:1/E:1/
  istore 4  ; two  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:1
  ldc 1  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:2/E:1/0,0
  getstatic Main/scanner Ljava/util/Scanner;  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:2/E:1/0,1
  invokevirtual java/util/Scanner/nextInt()I  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:2/E:1/0,1
  imul  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:2/E:1/0
  ldc 4  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:2/E:1/1
  imul  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:2/E:1/
  istore 2  ; input  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:2
  iload 2  ; input  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:3/E:1/
  istore 1  ; copy  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:3
  iload 2  ; input  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:4/E:1/0,0
  iload 4  ; two  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:4/E:1/0,1,0
  ldc 2  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:4/E:1/0,1,1
  imul  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:4/E:1/0,1
  iadd  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:4/E:1/0
  iload 1  ; copy  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:4/E:1/1,0
  ldc 4  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:4/E:1/1,1
  iadd  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:4/E:1/1
  iadd  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:4/E:1/
  istore 3  ; result  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:4
  iload 3  ; result  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:5/E:0/
  ireturn  ; P/B:Example.allOptimizations()/C:/CS:0/BB/BS:5
.end method  ; P

.method public algebraicSimplifications()I  ; P
.limit stack 2  ; P
.limit locals 2  ; P
  ldc 0  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:0
  istore 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:0
  ldc 183  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:1/E:1/
  istore 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:1
  iload 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:2/E:1/0
  ldc 0  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:2/E:1/1
  iadd  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:2/E:1/
  istore 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:2
  iload 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:3/E:1/0
  ldc 0  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:3/E:1/1
  isub  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:3/E:1/
  istore 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:3
  iload 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:4/E:1/0
  ldc 1  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:4/E:1/1
  imul  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:4/E:1/
  istore 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:4
  iload 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:5/E:1/0
  ldc 1  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:5/E:1/1
  idiv  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:5/E:1/
  istore 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:5
  iload 1  ; x  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:6/E:0/
  ireturn  ; P/B:Example.algebraicSimplifications()/C:/CS:0/BB/BS:6
.end method  ; P

.method public commonSubexpressionElimination()I  ; P
.limit stack 3  ; P
.limit locals 4  ; P
  ldc 0  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:0
  istore 1  ; a  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:0
  istore 2  ; b  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:0
  istore 3  ; result  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:0
  ldc 3  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:1/E:1/0,0
  ldc 4  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:1/E:1/0,1
  imul  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:1/E:1/0
  ldc 2  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:1/E:1/1
  idiv  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:1/E:1/
  istore 1  ; a  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:1
  ldc 3  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:2/E:1/0,0
  ldc 4  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:2/E:1/0,1
  imul  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:2/E:1/0
  ldc 2  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:2/E:1/1
  imul  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:2/E:1/
  istore 2  ; b  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:2
  iload 1  ; a  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:3/E:1/0,0
  ldc 10  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:3/E:1/0,1
  iadd  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:3/E:1/0
  iload 1  ; a  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:3/E:1/1,0
  ldc 10  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:3/E:1/1,1
  iadd  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:3/E:1/1
  imul  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:3/E:1/
  istore 3  ; result  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:3
  iload 3  ; result  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:4/E:0/
  ireturn  ; P/B:Example.commonSubexpressionElimination()/C:/CS:0/BB/BS:4
.end method  ; P

.method public constantFolding()I  ; P
.limit stack 2  ; P
.limit locals 2  ; P
  ldc 0  ; P/B:Example.constantFolding()/C:/CS:0/BB/BS:0
  istore 1  ; result  ; P/B:Example.constantFolding()/C:/CS:0/BB/BS:0
  ldc 2  ; P/B:Example.constantFolding()/C:/CS:0/BB/BS:1/E:1/0,0
  ldc 3  ; P/B:Example.constantFolding()/C:/CS:0/BB/BS:1/E:1/0,1
  imul  ; P/B:Example.constantFolding()/C:/CS:0/BB/BS:1/E:1/0
  ldc 4  ; P/B:Example.constantFolding()/C:/CS:0/BB/BS:1/E:1/1
  iadd  ; P/B:Example.constantFolding()/C:/CS:0/BB/BS:1/E:1/
  istore 1  ; result  ; P/B:Example.constantFolding()/C:/CS:0/BB/BS:1
  iload 1  ; result  ; P/B:Example.constantFolding()/C:/CS:0/BB/BS:2/E:0/
  ireturn  ; P/B:Example.constantFolding()/C:/CS:0/BB/BS:2
.end method  ; P

.method public constantPropagation()I  ; P
.limit stack 2  ; P
.limit locals 3  ; P
  ldc 0  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:0
  istore 1  ; i  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:0
  istore 2  ; result  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:0
  ldc 2  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:1/E:1/
  istore 1  ; i  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:1
  iload 1  ; i  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:2/E:1/0,0
  iload 1  ; i  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:2/E:1/0,1
  imul  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:2/E:1/0
  iload 1  ; i  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:2/E:1/1
  iadd  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:2/E:1/
  istore 2  ; result  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:2
  iload 2  ; result  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:3/E:0/
  ireturn  ; P/B:Example.constantPropagation()/C:/CS:0/BB/BS:3
.end method  ; P

.method public copyPropagation()I  ; P
.limit stack 2  ; P
.limit locals 4  ; P
  ldc 0  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:0
  istore 1  ; i  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:0
  istore 2  ; copy  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:0
  istore 3  ; result  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:0
  getstatic Main/scanner Ljava/util/Scanner;  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:1/E:1/
  invokevirtual java/util/Scanner/nextInt()I  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:1/E:1/
  istore 1  ; i  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:1
  iload 1  ; i  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:2/E:1/
  istore 2  ; copy  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:2
  iload 2  ; copy  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:3/E:1/0,0
  iload 2  ; copy  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:3/E:1/0,1
  imul  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:3/E:1/0
  iload 2  ; copy  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:3/E:1/1
  iadd  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:3/E:1/
  istore 3  ; result  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:3
  iload 3  ; result  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:4/E:0/
  ireturn  ; P/B:Example.copyPropagation()/C:/CS:0/BB/BS:4
.end method  ; P

.method public deadCodeElimination()I  ; P
.limit stack 2  ; P
.limit locals 4  ; P
  ldc 0  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:0
  istore 1  ; a  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:0
  istore 2  ; b  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:0
  istore 3  ; c  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:0
  ldc 201  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:1/E:1/
  istore 1  ; a  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:1
  ldc 302  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:2/E:1/
  istore 2  ; b  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:2
  iload 1  ; a  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:3/E:1/0
  ldc 2  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:3/E:1/1
  imul  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:3/E:1/
  istore 3  ; c  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:3
  iload 1  ; a  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:4/E:1/0
  ldc 3  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:4/E:1/1
  iadd  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:4/E:1/
  istore 2  ; b  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:4
  ldc 0  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:5/E:0/
  ireturn  ; P/B:Example.deadCodeElimination()/C:/CS:0/BB/BS:5
.end method  ; P

.method public reductionInStrength()I  ; P
.limit stack 2  ; P
.limit locals 2  ; P
  ldc 0  ; P/B:Example.reductionInStrength()/C:/CS:0/BB/BS:0
  istore 1  ; result  ; P/B:Example.reductionInStrength()/C:/CS:0/BB/BS:0
  ldc 2  ; P/B:Example.reductionInStrength()/C:/CS:0/BB/BS:1/E:1/0,0
  ldc 3  ; P/B:Example.reductionInStrength()/C:/CS:0/BB/BS:1/E:1/0,1
  imul  ; P/B:Example.reductionInStrength()/C:/CS:0/BB/BS:1/E:1/0
  ldc 4  ; P/B:Example.reductionInStrength()/C:/CS:0/BB/BS:1/E:1/1
  idiv  ; P/B:Example.reductionInStrength()/C:/CS:0/BB/BS:1/E:1/
  istore 1  ; result  ; P/B:Example.reductionInStrength()/C:/CS:0/BB/BS:1
  iload 1  ; result  ; P/B:Example.reductionInStrength()/C:/CS:0/BB/BS:2/E:0/
  ireturn  ; P/B:Example.reductionInStrength()/C:/CS:0/BB/BS:2
.end method  ; P

.method public unreachableCodeElimination()I  ; P
.limit stack 2  ; P
.limit locals 1  ; P
  ldc 1  ; P/B:Example.unreachableCodeElimination()/C:/CS:0/BB/BS:0/E:0/
  ifeq L3  ; P/B:Example.unreachableCodeElimination()/C:/CS:0/BB/BS:0
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Example.unreachableCodeElimination()/C:0,0/CS:0/BB/BS:0/E:0/
  ldc "always true\n"  ; P/B:Example.unreachableCodeElimination()/C:0,0/CS:0/BB/BS:0/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Example.unreachableCodeElimination()/C:0,0/CS:0/BB/BS:0/E:0/
  goto L4  ; P/B:Example.unreachableCodeElimination()/C:0,0/CS:0/BB
L3:  ; P/B:Example.unreachableCodeElimination()/C:0,1/CS:0/BB
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Example.unreachableCodeElimination()/C:0,1/CS:0/BB/BS:0/E:0/
  ldc "unreachable else branch\n"  ; P/B:Example.unreachableCodeElimination()/C:0,1/CS:0/BB/BS:0/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Example.unreachableCodeElimination()/C:0,1/CS:0/BB/BS:0/E:0/
L4:  ; P/B:Example.unreachableCodeElimination()/C:/CS:1/BB
  ldc 0  ; P/B:Example.unreachableCodeElimination()/C:/CS:1/BB/BS:0/E:0/
  ifeq L6  ; P/B:Example.unreachableCodeElimination()/C:/CS:1/BB/BS:0
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Example.unreachableCodeElimination()/C:1,0/CS:0/BB/BS:0/E:0/
  ldc "always false\n"  ; P/B:Example.unreachableCodeElimination()/C:1,0/CS:0/BB/BS:0/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Example.unreachableCodeElimination()/C:1,0/CS:0/BB/BS:0/E:0/
L6:  ; P/B:Example.unreachableCodeElimination()/C:/CS:2/BB
  ldc 0  ; P/B:Example.unreachableCodeElimination()/C:/CS:2/BB/BS:0/E:0/
  ifeq L8  ; P/B:Example.unreachableCodeElimination()/C:/CS:2/BB/BS:0
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Example.unreachableCodeElimination()/C:2/CS:0/BB/BS:0/E:0/
  ldc "unreachable loop\n"  ; P/B:Example.unreachableCodeElimination()/C:2/CS:0/BB/BS:0/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Example.unreachableCodeElimination()/C:2/CS:0/BB/BS:0/E:0/
  goto L6  ; P/B:Example.unreachableCodeElimination()/C:2/CS:0/BB
L8:  ; P/B:Example.unreachableCodeElimination()/C:/CS:3/BB
  ldc 0  ; P/B:Example.unreachableCodeElimination()/C:/CS:3/BB/BS:0/E:0/
  ireturn  ; P/B:Example.unreachableCodeElimination()/C:/CS:3/BB/BS:0
.end method  ; P

.method public flowGraph()I  ; P
.limit stack 2  ; P
.limit locals 3  ; P
  ldc 0  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:0
  istore 1  ; a  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:0
  ldc 0  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:0
  istore 2  ; b  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:0
  ldc 50  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:1/E:1/
  istore 1  ; a  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:1
  ldc 75  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:2/E:1/
  istore 2  ; b  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:2
  iload 1  ; a  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:3/E:0/0
  iload 2  ; b  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:3/E:0/1
  if_icmplt REL2THEN  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:3/E:0/
  ldc 0  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:3/E:0/
  goto REL2END  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:3/E:0/
REL2THEN:  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:3/E:0/
  ldc 1  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:3/E:0/
REL2END:  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:3/E:0/
  ifeq L4  ; P/B:Example.flowGraph()/C:/CS:0/BB/BS:3
  iload 1  ; a  ; P/B:Example.flowGraph()/C:0,0/CS:0/BB/BS:0/E:0/0
  ldc 0  ; P/B:Example.flowGraph()/C:0,0/CS:0/BB/BS:0/E:0/1
  if_icmpne REL1THEN  ; P/B:Example.flowGraph()/C:0,0/CS:0/BB/BS:0/E:0/
  ldc 0  ; P/B:Example.flowGraph()/C:0,0/CS:0/BB/BS:0/E:0/
  goto REL1END  ; P/B:Example.flowGraph()/C:0,0/CS:0/BB/BS:0/E:0/
REL1THEN:  ; P/B:Example.flowGraph()/C:0,0/CS:0/BB/BS:0/E:0/
  ldc 1  ; P/B:Example.flowGraph()/C:0,0/CS:0/BB/BS:0/E:0/
REL1END:  ; P/B:Example.flowGraph()/C:0,0/CS:0/BB/BS:0/E:0/
  ifeq L5  ; P/B:Example.flowGraph()/C:0,0/CS:0/BB/BS:0
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Example.flowGraph()/C:0,0,0,0/CS:0/BB/BS:0/E:0/
  ldc "true\n"  ; P/B:Example.flowGraph()/C:0,0,0,0/CS:0/BB/BS:0/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Example.flowGraph()/C:0,0,0,0/CS:0/BB/BS:0/E:0/
  goto L5  ; P/B:Example.flowGraph()/C:0,0,0,0/CS:0/BB
L4:  ; P/B:Example.flowGraph()/C:0,1/CS:0/BB
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Example.flowGraph()/C:0,1/CS:0/BB/BS:0/E:0/
  ldc "false\n"  ; P/B:Example.flowGraph()/C:0,1/CS:0/BB/BS:0/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Example.flowGraph()/C:0,1/CS:0/BB/BS:0/E:0/
L5:  ; P/B:Example.flowGraph()/C:/CS:1/BB
  iload 1  ; a  ; P/B:Example.flowGraph()/C:/CS:1/BB/BS:0/E:1/0
  ldc 1  ; P/B:Example.flowGraph()/C:/CS:1/BB/BS:0/E:1/1
  iadd  ; P/B:Example.flowGraph()/C:/CS:1/BB/BS:0/E:1/
  istore 1  ; a  ; P/B:Example.flowGraph()/C:/CS:1/BB/BS:0
L6:  ; P/B:Example.flowGraph()/C:/CS:2/BB
  iload 1  ; a  ; P/B:Example.flowGraph()/C:/CS:2/BB/BS:0/E:0/0
  iload 2  ; b  ; P/B:Example.flowGraph()/C:/CS:2/BB/BS:0/E:0/1
  if_icmpgt REL3THEN  ; P/B:Example.flowGraph()/C:/CS:2/BB/BS:0/E:0/
  ldc 0  ; P/B:Example.flowGraph()/C:/CS:2/BB/BS:0/E:0/
  goto REL3END  ; P/B:Example.flowGraph()/C:/CS:2/BB/BS:0/E:0/
REL3THEN:  ; P/B:Example.flowGraph()/C:/CS:2/BB/BS:0/E:0/
  ldc 1  ; P/B:Example.flowGraph()/C:/CS:2/BB/BS:0/E:0/
REL3END:  ; P/B:Example.flowGraph()/C:/CS:2/BB/BS:0/E:0/
  ifeq L8  ; P/B:Example.flowGraph()/C:/CS:2/BB/BS:0
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Example.flowGraph()/C:2/CS:0/BB/BS:0/E:0/
  ldc "while\n"  ; P/B:Example.flowGraph()/C:2/CS:0/BB/BS:0/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Example.flowGraph()/C:2/CS:0/BB/BS:0/E:0/
  goto L6  ; P/B:Example.flowGraph()/C:2/CS:0/BB
L8:  ; P/B:Example.flowGraph()/C:/CS:3/BB
  ldc 0  ; P/B:Example.flowGraph()/C:/CS:3/BB/BS:0/E:0/
  ireturn  ; P/B:Example.flowGraph()/C:/CS:3/BB/BS:0
.end method  ; P
.source in.jova  ; P
.class public Main  ; P
.super java/lang/Object  ; P

.field public static scanner Ljava/util/Scanner;  ; P

.method public static main([Ljava/lang/String;)V  ; P
.limit stack 3  ; P
.limit locals 2  ; P
  new java/util/Scanner  ; P/B:Main.main()
  dup  ; P/B:Main.main()
  getstatic java/lang/System/in Ljava/io/InputStream;  ; P/B:Main.main()
  invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V  ; P/B:Main.main()
  putstatic Main/scanner Ljava/util/Scanner;  ; P/B:Main.main()
  aconst_null  ; P/B:Main.main()/C:/CS:0/BB/BS:0
  astore 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:0
  new Example  ; P/B:Main.main()/C:/CS:0/BB/BS:1/E:1/
  dup  ; P/B:Main.main()/C:/CS:0/BB/BS:1/E:1/
  invokespecial Example/<init>()V  ; P/B:Main.main()/C:/CS:0/BB/BS:1/E:1/
  astore 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:1
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:2/E:0/
  aload 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:2/E:0/0,0
  invokevirtual Example/allOptimizations()I  ; P/B:Main.main()/C:/CS:0/BB/BS:2/E:0/0
  invokevirtual java/io/PrintStream/print(I)V  ; P/B:Main.main()/C:/CS:0/BB/BS:2/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:3/E:0/
  ldc "\n"  ; P/B:Main.main()/C:/CS:0/BB/BS:3/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Main.main()/C:/CS:0/BB/BS:3/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:4/E:0/
  aload 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:4/E:0/0,0
  invokevirtual Example/algebraicSimplifications()I  ; P/B:Main.main()/C:/CS:0/BB/BS:4/E:0/0
  invokevirtual java/io/PrintStream/print(I)V  ; P/B:Main.main()/C:/CS:0/BB/BS:4/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:5/E:0/
  ldc "\n"  ; P/B:Main.main()/C:/CS:0/BB/BS:5/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Main.main()/C:/CS:0/BB/BS:5/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:6/E:0/
  aload 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:6/E:0/0,0
  invokevirtual Example/commonSubexpressionElimination()I  ; P/B:Main.main()/C:/CS:0/BB/BS:6/E:0/0
  invokevirtual java/io/PrintStream/print(I)V  ; P/B:Main.main()/C:/CS:0/BB/BS:6/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:7/E:0/
  ldc "\n"  ; P/B:Main.main()/C:/CS:0/BB/BS:7/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Main.main()/C:/CS:0/BB/BS:7/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:8/E:0/
  aload 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:8/E:0/0,0
  invokevirtual Example/constantFolding()I  ; P/B:Main.main()/C:/CS:0/BB/BS:8/E:0/0
  invokevirtual java/io/PrintStream/print(I)V  ; P/B:Main.main()/C:/CS:0/BB/BS:8/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:9/E:0/
  ldc "\n"  ; P/B:Main.main()/C:/CS:0/BB/BS:9/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Main.main()/C:/CS:0/BB/BS:9/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:10/E:0/
  aload 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:10/E:0/0,0
  invokevirtual Example/constantPropagation()I  ; P/B:Main.main()/C:/CS:0/BB/BS:10/E:0/0
  invokevirtual java/io/PrintStream/print(I)V  ; P/B:Main.main()/C:/CS:0/BB/BS:10/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:11/E:0/
  ldc "\n"  ; P/B:Main.main()/C:/CS:0/BB/BS:11/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Main.main()/C:/CS:0/BB/BS:11/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:12/E:0/
  aload 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:12/E:0/0,0
  invokevirtual Example/copyPropagation()I  ; P/B:Main.main()/C:/CS:0/BB/BS:12/E:0/0
  invokevirtual java/io/PrintStream/print(I)V  ; P/B:Main.main()/C:/CS:0/BB/BS:12/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:13/E:0/
  ldc "\n"  ; P/B:Main.main()/C:/CS:0/BB/BS:13/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Main.main()/C:/CS:0/BB/BS:13/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:14/E:0/
  aload 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:14/E:0/0,0
  invokevirtual Example/deadCodeElimination()I  ; P/B:Main.main()/C:/CS:0/BB/BS:14/E:0/0
  invokevirtual java/io/PrintStream/print(I)V  ; P/B:Main.main()/C:/CS:0/BB/BS:14/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:15/E:0/
  ldc "\n"  ; P/B:Main.main()/C:/CS:0/BB/BS:15/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Main.main()/C:/CS:0/BB/BS:15/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:16/E:0/
  aload 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:16/E:0/0,0
  invokevirtual Example/reductionInStrength()I  ; P/B:Main.main()/C:/CS:0/BB/BS:16/E:0/0
  invokevirtual java/io/PrintStream/print(I)V  ; P/B:Main.main()/C:/CS:0/BB/BS:16/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:17/E:0/
  ldc "\n"  ; P/B:Main.main()/C:/CS:0/BB/BS:17/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Main.main()/C:/CS:0/BB/BS:17/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:18/E:0/
  aload 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:18/E:0/0,0
  invokevirtual Example/flowGraph()I  ; P/B:Main.main()/C:/CS:0/BB/BS:18/E:0/0
  invokevirtual java/io/PrintStream/print(I)V  ; P/B:Main.main()/C:/CS:0/BB/BS:18/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:19/E:0/
  ldc "\n"  ; P/B:Main.main()/C:/CS:0/BB/BS:19/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Main.main()/C:/CS:0/BB/BS:19/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:20/E:0/
  aload 1  ; instance  ; P/B:Main.main()/C:/CS:0/BB/BS:20/E:0/0,0
  invokevirtual Example/unreachableCodeElimination()I  ; P/B:Main.main()/C:/CS:0/BB/BS:20/E:0/0
  invokevirtual java/io/PrintStream/print(I)V  ; P/B:Main.main()/C:/CS:0/BB/BS:20/E:0/
  getstatic java/lang/System/out Ljava/io/PrintStream;  ; P/B:Main.main()/C:/CS:0/BB/BS:21/E:0/
  ldc "\n"  ; P/B:Main.main()/C:/CS:0/BB/BS:21/E:0/0
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V  ; P/B:Main.main()/C:/CS:0/BB/BS:21/E:0/
  ldc 0  ; P/B:Main.main()/C:/CS:0/BB/BS:22/E:0/
  invokestatic java/lang/System/exit(I)V  ; P/B:Main.main()/C:/CS:0/BB
  return  ; P/B:Main.main()/C:/CS:0/BB
.end method  ; P