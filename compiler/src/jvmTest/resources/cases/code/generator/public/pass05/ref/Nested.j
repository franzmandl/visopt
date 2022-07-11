.source noSource
.class public Nested
.super java/lang/Object
.method public <init>()V
  aload_0
  invokespecial java/lang/Object/<init>()V
  return
.end method

.method public fortyTwo()I
.limit stack 10
.limit locals 1
  ldc 42
  ireturn
.end method

.method public nestedIsland()I
.limit stack 10
.limit locals 3
  ldc 4
  ldc 8
  ldc 15
  imul
  ldc 16
  ldc 23
  isub
  imul
  aload_0
  invokevirtual Nested/fortyTwo()I
  idiv
  iadd
  istore 1
  ldc "The result of the nested expression is "
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 2
  iload 1
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(I)V
  ldc 0
  istore 2
  ldc 0
  ireturn
.end method

