.source noSource
.class public Evaluation
.super java/lang/Object
.method public <init>()V
  aload_0
  invokespecial java/lang/Object/<init>()V
  return
.end method

.method public init()I
.limit stack 10
.limit locals 1
  ldc "Please dont forget to evaluate this course!\n"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  ireturn
.end method

.method public setEvalMsg(Ljava/lang/String;I)I
.limit stack 10
.limit locals 4
  ldc "New evaluation for course "
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 3
  iload 2
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(I)V
  ldc 0
  istore 3
  ldc "\n"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 3
  aload 1
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 3
  ldc 0
  ireturn
.end method

