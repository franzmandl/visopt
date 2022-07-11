.source noSource
.class public Main
.super java/lang/Object
.method public static main([Ljava/lang/String;)V
.limit stack 10
.limit locals 2
  ldc "Hi, I am boolean: "
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 1
  ldc 1
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Z)V
  ldc 0
  istore 1
  ldc "\nHi boolean, I am number: "
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 1
  ldc 42
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(I)V
  ldc 0
  istore 1
  ldc 0
  invokestatic java/lang/System/exit(I)V
  return
.end method

